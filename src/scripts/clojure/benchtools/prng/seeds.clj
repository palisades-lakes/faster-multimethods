(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns benchtools.prng.seeds
  
  {:doc "Generate independent seeds for benchtools.prng."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-04-05"
   :version "2017-07-25s"}
  
  (:require [clojure.java.io :as io]
            [benchtools.random.seed :as seed])
  (:import java.time.LocalDate))
;;----------------------------------------------------------------
;; for Well44497b
(seed/write
  (seed/generate-randomdotorg-seed 1391)
  (io/file "src" "main" "resources" "seeds" 
           (str "Well44497b-" (LocalDate/now) ".edn")))
;;----------------------------------------------------------------
;; for Mersenne Twister
(seed/write
  (seed/generate-randomdotorg-seed 624)
  (io/file  "src" "main" "resources" "seeds" 
            (str "MT-" (LocalDate/now) ".edn")))
;;----------------------------------------------------------------

