(set! *warn-on-reflection* true) 
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns benchtools.random.seed
  
  {:doc "Independent seed generation and seed resource IO."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-04-05"
   :version "2017-07-25"}
  
  (:refer-clojure :exclude [read write])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp])
  (:import [java.io PushbackReader]
           [org.apache.commons.rng.simple.internal 
            ByteArray2IntArray]
           [org.uncommons.maths.random 
            DefaultSeedGenerator RandomDotOrgSeedGenerator]))
;;----------------------------------------------------------------
(defn generate-default-seed ^ints [^long size]
  (.convert 
    (ByteArray2IntArray.)
    (.generateSeed (DefaultSeedGenerator/getInstance) 
      (* 4 (int size)))))
;;----------------------------------------------------------------
(defn generate-randomdotorg-seed ^ints [^long size]
  (.convert 
    (ByteArray2IntArray.)
    (.generateSeed (RandomDotOrgSeedGenerator.) 
      (* 4 (int size)))))
;;----------------------------------------------------------------
(defn write [^ints seed f]
  (with-open [w (io/writer f)]
    (binding [*out* w]
      (pp/pprint (into [] seed)))))
;;----------------------------------------------------------------
(defn read ^ints [f]
  (with-open [r (PushbackReader. (io/reader f))]
    (int-array (edn/read r))))
;;----------------------------------------------------------------
;; TODO: move somewhere more appropriate
(let [c (class (int-array 0))]
  (defn- int-array? [x] (instance? c x)))
;;----------------------------------------------------------------
;; if the seed is a string, assume it's the name of a resource.
;; if it's a resource, read it.
;; TODO: what about ordinary files?
(defn seed ^ints [x]
  (cond (string? x) (recur (io/resource x))
        (instance? java.net.URL x) (recur (read x))
        (int-array? x) x
        :else
        (throw 
          (IllegalArgumentException. 
            (print-str "Invalid seed source:" x)))));
;;----------------------------------------------------------------
