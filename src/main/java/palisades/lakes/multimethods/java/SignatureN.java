package palisades.lakes.multimethods.java;

import java.util.List;

import clojure.lang.ArraySeq;

/** An immutable 'array list' of classes, for optimizing 
 * multimethod dispatch functions.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-05
 * @version 2017-10-09
 */

@SuppressWarnings("unchecked")
public final class SignatureN implements Signature {

  // TODO: separate 1st k Classes to make constructor faster?
  // might also make isAssignableFrom faster in the false case.
  // TODO: replace Class[] with ArraySeq to make Clojure 
  // interface simpler?

  public final Class[] classes;

  //--------------------------------------------------------------

  public final boolean isAssignableFrom (final SignatureN that) {
    final Class[] those = that.classes;
    if (classes.length != those.length) { return false; }
    for (int i=0;i<classes.length;i++) {
      if (! classes[i].isAssignableFrom(those[i])) {
        return false; } }
    return true; }

  //--------------------------------------------------------------

  @Override
  public final boolean isAssignableFrom (final Signature that) {
    if (that instanceof SignatureN) {
      return isAssignableFrom((SignatureN) that); }
    return false; }

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
    if (classes.length != ks.length) { return false; }
    for (int i=0;i<classes.length;i++) {
      if (! classes[i].isAssignableFrom(ks[i])) {
        return false; } }
    return true; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    int result = 17;
    for (final Class c : classes) {
      result = (37*result) + c.hashCode(); }
    return result; }

  @Override
  public final boolean equals (final Object that) {
    if (this == that) { return true; }
    if (that instanceof SignatureN) {
      if (classes.equals(((SignatureN) that).classes)) { 
        return true; } }
    return false; }

  @Override
  public final String toString () {
    final StringBuilder builder =
      new StringBuilder("(");
    builder.append(getClass().getSimpleName());
    builder.append(". ");
    for (final Class c : classes) {
      builder.append(c.getName());
      builder.append(" "); }
    builder.append(")");
    return builder.toString(); }

  //--------------------------------------------------------------
  // TODO: memoize singleton instances?

  public SignatureN (final Class... ks) {
    assert ks.length > 3;
    // is this safe?
    classes = ks; }

  public SignatureN (final Class c0,
                     final Class c1,
                     final Class c2,
                     final ArraySeq as) {
    // not really safe, trying to be fast, maybe not worth it
    final Object[] cs = as.array;
    final int n = cs.length + 3;
    assert n > 3;
    classes = new Class[n];
    classes[0] = c0;
    classes[1] = c1;
    classes[2] = c2;
    for (int i=3,j=0;i<n;i++,j++) { 
      classes[i] = (Class) cs[j]; } }

  // TODO: copy array?
  public static final SignatureN get (final Class... ks) {
    return new SignatureN(ks); }

  public static final SignatureN get (final Class k0,
                                      final Class k1,
                                      final List ks) {
    final Class[] classes = new Class[2 + ks.size()];
    classes[0] = k0;
    classes[1] = k1;
    int i = 2;
    for (final Object k : ks) { classes[i++] = (Class) k; }
    return new SignatureN(classes); }

  public static final SignatureN extract (final Object... xs) {
    final int n = xs.length;
    assert n > 3;
    final Class[] ks = new Class[n];
    for (int i=0;i<n;i++) { ks[i] = xs[i].getClass(); }
    return new SignatureN(ks); }

  public static final SignatureN extract (final Object x0,
                                          final Object x1,
                                          final Object x2,
                                          final ArraySeq as) {
    // not really safe, trying to be fast, maybe not worth it
    final Object[] xs = as.array;
    final int n = xs.length + 3;
    assert n > 3;
    final Class[] cs = new Class[n];
    cs[0] = x0.getClass();
    cs[1] = x1.getClass();
    cs[2] = x2.getClass();
    for (int i=3,j=0;i<n;i++,j++) { 
      cs[i] = xs[j].getClass(); } 
    return new SignatureN(cs); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
