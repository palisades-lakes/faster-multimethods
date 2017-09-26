(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.core
  
  {:doc "Faster multimethod method lookup."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-06-02"
   :version "2017-08-23"}
  (:refer-clojure :exclude [defmulti defmethod remove-all-methods
                            remove-method prefer-method methods
                            get-method prefers]))
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
(defmacro defmulti
  "*Clojure 1.8.0:*

  Creates a new multimethod with the associated dispatch function.
  The docstring and attr-map are optional.

  Options are key-value pairs and may be one of:

  :default

  The default dispatch value, defaults to :default

  :hierarchy

  The value used for hierarchical dispatch (e.g. ::square is-a ::shape)

  Hierarchies are type-like relationships that do not depend upon type
  inheritance. By default Clojure's multimethods dispatch off of a
  global hierarchy map.  However, a hierarchy relationship can be
  created with the derive function used to augment the root ancestor
  created with make-hierarchy.

  Multimethods expect the value of the hierarchy option to be supplied as
  a reference type e.g. a var (i.e. via the Var-quote dispatch macro #'
  or the var special form).

  *faster-multimethods:*

  The dispatch function must return a legal dispatch value (but
  there is no guarantee of what or where an exception will be 
  thrown if this is not true). A legal dispatch value is either
  an *atomic dispatch value*, or a vector containing atomic dispatch
  values. An *atomic dispatch value* must be either a `Class`, an
  instance of `clojure.lang.Named` (that is, a `Symbol` or a 
  `Keyword`), or a signature 
  (an instance of `palisades.lakes.multimethods.java.signature.Signature`).

   **Note:** currrently there is no check whether the `dispatch-value`
   is legal or not. A misleading exception may be thrown at some
   unspecified later time.

  If the value of :hierarchy is `false` or `nil`, then atomic 
  dispatch values are restricted to Classes and signatures.
  In that case, :default is ignored.
  "
  
  {:arglists '([name docstring? attr-map? dispatch-fn & options])
   :added "Clojure 1.0"}
  
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

(defmacro defmethod
  "Creates and installs a new method for ```multifn``` associated 
   with ```dispatch-value```. 

   `palisades.lakes.multimethods.defmethod` can only be used 
   with multimethods defined with 
   `palisades.lakes.multimethods.core/defmulti`.

   A legal `dispatch-value` is either
   an *atomic dispatch value*, or a vector containing atomic dispatch
   values. An *atomic dispatch value* must be either a `Class`, an
   instance of `clojure.lang.Named` (that is, a `Symbol` or a 
   `Keyword`), or a signature 
   (an instance of `palisades.lakes.multimethods.java.signature.Signature`).

   **Note:** currrently there is no check whether the `dispatch-value`
   is legal or not. A misleading exception may be thrown at some
   unspecified later time.

   If the value of :hierarchy in the corresponding `defmulti`
   is `false` or `nil`, then atomic 
   dispatch values are restricted to Classes and signatures.
   "
  {:added "Clojure 1.0"}
  
  [multifn dispatch-val & fn-tail]
  
  `(.addMethod ~(with-meta multifn {:tag 'palisades.lakes.multimethods.java.MultiFn}) 
     ~dispatch-val (fn ~multifn ~@fn-tail)))

(defn remove-all-methods
  "Removes all of the methods of multimethod.

   `palisades.lakes.multimethods.core/remove-all-methods` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "Clojure 1.2"
   :static true} 
  
  [^palisades.lakes.multimethods.java.MultiFn multifn]
  
  (.reset multifn))

(defn remove-method
  "Removes the method of multimethod associated with dispatch-value.

   `palisades.lakes.multimethods.core/remove-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "Clojure 1.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val]
  (.removeMethod multifn dispatch-val))

(defn prefer-method
  "Causes the multimethod to prefer matches of dispatch-val-x over dispatch-val-y 
   when there is a conflict.

   `palisades.lakes.multimethods.core/prefer-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  {:added "Clojure 1.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val-x dispatch-val-y]
  (.preferMethod multifn dispatch-val-x dispatch-val-y))

(defn methods
  "Given a multimethod, returns a map of dispatch values -> dispatch fns.

   `palisades.lakes.multimethods.core/methods` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  

  {:added "Clojure 1.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn] 
  (.getMethodTable multifn))

(defn get-method
  "Given a multimethod and a dispatch value, returns the dispatch fn
  that would apply to that value, or nil if none apply and no default.

   `palisades.lakes.multimethods.core/get-method` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  

  {:added "Clojure 1.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn dispatch-val] 
  (.getMethod multifn dispatch-val))

(defn prefers
  "Given a multimethod, returns a map of preferred value -> set of other values.

   `palisades.lakes.multimethods.core/prefers` can only be used 
   with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  

  {:added "Clojure 1.0"
   :static true}
  [^palisades.lakes.multimethods.java.MultiFn multifn] 
  (.getPreferTable multifn))

;;----------------------------------------------------------------

(defmacro signature 
  
  "Return an appropriate implementation of `Signature` for the
   `Class` arguments..

   `palisades.lakes.multimethods.core/signature` can only be used 
   as a dispatch function with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  ([c0] `(with-meta c0 {:tag 'Class}))
  ([c0 c1] 
    `(palisades.lakes.multimethods.java.signature.Signature2.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})))
  ([c0 c1 c2] 
    `(palisades.lakes.multimethods.java.signature.Signature3.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})
       ~(with-meta c2 {:tag 'Class})))
  ([c0 c1 c2 & cs] 
    `(SignatureN.
       ~(with-meta c0 {:tag 'Class})
       ~(with-meta c1 {:tag 'Class})
       ~(with-meta c2 {:tag 'Class})
       ~(with-meta cs {:tag 'clojure.lang.ArraySeq}))))

(defmacro extract-signature 
  
  "Return an appropriate implementation of `Signature` for the
   arguments, by applying `getClass` as needed.

   `palisades.lakes.multimethods.core/extract-signature` can only be used 
   as a dispatch function with multimethods
   defined with `palisades.lakes.multimethods.core/defmulti`."
  
  ([x0] `(.getClass ~(with-meta x0 {:tag 'Object})))
  ([x0 x1] 
    `(palisades.lakes.multimethods.java.signature.Signature2.
       (.getClass ~(with-meta x0 {:tag 'Object}))
       (.getClass ~(with-meta x1 {:tag 'Object}))))
  ([x0 x1 x2] 
    `(palisades.lakes.multimethods.java.signature.Signature3.
       (.getClass ~(with-meta x0 {:tag 'Object}))
       (.getClass ~(with-meta x1 {:tag 'Object}))
       (.getClass ~(with-meta x2 {:tag 'Object}))))
  ([x0 x1 x2 & xs] 
    `(SignatureN/extract 
       ~x0 ~x1 ~x2 ~with-meta xs {:tag 'clojure.lang.ArraySeq})))
;;----------------------------------------------------------------
