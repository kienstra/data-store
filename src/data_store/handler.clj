(ns data-store.handler)

(defn handler [input]
  (if
   (.contains input "PING")
    "$4\r\nPONG\r\n"
    ""))
