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

package faster.multimethods.java;

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

import faster.multimethods.java.signature.Signature;

/**
 * Changes from clojure.lang.MultiFn.
 *
 * 1) Replace persistent data structures with simple
 * unmodifiable HashMaps, etc., requiring some discipline to use
 * mutable objects as immutable.
 * Eventually, should replace with minimal immutable version.
 * 
 * 2) Permit Signature as a dispatch value.
 *
 * 3) All fields private.
 * 
 * 4) Non-volatile cache. Might cause redundant updates to the 
 * cache, but shouldn't break anything.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-20
 * @version 2017-08-04
 */
@SuppressWarnings("unchecked")
public final class MultiFnWithHierarchy extends AFn implements MultiFn {

  private final IFn dispatchFn;
  private final String name;
  private final ReentrantReadWriteLock rw;
  private volatile Map methodTable;
  private volatile Map preferTable;
  private Map methodCache;
  private final Object defaultDispatch;
  private final IRef hierarchy;
  private volatile Object cachedHierarchy;

  private static final Var parents = RT.var("clojure.core","parents");

  //--------------------------------------------------------------

  public MultiFnWithHierarchy (final String n, 
                               final IFn dispatchF, 
                               final Object defaultd, 
                               final IRef hierarky) {
    rw = new ReentrantReadWriteLock();
    name = n;
    dispatchFn = dispatchF;
    methodTable = Collections.emptyMap();
    methodCache = Collections.emptyMap();
    preferTable = Collections.emptyMap();
    defaultDispatch = defaultd;
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

  @Override
  public MultiFn preferMethod (final Object dispatchX,
                               final Object dispatchY) {
    rw.writeLock().lock();
    try {
      if (prefers(dispatchY,
        dispatchX)) { throw new IllegalStateException(
          String.format(
            "Preference conflict in multimethod '%s':" +
              "%s is already preferred to %s",
              name,dispatchY,dispatchX)); }
      preferTable = add(preferTable,dispatchX,dispatchY);
      resetCache();
      return this; }
    finally { rw.writeLock().unlock(); } }

  private boolean prefers (final Object x, 
                           final Object y) {
    final Set xprefs = (Set) preferTable.get(x);
    if ((xprefs != null) && xprefs.contains(y)) { return true; }
    for (ISeq ps = RT.seq(parents.invoke(y)); 
      ps != null;
      ps = ps.next()) {
      if (prefers(x,ps.first())) { return true; } }
    for (ISeq ps = RT.seq(parents.invoke(x)); 
      ps != null;
      ps = ps.next()) {
      if (prefers(ps.first(),y)) { return true; } }
    return false; }

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

  private boolean isA (final Object child, 
                       final Object parent) {
    return isA((Map) hierarchy.deref(), child, parent); }

  //--------------------------------------------------------------

  private boolean dominates (final Object x, 
                             final Object y) {
    return prefers(x,y) || isA(x,y); }

  //--------------------------------------------------------------

  private Map resetCache () {
    rw.writeLock().lock();
    try {
      methodCache = methodTable;
      cachedHierarchy = hierarchy.deref();
      return methodCache; }
    finally { rw.writeLock().unlock(); } }

  private IFn findAndCacheBestMethod (final Object dispatch) {
    rw.readLock().lock();
    Object bestValue;
    final Map mt = methodTable;
    final Map pt = preferTable;
    final Object ch = cachedHierarchy;
    try {
      Map.Entry bestEntry = null;
      for (final Object o : methodTable.entrySet()) {
        final Map.Entry e = (Map.Entry) o;
        if (isA(dispatch,e.getKey())) {
          if ((bestEntry == null)
            || dominates(e.getKey(),bestEntry.getKey())) {
            bestEntry = e; }
          if(! dominates(bestEntry.getKey(), e.getKey())) {
            throw new IllegalArgumentException(
              String.format(
                "Multiple methods in multimethod '%s' " + 
                  "match dispatch value: %s -> %s and %s, " +
                  "and neither is preferred",
                  name, dispatch, e.getKey(),
                  bestEntry.getKey())); } } }
      if (null == bestEntry) { 
        bestValue = methodTable.get(defaultDispatch);
        if (bestValue == null) { return null; } }
      else { 
        bestValue = bestEntry.getValue(); } }
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
