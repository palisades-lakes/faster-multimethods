package palisades.lakes.multimethods.java;

// Not implementing java.util.List to in an attempt to make
// signatures as lightweight as possible.
// Does it make a difference at runtime? I haven't checked
// carefully. It does make for less boilerplate code.

/** A 'list' of classes, for optimizing method lookup.
 * Implementation details, subject to revision. 
 * Use the Clojure API to be future-proof.
 * <p>
 * <strong>Note:</strong> 
 * does NOT implement <code>java.util.List</code>.
 * 
 * @author palisades dot lakes at gmail dot com
 * @since 2017-06-05
 * @version 2017-10-09
 */

public interface Signature {

  //--------------------------------------------------------------
  // inheritance partial ordering
  //--------------------------------------------------------------

  /** If <code>this.isAssignableFrom(that)</code>,
   * then a method defined for <code>this</code> can be used
   * on an arglist whose signature is <code>that</code>.
   */
  boolean isAssignableFrom (Signature that);

  /** If <code>this.isAssignableFrom(k0,k1)</code>,
   * then a method defined for <code>this</code> can be used
   * on a length 2 arglist whose classes are <code>k0, k1</code>.
   * <p>
   * This eliminates the need to create signature
   * instances in a common special case.
   */
  boolean isAssignableFrom (Class k0, Class k1);

  /** If <code>this.isAssignableFrom(k0,k1,k2)</code>,
   * then a method defined for <code>this</code> can be used
   * on a length 3 arglist whose classes are 
   * <code>k0, k1, k3</code>.
   * <p>
   * This eliminates the need to create signature
   * instances in a common special case.
   */
  boolean isAssignableFrom (Class k0, Class k1, Class k2);

  /** If <code>this.isAssignableFrom(ks)</code>,
   * then a method defined for <code>this</code> can be used
   * on a length 3 arglist whose classes are the elements of
   * <code>ks</code>.
   * <p>
   * This eliminates the need to create signature
   * instances in a common special case.
   */
  boolean isAssignableFrom (Class... ks);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
