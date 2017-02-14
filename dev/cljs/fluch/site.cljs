(ns fluch.site
  "For development purposes, serves as dev ground"
  (:require [fluch.core]
            [fluch.terminal :as terminal]))

(enable-console-print!)

(.log js/console "Hello from site!")

(def target-demo-1 (.querySelector js/document "#dev-app-1"))

(def term (terminal/create target-demo-1 {}))
