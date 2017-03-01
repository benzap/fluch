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
            [fluch.slider :as slider]
            
            ;;DevTests
            [fluch.devtests.screen]
            [fluch.devtests.slider]))

(enable-console-print!)





