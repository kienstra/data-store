(ns data-store.core
  (:gen-class)
  (:require
   [data-store.handler :refer [output
                               update-store]]
   [data-store.server :refer [serve!]]
   [data-store.store :refer [dynamic-expiry!]]))

(defn -main []
  (serve! 6379 output update-store)
  (dynamic-expiry! 100))
