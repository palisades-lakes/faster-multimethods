package benchtools.java.sets;

import clojure.lang.IFn;

//----------------------------------------------------------------
/** Half-open [min,max) interval in expressed in
 * <code>byte</code>, but applicable to any primitive or Object
 * number.
 *
 * TODO: how to implement [x,infinity]?
 * TODO: empty interval different from general empty set? has a
 * location so it can be transformed by functions R-&gt;R?
 * TODO: any sense in [i1,i0) where i1 > i0 ? Complement?
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-05-29
 * @version 2017-07-24
 */

public final class ByteInterval implements Set {

  public final byte min;
  public final byte max;

  //--------------------------------------------------------------
  // Set interface
  //--------------------------------------------------------------

  @Override
  public final double diameter () { return max - min; }

  //--------------------------------------------------------------

  @Override
  public final boolean contains (final boolean x) { 
    return false; }

  @Override
  public final boolean contains (final byte x) {
    return (min <= x) && (x < max); }

  @Override
  public final boolean contains (final char x) { 
    return false; }

  @Override
  public final boolean contains (final double x) {
    return (min <= x) && (x < max); }

  @Override
  public final boolean contains (final float x) {
    return (min <= x) && (x < max); }

  @Override
  public final boolean contains (final int x) {
    return (min <= x) && (x < max); }

  @Override
  public final boolean contains (final long x) {
    return (min <= x) && (x < max); }

  @Override
  public final boolean contains (final short x) {
    return (min <= x) && (x < max); }


  @Override
  public final boolean contains (final Boolean x) { 
    return false; }

  @Override
  public final boolean contains (final Byte x) {
    return (min <= x.byteValue()) && (x.byteValue() < max); }

  @Override
  public final boolean contains (final Character x) { 
    return false; }

  @Override
  public final boolean contains (final Double x) {
    return (min <= x.doubleValue()) && (x.doubleValue() < max); }

  @Override
  public final boolean contains (final Float x) {
    return (min <= x.floatValue()) && (x.floatValue() < max); }

  @Override
  public final boolean contains (final Integer x) {
    return (min <= x.intValue()) && (x.intValue() < max); }

  @Override
  public final boolean contains (final Long x) {
    return (min <= x.longValue()) && (x.longValue() < max); }

  @Override
  public final boolean contains (final Short x) {
    return (min <= x.shortValue()) && (x.shortValue() < max); }

  @Override
  public final boolean contains (final Object x) {
    if (x instanceof Byte) { return contains((Byte) x); }
    if (x instanceof Double) { return contains((Double) x); }
    if (x instanceof Float) { return contains((Float) x); }
    if (x instanceof Integer) { return contains((Integer) x); }
    if (x instanceof Long) { return contains((Long) x); }
    if (x instanceof Short) { return contains((Short) x); }
    return false; }
  //--------------------------------------------------------------

  public final boolean intersects (final ByteInterval that) {
    if (max <= that.min) { return false; }
    if (that.max <= min) { return false; }
    return true; }

  public final boolean intersects (final DoubleInterval that) {
    if (max <= that.min) { return false; }
    if (that.max <= min) { return false; }
    return true; }

  public final boolean intersects (final java.util.Set that) {
    for (final Object x : that) {
      if (contains(x)) { return true; } }
    return false; }

  @Override
  public final boolean intersects (final Object set) {
    if (set instanceof ByteInterval) {
      return intersects((ByteInterval) set); }
    if (set instanceof java.util.Set) {
      return intersects((java.util.Set) set); }
    throw new UnsupportedOperationException(
      "intersects" + " unsupported for " + getClass()); }

  //--------------------------------------------------------------

  private ByteInterval (final byte i0,
                        final byte i1) {
    assert (i0 <= i1);
    min = i0; max = i1; }

  public static final ByteInterval make (final byte i0,
                                         final byte i1) {

    if (i0 <= i1) { return new ByteInterval(i0,i1); }
    return new ByteInterval(i1,i0); }

  /** <code>g</code> is a 'function' of no arguments, which is 
   * expected to return a different value on each call, typically
   * wrapping some pseudo-random number generator.
   * Clojure unfortunately only supports functions returning
   * primitive <code>long</code> and <code>double</code>
   * values.
   * @throws an exception if the generated value is not within
   * the valid range.
   */
  public static final ByteInterval generate (final IFn.L g) {

    final long x0 = g.invokePrim();
    assert Byte.MIN_VALUE <= x0 && x0 <= Byte.MAX_VALUE;
    final long x1 = g.invokePrim();
    assert Byte.MIN_VALUE <= x1 && x1 <= Byte.MAX_VALUE;
    
    return make((byte) x0, (byte) x1); }

  //--------------------------------------------------------------
} // end of class
//----------------------------------------------------------------
