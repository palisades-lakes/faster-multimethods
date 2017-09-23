/** Fork of clojure.lang.MultiFn for performance experiments.
 *  Original MultiFn.java:
 *  
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Sep 13, 2007 */

package palisades.lakes.multimethods.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IRef;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import palisades.lakes.multimethods.java.signature.Signature;

/**  Semantic changes to clojure.lang.MultiFn.
 * 
 * 1) Make <code>prefers</code> transitive:
 * <code>(prefer-method f a b)</code> and 
 * <code>(prefer-method f b c)</code> should imply
 * <code>(prefer-method f a c)</code>, but that's not 
 * not true in <code>clojure.lang.MultiFn.prefers(x,y)</code>.
 * 
 * 2) The logic in <code>clojure.lang.MultiFn.prefers(x,y)</code>
 * appears to imply that 
 * <code>(prefer-method x some-ancestor-of-y)</code>
 * implies <code>(prefer-method x y)</code>
 * which make no sense to me. 
 * 
 * 3) <code>clojure.lang.MultFn.prefers()</code> ignores
 * the multimethod's hierarchy, so behavior is different between
 * multimethods that use <code>global-hierarchy</code> vs local.
 * Changed to explicitly use the local hierarchy in preference
 * evaluation.
 * 
 * Performance changes to clojure.lang.MultiFn.
 *
 * 1) Replace persistent data structures with simple
 * HashMaps, etc., requiring some discipline to use
 * mutable objects as immutable.
 * Eventually, should replace with minimal immutable version.
 * 
 * 2) Permit Signature as a dispatch value.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-20
 * @version 2017-09-22
 */
@SuppressWarnings("unchecked")
public final class MultiFnWithHierarchy extends AFn implements MultiFn {

  private final IFn dispatchFn;
  private final String name;
  private final ReentrantReadWriteLock rw;
  private volatile Map methodTable;
  private volatile Map preferTable;
  private volatile Map methodCache;
  private final Object defaultDispatch;
  private final IRef hierarchy;
  private volatile Map cachedHierarchy;

  //--------------------------------------------------------------

  public MultiFnWithHierarchy (final String n, 
                               final IFn dispatchF, 
                               final Object defaultd, 
                               final IRef hierarky) {
    rw = new ReentrantReadWriteLock();
    assert (null != n) && (! n.isEmpty());
    name = n;
    assert null != dispatchF;
    dispatchFn = dispatchF;
    methodTable = Collections.emptyMap();
    methodCache = Collections.emptyMap();
    preferTable = Collections.emptyMap();
    // can be null?
    defaultDispatch = defaultd;
    assert null != hierarky;
    hierarchy = hierarky;
    cachedHierarchy = null; }

  //--------------------------------------------------------------

  @Override
  public IPersistentMap getMethodTable () {
    return PersistentHashMap.create(methodTable); }

  @Override
  public IPersistentMap getPreferTable () {
    // convert values to IPersistentSet
    final Set ks = preferTable.keySet();
    final Object[] kvs = new Object[2 * ks.size()];
    int i = 0;
    for (final Object k : ks) {
      kvs[i++] = k;
      final Set v = (Set) preferTable.get(k);
      kvs[i++] = PersistentHashSet.create(v.toArray()); }
    return PersistentHashMap.create(kvs); }

  //--------------------------------------------------------------

  @Override
  public MultiFn reset () {
    rw.writeLock().lock();
    try {
      methodTable = Collections.emptyMap();
      methodCache = Collections.emptyMap();
      preferTable = Collections.emptyMap();
      cachedHierarchy = null;
      return this; }
    finally { rw.writeLock().unlock(); } }

  //--------------------------------------------------------------

  private static final Map assoc (final Map m,
                                  final Object k,
                                  final Object v) {
    final HashMap b = new HashMap(m);
    b.put(k,v);
    return b; }

  private static final Map dissoc (final Map m,
                                   final Object k) {
    final Map b = new HashMap(m);
    b.remove(k);
    return b; }

  private static final Set add (final Set s,
                                final Object v) {
    if (null == s) { return Collections.singleton(v); }
    final Set b = new HashSet(s);
    b.add(v);
    return b; }

  private static final Map add (final Map m,
                                final Object k,
                                final Object v) {
    final Map b = new HashMap(m);
    b.put(k,add((Set) b.get(k),v));
    return b; }

