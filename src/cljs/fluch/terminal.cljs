(ns fluch.terminal
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [fluch.utils :as utils :refer [boolean?]]
            [fluch.schemas :as schemas]
            [fluch.screen :as screen]
            [fluch.color :as color]
            [fluch.font :as font]
            [fluch.events :as events]))

(s/def ::echo? boolean?)
(s/def ::cbreak? boolean?)
(s/def ::show-cursor? boolean?)


(s/def ::cursor #(instance? % cljs.core.Atom))

(s/def ::options (s/keys :req-un [::echo-mode ::cbreak-mode ::show-cursor]))

;; TODO: specs for atoms?
(s/def ::screen #(instance? % cljs.core.Atom))

;; async channels
(s/def ::event-channel ::schemas/async-channel)
(s/def ::in-channel ::schemas/async-channel)
(s/def ::out-channel ::schemas/async-channel)

(s/def ::terminal
  (s/keys :req-un [::screen
                   ::cursor
                   ::event-channel
                   ::in-channel
                   ::out-channel]))

(defn terminal
  [{:keys [context] :as screen}
   {:keys [echo? cbreak? show-cursor? event-channel in-channel out-channel]
    :or {echo? true
         cbreak? true
         show-cursor? true
         event-channel (chan)
         in-channel (chan)
         out-channel (chan)}}]
  {:screen (atom screen)
   :cursor (atom [0 0])
   :event-channel event-channel
   :in-channel in-channel
   :out-channel out-channel
   :options {:echo? echo?
             :cbreak? cbreak?
             :show-cursor show-cursor?}})

(s/fdef terminal
        :args (s/cat :screen ::screen
                     :options (s/keys :opt-un [::echo?
                                               ::cbreak?
                                               ::show-cursor?
                                               ::event-channel
                                               ::in-channel
                                               ::out-channel]))
        :ret ::terminal)
