(ns fluch.screen
  (:require [cljs.spec :as s]
            [fluch.block :as block]))

(def default-foreground-color "rgba(255, 255, 255, 1.0)")
(def default-background-color "rgba(0, 0, 0, 0.0)")

(def default-screen-opts
  {:font {:family "monospace" :size 12}
   :foreground-color default-foreground-color
   :background-color default-background-color
   :cell-dimensions [20 20]
   :num-rows 36
   :num-cols 96})

(defn create 
  ([opts] (merge default-screen-opts opts))
  ([] (create {})))
