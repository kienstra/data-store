(ns data-store.expiration-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.expiration :refer [expired-keys expire-n has-exp]]))

(deftest expiration-test
  (testing "Expired"
    (is (= #{} (expired-keys {} 10)))
    (is (= #{} (expired-keys {"Name" {:val "John" :exp 9} "Another" {:val "Something"}} 10)))
    (is (= #{"Name"} (expired-keys {"Name" {:val "John" :exp 10} "Another" {:val "Something"}} 10)))
    (is (= #{"Name" "Another"} (expired-keys {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10}} 10))))
  (testing "Store with exp"
    (is (= {} (has-exp {})))
    (is (= {} (has-exp {"Name" {:val "John"}})))
    (is (= {"Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John"} "Another" {:val "John" :exp 10}})))
    (is (= {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}}))))
  (testing "Expire n"
    (is (= {} (expire-n {} 0 0)))
    (is (= {} (expire-n {"Name" {:val "John" :exp 10} "Another" {:val "John" :exp 10}} 10 2)))))
