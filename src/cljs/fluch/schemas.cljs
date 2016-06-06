(ns fluch.schemas
  (:require [cljs.spec :as s]
            [fluch.utils :as utils :refer [boolean?]]))

(enable-console-print!)

(defn in-unit-interval? [x]
  (and (>= x 0.0) (<= 1.0)))
(s/def ::unit-interval (s/and integer? in-unit-interval?))

(defn in-byte-range? [x]
  (and (<= x 255) (>= x 0) (integer? x)))

(defn >zero? [x] (> x 0))
(defn >=zero? [x] (>= x 0))

(s/def ::unsigned-int (s/and integer? >=zero?))
(s/def ::letter string?)
(s/def ::byterange (s/and integer? >=zero? in-byte-range?))

;; FIXME: check against actual core.async channels
(s/def ::async-channel #(instance? cljs.core.async.impl.channels.ManyToManyChannel %))
(s/def ::async-mult #(not= % nil))

