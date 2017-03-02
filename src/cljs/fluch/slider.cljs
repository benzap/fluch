(ns fluch.slider
  "Implementation of the slider data-structure as presented in
  https://github.com/mogenslund/liquid which appears similar to a gap
  buffer with extra magic."
  (:require [clojure.string :as str]
            [cljs.spec :as s]))

(defn lazyseq? [x] (instance? cljs.core/LazySeq x))
(defn list-or-lazy? [x] (or (lazyseq? x) (list? x)))
(defn string-or-keyword? [x] (or (string? x) (keyword? x)))
(defn regex? [x] (instance? js/RegExp x))

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

(defn right-until [slider cregex]
  (loop [s slider]
    (let [c (get-char s)]
      (if (or (end? s) (re-matches cregex c))
        s
        (recur (right s 1))))))

(s/fdef right-until
        :args (s/cat :slider ::slider :regex regex?)
        :ret ::slider)

(defn left-until [slider cregex]
  (loop [s (if (end? slider) (left slider 1) slider)]
    (let [c (get-char s)]
      (if (or (beginning? s) (re-matches cregex c))
        s
        (recur (left s 1))))))

(s/fdef left-until
        :args (s/cat :slider ::slider :regex regex?)
        :ret ::slider)

(defn forward-word [slider]
  (-> slider 
      (right-until #"\s")
      (right 1)
      (right-until #"\S")))

(s/fdef forward-word
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn end-of-line [slider]
  (right-until slider #"\n"))

(s/fdef end-of-line
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn beginning-of-line [slider]
  (loop [sl0 slider]
    (if (or (empty? (sl0 ::before)) (= (first (sl0 ::before)) "\n"))
      sl0
      (recur (left sl0 1)))))

(s/fdef beginning-of-line
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn forward-line 
  "If a column width is supplied, it will grab the first 'candidate'
  which is the last point that allows a full word to be displayed in
  the supplied column width."
  ([sl columns]
   (loop [s sl cand nil c 0]
     (cond (= (get-char s) "\n") (right s 1)
           (= c columns) (or cand s)
           (end? s) s
           :else (let [next (right s 1)]
                   (recur next
                          (if (= (get-char s) " ") next cand)
                          (inc c))))))
  ([sl]
    (-> sl (end-of-line) (right 1))))

(s/fdef forward-line
        :args (s/alt :column (s/cat :slider ::slider :column nat-int?)
                     :no-column (s/cat :slider ::slider))
        :ret ::slider)

(defn get-visual-column
  [slider columns]
  (let [pt (get-point slider)
        sl0 (beginning-of-line slider)]
    (loop [sl2 (forward-line sl0 columns) sl1 sl0]
      (cond 
        (or (> (get-point sl2) pt) (end? sl2))
        (- pt (get-point sl1))
        
        (= (get-point sl2) pt)
        0
            
        :else
        (recur (forward-line sl2 columns) sl2)))))

(s/fdef get-visual-column
        :args (s/cat :slider ::slider :cols nat-int?)
        :ret nat-int?)

(defn forward-visual-column
  [sl columns column]
  (let [cur-column (get-visual-column sl columns)]
    (loop [sl0 (-> sl (left cur-column) (forward-line columns))]
      (if (or (= (get-visual-column sl0 columns) column)
              (end? sl0)
              (= (get-char sl0) "\n"))
          sl0
          (recur (right sl0 1))))))

(s/fdef forward-visual-column
        :args (s/cat :slider ::slider :cols nat-int? :col nat-int?)
        :ret ::slider)

(defn backward-visual-column
  [sl columns column]
  (let [vc (get-visual-column sl columns)
        sl0 (left sl (+ vc 1))
        vc0 (get-visual-column sl0 columns)]
    (if (> vc0 column)
       (left sl0 (- vc0 column))
       sl0)))

(s/fdef backward-visual-column
        :args (s/cat :slider ::slider :cols nat-int? :col nat-int?)
        :ret ::slider)

(defn get-region
  [sl markname]
  (let [mark (get-mark sl markname)]
    (cond (nil? mark) nil
          (< mark (get-point sl)) (apply str (reverse
                                             (take (- (get-point sl) mark)
                                                  (sl ::before))))
          :else (apply str (take (- mark (get-point sl)) (sl ::after))))))

(s/fdef get-region
        :args (s/cat :slider ::slider :markname string-or-keyword?)
        :ret (s/nilable string?))

(defn delete-region
  [sl markname]
  (if-let [mark (get-mark sl markname)]
    (let [p0 (get-point sl)]
      (-> sl (set-point (max p0 mark))
             (delete (- (max p0 mark) (min p0 mark)))))
    sl))

(s/fdef delete-region
        :args (s/cat :slider ::slider :markname string-or-keyword?)
        :ret ::slider)

(defn delete-line
  [sl]
  (-> sl beginning-of-line
         (set-mark "deleteline")
         end-of-line
         (right 1)
         (delete-region "deleteline")))

(s/fdef delete-line
        :args (s/cat :slider ::slider)
        :ret ::slider)

(defn get-content
  "The full content of the slider"
  [sl]
  (apply str (-> sl beginning ::after)))

(s/fdef get-content
        :args (s/cat :slider ::slider)
        :ret string?)

(defn take-lines
  "Generate list of lines with at most columns chars. When exceeding
  end, empty lines will be provided."
  [sl rows columns]
  (map #(if (= (get-mark % "beginning") (get-point %))
          ""
          (get-region
            (if (= (get-char (left % 1)) "\n") (left % 1) %)
            "beginning"))
    (take rows (rest (iterate #(-> % (set-mark "beginning") (forward-line columns)) sl)))))

(s/fdef take-lines
        :args (s/cat :slider ::slider :rows nat-int? :cols nat-int?)
        :ret (s/coll-of string?))

(defn find-next
  "Moves the point to the next search match from the current point
  position."
  [sl search]
  (let [s (map str (seq (str/lower-case search)))
        len (count s)]
    (loop [sl0 (right sl 1)]
      (cond (= s (map str/lower-case (take len (sl0 ::after)))) sl0
            (end? sl0) sl
            :else (recur (right sl0 1))))))

(s/fdef find-next
        :args (s/cat :slider ::sldier :search string?)
        :ret ::slider)