  //--------------------------------------------------------------

  @Override
  public MultiFn addMethod (final Object dispatch,
                            final IFn method) {
    rw.writeLock().lock();
    try {
      methodTable = assoc(methodTable,dispatch,method);
      resetCache();
      return this; }
    finally { rw.writeLock().unlock(); } }

  @Override
  public MultiFn removeMethod (final Object dispatch) {
    rw.writeLock().lock();
    try {
      methodTable = dissoc(methodTable,dispatch);
      resetCache();
      return this; }
    finally { rw.writeLock().unlock(); } }

  //--------------------------------------------------------------

  private static final Var parents = RT.var("clojure.core","parents");

  private boolean prefers (final Map hierarky,
                           final Object x, 
                           final Object y) {

    final Set xprefs = (Set) preferTable.get(x);
    if (xprefs != null) {

      // is there an explicit prefer-method entry for (x,y)?
      if (xprefs.contains(y)) { return true; }

      // transitive closure of prefer-method relation
      // is x preferred to anything that is preferred to y?
      for (final Object xx : xprefs) {
        if (prefers(hierarky,xx,y)) { return true; } } }

    // For multi-arity dispatch functions, we need to check the
    // keys of the preferTable.
    // TODO: does this make the next loop unnecessary?
    for (final Object k : preferTable.keySet()) {
      if ((!x.equals(k)) 
        && isA(hierarky,x,k) 
        && prefers(hierarky,k,y)) {
        return true; } }

    // Note: this only works for atomic dispatch values
    // are any of x's parents preferred to y?
    // parents either in the multimethod's hierarchy or thru 
    // Class.isAssignableFrom
    for (ISeq ps = RT.seq(parents.invoke(hierarky,x)); 
      ps != null;
      ps = ps.next()) {
      if (prefers(hierarky,ps.first(),y)) { return true; } }

    return false; }

  //--------------------------------------------------------------

  @Override
  public MultiFn preferMethod (final Object dispatchX,
                               final Object dispatchY) {
    rw.writeLock().lock();
    try {
      if (prefers(cachedHierarchy,dispatchY,dispatchX)) { 
        throw new IllegalStateException(
          String.format(
            "Preference conflict in multimethod '%s':" +
              "%s is already preferred to %s",
              name,dispatchY,dispatchX)); }
      preferTable = add(preferTable,dispatchX,dispatchY);
      resetCache();
      return this; }
    finally { rw.writeLock().unlock(); } }

  //--------------------------------------------------------------

  private static final Set<Class> collectInterfaces (final Class c,
                                                     final Set<Class> interfaces) {
    for (Class ci=c; ci!=null; ci=ci.getSuperclass()) {
      for (final Class i : ci.getInterfaces()) {
        if (interfaces.add(i)) {
          collectInterfaces(i, interfaces); } } }
    return interfaces; }

  private static final Set<Class> interfaces (final Class c) {
    assert null != c;
    return collectInterfaces(c, new HashSet()); }

  private static final List<Class> superclasses (final Class c) {
    assert null != c;
    final List<Class> supers = new ArrayList();
    for (Class ci = c.getSuperclass(); ci != null; ci=ci.getSuperclass()) {
      supers.add(ci); }
    return supers; }

  //--------------------------------------------------------------

  private static final Keyword ANCESTORS = Keyword.intern("ancestors");

  //--------------------------------------------------------------
  /** See clojure.core/isa?
   */

  private static final boolean isA (final Map h,
                                    final Signature child,
                                    final Signature parent) {
    final int n = child.size();
    if (n != parent.size()) { return false; }
    if (parent.isAssignableFrom(child)) { return true; }
    for (int i = 0; i < n; i++) {
      if (!isA(h,child.get(i),parent.get(i))) { return false; } }
    return true; } 

  private static final boolean isA (final Map h,
                                    final IPersistentVector child,
                                    final IPersistentVector parent) {
    final int n = child.length();
    if (n != parent.length()) { return false; }
    for (int i = 0; i < n; i++) {
      if (!isA(h,child.nth(i),parent.nth(i))) { return false; } }
    return true; } 

