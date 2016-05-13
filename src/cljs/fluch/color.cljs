(ns fluch.color
  "Includes functions for creating colors and manipulating colors for
  the terminal canvas"
  (:require [schema.core :as s :include-macros true]
            [fluch.schemas :as schemas]))

(def re-hex #"(?i)#([0-9A-F]{1,2})([0-9A-F]{1,2})([0-9A-F]{1,2})")
(def re-rgba #"(?i)rgba\(([0-9]+),([0-9]+),([0-9]+),([0-9.]+)\)")
(def re-rgb #"(?i)rgb\(([0-9]+),([0-9]+),([0-9]+)\)")

(s/defn byte->unit-interval :- schemas/UnitInterval
  [x :- schemas/ByteRange]
  (/ x 255))

(s/defn unit-interval->byte :- schemas/ByteRange
  [x :- schemas/UnitInterval]
  (->> x (* 255) (.round js/Math)))

(defn- fix-hex
  "Fixes single letter string hex values
  ie. #fff --> #ffffff"
  [s]
  (if (= (count s) 1)
    (str s s)
    s))

(s/defn hex->term :- schemas/Color
  [s :- s/Str]
  (when-let [[_ r g b] (re-matches re-hex s)]
    [(js/parseInt (fix-hex r) 16)
     (js/parseInt (fix-hex g) 16)
     (js/parseInt (fix-hex b) 16)
     255]))

(s/defn rgba->term :- schemas/Color
  [s :- s/Str]
  (when-let [[_ r g b a] (re-matches re-rgba s)]
    [(js/parseInt r 16) (js/parseInt g 16) (js/parseInt b 16)
     (unit-interval->byte (js/parseFloat a))]))

(s/defn term->rgba :- s/Str
  [color :- schemas/Color]
  (let [[r g b a] color]
    (str "rgba(" r "," g "," b "," (byte->unit-interval a) ")")))

(s/defn rgb->term :- schemas/Color
  [s]
  (when-let [[_ r g b] (re-matches re-rgb s)]
    [(js/parseInt r 16) (js/parseInt g 16) (js/parseInt b 16) 255]))

(s/defn lighten :- schemas/Color
  [c :- schemas/Color ratio :- schemas/UnitInterval]
  
  )

