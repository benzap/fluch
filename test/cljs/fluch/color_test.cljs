(ns fluch.color-test
  (:require-macros [cljs.test :refer (is are deftest testing)])
  (:require [cljs.test]
            [fluch.color :as color]))

(deftest byte->unit-interval-test
  (let [byte 255]
    (is (= (color/byte->unit-interval byte) 1.0))
    )

  (let [byte 0]
    (is (= (color/byte->unit-interval byte) 0.0))
    )
)

(deftest unit-interval->byte-test
  (let [interval 0.0]
    (is (= (color/unit-interval->byte interval) 0))
    )

  (let [interval 1.0]
    (is (= (color/unit-interval->byte interval) 255))
    )
  )

(deftest hex->term-test
  (are [x y] (= (color/hex->term x) y)
    "#FFFFFF" [255 255 255 255]
    "#ffffff" [255 255 255 255]
    "#FFF" [255 255 255 255]
    "#fff" [255 255 255 255]
    "#FF00FF" [255 0 255 255]
    "#F0F" [255 0 255 255]
    ))
