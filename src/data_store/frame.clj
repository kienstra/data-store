(ns data-store.frame
  (:require [clojure.string :refer [join]]))

(defn buffer->count [b i]
  (if (Character/isDigit (get b i))
    (recur b (inc i))
    (Integer/parseInt (subs b 0 i))))

(def delim "\r\n")
(defn unserialize [b]
  (println b)
  (let [first-char (subs b 0 1)
        delim-index (.indexOf b delim)]
    (cond
      (or (= b "$-1\r\n") (= b "*-1\r\n"))
      nil
      (= first-char "+")
      (subs b 1 delim-index)
      (= first-char "-")
      {:error (subs b 1 delim-index)}
      (= first-char ":")
      (Integer/parseInt (subs b 1 (if (= delim-index -1) (count b) delim-index)))
      (= first-char "$")
      (let [bytes (buffer->count (subs b 1) 0)]
        (subs b (+ 3 (count (str bytes))) (+ 4 bytes)))
      (= first-char "*")
      (let [number-elements (buffer->count (subs b 1) 0)]
        (map unserialize (re-seq #"\$\d+\r\n\w+\r\n|[^\$][^\r\n]+\r\n" (subs (subs b (+ 1 (count (str number-elements)))) (+ 1 (count (str number-elements))))))))))

(defn serialize [x]
  (cond
    (string? x)
    (str "+" x delim)
    (number? x)
    (str ":" x delim)
    (= x nil)
    (str "$-1" delim)
    (seq? x)
    (let [length (count x)]
      (str "*" length delim (join (map serialize x))))
    (= x '())
    (str "*0" delim)
    :else x))
