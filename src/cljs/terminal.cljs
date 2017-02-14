(ns fluch.terminal
  (:require [cljs.spec :as s]
            
            [fluch.block :as block]))

(def default-num-rows 36)
(def default-num-cols 96)
(def default-cell-dimensions [20 20])
(def default-font {:family "monospace" :size 12})
(def default-foreground-color "rgba(255, 255, 255, 1.0)")
(def default-background-color "rgba(0, 0, 0, 0.0)")

(defprotocol ITerminal
  (set-echo! [this flag])
  (set-cursor! [this flag])
  (cursor-put [this block])
  (cursor-set [this x y])
  (cursor-get [this])
  (screen-properties [this])
  (put [this block x y]))

(defrecord DOMTerminal [root-dom context])



(defn create [root-dom {:keys [rows cols
                               foreground-color background-color
                               cell-dimensions
                               font cursor? echo?]
                        :or {rows default-num-rows
                             cols default-num-cols
                             foreground-color default-foreground-color
                             background-color default-background-color
                             cell-dimensions default-cell-dimensions
                             font default-font
                             cursor? true
                             echo? true}}]
  (let [context {:rows rows :cols cols
                 :screen (atom [])
                 :buffer []
                 :foreground-color foreground-color
                 :background-color background-color
                 :cell-dimensions cell-dimensions
                 :font font
                 :cursor? cursor?
                 :echo? echo?}]
    (->DOMTerminal root-dom context)))
