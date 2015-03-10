(ns stack.params-test
  (:require [clojure.test :refer :all]
            [stack.params :refer [parse-params]]))

(deftest parse-params-test
  (testing "given an an array of key-value pairs"
    (testing "returns a map with the result in :params"
      (let [result (parse-params ["foo=bar" "baz=barry"])]
        (is (= {:foo "bar"
                :baz "barry"}
               (:params result))))))

  (testing "given an empty array"
    (testing "returns an empty map"
      (is (= {} (:params (parse-params []))))))

  (testing "given an array with an invalid key-value pair"
    (testing "returns a map with an item in :errors"
      (let [result (parse-params ["foo-bar"])]
        (is (= 1 (count (:errors result)))))))

  (testing "given an array with some valid and invalid pairs"
    (testing "returns a map with both :params and :errors"
      (let [result (parse-params ["foo=bar" "zip-button"])]
        (is (= {:foo "bar"} (:params result)))
        (is (= 1 (count (:errors result))))))))
