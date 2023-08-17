(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [handler]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= [{} "-Error invalid command\r\n"] (handler {} ["*1" "$12" "DOESNOTEXIST"]))))
  (testing "PING"
    (is (= [{} "$4\r\nPONG\r\n"] (handler {} ["*1" "$4" "PING"]))))
  (testing "ECHO"
    (is (= [{} "+Hello World\r\n"] (handler {} ["*2" "$4" "ECHO" "$11" "Hello World"])))
    (is (= [{} "+Simple\r\n"] (handler {} ["*2" "$4" "ECHO" "$6" "Simple"])))
    (is (= [{} "+Several Words Together\r\n"] (handler {} ["*2" "$4" "ECHO" "$22" "Several Words Together"])))
    (is (= [{} "-Error nothing to echo\r\n"] (handler {} ["*2" "$4" "ECHO"]))))
  (testing "SET"
    (is (= [{} "-Error nothing to set\r\n"] (handler {} ["*2" "$3" "SET" "$4" "Name"])))
    (is (= [{"Name" "John"} "+OK\r\n"] (handler {} ["*2" "$3" "SET" "$4" "Name" "$4" "John"])))
    (is (= [{"Name" "Renamed"} "+OK\r\n"] (handler {"Name" "John"} ["*2" "$3" "SET" "$4" "Name" "$7" "Renamed"]))))
  (testing "GET"
    (is (= [{} "-Error nothing to get\r\n"] (handler {} ["*1" "$3" "GET"])))
    (is (= [{} "$-1\r\n"] (handler {} ["*2" "$3" "GET" "$4" "Name"])))
    (is (= [{"foo" 395} "-Error not a string\r\n"] (handler {"foo" 395} ["*2" "$3" "GET" "$3" "foo"])))
    (is (= [{"Name" "John"} "+John\r\n"] (handler {"Name" "John"} ["*2" "$3" "GET" "$4" "Name"])))))
