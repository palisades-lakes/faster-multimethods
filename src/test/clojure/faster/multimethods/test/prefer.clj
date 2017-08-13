(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns faster.multimethods.test.prefer
  {:doc "Check prefer-method"
   :author "palisades dot lakes at gmail dot com"
   :since "2017-08-12"
   :version "2017-08-12"}
  (:require [clojure.test :as test]
            [faster.multimethods.core :as fmc]))
;; mvn clojure:test -Dtest=faster.multimethods.test.prefer
;;----------------------------------------------------------------
(test/deftest fmc-prefers-with-local-hierarchy
  (def fmc-hierarchy 
    (let [h (make-hierarchy)
          h (derive h ::c0 ::b0)
          h (derive h ::d0 ::c0)
          h (derive h ::d0 ::a0)]
      h))
  (fmc/defmulti fmc identity :hierarchy #'fmc-hierarchy)
  (fmc/defmethod fmc ::a0 [x] [::a0 x]) 
  (fmc/defmethod fmc ::c0 [x] [::c0 x]) 
  (fmc/prefer-method fmc ::b0 ::a0)
  (test/is (= [::c0 ::d0] (fmc ::d0))))
;;----------------------------------------------------------------
(test/deftest MultiFn-prefers-bug-with-local-hierarchy
  (def bug-hierarchy 
    (let [h (make-hierarchy)
          h (derive h ::c0 ::b0)
          h (derive h ::d0 ::c0)
          h (derive h ::d0 ::a0)]
      h))
  (defmulti bug identity :hierarchy #'bug-hierarchy)
  (defmethod bug ::a0 [x] [::a0 x]) 
  (defmethod bug ::c0 [x] [::c0 x]) 
  (prefer-method bug ::b0 ::a0)
  (test/is 
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [::c0 ::d0] (bug ::d0)))))
;;----------------------------------------------------------------
; fails with Clojure 1.8.0
#_(test/deftest MultiFn-prefers-with-local-hierarchy
  (def local-hierarchy 
    (let [h (make-hierarchy)
          h (derive h ::c0 ::b0)
          h (derive h ::d0 ::c0)
          h (derive h ::d0 ::a0)]
      h))
  (defmulti local identity :hierarchy #'local-hierarchy)
  (defmethod local ::a0 [x] [::a0 x]) 
  (defmethod local ::c0 [x] [::c0 x]) 
  (prefer-method local ::b0 ::a0)
  (test/is (= [::c0 ::d0] (local ::d0))))
;;----------------------------------------------------------------
(test/deftest MultiFn-prefers-with-global-hierarchy
  (derive ::c1 ::b1)
  (derive ::d1 ::c1)
  (derive ::d1 ::a1)
  (defmulti global identity)
  (defmethod global ::a1 [x] [::a1 x]) 
  (defmethod global ::c1 [x] [::c1 x]) 
  (prefer-method global ::b1 ::a1)
  (global ::d1)
  (test/is (= [::c1 ::d1] (global ::d1))))
;;----------------------------------------------------------------
