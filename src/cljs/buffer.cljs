(ns fluch.buffer
  (:require [cljs.spec :as s]
            [clojure.string :as string]))

(defprotocol IBuffer
  (set-cursor! [this x])
  (move-cursor! [this x])
  (put-cursor! [this c])
  (get-cursor [this])
  (put! [this x c])
  (get-chars [this])
  (clear! [this])
  (clear-chars! [this x]))

(defrecord SimpleBuffer [content cursor]
  IBuffer
  (set-cursor! [this x]
    (reset! cursor x))
  (move-cursor! [this x]
    (swap! cursor + x))
  (put-cursor! [this c]
    (let [i @cursor
          s @content
          sfirst (subs s 0 i)
          slast (subs s i)]
      (reset! content (str sfirst c slast))
      (swap! cursor + (count c))))
  (get-cursor [this]
    @cursor)
  (put! [this x c]
    (let [i x
          s @content
          sfirst (subs s 0 i)
          slast (subs s i)]
      (reset! content (str sfirst c slast))
      (count c)))
  (get-chars [this]
    @content)
  (clear! [this] (reset! content ""))
  (clear-chars! [this x]
    (let [i @cursor
          s @content
          sfirst (subs s 0 (- i x))
          slast (subs s i)]
      (reset! content (str sfirst slast))
      (swap! cursor - x)
      )))

(defn simple-buffer 
  ([content] (->SimpleBuffer (atom content) (atom 0)))
  ([] (simple-buffer "")))
  
