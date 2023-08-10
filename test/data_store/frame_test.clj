(ns data-store.frame-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.frame :refer [buffer->count buffer->frame]]))

(deftest frame-test
  (testing "Simple string"
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
    (is (= 999999999 (buffer->frame ":999999999\r\n"))))
  (testing "Buffer count"
    (is (= 17 (buffer->count (subs "$17longerbulkstring\r\n" 1) 0))))
  (testing "Bulk string"
    (is (= nil (buffer->frame "$-1\r\n")))
    (is (= "short" (buffer->frame "$5\r\nshort\r\n")))
    (is (= "longerbulkstring" (buffer->frame "$17\r\nlongerbulkstring\r\n"))))
  (testing "Array"
    (is (= nil (buffer->frame "*-1\r\n")))
    (is (= '() (buffer->frame "*0\r\n")))
    (is (= '("foo" "bar") (buffer->frame "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n")))
    (is (= '(1 2 3) (buffer->frame "*3\r\n:1\r\n:2\r\n:3\r\n")))))
