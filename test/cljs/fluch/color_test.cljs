(ns fluch.color-test
  (:require-macros [cljs.test :refer (is are deftest testing)])
  (:require [cljs.test]
            [fluch.color :as color]))

(deftest byte->unit-interval-test
  (are [x y] (= (color/byte->unit-interval x) y)
    0 0.0
    255 1.0
    ))

(deftest unit-interval->byte-test
  (are [x y] (= (color/unit-interval->byte x) y)
    0.0 0
    1.0 255
    0.5 128
    ))

(deftest hex->term-test
  (are [x y] (= (color/hex->term x) y)
    "#FFFFFF" [255 255 255 255]
    "#ffffff" [255 255 255 255]
    "#FFF" [255 255 255 255]
    "#fff" [255 255 255 255]
    "#FF00FF" [255 0 255 255]
    "#F0F" [255 0 255 255]
    ))

(deftest term->hex-test
  (are [x y] (= (color/term->hex x) y)
    [255 255 255 255] "#ffffff"
    [0 0 0 255] "#000000"
    [255 0 255 0] "#ff00ff"
    ))

(deftest rgba->term-test
  (are [x y] (= (color/rgba->term x) y)
    "rgba(255,255,255,1.0)" [255 255 255 255]
    "rgba(255, 255, 255, 1.0)" [255 255 255 255]
    "rgba(128, 128, 128, 0.5)" [128, 128, 128, 128]
    ))

(deftest term->rgba-test
  (are [x y] (= (color/term->rgba x) y)
    [255 255 255 255] "rgba(255,255,255,1)"
    [128 0 255 0] "rgba(128,0,255,0)"
    ))

(deftest rgb->term-test
  (are [x y] (= (color/rgb->term x) y)
    "rgb(255,255,255)" [255 255 255 255]
    "rgb(255, 255, 255)" [255 255 255 255]
    "rgb(255, 128, 0)" [255 128 0 255]
    ))

(deftest term->rgb-test
  (are [x y] (= (color/term->rgb x) y)
    [255 255 255 255] "rgb(255,255,255)"
    [128 128 128 0] "rgb(128,128,128)"
    [0 0 0 255] "rgb(0,0,0)"
    ))
