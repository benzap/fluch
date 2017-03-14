(ns fluch.site
  "For development purposes, serves as dev ground"
  (:require-macros [devcards.core :refer [defcard defcard-rg]]
                   [fluch.utils :refer [console-time]])
  (:require [devcards.core :as dc]
            [sablono.core :as sab]

            ;; Local
            [fluch.core]
            [fluch.terminal :as terminal]
            [fluch.screen :as screen]
            [fluch.block :as block]
            [fluch.slider :as slider]
            [fluch.view :as view]
            
            ;;DevTests
            [fluch.devtests.screen]
            [fluch.devtests.slider]
            [fluch.devtests.buffer]))

(enable-console-print!)

;; Demo Page
(when-let [dev-app-1 (.querySelector js/document "#dev-app-1")]
  (let [v (view/create "app" dev-app-1)
        b (block/create "B" 0 0)]

    (view/clear! v)
    (view/draw! v b)
    (view/draw! v (block/create "r" 1 0))
    (view/draw! v (block/create "e" 2 0))
    (view/draw! v (block/create "a" 3 0))
    (view/draw! v (block/create "k" 4 0))
    (view/draw! v (block/create "r" 0 1))
    (view/draw! v (block/create "e" 0 2))
    (view/draw! v (block/create "a" 0 3))
    (view/draw! v (block/create "k" 0 4))
    ))

