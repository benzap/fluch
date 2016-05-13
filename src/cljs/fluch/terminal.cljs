(ns fluch.terminal
  (:require [schema.core :as s :include-macros true]
            [fluch.schemas :as schemas]))

(def default-num-rows 24)
(def default-num-cols 80)
(def default-cell-size 12) ;; px
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
           foreground-color
           background-color]
    :or {rows default-num-rows
         cols default-num-cols
         size default-cell-size
         foreground-cdddolor default-foreground-color
         background-color default-background-color}}]
  {:context context
   :options {:foreground-color foreground-color
             :background-color background-color}
   :rows rows
   :cols cols
   :size size
   :content (terminal-content
             rows cols
             foreground-color background-color)
   })

(s/defn draw :- schemas/Terminal
  [terminal :- schemas/Terminal]
  
  )
