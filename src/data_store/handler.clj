(ns data-store.handler
  (:require [clojure.string :refer [join lower-case]]
            [data-store.frame :refer [serialize]]))

(def delim "\r\n")

(defn command-store-get [input store time]
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

(defn command-output-get [input store time]
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

(defn command-store-set [input store time]
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

(defn command-output-set [input _ _]
  (let [k (first input)
        v (second input)]
    (cond
      (or (not k) (not v))
      (str "-Error nothing to set" delim)
      (not (string? v))
      (str "-Error not a string" delim)
      :else (serialize "OK"))))

(defn command-expire [input store time]
  (let [store-key (first input)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? store store-key))
      [(into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp-time)))})})
       (serialize 1)]
      [store (serialize 0)])))

(defn command-echo [input store _]
  (if-let [msg (first input)]
    [store (serialize msg)]
    [store (str "-Error nothing to echo" delim)]))

(defn command-ping [input store _]
  (if-let [msg (first input)]
    [store (serialize (str "PONG" " " msg))]
    [store (serialize "PONG")]))

(defn command-exists [[& keys] store _]
  (if
   (first keys)
    [store (join (map
                  #(serialize (if (contains? store %) 1 0))
                  keys))]
    [store (str "-Error nothing to check" delim)]))

(defn command-delete [[& keys] store _]
  (if
   (first keys)
    (let [existing-keys (filter #(contains? store %) keys)]
      [(apply dissoc store existing-keys) (serialize (count existing-keys))])
    [store (str "-Error nothing to delete" delim)]))

(defn command-incr [[key] store _]
  (if key
    (let [new-val (inc (Integer. (get (get store key) :val 0)))]
      [(into store {key (into (get store key {}) {:val (str new-val)})}) (serialize new-val)])
    [store (str "-Error nothing to increment" delim)]))

(defn command-decr [[key] store _]
  (if key
    (let [new-val (dec (Integer. (get (get store key) :val 0)))]
      [(into store {key (into (get store key {}) {:val (str new-val)})}) (serialize new-val)])
    [store (str "-Error nothing to decrement" delim)]))

(defn command-lpush [[key & vals] store _]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj (reverse vals) prev-val))]
      [(into store {key (into (get store key {}) {:val new-val})}) (serialize (count new-val))])
    [store (str "-Error nothing to push" delim)]))

(defn command-rpush [[key & vals] store _]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (apply conj vals prev-val))]
      [(into store {key (into (get store key {}) {:val new-val})}) (serialize (count new-val))])
    [store (str "-Error nothing to push" delim)]))

(defn command-output-unknown []
  (serialize "OK"))

(defn command-store-unknown [_ store _]
  store)

(defn store-handler [command & args]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-store-" command)))]
    (apply dispatch-handler args)
    (apply command-store-unknown args)))

(defn store-handler-adapter [store input time]
  (store-handler (lower-case (first input)) (rest input) store time))

(defn output-handler [command & args]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-output-" command)))]
    (apply dispatch-handler args)
    (command-output-unknown)))

(defn output-handler-adapter [store input time]
  (output-handler (lower-case (first input)) (rest input) store time))
