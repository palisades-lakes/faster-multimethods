(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.prefer
  {:doc "Check MultiFn.prefers(x,y), prefer-method, etc."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-08-12"
   :version "2017-08-18"}
  (:require [clojure.test :as test]
            [palisades.lakes.multimethods.core :as fmc]))
;; mvn clojure:test -Dtest=palisades.lakes.multimethods.test.prefer
;;----------------------------------------------------------------
;; prefer-method transitivity bugs
;;----------------------------------------------------------------
(test/deftest transitivity
  
  (derive ::transitive-d ::transitive-a)
  (derive ::transitive-d ::transitive-c)
  
  (defmulti transitive identity)
  (defmethod transitive ::transitive-a [x] [::transitive-a x]) 
  (defmethod transitive ::transitive-c [x] [::transitive-c x]) 
  (prefer-method transitive ::transitive-a ::transitive-b)
  (prefer-method transitive ::transitive-b ::transitive-c)
  ;; this should not throw an exception
  (test/is 
    #_(= [::transitive-a ::transitive-d] 
         (transitive ::transitive-d))
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [::transitive-a ::transitive-d] 
         (transitive ::transitive-d))))
  
  (fmc/defmulti fmc-transitive identity)
  (fmc/defmethod fmc-transitive ::transitive-a [x] [::transitive-a x]) 
  (fmc/defmethod fmc-transitive ::transitive-c [x] [::transitive-c x]) 
  (fmc/prefer-method fmc-transitive ::transitive-a ::transitive-b)
  (fmc/prefer-method fmc-transitive ::transitive-b ::transitive-c)
  (test/is (= [::transitive-a ::transitive-d] 
              (fmc-transitive ::transitive-d))))

(test/deftest transitivity2
  
  (derive ::transitive-c0 ::transitive-a0)
  (derive ::transitive-c1 ::transitive-a1)
  (derive ::transitive-d0 ::transitive-c0)
  (derive ::transitive-d1 ::transitive-c1)
  (derive ::transitive-d0 ::transitive-b0)
  (derive ::transitive-d1 ::transitive-b1)
  
  (defmulti transitive2 (fn [x0 x1] [x0 x1]))
  (defmethod transitive2 
    [::transitive-b0 ::transitive-b1]
    [x0 x1] 
    [[::transitive-b0 ::transitive-b1] [x0 x1]]) 
  (defmethod transitive2 
    [::transitive-c0 ::transitive-c1]
    [x0 x1] 
    [[::transitive-c0 ::transitive-c1] [x0 x1]]) 
  (prefer-method transitive2 
                 [::transitive-a0 ::transitive-a1]
                 [::transitive-b0 ::transitive-b1])
  
  ;; this should not throw an exception
  (test/is 
    #_(= [[::transitive-c0 ::transitive-c1] 
          [::transitive-d0 ::transitive-d1]] 
         (transitive2 ::transitive-d0 ::transitive-d1))
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [[::transitive-c0 ::transitive-c1] 
          [::transitive-d0 ::transitive-d1]] 
         (transitive2 ::transitive-d0 ::transitive-d1))))
  ;;--------------------------------------------------------------
  (fmc/defmulti fmc-transitive2 (fn [x0 x1] [x0 x1]))
  (fmc/defmethod fmc-transitive2 
    [::transitive-b0 ::transitive-b1]
    [x0 x1] 
    [[::transitive-b0 ::transitive-b1] [x0 x1]]) 
  (fmc/defmethod fmc-transitive2 
    [::transitive-c0 ::transitive-c1]
    [x0 x1] 
    [[::transitive-c0 ::transitive-c1] [x0 x1]]) 
  (fmc/prefer-method fmc-transitive2 
                     [::transitive-a0 ::transitive-a1]
                     [::transitive-b0 ::transitive-b1])
  
  ;; this should not throw an exception
  (test/is 
    (= [[::transitive-c0 ::transitive-c1] 
        [::transitive-d0 ::transitive-d1]] 
       (fmc-transitive2 ::transitive-d0 ::transitive-d1))))

;;----------------------------------------------------------------
;; (prefers x ancestor-of-y) wrongly implies (prefers x y)
;;----------------------------------------------------------------
(test/deftest inheritance
  (derive ::pi-b ::pi-a)
  (derive ::pi-e ::pi-d)
  (derive ::pi-c ::pi-b)
  (derive ::pi-c ::pi-e)
  
  (defmulti cpi identity)
  (defmethod cpi ::pi-b [x] [::pi-b x]) 
  (defmethod cpi ::pi-e [x] [::pi-e x]) 
  (prefer-method cpi ::pi-b ::pi-d)
  ;; This should throw the exception
  (test/is 
    (= [::pi-b ::pi-c] (cpi ::pi-c))
    #_(thrown-with-msg? 
        IllegalArgumentException 
        #"Multiple methods in multimethod"
        (= [::pi-b ::pi-c] (fpi ::pi-c))))
  
  (fmc/defmulti fpi identity)
  (fmc/defmethod fpi ::pi-b [x] [::pi-b x]) 
  (fmc/defmethod fpi ::pi-e [x] [::pi-e x]) 
  (fmc/prefer-method fpi ::pi-b ::pi-d)
  ;; This should throw the exception
  (test/is 
    #_(= [::pi-b ::pi-c] (fpi ::pi-c))
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [::pi-b ::pi-c] (fpi ::pi-c)))))
;;----------------------------------------------------------------
;; local vs global hierarchy consistency
;;----------------------------------------------------------------
(test/deftest prefers-global
  (derive ::global-c ::global-b)
  (derive ::global-d ::global-c)
  (derive ::global-d ::global-a)
  
  (defmulti global identity)
  (defmethod global ::global-a [x] [::global-a x])
  (defmethod global ::global-c [x] [::global-c x]) 
  (prefer-method global ::global-b ::global-a)
  (test/is (= [::global-c ::global-d] (global ::global-d)))
  
  (fmc/defmulti fmc-global identity)
  (fmc/defmethod fmc-global ::global-a [x] [::global-a x]) 
  (fmc/defmethod fmc-global ::global-c [x] [::global-c x]) 
  (fmc/prefer-method fmc-global ::global-b ::global-a)
  (test/is (= [::global-c ::global-d] (fmc-global ::global-d))))
;;----------------------------------------------------------------
(test/deftest prefers-local
  (def hierarchy 
    (let [h (make-hierarchy)
          h (derive h ::local-c ::local-b)
          h (derive h ::local-d ::local-c)
          h (derive h ::local-d ::local-a)]
      h))
  
  (defmulti local identity :hierarchy #'hierarchy)
  (defmethod local ::local-a [x] [::local-a x]) 
  (defmethod local ::local-c [x] [::local-c x]) 
  (prefer-method local ::local-b ::local-a)
  ;; this should not throw the exception
  (test/is 
    #_(= [::local-c ::local-d] (local ::local-d))
    (thrown-with-msg? 
      IllegalArgumentException 
      #"Multiple methods in multimethod"
      (= [::local-c ::local-d] (local ::local-d))))
  
  (fmc/defmulti fmc-local identity :hierarchy #'hierarchy)
  (fmc/defmethod fmc-local ::local-a [x] [::local-a x]) 
  (fmc/defmethod fmc-local ::local-c [x] [::local-c x]) 
  (fmc/prefer-method fmc-local ::local-b ::local-a)
  (test/is (= [::local-c ::local-d] (fmc-local ::local-d))))
;;----------------------------------------------------------------
;;----------------------------------------------------------------
