(ns data-store.handler
  (:require [data-store.frame :refer [serialize]]))

(def delim "\r\n")
(defn handler [store input]
  (let [command (nth input 2 nil)]
    (println input)
    (cond
      (nil? command)
      [store nil]
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
      (= command "CONFIG")
      (do
        (println "in CONFIG")
        [store "*2\r\n$4\r\nsave\r\n$3\r\nyes\r\n*2\r\n$10\r\nappendonly\r\n$3\r\nyes\r\n"]
      )
        :else [store "-Error invalid command\r\n"])))
