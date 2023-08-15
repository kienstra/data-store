(ns data-store.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(defn serve [handler port]
  (let [running (atom true)]
    (future
      (with-open [server-sock (ServerSocket. port)]
        (while @running
          (with-open [sock (.accept server-sock)
                      reader (io/reader sock)
                      writer (io/writer sock)]
            (.write writer (handler
                            (loop [acc []
                                   r reader]
                              (if (.ready r)
                                (recur (conj acc (.readLine r)) r)
                                acc))))))))
    running))
