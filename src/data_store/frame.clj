(ns data-store.frame
  (:require [clojure.string :refer [join]]))

(defn buffer->count [b i]
  (if (Character/isDigit (get b i))
    (recur b (inc i))
    (Integer/parseInt (subs b 0 i))))

(declare reduce-by)
(def delim "\r\n")
(defn unserialize [b]
  (if (= 0 (count b))
    [{:error "Invalid"} 0]
    (let [first-char (subs b 0 1)
          delim-index (.indexOf b delim)]
      (if (= delim-index -1)
        [{:error "Invalid"} 0]
        (let [after-delim-index (+ delim-index (count delim))]
          (cond
            (or (= b "$-1\r\n") (= b "*-1\r\n"))
            [nil 7]
            (= first-char "+")
            [(subs b 1 delim-index) after-delim-index]
            (= first-char "-")
            [{:error (subs b 1 delim-index)} after-delim-index]
            (= first-char ":")
            [(Integer/parseInt (subs b 1 (if (= delim-index -1) (count b) delim-index))) after-delim-index]
            (= first-char "$")
            (let [bytes (buffer->count (subs b 1) 0)
                  begin-index (+ (count first-char) (count (str bytes)) (count delim))
                  end-index (+ begin-index bytes)
                  expected-length (+ end-index (count delim))
                  ends-in-delim (and (>= (count b) expected-length) (= delim (subs b end-index (+ end-index (count delim)))))]
              (if ends-in-delim
                [(subs b begin-index end-index) (+ end-index (count delim))]
                [{:error "Does not end in the delimiter"} 0]))
            (= first-char "*")
            (let [number-elements (buffer->count (subs b 1) 0)
                  begin-index (+ (count first-char) (count (str number-elements)) (count delim))]
              (if (= begin-index (count b))
                [[] begin-index]
                (let [rest-to-parse (subs b begin-index)
                      [parsed _] (reduce-by rest-to-parse number-elements)]
                  (if (>= (count parsed) number-elements)
                    (reduce
                     (fn [[val-accum chars-used-accum] [val chars-used]]
                       [(conj val-accum val) (+ chars-used-accum chars-used)])
                     [[] begin-index]
                     parsed)
                    [{:error "Not all array elements found"} 0]))))))))))

(defn serialize [x]
  (cond
    (string? x)
    (str "+" x delim)
    (number? x)
    (str ":" x delim)
    (= x nil)
    (str "$-1" delim)
    (vector? x)
    (let [length (count x)]
      (str "*" length delim (join (map serialize x))))
    :else {:error "Could not serialize"}))

(defn reduce-by [to-parse number-elements]
  (reduce
   (fn [[parsed unparsed] char]
     (let [[ser used] (unserialize (str unparsed char))]
       (if (or (:error ser) (= (count parsed) number-elements))
         [parsed (str unparsed char)]
         [(conj parsed [ser used]) ""])))
   [[] ""]
   to-parse))
