(ns fluch.dev.core
  (:require
   [devtools.core :as devtools]
   [schema.core :as s]
   [fluch.core :as fluch]
   [fluch.terminal :as t]
   [fluch.canvas :as c]))

(devtools/install!)
(enable-console-print!)

(s/set-fn-validation! true)

(println "Hello World!")

(def canvas (c/init (.getElementById js/document "term")))
(def term (atom (t/terminal canvas {})))

;;(println "term" @term)
(.log js/console "term" @term)
