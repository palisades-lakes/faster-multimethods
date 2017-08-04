package benchtools.java.sets;

import java.util.Collections;
import java.util.Set;

/** Static intersection test.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-05-29
 * @version 2017-07-23
 */

@SuppressWarnings("unchecked")
public final class Intersects extends Object {

  //--------------------------------------------------------------
  // intersection tests
  //--------------------------------------------------------------

  public final static boolean intersects (final IntegerInterval s0,
                                          final IntegerInterval s1) {
    if (s0.max <= s1.min) { return false; }
    if (s1.max <= s0.min) { return false; }
    return true; }

  public final static boolean intersects (final IntegerInterval s0,
                                          final DoubleInterval s1) {
    if (s0.max <= s1.min) { return false; }
    if (s1.max <= s0.min) { return false; }
    return true; }

  public final static boolean intersects (final IntegerInterval s0,
                                          final Set s1) {
    return s0.intersects(s1); }

  //--------------------------------------------------------------

  public final static boolean intersects (final DoubleInterval s0,
                                          final IntegerInterval s1) {
    if (s0.max <= s1.min) { return false; }
    if (s1.max <= s0.min) { return false; }
    return true; }

  public final static boolean intersects (final DoubleInterval s0,
                                          final DoubleInterval s1) {
    if (s0.max <= s1.min) { return false; }
    if (s1.max <= s0.min) { return false; }
    return true; }

  public final static boolean intersects (final DoubleInterval s0,
                                          final Set s1) {
    return s0.intersects(s1); }

  //--------------------------------------------------------------

  public final static boolean intersects (final Set s0,
                                          final IntegerInterval s1) {
    return s1.intersects(s0); }

  public final static boolean intersects (final Set s0,
                                          final DoubleInterval s1) {
    return s1.intersects(s0); }

  public final static boolean intersects (final Set s0,
                                          final Set s1) {
    return (! Collections.disjoint(s0,s1)); }

  //--------------------------------------------------------------

  public final static boolean intersects (final Object s0,
                                          final Object s1) {
    throw new UnsupportedOperationException(
      "don't know how to test for intersections of " +
        s0.getClass().getSimpleName() +
        " and " +
        s1.getClass().getSimpleName()); }

  //--------------------------------------------------------------
  // lookup
  //--------------------------------------------------------------

  public static final boolean manual (final Object s0,
                                      final Object s1) {

    if (s0 instanceof IntegerInterval) {
      if (s1 instanceof IntegerInterval) {
        return intersects(
          (IntegerInterval) s0, (IntegerInterval) s1); }
      if (s1 instanceof DoubleInterval) {
        return intersects(
          (IntegerInterval) s0, (DoubleInterval) s1); }
      if (s1 instanceof Set) {
        return intersects((IntegerInterval) s0, (Set) s1); }
      return ((IntegerInterval) s0).intersects(s1); }

    if (s0 instanceof DoubleInterval) {
      if (s1 instanceof DoubleInterval) {
        return intersects(
          (DoubleInterval) s0, (DoubleInterval) s1); }
      if (s1 instanceof IntegerInterval) {
        return intersects(
          (DoubleInterval) s0, (IntegerInterval) s1); }
      if (s1 instanceof Set) {
        return intersects((DoubleInterval) s0, (Set) s1); }
      return ((DoubleInterval) s0).intersects(s1); }

    if (s0 instanceof Set) {
      if (s1 instanceof DoubleInterval) {
        return intersects((Set) s0, (DoubleInterval) s1); }
      if (s1 instanceof IntegerInterval) {
        return intersects((Set) s0, (IntegerInterval) s1); }
      if (s1 instanceof Set) {
        return intersects((Set) s0, (Set) s1); } }

    throw new UnsupportedOperationException(
      "Can't test for interesection of " +
        s0.getClass().getSimpleName() +
        " and " +
        s1.getClass().getSimpleName()); }

  //--------------------------------------------------------------
  // summaries
  //--------------------------------------------------------------

  public static final int 
  countIntersections (final IntegerInterval[] s0,
                      final IntegerInterval[] s1) {
    int k = 0;
    final int n = s0.length;
    assert n == s1.length;
    for (int i=0;i<n;i++) { 
      if (intersects(s0[i],s1[i])) { k++; } }
    return k; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Intersects () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  ///--------------------------------------------------------------
} // end class
//--------------------------------------------------------------
