(ns fluch.test-runner
  (:require [schema.core :as s]
            [doo.runner :refer-macros [doo-tests]]
            [fluch.core-test]
            [fluch.color-test]))

(enable-console-print!)
(s/set-fn-validation! true)

(doo-tests 'fluch.core-test
           'fluch.color-test)
