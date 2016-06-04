(ns fluch.color
  "Includes functions for creating colors and manipulating colors for
  the terminal canvas"
  (:require [fluch.schemas :as schemas]
            [cljs.spec :as s]))

(s/def ::color (s/tuple ::schemas/byterange
                        ::schemas/byterange
                        ::schemas/byterange
                        ::schemas/byterange))

(s/def ::foreground-color ::color)
(s/def ::background-color ::color)

(def re-hex #"(?i)#([0-9A-F]{1,2})([0-9A-F]{1,2})([0-9A-F]{1,2})")
(def re-rgba #"(?i)rgba\(([0-9]+),\s*([0-9]+),\s*([0-9]+),\s*([0-9.]+)\)")
(def re-rgb #"(?i)rgb\(([0-9]+),\s*([0-9]+),\s*([0-9]+)\)")

(defn byte->unit-interval
  [x] (/ x 255))

(defn unit-interval->byte [x]
  (->> x (* 255) (.round js/Math)))

(defn- fix-hex
  "Fixes single letter string hex values
  ie. #fff --> #ffffff"
  [s]
  (if (= (count s) 1)
    (str s s)
    s))

(defn hex->term [s]
  (when-let [[_ r g b] (re-matches re-hex s)]
    [(js/parseInt (fix-hex r) 16)
     (js/parseInt (fix-hex g) 16)
     (js/parseInt (fix-hex b) 16)
     255]))

(defn term->hex [c]
  (let [[r g b a] (->> c 
                       (map #(.toString % 16))
                       (map fix-hex))]
    (str "#" r g b)))

(defn rgba->term [s]
  (when-let [[_ r g b a] (re-matches re-rgba s)]
    [(js/parseInt r 10) (js/parseInt g 10) (js/parseInt b 10)
     (unit-interval->byte (js/parseFloat a))]))

(defn term->rgba [color]
  (let [[r g b a] color]
    (str "rgba(" r "," g "," b "," (byte->unit-interval a) ")")))

(defn rgb->term [s]
  (when-let [[_ r g b] (re-matches re-rgb s)]
    [(js/parseInt r 10) (js/parseInt g 10) (js/parseInt b 10) 255]))

(defn term->rgb [c]
  (let [[r g b a] c]
    (str "rgb(" r "," g "," b ")")))

(defn lighten
  [c ratio]
  )

