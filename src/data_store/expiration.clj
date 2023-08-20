(ns data-store.expiration)

(defn expired [store time]
  (reduce
   (fn [acc [k v]]
     (if (and (:exp v) (>= (get v :exp) time))
       (conj acc k)
       acc))
   #{}
   store))

(defn has-exp [store]
  (reduce
   (fn [acc [k v]]
     (if (contains? v :exp)
       (into acc {k v})
       acc))
   {}
   store))

(defn expire-n [store time n]
  (let [keys-expired
        (loop [expired (expired (take n (has-exp store)) time)]
          (if
           (or (< (count expired) (* 0.25 n)) (= (count expired) (count store)))
            expired
            (recur (expired (take n (has-exp (apply dissoc store expired))) time))))]
    (apply dissoc store keys-expired)))
