(ns data-store.core
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [data-store.handler :refer [handler]])
  (:import [java.net ServerSocket]))

(defn send-to-socket [writer msg]
  (.write writer msg))

(defn serve [port]
  (let [running (atom true)]
    (future
      (with-open [server-sock (ServerSocket. port)]
        (while @running
          (with-open [sock (.accept server-sock)
                      reader (io/reader sock)
                      writer (io/writer sock)]
            (while (.ready reader)
              (send-to-socket writer (handler (.readLine reader))))))))
    running))

(defn -main []
  (serve 6379))
