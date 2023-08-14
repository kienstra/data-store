(ns data-store.core
  (:require [clojure.java.io :as io])
  (:import [java.net InetAddress ServerSocket SocketException]))

(defn- server-socket [server]
  (ServerSocket.
   (:port server)
   (:backlog server)
   (InetAddress/getByName (:host server))))

(defn tcp-server
  [& {:as options}]
  {:pre [(:port options)
         (:handler options)]}
  (merge
   {:host "127.0.0.1"
    :backlog 50
    :socket (atom nil)
    :connections (atom #{})}
   options))

(defn close-socket [server socket]
  (swap! (:connections server) disj socket)
  (when-not (.isClosed socket)
    (.close socket)))

(defn- open-server-socket [server]
  (reset! (:socket server)
          (server-socket server)))

(defn- accept-connection
  [{:keys [handler connections socket] :as server}]
  (let [conn (.accept @socket)]
    (swap! connections conj conn)
    (future
      (handler conn))))

(defn running?
  [server]
  (if-let [socket @(:socket server)]
    (not (.isClosed socket))))

(defn start [server]
  (server-socket server))

(defn stop
  [server]
  (doseq [socket @(:connections server)]
    (close-socket server socket))
  (.close @(:socket server)))

(defn wrap-streams
  [handler]
  (fn [socket]
    (with-open [input  (.getInputStream socket)
                output (.getOutputStream socket)]
      (handler input output))))

(defn wrap-io
  [handler]
  (wrap-streams
   (fn [input output]
     (with-open [reader (io/reader input)
                 writer (io/writer output)]
       (while (.ready reader)
         (handler (.readLine reader) writer))))))

(defn handler [input writer]
  (println input)
  (if (= input "PING")
    (do
      (.write writer "A")
      (.write writer "PONG")
      (.flush writer))
    nil))

(def server
  (tcp-server
   :port    6379
   :handler (wrap-io handler)))

(defn -main []
  (start server))
