(ns fluch.core
  (:require [cljs.spec :as s]

            [fluch.terminal :as terminal]
            [fluch.buffer :as buffer]))

(.log js/console "Hello Fluch!")

(defn terminal
  "Create a terminal within the given DOMElement
  
  # Optional Parameters
  
  rows - Number of rows that make up the terminal
  cols - Number of columns that make up the terminal"
  [dom-root opts])

