(ns fluch.buffer
  (:require [cljs.spec :as s]
            [clojure.string :as string]))

(defprotocol IBuffer
  (set-cursor [this x])
  (move-cursor [this x])
  (put-cursor [this c])
  (get-cursor [this])
  (put [this x c])
  (get-chars [this])
  (clear [this])
  (clear-chars [this x]))

(defrecord SimpleBuffer [content cursor scroll]
  IBuffer
  (set-cursor [this x]
    (assoc this :cursor x))
  (move-cursor [this x]
    (update this :cursor + x))
  (put-cursor [this c]
    (let [i cursor
          s content
          sfirst (subs s 0 i)
          slast (subs s i)]
      (-> this
          (assoc :content (str sfirst c slast))
          (update :cursor + (count c)))))
  (get-cursor [this]
    (this :cursor))
  (put [this x c]
    (let [i x
          s content
          sfirst (subs s 0 i)
          slast (subs s i)]
      (assoc this :content (str sfirst c slast))))
  (get-chars [this]
    (this :content))
  (clear [this] (assoc this :content ""))
  (clear-chars [this x]
    (let [i cursor
          s content
          sfirst (subs s 0 (- i x))
          slast (subs s i)]
      (-> this
          (assoc :content (str sfirst slast))
          (update cursor - x)))))

(defn simple-buffer
  ([content] (->SimpleBuffer content 0 0))
  ([] (simple-buffer "")))
  
