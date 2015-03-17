(ns stack.commands.signal-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.commands.signal :as signal]))

(deftest instance-states-seq-test
  (testing "#'instance-states-seq"
    (testing "applies physical-id-fn with stack-name and elb"
      (let [seq-fn (constantly nil)
            physical-id-fn (bond/spy (constantly nil))]
        (signal/instance-states-seq {:seq-fn seq-fn
                                     :physical-id-fn physical-id-fn}
                                    "example"
                                    "loadBalancer")
        (is (= (-> (bond/calls physical-id-fn) first :args)
               ["example" "loadBalancer"]))))

    (testing "applies seq-fn with the result of physical-id-fn"
      (let [seq-fn (bond/spy (constantly nil))
            physical-id-fn (constantly "elb:1234/something")]
        (signal/instance-states-seq {:seq-fn seq-fn
                                     :physical-id-fn physical-id-fn}
                                    "example" "loadBalancer")
        (is (= (-> (bond/calls seq-fn) first :args)
               ["elb:1234/something"]))))

    (testing "returns the result of seq-fn"
      (let [seq-fn (constantly [{:instance-id "i-abc123"
                                 :state "InService"}])
            physical-id-fn (constantly nil)]
        (is (= (signal/instance-states-seq {:seq-fn seq-fn
                                             :physical-id-fn physical-id-fn}
               "example" "loadBalancer")
               [{:instance-id "i-abc123", :state "InService"}]))))))

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
