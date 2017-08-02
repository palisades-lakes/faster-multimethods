package benchtools.java.sets;


/** Minimal interface for multimethod benchmarks,
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-05-22
 * @version 2017-07-24
 */
public interface Set {

  //--------------------------------------------------------------
  // Doesn't make sense for general sets; here to permit benchmark
  // of single arg multimethod.
  
  public double diameter ();
  
  //--------------------------------------------------------------
  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (boolean x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (byte x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (char x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (double x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (float x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (int x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (long x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (short x);


  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Boolean x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Byte x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Character x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Double x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Float x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Integer x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Long x);

  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Short x);


  /** Is <code>x</code> an element of <code>this</code> set?
   */
  public boolean contains (Object x);

  //--------------------------------------------------------------
  /** Does <code>this</code> share any elements with
   * <code>set</code>?
   */
  public boolean intersects (Object set);

  //--------------------------------------------------------------
}
//--------------------------------------------------------------
