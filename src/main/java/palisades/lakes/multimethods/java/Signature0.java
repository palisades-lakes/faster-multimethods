package palisades.lakes.multimethods.java;

import java.util.Objects;

/** Singleton {@link Signature} implementation for zero-arity
 * methods.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-10-12
 * @version 2017-10-12
 */

@SuppressWarnings("unchecked")
public final class Signature0 implements Signature {

   //--------------------------------------------------------------

  @Override
  public final boolean isAssignableFrom (final Signature that) {
    return (that instanceof Signature0); }

  @Override
  public final boolean isAssignableFrom (final Class k0,
                                         final Class k1) {
    return false; }

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

  private static final int HASH = Objects.hash("Signature0");
  
  @Override
  public final int hashCode () { return HASH; }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (that instanceof Signature0) { return true; }
    return false; }

  @Override
  public final String toString () { return "Signature0"; }

  //--------------------------------------------------------------
  // TODO: memoize singleton instances?

  public Signature0 () { }

  public static final Signature0 INSTANCE = new Signature0();
  
  public static final Signature0 get () { return INSTANCE; }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
