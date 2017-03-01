(ns fluch.slider
  "Implementation of the slider data-structure as presented in
  https://github.com/mogenslund/liquid which appears similar to a gap
  buffer with extra magic."
  (:require [cljs.spec :as s]))

(defn lazyseq? [x] (instance? cljs.core/LazySeq x))
(defn list-or-lazy? [x] (or (lazyseq? x) (list? x)))

(s/def ::before (s/coll-of char? :kind list-or-lazy? :into '()))
(s/def ::after (s/coll-of char? :kind list-or-lazy? :into '()))
(s/def ::point nat-int?)
(s/def ::linenumber nat-int?)
(s/def ::totallines nat-int?)
(s/def ::marks (s/map-of string? nat-int?))

(s/def ::slider 
  (s/keys :req-n [::before ::after ::point ::linenumber ::totallines ::marks]))

(defn create
  ([text]
   (let [after (if (string? text) (map str text) text)]
     {::before '()
      ::after after
      ::point 0
      ::linenumber 1
      ::totallines (inc (count (filter #(= % "\n") after)))
      ::marks {}}))
  ([] (create '())))

(s/fdef create :ret ::slider)

(defn clear [slider] (create))

(s/fdef clear :ret ::slider)

(defn beginning [slider]
  (assoc slider
         ::before '()
         ::after (concat (reverse (::before slider)) (::after slider))
         ::point 0
         ::linenumber 1))

(s/fdef beginning :ret ::slider)

(defn end [slider]
  (let [temp-content (concat (reverse (::after slider)) (::before slider))]
    (assoc slider
           ::before temp-content
           ::after '()
           ::point (count temp-content)
           ::linenumber (::totallines slider))))

