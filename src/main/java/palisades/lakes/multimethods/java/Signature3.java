package palisades.lakes.multimethods.java;

import java.util.Objects;

/** A triple of classes, for optimizing multimethod dispatch
 * functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-12-13
 */

@SuppressWarnings("unchecked")
public final class Signature3 implements Signature {

  public final Class class0;
  public final Class class1;
  public final Class class2;

  //--------------------------------------------------------------

  public final boolean isAssignableFrom (final Signature3 that) {
    return
      Classes.isAssignableFrom(class0,that.class0)
      &&
      Classes.isAssignableFrom(class1,that.class1)
      &&
      Classes.isAssignableFrom(class2,that.class2); }

  //--------------------------------------------------------------

  @Override
  public final boolean isAssignableFrom (final Signature that) {
    if (that instanceof Signature3) {
      return isAssignableFrom((Signature3) that); }
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
      Classes.isAssignableFrom(class0,k0) 
      &&
      Classes.isAssignableFrom(class1,k1) 
      &&
      Classes.isAssignableFrom(class2,k2); }

  @Override
  public final boolean isAssignableFrom (final Class... ks) {
    return false; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    return
      (37*((37*((37*17) + Objects.hashCode(class0)))
        + Objects.hashCode(class1)))
      + Objects.hashCode(class2); }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (that instanceof Signature3) {
      return
        Objects.equals(class0,((Signature3) that).class0)
        &&
        Objects.equals(class1,((Signature3) that).class1)
        &&
        Objects.equals(class2,((Signature3) that).class2); }
    return false; }

  @Override
  public final String toString () {
    return "(" + getClass().getSimpleName() + ". "
      + Classes.getName(class0) + " "
      + Classes.getName(class1) + " "
      + Classes.getName(class2) + ")"; }

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
      Classes.classOf(k0),
      Classes.classOf(k1),
      Classes.classOf(k2)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
