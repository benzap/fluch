(ns fluch.site
  "For development purposes, serves as dev ground"
  (:require-macros [devcards.core :refer [defcard defcard-rg]]
                   [fluch.utils :refer [console-time]])
  (:require [devcards.core :as dc]
            [sablono.core :as sab]

            ;; Local
            [fluch.core]
            [fluch.terminal :as terminal]
            [fluch.view :as view]
            
            ;;DevTests
            [fluch.devtests.slider]))

(enable-console-print!)

;; Demo Page
(when-let [dev-app-1 (.querySelector js/document "#dev-app-1")]
  (let [v (view/create "app" dev-app-1)]
    ))

