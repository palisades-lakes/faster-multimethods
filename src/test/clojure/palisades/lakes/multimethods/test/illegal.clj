(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.illegal
  
  {:doc "check that illegal dispatch values cause errors."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-10-06"
   :version "2017-10-06"}
  (:require [clojure.test :as test]
            [palisades.lakes.multimethods.core :as d]
            [palisades.lakes.multimethods.test.classes])
  (:import [palisades.lakes.multimethods.java 
            MultiFnWithHierarchy MultiFnWoutHierarchy]
           [palisades.lakes.multimethods.test.classes 
            A B C D A0 B0 C0 D0 A1 B1 C1 D1 A2 B2 C2 D2 E2]))
;; mvn clojure:test -Dtest=palisades.lakes.multimethods.test.signatures
;;----------------------------------------------------------------
;; need a function
(defn extract 
  ([x0] (d/extract-signature x0))
  ([x0 x1] (d/extract-signature x0 x1)))
;;----------------------------------------------------------------
;; no hierarky
;;----------------------------------------------------------------
(test/deftest nohierarky-test
  (d/defmulti nohierarky extract :hierarchy false)
  (test/is (instance? MultiFnWoutHierarchy nohierarky))
  ;; no exception here
  (d/defmethod nohierarky A [x] [A (class x)])
  ;; no exception here
  (test/is (nil? (d/get-method nohierarky ::a)))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/defmethod nohierarky ::a [x] [::a x]))) 
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/defmethod nohierarky [A B] [x y] [A B (class x)])))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/prefer-method nohierarky ::a B)))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/prefer-method nohierarky A ::b)))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/prefer-method nohierarky [A B] [B C]))))
;;----------------------------------------------------------------
;; hierarky
;;----------------------------------------------------------------
(test/deftest hierarky-test
  ;; no exception
  (d/defmulti good-default identity :default ::a)
  (test/is (instance? MultiFnWithHierarchy good-default))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/defmulti bad-default 
        identity
        :default :a)))
  (d/defmulti hierarky 
    (fn dispatch-function 
      ([x] (if (keyword? x) x (class x)))
      ([x y] [(dispatch-function x) (dispatch-function y)])))
  ;; no exception here
  (d/defmethod hierarky ::a [x] [::a x])
  ;; no exception here
  (test/is (nil? (d/get-method hierarky :a)))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/defmethod hierarky :a [x] [:a x]))) 
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/defmethod hierarky [A 1] [x y] [A 1 (class x)])))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/prefer-method hierarky ::a :b)))
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"not legal"
      (d/prefer-method hierarky [A [B 1]] [1 [B C]]))))
;;----------------------------------------------------------------
