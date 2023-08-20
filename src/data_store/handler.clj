(ns data-store.handler
  (:require [clojure.string :refer [lower-case]]))

(def delim "\r\n")

(defn command-get [input store time]
  (let [store-key (nth input 4 nil)]
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
                [store (str "+" (:val (get store (nth input 4))) delim)])))))

(defn command-set [input store time]
  (cond
    (not (nth input 6 nil))
    [store (str "-Error nothing to set" delim)]
    (not (string? (nth input 6 nil)))
    [store (str "-Error not a string" delim)]
    :else (let [sub-command (nth input 8 nil)
                sub-command-arg (nth input 10 nil)]
            (cond
              (and (= sub-command "EX") sub-command-arg)
              [(into store {(nth input 4) {:val (nth input 6) :exp (+ time (* 1000 (Integer/parseInt sub-command-arg)))}}) (str "+OK" delim)]
              (and (= sub-command "PEX") sub-command-arg)
              [(into store {(nth input 4) {:val (nth input 6) :exp (+ time (Integer/parseInt sub-command-arg))}}) (str "+OK" delim)]
              (and (= sub-command "EXAT") sub-command-arg)
              [(into store {(nth input 4) {:val (nth input 6) :exp (* 1000 (Integer/parseInt sub-command-arg))}}) (str "+OK" delim)]
              (and (= sub-command "PXAT") sub-command-arg)
              [(into store {(nth input 4) {:val (nth input 6) :exp (Integer/parseInt sub-command-arg)}}) (str "+OK" delim)]
              :else [(into store {(nth input 4) {:val (nth input 6)}}) (str "+OK" delim)]))))

(defn command-expire [input store time]
  (let [store-key (nth input 4 nil)
        exp (nth input 6 nil)]
    (if (and
         store-key
         exp
         (contains? store store-key))
      [(into store {store-key (into (get store store-key) {:exp (+ time (* 1000 (Integer/parseInt exp)))})})
       (str ":1" delim)]
      [store (str ":0" delim)])))

(defn command-echo [input store _]
  (if (nth input 4 false)
    [store (str "+" (nth input 4) delim)]
    [store (str "-Error nothing to echo" delim)]))

(defn command-ping [_ store _]
  [store (str "$4" delim "PONG" delim)])

(defn command-unknown [_ store _]
  [store (str "+OK" delim)])

(defn command-handler [command & args]
  (if-let [dispatch-handler (ns-resolve 'data-store.handler (symbol (str "command-" command)))]
    (apply dispatch-handler args)
    (apply command-unknown args)))

(defn handler [store input time]
  (if-let [command (lower-case (nth input 2))]
    (command-handler command input store time)
    [store (str "-Error no command" delim)]))
