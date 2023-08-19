(ns data-store.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(def running (atom true))
(defn stop [] (swap! running (fn [_] false)))

(def write-lock (Object.))
(defn serve [handler port]
  (with-open [server-sock (ServerSocket. port)]
    (while @running
      (loop [store {}]
        (let [sock (.accept server-sock)
              reader (io/reader sock)
              writer (io/writer sock)
              input (loop [acc []
                           r reader]
                      (if (.ready r)
                        (recur (conj acc (.readLine r)) r)
                        acc))
              [new-store output] (handler store input)]
          (locking write-lock (.write writer output))
          (.close writer)
          (.close reader)
          (recur new-store))))))
