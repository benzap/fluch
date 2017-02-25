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

(def block-width 12)
(def block-height 16)

(def num-rows 24)
(def num-cols 60)

(def terminal-width (* block-width num-cols))
(def terminal-height (* block-height num-rows))

(doto target-demo-1
  (aset "style" "width" (str terminal-width "px"))
  (aset "style" "height" (str terminal-height "px")))

(def term (terminal/create target-demo-1
                           {:num-rows num-rows
                            :num-cols num-cols
                            :block-dimensions [block-width block-height]}))

(terminal/put-char! term 0 0 "H")
(terminal/put-char! term 1 0 "e")
(terminal/put-char! term 2 0 "l")
(terminal/put-char! term 3 0 "l")
(terminal/put-char! term 4 0 "o")

(terminal/put-char! term 1 1 "H")
(terminal/put-char! term 1 2 "e")
(terminal/put-char! term 1 3 "l")
(terminal/put-char! term 1 4 "l")
(terminal/put-char! term 1 5 "o")
(terminal/put-char! term 2 1 "e")
(terminal/put-char! term 3 1 "l")
(terminal/put-char! term 4 1 "l")
(terminal/put-char! term 5 1 "o")

(println (terminal/get-block term 1 1))


