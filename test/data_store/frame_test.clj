(ns data-store.frame-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.frame :refer [serialize unserialize]]))

(deftest frame-test-unserialize
  (testing "Simple string"
    (is (= ["OK" 5] (unserialize "+OK\r\n")))
    (is (= ["longer simple string" 23] (unserialize "+longer simple string\r\n"))))
  (testing "Error"
    (is (= [{:error "Example error"} 16] (unserialize "-Example error\r\n")))
    (is (= [{:error "Long error message that continues"} 36] (unserialize "-Long error message that continues\r\n"))))
  (testing "Integer"
    (is (= [123 6] (unserialize ":123\r\n")))
    (is (= [999999999 12] (unserialize ":999999999\r\n"))))
  (testing "Bulk string"
    (is (= [nil 5] (unserialize "$-1\r\n")))
    (is (= [{:error "Does not end in the delimiter"} 0] (unserialize "$5\r\nshort")))
    (is (= ["short" 11] (unserialize "$5\r\nshort\r\n")))
    (is (= ["longerbulkstring" 23] (unserialize "$16\r\nlongerbulkstring\r\n"))))
  (testing "Array"
    (is (= [nil 5] (unserialize "*-1\r\n")))
    (is (= [[] 4] (unserialize "*0\r\n")))
    (is (= [["foo" "bar"] 22]  (unserialize "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n")))
    (is (= [[1 2 3] 16] (unserialize "*3\r\n:1\r\n:2\r\n:3\r\n")))
    (is (= [[[1 2 3] 2 3] 28] (unserialize "*3\r\n*3\r\n:1\r\n:2\r\n:3\r\n:2\r\n:3\r\n")))
    (is (= [[[1 2 [1 2 3]] 2 3] 40] (unserialize "*3\r\n*3\r\n:1\r\n:2\r\n*3\r\n:1\r\n:2\r\n:3\r\n:2\r\n:3\r\n")))))

(deftest frame-test-serialize
  (testing "Simple string"
    (is (= "+OK\r\n" (serialize "OK")))
    (is (= "+longer simple string\r\n" (serialize "longer simple string"))))
  (testing "Integer"
    (is (= ":123\r\n" (serialize 123)))
    (is (= ":999999999999\r\n" (serialize 999999999999))))
  (testing "Array"
    (is (= "$-1\r\n" (serialize nil)))
    (is (= "*0\r\n" (serialize [])))
    (is (= "*2\r\n+foo\r\n+bar\r\n" (serialize ["foo" "bar"])))
    (is (= "*3\r\n:1\r\n:2\r\n:3\r\n" (serialize [1 2 3])))))

(deftest frame-test-serialize-unserialize
  (testing "Simple string"
    (let [initial "OK"
          [unserialized _] (unserialize (serialize initial))]
      (is (= initial unserialized)))
    (let [initial [1 2 [1 2 3]]
          [unserialized _] (unserialize (serialize initial))]
      (is (= initial unserialized)))))
