(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]))

(def delim "\r\n")
(defn handler [store input]
  (println input)
  (cond
    (= input "DOCS")
    [store "$-1\r\n"]
    (= input "PING")
    [store "$4\r\nPONG\r\n"]
    :else [store nil]))
