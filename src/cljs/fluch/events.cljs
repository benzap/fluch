(ns fluch.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.events :refer [EventType] :as events]
            [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [fluch.schemas :as schemas])
  (:import goog.events))

(.log js/console EventType)

(s/def ::type keyword?)

(defn init-mouse-keydown
  [{:keys [screen event-channel] :as terminal}]
  (events/listen (-> @screen :context .-canvas) (.-MOUSEDOWN EventType)
                 (fn [e]
                   (put! event-channel {:type :mouse-down
                                        :gevent e
                                        :offset [(.-offsetX e) (.-offsetY e)]
                                        :ctrl-pressed? (.-ctrlKey e)
                                        :alt-pressed? (.-altKey e)
                                        :shift-pressed? (.-shiftKey e)
                                        :key (case (.-button e)
                                               0 :left-mouse-button
                                               1 :middle-mouse-button
                                               2 :right-mouse-button)})
                   nil)))

(defn init-mouse-keyup
  [{:keys [screen event-channel] :as terminal}]
  (events/listen (-> @screen :context .-canvas) (.-MOUSEUP EventType)
                 (fn [e]
                   (put! event-channel {:type :mouse-up
                                        :gevent e
                                        :offset [(.-offsetX e) (.-offsetY e)]
                                        :ctrl-pressed? (.-ctrlKey e)
                                        :alt-pressed? (.-altKey e)
                                        :shift-pressed? (.-shiftKey e)
                                        :key (case (.-button e)
                                               0 :left-mouse-button
                                               1 :middle-mouse-button
                                               2 :right-mouse-button)})
                   nil)))

(defn init-mouse-move
  [{:keys [screen event-channel] :as terminal}]
  (events/listen (-> @screen :context .-canvas) (.-MOUSEMOVE EventType)
                 (fn [e]
                   (put! event-channel {:type :mouse-down
                                        :gevent e
                                        :offset [(.-offsetX e) (.-offsetY e)]
                                        :ctrl-pressed? (.-ctrlKey e)
                                        :alt-pressed? (.-altKey e)
                                        :shift-pressed? (.-shiftKey e)
                                        :key (case (.-button e)
                                               0 :left-mouse-button
                                               1 :middle-mouse-button
                                               2 :right-mouse-button)})
                   true)))

(def keyboard-mapping
  {8 "backspace"
   9 "\t"
   13 "\n"
   32 " "
   33 "!"
   34 "\""
   35 "#"
   36 "$"
   37 "%"
   38 "&"
   39 "'"
   40 "("
   41 ")"
   42 "*"
   43 "+"
   44 ","
   45 "-"
   46 "."
   47 "/"
   48 "0"
   49 "1"
   50 "2"
   51 "3"
   52 "4"
   53 "5"
   54 "6"
   55 "7"
   56 "8"
   57 "9"
   58 ":"
   59 ";"
   60 "<"
   61 "="
   62 ">"
   63 "?"
   64 "@"
   65 "A"
   66 "B"
   67 "C"
   68 "D"
   69 "E"
   70 "F"
   71 "G"
   72 "H"
   73 "I"
   74 "J"
   75 "K"
   76 "L"
   77 "M"
   78 "N"
   79 "O"
   80 "P"
   81 "Q"
   82 "R"
   83 "S"
   84 "T"
   85 "U"
   86 "V"
   87 "W"
   88 "X"
   89 "Y"
   90 "Z"
   91 "["
   92 "\\"
   93 "]"
   94 "^"
   95 "_"
   97 "a"
   98 "b"
   99 "c"
   100 "d"
   101 "e"
   102 "f"
   103 "g"
   104 "h"
   105 "i"
   106 "j"
   107 "k"
   108 "l"
   109 "m"
   110 "n"
   111 "o"
   112 "p"
   113 "q"
   114 "r"
   115 "s"
   116 "t"
   117 "u"
   118 "v"
   119 "w"
   120 "x"
   121 "y"
   122 "z"
   123 "{"
   124 "|"
   125 "}"
   })

(defn init-capture-backspace
  "backspace in most browsers causes it to go to the previous
  webpage. This disables the default behaviour."
  [{:keys [event-channel] :as terminal}]
  (events/listen js/window (.-KEYDOWN EventType)
                (fn [e]
                  (when (= (.-keyCode e) 8)
                    (put! event-channel {:type :backspace
                                         :gevent e
                                         :ctrl-pressed? (.-ctrlKey e)
                                         :alt-pressed? (.-altKey e)
                                         :shift-pressed? (.-shiftKey e)
                                         :char-code (.-keyCode e)
                                         :key (get keyboard-mapping (.-keyCode e))})
                    (.preventDefault e)))
                true))

(defn init-keypress
  [{:keys [event-channel] :as terminal}]
  (events/listen js/window (.-KEYPRESS EventType)
                 (fn [e]
                   (put! event-channel {:type :key-press
                                        :gevent e
                                        :ctrl-pressed? (.-ctrlKey e)
                                        :alt-pressed? (.-altKey e)
                                        :shift-pressed? (.-shiftKey e)
                                        :char-code (.-keyCode e)
                                        :key (get keyboard-mapping (.-keyCode e))})
                   #_(.log js/console "key: " (.-keyCode e) (aget e "event_" "code") " --> "
                           (get keyboard-mapping (.-keyCode e)))


                   nil)
                 true))

(defn init [terminal]
  #_(init-mouse-keydown terminal)
  #_(init-mouse-keyup terminal)
  #_(init-mouse-move terminal)
  (init-capture-backspace terminal)
  (init-keypress terminal))
