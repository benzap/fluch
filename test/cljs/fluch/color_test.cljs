(ns fluch.color-test
  (:require-macros [cljs.test :refer (is deftest testing)])
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
  (is (= (color/hex->term "#FFFFFF") [255 255 255 255]))
  (is (= (color/hex->term "#ffffff") [255 255 255 255]))
  (is (= (color/hex->term "#FFF") [255 255 255 255]))
  (is (= (color/hex->term "#fff") [255 255 255 255]))

  (is (= (color/hex->term "#FF00FF") [255 0 255 255]))
  (is (= (color/hex->term "#F0F") [255 0 255 255]))

  )
