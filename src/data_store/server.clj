(ns data-store.server
  (:require
   [clojure.core.async :as async]
   [clojure.java.io :as io])
  (:import (java.net ServerSocket SocketException)))

(defn pmap-file
  [socket handler]
  (with-open [rdr (io/reader socket :append true)
              wtr (io/writer socket :append true)]
    (let [lines (line-seq rdr)]
      (dorun
       (map #(when % (.write wtr %))
            (pmap handler lines))
       (.flush wtr)))))

;; Example of calling this
(def accumulator (atom 0))

(defn- example-row-fn
  "Trivial example"
  [row-string]
  (str row-string "," (swap! accumulator inc) "\n"))

(defn serve [port handler]
  (with-open [server (ServerSocket. port)]
   (while true
      (let [socket (.accept server)]
        (pmap-file socket handler)))))
