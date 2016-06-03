(ns fluch.dev.core
  (:require
   [devtools.core :as devtools]
   [fluch.core :as fluch]
   [fluch.terminal :as t]
   [fluch.canvas :as c]
   [fluch.font]))

(devtools/install!)
(enable-console-print!)

(def canvas (c/init (.getElementById js/document "term")))
(.log js/console "canvas" canvas)

(def term (atom (t/terminal canvas {:rows 4 :cols 4
                                    :size 60
                                    :font fluch.font/monospace
                                    :offset [1 1]
                                    :foreground-color [255 255 255 255]
                                    :background-color [255 128 128 255]})))
(reset! term (t/put-block @term (t/text-block "a" {}) 1 1))
(reset! term (t/swap-block-left @term 1 1))
(reset! term (t/swap-col-left @term 1))
(reset! term (t/swap-row-up @term 1))
(reset! term (t/swap-row-up @term 1))
(let [sterm (t/sub-term @term 1 1 2 2)]
  (.log js/console "sterm" sterm)
  )

(t/refresh! @term)

(.log js/console "term" @term)
