(ns data-store.server
  (:require [clojure.java.io :refer [reader writer]])
  (:import [java.net ServerSocket]))

(defn receive [sock]
  (.readLine (reader sock)))

(defn send-to-socket [sock msg]
  (let [writer (writer sock)]
    (.write writer msg)
    (.flush writer)))

(defn serve [port handler]
  (with-open [server-sock (ServerSocket. port)
              sock (.accept server-sock)]
    (send-to-socket sock (handler (receive sock)))))
