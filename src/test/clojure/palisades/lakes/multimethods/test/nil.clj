(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.nil
  
  {:doc "Test nil dispatch value and in signatures."
   :author "palisades dot lakes at gmail dot com"
   :version "2017-12-13"}
  (:require [clojure.test :as test]
            [palisades.lakes.multimethods.core :as d])
  (:import [java.util Collection]
           [clojure.lang IFn]))
;; mvn clojure:test -Dtest=palisades.lakes.multimethods.test.nil
;;----------------------------------------------------------------
(test/deftest class0
  (d/defmulti count0 class :hierarchy false)
  (d/defmethod count0 nil [_] 0) 
  (d/defmethod count0 Collection [^Collection x] (.size x))
  (test/is (== 0 (count0 nil)))
  (test/is (== 0 (count0 [])))
  (test/is (== 2 (count0 [:a :b]))))

(test/deftest class1
  (d/defmulti count1 class)
  (d/defmethod count1 nil [_] 0) 
  (d/defmethod count1 Collection [^Collection x] (.size x))
  (test/is (== 0 (count1 nil)))
  (test/is (== 0 (count1 [])))
  (test/is (== 2 (count1 [:a :b]))))
;;----------------------------------------------------------------
(defn- square 
  (^long [^long x] (* x x))
  ([^long x ^long y] 
    [(* x x) (* y y)])
  ([^long x ^long y ^long z] 
    [(* x x) (* y y) (* z z)]))
;;----------------------------------------------------------------
(test/deftest signature0
  (d/defmulti map0 d/signature :hierarchy false)
  (d/defmethod map0 
    (d/to-signature nil nil) [_ _] 
    nil) 
  (d/defmethod map0 
    (d/to-signature IFn nil) [_ _] 
    nil) 
  (d/defmethod map0 
    (d/to-signature IFn Collection) 
    [^IFn f ^Collection things0] (mapv f things0))
  (d/defmethod map0 
    (d/to-signature IFn Collection Collection) 
    [^IFn f ^Collection things0 ^Collection things1] 
    (mapv f things0 things1))
  (d/defmethod map0 
    (d/to-signature IFn nil Collection) 
    [_ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn Collection nil) 
    [_ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature nil nil Collection) 
    [_ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn nil nil) 
    [_ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature nil nil nil) 
    [_ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn Collection Collection Collection) 
    [^IFn f 
     ^Collection things0 
     ^Collection things1 
     ^Collection things2] 
    (mapv f things0 things1 things2))
  (d/defmethod map0 
    (d/to-signature IFn nil Collection Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature nil Collection Collection Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn Collection nil Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn nil nil Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature IFn nil nil nil) 
    [_ _ _ _] 
    nil)
  (d/defmethod map0 
    (d/to-signature nil nil nil nil) 
    [_ _ _ _] 
    nil)
  (test/is (nil? (map0 square nil)))
  (test/is (nil? (map0 nil nil)))
  (test/is (nil? (map0 square nil [2 4])))
  (test/is (nil? (map0 nil nil [3 5])))
  (test/is (nil? (map0 nil nil nil)))
  (test/is (nil? (map0 square nil nil)))
  (test/is (nil? (map0 square nil [2 4])))
  (test/is (nil? (map0 nil [1 2] [2 4] [3 5])))
  (test/is (nil? (map0 nil nil nil nil)))
  (test/is (nil? (map0 square nil nil [3 5])))
  (test/is (= [[1 4 9][4 16 25]]
              (map0 square [1 2] [2 4] [3 5])))
  )

;;----------------------------------------------------------------
(test/deftest signature1
  (d/defmulti map1 d/signature)
  (d/defmethod map1 
    (d/to-signature nil nil) [_ _] 
    nil) 
  (d/defmethod map1 
    (d/to-signature IFn nil) [_ _] 
    nil) 
  (d/defmethod map1 
    (d/to-signature IFn Collection) 
    [^IFn f ^Collection things0] (mapv f things0))
  (d/defmethod map1 
    (d/to-signature IFn Collection Collection) 
    [^IFn f ^Collection things0 ^Collection things1] 
    (mapv f things0 things1))
  (d/defmethod map1 
    (d/to-signature IFn nil Collection) 
    [_ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn Collection nil) 
    [_ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature nil nil Collection) 
    [_ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn nil nil) 
    [_ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature nil nil nil) 
    [_ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn Collection Collection Collection) 
    [^IFn f 
     ^Collection things0 
     ^Collection things1 
     ^Collection things2] 
    (mapv f things0 things1 things2))
  (d/defmethod map1 
    (d/to-signature IFn nil Collection Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature nil Collection Collection Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn Collection nil Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn nil nil Collection) 
    [_ _ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature IFn nil nil nil) 
    [_ _ _ _] 
    nil)
  (d/defmethod map1 
    (d/to-signature nil nil nil nil) 
    [_ _ _ _] 
    nil)
  (test/is (nil? (map1 square nil)))
  (test/is (nil? (map1 nil nil)))
  (test/is (nil? (map1 square nil [2 4])))
  (test/is (nil? (map1 nil nil [3 5])))
  (test/is (nil? (map1 nil nil nil)))
  (test/is (nil? (map1 square nil nil)))
  (test/is (nil? (map1 square nil [2 4])))
  (test/is (nil? (map1 nil [1 2] [2 4] [3 5])))
  (test/is (nil? (map1 nil nil nil nil)))
  (test/is (nil? (map1 square nil nil [3 5])))
  (test/is (= [[1 4 9][4 16 25]]
              (map1 square [1 2] [2 4] [3 5])))
  )
;;----------------------------------------------------------------
