(ns fluch.buffer
  (:require [cljs.spec :as s]))

(defprotocol IBuffer
  (insert [this text pos])
  (append [this text])
  (delete [this pos])
  (flush [this out-channel]))

(defrecord Buffer [string]
  IBuffer
  (insert [this text pos]
    (let [begin (.substr string 0 pos)
          end (.substr string pos)]
      (assoc this :string (str begin text end))))

  (append [this text]
    (assoc this :string (str string text)))

  (delete [this pos]
    (let [begin (.substr string 0 (dec pos))
          end (.substr string pos)]))

  (flush [this out-channel]))

(defn buffer []
  (map->Buffer
   {:string ""
    :cursor 0}))
