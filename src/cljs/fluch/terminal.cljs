(ns fluch.terminal
  (:require [schema.core :as s :include-macros true]
            [fluch.schemas :as schemas]))

(def default-num-rows 24)
(def default-num-cols 80)
(def default-cell-size 12) ;;px

(s/defn create-terminal :- schemas/Terminal
  [context :- js/Element
   {:keys [rows cols size]
    :or {rows default-num-rows
         cols default-num-cols
         size default-cell-size}}
   :- {:rows (s/pred integer?)
       :cols (s/pred integer?)
       :size s/Num}]
  {:context context}
  )
