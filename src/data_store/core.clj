(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [output-handler-factory
                               store-handler-factory]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379 output-handler-factory store-handler-factory)
  (dynamic-expiry! 100))
