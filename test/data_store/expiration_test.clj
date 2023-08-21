(ns data-store.expiration-test
  (:require [clojure.test :refer [deftest is testing]]
            [data-store.expiration :refer [expired expire-n has-exp]]))

(deftest expiration-test
  (testing "Gets keys that have expired"
    (is (= #{} (expired {} 10)))
    (is (= #{} (expired {"Name" {:val "John" :exp 9} "Another" {:val "Something"}} 10)))
    (is (= #{"Name"} (expired {"Name" {:val "John" :exp 10} "Another" {:val "Something"}} 10)))
    (is (= #{"Name" "Another"} (expired {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10}} 10))))
  (testing "Gets keys with any expiration, even if not expired"
    (is (= {} (has-exp {})))
    (is (= {} (has-exp {"Name" {:val "John"}})))
    (is (= {"Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John"} "Another" {:val "John" :exp 10}})))
    (is (= {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}} (has-exp {"Name" {:val "John" :exp 19} "Another" {:val "John" :exp 10}}))))
  (testing "Continues expiring n sample of keys until only <= 25% of the sample are expired"
    (is (= {} (expire-n {} 0 0)))
    (is (= {} (expire-n {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10}} 10 2)))
    (is (= {} (expire-n {"Name" {:val "John" :exp 10} "Another" {:val "Something" :exp 10} "Baz" {:val "This" :exp 10}} 10 2)))))
