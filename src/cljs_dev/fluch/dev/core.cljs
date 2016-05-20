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

(def canvas (c/init (.getElementById js/document "term")))
(def term (atom (t/terminal canvas {:rows 2 :cols 2 :background-color [128 128 128 255]})))
(t/refresh @term)

;;(println "term" @term)
(.log js/console "term" @term)
