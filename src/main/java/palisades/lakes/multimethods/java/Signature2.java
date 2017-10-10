package palisades.lakes.multimethods.java;

/** A pair of classes, for optimizing multimethod dispatch
 * functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-02
 * @version 2017-10-09
 */

@SuppressWarnings("unchecked")
public final class Signature2 implements Signature {

  private final int hash;
  private final Class class0;
  private final Class class1;

  //--------------------------------------------------------------

  public final boolean isAssignableFrom (final Signature2 that) {
    return
      class0.isAssignableFrom(that.class0)
      &&
      class1.isAssignableFrom(that.class1); }

  //--------------------------------------------------------------

  @Override
  public final boolean isAssignableFrom (final Signature that) {
    if (that instanceof Signature2) {
      return isAssignableFrom((Signature2) that); }
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
}
//--------------------------------------------------------------
