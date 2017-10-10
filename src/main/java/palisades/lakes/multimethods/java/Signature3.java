package palisades.lakes.multimethods.java;

/** A triple of classes, for optimizing multimethod dispatch
 * functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-05
 * @version 2017-10-09
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
      class0.isAssignableFrom(k0) &&
      class1.isAssignableFrom(k1) &&
      class2.isAssignableFrom(k2); }

  @Override
  public final boolean isAssignableFrom (final Class... ks) {
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
}
//--------------------------------------------------------------
