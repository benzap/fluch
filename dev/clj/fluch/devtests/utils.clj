(ns fluch.devtests.utils)

(defmacro is-conformed 
  [spec test]
  `(cljs.test/is 
    (not= 
     (cljs.spec/conform ~spec ~test)
     ::cljs.spec/invalid)))

#_(macroexpand-1 '(is-conformed ::spec 12))
