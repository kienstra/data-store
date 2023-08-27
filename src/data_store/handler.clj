(ns data-store.handler
  (:require [clojure.string :refer [join lower-case]]
            [data-store.frame :refer [delim serialize]]))

(defmulti command-store (fn [command & _]
                          command))

(defmulti command-output (fn [command & _]
                           command))

(defmethod command-store :get [_ input time store]
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

(defmethod command-output :get [_ input time store _]
  (let [store-key (nth input 0)]
    (cond
      (not store-key)
      (str "-Error nothing to get" delim)
      (not (contains? store store-key))
      (serialize nil)
      (not (string? (:val (get store store-key))))
      (str "-Error not a string" delim)
      :else (let [exp (:exp (get store store-key))
                  expired? (and exp (>= time exp))]
              (if
               expired?
                (serialize nil)
                (serialize (:val (get store store-key))))))))

(defmethod command-store :set [_ input time store]
  (let [k (nth input 0)
        v (second input)]
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

(defmethod command-output :set [_ input _ _ _]
  (let [k (nth input 0)
        v (second input)]
    (cond
      (or (not k) (not v))
      (str "-Error nothing to set" delim)
      (not (string? v))
      (str "-Error not a string" delim)
      :else (serialize "OK"))))

(defmethod command-store :expire [_ input time store]
  (let [store-key (nth input 0)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? store store-key))
      (into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp-time)))})})
      store)))

(defmethod command-output :expire [_ input _ old-store _]
  (let [store-key (nth input 0)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? old-store store-key))
      (serialize 1)
      (serialize 0))))

(defmethod command-output :echo [_ input _ _ _]
  (if-let [msg (nth input 0)]
    (serialize msg)
    (str "-Error nothing to echo" delim)))

(defmethod command-output :ping [_ input _ _ _]
  (if-let [msg (nth input 0)]
    (serialize (str "PONG" " " msg))
    (serialize "PONG")))

(defmethod command-output :exists [_ [& keys] _ store _]
  (if
   (nth keys 0)
    [store (join (map
                  #(serialize (if (contains? store %) 1 0))
                  keys))]
    [store (str "-Error nothing to check" delim)]))

(defmethod command-store :delete [_ [& keys] _ store]
  (if
   (nth keys 0)
    (let [existing-keys (filter #(contains? store %) keys)]
      (apply dissoc store existing-keys))
    store))

(defmethod command-output :delete [_ [& keys] _ old-store _]
  (if
   (nth keys 0)
    (let [existing-keys (filter #(contains? old-store %) keys)]
      (serialize (count existing-keys)))
    (str "-Error nothing to delete" delim)))

(defmethod command-store :incr [_ [key] _ store]
  (if key
    (let [new-val (inc (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defmethod command-output :incr [_ [key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (str "-Error nothing to increment" delim)))

(defmethod command-store :decr [_ [key] _ store]
  (if key
    (let [new-val (dec (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defmethod command-output :decr [_ [key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (str "-Error nothing to decrement" delim)))

(defmethod command-store :lpush [_ [key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj (reverse vals) prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defmethod command-output :lpush [_ [key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (str "-Error nothing to push" delim)))

(defmethod command-store :rpush [_ [key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj vals prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defmethod command-output :rpush [_ [key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (str "-Error nothing to push" delim)))

(defmethod command-output :default [command _ _ _ _]
  (serialize {:error (str "Error unknown command: " (name command))}))

(defmethod command-store :default [_ _ _ store]
  store)

(defn store-handler-strategy [[command & args] time store]
  (command-store (keyword (lower-case command)) args time store))

(defn output-handler-strategy [[command & args] time old-store new-store]
  (command-output (keyword (lower-case command)) args time old-store new-store))
