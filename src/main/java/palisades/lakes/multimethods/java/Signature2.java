package palisades.lakes.multimethods.java;

import java.util.Objects;

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
      Classes.isAssignableFrom(class0,that.class0)
      &&
      Classes.isAssignableFrom(class1,that.class1); }

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
      Classes.isAssignableFrom(class0,k0) 
      && 
      Classes.isAssignableFrom(class1,k1); }

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
    if (! (that instanceof Signature2)) { return false; }
    if (hash != ((Signature2) that).hash) { return false; }
    if (! Objects.equals(class0,((Signature2) that).class0)) {
      return false; }
    if (! Objects.equals(class1,((Signature2) that).class1)) {
      return false; }
    return true; }

  @Override
  public final String toString () {
    return 
      Classes.getSimpleName(class0) + 
      "_" + 
      Classes.getSimpleName(class1); }

  //--------------------------------------------------------------
  // TODO: memoize singleton instances?

  public Signature2 (final Class k0, final Class k1) {
    hash = 37*((37*17) + Objects.hashCode(k0)) 
      + Objects.hashCode(k1);
    class0 = k0; 
    class1 = k1; }

  public static final Signature2 get (final Class k0,
                                      final Class k1) {
    return new Signature2(k0,k1); }

  public static final Signature2 extract (final Object k0,
                                          final Object k1) {
    return new Signature2(
      Classes.classOf(k0),
      Classes.classOf(k1)); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
