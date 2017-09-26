(set! *warn-on-reflection* false)
(set! *unchecked-math* false)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.doc.codox
  
  {:doc "Generate codox for faster-multimethods."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-08-05"
   :version "2017-09-26"}
  
  (:require [clojure.java.io :as io]
            [codox.main :as codox]))
;;----------------------------------------------------------------
#_(set! *warn-on-reflection* true)
#_(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(let [version "0.0.7"
      project-name "faster-multimethods"
      description "Faster, (almost) backwards compatible multimethods."
      options {:name project-name
               :version version 
               :description description
               :language :clojure
               :root-path (io/file "./")
               :output-path "target/doc"
               :source-paths ["src/main/clojure"
                              #_"src/test/clojure"
                              #_"src/scripts/clojure"]
               ;;https://github.com/palisades-lakes/faster-multimethods/blob/faster-multimethods-0.0.7/src/main/clojure/palisades/lakes/multimethods/core.clj
               :source-uri 
               {#"src/main/clojure"
                (str "https://github.com/palisades-lakes/"
                      project-name
                      "/blob/"
                      project-name
                      "-{version}/"
                      "src/main/clojure"
                      "/{classpath}#L{line}")
                #"src/test/clojure"
                (str "https://github.com/palisades-lakes/"
                      project-name
                      "/blob/"
                      project-name
                      "-{version}/"
                      "src/test/clojure"
                      "/{classpath}#L{line}")
                #"src/scripts/clojure"
                (str "https://github.com/palisades-lakes/"
                      project-name
                      "/blob/"
                      project-name
                      "-{version}/"
                      "src/scripts/clojure"
                      "/{classpath}#L{line}")}
               :namespaces :all
               ;;:doc-paths ["docs"]
               :doc-files ["docs/overview.md" 
                           "docs/benchmarks.md" 
                           "docs/changes.md" ]
               :html {:namespace-list :flat}
               ;;:exclude-vars #"^(map)?->\p{Upper}"
               :metadata {:doc "TODO: write docs"
                          :doc/format :markdown}
               :themes [:default]}]
  (codox/generate-docs options))
;;----------------------------------------------------------------

