(ns data-store.handler
  (:require [clojure.string :refer [join lower-case]]
            [data-store.frame :refer [delim serialize]]))

(defn command-store-get [input time store]
  (let [store-key (first input)]
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

(defn command-output-get [input time store _]
  (let [store-key (first input)]
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

(defn command-store-set [input time store]
  (let [k (first input)
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

(defn command-output-set [input _ _ _]
  (let [k (first input)
        v (second input)]
    (cond
      (or (not k) (not v))
      (str "-Error nothing to set" delim)
      (not (string? v))
      (str "-Error not a string" delim)
      :else (serialize "OK"))))

(defn command-store-expire [input time store]
  (let [store-key (first input)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? store store-key))
      (into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp-time)))})})
      store)))

(defn command-output-expire [input _ old-store _]
  (let [store-key (first input)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? old-store store-key))
      (serialize 1)
      (serialize 0))))

(defn command-output-echo [input _ _ _]
  (if-let [msg (first input)]
    (serialize msg)
    (str "-Error nothing to echo" delim)))

(defn command-output-ping [input _ _ _]
  (if-let [msg (first input)]
    (serialize (str "PONG" " " msg))
    (serialize "PONG")))

(defn command-output-exists [[& keys] _ store _]
  (if
   (first keys)
    [store (join (map
                  #(serialize (if (contains? store %) 1 0))
                  keys))]
    [store (str "-Error nothing to check" delim)]))

(defn command-store-delete [[& keys] _ store]
  (if
   (first keys)
    (let [existing-keys (filter #(contains? store %) keys)]
      (apply dissoc store existing-keys))
    store))

(defn command-output-delete [[& keys] _ old-store _]
  (if
   (first keys)
    (let [existing-keys (filter #(contains? old-store %) keys)]
      (serialize (count existing-keys)))
    (str "-Error nothing to delete" delim)))

(defn command-store-incr [[key] _ store]
  (if key
    (let [new-val (inc (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defn command-output-incr [[key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (str "-Error nothing to increment" delim)))

(defn command-store-decr [[key] _ store]
  (if key
    (let [new-val (dec (Integer. (get (get store key) :val 0)))]
      (into store {key (into (get store key {}) {:val (str new-val)})}))
    store))

(defn command-output-decr [[key] _ _ new-store]
  (if key
    (serialize (Integer. (get (get new-store key) :val 0)))
    (str "-Error nothing to decrement" delim)))

(defn command-store-lpush [[key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj (reverse vals) prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defn command-output-lpush [[key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (str "-Error nothing to push" delim)))

(defn command-store-rpush [[key & vals] _ store]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj vals prev-val))]
      (into store {key (into (get store key {}) {:val new-val})}))
    store))

(defn command-output-rpush [[key] _ _ new-store]
  (if key
    (serialize (count (get (get new-store key) :val [])))
    (str "-Error nothing to push" delim)))

(defn command-output-unknown [command ]
  (serialize {:error (str "Error unknown command: " command)}))

(defn command-store-unknown [_ _ store]
  store)

(defn store-handler-strategy [[command & args] time store]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-store-" (lower-case command))))]
    (dispatch-handler args time store)
    (command-store-unknown args time store)))

(defn output-handler-strategy [[command & args] time old-store new-store]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-output-" (lower-case command))))]
    (dispatch-handler args time old-store new-store)
    (command-output-unknown command)))
