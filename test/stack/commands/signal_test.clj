(ns stack.commands.signal-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [bond.james :as bond]
            [stack.commands.signal :as signal]))

(deftest report-instance-state-test
  (testing "#'report-instance-state"
    (testing "prints 'Instance <id> is now <state>'"
      (let [output (with-out-str
                     (signal/report-instance-state {:instance-id "i-abc123"
                                                    :state "InService"}))]
        (is (= "Instance i-abc123 is now InService"
               (string/trim output)))))))

(deftest handle-instance-state-test
  (testing "#'handle-instance-state"
    (testing "applies report-fn with the instance state"
      (let [report-fn (bond/spy (constantly nil))
            signal-fn (constantly nil)]
        ((signal/handle-instance-state-fn
           :report-fn report-fn
           :signal-fn signal-fn)
         "example"
         "asg"
         {:instance-id "i-abc123", :state "InService"})
        (is (= (-> (bond/calls report-fn) first :args)
               [{:instance-id "i-abc123", :state "InService"}])))

      (testing "for an InService instance"
        (testing "applies signal-fn for the ASG"
          (let [report-fn (constantly nil)
                signal-fn (bond/spy (constantly nil))]
            ((signal/handle-instance-state-fn
               :report-fn report-fn
               :signal-fn signal-fn)
             "example"
             "asg"
             {:instance-id "i-abc123", :state "InService"})
            (is (= (-> (bond/calls signal-fn) first :args)
                   ["example" "asg" "i-abc123"])))))

      (testing "for an OutOfService instance"
        (testing "does not apply signal-fn"
          (let [report-fn (constantly nil)
                signal-fn (bond/spy (constantly nil))]
            ((signal/handle-instance-state-fn
               :report-fn report-fn
               :signal-fn signal-fn)
             "example"
             "asg"
             {:instance-id "i-abc123", :state "OutOfService"})
            (is (= (-> (bond/calls signal-fn) count) 0))))))))

(deftest instance-states-seq-test
  (testing "#'instance-states-seq"
    (testing "applies physical-id-fn with stack-name and elb"
      (let [seq-fn (constantly nil)
            physical-id-fn (bond/spy (constantly nil))]
        ((signal/instance-states-seq-fn
          :seq-fn seq-fn
          :physical-id-fn physical-id-fn)
         "example" "loadBalancer")
        (is (= (-> (bond/calls physical-id-fn) first :args)
               ["example" "loadBalancer"]))))

    (testing "applies seq-fn with the result of physical-id-fn"
      (let [seq-fn (bond/spy (constantly nil))
            physical-id-fn (constantly "elb:1234/something")]
        ((signal/instance-states-seq-fn
           :seq-fn seq-fn
           :physical-id-fn physical-id-fn)
         "example" "loadBalancer")
        (is (= (-> (bond/calls seq-fn) first :args)
               ["elb:1234/something"]))))

    (testing "returns the result of seq-fn"
      (let [seq-fn (constantly [{:instance-id "i-abc123"
                                 :state "InService"}])
            physical-id-fn (constantly nil)]
        (is (= ((signal/instance-states-seq-fn
                  :seq-fn seq-fn
                  :physical-id-fn physical-id-fn)
                "example" "loadBalancer")
               [{:instance-id "i-abc123", :state "InService"}]))))))

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              instance-states-fn (constantly nil)
              signal-fn (constantly nil)]
          ((signal/action-fn
             :error-fn error-fn
             :instance-states-fn instance-states-fn
             :signal-fn signal-fn)
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
          ((signal/action-fn
             :error-fn error-fn
             :instance-states-fn instance-states-fn
             :signal-fn signal-fn)
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
          ((signal/action-fn
             :error-fn error-fn
             :instance-states-fn instance-states-fn
             :signal-fn signal-fn)
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
              handler-fn (constantly nil)]
          ((signal/action-fn
             :error-fn error-fn
             :instance-states-fn instance-states-fn
             :handler-fn handler-fn)
           ["example" "elbName:asgName"]
           (hash-map))
          (is (= (-> (bond/calls instance-states-fn) first :args)
                 ["example" "elbName"]))))

      (testing "apples handler-fn for each instance state"
        (let [error-fn (constantly nil)
              instance-states-fn (constantly [{:instance-id "i-abc123"
                                               :state "InService"}])
              handler-fn (bond/spy (constantly nil))]
          ((signal/action-fn
             :error-fn error-fn
             :instance-states-fn instance-states-fn
             :handler-fn handler-fn)
           ["example" "elbName:asgName"]
           (hash-map))
          (is (= (-> (bond/calls handler-fn) first :args)
                 ["example" "asgName" {:instance-id "i-abc123"
                                       :state "InService"}])))))))
