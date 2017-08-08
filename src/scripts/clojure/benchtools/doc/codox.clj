(ns benchtools.codox
  
  {:doc "Generate codox for benchtools."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-08-05"
   :version "2017-08-05"}
  
  (:require [clojure.java.io :as io]
            [codox.main :as codox]))
;;----------------------------------------------------------------
(let [options  {:project 
                {:name "benchtools"
                 :version "0.0.0" 
                 :description 
                 "Currently unstable utilities for benchmarking clojure and java."}
                :language :clojure
                :root-path (io/file "./")
                :output-path "docs/codox"
                :source-paths ["src/main/clojure"]
                ;;:source-uri "https://github.com/palisades-lakes/benchtools/blob/{version}/{filepath}#L{line}"
                :namespaces :all
                ;;:doc-paths ["docs"]
                :doc-files ["README.md"]
                :html {:namespace-list :flat}
                ;;:exclude-vars #"^(map)?->\p{Upper}"
                :metadata {:doc "TODO: write docs"
                           :doc/format :markdown}
                :themes [:default]}]
  (codox/generate-docs options))
;;----------------------------------------------------------------

