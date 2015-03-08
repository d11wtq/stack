(ns stack.core-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.core :as core]))

(deftest make-dispatch-fn-test
  (testing "#'make-dispatch-fn"
    (testing "with a valid sub-command"
      (testing "applies the remaining args to the command"
        (let [foo (bond/spy (constantly nil))
              bar (bond/spy (constantly nil))
              dispatch (core/make-dispatch-fn {:foo foo, :bar bar}
                                              (constantly nil))]
          (dispatch "foo" "a" "b")
          (is (= (-> (bond/calls foo) first :args)
                 ["a" "b"])))))

    (testing "with an invalid sub-command"
      (testing "applies error-fn"
        (let [foo (constantly nil)
              error-fn (bond/spy (constantly nil))
              dispatch (core/make-dispatch-fn {:foo foo}
                                              error-fn)]
          (dispatch "boo" "a" "b")
          (is (= (-> (bond/calls error-fn) count)
                 1)))))))
