(ns data-store.server
  (:require [clojure.java.io :as io])
  (:import [java.net ServerSocket]))

(def running (atom true))
(defn stop [] (swap! running (fn [_] false)))

(defn serve [handler port]
  (with-open [server-sock (ServerSocket. port)]
    (while @running
      (loop [store {}]
        (let [sock (.accept server-sock)
              reader (io/reader sock)
              writer (io/writer sock)]
          (loop [r reader]
            (if (.ready r)
              (let [input (.readLine r)]
                (println input)
                (when
                 (= input "PING")
                  (.write writer "$4\r\nPONG\r\n"))
                (when
                 (= input "DOCS")
                  (.write writer "*2\r\n$4\r\nsave\r\n*2\r\n$3\r\nfoo\r\n$2\r\nbar\r\n"))
                (recur r))
              (do
                (.close writer)
                (.close reader))))
          (recur {}))))))
