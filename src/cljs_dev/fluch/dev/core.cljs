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
#_(s/instrument-all)

(def canvas (c/init (.getElementById js/document "term")))

(def screen (atom (screen/screen canvas {:rows 36 :cols 96
                                         :size 20
                                         :font fluch.font/monospace
                                         :offset [0 0]
                                         :foreground-color [255 255 255 255]
                                         :background-color [255 128 128 255]})))

(def terminal (t/terminal @screen {}))
(t/init terminal)

;; FIXME: Re-render the screen a few times to fix overlap issues
;; between blocks
(screen/refresh! @screen)
(screen/refresh! @screen)
(screen/refresh! @screen)
(screen/refresh! @screen)
