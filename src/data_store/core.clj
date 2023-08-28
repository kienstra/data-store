(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [output-strategy
                               update-store-strategy]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379)
  (dynamic-expiry! 100))
