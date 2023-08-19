(ns data-store.handler
  (:require [data-store.frame :refer [serialize unserialize]]))

(def delim "\r\n")
(defn handler [input]
  (println "the input is" input)
    (cond
      (= input "PING")
      "$4\r\nPONG\r\n"
      (= input "DOCS")
      "$-1\r\n"
      :else nil))
