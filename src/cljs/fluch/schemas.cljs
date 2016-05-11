(ns fluch.schemas
  (:require [schema.core :as s :include-macros true]))

(enable-console-print!)

(defn in-byte-range? [x]
  (and (<= x 255) (>= x 0)))

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

(def TerminalContent
  "The terminal blocks that make up the terminal"
  [[(s/conditional
     #(is-type? % "Text")
     TextBlock
     :else
     EmptyBlock
     )]])

(def TerminalOptions
  "Schema for Terminal Options"
  {:foreground-color Color
   :background-color Color
   })

(def Terminal
  "Schema for a Terminal"
  {:context js/CanvasRenderingContext2D
   :options TerminalOptions
   :rows (s/pred integer?)
   :cols (s/pred integer?)
   :size (s/constrained s/Num >zero?)
   :content TerminalContent})
