package faster.multimethods.java;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;

/**
 * Interface for MultiFn implementations.
 *
 * @author palisades dot lakes at gmail dot com
 * @since 2017-07-16
 * @version 2017-07-27
 */
@SuppressWarnings("unchecked")
public interface MultiFn extends IFn {
 
  public MultiFn reset();
  public MultiFn addMethod (final Object dispatchVal, 
                             final IFn method);

  public MultiFn removeMethod (final Object dispatchVal);

  public MultiFn preferMethod (final Object dispatchValX, 
                               final Object dispatchValY);

  public IFn getMethod(final Object dispatchVal) ;

  public IPersistentMap getMethodTable ();

  public IPersistentMap getPreferTable();
  
}