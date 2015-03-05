(ns stack.util-test
  (:require [clojure.test :refer :all]
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
