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

(dc/deftest slider-tests

  (t/testing "slider/create"
    (is-conformed ::slider/slider (slider/create))
    (is-conformed ::slider/slider (slider/create "test")))

  (t/testing "slider/clear"
    (is-conformed ::slider/slider (-> (slider/create "test") slider/clear))
    (t/is (let [sl (slider/create "test")
                sl0 (slider/clear sl)]
            (empty? (sl0 ::slider/after)))))

  (t/testing "slider/beginning"
    (is-conformed
     ::slider/slider
     (-> (slider/create) slider/beginning)))

  (t/testing "slider/end"
    (is-conformed 
     ::slider/slider
     (-> (slider/create)
         slider/end)))

  (t/testing "slider/get-char"
    (is-conformed
     char?
     (-> (slider/create "test")
         slider/get-char))

    (t/is (let [sl (slider/create "test")]
            (= (slider/get-char sl) "t"))))

  (t/testing "slider/left"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/left 2)))

    (t/is (let [sl (-> (slider/create "test")
                     (slider/end)
                     (slider/left 2))]
          (= (slider/get-char sl) "s"))))

  (t/testing "slider/right"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/right 2)))

    (t/is (let [sl (-> (slider/create "test")
                     (slider/right 2))]
          (= (slider/get-char sl) "s"))))  

  (t/testing "slider/set-point"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/set-point 2)))

    (t/is (let [sl (-> (slider/create "test")
                       (slider/set-point 2))]
            (= (slider/get-char sl) "s"))))

  (t/testing "slider/insert"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/insert "ing")))
    (t/is (let [sl (-> (slider/create "test")
                       (slider/insert "ing")
                       (slider/left 3))]
            (= (slider/get-char sl) "i"))))
  
  (t/testing "slider/delete"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/end)
         (slider/delete 2)))
    (t/is (let [sl (-> (slider/create "test")
                       (slider/end)
                       (slider/delete 2)
                       (slider/left 1))]
            (= (slider/get-char sl) "e"))))

  (t/testing "slider/set-mark slider/get-mark slider/remove-mark slider/clear-marks"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/set-mark :test)))

    (is-conformed
     ::slider/point
     (-> (slider/create "test")
         (slider/set-point 2)
         (slider/set-mark :test)
         (slider/get-mark :test)))

    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/set-mark :test)
         (slider/remove-mark :test)))

    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/set-mark :test)
         (slider/clear-marks))))

  (t/testing "slider/right-until"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/right-until #"s")))

    (t/is (let [sl (-> (slider/create "test")
                     (slider/right-until #"s"))]
          (= (slider/get-char sl) "s"))))
 
  (t/testing "slider/left-until"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test")
         (slider/end)
         (slider/left-until #"s")))

    (t/is (let [sl (-> (slider/create "test")
                       (slider/end)
                       (slider/left-until #"s"))]
            (= (slider/get-char sl) "s"))))

  (t/testing "slider/forward-word"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test good")
         (slider/forward-word)))

    (t/is (let [sl (-> (slider/create "test good")
                     (slider/forward-word))]
          (= (slider/get-char sl) "g"))))
 
  (t/testing "slider/end-of-line"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test\ngood")
         (slider/end-of-line)))
    
    (t/is (let [sl (-> (slider/create "test\ngood")
                       (slider/end-of-line))]
            (= (slider/get-char sl) "\n"))))

  (t/testing "slider/beginning-of-line"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test\ngood")
         (slider/end)
         (slider/beginning-of-line)))
    
    (t/is (let [sl (-> (slider/create "test\ngood")
                       (slider/end)
                       (slider/beginning-of-line))]
            (= (slider/get-char sl) "g"))))

  (t/testing "slider/forward-line"
    (is-conformed
     ::slider/slider
     (-> (slider/create "test\ngood")
         (slider/forward-line)))
    
    (t/is (let [sl (-> (slider/create "test\ngood")
                       (slider/forward-line))]
            (= (slider/get-char sl) "g"))))
)
