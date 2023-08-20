(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [handler]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= [{} "-Error invalid command\r\n"] (handler {} ["*1" "$12" "DOESNOTEXIST"] 0))))
  (testing "PING"
    (is (= [{} "$4\r\nPONG\r\n"] (handler {} ["*1" "$4" "PING"] 0))))
  (testing "ECHO"
    (is (= [{} "+Hello World\r\n"] (handler {} ["*2" "$4" "ECHO" "$11" "Hello World"] 0)))
    (is (= [{} "+Simple\r\n"] (handler {} ["*2" "$4" "ECHO" "$6" "Simple"] 0)))
    (is (= [{} "+Several Words Together\r\n"] (handler {} ["*2" "$4" "ECHO" "$22" "Several Words Together"] 0)))
    (is (= [{} "-Error nothing to echo\r\n"] (handler {} ["*2" "$4" "ECHO"] 0))))
  (testing "SET"
    (is (= [{} "-Error nothing to set\r\n"] (handler {} ["*1" "$3" "SET"] 0)))
    (is (= [{} "-Error not a string\r\n"] (handler {} ["*2" "$3" "SET" "$4" "Name" "$4" 9325] 0)))
    (is (= [{"Name" {:val "John"}} "+OK\r\n"] (handler {} ["*2" "$3" "SET" "$4" "Name" "$4" "John"] 0)))
    (is (= [{"Name" {:val "Renamed"}} "+OK\r\n"] (handler {"Name" {:val "John"}} ["*2" "$3" "SET" "$4" "Name" "$7" "Renamed"] 0))))
  (testing "SET with expiration"
    (is (= [{"Name" {:val "Renamed" :exp 100000}} "+OK\r\n"] (handler {"Name" {:val "John"}} ["*3" "$3" "SET" "$4" "Name" "$7" "Renamed" "$2" "EX" "$3" "100"] 0))))
  (testing "GET"
    (is (= [{} "-Error nothing to get\r\n"] (handler {} ["*1" "$3" "GET"] 0)))
    (is (= [{} "$-1\r\n"] (handler {} ["*2" "$3" "GET" "$4" "Name"] 0)))
    (is (= [{"foo" {:val 395}} "-Error not a string\r\n"] (handler {"foo" {:val 395}} ["*2" "$3" "GET" "$3" "foo"] 0)))
    (is (= [{"Name" {:val "John"}} "+John\r\n"] (handler {"Name" {:val "John"}} ["*2" "$3" "GET" "$4" "Name"] 0))))
  (testing "GET with expiration"
    (is (= [{"Name" {:val "John" :exp 10}} "$-1\r\n"] (handler {"Name" {:val "John" :exp 10}} ["*2" "$3" "GET" "$4" "Name"] 20)))))
