(ns data-store.handler
  (:require [clojure.string :refer [join split]]))

(defn handler [input]
  (let [spl (split input #" ")
        command (nth spl 0)
        args (rest spl)]
    (cond
      (= command "PING")
      "PONG"
      (= command "ECHO")
      (join " " args)
      :else nil)))
