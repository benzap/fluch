(ns fluch.canvas
  (:require [schema.core :as s :include-macros true]))

(s/defn init :- js/CanvasRenderingContext2D
  "Creates a canvas in the given div element"
  [div :- js/Element]
  (let [canvas (.createElement js/document "canvas")]
    (.setAttribute canvas "style"
                   #js {:position "relative"
                        :width "100%"
                        :height "100%"})
    (.appendChild div canvas)
    (.getContext canvas "2d")))
