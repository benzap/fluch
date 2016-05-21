(ns fluch.terminal
  (:require [schema.core :as s :include-macros true]
            [com.rpl.specter :as specter]
            [fluch.schemas :as schemas]
            [fluch.color :as color]
            [fluch.canvas :as canvas]
            [fluch.font]
            ))

(def default-num-rows 24)
(def default-num-cols 80)
(def default-cell-size 12) ;; px
(def default-foreground-color [255 255 255 255])
(def default-background-color [0 0 0 255])
(def default-row-spacing 1) ;; px
(def default-col-spacing 1) ;; px


(s/defn text-block :- schemas/TextBlock
  [text
   {:keys [foreground-color
           background-color
           bold
           underline
           italic]
    :or {foreground-color default-foreground-color
         background-color default-background-color
         bold false
         underline false
         italic false}}]
  {:type "Text"
   :foreground-color foreground-color
   :background-color background-color
   :style {:bold bold
           :underline underline
           :italic italic}
   :text text})

(s/defn empty-block :- schemas/EmptyBlock
  [{:keys [foreground-color
           background-color]
    :or {foreground-color default-foreground-color
         background-color default-background-color}}]
  {:type "Empty"
   :foreground-color foreground-color
   :background-color background-color})

(s/defn terminal-content :- schemas/TerminalContent
  [rows cols foreground-color background-color]
  (let [terminal-block (text-block "0"
                        {:foreground-color foreground-color
                         :background-color background-color})
        terminal-row (vec (repeat cols terminal-block))]
    (vec (repeat rows terminal-row))
    ))

(s/defn terminal :- schemas/Terminal
  [context
   {:keys [rows :- s/Num
           cols :- s/Num
           size :- s/Num
           font :- schemas/TerminalFont
           foreground-color :- schemas/Color
           background-color :- schemas/Color]
    :or {rows default-num-rows
         cols default-num-cols
         size default-cell-size
         font fluch.font/monospace
         foreground-color default-foreground-color
         background-color default-background-color}}]
  {:context context
   :options {:foreground-color foreground-color
             :background-color background-color
             :font font}
   :rows rows
   :cols cols
   :size size
   :content (terminal-content
             rows cols
             foreground-color background-color)
   })

(s/defn locate-block
  "returns the pixel location and dimensions of the block in pixels"
  [{:keys [size options]} :- schemas/Terminal
   col-index :- s/Num
   row-index :- s/Num]
  (let [{:keys [font]} options
        {:keys [ratio]} font
        [x-ratio y-ratio] ratio
        width (* (/ size 2) x-ratio)
        height (* size y-ratio)]
    {:x (-> width (* col-index))
     :y (-> height (* row-index))
     :width width
     :height height}))

(s/defn get-block :- schemas/TerminalBlock
  "get the block at the given column-row point"
  [{:keys [content]} :- schemas/Terminal
   row-index :- s/Num
   col-index :- s/Num]
  (-> content (get row-index) (get col-index)))

(defmulti draw-block
  (fn [term block row-index col-index]
    (:type block)))

(s/defmethod draw-block "Empty"
  [term :- schemas/Terminal
   block :- schemas/EmptyBlock
   col-index :- s/Num
   row-index :- s/Num]
  (let [{:keys [context options]}
        term
        {:keys [background-color]}
        options
        {:keys [x y width height]}
        (locate-block term col-index row-index)]
    (canvas/fill-rect context x y width height :color background-color)
    ))

(s/defmethod draw-block "Text"
  [term :- schemas/Terminal
   block :- schemas/TextBlock
   col-index :- s/Num
   row-index :- s/Num]
  (let [{:keys [context options size]} term
        {:keys [foreground-color background-color text font]} (merge options block)
        {:keys [x y width height]} (locate-block term col-index row-index)]
    (canvas/fill-rect context x y width height :color background-color)
    (canvas/draw-text context text x y
                      :size size
                      :family (:family font)
                      :foreground-color foreground-color)))

(defn refresh
  [{:keys [rows cols] :as term}]
  (loop [i 0 j 0]
    (draw-block term (get-block term i j) i j)
    (cond
      ;; processed all blocks
      (and (>= (inc j) rows) (>= (inc i) cols))
      term
      ;; at the end of the row
      (>= (inc i) cols)
      (recur 0 (inc j))
      ;; process the next column in the row
      :else
      (recur (inc i) j)
      )))
