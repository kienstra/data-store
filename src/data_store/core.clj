(ns data-store.core
  (:require [data-store.server :refer [serve]]
            [data-store.handler :refer [handler]]))

(defn -main []
  (serve 6379 handler))
