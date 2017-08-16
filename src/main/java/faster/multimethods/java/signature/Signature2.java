package faster.multimethods.java.signature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** A pair of classes, for optimizing multimethod dispatch
 * functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-02
 * @version 2017-07-29
 */

@SuppressWarnings("unchecked")
public final class Signature2 implements Signature {

  private final int hash;
  private final Class class0;
  private final Class class1;

  public final Class getClass (final int i) {
    if (0 == i) { return class0; }
    if (1 == i) { return class1; }
    throw new IllegalArgumentException(
      "Only 2 classes in a " + getClass().getSimpleName() + ": "
      + i); }
  //--------------------------------------------------------------

  public final boolean isAssignableFrom (final Signature2 that) {
    return
      class0.isAssignableFrom(that.class0)
      &&
      class1.isAssignableFrom(that.class1); }

  //--------------------------------------------------------------

  @Override
  public final int size () { return 2; }

  @Override
  public final boolean isAssignableFrom (final Signature that) {
    if (that instanceof Signature2) {
      return isAssignableFrom((Signature2) that); }
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class k) {
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class k0,
                                         final Class k1) {
    return 
      class0.isAssignableFrom(k0) 
      && 
      class1.isAssignableFrom(k1); }

  @Override
  public final boolean isAssignableFrom (final Class k0,
                                         final Class k1,
                                         final Class k2) {
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class... ks) {
    return false; }

  //--------------------------------------------------------------

  @Override
  public final boolean equiv (final Class k0,
                              final Class k1) {
    return class0.equals(k0) && class1.equals(k1); }

  @Override
  public final boolean equiv (final Class k0,
                              final Class k1,
                              final Class k2) {
    return false; }

  @Override
  public final boolean equiv (final Class... ks) {
    return false; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return hash; }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (that instanceof Signature2) {
      return
        hash == that.hashCode()
        &&
        class0.equals(((Signature2) that).class0)
        &&
        class1.equals(((Signature2) that).class1); }
    return false; }

  @Override
  public final String toString () {
  return class0.getSimpleName() + "_" + class1.getSimpleName(); }

  //--------------------------------------------------------------
  // TODO: memoize singleton instances?

  public Signature2 (final Class k0, final Class k1) {
    hash = (37*((37*17) + k0.hashCode())) + k1.hashCode();
    class0 = k0; 
    class1 = k1; }

  public static final Signature2 get (final Class k0,
                                      final Class k1) {
    return new Signature2(k0,k1); }

  public static final Signature2 extract (final Object k0,
                                          final Object k1) {
    return new Signature2(k0.getClass(),k1.getClass()); }

  //--------------------------------------------------------------
  // List interface
  //--------------------------------------------------------------

  @Override
  public boolean add (final Object x) {
    throw new UnsupportedOperationException(
      "add" + " unsupported for " + getClass()); }

  @Override
  public void add (final int x0, final Object x1) {
    throw new UnsupportedOperationException(
      "add" + " unsupported for " + getClass()); }

  @Override
  public boolean addAll (final Collection x) {
    throw new UnsupportedOperationException(
      "addAll" + " unsupported for " + getClass()); }

  @Override
  public boolean addAll (final int x0, final Collection x1) {
    throw new UnsupportedOperationException(
      "addAll" + " unsupported for " + getClass()); }

  @Override
  public void clear () {
    throw new UnsupportedOperationException(
      "clear" + " unsupported for " + getClass()); }

  @Override
  public boolean contains (final Object x) {
    return class0.equals(x) || class1.equals(x); }

  @Override
  public boolean containsAll (final Collection x) {
    for (final Object o : x) {
      if (! contains(o)) { return false; } }
    return true; }

  @Override
  public Object get (final int x) {
    if (0 == x) { return class0; }
    if (1 == x) { return class1; }
    throw new IndexOutOfBoundsException(
      this + " has 2 elements: "  + x); }

  @Override
  public int indexOf (final Object x) {
    if (class0.equals(x)) { return 0; }
    if (class1.equals(x)) { return 1; }
    return -1; }

  @Override
  public boolean isEmpty () { return false;  }

  @Override
  public int lastIndexOf (final Object arg0) { 
    if (class1.equals(arg0)) { return 1; }
    if (class0.equals(arg0)) { return 0; }
    return -1; }

  @Override
  public boolean remove (final Object x) {
    throw new UnsupportedOperationException(
      "remove" + " unsupported for " + getClass()); }

  @Override
  public Object remove (final int x) {
    throw new UnsupportedOperationException(
      "remove" + " unsupported for " + getClass()); }

  @Override
  public boolean removeAll (final Collection x) {
    throw new UnsupportedOperationException(
      "removeAll" + " unsupported for " + getClass()); }

  @Override
  public boolean retainAll (final Collection x) {
    throw new UnsupportedOperationException(
      "retainAll" + " unsupported for " + getClass()); }

  @Override
  public Object set (final int x0, final Object x1) {
    throw new UnsupportedOperationException(
      "set" + " unsupported for " + getClass()); }

  @Override // TODO: could implement this?
  public List subList (final int x0, final int x1) {
    throw new UnsupportedOperationException(
      "subList" + " unsupported for " + getClass()); }

  @Override
  public Object[] toArray () {
    return new Object[] { class0, class1, }; }

  @Override  // TODO: could implement this?
  public Object[] toArray (final Object[] x) {
    throw new UnsupportedOperationException(
      "toArray" + " unsupported for " + getClass()); }

  @Override
  public Iterator iterator () {
    // doesn't need to be fast or gc frugal
    return Arrays.asList(toArray()).iterator(); }

  @Override
  public ListIterator listIterator () {
    // doesn't need to be fast or gc frugal
    return Arrays.asList(toArray()).listIterator(); }

  @Override
  public ListIterator listIterator (final int x) {
    return Arrays.asList(toArray()).listIterator(x); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
