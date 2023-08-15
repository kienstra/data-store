(ns data-store.core
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [data-store.handler :refer [handler]])
  (:import [java.net ServerSocket]))

(defn serve [port]
  (let [running (atom true)]
    (future
      (with-open [server-sock (ServerSocket. port)]
        (while @running
          (let [sock (.accept server-sock)
                      reader (io/reader sock)]
            (doseq [line (line-seq reader)]
              (.write (io/writer (.accept server-sock)) line))))))
              ; Push (.readLine reader) onto data type for input array
              ; i in n
              ; once i is more than n, the array is complete
              ; (send-to-socket processed-input)
    running))

(defn -main []
  (serve 6379))
