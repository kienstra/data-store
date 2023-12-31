(ns data-store.frame
  (:require [clojure.string :refer [join]]))

(declare unserialize)
(defn buffer->count [b i]
  (if (Character/isDigit (get b i))
    (recur b (inc i))
    (Integer/parseInt (subs b 0 i))))

(def delim "\r\n")

(defn- unserialize-bulk-string [b first-char]
  (let [bytes (buffer->count (subs b 1) 0)
        begin-index (+ (count first-char) (count (str bytes)) (count delim))
        end-index (+ begin-index bytes)
        expected-length (+ end-index (count delim))
        ends-in-delim (and (>= (count b) expected-length) (= delim (subs b end-index expected-length)))]
    (if ends-in-delim
      [(subs b begin-index end-index) (+ end-index (count delim))]
      [{:error "Does not end in the delimiter"} 0])))

(defn- unserialize-array [b first-char]
  (let [n-elements (buffer->count (subs b 1) 0)
        begin-index (+ (count first-char) (count (str n-elements)) (count delim))]
    (if (= begin-index (count b))
      [[] begin-index]
      (let [rest-to-parse (subs b begin-index)
            initial (unserialize rest-to-parse)]
        (loop [[parsed i] [(vector (nth initial 0)) (nth initial 1)]]
          (if (= (count parsed) n-elements)
            [parsed (+ i begin-index)]
            (let [[new-parsed new-i] (unserialize (subs rest-to-parse i))]
              (recur [(conj parsed new-parsed) (+ i new-i)]))))))))

(defn unserialize [b]
  (let [first-char (subs b 0 1)
        delim-index (.indexOf b delim)
        after-delim-index (+ delim-index (count delim))]
    (cond
      (or (= b "$-1\r\n") (= b "*-1\r\n"))
      [nil 5]
      (= first-char "+")
      [(subs b 1 delim-index) after-delim-index]
      (= first-char "-")
      [{:error (subs b 1 delim-index)} after-delim-index]
      (= first-char ":")
      [(Integer/parseInt (subs b 1 (if (= delim-index -1) (count b) delim-index))) after-delim-index]
      (= first-char "$")
      (unserialize-bulk-string b first-char)
      (= first-char "*")
      (unserialize-array b first-char)
      :else {:error "Could not unserialize"})))

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
    (contains? x :error)
    (str "-" (:error x) delim)
    :else {:error "Could not serialize"}))
