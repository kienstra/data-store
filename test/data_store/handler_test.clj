(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [handler]]))

(deftest handler-test
  (testing "Invalid command"
    (is (= [{} "-Error invalid command\r\n"] (handler {} ["*1" "$12" "DOESNOTEXIST"]))))
  (testing "PING"
    (is (= [{} "$4\r\nPONG\r\n"] (handler {} ["*1" "$4" "PING"]))))
  (testing "ECHO"
    (is (= [{} "$11\r\nHello World\r\n"] (handler {} ["*2" "$4" "ECHO" "$11" "Hello World"])))
    (is (= [{} "$6\r\nSimple\r\n"] (handler {} ["*2" "$4" "ECHO" "$6" "Simple"])))
    (is (= [{} "$22\r\nSeveral Words Together\r\n"] (handler {} ["*2" "$4" "ECHO" "$22" "Several Words Together"])))
    (is (= [{} "-Error nothing to echo\r\n"] (handler {} ["*2" "$4" "ECHO"]))))
  (testing "SET"
    (is (= [{} "-Error nothing to set\r\n"] (handler {} ["*2" "$4" "SET" "$4" "Name"])))
    (is (= [{"Name" "John"} "$2\r\nOK\r\n"] (handler {} ["*2" "$4" "SET" "$4" "Name" "$4" "John"])))
    (is (= [{"Name" "Renamed"} "$2\r\nOK\r\n"] (handler {"Name" "John"} ["*2" "$4" "SET" "$4" "Name" "$4" "Renamed"]))))
  (testing "GET"
    (is (= [{} "-Error nothing to get\r\n"] (handler {} ["*1" "$4" "GET"])))
    (is (= [{} "-Error key not found\r\n"] (handler {} ["*2" "$4" "GET" "$4" "Name"])))
    (is (= [{"Name" "John"} "$4\r\nJohn\r\n"] (handler {"Name" "John"} ["*2" "$4" "GET" "$4" "Name"])))))
