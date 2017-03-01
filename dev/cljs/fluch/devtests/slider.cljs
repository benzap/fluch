(ns fluch.devtests.slider
  (:require-macros [devcards.core :refer [defcard]])
  (:require [cljs.spec :as s]
            [cljs.spec.test :as st :refer [instrument]]
            [cljs.test :as t :refer [] :include-macros true]
            [devcards.core :as dc :include-macros true]
            [fluch.devtests.utils :as utils :include-macros true :refer-macros [is-conformed]]
            [fluch.slider :as slider]))

(instrument 'fluch.slider.clear
            'fluch.slider.create)

(dc/deftest slider-create
  (is-conformed ::slider/slider (slider/create))
  (is-conformed ::slider/slider (slider/create "test")))

(dc/deftest slider-clear
  (is-conformed ::slider/slider (-> (slider/create "test") slider/clear)))

(dc/deftest slider-beginning
    (is-conformed
     ::slider/slider
     (-> (slider/create) slider/beginning)))

(dc/deftest slider-end
  (is-conformed 
   ::slider/slider
   (-> (slider/create)
       slider/end)))

