(ns data-store.handler
  (:require [clojure.string :refer [join lower-case]]
            [data-store.frame :refer [serialize]]))

(def delim "\r\n")

(defn command-get [input store time]
  (let [store-key (first input)]
    (cond
      (not store-key)
      [store (str "-Error nothing to get" delim)]
      (not (contains? store store-key))
      [store (serialize nil)]
      (not (string? (:val (get store store-key))))
      [store (str "-Error not a string" delim)]
      :else (let [exp (:exp (get store store-key))
                  expired? (and exp (>= time exp))]
              (if
               expired?
                [(dissoc store store-key) (serialize nil)]
                [store (serialize (:val (get store store-key)))])))))

(defn command-set [input store time]
  (let [k (first input)
        v (second input)]
    (cond
      (or (not k) (not v))
      [store (str "-Error nothing to set" delim)]
      (not (string? v))
      [store (str "-Error not a string" delim)]
      :else (let [sub-command (nth input 2 nil)
                  sub-command-arg (nth input 3 nil)]
              (cond
                (and (= sub-command "EX") sub-command-arg)
                [(into store {k {:val v :exp (+ time (* 1000 (Integer/parseInt sub-command-arg)))}}) (serialize "OK")]
                (and (= sub-command "PEX") sub-command-arg)
                [(into store {k {:val v :exp (+ time (Integer/parseInt sub-command-arg))}}) (serialize "OK")]
                (and (= sub-command "EXAT") sub-command-arg)
                [(into store {k {:val v :exp (* 1000 (Integer/parseInt sub-command-arg))}}) (serialize "OK")]
                (and (= sub-command "PXAT") sub-command-arg)
                [(into store {k {:val v :exp (Integer/parseInt sub-command-arg)}}) (serialize "OK")]
                :else [(into store {k {:val v}}) (serialize "OK")])))))

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
          new-val (vec (concat (reverse vals) prev-val))]
      [(into store {key (into (get store key {}) {:val new-val})}) (serialize (count new-val))])
    [store (str "-Error nothing to push" delim)]))

(defn command-rpush [[key & vals] store _]
  (if key
    (let [prev-val (get (get store key) :val [])
          new-val (vec (concat vals prev-val))]
      [(into store {key (into (get store key {}) {:val new-val})}) (serialize (count new-val))])
    [store (str "-Error nothing to push" delim)]))

(defn command-unknown [_ store _]
  [store (serialize "OK")])

(defn command-handler [command & args]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-" command)))]
    (apply dispatch-handler args)
    (apply command-unknown args)))

(defn handler [store input time]
  (if-let [command (lower-case (first input))]
    (command-handler command (rest input) store time)
    [store (str "-Error no command" delim)]))
