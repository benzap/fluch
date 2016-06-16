(ns fluch.canvas
  (:require [fluch.schemas :as schemas]
            [fluch.color :as color]))

(def default-width 1152)
(def default-height 864)

(defn init
  [div & {:keys [width height]
          :or {width default-width
               height default-height}}]
  (let [dom-canvas (.createElement js/document "canvas")]
    (aset dom-canvas "style" "position" "relative")
    (aset dom-canvas "style" "width" "100%")
    (aset dom-canvas "style" "height" "100%")
    (aset dom-canvas "width" width)
    (aset dom-canvas "height" height)
    (.appendChild div dom-canvas)
    (let [context (.getContext dom-canvas "2d")]
      (aset context "imageSmoothingEnabled" false)
      context)))

(defn fill-rect
  [ctx x y width height
   & {:keys [color]
      :or {color [255 255 255 255]}}]
  (let [scolor (color/term->rgba color)]
    (.beginPath ctx)
    (.rect ctx x y width height)
    (aset ctx "fillStyle" scolor)
    (.fill ctx))
  ctx)

(defn draw-text
  [ctx text x y
   & {:keys [size family foreground-color]
      :or {size 12
           family "monospace"
           foreground-color [255 255 255 255]}}]
  (aset ctx "font" (str size "px" " " family))
  (aset ctx "fillStyle" (color/term->rgba foreground-color))
  (aset ctx "textAlign" "left")
  (aset ctx "textBaseline" "top")
  (.fillText ctx text x y))
