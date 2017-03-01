(ns fluch.devtests.utils
  (:require-macros [cljs.test :refer [is]])
  (:require [cljs.spec :as s]
            [cljs.test :as t :include-macros true]))

(defn check-conformed [spec test]
  (is (not= 
       (s/conform spec test)
       ::s/invalid)))
