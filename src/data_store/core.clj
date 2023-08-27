(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [handler]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379 handler)
  (dynamic-expiry! 100))
