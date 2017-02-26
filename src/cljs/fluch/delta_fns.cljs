(ns fluch.delta-fns
  (:require [cljs.spec :as s]))

(defmulti apply-block-property! (fn [element prop] (first prop)))

(defmethod apply-block-property! :type
  [element [_ value]])

(defmethod apply-block-property! :content
  [element [_ value]]
  (aset element "innerHTML" value))

(defmethod apply-block-property! :background-color
  [element [_ value]]
  (aset element "style" "backgroundColor" value))

(defmethod apply-block-property! :foreground-color
  [element [_ value]]
  (aset element "style" "color" value))

(defmethod apply-block-property! :style
  [element [_ value]]
  (doseq [[k v] value]
    (condp = k
      :bold
      (aset element "style" "fontWeight" (if v "bold" "normal"))
      :underline
      (aset element "style" "textDecoration" (if v "underline" "none"))
      :strikethrough
      (aset element "style" "textDecoration" (if v "line-through" "none"))
      :italic
      (aset element "style" "fontStyle" (if v "italic" "normal")))))

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
