(ns fluch.site
  "For development purposes, serves as dev ground"
  (:require-macros [devcards.core :refer [defcard]])
  (:require [devcards.core :as dc]
            [sablono.core :as sab]

            ;; Local
            [fluch.core]
            [fluch.terminal :as terminal]
            [fluch.screen :as screen]
            [fluch.block :as block]
            
            ;;DevTests
            [fluch.devtests.screen]))

(enable-console-print!)

(defcard sometests {:test 123})

(.log js/console "Hello from site!")

(def target-demo-1 (.querySelector js/document "#dev-app-1"))

(def term (terminal/create target-demo-1 {}))

(terminal/put-char! term 1 1 "a")

(println (terminal/get-block term 1 1))

