(ns fluch.matrix)

(defn vempties 
  ([n d]
   (loop [i 0 v (transient [])]
     (if (< i n)
       (recur (inc i) (conj! v d))
       (persistent! v))))
  ([n] (vempties n nil)))

(defn create [m n]
  (vempties n (vempties m)))

(defn mset [m i j v]
  (assoc-in m [j i] v))

(let [s
      (-> (create 3 2)
          (mset 0 0 "b")
          (mset 1 0 "e")
          (mset 2 0 "n"))]
  (doseq [[j _v] (map-indexed vector s)]
    (doseq [[i v] (map-indexed vector _v)]
      (println [i j v]))))
