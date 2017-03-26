(ns fluch.view
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [fluch.utils :refer [console-time]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.string :as string]
            [clojure.data :refer [diff]]
            [garden.core :as garden]))

(defn class-encode [s]
  (string/replace s #"([_\s.,#])" "-"))

(defn class-root [view]
  (str "fluch-view-" (class-encode (::name view))))

(defn- init [view]
  (doto (::root-dom view)
    (.setAttribute "class" (class-root view)))
  view)

(defn create [name root-dom]
  (let [view
        {::name name
         ::root-dom root-dom
         ::input-channel (chan)}]
    (init view)))
    
(defn clear! [view]
  (doto (::root-dom view)
    (aset "innerHTML" "")))

(defn draw! [view text]
  (let [el (.createElement js/document "span")
        style (garden/style {:position "relative"
                             :color "white"
                             :font-family "monospace"})]
    (doto el
      (.setAttribute "style" style))
    (aset el "innerHTML" text)
    (.appendChild (view ::root-dom) el)))
