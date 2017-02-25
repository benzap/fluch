(ns fluch.delta-fns
  (:require [cljs.spec :as s]))

(defmulti apply-block-property! (fn [element prop] (first prop)))

(defmethod apply-block-property! :type
  [element [_ value]])

(defmethod apply-block-property! :content
  [element [_ value]]
  (aset element "innerHTML" value))

(defmulti apply-delta! (fn [aview delta] (:action delta)))

(defmethod apply-delta! :modify
  [aview {:keys [index props] :as delta}]
  (let [dom-elements (-> @aview :dom-elements)
        element (get dom-elements index)]
    (doseq [prop props]
      (apply-block-property! element prop))))

(defn apply-block-deltas! [aview block-deltas]
  (doseq [delta block-deltas]
    (apply-delta! aview delta)))
