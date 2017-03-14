(ns fluch.devtests.buffer
  (:require-macros [devcards.core :refer [defcard]])
  (:require [cljs.spec :as s]
            [cljs.spec.test :as st :refer [instrument]]
            [cljs.test :as t :refer [] :include-macros true]
            [devcards.core :as dc :include-macros true]
            [fluch.devtests.utils :as utils :include-macros true :refer-macros [is-conformed]]
            [fluch.slider :as slider]
            [fluch.buffer :as buffer]))

(dc/deftest buffer-tests
  
  (t/testing "buffer/create"
    (is-conformed ::buffer/buffer (buffer/create "*scratch*"))))
