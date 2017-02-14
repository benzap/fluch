(ns fluch.block
  (:require [cljs.spec :as s]))

(def default-block-opts 
  {:type :none
   :font :default
   :foreground-color :default
   :background-color :default
   :style {:underline false
           :bold false
           :strikethrough false
           :italic false}
   :content :none})

(defn create-block [type opts]
  (merge default-block-opts opts {:type type}))

(defn empty-block
  ([opts]
   (create-block :empty opts))
  ([] (empty-block {})))

(defn letter-block
  ([char opts] (create-block :text (merge opts {:content char}))
   [char] (letter-block char {})))
