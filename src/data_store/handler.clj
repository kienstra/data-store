(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]))

(defn handler [store input]
  (let [command (nth input 2 nil)]
    (cond
      (nil? command)
      [store "-Error no command \r\n"]
      (= command "PING")
      [store "$4\r\nPONG\r\n"]
      (= command "ECHO")
      (if (nth input 4 false)
        [store (str "+" (nth input 4) "\r\n")]
        [store "-Error nothing to echo\r\n"])
      (= command "SET")
      (if
       (nth input 6 false)
        [(into store {(nth input 4) (unserialize (nth input 6))}) "+OK\r\n"]
        [store "-Error nothing to set\r\n"])
      (= command "GET")
      (if
       (nth input 4 false)
        [store (serialize (get store (nth input 4)))]
        [store "-Error nothing to get\r\n"])
      :else [store "-Error invalid command\r\n"])))
