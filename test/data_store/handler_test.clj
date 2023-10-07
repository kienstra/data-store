(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [update-store-strategy output-strategy]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= {} (update-store-strategy ["DOESNOTEXIST"] 0 {})))
    (is (= "-Error unknown command: DOESNOTEXIST\r\n" (output-strategy ["DOESNOTEXIST"] 0 {} {}))))
  (testing "PING"
    (is (= {} (update-store-strategy ["PING"] 0 {})))
    (is (= "+PONG\r\n" (output-strategy ["PING"] 0 {} {}))))
  (testing "ECHO"
    (is (= {} (update-store-strategy ["ECHO" "Hello World"] 0 {})))
    (is (= "+Hello World\r\n" (output-strategy ["ECHO" "Hello World"] 0 {} {})))
    (is (= "+Simple\r\n" (output-strategy ["ECHO" "Simple"] 0 {} {})))
    (is (= "+Several Words Together\r\n" (output-strategy ["ECHO" "Several Words Together"] 0 {} {})))
    (is (= "-Error nothing to echo\r\n" (output-strategy ["ECHO"] 0 {} {}))))
  (testing "SET"
    (is (= {} (update-store-strategy ["SET"] 0 {})))
    (is (= "-Error nothing to set\r\n" (output-strategy ["SET"] 0 {} {})))
    (is (= {} (update-store-strategy ["SET" "Name" 9325] 0 {})))
    (is (= "-Error not a string\r\n" (output-strategy ["SET" "Name" 9325] 0 {} {})))
    (is (= {"Name" {:val "John"}} (update-store-strategy ["SET" "Name" "John"] 0 {})))
    (is (= "+OK\r\n" (output-strategy ["SET" "Name" "John"] 0 {} {})))
    (is (= {"Name" {:val "Renamed"}} (update-store-strategy ["SET" "Name" "Renamed"] 0 {"Name" {:val "John"}})))
    (is (= "+OK\r\n" (output-strategy ["SET" "Name" "Renamed"] 0 {"Name" {:val "John"}} {"Name" {:val "Renamed"}}))))
  (testing "GET"
    (is (= {} (update-store-strategy ["GET"] 0 {})))
    (is (= "-Error nothing to get\r\n" (output-strategy ["GET"] 0 {} {})))
    (is (= "$-1\r\n" (output-strategy ["GET" "Name"] 0 {} {})))
    (is (= "-Error not a string\r\n" (output-strategy ["GET" "foo"] 0 {"foo" {:val 395}} {"foo" {:val 395}})))
    (is (= "+John\r\n" (output-strategy ["GET" "Name"] 0  {"Name" {:val "John"}}  {"Name" {:val "John"}}))))
  (testing "SET with expiration"
    (is (= {"Name" {:val "John" :exp 100100}} (update-store-strategy ["SET" "Name" "John" "EX" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 200}} (update-store-strategy ["SET" "Name" "John" "PEX" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 100000}} (update-store-strategy ["SET" "Name" "John" "EXAT" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 100}} (update-store-strategy ["SET" "Name" "John" "PXAT" "100"] 100 {"Name" {:val "John"}}))))
  (testing "GET with expiration"
    (is (= "+John\r\n" (output-strategy ["GET" "Name"] 20 {"Name" {:val "John" :exp 30}} {"Name" {:val "John" :exp 30}})))
    (is (= "$-1\r\n" (output-strategy ["GET" "Name"] 20 {"Name" {:val "John" :exp 20}} {})))
    (is (= "$-1\r\n" (output-strategy ["GET" "Name"] 20 {"Name" {:val "John" :exp 10}} {}))))
  (testing "SET and GET with expiration"
    (let [store (update-store-strategy ["SET" "Name" "John" "EX" "100"] 100 {"Name" {:val "John"}})]
      (is (= "+John\r\n" (output-strategy ["GET" "Name"] 100099 store store)))
      (is (= "$-1\r\n" (output-strategy ["GET" "Name"] 100100 store store)))))
  (testing "EXPIRE"
    (is (= ":0\r\n" (output-strategy ["EXPIRE"] 0 {} {})))
    (is (= ":0\r\n" (output-strategy ["EXPIRE" "Name"] 0 {} {})))
    (is (= ":0\r\n" (output-strategy ["EXPIRE" "Name" "100"] 0 {} {})))
    (is (= {"Name" {:val "John" :exp 100000}} (update-store-strategy ["EXPIRE" "Name" "100"] 0 {"Name" {:val "John"}})))
    (is (= ":1\r\n" (output-strategy ["EXPIRE" "Name" "100"] 0 {"Name" {:val "John"}} {"Name" {:val "John" :exp 100000}}))))
  (testing "EXISTS"
    (is (= {} (update-store-strategy ["EXISTS"] 0 {})))
    (is (= "-Error nothing to check\r\n" (output-strategy ["EXISTS"] 0  {} {})))
    (is (= ":0\r\n" (output-strategy ["EXISTS" "Name"] 0 {} {})))
    (is (= ":1\r\n" (output-strategy ["EXISTS" "Name"] 0 {"Name" {:val nil}} {"Name" {:val nil}})))
    (is (= ":1\r\n" (output-strategy ["EXISTS" "Name"] 0 {"Name" {:val "John"}} {"Name" {:val "John"}})))
    (is (= ":1\r\n" (output-strategy ["EXISTS" "Name" "Doesnotexist"] 0 {"Name" {:val "John"}} {"Name" {:val "John"}}))))
  (testing "DELETE"
    (is (= "-Error nothing to delete\r\n" (output-strategy ["DELETE"] 0 {} {})))
    (is (= {} (update-store-strategy ["DELETE" "Name"] 0 {})))
    (is (= ":0\r\n" (output-strategy ["DELETE" "Name"] 0 {} {})))
    (is (= {} (update-store-strategy ["DELETE" "foo"] 0 {"foo" {:value "bar"}})))
    (is (= ":1\r\n" (output-strategy ["DELETE" "foo"] 0 {"foo" {:value "bar"}} {"foo" {:value "bar"}})))
    (is (= {} (update-store-strategy ["DELETE" "foo" "another"] 0 {"foo" {:value "bar"} "another" {:value "something"}})))
    (is (= ":2\r\n" (output-strategy ["DELETE" "foo" "another"] 0  {"foo" {:value "bar"} "another" {:value "something"}} {}))))
  (testing "INCR"
    (is (= {} (update-store-strategy ["INCR"] 0 {})))
    (is (= "-Error nothing to increment\r\n" (output-strategy ["INCR"] 0 {} {})))
    (is (= {"foo" {:val "1"}} (update-store-strategy ["INCR" "foo"] 0 {})))
    (is (= ":1\r\n" (output-strategy ["INCR" "foo"] 0 {} {"foo" {:val "1"}})))
    (is (= {"baz" {:val "91"}} (update-store-strategy ["INCR" "baz"] 0 {"baz" {:val "90"}})))
    (is (= ":91\r\n" (output-strategy ["INCR" "baz"] 0 {"baz" {:val "90"}} {"baz" {:val "91"}}))))
  (testing "DECR"
    (is (= "-Error nothing to decrement\r\n" (output-strategy ["DECR"] 0 {} {})))
    (is (= {"foo" {:val "-1"}} (update-store-strategy ["DECR" "foo"] 0 {})))
    (is (= ":-1\r\n" (output-strategy ["DECR" "foo"] 0 {} {"foo" {:val "-1"}})))
    (is (= {"baz" {:val "89"}} (update-store-strategy ["DECR" "baz"] 0 {"baz" {:val "90"}})))
    (is (= ":89\r\n" (output-strategy ["DECR" "baz"] 0 {"baz" {:val "90"}} {"baz" {:val "89"}}))))
  (testing "LPUSH"
    (is (= "-Error nothing to push\r\n" (output-strategy ["LPUSH"] 0 {} {})))
    (is (= {"foo" {:val ["bar"]}} (update-store-strategy ["LPUSH" "foo" "bar"] 0 {})))
    (is (= ":1\r\n" (output-strategy ["LPUSH" "foo" "bar"] 0 {} {"foo" {:val ["bar"]}})))
    (is (= {"foo" {:val ["baz" "bar"]}} (update-store-strategy ["LPUSH" "foo" "bar" "baz"] 0 {})))
    (is (= ":2\r\n" (output-strategy ["LPUSH" "foo" "bar" "baz"] 0 {} {"foo" {:val ["baz" "bar"]}}))))
  (testing "RPUSH"
    (is (= "-Error nothing to push\r\n" (output-strategy ["RPUSH"] 0 {} {})))
    (is (= {"foo" {:val ["bar"]}} (update-store-strategy ["RPUSH" "foo" "bar"] 0 {})))
    (is (= ":1\r\n" (output-strategy ["RPUSH" "foo" "bar"] 0 {} {"foo" {:val ["bar"]}})))
    (is (= {"foo" {:val ["bar" "baz"]}} (update-store-strategy ["RPUSH" "foo" "bar" "baz"] 0 {})))
    (is (= ":2\r\n" (output-strategy ["RPUSH" "foo" "bar" "baz"] 0 {} {"foo" {:val ["bar" "baz"]}})))))
