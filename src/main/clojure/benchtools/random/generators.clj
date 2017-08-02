(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns benchtools.random.generators
  
  {:doc "Random object generators assuming prngs are represented
         as clojure.lang.Function --- in other words, no direct 
         dependence on any prng library."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-05-29"
   :version "2017-07-25"}
  
  (:import [java.util Collections]
           [benchtools.java.sets ByteInterval DoubleInterval 
            FloatInterval IntegerInterval LongInterval
            ShortInterval]))
;;----------------------------------------------------------------
(defn byte-intervals ^clojure.lang.IFn [^clojure.lang.IFn$L g]
  (fn byte-interval ^benchtools.java.sets.ByteInterval []
    (ByteInterval/generate g)))
(defn double-intervals ^clojure.lang.IFn [^clojure.lang.IFn$D g]
  (fn double-interval ^benchtools.java.sets.DoubleInterval []
    (DoubleInterval/generate g)))
(defn float-intervals ^clojure.lang.IFn [^clojure.lang.IFn$D g]
  (fn float-interval ^benchtools.java.sets.FloatInterval []
    (FloatInterval/generate g)))
(defn integer-intervals ^clojure.lang.IFn [^clojure.lang.IFn$L g]
  (fn integer-interval ^benchtools.java.sets.IntegerInterval []
    (IntegerInterval/generate g)))
(defn long-intervals ^clojure.lang.IFn [^clojure.lang.IFn$L g]
  (fn long-interval ^benchtools.java.sets.LongInterval []
    (LongInterval/generate g)))
(defn short-intervals ^clojure.lang.IFn [^clojure.lang.IFn$L g]
  (fn short-interval ^benchtools.java.sets.ShortInterval []
    (ShortInterval/generate g)))
;;----------------------------------------------------------------
(defn random-singleton-set
  ^clojure.lang.IFn [^clojure.lang.IFn generator]
  (fn random-singleton-set ^java.util.Set []
    (Collections/singleton (generator))))
;;----------------------------------------------------------------
(defn generate-array 
  (^objects [^clojure.lang.IFn generator 
             ^long n
             ^Class element-type]
    (let [^objects sets (make-array element-type n)]
      (dotimes [i n] (aset sets i (generator)))
      sets))
  (^objects [^clojure.lang.IFn generator 
             ^long n]
    (generate-array generator n java.lang.Object)))
;;----------------------------------------------------------------
