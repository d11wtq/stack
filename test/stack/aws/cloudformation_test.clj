(ns stack.aws.cloudformation-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.aws.cloudformation :refer [apply-stack-fn
                                              deploy-stack-fn
                                              list-stack-events-fn
                                              stack-events-seq-fn
                                              physical-resource-id-fn
                                              wait-for-resource-fn]])
  (:import [com.amazonaws.services.cloudformation.model
            AlreadyExistsException]))

(deftest apply-stack-test
  (testing "#'apply-stack"
    (let [payload {:stack-name "example"
                   :template-body "{}"
                   :parameters []}]
      (testing "applies create-fn with payload"
        (let [create-fn (bond/spy (constantly nil))
              update-fn (constantly nil)]
          ((apply-stack-fn :create-fn create-fn
                           :update-fn update-fn) payload)
          (is (= (-> (bond/calls create-fn) first :args)
                 [payload]))))

      (testing "when the stack already exists"
        (testing "applies update-fn with payload"
          (let [create-fn (fn [& args] (throw (AlreadyExistsException. "test")))
                update-fn (bond/spy (constantly nil))]
            ((apply-stack-fn :create-fn create-fn
                             :update-fn update-fn) payload)
            (is (= (-> (bond/calls update-fn) first :args)
                 [payload]))))))))

(deftest deploy-stack-test
  (testing "#'deploy-stack"
    (testing "applies apply-fn with a constructed payload"
      (let [apply-fn (bond/spy (constantly nil))]
        ((deploy-stack-fn :apply-fn apply-fn)
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

(deftest physical-resource-id-test
  (testing "#'physical-resource-id"
    (testing "gets stack resource detail"
      (let [stack-resource-fn (bond/spy (constantly nil))]
        ((physical-resource-id-fn :stack-resource-fn stack-resource-fn)
         "example" "logical-id")
        (is (= (-> (bond/calls stack-resource-fn) first :args)
               [{:stack-name "example"
                 :logical-resource-id "logical-id"}]))))

    (testing "extracts the physical-resource-id"
      (let [stack-resource-fn (constantly
                                {:stack-resource-detail
                                 {:physical-resource-id "physical-id"}})]
        (is (= "physical-id"
               ((physical-resource-id-fn :stack-resource-fn stack-resource-fn)
                "example" "logical-id")))))))

(deftest wait-for-resource-test
  (testing "#'wait-for-resource"
    (testing "gets the physical-id of the resource"
      (let [physical-id-fn (bond/spy (constantly "physical-id"))
            sleep-fn (constantly nil)]
        ((wait-for-resource-fn :physical-id-fn physical-id-fn
                               :sleep-fn sleep-fn)
         "example" "logical-id")
        (is (= (-> (bond/calls physical-id-fn) first :args)
               ["example" "logical-id"]))))

    (testing "when the resource doesn't exist"
      (let [values (atom (list nil nil "physical-id"))
            physical-id-fn (fn [& args] (let [v (first @values)]
                                          (swap! values pop)
                                          v))
            sleep-fn (bond/spy (constantly nil))]
        (let [physical-id ((wait-for-resource-fn
                             :physical-id-fn physical-id-fn
                             :sleep-fn sleep-fn)
                           "example" "logical-id")]

          (testing "applies sleep-fn"
            (is (= (-> (bond/calls sleep-fn) count) 2)))

          (testing "retries until a value is returned"
            (is (= "physical-id" physical-id))))))))
