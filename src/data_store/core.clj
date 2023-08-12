(ns data-store.core
  (:require [org.httpkit.server :refer [on-close on-receive run-server with-channel]]
            [clojure.core.async :refer [chan]]))

(def channel (chan))
(defn handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (println "channel closed: " status)))
    (on-receive channel (fn [data]
                          (println data)
                          (send! channel data)))))

(defn -main []
  (run-server handler {:port 6379}))
