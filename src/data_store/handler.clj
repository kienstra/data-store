(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]))

(def delim "\r\n")
(defn handler [store input]
  (println "top of handler")
  (println input)
  (let [command (first input)]
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
        :else [(into store {(nth input 4) (nth input 6)}) "+OK\r\n"])
      (= command "GET")
      (cond
        (not (nth input 4 nil))
        [store "-Error nothing to get\r\n"]
        (not (contains? store (nth input 4)))
        [store "$-1\r\n"]
        (not (string? (get store (nth input 4 nil))))
        [store "-Error not a string\r\n"]
        :else [store (str "+" (get store (nth input 4)) delim)])
      :else [store "-Error invalid command\r\n"])))
