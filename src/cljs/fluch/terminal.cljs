(ns fluch.terminal
  (:require [schema.core :as s :include-macros true]
            [com.rpl.specter :as specter]
            [fluch.schemas :as schemas]
            [fluch.color :as color]
            [fluch.canvas :as canvas]
            ))

(def default-num-rows 24)
(def default-num-cols 80)
(def default-cell-size 12) ;; px
(def default-foreground-color [255 255 255 255])
(def default-background-color [0 0 0 255])
(def default-row-spacing 3) ;; px
(def default-col-spacing 5) ;; px


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
  (let [terminal-block (empty-block 
                        {:foreground-color foreground-color
                         :background-color background-color})
        terminal-row (vec (repeat cols terminal-block))]
    (vec (repeat rows terminal-row))
    ))

(s/defn terminal :- schemas/Terminal
  [context
   {:keys [rows
           cols
           size
           row-spacing
           col-spacing
           foreground-color
           background-color]
    :or {rows default-num-rows
         cols default-num-cols
         size default-cell-size
         row-spacing default-row-spacing
         col-spacing default-col-spacing
         foreground-color default-foreground-color
         background-color default-background-color}}]
  {:context context
   :options {:foreground-color foreground-color
             :background-color background-color
             :row-spacing row-spacing
             :col-spacing col-spacing}
   :rows rows
   :cols cols
   :size size
   :content (terminal-content
             rows cols
             foreground-color background-color)
   })

(s/defn calc-dimensions 
  [{:keys [size options]} :- schemas/Terminal
   row-index :- s/Num
   col-index :- s/Num]
  (let [{:keys [row-spacing col-spacing]} options
        width (+ size (* 2 col-spacing))
        height (+ size (* 2 row-spacing))]
    {:x (-> width (* col-index))
     :y (-> height (* row-index))
     :width width
     :height height}))

(defmulti draw-block
  (fn [term block row-index col-index]
    (:type block)))

(s/defmethod draw-block "Empty"
  [term :- schemas/Terminal
   block :- schemas/EmptyBlock
   row-index :- s/Num
   col-index :- s/Num]
  (let [{:keys [context options]}
        term
        {:keys [background-color]}
        options
        {:keys [x y width height]}
        (calc-dimensions term row-index col-index)
        ]
    (canvas/fill-rect context x y width height {:color background-color})
    (.log js/console "Drawing:" x y width height)
    ))

(s/defmethod draw-block "Text"
  [term :- schemas/Terminal
   block :- schemas/TextBlock
   row-index :- s/Num
   col-index :- s/Num]
  (let [{:keys [context options]}
        term
        {:keys [background-color]}
        options
        {:keys [x y width height]}
        (calc-dimensions term row-index col-index)
        ]
    (canvas/fill-rect context x y width height {:color background-color})
    ))

(s/defn refresh
  [{:keys [content] :as term} :- schemas/Terminal]
  (let [num-rows (count content)
        num-cols (count (first content))]
    (loop [i 0 j 0]
      (draw-block term (-> content (get j) (get i)) j i)
      (cond
        ;; processed all blocks
        (and (>= (inc j) num-rows) (>= (inc i) num-cols))
        term
        ;; at the end of the row
        (>= (inc i) num-cols)
        (recur 0 (inc j))
        ;; process the next column in the row
        :else
        (recur (inc i) j)
        ))))
