(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.transitivity
  
  {:doc "dummy classes for prefer-method tests."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-09-14"
   :version "2017-09-14"}
  (:require [clojure.test :as test]
            [palisades.lakes.multimethods.core :as fm]
            [palisades.lakes.multimethods.test.classes])
  (:import [palisades.lakes.multimethods.test.classes 
            A B C D A0 B0 C0 D0 A1 B1 C1 D1 A2 B2 C2 D2 E2]))
;; mvn clojure:test -Dtest=palisades.lakes.multimethods.test.transitivity
;;----------------------------------------------------------------
;; prefer-method transitivity bugs
;;----------------------------------------------------------------
(test/deftest transitive1
  
  (defmulti transitive class)
  (defmethod transitive A [x] [A (class x)]) 
  (defmethod transitive C [x] [C (class x)]) 
  (prefer-method transitive A B)
  (prefer-method transitive B C)

  ;; 'Bug' in Clojure 1.8.0: this should not throw an exception?
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [A D] (transitive (D.)))))
  
  (fm/defmulti fm-transitive class)
  (fm/defmethod fm-transitive A [x] [A (class x)]) 
  (fm/defmethod fm-transitive C [x] [C (class x)]) 
  (fm/prefer-method fm-transitive A B)
  (fm/prefer-method fm-transitive B C)

  (test/is (= [A D] (fm-transitive (D.)))))
;;----------------------------------------------------------------
(test/deftest transitive2
  (defn classes [x0 x1] [(class x0) (class x1)])
  
  (defmulti transitive2 classes)
  (defmethod transitive2 [B0 B1] [x0 x1] [[B0 B1] (classes x0 x1)])
  (defmethod transitive2 [C0 C1] [x0 x1] [[C0 C1] (classes x0 x1)])
  (prefer-method transitive2 [A0 A1] [B0 B1])
  ;; 'Bug' in Clojure 1.8.0: this should not throw an exception?
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [[C0 C1] [D0 D1]] (transitive2 (D0.) (D1.)))))
  
  (fm/defmulti fm-transitive2 classes)
  (fm/defmethod fm-transitive2 [B0 B1] [x0 x1] [[B0 B1] (classes x0 x1)])
  (fm/defmethod fm-transitive2 [C0 C1] [x0 x1] [[C0 C1] (classes x0 x1)])
  (fm/prefer-method fm-transitive2 [A0 A1] [B0 B1])
  (test/is (= [[C0 C1] [D0 D1]] (fm-transitive2 (D0.) (D1.)))))
;;----------------------------------------------------------------
;; (prefers x ancestor-of-y) wrongly implies (prefers x y)
;;----------------------------------------------------------------
(test/deftest sins-of-the-parents
  
  (defmulti cpi class)
  (defmethod cpi B2 [x] [B2 (class x)]) 
  (defmethod cpi E2 [x] [E2 (class x)]) 
  (prefer-method cpi B2 D2)
  ;; This should throw the exception
  (test/is 
    (= [B2 C2] (cpi (C2.)))
    #_(thrown-with-msg? 
        IllegalArgumentException 
        #"Multiple methods in multimethod"
        (= [::pi-b ::pi-c] (fpi ::pi-c))))
  
  (fm/defmulti fpi class)
  (fm/defmethod fpi B2 [x] [B2 (class x)]) 
  (fm/defmethod fpi E2 [x] [E2 (class x)]) 
  (fm/prefer-method fpi B2 D2)
  ;; This should throw the exception
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [B2 C2] (cpi (C2.))))))
;;----------------------------------------------------------------
