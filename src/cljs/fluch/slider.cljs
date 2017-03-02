(ns fluch.slider
  "Implementation of the slider data-structure as presented in
  https://github.com/mogenslund/liquid which appears similar to a gap
  buffer with extra magic."
  (:require [cljs.spec :as s]))

(defn lazyseq? [x] (instance? cljs.core/LazySeq x))
(defn list-or-lazy? [x] (or (lazyseq? x) (list? x)))
(defn string-or-keyword? [x] (or (string? x) (keyword? x)))

(s/def ::before (s/coll-of char? :kind list-or-lazy? :into '()))
(s/def ::after (s/coll-of char? :kind list-or-lazy? :into '()))
(s/def ::point nat-int?)
(s/def ::linenumber nat-int?)
(s/def ::totallines nat-int?)
(s/def ::marks (s/map-of string-or-keyword? nat-int?))

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

(s/fdef create
        :args (s/alt :text (s/coll-of char? :kind list-or-lazy? :into '())
                     :empty nil)
        :ret ::slider)

(defn clear [slider] (create))

(s/fdef clear
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn beginning [slider]
  (assoc slider
         ::before '()
         ::after (concat (reverse (::before slider)) (::after slider))
         ::point 0
         ::linenumber 1))

(s/fdef beginning 
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn end [slider]
  (let [temp-content (concat (reverse (::after slider)) (::before slider))]
    (assoc slider
           ::before temp-content
           ::after '()
           ::point (count temp-content)
           ::linenumber (::totallines slider))))

(s/fdef end
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn get-char [slider]
  (first (::after slider)))

(s/fdef get-char
        :args (s/cat :slider ::slider)
        :ret char?)

(defn get-point [slider]
  (::point slider))

(s/fdef get-point
        :args (s/cat :slider ::slider)
        :ret ::point)

(defn get-linenumber [slider]
  (::linenumber slider))

(s/fdef get-linenumber
        :args (s/cat :slider ::slider)
        :ret ::linenumber)

(defn slide-marks [marks point amount]
  (let [ks (keys (select-keys marks (for [[k v] marks :when (> v point)] k)))]
    (reduce #(assoc %1 %2 (max point (+ (%1 %2) amount))) marks ks)))

(s/fdef slide-marks
        :args (s/cat :marks ::marks :point ::point :amount nat-int?)
        :ret ::marks)

(defn left [slider amount]
  (let [tmp (take amount (::before slider))
        n (count tmp)
        linecount (count (filter #(= % "\n") tmp))]
    (assoc slider
           ::before (drop amount (::before slider))
           ::after (concat (reverse tmp) (::after slider))
           ::point (- (::point slider) n)
           ::linenumber (- (::linenumber slider) linecount))))

(s/fdef left
        :args (s/cat :slider ::slider :amount nat-int?)
        :ret ::slider)

(defn right [slider amount]
  (let [tmp (take amount (::after slider))
        n (count tmp)
        linecount (count (filter #(= % "\n") tmp))]
    (assoc slider
           ::before (concat (reverse tmp) (::before slider))
           ::after (drop n (::after slider))
           ::point (+ (::point slider) n)
           ::linenumber (+ (::linenumber slider) linecount))))

(s/fdef right
        :args (s/cat :slider ::slider :amount nat-int?)
        :ret ::slider)

(defn set-point [slider newpoint]
  (cond
    (> newpoint (::point slider))
    (right slider (- newpoint (::point slider)))
    (< newpoint (::point slider))
    (left slider (- (::point slider) newpoint))
    :else slider))

(s/fdef set-point
        :args (s/cat :slider ::slider
                     :newpoint ::point)
        :ret ::slider)

(defn beginning? [slider]
  (empty? (::before slider)))

(s/fdef beginning?
        :args (s/cat :slider ::slider)
        :ret boolean?)

(defn end? [slider]
  (empty? (::after slider)))

(s/fdef end?
        :args (s/cat :slider ::slider)
        :ret boolean?)

(defn insert [slider text]
  (let [n (count text)
        linecount (count (filter #(= % "\n") text))]
    (assoc slider
           ::before (concat (reverse text) (::before slider))
           ::point (+ (::point slider) n)
           ::linenumber (+ (::linenumber slider) linecount)
           ::totallines (+ (::totallines slider) linecount)
           ::marks (slide-marks (::marks slider) (+ (::point slider) n -1) n))))

(s/fdef insert
        :args (s/cat :slider ::slider :text string?)
        :ret ::slider)

(defn delete [slider amount]
  (let [tmp (take amount (::before slider))
        linecount (count (filter #(= % "\n") tmp))
        n (count tmp)]
    (assoc slider
           ::before (drop n (::before slider))
           ::point (- (::point slider) n)
           ::linenumber (- (::linenumber slider) linecount)
           ::totallines (- (::totallines slider) linecount)
           ::marks (slide-marks (::marks slider) (- (::point slider) n) n))))

(s/fdef delete
        :args (s/cat :slider ::slider :amount nat-int?)
        :ret ::slider)

(defn set-mark [slider name]
  (assoc-in slider [::marks name] (::point slider)))

(s/fdef set-mark
        :args (s/cat :slider ::slider :name string-or-keyword?)
        :ret ::slider)

(defn get-mark [slider name]
  (get-in slider [::marks name] nil))

(s/fdef get-mark
        :args (s/cat :slider ::slider :name string-or-keyword?)
        :ret ::point)

(defn remove-mark [slider name]
  (update-in slider [::marks] dissoc name))

(s/fdef remove-mark
        :args (s/cat :slider ::slider :name string-or-keyword?)
        :ret ::slider)

(defn clear-marks [slider]
  (assoc slider ::marks {}))

(s/fdef clear-marks
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn point-to-mark [slider name]
  (if (get-mark slider name)
    (set-point slider (get-mark slider name))
    slider))

(s/fdef point-to-mark
        :args (s/cat :slider ::slider :name string-or-keyword?)
        :ret ::slider)
