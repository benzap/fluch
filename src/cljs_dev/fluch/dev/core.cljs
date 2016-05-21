(ns fluch.dev.core
  (:require
   [devtools.core :as devtools]
   [schema.core :as s]
   [fluch.core :as fluch]
   [fluch.terminal :as t]
   [fluch.canvas :as c]
   [fluch.font]))

(devtools/install!)
(enable-console-print!)
(s/set-fn-validation! false)

(def canvas (c/init (.getElementById js/document "term")))
(.log js/console "canvas" canvas)

(def term (atom (t/terminal canvas {:rows 4 :cols 4
                                    :size 60
                                    :font fluch.font/monospace
                                    :foreground-color [255 255 255 255]
                                    :background-color [255 128 128 255]})))
(t/refresh @term)

(.log js/console "term" @term)
