(set! *warn-on-reflection* false)
(set! *unchecked-math* false)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.doc.codox
  
  {:doc "Generate codox for faster-multimethods."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-08-05"
   :date "2017-10-23"}
  
  (:require [clojure.java.io :as io]
            [codox.main :as codox])
  #_(:import [org.eclipse.jgit.storage.file FileRepositoryBuilder]))
;;----------------------------------------------------------------
#_(set! *warn-on-reflection* true)
#_(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
;; TODO: get this from the pom somehow?
(def version "0.1.0")
(def group "palisades-lakes")
(def project-name "faster-multimethods")
(def description "Faster, (almost) backwards compatible multimethods.")
(def doc-files ["docs/overview.md" "docs/benchmarks.md" "docs/lookup.md" 
                "docs/changes.md"])
(def namespaces :all)
;;----------------------------------------------------------------
(defn- src-path [subfolder] (str "src/" subfolder "/clojure"))
(defn- src-pattern [subfolder] (re-pattern (src-path subfolder)))
(defn- src-uri [subfolder]
  (str "https://github.com/"
       group
       "/blob/"
       project-name
       "-{version}/"
       (src-path subfolder)
       "/{classpath}#L{line}"))
;; source-uri for a branch rather than a tag
#_(str ""
       branch
       "/--/src/main/clojure/{classpath}#L{line}")
;;:source-uri "file:///{filepath}#line={line}"
;;----------------------------------------------------------------
(let [#_branch #_(.getBranch 
                   (.build 
                     (.findGitDir
                       (.readEnvironment
                         (.setGitDir 
                           (FileRepositoryBuilder.)
                           (io/file ".git"))))))
      source-paths (mapv src-path ["main" #_"test" #_"scripts"])
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
               ;;:doc-paths ["docs/codox"]
               :doc-files doc-files
               :html {:namespace-list :flat}
               ;;:exclude-vars #"^(map)?->\p{Upper}"
               :metadata {:doc "TODO: write docs"
                          :doc/format :markdown}
               :themes [:default]}]
  (codox/generate-docs options))
;;----------------------------------------------------------------

