(ns fluch.devtests.screen
  (:require-macros [devcards.core :refer [defcard]])
  (:require [devcards.core :as dc :include-macros true]
            
            [fluch.screen :as screen]))

(defcard screen-initialization
  "#Testing the initialization"
  (let [scr (screen/create {:num-rows 2 :num-cols 2})]
    scr))
