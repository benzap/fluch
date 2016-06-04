(ns fluch.utils
  (:require [cljs.spec :as s]))

(defn boolean? [x]
  (or (= x true) (= x false)))
