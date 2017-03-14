(ns fluch.screen
  (:require [cljs.spec :as s]
            [fluch.block :as block]
            [fluch.buffer :as buffer]
            [fluch.slider :as slider]
            [fluch.matrix :as matrix]))

(defn empty-content [rows cols]
  (matrix/create rows cols))

(defn create 
  [buffer
   {:keys [rows cols block-width block-height]
    :or {top 0 left 0 rows 24 cols 48 block-width 16 block-height 24}}]
  {::rows rows
   ::cols cols
   ::buffer buffer
   ::content (empty-content rows cols)})

(defn insert [screen block i j]
  (assoc screen
         ::content (matrix/mset (::content screen) i j block)))

(defn refresh [screen]
  (let []))
