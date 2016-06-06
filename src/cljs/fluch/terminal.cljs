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
  (init [this])
  (current-position [this])
  (move-to! [this col row])
  (put-char! [this char])
  (up-char! [this])
  (right-char! [this])
  (down-char! [this])
  (left-char! [this])
  (in> [this input options])
  (refresh! [this])
  (out< [this options])
  (out<< [this callback options]))

(defrecord Terminal [screen cursor event-channel in-channel out-channel options]
  ITerminal
  (init [{:keys [event-channel] :as this}]
    (events/init this)

    ;; Track Events
    (go-loop []
      (let [{:keys [type] :as event} (<! event-channel)]
        (.log js/console "Received Event" event))
      (recur)))

  (current-position [{:keys [cursor]}]
    @cursor)
  
  (move-to! [{:keys [cursor]} col row]
    (reset! cursor [col row]))

  (put-char! [{:keys [screen] :as this} char]
    (let [[i j] (current-position this)]
      (reset! screen (screen/put-char @screen char i j {}))))

  (up-char! [this]
    (let [[i j] (current-position this)]
      (when (> j 0)
        (move-to! this i (dec j)))))

  (right-char! [{:keys [screen] :as this}]
    (let [[i j] (current-position this)
          {:keys [rows cols]} @screen]
      (when (< i (dec cols))
        (move-to! this (inc i) j))))

  (down-char! [{:keys [screen] :as this}]
    (let [[i j] (current-position this)
          {:keys [rows cols]} @screen]
      (when (< j (dec rows))
        (move-to! this i (inc j)))))

  (left-char! [this]
    (let [[i j] (current-position this)]
      (when (> i 0)
        (move-to! this (dec i) j))))

  (in> [this input options])

  (refresh! [{:keys [screen]}]
    (screen/refresh! @screen))

  (out< [this options])

  (out<< [this callback options]))

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
