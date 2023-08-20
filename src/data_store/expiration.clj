(ns data-store.expiration
  (:require [clojure.set :refer [union]]))

(defn expired [store time]
  (reduce
   (fn [acc [k v]]
     (if (and (:exp v) (>= (:exp v) time))
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
        (loop [iter-keys-expired (expired (take n (has-exp store)) time)]
          (if
           (or (< (count iter-keys-expired) (* 0.25 n)) (= (count iter-keys-expired) (count store)))
            iter-keys-expired
            (recur (union
                    iter-keys-expired
                    (expired (take n (has-exp (apply dissoc store iter-keys-expired))) time)))))]
    (apply dissoc store keys-expired)))
