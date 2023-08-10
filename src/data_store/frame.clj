(ns data-store.frame)

(defn buffer->frame [b]
  (let [first-char (subs b 0 1)
        delim (.indexOf b "\r\n")]
    (if
     (= delim -1)
      nil
      (cond
        (= first-char "+")
        (subs b 1 delim)
        (= first-char "-")
        {:error (subs b 1 delim)}
        (= first-char ":")
        (Integer/parseInt (subs b 1 delim))))))
