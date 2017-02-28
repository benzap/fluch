(ns fluch.terminal
  (:require-macros [fluch.utils :refer [console-time]])
  (:require [cljs.spec :as s]
            
            [fluch.block :as block]
            [fluch.screen :as screen]
            [fluch.view :as view]
            [fluch.buffer :as buffer]))

(defn draw-line [{:keys [num-cols] :as screen} row-index line]
  (let [len (min num-cols (count line))]
    (reduce 
     (fn [s i]
       (screen/put s i row-index (block/letter-block (nth line i))))
     screen (range 0 len))))

(defn redraw [{:keys [buffer screen]}]
  (let [scroll (buffer/get-scroll @buffer)
        {:keys [num-rows num-cols]} @screen
        content-lines (buffer/get-lines @buffer scroll (+ scroll num-rows))
        ]
    ;;(swap! screen screen/clear-all)
    (doseq [[row-index line] (map-indexed vector content-lines)]
      ;; TODO: Apply middleware
      (swap! screen draw-line row-index line)
      )))

(defn buffer-watcher-fn [terminal key ref old-buffer new-buffer]
  (let [old-content (buffer/get-chars old-buffer)
        new-content (buffer/get-chars new-buffer)
        old-cursor (buffer/get-cursor old-buffer)
        new-cursor (buffer/get-cursor new-buffer)
        old-scroll (buffer/get-scroll old-buffer)
        new-scroll (buffer/get-scroll new-buffer)]
    (when (or (not= old-content new-content)
              (not= old-scroll new-scroll))
      (console-time "terminal redraw" (redraw terminal)))
    (when-not (= old-cursor new-cursor)
      ;; TODO: Change cursor
      )))

(defn -add-buffer-watcher! [terminal buffer]
  (let [watch-fn (partial buffer-watcher-fn terminal)]
    (add-watch buffer nil watch-fn)))

(defn -init [{:keys [view buffer] :as terminal}]
  (-add-buffer-watcher! terminal buffer)
  terminal)

(defn create [dom-root opts]
  (let [screen (atom (screen/create opts))
        view (view/create-dom-view dom-root screen)
        buffer (atom (buffer/simple-buffer))
        terminal
        (merge opts
               {:dom-root dom-root
                :screen screen
                :view view
                :buffer buffer
                :cursor? true
                :echo? true})]
    (-init terminal)
    ))

(defn putch!
  [{:keys [buffer] :as terminal} c]
  (swap! buffer buffer/put-cursor c))

(defn put-block!
  [{:keys [screen]} i j block]
  (swap! screen screen/put i j block))

(defn put-char!
  ([{:keys [screen]} i j c opts]
   (swap! screen screen/put i j (block/letter-block c opts)))
  ([terminal i j c]
   (put-char! terminal i j c {})))

(defn get-block [{:keys [screen]} i j]
  (-> @screen (screen/get-block i j)))

(defn clear-char!
  [{:keys [screen]} i j]
  (swap! screen screen/clear i j))