  private static final boolean isA (final Map h,
                                    final Object child,
                                    final Object parent) {

    if (child.equals(parent)) { return true; }

    if ((child instanceof Class) && (parent instanceof Class)) {
      // Note: not correct for primitive types like Float/TYPE
      if (((Class) parent).isAssignableFrom((Class) child)) {
        return true; } }

    final Map ancestorMap = (Map) h.get(ANCESTORS);
    final Set ancestors = (Set) ancestorMap.get(child);
    if ((null != ancestors) && ancestors.contains(parent)) {
      return true; }

    if (child instanceof Class) {
      for (final Class c : superclasses((Class) child)) {
        final Set a = (Set) ancestorMap.get(c);
        if ((null != a) && a.contains(parent)) { return true; } }
      for (final Class c : interfaces((Class) child)) {
        final Set a = (Set) ancestorMap.get(c);
        if ((null != a) && a.contains(parent)) { return true; } } }

    if ((child instanceof Signature) 
      && (parent instanceof Signature)) {
      return isA(h, (Signature) child, (Signature) parent); }

    if ((child instanceof IPersistentVector) 
      && (parent instanceof IPersistentVector)) {
      return isA(h,
        (IPersistentVector) child, 
        (IPersistentVector) parent); }
    return false; }

  //--------------------------------------------------------------

  private boolean dominates (final Map hierarky,
                             final Object x, 
                             final Object y) {
    return prefers(hierarky,x,y) || isA(hierarky,x,y); }

  //--------------------------------------------------------------

  private Map resetCache () {
    rw.writeLock().lock();
    try {
      methodCache = methodTable;
      cachedHierarchy = (Map) hierarchy.deref();
      return methodCache; }
    finally { rw.writeLock().unlock(); } }

  //--------------------------------------------------------------

  private final Set updateMinima (final Map h,
                                  final Map.Entry e0,
                                  final Set<Map.Entry> minima) {
    boolean add = true;
    final Set<Map.Entry> updated = new HashSet(minima.size());
    final Object k0 = e0.getKey();
    for (final Map.Entry e : minima) {
      final Object k = e.getKey();
      if (dominates(h,k,k0)) { add = false; }
      if (! dominates(h,k0,k)) { updated.add(e); } } 
    if (add) { updated.add(e0); }
    return updated; }
  
  private static final Map.Entry first (final Set<Map.Entry> i) {
    return i.iterator().next(); }
  
  private IFn findAndCacheBestMethod (final Object dispatch) {
    rw.readLock().lock();
    Object bestValue;
    final Map mt = methodTable;
    final Map pt = preferTable;
    final Map ch = cachedHierarchy;
    try {
      Set<Map.Entry> minima = new HashSet(); // should be immutable?
      for (final Object o : methodTable.entrySet()) {
        final Map.Entry e = (Map.Entry) o;
        if (isA(ch,dispatch,e.getKey())) {
          minima = updateMinima(ch,e,minima); } } 
      if (minima.isEmpty()) { 
        bestValue = methodTable.get(defaultDispatch);
        if (bestValue == null) { return null; } }
      else if (1 != minima.size()) {
        throw new IllegalArgumentException(
          String.format(
            "Multiple methods in multimethod '%s' " + 
              "match dispatch value: %s -> %s, " +
              "and none is preferred",
              name, dispatch, minima));  }
      else {
        bestValue = first(minima).getValue(); } }
    finally { rw.readLock().unlock(); }

    // ensure basis has stayed stable throughout, else redo
    rw.writeLock().lock();
    try {
      if ((mt == methodTable) && 
        (pt == preferTable) &&
        (ch == cachedHierarchy) &&
        (cachedHierarchy == hierarchy.deref())) {
        methodCache = assoc(methodCache,dispatch,bestValue);
        return (IFn) bestValue; }
      resetCache();
      return findAndCacheBestMethod(dispatch); }
    finally { rw.writeLock().unlock(); } }

  //--------------------------------------------------------------

  @Override
  public IFn getMethod (final Object dispatch) {
    if(cachedHierarchy != hierarchy.deref()) { resetCache(); }
    final IFn targetFn = (IFn) methodCache.get(dispatch);
    if (targetFn != null) { return targetFn; }
    return findAndCacheBestMethod(dispatch); }

  private IFn getFn (final Object dispatch) {
    final IFn targetFn = getMethod(dispatch);
    if (targetFn == null) { 
      throw new IllegalArgumentException(
        String.format(
          "No method in multimethod '%s' for dispatch value: %s",
          name,dispatch)); }
    return targetFn; }

