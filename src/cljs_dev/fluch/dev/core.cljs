(ns fluch.dev.core
  (:require
   [devtools.core :as devtools]
   [com.rpl.specter :as specter]
   [cljs.spec :as s]
   [fluch.core :as fluch]
   [fluch.screen :as screen]
   [fluch.canvas :as c]
   [fluch.terminal :as t]
   [fluch.font]))

(devtools/install!)
(enable-console-print!)
(s/instrument-all)

(def canvas (c/init (.getElementById js/document "term")))
(.log js/console "canvas" canvas)

(def screen (atom (screen/screen canvas {:rows 4 :cols 4
                                         :size 64
                                         :font fluch.font/monospace
                                         :offset [0 0]
                                         :foreground-color [255 255 255 255]
                                         :background-color [255 128 128 255]})))

(reset! screen (screen/put-block @screen (screen/text-block "a" {}) 1 1))
(reset! screen (screen/swap-block-left @screen 1 1))
(reset! screen (screen/swap-col-left @screen 1))
(reset! screen (screen/swap-row-up @screen 1))
(reset! screen (screen/swap-row-up @screen 1))
(let [sscreen (screen/sub-screen @screen 1 1 2 2)]
  (.log js/console "sscreen" sscreen)
  (reset! screen (screen/put-screen @screen sscreen 2 2))
  (reset! screen (screen/put-screen @screen sscreen 1 2))
  (reset! screen (screen/put-screen @screen sscreen 0 0))
  (reset! screen (screen/put-char @screen "b" 0 0 {}))
  )

(.log js/console "screen" screen)

(screen/refresh! @screen)

