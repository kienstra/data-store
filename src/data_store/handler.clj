(ns data-store.handler
  (:require [clojure.string :refer [upper-case]]))

(def delim "\r\n")

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
              :else [(into store {(nth input 4) {:val (nth input 6)}}) (str "+OK" delim)]))))

(defn command-get [input store time]
  (cond
    (not (nth input 4 nil))
    [store (str "-Error nothing to get" delim)]
    (not (contains? store (nth input 4)))
    [store (str "$-1" delim)]
    (not (string? (:val (get store (nth input 4 nil)))))
    [store (str "-Error not a string" delim)]
    :else (let [exp (:exp (get store (nth input 4)))
                expired? (and exp (> time exp))]
            (if
             expired?
              [store (str "$-1" delim)]
              [store (str "+" (:val (get store (nth input 4))) delim)]))))

(defn command-echo [input store]
  (if (nth input 4 false)
    [store (str "+" (nth input 4) delim)]
    [store (str "-Error nothing to echo" delim)]))

(defn command-ping [store]
  [store (str "$4" delim "PONG" delim)])

(defn handler [store input time]
  (if (nil? (nth input 2))
    [store (str "-Error no command" delim)]
    (let [command (upper-case (nth input 2))]
      (cond
        (= command "PING")
        (command-ping store)
        (= command "ECHO")
        (command-echo input store)
        (= command "GET")
        (command-get input store time)
        (= command "SET")
        (command-set input store time)
        :else [store (str "+OK" delim)]))))
