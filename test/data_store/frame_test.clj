(ns data-store.frame-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.frame :refer [buffer->frame]]))

(deftest frame-test
  (testing "Simple String"
    (is (= nil (buffer->frame "+noend")))
    (is (= "OK" (buffer->frame "+OK\r\n")))
    (is (= "longer simple string" (buffer->frame "+longer simple string\r\n"))))
  (testing "Error"
    (is (= nil (buffer->frame "-errornoend")))
    (is (= {:error "Example error"} (buffer->frame "-Example error\r\n")))
    (is (= {:error "Long error message that continues"} (buffer->frame "-Long error message that continues\r\n"))))
  (testing "Integer"
    (is (= nil (buffer->frame ":123")))
    (is (= 123 (buffer->frame ":123\r\n")))
    (is (= 999999999 (buffer->frame ":999999999\r\n")))))
