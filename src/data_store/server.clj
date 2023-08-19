(ns data-store.server
  (:require
   [clojure.core.async :as async]
   [clojure.java.io :as io])
  (:import (java.net ServerSocket SocketException)))

(defn async-loop
  [in out]
  (async/go-loop []
    (let [msg (async/<! in)]
      (async/>! out msg)
      (recur))))

(defn line-in
  [sock]
  (let [in (async/chan)
        reader (io/reader sock)]
    (async/go-loop []
      (when (.ready reader)
        (let [msg (.readLine reader)]
          (when msg
            (async/>! in msg))
          (recur))))
    in))

(defn line-out
  [sock handler]
  (let [out (async/chan)]
    (async/go-loop [store {}]
      (let [msg (async/<! out)
            [new-store out-msg] (handler store msg)]
        (try
          (when out-msg
           (let [writer (io/writer sock)]
             (.write writer out-msg)
             (.write writer "$-1\r\n")
             (.flush writer)))
            (catch SocketException _ #()))
        (recur new-store)))
    out))

(defn serve [sock handler]
  (with-open [server (ServerSocket. sock)]
    (while true
      (let [sock (.accept server)]
        (async/go
          (async-loop
           (line-in sock)
           (line-out sock handler)))))))
