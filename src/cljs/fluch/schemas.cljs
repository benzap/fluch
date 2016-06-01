(ns fluch.schemas
  (:require [schema.core :as s :include-macros true]))

(enable-console-print!)

(defn in-unit-interval? [x]
  (and (>= x 0.0) (<= 1.0)))
(def UnitInterval (s/constrained s/Num in-unit-interval?))

(defn in-byte-range? [x]
  (and (<= x 255) (>= x 0) (integer? x)))

(defn >zero? [x] (> x 0))
(defn >=zero? [x] (>= x 0))

(def Letter s/Str)

(def ByteRange (s/constrained s/Num in-byte-range?))

(def Color [(s/one ByteRange "r")
            (s/one ByteRange "g")
            (s/one ByteRange "b")
            (s/one ByteRange "a")])

;;
;; Type of Terminal Blocks
;;

(def TextBlock {:type (s/eq "Text")
                :foreground-color Color
                :background-color Color
                :style {(s/optional-key :bold) s/Bool
                        (s/optional-key :underline) s/Bool
                        (s/optional-key :italic) s/Bool}
                :text Letter})

(def EmptyBlock {:type (s/eq "Empty")
                 :foreground-color Color
                 :background-color Color})


;; Checking type of Terminal Block
(defn is-type? [xs x] (= (:type xs) x))
(def TerminalBlock
  (s/conditional
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
  {:family s/Str
   :ratio [(s/one s/Num "x-ratio")
           (s/one s/Num "y-ratio")]})

(def TerminalOffset
  [(s/one s/Num "col-offset")
   (s/one s/Num "row-offset")])

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
   :rows (s/pred integer?)
   :cols (s/pred integer?)
   :size (s/constrained s/Num >zero?)
   :content TerminalContent})
