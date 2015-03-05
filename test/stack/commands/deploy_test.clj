(ns stack.commands.deploy-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.commands.deploy :as deploy]))

(deftest dispatch-test
  (testing "#'dispatch"
    (testing "parses & args with parse-fn"
      (let [parse-fn (bond/spy (constantly nil))
            handler-fn (constantly nil)]
        (deploy/dispatch {:parse-fn parse-fn
                          :handler-fn handler-fn}
                         "a" "b")
        (is (= (-> (bond/calls parse-fn) first :args first)
               ["a" "b"]))))

    (testing "delegates to handler-fn with parsed args"
      (let [parsed-args {:arguments [], :options {}, :summary " -h Help"}
            parse-fn (constantly parsed-args)
            handler-fn (bond/spy (constantly nil))]
        (deploy/dispatch {:parse-fn parse-fn
                          :handler-fn handler-fn}
                         "a" "b")
        (is (= (-> (bond/calls handler-fn) first :args)
               [parsed-args]))))))

(deftest handle-args-test
  (testing "#'handle-args"
    (testing "with errors"
      (testing "invokes error-fn with the first error"
        (let [error-fn (bond/spy (constantly nil))
              usage-fn (constantly nil)
              action-fn (constantly nil)]
          (deploy/handle-args {:error-fn error-fn
                               :action-fn action-fn
                               :usage-fn usage-fn}
                              {:errors ["error one"
                                        "error two"]})
          (is (= (-> (bond/calls error-fn) first :args)
                 ["error one"])))))

    (testing "with :help"
      (testing "dispatches to usage-fn with summary"
        (let [error-fn (constantly nil)
              usage-fn (bond/spy (constantly nil))
              action-fn (constantly nil)]
          (deploy/handle-args {:error-fn error-fn
                               :action-fn action-fn
                               :usage-fn usage-fn}
                              {:options {:help true}
                               :summary "test"})
          (is (= (-> (bond/calls usage-fn) first :args)
                 ["test"])))))

    (testing "with a stack-name and template"
      (testing "dispatches to action-fn with arguments and options"
        (let [error-fn (constantly nil)
              usage-fn (constantly nil)
              action-fn (bond/spy (constantly nil))]
          (deploy/handle-args {:error-fn error-fn
                               :action-fn action-fn
                               :usage-fn usage-fn}
                              {:arguments ["example" "stack.json"]
                               :options {}})
          (is (= (->> (bond/calls action-fn) first :args (drop 1))
                 [["example" "stack.json"] {}])))))))
