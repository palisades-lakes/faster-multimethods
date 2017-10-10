package palisades.lakes.multimethods.java;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;

/**
 * Multimethod Java API. 
 * Implementation details, subject to revision. 
 * Use the Clojure API to be future-proof.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-07-16
 * @version 2017-10-09
 */
@SuppressWarnings("unchecked")
public interface MultiFn extends IFn {

  /** Is <code>x</code> a legal dispatch value for this 
   * <code>MultiFn</code>?
   * <p>
   * For performance reasons, some implementations of
   * <code>MultiFn</code> may restrict kinds of dispatch values
   * they support.
   * <p>
   * Note: 'Legal' means it is possible to define methods for 
   * <code>x</code>, not that one has already been defined.
   */
  public boolean isLegalDispatchValue (final Object x);

  /** Throw an exception if <code>x</code> is not supported by
   * this <code>MultiFn</code>.
   * 
   * @throws IllegalArgumentException
   if not {@link #isLegalDispatchValue(Object) isLegalDisptachValue(x)}.
   */
  public default void checkLegalDispatchValue (final Object x) {
    if (! isLegalDispatchValue(x)) {
      throw new IllegalArgumentException("not legal: " + x); } }

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
   * 
   * @return the modified <code>MultiFn</code>.
   */
  public MultiFn reset();


  /** Make <code>f</code> the defined method for dispatch value 
   * <code>x</code>.
   *
   * @throws IllegalArgumentException if <code>x</code>
   * is not a legal dispatch value for this
   * <code>MultiFn</code>.
   * 
   * @return the modified <code>MultiFn</code>.
   */
  public MultiFn addMethod (final Object x, 
                            final IFn f);

  /** Removes the defined method for dispatch value 
   * <code>x</code>, if there is one. If not, does nothing.
   *
   * @throws IllegalArgumentException if <code>x</code>
   * is not a legal dispatch value for this
   * <code>MultiFn</code>.
   * 
   * @return the modified <code>MultiFn</code>.
   */
  public MultiFn removeMethod (final Object x);

  /** Prefer a method defined for dispatch value <code>x</code>
   * to one defined for <code>y</code>.
   * 
   * @throws IllegalArgumentException if <code>x</code> or 
   * <code>y</code> are not legal dispatch values for this
   * <code>MultiFn</code>.
   * 
   * @return the modified <code>MultiFn</code>.
   */
  public MultiFn preferMethod (final Object x, 
                               final Object y);

  /** Look up the most preferred method for
   * dispatch value <code>x</code>, if any applicable methods are
   * defined.
   * 
   * To keep method lookup fast, does NOT check that dispatch
   * value is legal, 
   * just returning <code>null</code> in that case.
   * 
   * @return the dominating method function, 
   * or <code>null</code> if no applicable method is defined.
   */
  public IFn getMethod (final Object x) ;

  /** Return the table of defined methods,
   * a map from dispatch values to functions.
   * 
   * @return an instance of 
   * <code>clojure.lang.IPersistentMap</code>
   */
  public IPersistentMap getMethodTable ();

  /** Return the table of explicit method preferences,
   * a map from dispatch value to set of dispatch values,
   * with the interpretation a method defined for the key dispatch
   * value is preferred to methods defined for any of the dispatch
   * values in the set.
   * 
   * @return an instance of 
   * <code>clojure.lang.IPersistentMap</code>
   */
  public IPersistentMap getPreferTable();

}
