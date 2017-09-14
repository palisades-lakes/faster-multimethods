(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.nohierarchy
  
  {:doc "prefers transitivity with signature dispatch and no hierarchy."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-09-14"
   :version "2017-09-14"}
  (:require [clojure.test :as test]
            [palisades.lakes.multimethods.core :as d]
            [palisades.lakes.multimethods.test.classes])
  (:import [palisades.lakes.multimethods.test.classes 
            A B C D A0 B0 C0 D0 A1 B1 C1 D1 A2 B2 C2 D2 E2]))
;; mvn clojure:test -Dtest=palisades.lakes.multimethods.test.signatures
;;----------------------------------------------------------------
;; need a function
(defn extract 
  ([x0] (d/extract-signature x0))
  ([x0 x1]  (d/extract-signature x0 x1)))
;;----------------------------------------------------------------
;; prefer-method transitivity
;;----------------------------------------------------------------
(test/deftest transitive1
  (d/defmulti transitive1 extract :hierarchy false)
  (d/defmethod transitive1 A [x] [A (class x)]) 
  (d/defmethod transitive1 C [x] [C (class x)]) 
  (d/prefer-method transitive1 A B)
  (d/prefer-method transitive1 B C)
  (test/is (= [A D] (transitive1 (D.)))))
;;----------------------------------------------------------------
(test/deftest transitive2
  (d/defmulti transitive2 extract :hierarchy false)
  (d/defmethod transitive2 
    (d/signature B0 B1) 
    [x0 x1] 
    [(d/signature B0 B1) (d/extract-signature x0 x1)])
  (d/defmethod transitive2 
    (d/signature C0 C1) 
    [x0 x1] 
    [(d/signature C0 C1) (d/extract-signature x0 x1)])
  (d/prefer-method transitive2 
                    (d/signature A0 A1) 
                    (d/signature B0 B1))
  (test/is (= [(d/signature C0 C1) (d/signature D0 D1)]
              (transitive2 (D0.) (D1.)))))
;;----------------------------------------------------------------
;; (prefers x ancestor-of-y) wrongly implies (prefers x y)
;;----------------------------------------------------------------
(test/deftest sins-of-the-parents
  (d/defmulti sins-of-the-parents extract :hierarchy false)
  (d/defmethod sins-of-the-parents B2 [x] [B2 (class x)]) 
  (d/defmethod sins-of-the-parents E2 [x] [E2 (class x)]) 
  (d/prefer-method sins-of-the-parents B2 D2)
  ;; This should throw the exception
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [B2 C2] (sins-of-the-parents (C2.))))))
;;----------------------------------------------------------------
