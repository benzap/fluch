(ns fluch.tests
  (:require-macros [devcards.core :refer [defcard]])
  (:require [devcards.core :as dc]
            
            [fluch.screen :as screen]))

(defcard testing
  {:test 123})

(def testing "123")


(println "In dev tests")

