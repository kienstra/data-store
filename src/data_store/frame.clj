(ns data-store.frame)

(defn buffer->count [b i]
  (if (Character/isDigit (get b i))
    (recur b (+ i 1))
    (Integer/parseInt (subs b 0 i))))

(defn buffer->frame [b]
  (let [first-char (subs b 0 1)
        delim (.indexOf b "\r\n")]
    (cond
      (or (= delim -1) (= b "$-1\r\n") (= b "*-1\r\n"))
      nil
      (= first-char "+")
      (subs b 1 delim)
      (= first-char "-")
      {:error (subs b 1 delim)}
      (= first-char ":")
      (Integer/parseInt (subs b 1 delim))
      (= first-char "$")
      (let [bytes (buffer->count (subs b 1) 0)]
        (subs b (+ 3 (count (str bytes))) (+ 4 bytes)))
      (= first-char "*")
      (let [number-elements (buffer->count (subs b 1) 0)]
        (map buffer->frame (re-seq #"\$\d+\r\n\w+\r\n|[^\$][^\r\n]+\r\n" (subs (subs b (+ 1 (count (str number-elements)))) (+ 1 (count (str number-elements))))))))))
