(ns fluch.view
  (:require [cljs.spec :as s]))

(defn delta-action-add [i j]
  {:index [i j]
   :action :add})

(defn delta-action-remove [i j]
  {:index [i j]
   :action :remove})

(defn delta-action-modify [i j props]
  {:index [i j]
   :action :modify
   :props props})

(defn dom-element [screen block i j])

(defn find-block-delta [old-block new-block])

(defn process-delta-dimensions
  [old-screen new-screen]
  (let [nr-old (:num-rows old-screen)
        nr-new (:num-rows new-screen)
        delta-rows (- nr-old nr-new)
        
        nc-old (:num-cols old-screen)
        nc-new (:num-cols new-screen)
        delta-cols (- nc-old nc-new)]
    (cond 
      (> delta-rows 0)
      nil
      
      )))

(defn find-screen-delta [old-screen new-screen]
  (concat (process-delta-dimensions old-screen new-screen)
          ))

(defn screen-watcher-fn [view key ref old-screen new-screen]
  (let [delta-blocks (find-screen-delta old-screen new-screen)]
    (println "screen changed")
    (.log js/console (clj->js old-screen))
    (.log js/console (clj->js new-screen))
    ))

(defn -add-screen-watcher [view screen]
  (let [watch-fn (partial screen-watcher-fn view)]
    (add-watch screen nil watch-fn)
    ))

(defn -init-view [dom-root screen]
  (let [view (atom {:dom-elements {}})]
    (-add-screen-watcher view screen)))

(defn create-dom-view
  [dom-root screen]
  (let [view (-init-view dom-root screen)]
    view))
