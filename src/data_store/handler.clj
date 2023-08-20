(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]
            [clojure.string :refer [split]]))

(def delim "\r\n")
(defn handler [store input time]
  (let [command (nth input 2)]
    (cond
      (nil? command)
      [store "-Error no command \r\n"]
      (= command "PING")
      [store "$4\r\nPONG\r\n"]
      (= command "ECHO")
      (if (nth input 4 false)
        [store (str "+" (nth input 4) delim)]
        [store "-Error nothing to echo\r\n"])
      (= command "SET")
      (cond
        (not (nth input 6 nil))
        [store "-Error nothing to set\r\n"]
        (not (string? (nth input 6 nil)))
        [store "-Error not a string\r\n"]
        :else (let [sub-command (nth input 7 nil)
                    sub-command-arg (nth input 8 nil)]
                (if (and (= sub-command "EX") sub-command-arg)
                  [(into store {(nth input 4) {:val (nth input 6) :exp (+ time (* 1000 sub-command-arg))}}) "+OK\r\n"]
                  [(into store {(nth input 4) {:val (nth input 6)}}) "+OK\r\n"])))
      (= command "GET")
      (cond
        (not (nth input 4 nil))
        [store "-Error nothing to get\r\n"]
        (not (contains? store (nth input 4)))
        [store "$-1\r\n"]
        (not (string? (:val (get store (nth input 4 nil)))))
        [store "-Error not a string\r\n"]
        :else [store (str "+" (:val (get store (nth input 4))) delim)])
      :else [store "-Error invalid command\r\n"])))
