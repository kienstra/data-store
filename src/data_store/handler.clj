(ns data-store.handler
  (:require [clojure.string :refer [join lower-case]]))

(def delim "\r\n")

(defn command-get [input store time]
  (let [store-key (first input)]
    (cond
      (not store-key)
      [store (str "-Error nothing to get" delim)]
      (not (contains? store store-key))
      [store (str "$-1" delim)]
      (not (string? (:val (get store store-key))))
      [store (str "-Error not a string" delim)]
      :else (let [exp (:exp (get store store-key))
                  expired? (and exp (>= time exp))]
              (if
               expired?
                [(dissoc store store-key) (str "$-1" delim)]
                [store (str "+" (:val (get store store-key)) delim)])))))

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
                [(into store {k {:val v :exp (+ time (* 1000 (Integer/parseInt sub-command-arg)))}}) (str "+OK" delim)]
                (and (= sub-command "PEX") sub-command-arg)
                [(into store {k {:val v :exp (+ time (Integer/parseInt sub-command-arg))}}) (str "+OK" delim)]
                (and (= sub-command "EXAT") sub-command-arg)
                [(into store {k {:val v :exp (* 1000 (Integer/parseInt sub-command-arg))}}) (str "+OK" delim)]
                (and (= sub-command "PXAT") sub-command-arg)
                [(into store {k {:val v :exp (Integer/parseInt sub-command-arg)}}) (str "+OK" delim)]
                :else [(into store {k {:val v}}) (str "+OK" delim)])))))

(defn command-expire [input store time]
  (let [store-key (first input)
        exp-time (second input)]
    (if (and
         store-key
         exp-time
         (contains? store store-key))
      [(into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp-time)))})})
       (str ":1" delim)]
      [store (str ":0" delim)])))

(defn command-echo [input store _]
  (if-let [msg (first input)]
    [store (str "+" msg delim)]
    [store (str "-Error nothing to echo" delim)]))

(defn command-ping [input store _]
  (if-let [msg (first input)]
    [store (str "+PONG" " " msg delim)]
    [store (str "+PONG" delim)]))

(defn command-exists [[& keys] store _]
  (if
   (first keys)
    [store (join (map
                  #(str ":" (if (contains? store %) "1" "0") delim)
                  keys))]
    [store (str "-Error nothing to check" delim)]))

(defn command-unknown [_ store _]
  [store (str "+OK" delim)])

(defn command-handler [command & args]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-" command)))]
    (apply dispatch-handler args)
    (apply command-unknown args)))

(defn handler [store input time]
  (if-let [command (lower-case (first input))]
    (command-handler command (rest input) store time)
    [store (str "-Error no command" delim)]))
