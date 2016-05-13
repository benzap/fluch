(ns fluch.canvas
  (:require [schema.core :as s :include-macros true]
            [fluch.schemas :as schemas]
            [fluch.color :as color]))

(s/defn init :- js/CanvasRenderingContext2D
  "Creates a canvas in the given div element"
  [div :- js/Element]
  (let [canvas (.createElement js/document "canvas")]
    (aset canvas "style" "position" "relative")
    (aset canvas "style" "width" "100%")
    (aset canvas "style" "height" "100%")
    (.appendChild div canvas)
    (.getContext canvas "2d")))

(s/defn fill-rect :- js/CanvasRenderingContext2D
  [ctx :- js/CanvasRenderingContext2D
   x :- s/Num
   y :- s/Num
   width :- s/Num
   height :- s/Num
   {:keys [color]
    :or {color [255 255 255 255]}}
   :- {:color schemas/Color}]
  
  )
