(ns fluch.dev.core
  (:require 
   [schema.core :as s]
   [fluch.core :as fluch]
   [fluch.terminal :as t]
   [fluch.canvas :as c]))

(enable-console-print!)

(s/set-fn-validation! true)

(println "Hello World!")

(def canvas (c/init (.getElementById js/document "term")))
(def term (atom (t/terminal canvas {})))

(println "term" @term)
