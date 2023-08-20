(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]
            [clojure.string :refer [split]]))

(def delim "\r\n")
(defn handler [store input]
  (let [parsed (split input #"\r\n")
        command (nth parsed 2)]
    (cond
      (nil? command)
      [store "-Error no command \r\n"]
      (= command "PING")
      [store "$4\r\nPONG\r\n"]
      (= command "ECHO")
      (if (nth parsed 4 false)
        [store (str "+" (nth parsed 4) delim)]
        [store "-Error nothing to echo\r\n"])
      (= command "SET")
      (cond
        (not (nth parsed 6 nil))
        [store "-Error nothing to set\r\n"]
        (not (string? (nth parsed 6 nil)))
        [store "-Error not a string\r\n"]
        :else [(into store {(nth parsed 4) (nth parsed 6)}) "+OK\r\n"])
      (= command "GET")
      (cond
        (not (nth parsed 4 nil))
        [store "-Error nothing to get\r\n"]
        (not (contains? store (nth parsed 4)))
        [store "$-1\r\n"]
        (not (string? (get store (nth parsed 4 nil))))
        [store "-Error not a string\r\n"]
        :else [store (str "+" (get store (nth parsed 4)) delim)])
      :else [store "-Error invalid command\r\n"])))
