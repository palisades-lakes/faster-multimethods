package palisades.lakes.multimethods.java;

/** Utilities.
 * 
 * @author palisades dot lakes at gmail dot com
 * @version 2017-12-13
 */

@SuppressWarnings("unchecked")
public final class Classes {

  //--------------------------------------------------------------
  // Null safe utilities
  //--------------------------------------------------------------
  /** Null safe <code>getName()</code>.
   */
  public static String getName (final Class c) {
    if (null == c) { return "null"; }
    return c.getName(); }
  //--------------------------------------------------------------
  /** Null safe <code>getClassName()</code>.
   */
  public static String getSimpleName (final Class c) {
    if (null == c) { return "null"; }
    return c.getSimpleName(); }
  //--------------------------------------------------------------
  /** Null safe <code>getClass</code>.
   */
  public static Class classOf (final Object x) {
    if (null == x) { return null; }
    return x.getClass(); }
  //--------------------------------------------------------------
  /** Null safe <code>isAssignableFrom</code>.
   */
  public static boolean isAssignableFrom (final Class k0, 
                                          final Class k1) {
    if (null == k0) { return null == k1; }
    if (null == k1) { return false; }
    return k0.isAssignableFrom(k1); }
  //--------------------------------------------------------------
  // disabled constructor

  private Classes () {
    throw new UnsupportedOperationException(
      "can't instantiate " + getClass()); }

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
