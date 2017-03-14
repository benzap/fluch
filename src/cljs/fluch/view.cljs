(ns fluch.view
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [fluch.utils :refer [console-time]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [clojure.string :as string]
            [clojure.data :refer [diff]]
            [garden.core :as garden]

            [fluch.screen :as screen]
            [fluch.block :as block]
            [fluch.delta-fns :as delta-fns]))

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

(defn draw! [view block]
  (let [style 
        (garden/style {:position "absolute"
                       :text-align "center"
                       :line-height (str (::block/height block) "px")
                       :left (str (::block/left block) "px")
                       :top (str (::block/top block) "px")
                       :color (::block/foreground-color block)
                       :background-color (::block/background-color block)
                       :font-family (::block/font-family block)
                       :font-size (str (::block/font-size block) "px")
                       :width (str (::block/width block) "px")
                       :height (str (::block/height block) "px")})
        elem
        (doto (.createElement js/document "div")
          (.setAttribute "style" style)
          (aset "innerHTML" (::block/char block)))]
    (.appendChild (::root-dom view) elem)))

(defn draw-screen! [view screen]
  (doseq [[j _v] (map-indexed vector screen)]
    (doseq [[i block] (map-indexed vector _v)]
      (when block (draw! view block)))))
