(ns data-store.server
  "This namespace assumes two things:
  1. that you have put this code in a directory with the path
     `CODE-PATH/examples/tcp/echo_server/server.clj`, and
  2. that you have added `CODE-PATH` to one of your `:source-paths` definitions
     in your `project.clj` file."
  (:require
   [clojure.core.async :as async]
   [clojure.java.io :as io])
  (:import (java.net ServerSocket SocketException)))


(defn serve [socket]
  []
    (with-open [server (ServerSocket. socket)]
      (while true
        (loop []
          (println "recurred in serve function")
          (let [sock (.accept server)
                reader (io/reader sock)
                writer (io/writer sock)
                _ (.readLine reader)]
            (.write writer "$4\r\nPONG\r\n")
            (.close writer)
            (.close reader))
          (recur)))))
