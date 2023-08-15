(ns data-store.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.handler :refer [handler]]))

(deftest handler-test
  (testing "Handler"
    (is (= "-Error no command \r\n" (handler [])))
    (is (= "$4\r\nPONG\r\n" (handler ["*1" "$4" "PING"])))
    (is (= "$11\r\nHello World\r\n" (handler ["*2" "$4" "ECHO" "$11" "Hello World"])))
    (is (= "$6\r\nSimple\r\n" (handler ["*2" "$4" "ECHO" "$6" "Simple"])))
    (is (= "$22\r\nSeveral Words Together\r\n" (handler ["*2" "$4" "ECHO" "$22" "Several Words Together"])))
    (is (= "$-1\r\n" (handler ["*2" "$4" "ECHO"])))))
