(ns fluch.schemas
  (:require [schema.core :as schema :include-macros true]
            [cljs.spec :as s]))

(enable-console-print!)

(defn in-unit-interval? [x]
  (and (>= x 0.0) (<= 1.0)))
(def UnitInterval (schema/constrained schema/Num in-unit-interval?))
(s/def ::unit-interval (s/and integer? in-unit-interval?))

(defn in-byte-range? [x]
  (and (<= x 255) (>= x 0) (integer? x)))

(defn >zero? [x] (> x 0))
(defn >=zero? [x] (>= x 0))

(def Letter schema/Str)
(s/def ::letter string?)

(def ByteRange (schema/constrained schema/Num in-byte-range?))
(s/def ::byterange (s/and integer? >=zero? in-byte-range?))

(def Color [(schema/one ByteRange "r")
            (schema/one ByteRange "g")
            (schema/one ByteRange "b")
            (schema/one ByteRange "a")])
;; RGBA
(s/def ::color (s/tuple ::byterange ::byterange ::byterange ::byterange))

;;
;; Type of Terminal Blocks
;;

(def TextBlock {:type (schema/eq "Text")
                :foreground-color Color
                :background-color Color
                :style {(schema/optional-key :bold) schema/Bool
                        (schema/optional-key :underline) schema/Bool
                        (schema/optional-key :italic) schema/Bool}
                :text Letter})

(s/def ::foreground-color ::color)
(s/def ::background-color ::color)
(s/def ::style (s/keys :opt-un [::bold ::underline ::italic]))

(s/def ::textblock 
  (s/keys 
   :req-un [::type
            ::foreground-color
            ::background-color
            ::style]))

(def EmptyBlock {:type (schema/eq "Empty")
                 :foreground-color Color
                 :background-color Color})

;; Checking type of Terminal Block
(defn is-type? [xs x] (= (:type xs) x))
(def TerminalBlock
  (schema/conditional
     #(is-type? % "Text")
     TextBlock
     :else
     EmptyBlock
     ))

(def TerminalContent
  "The terminal blocks that make up the terminal"
  [[TerminalBlock]])

(def TerminalFont
  "Schema for a terminal font"
  {:family schema/Str
   :ratio [(schema/one schema/Num "x-ratio")
           (schema/one schema/Num "y-ratio")]})

(def TerminalOffset
  [(schema/one schema/Num "col-offset")
   (schema/one schema/Num "row-offset")])

(def TerminalOptions
  "Schema for Terminal Options"
  {:foreground-color Color
   :background-color Color
   :font TerminalFont
   :offset TerminalOffset
   })

(def Terminal
  "Schema for a Terminal"
  {:context js/CanvasRenderingContext2D
   :options TerminalOptions
   :rows (schema/pred integer?)
   :cols (schema/pred integer?)
   :size (schema/constrained schema/Num >zero?)
   :content TerminalContent})
