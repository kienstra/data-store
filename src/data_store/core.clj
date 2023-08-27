(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [output-handler-strategy
                               store-handler-strategy]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379 output-handler-strategy store-handler-strategy)
  (dynamic-expiry! 100))
