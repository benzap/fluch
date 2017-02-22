(ns fluch.view
  (:require [cljs.spec :as s]
            [clojure.data :refer [diff]]

            [fluch.screen :as screen]))

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

(defn dom-element
  [{:keys [block-dimensions foreground-color background-color font] :as screen}
   {:keys [style type content] :as block} i j]
  (let [element (.createElement js/document "span")
        foreground-color (if (= (:foreground-color block) :default)
                           foreground-color (:foreground-color block))

        background-color (if (= (:background-color block) :default)
                           background-color (:background-color block))

        {:keys [family size] :or {family "monospace" size 12}} 
        (if (= (:font block) :default) font (:font block))

        {:keys [underline bold strikethrough italic]} style
        
        [w h] block-dimensions

        x-pos (* i w)
        y-pos (* j h)]
    (doto element
      (aset "style" "position" "absolute")
      (aset "style" "left" (str x-pos "px"))
      (aset "style" "top" (str y-pos "px"))
      (aset "style" "color" foreground-color)
      (aset "style" "backgroundColor" background-color)
      (aset "style" "fontFamily" family)
      (aset "style" "fontSize" (str size "px"))
      (aset "style" "lineHeight" (str h "px"))
      (aset "style" "textAlign" "center")
      (aset "style" "textDecoration" 
            (cond strikethrough "line-through"
                  underline "underline"
                  :else "none"))
      (aset "style" "fontStyle" (if italic "italic" "normal"))
      (aset "style" "fontWeight" (if bold "bold" "normal"))
      (aset "innerHTML" (cond (= type :text) content
                              :else "")))))

(defn find-block-delta [old-block new-block]
  (let [[_ props _] (diff old-block new-block)]
    props))

(defn process-delta-dimensions
  [old-screen new-screen]
  (let [nr-old (:num-rows old-screen)
        nr-new (:num-rows new-screen)
        delta-rows (- nr-new nr-old)
        
        nc-old (:num-cols old-screen)
        nc-new (:num-cols new-screen)
        delta-cols (- nc-new nc-old)]
    (cond
      ;; No Change
      (and (= delta-rows 0) (= delta-cols 0))
      []

      ;; Only Elongated Rows
      (and (> delta-rows 0) (= delta-cols 0))
      (vec (for [i (range 0 nc-old)
                 j (range nr-old nr-new)]
             (delta-action-add i j)))

      ;; Only Elongated Cols
      (and (= delta-rows 0) (> delta-cols 0))
      (vec (for [i (range nc-old nc-new)
                 j (range 0 nr-old)]
             (delta-action-add i j)))
      
      ;; Only Collapsed Rows
      (and (< delta-rows 0) (= delta-cols 0))
      (vec (for [i (range 0 nc-old)
                 j (range nr-new nr-old)]
             (delta-action-remove i j)))

      ;; Only Collapsed Cols
      (and (= delta-rows 0) (< delta-cols 0))
      (vec (for [i (range nc-new nc-old)
                 j (range 0 nr-old)]
             (delta-action-remove i j)))

      ;; Elongated Rows + Elongated Cols
      (and (> delta-rows 0) (> delta-cols 0))
      (concat
       (vec (for [i (range nc-old nc-new)
                  j (range 0 nr-old)]
              (delta-action-add i j)))
       (vec (for [i (range 0 nc-new)
                  j (range nr-old nr-new)]
              (delta-action-add i j))))

      ;; Elongated Rows + Collapsed Cols
      (and (> delta-rows 0) (< delta-cols 0))
      (concat
       (vec (for [i (range 0 nc-new)
                  j (range nr-old nr-new)]
              (delta-action-add i j)))
       (vec (for [i (range nc-new nc-old)
                  j (range 0 nr-old)]
              (delta-action-remove i j))))

      ;; Collapsed Rows + Elongated Cols
      (and (< delta-rows 0) (> delta-cols 0))
      (concat
       (vec (for [i (range nc-old nc-new)
                  j (range 0 nr-new)]
              (delta-action-add i j)))
       (vec (for [i (range 0 nc-old)
                  j (range nr-new nr-old)]
              (delta-action-remove i j))))

      ;; Collapsed Rows + Collapsed Cols
      (and (< delta-rows 0) (< delta-cols 0))
      (concat
       (vec (for [i (range nc-new nc-old)
                  j (range 0 nr-new)]
              (delta-action-remove i j)))
       (vec (for [i (range 0 nc-old)
                  j (range nr-new nr-old)]
              (delta-action-remove i j)))))))

(defn process-delta-blocks [old-screen new-screen]
  (let [num-rows (min (:num-rows old-screen)
                      (:num-rows new-screen))
        num-cols (min (:num-cols old-screen)
                      (:num-cols new-screen))
        deltas
        (for [i (range 0 num-cols)
              j (range 0 num-rows)]
          (when-let [props 
                     (find-block-delta (screen/get-block old-screen i j)
                                       (screen/get-block new-screen i j))]
            (delta-action-modify i j props)))]
    ;; Remove nils
    (filter (complement nil?) deltas)))

(defn find-screen-delta [old-screen new-screen]
  (concat (process-delta-dimensions old-screen new-screen)
          (process-delta-blocks old-screen new-screen)))

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
