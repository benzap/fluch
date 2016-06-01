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
(def default-cell-size 12)
(def default-foreground-color [255 255 255 255])
(def default-background-color [0 0 0 255])


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

(defn terminal-content
  [rows cols foreground-color background-color]
  (let [terminal-block (text-block "0"
                        {:foreground-color foreground-color
                         :background-color background-color})
        terminal-row (vec (repeat cols terminal-block))]
    (vec (repeat rows terminal-row))))

(s/defn terminal :- schemas/Terminal
  [context
   {:keys [rows :- s/Num
           cols :- s/Num
           size :- s/Num
           font :- schemas/TerminalFont
           offset :- schemas/TerminalOffset
           foreground-color :- schemas/Color
           background-color :- schemas/Color]
    :or {rows default-num-rows
         cols default-num-cols
         size default-cell-size
         font fluch.font/monospace
         offset [0 0]
         foreground-color default-foreground-color
         background-color default-background-color}}]
  {:context context
   :options {:foreground-color foreground-color
             :background-color background-color
             :offset offset
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
  (let [{:keys [font offset]} options
        {:keys [ratio]} font
        [col-offset row-offset] offset
        [x-ratio y-ratio] ratio
        width (* (/ size 2) x-ratio)
        height (* size y-ratio)]
    {:x (-> width (* (+ col-index col-offset)))
     :y (-> height (* (+ row-index row-offset)))
     :width width
     :height height}))

(s/defn get-block :- schemas/TerminalBlock
  "get the block at the given column-row point"
  [{:keys [content]} :- schemas/Terminal
   col-index :- s/Num
   row-index :- s/Num]
  (-> content (get row-index) (get col-index)))

(s/defn put-block :- schemas/Terminal
  [{:keys [content] :as term} :- schemas/Terminal
   block :- schemas/TerminalBlock
   col-index :- s/Num
   row-index :- s/Num]
  (let [new-content
        (specter/transform [(specter/keypath row-index)
                            (specter/keypath col-index)]
                           (fn [_] block) content)]
    (assoc term :content new-content)))

(s/defn clear-block :- schemas/Terminal
  [{:keys [options] :as term} :- schemas/Terminal
   col-index :- s/Num
   row-index :- s/Num]
  (let [{:keys [foreground-color background-color]} options
        empty (empty-block {:foreground-color foreground-color
                            :background-color background-color})]
    (put-block term empty col-index row-index)))

(defmulti draw-block!
  (fn [term block row-index col-index]
    (:type block)))

(s/defmethod draw-block! "Empty"
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
    (canvas/fill-rect context x y width height :color background-color)))

(s/defmethod draw-block! "Text"
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

(defn refresh!
  [{:keys [rows cols] :as term}]
  (loop [i 0 j 0]
    (draw-block! term (get-block term i j) i j)
    (cond
      ;; processed all blocks
      (and (>= (inc j) rows) (>= (inc i) cols))
      term
      ;; at the end of the row
      (>= (inc i) cols)
      (recur 0 (inc j))
      ;; process the next column in the row
      :else
      (recur (inc i) j))))

(s/defn sub-term :- schemas/Terminal
  "Get a sub terminal, representing a section of the current
  terminal.

  A sub-terminal has the same characteristics as a normal
  Terminal. col-offset and row-offset are the offset from the top-left
  corner of the terminal. cols and rows are the size extents of the
  sub terminal, which should be smaller than or equal to the area you
  are attempting to make a sub-terminal from"
  [term :- schemas/Terminal
   col-offset :- s/Num
   row-offset :- s/Num
   cols :- s/Num
   rows :- s/Num])

(s/defn put-term :- schemas/Terminal
  "Fill a section of a terminal with a sub-terminal"
  [term :- schemas/Terminal
   sub-term :- schemas/Terminal
   col-offset :- s/Num
   row-offset :- s/Num])

(defn swap-blocks [term col1 row1 col2 row2]
  (let [first-block (get-block term col1 row1)
        second-block (get-block term col2 row2)]
    (-> term
        (put-block first-block col2 row2)
        (put-block second-block col1 row1))))

(defn swap-block-left
  [term col row]
  (if (<= col 0)
    (clear-block term col row)
    (swap-blocks term col row (dec col) row)))

(defn swap-block-right
  [{:keys [cols] :as term} col row]
  (if (>= col cols)
    (clear-block term col row)
    (swap-blocks term col row (inc col) row)))

(defn swap-block-up
  [term col row]
  (if (<= row 0)
    (clear-block term col row)
    (swap-blocks term col row col (dec row))))


(defn swap-block-down
  [{:keys [rows] :as term} col row]
  (if (>= row rows)
    (clear-block term col row)
    (swap-blocks term col row col (inc row))))

(defn swap-col-left
  [{:keys [rows] :as term} col]
  (if (<= col 0)
    (reduce #(clear-block %1 %2 col) term (range rows))
    (reduce #(swap-blocks %1 col %2 (dec col) %2) term (range rows))))

(defn swap-col-right
  [{:keys [rows cols] :as term} col]
  (if (>= col cols)
    (reduce #(clear-block %1 %2 col) term (range rows))
    (reduce #(swap-blocks %1 col %2 (inc col) %2) term (range rows))))

(defn swap-row-up
  [{:keys [rows cols] :as term} row]
  (if (<= row 0)
    (reduce #(clear-block %1 row %2) term (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (dec row)) term (range cols))))

(defn swap-row-down
  [{:keys [rows cols] :as term} row]
  (if (>= row rows)
    (reduce #(clear-block %1 row %2) term (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (inc row)) term (range cols))))

(defn resize
  "Resize a Terminal to fill the new resized extents"
  [term & {:keys [rows cols size]}])
