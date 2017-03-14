(ns fluch.block
  (:require [cljs.spec :as s]))

(def default-block-width 16)
(def default-block-height 24)

(defn create [char x y]
  {::char char
   ::x x
   ::y y
   ::foreground-color "#fdfdfd"
   ::background-color "#1d1d1d"
   ::font-family "monospace"
   ::font-size default-block-height
   ::width default-block-width
   ::height default-block-height
   ::top (* default-block-height y)
   ::left (* default-block-width x)})

(defn set-position [block x y]
  (assoc block
         ::x x
         ::y y
         ::top (* (::width block) y)
         ::left (* (::height block) x)))

(defn set-foreground [block foreground-color]
  (assoc block
         ::foreground-color foreground-color))

(defn set-background [block background-color]
  (assoc block
         ::background-color background-color))

(defn set-font-size [block x]
  (-> block 
      (assoc 
       ::font-size x
       ::width x
       ::height (.floor js/Math (* x 1.5)))
      (set-position (::x block) (::y block))))
      
