(ns fluch.metrics
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! close!]]
            [garden.core :as garden]))

(defn get-metrics [font-family]
  (let [c (chan)
        el (.createElement js/document "span")]
    
    (.setAttribute el "style"
                   (garden/style {:position "absolute"
                                  :left "-100px"
                                  :font-family font-family}))
    (aset el "innerHTML" "a")

    ;;onload, we grab the width parameter
    (.addEventListener el "load" (fn [] 
                                   (put! c (aget el "offsetWidth"))
                                   (.remove el)))

    (.appendChild js/document.body el)
    c))

#_(go (let [width (<! (get-metrics "monospace"))]
  (.log js/console width)))
