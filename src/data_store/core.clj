(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [output-handler-adapter
                               store-handler-adapter]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379 output-handler-adapter store-handler-adapter)
  (dynamic-expiry! 100))
