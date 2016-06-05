(ns fluch.terminal
  (:require [cljs.spec :as s]
            [com.rpl.specter :as specter]
            
            [fluch.schemas :as schemas]
            [fluch.color :as color]
            [fluch.canvas :as canvas]
            [fluch.font :as font]))

(def default-num-rows 24)
(def default-num-cols 80)
(def default-cell-size 12)
(def default-foreground-color [255 255 255 255])
(def default-background-color [0 0 0 255])


(defn text-block
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

(s/def ::type string?)
(s/def ::text string?)
(s/def ::text-block 
  (s/keys :req-un [::type 
                   ::color/background-color
                   ::color/foreground-color
                   ::font/style
                   ::text]))

(s/fdef text-block
        :args (s/cat :text ::text
                     :options (s/keys :opt-un [::color/foreground-color
                                               ::color/background-color
                                               ::font/bold
                                               ::font/underline
                                               ::font/italic]))
        :ret ::text-block)

(defn empty-block
  [{:keys [foreground-color
           background-color]
    :or {foreground-color default-foreground-color
         background-color default-background-color}}]
  {:type "Empty"
   :foreground-color foreground-color
   :background-color background-color})

(s/def ::empty-block
  (s/keys :req-un [::type
                   ::color/foreground-color
                   ::color/background-color]))

(s/fdef empty-block
        :args (s/cat :options (s/keys :opt-un [::color/foreground-color
                                               ::color/background-color]))
        :ret ::empty-block)

(s/def ::block (s/or ::text-block 
                     ::empty-block))

(defn terminal-content
  [rows cols foreground-color background-color]
  (let [terminal-block (empty-block
                        {:foreground-color foreground-color
                         :background-color background-color})
        terminal-row (vec (repeat cols terminal-block))]
    (vec (repeat rows terminal-row))))

(s/def ::terminal-font (s/keys :req-un [::family ::ratio]))
(s/def ::terminal-row (s/coll-of ::block []))
(s/def ::content (s/coll-of ::terminal-row []))

(defn terminal
  [context
   {:keys [rows cols size font offset
           foreground-color
           background-color]
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
             foreground-color background-color)})


(s/def ::context #(instance? js/CanvasRenderingContext2D %))
(s/def ::rows ::schemas/unsigned-int)
(s/def ::cols ::schemas/unsigned-int)
(s/def ::size (s/and number? schemas/>=zero?))
(s/def ::offset (s/cat :x-offset ::schemas/unsigned-int
                       :y-offset ::schemas/unsigned-int))

(s/def ::options (s/keys :req-un
                         [::color/foreground-color
                          ::color/background-color
                          ::offset
                          ::font/font]))

(s/def ::terminal (s/keys :req-un
                          [::context
                           ::options
                           ::rows
                           ::cols
                           ::size
                           ::content]))

(s/fdef terminal
        :args (s/cat :context ::context
                     :terminal-options 
                     (s/keys :req-un [::rows
                                      ::cols
                                      ::size
                                      ::font/font
                                      ::offset
                                      ::color/foreground-color
                                      ::color/background-color]))
        :ret ::terminal)

(defn locate-block
  "returns the pixel location and dimensions of the block in pixels"
  [{:keys [size options]} col-index row-index]
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

(s/fdef locate-block
        :args (s/cat :terminal ::terminal
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int))

(defn get-block
  "get the block at the given column-row point"
  [{:keys [content]}
   col-index row-index]
  (-> content (get row-index) (get col-index)))

(defn put-block
  [{:keys [content rows cols] :as term}
   block col-index row-index]
  (if (and (< col-index cols) (< row-index rows))
    (let [new-content
          (specter/setval [(specter/keypath row-index)
                           (specter/keypath col-index)]
                          block content)]
      (assoc term :content new-content))
    (do
      (.log js/console "Failed Index" col-index row-index)
      term)))

(s/fdef put-block
        :args (s/cat :terminal ::terminal
                     :block ::block
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int)
        :ret ::terminal)

(defn clear-block
  [{:keys [options] :as term}
   col-index row-index]
  (let [{:keys [foreground-color background-color]} options
        empty (empty-block {:foreground-color foreground-color
                            :background-color background-color})]
    (put-block term empty col-index row-index)))

(s/fdef clear-block
        :args (s/cat :terminal ::terminal
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int)
        :ret ::terminal)

(defmulti draw-block!
  (fn [term block row-index col-index]
    (:type block)))

(defmethod draw-block! "Empty"
  [term block col-index row-index]
  (let [{:keys [context options]}
        term
        {:keys [background-color]}
        options
        {:keys [x y width height]}
        (locate-block term col-index row-index)]
    (canvas/fill-rect context x y width height :color background-color)))

(defmethod draw-block! "Text"
  [term block
   col-index row-index]
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

(s/fdef refresh!
        :args (s/cat :terminal ::terminal)
        :ret ::terminal)

(defn NAV-SUBMAT [i j rows cols]
  [[(specter/srange j (+ j rows)) specter/ALL] (specter/srange i (+ i cols))])

(defn sub-term
  "Get a sub terminal, representing a section of the current
  terminal.

  A sub-terminal has the same characteristics as a normal
  Terminal. col-offset and row-offset are the offset from the top-left
  corner of the terminal. cols and rows are the size extents of the
  sub terminal, which should be smaller than or equal to the area you
  are attempting to make a sub-terminal from"
  [{:keys [content] :as term}
   col-offset row-offset
   cols rows]
  (let [new-content 
        (specter/select (NAV-SUBMAT col-offset row-offset cols rows) content)]
    (-> term 
        (assoc :content new-content)
        (assoc :cols cols)
        (assoc :rows rows)
        (assoc-in [:options :offset] [col-offset row-offset]))))

(s/fdef sub-term
        :args (s/cat :terminal ::terminal
                     :col-offset ::schemas/unsigned-int
                     :row-offset ::schemas/unsigned-int
                     :cols ::schemas/unsigned-int
                     :rows ::schemas/unsigned-int)
        :ret ::terminal)

(defn put-term
  "Fill a section of a terminal with a sub-terminal"
  [term sub-term
   col-offset row-offset]
  (let [sub-rows (:rows sub-term)
        sub-cols (:cols sub-term)]
    (loop [i 0 j 0 new-term term]
      (let [sub-block (get-block sub-term i j)
            ioffset (+ i col-offset)
            joffset (+ j row-offset)
            updated-term (put-block new-term sub-block ioffset joffset)]
        (cond
          (and (>= (inc j) sub-rows) (>= (inc i) sub-cols))
          updated-term
          (>= (inc i) sub-cols)
          (recur 0 (inc j) updated-term)
          :else
          (recur (inc i) j updated-term)
          )))))

(s/fdef put-term
        :args (s/cat :term ::terminal
                     :sub-term ::terminal
                     :col-offset ::schemas/unsigned-int
                     :row-offset ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-blocks [term col1 row1 col2 row2]
  (let [first-block (get-block term col1 row1)
        second-block (get-block term col2 row2)]
    (-> term
        (put-block first-block col2 row2)
        (put-block second-block col1 row1))))

(s/fdef swap-blocks
        :args (s/cat :term ::terminal
                     :col1 ::schemas/unsigned-int
                     :row1 ::schemas/unsigned-int
                     :col2 ::schemas/unsigned-int
                     :row2 ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-block-left
  [term col row]
  (if (<= col 0)
    (clear-block term col row)
    (swap-blocks term col row (dec col) row)))

(s/fdef swap-block-left
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-block-right
  [{:keys [cols] :as term} col row]
  (if (>= col cols)
    (clear-block term col row)
    (swap-blocks term col row (inc col) row)))

(s/fdef swap-block-right
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-block-up
  [term col row]
  (if (<= row 0)
    (clear-block term col row)
    (swap-blocks term col row col (dec row))))

(s/fdef swap-block-up
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-block-down
  [{:keys [rows] :as term} col row]
  (if (>= row rows)
    (clear-block term col row)
    (swap-blocks term col row col (inc row))))

(s/fdef swap-block-down
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-col-left
  [{:keys [rows] :as term} col]
  (if (<= col 0)
    (reduce #(clear-block %1 %2 col) term (range rows))
    (reduce #(swap-blocks %1 col %2 (dec col) %2) term (range rows))))

(s/fdef swap-col-left
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-col-right
  [{:keys [rows cols] :as term} col]
  (if (>= col cols)
    (reduce #(clear-block %1 %2 col) term (range rows))
    (reduce #(swap-blocks %1 col %2 (inc col) %2) term (range rows))))

(s/fdef swap-col-right
        :args (s/cat :term ::terminal
                     :col ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-row-up
  [{:keys [rows cols] :as term} row]
  (if (<= row 0)
    (reduce #(clear-block %1 row %2) term (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (dec row)) term (range cols))))

(s/fdef swap-row-up
        :args (s/cat :term ::terminal
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn swap-row-down
  [{:keys [rows cols] :as term} row]
  (if (>= row rows)
    (reduce #(clear-block %1 row %2) term (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (inc row)) term (range cols))))

(s/fdef swap-row-down
        :args (s/cat :term ::terminal
                     :row ::schemas/unsigned-int)
        :ret ::terminal)

(defn resize
  "Resize a Terminal to fill the new resized extents"
  [term & {:keys [rows cols size]}])
