(ns stack.aws.cloudformation-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.aws.cloudformation :refer [expand-parameters
                                              apply-stack
                                              deploy-stack]])
  (:import [com.amazonaws.services.cloudformation.model
            AlreadyExistsException]))

(deftest expand-parameters-test
  (testing "#'expand-parameters"
    (testing "expands a map to a list of key-value pairs"
      (is (= [{:parameter-key "foo", :parameter-value "bar"}
              {:parameter-key "zip", :parameter-value "bob"}]
             (expand-parameters (sorted-map :foo "bar"
                                            :zip "bob")))))))

(deftest apply-stack-test
  (testing "#'apply-stack"
    (let [payload {:stack-name "example"
                   :template-body "{}"
                   :parameters []}]
      (testing "applies create-fn with payload"
        (let [create-fn (bond/spy (constantly nil))
              update-fn (constantly nil)]
          (apply-stack {:create-fn create-fn
                        :update-fn update-fn}
                       payload)
          (is (= (-> (bond/calls create-fn) first :args)
                 [payload]))))

      (testing "when the stack already exists"
        (testing "applies update-fn with payload"
          (let [create-fn (fn [& args] (throw (AlreadyExistsException. "test")))
                update-fn (bond/spy (constantly nil))]
            (apply-stack {:create-fn create-fn
                          :update-fn update-fn}
                         payload)
            (is (= (-> (bond/calls update-fn) first :args)
                 [payload]))))))))

(deftest deploy-stack-test
  (testing "#'deploy-stack"
    (testing "applies apply-fn with a constructed payload"
      (let [apply-fn (bond/spy (constantly nil))]
        (deploy-stack {:apply-fn apply-fn}
                      "example"
                      {:Description "Example"}
                      {:amiId "ami-abc123"})
        (is (= (-> (bond/calls apply-fn) first :args)
               [{:stack-name "example"
                 :template-body "{\"Description\":\"Example\"}"
                 :parameters [{:parameter-key "amiId"
                               :parameter-value "ami-abc123"}]}]))))))
