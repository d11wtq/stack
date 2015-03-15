(ns stack.util-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.util :as util]))

(deftest error-fn-test
  (testing "#'error-fn"
    (testing "throws an Exception"
      (is (thrown? Exception (util/error-fn "test"))))))

(deftest make-print-usage-fn-test
  (testing "#'make-print-usage-fn"
    (testing "returns a function that prints usage"
      (let [summary "  -h, --help  Show this help"
            usage (fn [s] (str "Options:\n" s))]
        (is (= (with-out-str
                 ((util/make-print-usage-fn usage)
                  summary))
               (str "Options:\n" summary "\n")))))))

(deftest make-validate-fn
  (testing "#'make-validate-fn"
    (testing "passes [arguments options] to each validator"
      (let [validator1 (bond/spy (constantly nil))
            validator2 (bond/spy (constantly nil))]
        ((util/make-validate-fn [validator1 validator2])
         ["a" "b"] {:c true})
        (are [f] (= (-> (bond/calls f) first :args)
                    [["a" "b"] {:c true}])
             validator1
             validator2)))

    (testing "returns the first error"
      (let [validator1 (constantly nil)
            validator2 (constantly "test")
            validator3 (constantly "other")]
        (is (= ((util/make-validate-fn [validator1 validator2 validator3])
                ["a" "b"] {:c true})
               "test"))))))
