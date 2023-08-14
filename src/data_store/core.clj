(ns data-store.core
  (:require [clojure.java.io :as io]
            [data-store.handler :refer [handler]])
  (:import [java.net ServerSocket]))

(defn send-to-socket [writer msg]
  (.write writer msg))

(defn serve [port]
  (with-open
   [server-sock (ServerSocket. port)
    sock (.accept server-sock)
    reader (io/reader sock)
    writer (io/writer sock)]
    (while (.ready reader)
      (let [msg-in (.readLine reader)
            msg-out (handler msg-in)]
        (send-to-socket writer msg-out)))))

(defn -main []
  (serve 6379))
