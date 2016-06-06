(ns fluch.terminal
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! >! timeout close! mult tap]]
            [fluch.utils :as utils :refer [boolean?]]
            [fluch.schemas :as schemas]
            [fluch.screen :as screen]
            [fluch.color :as color]
            [fluch.font :as font]
            [fluch.events :as events]))

(def ^:dynamic *focused-terminal* nil)

(defn process-input [])

(s/def ::echo? boolean?)
(s/def ::cbreak? boolean?)
(s/def ::show-cursor? boolean?)

(s/def ::cursor #(instance? cljs.core.Atom %))

(s/def ::options (s/keys :req-un [::echo? ::cbreak? ::show-cursor]))

;; TODO: specs for atoms?
(s/def ::screen #(instance? cljs.core.Atom %))

;; async channels
(s/def ::event-channel ::schemas/async-channel)
(s/def ::in-channel ::schemas/async-channel)
(s/def ::out-channel ::schemas/async-channel)

(s/def ::terminal
  (s/keys :req-un [::screen
                   ::cursor
                   ::event-channel
                   ::in-channel
                   ::out-channel
                   ::options]))

(defprotocol ITerminal
  (init [this]))

(defrecord Terminal [screen cursor event-channel in-channel out-channel options]
  ITerminal
  (init [{:keys [event-channel] :as this}]
    (events/init this)
    (go-loop []
      (let [{:keys [type] :as event} (<! event-channel)]
        (.log js/console "Received Event" event))
      (recur))))

(defn terminal
  [screen
   {:keys [echo? cbreak? show-cursor?]
    :or {echo? true
         cbreak? true
         show-cursor? true}}]
  (let [event-channel (chan)
        in-channel (chan)
        out-channel (chan)]
    (map->Terminal
     {:screen (atom screen)
      :cursor (atom [0 0])
      :event-channel event-channel
      :in-channel in-channel
      :out-channel out-channel
      :options {:echo? echo?
                :cbreak? cbreak?
                :show-cursor show-cursor?}})))

(s/fdef terminal
        :args (s/cat :screen ::screen/screen
                     :options (s/keys :opt-un [::echo?
                                               ::cbreak?
                                               ::show-cursor?]))
        :ret ::terminal)
