(ns data-store.handler)

(defn handler [input]
  (if
   (= input "PING")
    "$4\r\nPONG\r\n"
    ""))
