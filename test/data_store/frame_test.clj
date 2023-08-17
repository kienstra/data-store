(ns data-store.frame-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.frame :refer [serialize unserialize]]))

(deftest frame-test-unserialize
  (testing "Simple string"
    (is (= "OK" (unserialize "+OK\r\n")))
    (is (= "longer simple string" (unserialize "+longer simple string\r\n"))))
  (testing "Error"
    (is (= {:error "Example error"} (unserialize "-Example error\r\n")))
    (is (= {:error "Long error message that continues"} (unserialize "-Long error message that continues\r\n"))))
  (testing "Integer"
    (is (= 123 (unserialize ":123\r\n")))
    (is (= 999999999 (unserialize ":999999999\r\n"))))
  (testing "Bulk string"
    (is (= nil (unserialize "$-1\r\n")))
    (is (= "short" (unserialize "$5\r\nshort\r\n")))
    (is (= "longerbulkstring" (unserialize "$17\r\nlongerbulkstring\r\n"))))
  (testing "Array"
    (is (= nil (unserialize "*-1\r\n")))
    (is (= '() (unserialize "*0\r\n")))
    (is (= '("foo" "bar") (unserialize "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n")))
    (is (= '(1 2 3) (unserialize "*3\r\n:1\r\n:2\r\n:3\r\n")))))

(deftest frame-test-serialize
  (testing "Simple string"
    (is (= "+OK\r\n" (serialize "OK")))
    (is (= "+longer simple string\r\n" (serialize "longer simple string"))))
  (testing "Integer"
    (is (= ":123\r\n" (serialize 123)))
    (is (= ":999999999999\r\n" (serialize 999999999999))))
  (testing "Array"
    (is (= "$-1\r\n" (serialize nil)))
    (is (= "*0\r\n" (serialize '())))
    (is (= "*2\r\n+foo\r\n+bar\r\n" (serialize '("foo" "bar"))))
    (is (= "*3\r\n:1\r\n:2\r\n:3\r\n" (serialize '(1 2 3))))))
