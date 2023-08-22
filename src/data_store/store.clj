(ns data-store.store
  (:require
   [clojure.core.async :refer [<! go-loop timeout]]
   [data-store.expiration :refer [expire-n]]))

(def store (atom {}))
(defn dynamic-expiry! [t]
  (go-loop []
    (<! (timeout t))
    (swap! store (fn [previous-store]
                   (expire-n previous-store (System/currentTimeMillis) 20)))
    (recur)))
