(ns fluch.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.spec :as s]
            [cljs.core.async :refer [put! chan <! close!]]
            [fluch.terminal :as terminal]))

(.log js/console "Hello Fluch!")

(defn terminal
  "Create a terminal within the given DOMElement
  
  # Optional Parameters
  
  rows - Number of rows that make up the terminal
  cols - Number of columns that make up the terminal"
  [dom-root opts])

