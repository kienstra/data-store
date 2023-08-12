(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [handler]]))

(deftest handler-test
  (testing "Handler"
    (is (= nil (handler "NONEXISTENT")))
    (is (= nil (handler "NONEXISTENT FOO")))
    (is (= "PONG" (handler "PING")))
    (is (= "Hello World" (handler "ECHO Hello World")))))
