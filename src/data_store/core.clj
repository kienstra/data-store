(ns data-store.core
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(defn receive
  "Read a line of textual data from the given socket"
  [reader]
  (.readLine reader))

(defn send-to-socket [writer msg]
  (if
   msg
    (do
      (.write writer "*1")
      (.write writer msg))
    nil))

(defn serve [port handler]
  (let [running (atom true)]
    (future
      (with-open [server-sock (ServerSocket. port)
                  sock (.accept server-sock)
                  reader (io/reader sock)
                  writer (io/writer sock)]
        (while (.ready reader)
          (let [msg-in (receive reader)
                msg-out (handler msg-in)]
            (send-to-socket writer msg-out)))))
    running))

(defn handler [input]
  (println input)
  (if (= input "PING")
    "PONG"
    nil))

(defn -main []
  (serve 6379 handler))
