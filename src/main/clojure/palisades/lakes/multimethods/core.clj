(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.core
  
  {:doc "Faster multimethod method lookup."
   :author "palisades dot lakes at gmail dot com"
   :version "2017-12-13"}
  (:refer-clojure :exclude [defmulti defmethod remove-all-methods
                            remove-method prefer-method methods
                            get-method prefers])
  (:import [java.util List]
           [palisades.lakes.multimethods.java 
            Classes MultiFn Signature
            Signature0 Signature2 Signature3 SignatureN]))
;;----------------------------------------------------------------

(defn to-signature 
  
  "Return an appropriate instance of 
   `Signature` for the `Class` valued arguments
    (in the arity 1 case, it just returns the `Class` itself).

   **Warning:** [[to-signature]] can only be used 
   to generate dispatch values for multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  {:added "faster-multimethods 0.0.9"}
  
  (^Signature0 [] Signature0/INSTANCE)
  (^Class [^Class c0] c0)
  (^Signature2 [^Class c0 ^Class c1] 
    (Signature2. c0 c1))
  (^Signature3 [^Class c0 ^Class c1 ^Class c2] 
    (Signature3. c0 c1 c2))
  (^SignatureN [^Class c0 ^Class c1 ^Class c2 & cs] 
    (SignatureN. c0 c1 c2 ^List cs)))

(defn signature 
  
  "The standard dispatch function for the `:hierarchy false` case.
   Returns the `Class` of the argument for arity 1,
   and an appropriate implementation of `Signature` for other 
   arities, calling `(Classes/classOf xi)` as needed.

   **Warning:** [[signature]] can only be used 
   as a dispatch function with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."

  {:added "faster-multimethods 0.0.9"}
  
  (^Signature0 [] Signature0/INSTANCE)
  (^Class [x0] (Classes/classOf ^Object x0))
  (^Signature2 [x0 x1] 
    (Signature2.
      (Classes/classOf ^Object x0)
      (Classes/classOf ^Object x1)))
  (^Signature3 [x0 x1 x2] 
    (Signature3.
      (Classes/classOf ^Object x0)
      (Classes/classOf ^Object x1)
      (Classes/classOf ^Object x2)))
  (^SignatureN [x0 x1 x2 & xs] 
    (SignatureN/get 
      (Classes/classOf ^Object x0)
      (Classes/classOf ^Object x1)
      (Classes/classOf ^Object x2) 
      (mapv class xs))))

(defn signature? 
  "Is `v` a signature 
   (ie, an instance of `palisades.lakes.multimethods.java.Signature`)?"
  {:added "faster-multimethods 0.0.8"}
  [v] 
  (instance? palisades.lakes.multimethods.java.Signature v))   
;;----------------------------------------------------------------

(defn legal-dispatch-value? 
  
  "Is `v` a legal dispatch value for `multifn`?

   All multimethods accept classes and signatures.
   Multimethods with a hierarchy also accept namespace
   qualified symbols and keywords, the special 
   non-namespace-qualified keyword `:default`,
   and vectors whose elements are all legal dispatch values,
   permitting arbitrary nesting.
  
   **Note:** [[legal-dispatch-value?]] can only be used 
   with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  {:added "faster-multimethods 0.0.9"}
  
  [^MultiFn multifn v]
  
  (.isLegalDispatchValue multifn v))

;;----------------------------------------------------------------
;; dispatch value partial orderings
;; for testing/debugging, not used in method lookup
;;----------------------------------------------------------------

(defn isa<= 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  <code>([[isa<=]] multifn x y)</code>
  is eqiuvalent to 
  <code>(or ([[isa<]] multifn x y) (= x y))</code><br>.
  [[isa<=]] implies [[dominates<=]].<br>

  Throws an exception if `x` and `y` are not legal dispatch 
  values for `multifn`.

  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
  {:added "faster-multimethods 0.0.8"}
  [^MultiFn multifn x y]
  (assert (legal-dispatch-value? multifn x))
  (assert (legal-dispatch-value? multifn y))
  (.isA multifn x y))

