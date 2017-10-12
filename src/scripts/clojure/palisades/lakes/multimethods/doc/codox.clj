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
(def version "0.1.0")
(def project-name "faster-multimethods")
(def description "Faster, (almost) backwards compatible multimethods.")
(def doc-files ["docs/overview.md" 
                "docs/benchmarks.md" 
                "docs/lookup.md"
                "docs/changes.md" ])
(def namespaces :all)
;;----------------------------------------------------------------
(defn- src-path [branch] (str "src/" branch "/clojure"))
(defn- src-pattern [branch] (re-pattern (src-path branch)))
(defn- src-uri [branch]
  (str "https://github.com/palisades-lakes/"
       project-name
       "/blob/"
       project-name
       "-{version}/"
       (src-path branch)
       "/{classpath}#L{line}"))
;;----------------------------------------------------------------
(let [source-paths (mapv src-path ["main" #_"test" #_"scripts"])
      source-uri (into {} (map #(vector (src-pattern %) (src-uri %))
                               ["main" "test" "scripts"]))
      options {:name project-name
               :version version 
               :description description
               :language :clojure
               :root-path (io/file "./")
               :output-path "target/doc"
               :source-paths source-paths
               :source-uri source-uri
               :namespaces namespaces
               ;;:doc-paths ["docs"]
               :doc-files doc-files
               :html {:namespace-list :flat}
               ;;:exclude-vars #"^(map)?->\p{Upper}"
               :metadata {:doc "TODO: write docs"
                          :doc/format :markdown}
               :themes [:default]}]
  (codox/generate-docs options))
;;----------------------------------------------------------------

