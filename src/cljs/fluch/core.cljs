(ns fluch.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <!]]
            [schema.core :as s]))

(enable-console-print!)

(def Color {:r s/Num :g s/Num :b s/Num})

(println (s/explain Color))

(def TextBlock {:type "Text"
                :foreground-color Color
                :background-color Color
                :text s/Str})

(s/def Terminal
  "Schema for a Terminal"
  {:context js/Element
   :data [[{:foreground-color Color
            :background-color Color
            }]]
   })
