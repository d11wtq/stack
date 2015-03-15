(ns stack.aws.cloudformation-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.aws.cloudformation :refer [expand-parameters
                                              apply-stack
                                              deploy-stack
                                              list-stack-events-fn
                                              stack-events-seq-fn]])
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

(deftest list-stack-events-test
  (testing "#'list-stack-events"
    (testing "lists events"
      (let [events-fn (bond/spy (constantly nil))]
        ((list-stack-events-fn :events-fn events-fn) "example")
        (is (= (-> (bond/calls events-fn) first :args)
               [{:stack-name "example"}]))))

    (testing "returns all events in reverse order"
      (let [events-fn (fn [{:keys [next-token]}]
                        (if (= "test-token" next-token)
                          {:stack-events [:c :b :a]}
                          {:stack-events [:f :e :d]
                           :next-token "test-token"}))]
        (is (= [:a :b :c :d :e :f]
               ((list-stack-events-fn :events-fn events-fn) "example")))))))

(deftest stack-events-seq-test
  (testing "#'stack-events-seq"
    (testing "with :follow false"
      (testing "applies list-fn with stack-name and returns the result"
        (let [list-fn (bond/spy (constantly ["a" "b" "c"]))
              sleep-fn (constantly nil)
              result ((stack-events-seq-fn
                        :list-fn list-fn
                        :sleep-fn sleep-fn) "example" :follow false)]
          (is (= ["a" "b" "c"] result))
          (is (= (-> (bond/calls list-fn) first :args)
                 ["example"])))))

    (testing "with :follow true"
      (testing "applies distinct items from successive calls to list-fn"
        (let [items (atom (list ["a" "b" "c"]
                                ["c" "d" "e"]
                                ["d" "e" "f"]
                                ["g" "h" "i"]))
              list-fn (bond/spy (fn [s]
                                  (let [v (first @items)]
                                    (swap! items pop)
                                    v)))
              sleep-fn (constantly nil)
              result ((stack-events-seq-fn
                        :list-fn list-fn
                        :sleep-fn sleep-fn) "example" :follow true)]
          (is (= ["a" "b" "c" "d" "e" "f"]
                 (take 6 result))))))))