(defn isa< 
  "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  <code>([[isa<]] multifn x y)</code>
  is eqiuvalent to 
  <code>(and ([[isa<=]] multifn x y) (not= x y))</code>.<br>
  [[isa<]] implies [[dominates<]].
  
  Throws an exception if `x` and `y` are not legal dispatch 
  values for `multifn`.

  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
  {:added "faster-multimethods 0.0.8"}
  [^MultiFn multifn x y]
  (assert (legal-dispatch-value? multifn x))
  (assert (legal-dispatch-value? multifn y))
  (and (not= x y) (isa<= multifn x y)))

#_(defn isa>= 
    "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
    {:added "faster-multimethods 0.0.8"}
    [^MultiFn multifn x y]
    (assert (legal-dispatch-value? multifn x))
    (assert (legal-dispatch-value? multifn y))
    (.isA multifn y x))

#_(defn isa> 
    "Extension of `clojure.core/isa?`, for a particular multimethod,
  to all legal dispatch values.<br>
  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
    {:added "faster-multimethods 0.0.8"}
    [^MultiFn multifn x y]
    (assert (legal-dispatch-value? multifn x))
    (assert (legal-dispatch-value? multifn y))
    (and (not= x y) (isa>= multifn x y)))

;;----------------------------------------------------------------

(defn dominates<= 
  "Transitive extension of [[isa<=]] with pairs created by
   calls to [[prefer-method]].<br>
  <code>([[dominates<=]] multifn x y)</code>
  is eqiuvalent to 
  <code>(or ([[dominates<]] multifn x y) (= x y))</code>.<br>

  Throws an exception if `x` and `y` are not legal dispatch 
  values for `multifn`.

  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
  {:added "faster-multimethods 0.0.8"}
  [^MultiFn multifn x y]
  (assert (legal-dispatch-value? multifn x))
  (assert (legal-dispatch-value? multifn y))
  (.dominates multifn x y))

(defn dominates< 
  "Transitive extension of [[isa<]] with pairs created by
   calls to [[prefer-method]].<br>
  <code>([[dominates<]] multifn x y)</code>
  is eqiuvalent to 
  <code>(and ([[dominates<=]] multifn x y) (not= x y))</code>.<br>
  
  Throws an exception if `x` and `y` are not legal dispatch 
  values for `multifn`.

  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
  {:added "faster-multimethods 0.0.8"}
  [^MultiFn multifn x y]
  (assert (legal-dispatch-value? multifn x))
  (assert (legal-dispatch-value? multifn y))
  (and (not= x y) (dominates<= multifn x y)))

#_(defn dominates>= 
    "Transitive extension of [[isa>=]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
    {:added "faster-multimethods 0.0.8"}
    [^MultiFn multifn x y]
    (assert (legal-dispatch-value? multifn x))
    (assert (legal-dispatch-value? multifn y))
    (assert (legal-dispatch-value? multifn x))
    (assert (legal-dispatch-value? multifn y))
    (.dominates multifn y x))

#_(defn dominates> 
    "Transitive extension of [[isa>]] with pairs created by
   calls to [[prefer-method]].<br>
  Not used in method lookup, which is implemented in Java, but may be useful for debugging."
    {:added "faster-multimethods 0.0.8"}
    [^MultiFn multifn x y]
    (assert (legal-dispatch-value? multifn x))
    (assert (legal-dispatch-value? multifn y))
    (and (not= x y) (dominates>= multifn x y)))

