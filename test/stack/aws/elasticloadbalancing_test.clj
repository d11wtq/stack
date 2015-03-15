(ns stack.aws.elasticloadbalancing-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.aws.elasticloadbalancing :refer [list-instance-states-fn]]))

(deftest list-instance-states-test
  (testing "#'list-instance-states"
    (testing "lists instance states"
      (let [health-fn (bond/spy (constantly nil))]
        ((list-instance-states-fn :health-fn health-fn) "example")
        (is (= (-> (bond/calls health-fn) first :args)
               [{:load-balancer-name "example"}]))))

    (testing "returns all instance states"
      (let [health-fn (constantly
                        {:instance-states [{:instance-id "i-abc123"
                                            :reason "Example"
                                            :state "InService"}
                                           {:instance-id "i-def456"
                                            :reason "Example"
                                            :state "OutOfService"}]})]
        (is (= [{:instance-id "i-abc123" :state "InService"}
                {:instance-id "i-def456" :state "OutOfService"}]
               ((list-instance-states-fn :health-fn health-fn) "example")))))))
