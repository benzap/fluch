(ns fluch.screen
  (:require [cljs.spec :as s]
            [com.rpl.specter :as specter]
            
            [fluch.utils :refer [boolean?]]
            [fluch.schemas :as schemas]
            [fluch.color :as color]
            [fluch.canvas :as canvas]
            [fluch.font :as font]))

(def default-num-rows 36)
(def default-num-cols 96)
(def default-cell-size 20)
(def default-foreground-color [255 255 255 255])
(def default-background-color [0 0 0 255])


(defn text-block
  [text
   {:keys [foreground-color
           background-color
           cursor?
           bold
           underline
           italic]
    :or {foreground-color default-foreground-color
         background-color default-background-color
         cursor? false
         bold false
         underline false
         italic false}}]
  {:type "Text"
   :foreground-color foreground-color
   :background-color background-color
   :cursor? cursor?
   :style {:bold bold
           :underline underline
           :italic italic}
   :text text})

(s/def ::type string?)
(s/def ::text string?)
(s/def ::cursor? boolean?)
(s/def ::text-block 
  (s/keys :req-un [::type 
                   ::color/background-color
                   ::color/foreground-color
                   ::cursor?
                   ::font/style
                   ::text]))

(s/fdef text-block
        :args (s/cat :text ::text
                     :options (s/keys :opt-un [::color/foreground-color
                                               ::color/background-color
                                               ::cursor?
                                               ::font/bold
                                               ::font/underline
                                               ::font/italic]))
        :ret ::text-block)

(defn empty-block
  [{:keys [foreground-color
           background-color
           cursor?]
    :or {foreground-color default-foreground-color
         background-color default-background-color
         cursor? false}}]
  {:type "Empty"
   :foreground-color foreground-color
   :background-color background-color
   :cursor? cursor?})

(s/def ::empty-block
  (s/keys :req-un [::type
                   ::color/foreground-color
                   ::color/background-color
                   ::cursor?]))

(s/fdef empty-block
        :args (s/cat :options (s/keys :opt-un [::color/foreground-color
                                               ::color/background-color
                                               ::cursor?]))
        :ret ::empty-block)

(s/def ::block (s/or :text-block ::text-block 
                     :empty-block ::empty-block))

(defn screen-content
  [rows cols foreground-color background-color]
  (let [screen-block (empty-block
                        {:foreground-color foreground-color
                         :background-color background-color})
        screen-row (vec (repeat cols screen-block))]
    (vec (repeat rows screen-row))))

(s/def ::screen-font (s/keys :req-un [::family ::ratio]))
(s/def ::screen-row (s/coll-of ::block []))
(s/def ::content (s/coll-of ::screen-row []))

