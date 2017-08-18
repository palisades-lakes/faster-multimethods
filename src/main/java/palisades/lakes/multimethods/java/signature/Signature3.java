package palisades.lakes.multimethods.java.signature;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** A triple of classes, for optimizing multimethod dispatch
 * functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-05
 * @version 2017-08-05
 */

@SuppressWarnings("unchecked")
public final class Signature3 implements Signature {

  public final Class class0;
  public final Class class1;
  public final Class class2;

  //--------------------------------------------------------------

  public final boolean isAssignableFrom (final Signature3 that) {
    return
      class0.isAssignableFrom(that.class0)
      &&
      class1.isAssignableFrom(that.class1)
      &&
      class2.isAssignableFrom(that.class2); }

  //--------------------------------------------------------------

  @Override
  public final int size () { return 3; }
  @Override
  public final boolean isAssignableFrom (final Signature that) {
    if (that instanceof Signature3) {
      return isAssignableFrom((Signature3) that); }
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class k) {
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class k0,
                                         final Class k1) {
    return false; }

  @Override
  public final boolean isAssignableFrom (final Class k0,
                                         final Class k1,
                                         final Class k2) {
    return
      class0.isAssignableFrom(k0) &&
      class1.isAssignableFrom(k1) &&
      class2.isAssignableFrom(k2); }

  @Override
  public final boolean isAssignableFrom (final Class... ks) {
    return false; }

  //--------------------------------------------------------------

  @Override
  public final boolean equiv (final Class k0,
                              final Class k1) {
    return false; }

  @Override
  public final boolean equiv (final Class k0,
                              final Class k1,
                              final Class k2) {
    return
      class0.equals(k0) &&
      class1.equals(k1) &&
      class2.equals(k2); }

  @Override
  public final boolean equiv (final Class... ks) {
    return false; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    return
      (37*((37*((37*17) + class0.hashCode()))
        + class1.hashCode()))
      + class2.hashCode(); }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (that instanceof Signature3) {
      return
        class0.equals(((Signature3) that).class0)
        &&
        class1.equals(((Signature3) that).class1)
        &&
        class2.equals(((Signature3) that).class2); }
    return false; }

  @Override
  public final String toString () {
    return "(" + getClass().getSimpleName() + ". "
      + class0.getName() + " "
      + class1.getName() + " "
      + class2.getName() + ")"; }

  //--------------------------------------------------------------
  // TODO: memoize singleton instances?

  public Signature3 (final Class k0,
                      final Class k1,
                      final Class k2) {
    class0 = k0; class1 = k1; class2 = k2; }

  public static final Signature3 get (final Class k0,
                                      final Class k1,
                                      final Class k2) {
    return new Signature3(k0,k1,k2); }


  public static final Signature3 extract (final Object k0,
                                          final Object k1,
                                          final Object k2) {
    return new Signature3(
      k0.getClass(),
      k1.getClass(),
      k2.getClass()); }

  //--------------------------------------------------------------
  // List interface
  //--------------------------------------------------------------

  @Override
  public boolean add (final Object x) {
    throw new UnsupportedOperationException(
      "add" + " unsupported for " + getClass()); }

  @Override
  public void add (final int x, final Object x1) {
    throw new UnsupportedOperationException(
      "add" + " unsupported for " + getClass()); }

  @Override
  public boolean addAll (final Collection x) {
    throw new UnsupportedOperationException(
      "addAll" + " unsupported for " + getClass()); }

  @Override
  public boolean addAll (final int x0, 
                         final Collection x1) {
    throw new UnsupportedOperationException(
      "addAll" + " unsupported for " + getClass()); }

  @Override
  public void clear () {
    throw new UnsupportedOperationException(
      "clear" + " unsupported for " + getClass()); }

  @Override
  public boolean contains (final Object x) {
    return class0.equals(x)
      || class1.equals(x)
      || class2.equals(x); }

  @Override
  public boolean containsAll (final Collection x) {
    for (final Object o : x) {
      if (! contains(o)) { return false; } }
    return true; }

  @Override
  public Object get (final int x) {
    if (0 == x) { return class0; }
    if (1 == x) { return class1; }
    if (2 == x) { return class2; }
    throw new IndexOutOfBoundsException(
      this + " has 2 elements: "  + x); }

  @Override
  public int indexOf (final Object x) {
    if (class0.equals(x)) { return 0; }
    if (class1.equals(x)) { return 1; }
    if (class2.equals(x)) { return 2; }
    return -1; }

  @Override
  public boolean isEmpty () { return false;  }

  @Override
  public int lastIndexOf (final Object x) { 
    if (class2.equals(x)) { return 2; }
    if (class1.equals(x)) { return 1; }
    if (class0.equals(x)) { return 0; }
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
  public Object set (final int x0, 
                     final Object x1) {
    throw new UnsupportedOperationException(
      "set" + " unsupported for " + getClass()); }

  @Override // TODO: implement this?
  public List subList (final int x, final int x1) {
    throw new UnsupportedOperationException(
      "subList" + " unsupported for " + getClass()); }

  @Override
  public Object[] toArray () {
    return new Object[] { class0, class1, }; }

  @Override  // TODO: implement this?
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
