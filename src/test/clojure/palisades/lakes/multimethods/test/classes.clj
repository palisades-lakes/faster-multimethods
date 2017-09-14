(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns palisades.lakes.multimethods.test.classes
  
  {:doc "dummy classes for prefer-method tests."
   :author "palisades dot lakes at gmail dot com"
   :since "2017-09-14"
   :version "2017-09-14"})
;;----------------------------------------------------------------
(definterface A)
(gen-class 
  :name palisades.lakes.multimethods.test.classes.B)
(gen-class 
  :name palisades.lakes.multimethods.test.classes.C)
(gen-class 
  :name palisades.lakes.multimethods.test.classes.D
  :implements [palisades.lakes.multimethods.test.classes.A]
  :extends palisades.lakes.multimethods.test.classes.C)
;;----------------------------------------------------------------
(definterface A0)
(definterface B0)
(gen-class 
  :name palisades.lakes.multimethods.test.classes.C0
  :implements [palisades.lakes.multimethods.test.classes.A0])
(gen-class 
  :name palisades.lakes.multimethods.test.classes.D0
  :implements [palisades.lakes.multimethods.test.classes.B0]
  :extends palisades.lakes.multimethods.test.classes.C0)
;;----------------------------------------------------------------
(definterface A1)
(definterface B1)
(gen-class 
  :name palisades.lakes.multimethods.test.classes.C1
  :implements [palisades.lakes.multimethods.test.classes.A1])
(gen-class 
  :name palisades.lakes.multimethods.test.classes.D1
  :implements [palisades.lakes.multimethods.test.classes.B1]
  :extends palisades.lakes.multimethods.test.classes.C1)
;;----------------------------------------------------------------
(gen-interface 
  :name palisades.lakes.multimethods.test.classes.A2)
(gen-interface
  :name palisades.lakes.multimethods.test.classes.B2
  :extends [palisades.lakes.multimethods.test.classes.A2])
(gen-interface 
  :name palisades.lakes.multimethods.test.classes.D2)
(gen-interface
  :name palisades.lakes.multimethods.test.classes.E2
  :extends [palisades.lakes.multimethods.test.classes.D2])
(gen-class 
  :name palisades.lakes.multimethods.test.classes.C2
  :implements [palisades.lakes.multimethods.test.classes.B2
               palisades.lakes.multimethods.test.classes.E2])
;;----------------------------------------------------------------
