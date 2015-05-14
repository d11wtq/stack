(ns stack.commands.delete-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.commands.delete :as delete]))

(deftest dispatch-events-test
  (testing "#'dispatch-events"
    (testing "applies events-fn with stack-name --follow --update"
      (let [events-fn (bond/spy (constantly nil))]
        ((delete/dispatch-events-fn :events-fn events-fn)
         ["example"]
         (hash-map))
        (is (= (-> (bond/calls events-fn) first :args)
               ["example" "--follow" "--update"]))))))

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              destroy-fn (constantly nil)
              after-fn (constantly nil)]
          ((delete/action-fn
             :error-fn error-fn
             :destroy-fn destroy-fn
             :after-fn after-fn)
           (vector)
           (hash-map))
          (is (re-find #"<stack-name>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "with a <stack-name>"
      (testing "applies destroy-fn"
        (let [error-fn (constantly nil)
              destroy-fn (bond/spy (constantly nil))
              after-fn (constantly nil)]
          ((delete/action-fn
             :error-fn error-fn
             :destroy-fn destroy-fn
             :after-fn after-fn)
           ["example-stack"]
           (hash-map))
          (is (= (-> (bond/calls destroy-fn) first :args)
                 ["example-stack"]))))

      (testing "applies after-fn with arguments and options"
        (let [error-fn (constantly nil)
              destroy-fn (constantly nil)
              after-fn (bond/spy (constantly nil))]
          ((delete/action-fn
             :error-fn error-fn
             :destroy-fn destroy-fn
             :after-fn after-fn)
           ["example-stack"]
           (hash-map))
          (is (= (-> (bond/calls after-fn) first :args)
                 [["example-stack"] (hash-map)])))))))
