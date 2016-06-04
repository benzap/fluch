(ns fluch.font
  "Includes several fonts, and the ratios required to make them appear
  correctly within the canvas when placing"
  (:require [cljs.spec :as s]
            [fluch.utils :as utils :refer [boolean?]]))

(s/def ::family string?)
(s/def ::ratio (s/cat :x-ratio number? :y-ratio number?))
(s/def ::font (s/keys :req-un [::family  ::ratio]))

(def monospace {:family "monospace" :ratio [1.1 1.15]})
(def courier-new {:family "Courier New" :ratio [1.23 1.13]})
(def monaco {:family "Monaco" :ratio [2.2 1.13]})

(s/def ::bold boolean?)
(s/def ::underline boolean?)
(s/def ::italic boolean?)

(s/def ::style (s/keys :opt-un [::bold ::underline ::italic]))