;;----------------------------------------------------------------
;; finally the multimethods
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
               (map #(str ", " %) (rest valid-keys)))))))
;;----------------------------------------------------------------
(defmacro defmulti
  "Creates a new multimethod 
   (an instance of `palisades.lakes.multimethods.java.MultiFn`)
   named `mm-name` that uses `dispatch-fn` to generate dispatch 
   values.

  - `mm-name`: a namespace qualified `Symbol`. The `MultFn` will
  be the value of the `Var` with that name. 
  
  - `docstring?` (optional): documentation string used as the
  `:doc` metadata on `#'mm-name`.

  - `attr-map?` (optional): more metadata for `#'mm-name`.

  - `dispatch-fn` an instance of `clojure.lang.IFn` that returns
   _legal dispatch values_ when applied to (supported) arguments 
   passed to the `MultiFn`. There is no check that the 
   `dispatch-fn` returns legal values. You may use
   [[legal-dispatch-value?]] in the `dispatch-fn` to validate
   the returned value, perhaps only during debugging if 
   performance is critical.

  - `options` (optional): are key-value pairs and may be one of:

      - `:hierarchy`: `false`, `nil`, or a `Var` whose value is a 
         [hierarchy](https://clojure.org/reference/multimethods).
        Used for method lookup when the dispatch
        values are or contain namespace-qualified instances of
       `Named`. 
        
          See
        [multimethods and hierarchies](https://clojure.org/reference/multimethods)
        for information on how to create and modify hierarchies.

          If the `:hierarchy` is not supplied, it defaults to 
         `#'clojure.core/global-hierarchy`.

          If `:hierarchy` is supplied and its value is `nil` or 
          `false`,
          then only `Class` and `Signature` dispatch values are 
          permitted, and method lookup is optimized for that case.

          Otherwise the value of the `:hierarchy` option 
          must be a `Var` 
         (i.e. via the Var-quote dispatch macro #' or the var 
         special form) holding a hashmap created with 
         `clojure.core/make-hierarchy`.

          **Warning:** Multimethods that use hierarchies depend on
          mutable shared state. It is possible for someone else
          to modify the shared hierarchy in a way that breaks
          method lookup.

          **Warning:** the Clojure hierarchy functions behave 
        differently for the default 
        `#'clojure.core/global-hierarchy` versus a custom local 
        hierarchy.
        Updates to the global hierarchy call `alter-var-root`
        on `#'clojure.core/global-hierarchy`, mutating shared state.
        Updates to a custom hierarchy return a new hashmap; it's 
        left to the caller to rebind any `Var`
        which might be pointing to the original hierarchy. 
        If you use a 
        local hierarchy, and modify it after the multimethod is
        created, it will have no effect unless you explicitly
        rebind the `Var` that was passed to [[defmulti]].
        This is an easy mistake to make, and not so easy to see
        what's wrong.
        
          **Note:** like the Clojure implementation, there is no
          way to determine what hierarchy is used by an existing
          multimethod, which might be useful if you wanted to 
          ensure a new multimethod had the same inheritance 
          behavior as an existing one. This may change in a future
          release.

      - `:default`: The default dispatch value, defaults to `:default`.
         Not supported, and an exception is thrown,
          when `:hierarchy false` or `:hierarchy nil`
          and the value of `:default` is not `false` or `nil`.

  **Warning:** If `#'mm-name`
  is already defined and its value is a `MultiFn`, [[defmulti]]
  silently does nothing. This is the Clojure 1.8.0 [[defmulti]]
  behavior. This may change in a future release.<br>
  (This seems to me like bad answer to the problem
  of accidentally re-evaluating a given [[defmulti]] and wiping
  out all the methods. I think a better design would make
  `MultiFn` effectively immutable. [[defmulti]], [[defmethod]],
  etc., would return new instances which are updates of the existing
  instance, and call `alter-var-root` to update the value of `#'mm-name`. 
  This, I believe, is what `defn` does.)"
  
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
          default   (get options :default (if hierarchy :default nil))]
      (assert (or hierarchy (nil? default))
              (pr-str 
                "can't supply a default dispatch value with no hierarchy:"
                default))
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
   with dispatch value `v`.

   - `multifn`: an instance of 
    `palisades.lakes.multimethods.java.MultiFn`,
    created with [[palisades.lakes.multimethods.core/defmulti]].

   - `v`: satisfies <code>([[legal-dispatch-value?]] multifn v)</code>.

  - `fn-tail`: one or more of arglist plus function body, which are
     passed to `fn` to generate the method function. Note that
     signatures only support single arity method functions.

  Throws an exception if `v` is not a legal dispatch value for
  `multifn`.

   **Warning:** mutates `multifn`.
  
  **Note:** unlike [[defmulti]], re-evaluating [[defmethod]] will
  replace any existing method for `v`.

  **Note:** [[defmethod]] can only be used 
   with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  {:added "faster-multimethods 0.0.0"}
  
  [multifn v & fn-tail]
  
  `(.addMethod 
     ~(with-meta multifn 
        {:tag 'palisades.lakes.multimethods.java.MultiFn}) 
     ~v
     (fn ~multifn ~@fn-tail)))

;;----------------------------------------------------------------

(defn remove-all-methods
  
  "Removes all of the methods of multimethod.

   **Note:** [[remove-all-methods]] can only be used 
   with multimethods defined with 
   [[palisades.lakes.multimethods.core/defmulti]].

   **Warning:** despite the name, this actually removes all the 
   _preferences_, in addition to the methods, 
   and clears the cache.

   **Warning:** mutates `multifn`."
  
  {:added "faster-multimethods 0.0.0"} 
  
  [^MultiFn multifn]
  
  (.reset multifn))

;;----------------------------------------------------------------

(defn remove-method
  
  "Removes the method of multimethod associated with `v`. Does 
   nothing if no method is defined for `v`.

   Throws an exception if `v` is not  legal dispatch value
   for `multifn`.
  
   **Note:** [[remove-method]] can only be used 
   with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]].

   **Warning:** mutates `multifn`."
  
  {:added "faster-multimethods 0.0.0"}
  
  [^MultiFn multifn v]
  
  (.removeMethod multifn v))

;;----------------------------------------------------------------

(defn prefer-method
  
  "Causes the multimethod to prefer matches of `x` over `y` 
   when there is a conflict.

   Throws an exception if `x` and `y` are not legal dispatch values
   for `multifn`.
  
   **Note:** [[prefer-method]] can only be used with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]].

   **Warning:** mutates `multifn`."
  
  {:added "faster-multimethods 0.0.0"} 
  
  [^MultiFn multifn x y]
  
  (.preferMethod multifn x y))

;;----------------------------------------------------------------

(defn methods
  
  "Given a multimethod, returns a map of 
   dispatch values -> method functions.

   **Note:** [[methods]] can only be used with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  {:added "faster-multimethods 0.0.0"}
  
  [^MultiFn multifn] 
  
  (.getMethodTable multifn))

;;----------------------------------------------------------------

(defn get-method
  
  "Given a multimethod `multifn` and a dispatch value `v`, 
  returns the defined method function
  that would be applied to any arglist that resulted in the
  dispatch value `v`. If there are no methods that are 
  applicable to `v`, returns `nil`.

  **Note:** [[get-method]] can only be used with multimethods
  defined with [[palisades.lakes.multimethods.core/defmulti]].

   **Warning:** Does NOT throw an exception if `v` is an illegal 
   dispatch value for `multifn`; returns `nil` in that case."
  
  {:added "faster-multimethods 0.0.0"}
  
  [^MultiFn multifn v] 
  
  (.getMethod multifn v))

;;----------------------------------------------------------------

(defn prefers
  
  "Given a multimethod, returns a map of 
   more preferred dispatch value -> set of less preferred dispatch
   values.

   **Note:** [[prefers]] can only be used 
   with multimethods
   defined with [[palisades.lakes.multimethods.core/defmulti]]."
  
  {:added "faster-multimethods 0.0.0"}
  
  [^MultiFn multifn] 
  
  (.getPreferTable multifn))

;;----------------------------------------------------------------
