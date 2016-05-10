(ns fluch.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <!]]
            [fluch.schemas :as schemas]))

(enable-console-print!)


