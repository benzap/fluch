(ns fluch.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]))

(deftest example-passing-test
  (is (= 1 1)))

(deftest example-failing-test
  (is (= 1 2)))
