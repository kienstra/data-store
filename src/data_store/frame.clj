(ns data-store.frame
  (:require [clojure.string :refer [index-of]]))

(defn buffer->bytes [b i]
  (if (Character/isDigit (get b i))
    (recur b (+ i 1))
    (Integer/parseInt (subs b 0 i))))

(defn buffer->frame [b]
  (let [first-char (subs b 0 1)
        delim (.indexOf b "\r\n")]
    (cond
      (or (= delim -1) (= b "$-1\r\n"))
      nil
      (= first-char "+")
      (subs b 1 delim)
      (= first-char "-")
      {:error (subs b 1 delim)}
      (= first-char ":")
      (Integer/parseInt (subs b 1 delim))
      (= first-char "$")
      (let [bytes (buffer->bytes (subs b 1) 0)]
        (subs b (+ 1 (count (str bytes))) (+ 2 bytes))))))
