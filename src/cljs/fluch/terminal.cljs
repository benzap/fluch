(ns fluch.terminal
  (:require [cljs.spec :as s]
            
            [fluch.block :as block]
            [fluch.screen :as screen]
            [fluch.view :as view]
            [fluch.buffer :as buffer]))

(defn -init [{:keys [view] :as terminal}]
  terminal)

(defn create [dom-root opts]
  (let [screen (atom (screen/create opts))
        view (view/create-dom-view dom-root screen)
        buffer (buffer/simple-buffer)
        terminal
        (merge opts
               {:dom-root dom-root
                :screen screen
                :view view
                :buffer buffer
                :cursor? false
                :echo? false})]
    (-init terminal)
    ))

(defn put-block!
  [{:keys [screen]} i j block]
  (swap! screen screen/put i j block))

(defn put-char!
  ([{:keys [screen]} i j c opts]
   (swap! screen screen/put i j (block/letter-block c opts)))
  ([screen i j c]
   (put-char! screen i j c {})))

(defn get-block [{:keys [screen]} i j]
  (-> @screen (screen/get-block i j)))

(defn clear-char!
  [{:keys [screen]} i j]
  (swap! screen screen/clear i j))
