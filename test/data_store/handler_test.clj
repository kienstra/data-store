(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [store-handler-strategy]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= [{} "+OK\r\n"] (store-handler-strategy {} ["DOESNOTEXIST"] 0))))
  (testing "PING"
    (is (= [{} "+PONG\r\n"] (store-handler-strategy {} ["PING"] 0))))
  (testing "ECHO"
    (is (= [{} "+Hello World\r\n"] (store-handler-strategy {} ["ECHO" "Hello World"] 0)))
    (is (= [{} "+Simple\r\n"] (store-handler-strategy {} ["ECHO" "Simple"] 0)))
    (is (= [{} "+Several Words Together\r\n"] (store-handler-strategy {} ["ECHO" "Several Words Together"] 0)))
    (is (= [{} "-Error nothing to echo\r\n"] (store-handler-strategy {} ["ECHO"] 0))))
  (testing "SET"
    (is (= [{} "-Error nothing to set\r\n"] (store-handler-strategy {} ["SET"] 0)))
    (is (= [{} "-Error not a string\r\n"] (store-handler-strategy {} ["SET" "Name" 9325] 0)))
    (is (= [{"Name" {:val "John"}} "+OK\r\n"] (store-handler-strategy {} ["SET" "Name" "John"] 0)))
    (is (= [{"Name" {:val "Renamed"}} "+OK\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "Renamed"] 0))))
  (testing "GET"
    (is (= [{} "-Error nothing to get\r\n"] (store-handler-strategy {} ["GET"] 0)))
    (is (= [{} "$-1\r\n"] (store-handler-strategy {} ["GET" "Name"] 0)))
    (is (= [{"foo" {:val 395}} "-Error not a string\r\n"] (store-handler-strategy {"foo" {:val 395}} ["GET" "foo"] 0)))
    (is (= [{"Name" {:val "John"}} "+John\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["GET" "Name"] 0))))
  (testing "SET with expiration"
    (is (= [{"Name" {:val "John" :exp 100100}} "+OK\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "John" "EX" "100"] 100)))
    (is (= [{"Name" {:val "John" :exp 200}} "+OK\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "John" "PEX" "100"] 100)))
    (is (= [{"Name" {:val "John" :exp 100000}} "+OK\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "John" "EXAT" "100"] 100)))
    (is (= [{"Name" {:val "John" :exp 100}} "+OK\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "John" "PXAT" "100"] 100))))
  (testing "GET with expiration"
    (is (= [{"Name" {:val "John" :exp 30}} "+John\r\n"] (store-handler-strategy {"Name" {:val "John" :exp 30}} ["GET" "Name"] 20)))
    (is (= [{} "$-1\r\n"] (store-handler-strategy {"Name" {:val "John" :exp 20}} ["GET" "Name"] 20)))
    (is (= [{} "$-1\r\n"] (store-handler-strategy {"Name" {:val "John" :exp 10}} ["GET" "Name"] 20))))
  (testing "SET and GET with expiration"
    (let [[store _] (store-handler-strategy {"Name" {:val "John"}} ["SET" "Name" "John" "EX" "100"] 100)]
      (is (= [{"Name" {:val "John" :exp 100100}} "+John\r\n"] (store-handler-strategy store ["GET" "Name"] 100099)))
      (is (= [{} "$-1\r\n"] (store-handler-strategy store ["GET" "Name"] 100100)))))
  (testing "EXPIRE"
    (is (= [{} ":0\r\n"] (store-handler-strategy {} ["EXPIRE"] 0)))
    (is (= [{} ":0\r\n"] (store-handler-strategy {} ["EXPIRE" "Name"] 0)))
    (is (= [{} ":0\r\n"] (store-handler-strategy {} ["EXPIRE" "Name" "100"] 0)))
    (is (= [{"Name" {:val "John" :exp 100000}} ":1\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["EXPIRE" "Name" "100"] 0))))
  (testing "EXISTS"
    (is (= [{} "-Error nothing to check\r\n"] (store-handler-strategy {} ["EXISTS"] 0)))
    (is (= [{} ":0\r\n"] (store-handler-strategy {} ["EXISTS" "Name"] 0)))
    (is (= [{"Name" {:val nil}} ":1\r\n"] (store-handler-strategy {"Name" {:val nil}} ["EXISTS" "Name"] 0)))
    (is (= [{"Name" {:val "John"}} ":1\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["EXISTS" "Name"] 0)))
    (is (= [{"Name" {:val "John"}} ":1\r\n:0\r\n"] (store-handler-strategy {"Name" {:val "John"}} ["EXISTS" "Name" "Doesnotexist"] 0))))
  (testing "DELETE"
    (is (= [{} "-Error nothing to delete\r\n"] (store-handler-strategy {} ["DELETE"] 0)))
    (is (= [{} ":0\r\n"] (store-handler-strategy {} ["DELETE" "Name"] 0)))
    (is (= [{} ":1\r\n"] (store-handler-strategy {"foo" {:value "bar"}} ["DELETE" "foo"] 0)))
    (is (= [{} ":2\r\n"] (store-handler-strategy {"foo" {:value "bar"} "another" {:value "something"}} ["DELETE" "foo" "another"] 0))))
  (testing "INCR"
    (is (= [{} "-Error nothing to increment\r\n"] (store-handler-strategy {} ["INCR"] 0)))
    (is (= [{"foo" {:val "1"}} ":1\r\n"] (store-handler-strategy {} ["INCR" "foo"] 0)))
    (is (= [{"baz" {:val "91"}} ":91\r\n"] (store-handler-strategy {"baz" {:val "90"}} ["INCR" "baz"] 0))))
  (testing "DECR"
    (is (= [{} "-Error nothing to decrement\r\n"] (store-handler-strategy {} ["DECR"] 0)))
    (is (= [{"foo" {:val "-1"}} ":-1\r\n"] (store-handler-strategy {} ["DECR" "foo"] 0)))
    (is (= [{"baz" {:val "89"}} ":89\r\n"] (store-handler-strategy {"baz" {:val "90"}} ["DECR" "baz"] 0))))
  (testing "LPUSH"
    (is (= [{} "-Error nothing to push\r\n"] (store-handler-strategy {} ["LPUSH"] 0)))
    (is (= [{"foo" {:val ["bar"]}} ":1\r\n"] (store-handler-strategy {} ["LPUSH" "foo" "bar"] 0)))
    (is (= [{"foo" {:val ["baz" "bar"]}} ":2\r\n"] (store-handler-strategy {} ["LPUSH" "foo" "bar" "baz"] 0))))
  (testing "RPUSH"
    (is (= [{} "-Error nothing to push\r\n"] (store-handler-strategy {} ["RPUSH"] 0)))
    (is (= [{"foo" {:val ["bar"]}} ":1\r\n"] (store-handler-strategy {} ["RPUSH" "foo" "bar"] 0)))
    (is (= [{"foo" {:val ["bar" "baz"]}} ":2\r\n"] (store-handler-strategy {} ["RPUSH" "foo" "bar" "baz"] 0)))))
