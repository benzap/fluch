(ns fluch.screen
  (:require [cljs.spec :as s]
            [fluch.block :as block]
            [fluch.buffer :as buffer]))

(def default-foreground-color "rgba(255, 255, 255, 1.0)")
(def default-background-color "rgba(0, 0, 0, 0.0)")

(def default-screen-opts
  {:font {:family "monospace" :size 12}
   :foreground-color default-foreground-color
   :background-color default-background-color
   :num-rows 36
   :num-cols 96
   :block-dimensions [20 20]})

(defn -populate-screen 
  [{:keys [num-rows num-cols] :as screen}]
  (let [content 
        (loop [i 0 v (transient [])]
          (if (< i (* num-rows num-cols))
            (recur (inc i) (conj! v (block/empty-block)))
            (persistent! v)))]
    (assoc screen :content (vec content))))

(defn -init [screen]
  (-> screen
      -populate-screen))

(defn position-index 
  [{:keys [num-rows num-cols] :as screen} i j]
  (+ i (* j num-cols)))

(defn create
  ([opts]
   (let [screen (merge default-screen-opts opts)]
     (-init screen)))
  ([] (create {})))

(defn put
  [{:keys [content] :as screen} i j block]
  (let [index (position-index screen i j)
        new-content (assoc content index block)]
    (assoc screen :content new-content)))

(defn get-block
  [{:keys [content] :as screen} i j]
  (let [index (position-index screen i j)]
    (get content index)))

(defn clear
  [{:keys [content] :as screen} i j]
  (put screen i j (block/empty-block)))

(defn sub [screen w h i j])
