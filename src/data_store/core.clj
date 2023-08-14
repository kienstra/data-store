(ns data-store.core
  "Functions for creating a threaded TCP server."
  (:require [clojure.java.io :as io])
  (:import [java.net InetAddress ServerSocket Socket SocketException]))

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
      (try (handler conn)
           (finally (close-socket server conn))))))

(defn running?
  [server]
  (if-let [socket @(:socket server)]
    (not (.isClosed socket))))

(defn start [server]
  (open-server-socket server)
  (future
    (while (running? server)
      (try
        (accept-connection server)
        (catch SocketException _)))))

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
  "Wrap a handler so that it expects a Reader and Writer as arguments, rather
    than a raw Socket."
  [handler]
  (wrap-streams
   (fn [input output]
     (with-open [reader (io/reader input)
                 writer (io/writer output)]
       (while (.ready reader)
         (handler (.readLine reader) writer))))))

(defn handler [reader writer]
  (.append writer ":12"))

(def server
  (tcp-server
   :port    6379
   :handler (wrap-io handler)))

(defn -main []
  (start server))
