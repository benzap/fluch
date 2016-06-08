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
(s/def ::event-bus #(not (nil? %)))
(s/def ::event-channel ::schemas/async-channel)
(s/def ::in-channel ::schemas/async-channel)
(s/def ::out-channel ::schemas/async-channel)

(s/def ::terminal
  (s/keys :req-un [::screen
                   ::cursor
                   ::event-bus
                   ::event-channel
                   ::in-channel
                   ::out-channel
                   ::options]))

(declare process-input-text)

(defprotocol ITerminal
  (init [this])
  (current-position [this])
  (move-to! [this col row])
  (put-char! [this char])
  (delete-char! [this])
  (up-char! [this])
  (right-char! [this])
  (down-char! [this])
  (left-char! [this])
  (beginning-of-line! [this])
  (end-of-line! [this])
  (new-line! [this])
  (delete-backward-char! [this])
  (in> [this input options])
  (in>> [this input options])
  (refresh! [this])
  (out< [this options])
  (out<< [this callback options]))

(defrecord Terminal [screen cursor event-bus event-channel in-channel out-channel options]
  ITerminal
  (init [this]
    (events/init-terminal this)

    ;; Track Events, tap into the event-bus
    (let [event-channel (chan)]
      (tap event-bus event-channel)
      (go-loop []
        (let [{:keys [type key] :as event} (<! event-channel)]
          (case type
            :key-press
            (>! in-channel key)
            :key-press-special
            (case key
              :backspace
              (>! in-channel "\b")
              :enter
              (>! in-channel "\n")
              nil)
            nil)
          )
        (recur)))
    
    (go-loop []
      (let [text (<! in-channel)]
        (process-input-text this text)
        (refresh! this))
      (recur)))

  (current-position [this]
    @cursor)
  
  (move-to! [this col row]
    (reset! cursor [col row]))

  (put-char! [this char]
    (let [[i j] (current-position this)]
      (reset! screen (screen/put-char @screen char i j {}))))

  (delete-char! [this]
    (let [[i j] (current-position this)]
      (reset! screen (screen/clear-block @screen i j))))

  (up-char! [this]
    (let [[i j] (current-position this)]
      (when (> j 0)
        (move-to! this i (dec j)))))

  (right-char! [this]
    (let [[i j] (current-position this)
          {:keys [rows cols]} @screen]
      (cond 
        (< i (dec cols))
        (move-to! this (inc i) j)
        (< j (dec rows))
        (move-to! this 0 (inc j)))))

  (down-char! [this]
    (let [[i j] (current-position this)
          {:keys [rows cols]} @screen]
      (when (< j (dec rows))
        (move-to! this i (inc j)))))

  (left-char! [this]
    (let [[i j] (current-position this)]
      (when (> i 0)
        (move-to! this (dec i) j))))

  (beginning-of-line! [this]
    (let [[i j] (current-position this)]
      (move-to! this 0 j)))

  (end-of-line! [this]
    (let [[i j] (current-position this)
          {:keys [rows cols]} @screen]
      (move-to! this (dec cols) j)))

  (new-line! [this]
    (down-char! this)
    (beginning-of-line! this))

  (delete-backward-char! [this]
    (left-char! this)
    (delete-char! this))

  (in> [this input options])

  (in>> [this input options])

  (refresh! [this]
    (screen/refresh! @screen))

  (out< [this options])

  (out<< [this callback options]))

(defn process-input-text [terminal text]
  (loop [text text]
    (cond
      (<= (count text) 0)
      nil
      (.startsWith text "\n")
      (do 
        (new-line! terminal)
        (recur (-> text rest rest)))
      (.startsWith text "\b")
      (do
        (delete-backward-char! terminal)
        (recur (-> text rest rest)))
      :else
      (do
        (put-char! terminal (first text))
        (right-char! terminal))
      )))

(defn terminal
  [screen
   {:keys [echo? cbreak? show-cursor?]
    :or {echo? true
         cbreak? true
         show-cursor? true}}]
  (let [event-channel (chan)
        event-bus (mult event-channel)
        in-channel (chan)
        out-channel (chan)]
    (map->Terminal
     {:screen (atom screen)
      :cursor (atom [0 0])
      :event-bus event-bus
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
