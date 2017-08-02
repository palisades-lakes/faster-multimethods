(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns benchtools.random.prng
  
  {:doc "pseudo-random number generators."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-04-05"
   :version "2017-07-25"}
  
  (:require [benchtools.random.seed :as seed])
  (:import [java.util Collection]
           [org.apache.commons.rng UniformRandomProvider]
           [org.apache.commons.rng.sampling CollectionSampler]
           [org.apache.commons.rng.sampling.distribution 
            ContinuousSampler ContinuousUniformSampler 
            DiscreteSampler DiscreteUniformSampler]
           [org.apache.commons.rng.simple RandomSource]))
;;----------------------------------------------------------------
;; http://www.iro.umontreal.ca/~panneton/WELLRNG.html
;; http://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/random/Well44497b.html
(defn- well44497b 
  ^org.apache.commons.rng.UniformRandomProvider [^ints seed]
  (assert (== 1391 (int (alength seed))))
  (RandomSource/create RandomSource/WELL_44497_B seed nil))
;;----------------------------------------------------------------
#_(defn- mersenne-twister 
    ^org.apache.commons.rng.UniformRandomProvider [^ints seed]
    (assert (== 624 (int (alength seed))))
    (RandomSource/create RandomSource/MT seed nil))
;;----------------------------------------------------------------
;; default is well44497b.

(defn uniform-random-provider
  ^org.apache.commons.rng.UniformRandomProvider [seed]
  (well44497b (seed/seed seed)))
;;----------------------------------------------------------------
(defn uniform-double-generator 
  ^clojure.lang.IFn$D [^double umin
                       ^double umax
                       ^UniformRandomProvider urp]
  (let [^ContinuousSampler g (ContinuousUniformSampler. 
                               urp umin umax)]
    (fn uniform-double-generator ^double [] (.sample g))))
;;----------------------------------------------------------------
(defn uniform-int-generator 
  ^clojure.lang.IFn$L [^long umin
                       ^long umax
                       ^UniformRandomProvider urp]
  (let [^DiscreteSampler g (DiscreteUniformSampler. 
                             urp (int umin) (int umax))]
    (fn uniform-int-generator ^long [] (.sample g))))
;;----------------------------------------------------------------
(defn uniform-long-generator 
  ^clojure.lang.IFn$L [^long umin
                       ^long umax
                       ^UniformRandomProvider urp]
  (let [^DiscreteSampler g (DiscreteUniformSampler. 
                             urp umin umax)]
    (fn uniform-long-generator ^long [] (.sample g))))
;;----------------------------------------------------------------
(defn uniform-element-generator 
  ^clojure.lang.IFn [^Collection c ^UniformRandomProvider urp]
  (let [^CollectionSampler cs (CollectionSampler. urp c)]
    (fn random-set [] ((.sample cs)))))
;;----------------------------------------------------------------
;; TODO: upper bound exclusive?
(defn random-doubles 
  (^doubles [^long n ^double lower ^double upper
             ^UniformRandomProvider urp]
    (assert (<= lower upper))
    (let [^ContinuousSampler g (ContinuousUniformSampler.
                                 urp lower upper)
          a (double-array n)]
      (dotimes [i n] (aset-double a i (.sample g)))
      a)))
;;----------------------------------------------------------------
(defn uniform-ints
  (^ints [^long n ^long lower ^long upper
          ^UniformRandomProvider urp]
    (assert (<= lower upper))
    (let [^DiscreteSampler g (DiscreteUniformSampler. 
                               urp (int lower) (int upper))
          a (int-array n)]
      (dotimes [i n] (aset-double a i (.sample g)))
      a)))
;;----------------------------------------------------------------
;; NOT thread safe!
(defn cycling-generator ^clojure.lang.IFn [generators]
  (let [^ints i (int-array 1)
        n (int (count generators))]
    (aset-int i 0 0)
    (fn cycling-generator []
      (let [ii (aget i 0)
            gi (nth generators ii)]
        (aset-int i 0 (mod (inc ii) n))
        (gi)))))
;;----------------------------------------------------------------
(defn nested-uniform-generator 
  ^clojure.lang.IFn [generators ^UniformRandomProvider urp]
  (let [^CollectionSampler cs (CollectionSampler. urp generators)]
      (fn nested-uniform-generator [] ((.sample cs)))))
;;----------------------------------------------------------------
