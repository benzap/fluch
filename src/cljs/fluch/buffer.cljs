(ns fluch.buffer
  (:require [cljs.spec :as s]
            [clojure.string :as string]

            [fluch.slider :as slider]))

(s/def ::name string?)
(s/def ::slider ::slider/slider)
(s/def ::dirty? boolean?)

(s/def ::buffer 
  (s/keys :req-n [::name ::slider ::dirty?]))

(defn create [name]
  {::name name
   ::slider (slider/create)
   ::dirty? false})

(defn doto-slider [buffer f & args]
  (update (::slider buffer) apply f args))
