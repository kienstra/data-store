(ns data-store.expiration
  (:require [clojure.set :refer [union]]))

(defn expired [time store]
  (reduce
   (fn [acc [k v]]
     (if (and (:exp v) (>= time (:exp v)))
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
        (loop [iter-keys-expired (->> store
                                      has-exp
                                      (take n)
                                      (expired time))]
          (if
           (or (<= (count iter-keys-expired) (* 0.25 n)) (= (count iter-keys-expired) (count store)))
            iter-keys-expired
            (recur (union
                    iter-keys-expired
                    (->> iter-keys-expired
                         (apply dissoc store)
                         has-exp
                         (take n)
                         (expired time))))))]
    (apply dissoc store keys-expired)))
