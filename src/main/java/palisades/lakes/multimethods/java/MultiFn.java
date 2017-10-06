package palisades.lakes.multimethods.java;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;

/**
 * Interface for MultiFn implementations.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-07-16
 * @version 2017-10-06
 */
@SuppressWarnings("unchecked")
public interface MultiFn extends IFn {

  /** Is <code>x</code> a legal dispatch value for this 
   * <code>MultiFn</code>?
   */
  public boolean isLegalDispatchValue (final Object x);

  /** Throw an <code>IllegalArgumentException</code> if 
   * <code>x</code> is not a legal dispatch value for this
   * <code>MultiFn</code>.
   */
  public default void checkLegalDispatchValue (final Object x) {
    if (! isLegalDispatchValue(x)) {
      throw new IllegalArgumentException("not legal:" + x); } }

  /** Are methods defined for <code>y</code> applicable to
   * <code>x</code>?
   */
  public boolean isA (final Object x, final Object y);

  /** Is a method defined for <code>x</code> preferred to a 
   * method defined for <code>y</code>?
   */
  public boolean dominates (final Object x, final Object y);

  /** Clear the <code>MultiFn</code>, removing all defined
   * methods and preferences, and emptying the cache.
   */
  public MultiFn reset();


  /** Make <code>f</code> the defined method for dispatch value 
   * <code>x</code>.
   *
   * @throws an exception if <code>x</code>
   * is not a legal dispatch value.
   */

  public MultiFn addMethod (final Object x, 
                            final IFn f);

  public MultiFn removeMethod (final Object x);

  /** Prefer a method defined for dispatch value <code>x</code>
   * to one defined for <code>y</code>.
   * 
   * @throws an exception if <code>x</code> or <code>y</code>
   * are not legal dispatch values.
   */
  public MultiFn preferMethod (final Object x, 
                               final Object y);

  /** Return the method (a function), if any, defined for
   * dispatch value <code>x</code>.
   * 
   * To keep method lookup fast, does NOT check that dispatch
   * value is legal, just returning <code>null</code> in that case.
   */
  public IFn getMethod(final Object x) ;

  /** Return the table of defined methods,
   * a map from dispatch values to functions.
   */
  public IPersistentMap getMethodTable ();

  /** Return the table of explicit method preferences,
   * a map from dispatch value to set of dispatch values,
   * with the interpretation a method defined for the key dispatch
   * value is preferred to methods defined for any of the dispatch
   * values in the set.
   */
  public IPersistentMap getPreferTable();

}
