(ns fluch.terminal
  (:require [cljs.spec :as s]
            
            [fluch.block :as block]
            [fluch.screen :as screen]
            [fluch.view :as view]))

(defn -init [{:keys [view] :as terminal}]
  terminal)

(defn -on-view-update [old-screen new-screen])

(defn create [dom-root opts]
  (let [screen (atom (screen/create))
        terminal
        (merge opts
               {:dom-root dom-root
                :screen screen
                :view (view/create-dom-view dom-root screen)
                :buffer []
                :cursor? false
                :echo? false})]
    (-init terminal)
    ))

(defn put-block!
  [{:keys [screen]} i j block]
  (swap! screen screen/put i j block))

(defn put-char!
  [{:keys [screen]} i j c]
  (swap! screen screen/put i j (block/letter-block c)))

(defn get-block [{:keys [screen]} i j]
  (-> @screen (screen/get-block i j)))
