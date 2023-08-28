(ns data-store.handler
  (:require [clojure.string :refer [lower-case]]
            [data-store.frame :refer [serialize]]))

(defmulti update-store (fn [command & _]
                         command))

(defmulti output (fn [command & _]
                   command))

(defn update-store-strategy [[command & args] time store]
  (update-store (keyword (lower-case command)) args time store))

(defn output-strategy [[command & args] time old-store new-store]
  (output (keyword (lower-case command)) args time old-store new-store))

(defmethod update-store :get [_ input time store]
  (let [store-key (nth input 0)]
    (cond
      (not store-key)
      store
      (not (contains? store store-key))
      store
      (not (string? (:val (get store store-key))))
      store
      :else (let [exp (:exp (get store store-key))
                  expired? (and exp (>= time exp))]
              (if
               expired?
                (dissoc store store-key)
                store)))))

(defmethod output :get [_ input time store _]
  (let [store-key (nth input 0)]
    (cond
      (not store-key)
      (serialize {:error "Error nothing to get"})
      (not (contains? store store-key))
      (serialize nil)
      (not (string? (:val (get store store-key))))
      (serialize {:error "Error not a string"})
      :else (let [exp (:exp (get store store-key))
                  expired? (and exp (>= time exp))]
              (if
               expired?
                (serialize nil)
                (serialize (:val (get store store-key))))))))

(defmethod update-store :set [_ input time store]
  (let [k (nth input 0)
        v (nth input 1)]
    (cond
      (or (not k) (not v))
      store
      (not (string? v))
      store
      :else (let [sub-command (nth input 2 nil)
                  sub-command-arg (nth input 3 nil)]
              (cond
                (and (= sub-command "EX") sub-command-arg)
                (into store {k {:val v :exp (+ time (* 1000 (Integer/parseInt sub-command-arg)))}})
                (and (= sub-command "PEX") sub-command-arg)
                (into store {k {:val v :exp (+ time (Integer/parseInt sub-command-arg))}})
                (and (= sub-command "EXAT") sub-command-arg)
                (into store {k {:val v :exp (* 1000 (Integer/parseInt sub-command-arg))}})
                (and (= sub-command "PXAT") sub-command-arg)
                (into store {k {:val v :exp (Integer/parseInt sub-command-arg)}})
                :else (into store {k {:val v}}))))))

(defmethod output :set [_ input _ _ _]
  (let [k (nth input 0)
        v (nth input 1)]
    (cond
      (or (not k) (not v))
      (serialize {:error "Error nothing to set"})
      (not (string? v))
      (serialize {:error "Error not a string"})
      :else (serialize "OK"))))

(defmethod update-store :expire [_ input time store]
  (let [store-key (nth input 0)
        exp-time (nth input 1)]
    (if (and
         store-key
         exp-time
         (contains? store store-key))
      (into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp-time)))})})
      store)))

(defmethod output :expire [_ input _ old-store _]
  (let [store-key (nth input 0)
        exp-time (nth input 1)]
    (if (and
         store-key
         exp-time
         (contains? old-store store-key))
      (serialize 1)
      (serialize 0))))

(defmethod output :echo [_ input _ _ _]
  (if-let [msg (nth input 0)]
    (serialize msg)
    (serialize {:error "Error nothing to echo"})))

(defmethod output :ping [_ input _ _ _]
  (if-let [msg (nth input 0)]
    (serialize (str "PONG " msg))
    (serialize "PONG")))

(defmethod output :exists [_ [& keys] _ store _]
  (if
   (nth keys 0)
    (->> keys
         (filter #(contains? store %))
         count
         serialize)
    (serialize {:error "Error nothing to check"})))

(defmethod update-store :delete [_ [& keys] _ store]
  (if
   (nth keys 0)
    (->> keys
         (filter #(contains? store %))
         (apply dissoc store))
    store))

(defmethod output :delete [_ [& keys] _ old-store _]
  (if
   (nth keys 0)
    (->> keys
         (filter #(contains? old-store %))
         count
         serialize)
    (serialize {:error "Error nothing to delete"})))

(defmethod update-store :incr [_ [key] _ store]
  (if key
    (let [new-val (inc (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defmethod output :incr [_ [key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (serialize {:error "Error nothing to increment"})))

(defmethod update-store :decr [_ [key] _ store]
  (if key
    (let [new-val (dec (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defmethod output :decr [_ [key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (serialize {:error "Error nothing to decrement"})))

(defmethod update-store :lpush [_ [key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj (reverse vals) prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defmethod output :lpush [_ [key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (serialize {:error "Error nothing to push"})))

(defmethod update-store :rpush [_ [key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj vals prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defmethod output :rpush [_ [key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (serialize {:error "Error nothing to push"})))

(defmethod output :default [command _ _ _ _]
  (serialize {:error (str "Error unknown command: " (name command))}))

(defmethod update-store :default [_ _ _ store]
  store)
