(ns fluch.canvas
  (:require [schema.core :as s :include-macros true]))

(s/defn init :- js/CanvasRenderingContext2D
  "Creates a canvas in the given div element"
  [div :- js/Element]
  (let [canvas (.createElement js/document "canvas")]
    (aset canvas "style" "position" "relative")
    (aset canvas "style" "width" "100%")
    (aset canvas "style" "height" "100%")
    (.appendChild div canvas)
    (.getContext canvas "2d")))
