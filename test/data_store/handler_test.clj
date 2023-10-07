(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [update-store output]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= {} (update-store "DOESNOTEXIST" [] 0 {})))
    (is (= "-Error unknown command: DOESNOTEXIST\r\n" (output "DOESNOTEXIST" [] 0 {} {}))))
  (testing "PING"
    (is (= {} (update-store "PING" [] 0 {})))
    (is (= "+PONG\r\n" (output "PING" [] 0 {} {}))))
  (testing "ECHO"
    (is (= {} (update-store "ECHO" ["Hello World"] 0 {})))
    (is (= "+Hello World\r\n" (output "ECHO" ["Hello World"] 0 {} {})))
    (is (= "+Simple\r\n" (output "ECHO" ["Simple"] 0 {} {})))
    (is (= "+Several Words Together\r\n" (output "ECHO" ["Several Words Together"] 0 {} {})))
    (is (= "-Error nothing to echo\r\n" (output "ECHO" [] 0 {} {}))))
  (testing "SET"
    (is (= {} (update-store "SET" [] 0 {})))
    (is (= "-Error nothing to set\r\n" (output "SET" [] 0 {} {})))
    (is (= {} (update-store "SET" ["Name" 9325] 0 {})))
    (is (= "-Error not a string\r\n" (output "SET" ["Name" 9325] 0 {} {})))
    (is (= {"Name" {:val "John"}} (update-store "SET" ["Name" "John"] 0 {})))
    (is (= "+OK\r\n" (output "SET" ["Name" "John"] 0 {} {})))
    (is (= {"Name" {:val "Renamed"}} (update-store "SET" ["Name" "Renamed"] 0 {"Name" {:val "John"}})))
    (is (= "+OK\r\n" (output "SET" ["Name" "Renamed"] 0 {"Name" {:val "John"}} {"Name" {:val "Renamed"}}))))
  (testing "GET"
    (is (= {} (update-store "GET" [] 0 {})))
    (is (= "-Error nothing to get\r\n" (output "GET" [] 0 {} {})))
    (is (= "$-1\r\n" (output "GET" ["Name"] 0 {} {})))
    (is (= "-Error not a string\r\n" (output "GET" ["foo"] 0 {"foo" {:val 395}} {"foo" {:val 395}})))
    (is (= "+John\r\n" (output "GET" ["Name"] 0  {"Name" {:val "John"}}  {"Name" {:val "John"}}))))
  (testing "SET with expiration"
    (is (= {"Name" {:val "John" :exp 100100}} (update-store "SET" ["Name" "John" "EX" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 200}} (update-store "SET" ["Name" "John" "PEX" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 100000}} (update-store "SET" ["Name" "John" "EXAT" "100"] 100 {"Name" {:val "John"}})))
    (is (= {"Name" {:val "John" :exp 100}} (update-store "SET" ["Name" "John" "PXAT" "100"] 100 {"Name" {:val "John"}}))))
  (testing "GET with expiration"
    (is (= "+John\r\n" (output "GET" ["Name"] 20 {"Name" {:val "John" :exp 30}} {"Name" {:val "John" :exp 30}})))
    (is (= "$-1\r\n" (output "GET" ["Name"] 20 {"Name" {:val "John" :exp 20}} {})))
    (is (= "$-1\r\n" (output "GET" ["Name"] 20 {"Name" {:val "John" :exp 10}} {}))))
  (testing "SET and GET with expiration"
    (let [store (update-store "SET" ["Name" "John" "EX" "100"] 100 {"Name" {:val "John"}})]
      (is (= "+John\r\n" (output "GET" ["Name"] 100099 store store)))
      (is (= "$-1\r\n" (output "GET" ["Name"] 100100 store store)))))
  (testing "EXPIRE"
    (is (= ":0\r\n" (output "EXPIRE" [] 0 {} {})))
    (is (= ":0\r\n" (output "EXPIRE" ["Name"] 0 {} {})))
    (is (= ":0\r\n" (output "EXPIRE" ["Name" "100"] 0 {} {})))
    (is (= {"Name" {:val "John" :exp 100000}} (update-store "EXPIRE" ["Name" "100"] 0 {"Name" {:val "John"}})))
    (is (= ":1\r\n" (output "EXPIRE" ["Name" "100"] 0 {"Name" {:val "John"}} {"Name" {:val "John" :exp 100000}}))))
  (testing "EXISTS"
    (is (= {} (update-store "EXISTS" [] 0 {})))
    (is (= "-Error nothing to check\r\n" (output "EXISTS" [] 0  {} {})))
    (is (= ":0\r\n" (output "EXISTS" ["Name"] 0 {} {})))
    (is (= ":1\r\n" (output "EXISTS" ["Name"] 0 {"Name" {:val nil}} {"Name" {:val nil}})))
    (is (= ":1\r\n" (output "EXISTS" ["Name"] 0 {"Name" {:val "John"}} {"Name" {:val "John"}})))
    (is (= ":1\r\n" (output "EXISTS" ["Name" "Doesnotexist"] 0 {"Name" {:val "John"}} {"Name" {:val "John"}}))))
  (testing "DELETE"
    (is (= "-Error nothing to delete\r\n" (output "DELETE" [] 0 {} {})))
    (is (= {} (update-store "DELETE" ["Name"] 0 {})))
    (is (= ":0\r\n" (output "DELETE" ["Name"] 0 {} {})))
    (is (= {} (update-store "DELETE" ["foo"] 0 {"foo" {:value "bar"}})))
    (is (= ":1\r\n" (output "DELETE" ["foo"] 0 {"foo" {:value "bar"}} {"foo" {:value "bar"}})))
    (is (= {} (update-store "DELETE" ["foo" "another"] 0 {"foo" {:value "bar"} "another" {:value "something"}})))
    (is (= ":2\r\n" (output "DELETE" ["foo" "another"] 0  {"foo" {:value "bar"} "another" {:value "something"}} {}))))
  (testing "INCR"
    (is (= {} (update-store "INCR" [] 0 {})))
    (is (= "-Error nothing to increment\r\n" (output "INCR" [] 0 {} {})))
    (is (= {"foo" {:val "1"}} (update-store "INCR" ["foo"] 0 {})))
    (is (= ":1\r\n" (output "INCR" ["foo"] 0 {} {"foo" {:val "1"}})))
    (is (= {"baz" {:val "91"}} (update-store "INCR" ["baz"] 0 {"baz" {:val "90"}})))
    (is (= ":91\r\n" (output "INCR" ["baz"] 0 {"baz" {:val "90"}} {"baz" {:val "91"}}))))
  (testing "DECR"
    (is (= "-Error nothing to decrement\r\n" (output "DECR" [] 0 {} {})))
    (is (= {"foo" {:val "-1"}} (update-store "DECR" ["foo"] 0 {})))
    (is (= ":-1\r\n" (output "DECR" ["foo"] 0 {} {"foo" {:val "-1"}})))
    (is (= {"baz" {:val "89"}} (update-store "DECR" ["baz"] 0 {"baz" {:val "90"}})))
    (is (= ":89\r\n" (output "DECR" ["baz"] 0 {"baz" {:val "90"}} {"baz" {:val "89"}}))))
  (testing "LPUSH"
    (is (= "-Error nothing to push\r\n" (output "LPUSH" [] 0 {} {})))
    (is (= {"foo" {:val ["bar"]}} (update-store "LPUSH" ["foo" "bar"] 0 {})))
    (is (= ":1\r\n" (output "LPUSH" ["foo" "bar"] 0 {} {"foo" {:val ["bar"]}})))
    (is (= {"foo" {:val ["baz" "bar"]}} (update-store "LPUSH" ["foo" "bar" "baz"] 0 {})))
    (is (= ":2\r\n" (output "LPUSH" ["foo" "bar" "baz"] 0 {} {"foo" {:val ["baz" "bar"]}}))))
  (testing "RPUSH"
    (is (= "-Error nothing to push\r\n" (output "RPUSH" [] 0 {} {})))
    (is (= {"foo" {:val ["bar"]}} (update-store "RPUSH" ["foo" "bar"] 0 {})))
    (is (= ":1\r\n" (output "RPUSH" ["foo" "bar"] 0 {} {"foo" {:val ["bar"]}})))
    (is (= {"foo" {:val ["bar" "baz"]}} (update-store "RPUSH" ["foo" "bar" "baz"] 0 {})))
    (is (= ":2\r\n" (output "RPUSH" ["foo" "bar" "baz"] 0 {} {"foo" {:val ["bar" "baz"]}})))))
