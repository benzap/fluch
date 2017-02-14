(ns fluch.block
  (:require [cljs.spec :as s]))

(defrecord Block [type x y opts])

(defn empty-block [])
