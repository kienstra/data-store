(ns data-store.handler)

(defn handler [input]
  (if-let [command (nth input 2 false)]
    (cond
      (= command "PING")
      "$4\r\nPONG\r\n"
      (and (= command "ECHO") (nth input 4))
      (str (nth input 3) "\r\n" (nth input 4) "\r\n")
      :else "$-1\r\n")
    "-Error no command \r\n"))
