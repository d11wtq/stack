(ns stack.commands.signal-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.commands.signal :as signal]))

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              instance-states-fn (constantly nil)
              signal-fn (constantly nil)]
          (signal/action {:error-fn error-fn
                          :instance-states-fn instance-states-fn
                          :signal-fn signal-fn}
                         (vector)
                         (hash-map))
          (is (re-find #"<stack-name>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "without a <elb>:<asg>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              instance-states-fn (constantly nil)
              signal-fn (constantly nil)]
          (signal/action {:error-fn error-fn
                          :instance-states-fn instance-states-fn
                          :signal-fn signal-fn}
                         ["example"]
                         (hash-map))
          (is (re-find #"<elb>:<asg>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "with malformed <elb>:<asg>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              instance-states-fn (constantly nil)
              signal-fn (constantly nil)]
          (signal/action {:error-fn error-fn
                          :instance-states-fn instance-states-fn
                          :signal-fn signal-fn}
                         ["example" "lbName:" "asgName"]
                         (hash-map))
          (is (re-find #"<elb>:<asg>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "with a <stack-name> and <elb>:<asg>"
      (testing "applies instance-states-fn with stack-name and elb"
        (let [error-fn (constantly nil)
              instance-states-fn (bond/spy (constantly nil))
              signal-fn (constantly nil)]
          (signal/action {:error-fn error-fn
                          :instance-states-fn instance-states-fn
                          :signal-fn signal-fn}
                         ["example" "elbName:asgName"]
                         (hash-map))
          (is (= (-> (bond/calls instance-states-fn) first :args)
                 ["example" "elbName"])))))))
