(ns fluch.schemas
  (:require [schema.core :as s :include-macros true]))

(enable-console-print!)

(defn num-in-byte-range? [x]
  (and (<= x 255) (>= x 0)))



(def ByteRange (s/constrained s/Num num-in-byte-range?))

(def Color {:r ByteRange
            :g ByteRange
            :b ByteRange
            :a ByteRange})

(def TextBlock {:type (s/eq "Text")
                :foreground-color Color
                :background-color Color
                :text s/Str})

(s/validate TextBlock {:type "Text"
                       :foreground-color {:r 1 :g 1 :b 1 :a 1}
                       :background-color {:r 1 :g 1 :b 1 :a 1}
                       :text "a"})

(def TerminalOptions
  "Schema for Terminal Options"
  {;; defaults
   :foreground-color Color
   :background-color Color
   })

(def Terminal
  "Schema for a Terminal"
  {:context js/Element
   :rows (s/pred integer?)
   :cols (s/pred integer?)
   :size s/Num
   :content [[TextBlock]]})