(defn screen
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
   :content (screen-content
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

(s/def ::screen (s/keys :req-un
                          [::context
                           ::options
                           ::rows
                           ::cols
                           ::size
                           ::content]))

(s/fdef screen
        :args (s/cat :context ::context
                     :screen-options 
                     (s/keys :req-un [::rows
                                      ::cols
                                      ::size
                                      ::font/font
                                      ::offset
                                      ::color/foreground-color
                                      ::color/background-color]))
        :ret ::screen)

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
        :args (s/cat :screen ::screen
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int))

(defn get-block
  "get the block at the given column-row point"
  [{:keys [content]}
   col-index row-index]
  (-> content (get row-index) (get col-index)))

(s/fdef get-block
        :args (s/cat :screen ::screen
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int)
        :ret ::block)

(defn put-block
  [{:keys [content rows cols] :as screen}
   block col-index row-index]
  (if (and (< col-index cols) (< row-index rows))
    (let [new-content
          (specter/setval [(specter/keypath row-index)
                           (specter/keypath col-index)]
                          block content)]
      (assoc screen :content new-content))
    screen))

(s/fdef put-block
        :args (s/cat :screen ::screen
                     :block ::block
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int)
        :ret ::screen)

(defn clear-block
  [{:keys [options] :as screen}
   col-index row-index]
  (let [{:keys [cursor?]} (get-block screen col-index row-index)
        {:keys [foreground-color background-color]} options
        empty (empty-block {:foreground-color foreground-color
                            :background-color background-color
                            :cursor? cursor?})]
    (put-block screen empty col-index row-index)))

(s/fdef clear-block
        :args (s/cat :screen ::screen
                     :col-index ::schemas/unsigned-int
                     :row-index ::schemas/unsigned-int)
        :ret ::screen)

(defmulti draw-block!
  (fn [screen block row-index col-index]
    (:type block)))

(defmethod draw-block! "Empty"
  [screen block col-index row-index]
  (let [{:keys [context options]} screen
        {:keys [foreground-color background-color cursor?]} (merge options block)
        {:keys [x y width height]} (locate-block screen col-index row-index)
        ;; Reverse the fore and back if it's a cursor
        background-color (if cursor? foreground-color background-color)]
    (canvas/fill-rect context x y width height :color background-color)))

(defmethod draw-block! "Text"
  [screen block
   col-index row-index]
  (let [{:keys [context options size]} screen
        {:keys [foreground-color background-color text font cursor?]} (merge options block)
        {:keys [x y width height]} (locate-block screen col-index row-index)
        ;; Reverse the fore and back if it's a cursor
        foreground-color (if cursor? background-color foreground-color)
        background-color (if cursor? foreground-color background-color)]
    (canvas/fill-rect context x y width height :color background-color)
    (canvas/draw-text context text x y
                      :size size
                      :family (:family font)
                      :foreground-color foreground-color)))

(defn refresh!
  ([screen i j rows cols]
   (let [rows (+ rows j)
         cols (+ cols i)]
     (loop [i i j j]
       (draw-block! screen (get-block screen i j) i j)
       (cond
         ;; processed all blocks
         (and (>= (inc j) rows) (>= (inc i) cols))
         screen
         ;; at the end of the row
         (>= (inc i) cols)
         (recur 0 (inc j))
         ;; process the next column in the row
         :else
         (recur (inc i) j)))))
  ([{:keys [rows cols] :as screen}]
   (refresh! screen 0 0 rows cols))
  ([{:keys [rows cols] :as screen} i j]
   (refresh! screen i j 1 1)))

(s/fdef refresh!
        :args (s/cat :screen ::screen
                     :i ::schemas/unsigned-int
                     :j ::schemas/unsigned-int
                     :rows ::schemas/unsigned-int
                     :cols ::schemas/unsigned-int)
        :ret ::screen)

(defn NAV-SUBMAT [i j rows cols]
  [[(specter/srange j (+ j rows)) specter/ALL] (specter/srange i (+ i cols))])

(defn sub-screen
  "Get a sub screen, representing a section of the current
  screen.

  A sub-screen has the same characteristics as a normal
  Screen. col-offset and row-offset are the offset from the top-left
  corner of the screen. cols and rows are the size extents of the
  sub screen, which should be smaller than or equal to the area you
  are attempting to make a sub-screen from"
  [{:keys [content] :as screen}
   col-offset row-offset
   cols rows]
  (let [new-content 
        (specter/select (NAV-SUBMAT col-offset row-offset cols rows) content)]
    (-> screen 
        (assoc :content new-content)
        (assoc :cols cols)
        (assoc :rows rows)
        (assoc-in [:options :offset] [col-offset row-offset]))))

(s/fdef sub-screen
        :args (s/cat :screen ::screen
                     :col-offset ::schemas/unsigned-int
                     :row-offset ::schemas/unsigned-int
                     :cols ::schemas/unsigned-int
                     :rows ::schemas/unsigned-int)
        :ret ::screen)

(defn put-screen
  "Fill a section of a screen with a sub-screen"
  [screen sub-screen
   col-offset row-offset]
  (let [sub-rows (:rows sub-screen)
        sub-cols (:cols sub-screen)]
    (loop [i 0 j 0 new-screen screen]
      (let [sub-block (get-block sub-screen i j)
            ioffset (+ i col-offset)
            joffset (+ j row-offset)
            updated-screen (put-block new-screen sub-block ioffset joffset)]
        (cond
          (and (>= (inc j) sub-rows) (>= (inc i) sub-cols))
          updated-screen
          (>= (inc i) sub-cols)
          (recur 0 (inc j) updated-screen)
          :else
          (recur (inc i) j updated-screen)
          )))))

(s/fdef put-screen
        :args (s/cat :screen ::screen
                     :sub-screen ::screen
                     :col-offset ::schemas/unsigned-int
                     :row-offset ::schemas/unsigned-int)
        :ret ::screen)

(defn enable-cursor
  "Enables the cursor on a given block at col row"
  [screen col row]
  (let [block (get-block screen col row)
        block (assoc block :cursor? true)]
    (put-block screen block col row)))

(defn disable-cursor
  "Disables the cursor on a given block at col row"
  [screen col row]
  (let [block (get-block screen col row)
        block (assoc block :cursor? false)]
    (put-block screen block col row)))

(defn swap-blocks [screen col1 row1 col2 row2]
  (let [first-block (get-block screen col1 row1)
        second-block (get-block screen col2 row2)]
    (-> screen
        (put-block first-block col2 row2)
        (put-block second-block col1 row1))))

(s/fdef swap-blocks
        :args (s/cat :screen ::screen
                     :col1 ::schemas/unsigned-int
                     :row1 ::schemas/unsigned-int
                     :col2 ::schemas/unsigned-int
                     :row2 ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-block-left
  [screen col row]
  (if (<= col 0)
    (clear-block screen col row)
    (swap-blocks screen col row (dec col) row)))

(s/fdef swap-block-left
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-block-right
  [{:keys [cols] :as screen} col row]
  (if (>= col cols)
    (clear-block screen col row)
    (swap-blocks screen col row (inc col) row)))

(s/fdef swap-block-right
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-block-up
  [screen col row]
  (if (<= row 0)
    (clear-block screen col row)
    (swap-blocks screen col row col (dec row))))

(s/fdef swap-block-up
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-block-down
  [{:keys [rows] :as screen} col row]
  (if (>= row rows)
    (clear-block screen col row)
    (swap-blocks screen col row col (inc row))))

(s/fdef swap-block-down
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-col-left
  [{:keys [rows] :as screen} col]
  (if (<= col 0)
    (reduce #(clear-block %1 %2 col) screen (range rows))
    (reduce #(swap-blocks %1 col %2 (dec col) %2) screen (range rows))))

(s/fdef swap-col-left
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-col-right
  [{:keys [rows cols] :as screen} col]
  (if (>= col cols)
    (reduce #(clear-block %1 %2 col) screen (range rows))
    (reduce #(swap-blocks %1 col %2 (inc col) %2) screen (range rows))))

(s/fdef swap-col-right
        :args (s/cat :screen ::screen
                     :col ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-row-up
  [{:keys [rows cols] :as screen} row]
  (if (<= row 0)
    (reduce #(clear-block %1 row %2) screen (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (dec row)) screen (range cols))))

(s/fdef swap-row-up
        :args (s/cat :screen ::screen
                     :row ::schemas/unsigned-int)
        :ret ::screen)

(defn swap-row-down
  [{:keys [rows cols] :as screen} row]
  (if (>= row rows)
    (reduce #(clear-block %1 row %2) screen (range cols))
    (reduce #(swap-blocks %1 %2 row %2 (inc row)) screen (range cols))))

(s/fdef swap-row-down
        :args (s/cat :screen ::screen
                     :row ::schemas/unsigned-int)
        :ret ::screen)

;; TODO
(defn resize
  "Resize a Screen to fill the new resized extents"
  [screen & {:keys [rows cols size]}])

(defn put-char 
  [{:keys [options] :as screen} char col row
   font-options]
  (let [options (merge options font-options)
        block (text-block char options)]
    (put-block screen block col row)))

(s/fdef put-char
        :args (s/cat :screen ::screen
                     :char ::text
                     :col ::schemas/unsigned-int
                     :row ::schemas/unsigned-int
                     :options (s/keys :opt-un [::color/foreground-color
                                               ::color/background-color
                                               ::font/bold
                                               ::font/underline
                                               ::font/italic]))
        :ret ::screen)
