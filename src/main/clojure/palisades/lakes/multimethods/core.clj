(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.core
  
  {:doc "Faster multimethod method lookup."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-06-02"
   :version "2017-09-27"}
  (:refer-clojure :exclude [defmulti defmethod remove-all-methods
                            remove-method prefer-method methods
                            get-method prefers]))
;;----------------------------------------------------------------

(defmacro signature 
  
  "Return an appropriate implementation of 
   `Signature` for the `Class` valued arguments
    (in the arity 1 case, it just returns the `Class` itself).

   **Warning:** `signature` can only be used 
   as a dispatch function with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  { :arglists '([^Class c0] 
                 [^Class c0 ^Class c1]
                 [^Class c0 ^Class c1 ^Class c2]
                 [^Class c0 ^Class c1 ^Class c2 & classes])
   :added "faster-multimethods 0.0.0"}
  
  ([c0] `(with-meta c0 {:tag 'Class}))
  ([c0 c1] 
    `(palisades.lakes.multimethods.java.Signature2.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})))
  ([c0 c1 c2] 
    `(palisades.lakes.multimethods.java.Signature3.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})
       ~(with-meta c2 {:tag 'Class})))
  ([c0 c1 c2 & cs] 
    `(palisades.lakes.multimethods.java.SignatureN.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})
       ~(with-meta c2 {:tag 'Class})
       ~(with-meta cs {:tag 'clojure.lang.ArraySeq}))))

(defmacro extract-signature 
  
  "Return an appropriate implementation of `Signature` for the
   arguments, calling `(.getClass xi)` as needed
   (in the arity 1 case, it returns `(.getClass x0)` itself).

   **Warning:** `extract-signature` can only be used 
   as a dispatch function with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  { :arglists '([x0] 
                 [x0 x1]
                 [x0 x1 x2]
                 [x0 x1 x2 & args])
   :added "faster-multimethods 0.0.0"}
  
  ([x0] `(.getClass ~(with-meta x0 {:tag 'Object})))
  ([x0 x1] 
    `(palisades.lakes.multimethods.java.Signature2.
       (.getClass ~(with-meta x0 {:tag 'Object}))
       (.getClass ~(with-meta x1 {:tag 'Object}))))
  ([x0 x1 x2] 
    `(palisades.lakes.multimethods.java.Signature3.
       (.getClass ~(with-meta x0 {:tag 'Object}))
       (.getClass ~(with-meta x1 {:tag 'Object}))
       (.getClass ~(with-meta x2 {:tag 'Object}))))
  ([x0 x1 x2 & xs] 
    `(SignatureN/extract 
       ~x0 ~x1 ~x2 ~with-meta xs {:tag 'clojure.lang.ArraySeq})))

(defn signature? 
  "Is `v` a signatue 
   (ie, an instance of `palisades.lakes.multimethods.java.Signature`)?"
  [v] 
  (instance? palisades.lakes.multimethods.java.Signature v))   
;;----------------------------------------------------------------
;; dispatch value validation
;;----------------------------------------------------------------
(defn atomic-dispatch-value? 
  "Is `v` an atomic dispatch value (ie a `Class` or a
   namespace-qualified instance of `Named`, concretely,
   a namespace-qualified `Symbol` or `Keyword`)?"
  [v]
  (or (class? v)
      (and (instance? clojure.lang.Named v) 
           (namespace v))))

(defn recursive-dispatch-value? 
  "Is `v` a recursive dispatch value (ie a vector whose elements
   are atomic or recurswive dispatch values)?"
  [v]
  (and (vector? v)
       (every? #(or (atomic-dispatch-value? %)
                    (recursive-dispatch-value? %))
               v)))

(defn legal-dispatch-value?
  "Is `v` a legal dispatch value?"
  [v]
  (or (atomic-dispatch-value? v)
      (signature? v)
      (recursive-dispatch-value? v)))
;;----------------------------------------------------------------
(defn- check-valid-options
  "Throws an exception if the given option map contains keys not listed
  as valid, else returns nil."
  [options & valid-keys]
  (when (seq (apply disj (apply hash-set (keys options)) valid-keys))
    (throw
      (IllegalArgumentException.
        ^String
        (apply str "Only these options are valid: "
               (first valid-keys)
               (map #(str ", " %) (rest valid-keys))))))
  (if (and (not (:hierarchy options)) (:default options))
    (throw
      (IllegalArgumentException.
        (str ":hierarchy is" (:hierarchy options)
             ", which means truthy :default ("
             (:default options) ") is not allowed.")))))
;;----------------------------------------------------------------
;; dispatch value partial orderings
;;----------------------------------------------------------------

(defn isa<= 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  Not used is method lookup, but may be useful for debugging."
  
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (.isA f x y))

(defn isa< 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (and (!= x y) (isa<= x y)))

(defn isa>= 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  Not used is method lookup, but may be useful for debugging."
  
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (.isA f y x))

(defn isa> 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (and (!= x y) (isa>= x y)))

;;----------------------------------------------------------------

(defn dominates<= 
  "Transitive extension of [[isa<=]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used is method lookup, but may be useful for debugging."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (.dominates f x y))

(defn dominates< 
  "Transitive extension of [[isa<]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used is method lookup, but may be useful for debugging."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (and (!= x y) (dominates<= x y)))

(defn dominates>= 
  "Transitive extension of [[isa>=]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used is method lookup, but may be useful for debugging."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (.dominates f y x))

(defn domainates> 
  "Transitive extension of [[isa>]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used is method lookup, but may be useful for debugging."
  [^palisades.lakes.multimethods.java.MultiFn multifn x y]
  (assert (legal-dispatch-value? x))
  (assert (legal-dispatch-value? y))
  (and (!= x y) (domainates>= x y)))

;;----------------------------------------------------------------
;; finally the multimethods
;;----------------------------------------------------------------
(defmacro defmulti
  "Creates a new multimethod 
   (an instance of `palisades.lakes.multimethods.java.MultiFn`)
   named `mm-name` that uses `dispatch-fn` to generate method
   lookup keys (aka dispatch values).

  If the `mm-name` `Var`
  is already defined and its value is a `MultiFn`, `defmulti`
  silently does nothing. This is the Clojure 1.8.0 `defmulti`
  behavior. 
 
  (This seems to me like bad answer to the problem
  of accidentally re-evaluating a given `defmulti` and wiping
  out all the methods. I think a better design would make
  `MultiFn` effectively immutable. [[defmulti]], [[defmethod]],
  etc., would return new instances which are updates of the existing
  instance, and call `alter-var-root` to update the value of `mm-name`. 
  This, I believe, is what `defn` does.)
 
  - `mm-name`: a namespace qualified `Symbol`. The `MultFn` will
  be the value of the `Var` with that name. 

  
  - `docstring?` (optional): documentation string used as the
  `:doc` metadata on `#'mm-name`.

  - `attr-map?` (optional): more metadata for the `#'mm-name`.

  - `dispatch-fn` an instance of `clojure.lang.IFn` that returns
   _legal dispatch values_ when applied to (supported) arguments 
   passed to the `MultiFn`. 

      A legal dispatch value is either an _atomic dispatch value_, 
   an instance of `clojure.lang.IPersistentVector` 
   containing legal dispatch values, or
   an instance of `palisades.lakes.multimethods.java.Signature`.   
   
      An *atomic dispatch value* must be either a `Class`, or a
   namespace-qualified instance of `clojure.lang.Named` 
  (that is, a `Symbol` or a `Keyword`). If the value of `:hierarchy` in the corresponding 
   [[defmulti]] is explicitly `false` or `nil` (not just missing), 
   then atomic dispatch values are restricted to classes. 
   
      A `Signature` is essentially an immutable list of classes.
      If using signatures, the `dispatch-fn` should call 
      [[extract-signature]]. 

      **Warning:** I believe vectors containing dispatch values can 
   be recursive, that is, a vector of dispatch values may vectors
   of dispatch values, as long as the tree eventually terminates
   in atomic dispatch values. However, I haven't tested this,
   and the unit tests inherited from Clojure 1.8.0 don't seem
   to test it either.  
   Signatures are NOT recursive.  
   **TODO:** add tests to verify or exlcude this possibility.

      **Warning:** Vectors of signatures probably
   work, but hasn't been tested. It's almost surely better not to 
   mix them.  
   **TODO:** add tests to verify or exlcude this possibility.
   
      **Warning:** currrently there is no check whether 
   `dispatch-value` is legal or not. A misleading exception may be 
   thrown at some unspecified later time.  
   **TODO:** add validation.

  - `options` (optional): are key-value pairs and may be one of:

      - `:hierarchy` used for method lookup when the dispatch
        values are or contain namespace-qualified instances of `Named`. 
        
          See
        [multimethods and hierarchies](https://clojure.org/reference/multimethods)
        for information on how to create and modify hierarchies.

          If the `:hierarchy` is not supplied, it defaults to 
         `#'clojure.core/global-hierarchy`.

          If `:hierarchy` is supplied and its value is `nil` or 'false`,
          then no `Named` or vector dispatch values are permitted;
          method lookup is optimized assuming classes and 
          signatures only.

          Otherwise the value of the `:hierarchy` option 
          must be a `Var` 
         (i.e. via the Var-quote dispatch macro #' or the var 
         special form) holding a hashmape created with 
         `clojure.core/make-hierarchy`.

          **Warning:** Multimethods that use hierarchies depend on
          mutable shared state. 

          **Warning:** the Clojure hierarchy functions behave 
        differently for 
        the default `clojure.core/global-hierarchy` versus a 
        custom local hierarchy.
        Updates to the global hierarchy call `alter-var-root`
        on `clojure.core/global-hierarchy`, mutating shared state.
        Updates to a custom hierarchy return a new updated
        hashmap; it's left to the caller to rebind any `Var`
        which might be pointing ot that hashmap.
        
      - `:default`: The default dispatch value, defaults to `:default`.
         Not supported, and an exception is thrown,
          when `:hierarchy false` or `:hierarchy nil`
          and the value of `:default` is not `false` or `nil`.

  The dispatch function must return a legal dispatch value (but
  there is no guarantee of what or where an exception will be 
  thrown if this is not true). A legal dispatch value is either
  an *atomic dispatch value*, or a vector containing atomic dispatch
  values. An *atomic dispatch value* must be either a `Class`, an
  instance of `clojure.lang.Named` (that is, a `Symbol` or a 
  `Keyword`), or a signature 
  (an instance of `palisades.lakes.multimethods.java.Signature`).

   **Note:** currrently there is no check whether the `dispatch-value`
   is legal or not. A misleading exception may be thrown at some
   unspecified later time.

   If the value of :hierarchy is `false` or `nil`, then atomic 
  dispatch values are restricted to Classes and signatures.
  In that case, :default is ignored.
  "
  
  {:arglists '([mm-name docstring? attr-map? dispatch-fn & options])
   :added "faster-multimethods 0.0.0"}
  
  [mm-name & options]
  
  (let [docstring   (if (string? (first options))
                      (first options)
                      nil)
        options     (if (string? (first options))
                      (next options)
                      options)
        m           (if (map? (first options))
                      (first options)
                      {})
        options     (if (map? (first options))
                      (next options)
                      options)
        dispatch-fn (first options)
        options     (next options)
        m           (if docstring
                      (assoc m :doc docstring)
                      m)
        m           (if (meta mm-name)
                      (conj (meta mm-name) m)
                      m)]
    (when (= (count options) 1)
      ;; TODO: better error msg
      (throw (IllegalArgumentException. 
               (str "The syntax for defmulti has changed. "
                    "Example: "
                    "(defmulti name dispatch-fn :default dispatch-value)"))))
    (let [options   (apply hash-map options)
          hierarchy (get options :hierarchy #'clojure.core/global-hierarchy)
          default   (get options :default 
                         (if hierarchy :default nil))]
      (check-valid-options options :default :hierarchy)
      (if hierarchy
        `(let [v# (def ~mm-name)]
           (when-not (and (.hasRoot v#) (instance? palisades.lakes.multimethods.java.MultiFn (deref v#)))
             (def ~(with-meta mm-name m)
               (new palisades.lakes.multimethods.java.MultiFnWithHierarchy ~(name mm-name) ~dispatch-fn ~default ~hierarchy))))
        `(let [v# (def ~mm-name)]
           (when-not (and (.hasRoot v#) (instance? palisades.lakes.multimethods.java.MultiFn (deref v#)))
             (def ~(with-meta mm-name m)
               (new palisades.lakes.multimethods.java.MultiFnWoutHierarchy ~(name mm-name) ~dispatch-fn))))))))

;;----------------------------------------------------------------

(defmacro defmethod
  
  "Creates and installs a new method for `multifn` associated 
   with `dispatch-value`. Modifies `multifn` destructively.

   - `multifn`: an instance of 
    `palisades.lakes.multimethods.java.MultiFn`,
    created with [[palisades.lakes.multimethods/defmulti]].

   - `dispatch-value`: a _legal dispatch value_.

      A legal dispatch value is either an _atomic dispatch value_, 
   an instance of `clojure.lang.IPersistentVector` 
   containing legal dispatch values, or
   an instance of `palisades.lakes.multimethods.java.Signature`.   
   
      An *atomic dispatch value* must be either a `Class`, or a
   namespace-qualified instance of `clojure.lang.Named` 
   (that is, a `Symbol` or a `Keyword`). 
   If the value of `:hierarchy` in the corresponding 
   [[defmulti]] is explicitly `false` or `nil` (not just missing), 
   then atomic dispatch values are restricted to classes. 
   
      A `Signature` is essentially an immutable list of classes.
      If using signatures, the `dispatch-value` should be computed
      with a call to [[signature]]. 

      **Warning:** I believe vectors containing dispatch values can 
   be recursive, that is, a vector of dispatch values may vectors
   of dispatch values, as long as the tree eventually terminates
   in atomic dispatch values. However, I haven't tested this,
   and the unit tests inherited from Clojure 1.8.0 don't seem
   to test it either.  
   Signatures are NOT recursive.  
   **TODO:** add tests to verify or exlcude this possibility.

      **Warning:** Vectors of signatures probably
   work, but hasn't been tested. It's almost surely better not to 
   mix them.  
   **TODO:** add tests to verify or exlcude this possibility.
   
      **Warning:** currrently there is no check whether 
   `dispatch-value` is legal or not. A misleading exception may be 
   thrown at some unspecified later time.  
   **TODO:** add validation.
  
  - `fn-tail`: one or more of arglist plus function body, which are
     passed to `fn` to generate the method function. Note that
     signatures are only intended to support single arity method
     functions.

  **Note:** unlike [[defmulti]], re-evaluating `defmethod` will
  replace any existing method, mutating the `MultiFn`
  (rather than creating a new `MultiFn` and re-binding the `Var`
  holding it."
  
  {:added "faster-multimethods 0.0.0"}
  
  [multifn dispatch-val & fn-tail]
  
  (assert (legal-dispatch-value? dispatch-val))
  `(.addMethod 
     ~(with-meta multifn 
        {:tag 'palisades.lakes.multimethods.java.MultiFn}) 
     ~dispatch-val (fn ~multifn ~@fn-tail)))

(defn remove-all-methods
  "Removes all of the methods of multimethod.

   `palisades.lakes.multimethods.core/remove-all-methods` can only be used 
   with multimethods defined with 
   `[[palisades.lakes.multimethods.core/defmulti]]`.

   **Warning:** despite the name, this actually removes all the 
   methods, _preferences_, and clears the cache.

   **Warning:** mutates the MutliFn in place."
  
  {:added "faster-multimethods 0.0.0"
   :static true} 
  
  [^palisades.lakes.multimethods.java.MultiFn multifn]
  
  (.reset multifn))

(defn remove-method
  "Removes the method of multimethod associated with dispatch-value.

   `palisades.lakes.multimethods.core/remove-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`.

   **Warning:** mutates the MutliFn in place."

  
  {:added {:added "faster-multimethods 0.0.0"}
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val]
  (assert (legal-dispatch-value? dispatch-val))
  (.removeMethod multifn dispatch-val))

(defn prefer-method
  "Causes the multimethod to prefer matches of dispatch-val-x over dispatch-val-y 
   when there is a conflict.

   `palisades.lakes.multimethods.core/prefer-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`.

   **Warning:** mutates the MutliFn in place."
  
  {:added {:added "faster-multimethods 0.0.0"}
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val-x dispatch-val-y]
  (assert (legal-dispatch-value? dispatch-val-x))
  (assert (legal-dispatch-value? dispatch-val-y))
  (.preferMethod multifn dispatch-val-x dispatch-val-y))

(defn methods

  "Given a multimethod, returns a map of dispatch values -> dispatch fns.

   `palisades.lakes.multimethods.core/methods` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "faster-multimethods 0.0.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn] 
  (.getMethodTable multifn))

(defn get-method

  "Given a multimethod and a dispatch value, returns the dispatch fn
  that would apply to that value, or nil if none apply and no default.

   `palisades.lakes.multimethods.core/get-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "faster-multimethods 0.0.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val] 
  (assert (legal-dispatch-value? dispatch-val))
  (.getMethod multifn dispatch-val))

(defn prefers

  "Given a multimethod, returns a map of 
   preferred value -> set of other values.

   `palisades.lakes.multimethods.core/prefers` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "faster-multimethods 0.0.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn] 
  (.getPreferTable multifn))

;;----------------------------------------------------------------
