(ns data-store.expiration-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.expiration :refer [expired expire-n has-exp]]))

(deftest expiration-test
  (testing "Get keys that have expired"
    (is (= #{} (expired 10 {})))
    (is (= #{"Name"} (expired 10 {"Name" {:val "John" :exp 9} "Another" {:val "Something"}})))
    (is (= #{"Name"} (expired 10 {"Name" {:val "John" :exp 10} "Another" {:val "Something"}})))
    (is (= #{"Name" "Another"} (expired 10 {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10}}))))
  (testing "Get keys with any expiration, even if not expired"
    (is (= {} (has-exp {})))
    (is (= {} (has-exp {"Name" {:val "John"}})))
    (is (= {"Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John"} "Another" {:val "John" :exp 10}})))
    (is (= {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}}))))
  (testing "Continue expiring n sample of keys until only <= 25% of the sample are expired"
    (is (= {} (expire-n {} 0 0)))
    (is (= {} (expire-n {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10}} 10 2)))
    (is (= {} (expire-n {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10} "Baz" {:val "This" :exp 10}} 10 2)))))