  //--------------------------------------------------------------
  // IFn interface
  //--------------------------------------------------------------

  @Override
  public final Object invoke () {
    return getFn(dispatchFn.invoke()).invoke();
  }

  @Override
  public final Object invoke (Object arg1) {
    return getFn(dispatchFn.invoke(arg1))
      .invoke(Util.ret1(arg1,arg1 = null));
  }

  @Override
  public final Object invoke (Object arg1, 
                              Object arg2) {
    final Object k = dispatchFn.invoke(arg1,arg2);
    final IFn f = getMethod(k);
    assert null != f;
    return f.invoke(
      Util.ret1(arg1,arg1 = null),
      Util.ret1(arg2,arg2 = null)); }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3)).invoke(
      Util.ret1(arg1,arg1 = null),
      Util.ret1(arg2,arg2 = null),
      Util.ret1(arg3,arg3 = null)); }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4)).invoke(
      Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
      Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5))
      .invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6))
      .invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7) {
    return getFn(
      dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,arg7))
      .invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8) {
    return getFn(
      dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8))
      .invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9)).invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10)).invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null),
        Util.ret1(arg10,arg10 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11,
                              Object arg12) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14,
                              Object arg15) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16))
      .invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null),
        Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16, Object arg17) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,
      arg17)).invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null),
        Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null),
        Util.ret1(arg17,arg17 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16, Object arg17,
                              Object arg18) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,
      arg17,arg18)).invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null),
        Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null),
        Util.ret1(arg17,arg17 = null),
        Util.ret1(arg18,arg18 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16, Object arg17, Object arg18,
                              Object arg19) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,
      arg17,arg18,arg19)).invoke(Util.ret1(arg1,arg1 = null),
        Util.ret1(arg2,arg2 = null),Util.ret1(arg3,arg3 = null),
        Util.ret1(arg4,arg4 = null),Util.ret1(arg5,arg5 = null),
        Util.ret1(arg6,arg6 = null),Util.ret1(arg7,arg7 = null),
        Util.ret1(arg8,arg8 = null),Util.ret1(arg9,arg9 = null),
        Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null),
        Util.ret1(arg17,arg17 = null),
        Util.ret1(arg18,arg18 = null),
        Util.ret1(arg19,arg19 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16, Object arg17, Object arg18,
                              Object arg19, Object arg20) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,
      arg17,arg18,arg19,arg20)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null),
        Util.ret1(arg17,arg17 = null),
        Util.ret1(arg18,arg18 = null),
        Util.ret1(arg19,arg19 = null),
        Util.ret1(arg20,arg20 = null));
  }

  @Override
  public final Object invoke (Object arg1, Object arg2, Object arg3,
                              Object arg4, Object arg5, Object arg6,
                              Object arg7, Object arg8, Object arg9,
                              Object arg10, Object arg11, Object arg12,
                              Object arg13, Object arg14, Object arg15,
                              Object arg16, Object arg17, Object arg18,
                              Object arg19, Object arg20,
                              final Object... args) {
    return getFn(dispatchFn.invoke(arg1,arg2,arg3,arg4,arg5,arg6,
      arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,
      arg17,arg18,arg19,arg20,args)).invoke(
        Util.ret1(arg1,arg1 = null),Util.ret1(arg2,arg2 = null),
        Util.ret1(arg3,arg3 = null),Util.ret1(arg4,arg4 = null),
        Util.ret1(arg5,arg5 = null),Util.ret1(arg6,arg6 = null),
        Util.ret1(arg7,arg7 = null),Util.ret1(arg8,arg8 = null),
        Util.ret1(arg9,arg9 = null),Util.ret1(arg10,arg10 = null),
        Util.ret1(arg11,arg11 = null),
        Util.ret1(arg12,arg12 = null),
        Util.ret1(arg13,arg13 = null),
        Util.ret1(arg14,arg14 = null),
        Util.ret1(arg15,arg15 = null),
        Util.ret1(arg16,arg16 = null),
        Util.ret1(arg17,arg17 = null),
        Util.ret1(arg18,arg18 = null),
        Util.ret1(arg19,arg19 = null),
        Util.ret1(arg20,arg20 = null),args);
  }

  //--------------------------------------------------------------
}
