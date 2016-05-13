(ns fluch.test-runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [fluch.core-test]
              [fluch.color-test]))

(enable-console-print!)

(doo-tests 'fluch.core-test
           'fluch.color-test)
