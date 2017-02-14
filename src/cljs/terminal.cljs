(ns fluch.terminal
  (:require [cljs.spec :as s]
            
            [fluch.block :as block]
            [fluch.screen :as screen]))

(defn create [root-dom opts]
  (merge 
   opts
   {:root-dom root-dom
    :screen (atom (screen/create))
    :buffer []
    :cursor? false
    :echo? false}))

