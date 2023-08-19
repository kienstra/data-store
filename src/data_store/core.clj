(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [handler]]
   [data-store.server :refer [serve]]))

(defn -main []
  (serve 6379 handler))
