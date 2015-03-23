(ns stack.commands.deploy-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [clojure.data.json :as json]
            [stack.commands.deploy :as deploy]))

(def sample-template-path
  "tmp/sample-template.json")

(def sample-template
  {:AWSTemplateFormatVersion "2010-09-09"
   :Description "Example"
   :Parameters {:dockerImage {:Type "String"}
                :hostPort {:Type "Number"}}})

(def sample-params-path
  "tmp/sample-params.json")

(def sample-params
  {:dockerImage "d11wtq/example:latest"
   :hostPort "80"})

(use-fixtures
  :once
  (fn [f]
    (spit sample-template-path
          (json/write-str sample-template))

    (spit sample-params-path
          (json/write-str sample-params))
    (f)))

(deftest dispatch-events-test
  (testing "#'dispatch-events"
    (testing "applies events-fn with stack-name --follow"
      (let [events-fn (bond/spy (constantly nil))]
        ((deploy/dispatch-events-fn :events-fn events-fn)
         ["example" "template.json"] {:params "params.json"})
        (is (= (-> (bond/calls events-fn) first :args)
               ["example" "--follow"]))))))

(deftest dispatch-signal-test
  (testing "#'dispatch-signal"
    (testing "without --signal"
      (testing "does nothing"
        (let [signal-fn (bond/spy (constantly nil))]
          ((deploy/dispatch-signal-fn :signal-fn signal-fn)
           ["example" "template.json"] {})
          (is (= (-> (bond/calls signal-fn) count) 0)))))

    (testing "with --signal"
      (testing "applies signal-fn with stack-name signal"
        (let [signal-fn (bond/spy (constantly nil))]
          ((deploy/dispatch-signal-fn :signal-fn signal-fn)
           ["example" "template.json"] {:signal "elb:asg"})
          (is (= (-> (bond/calls signal-fn) first :args)
                 ["example" "elb:asg"])))))))

(deftest dispatch-wait-test
  (testing "#'dispatch-wait"
    (testing "applies wait-fn with stack-name"
      (let [wait-fn (bond/spy (constantly nil))]
        ((deploy/dispatch-wait-fn :wait-fn wait-fn)
         ["example" "template.json"] (hash-map))
        (is (= (-> (bond/calls wait-fn) first :args)
               ["example"]))))))

(deftest dispatch-parallel-actions-test
  (testing "#'dispatch-parallel-actions"
    (testing "applies each action"
      (let [action-a (bond/spy (constantly nil))
            action-b (bond/spy (fn [& args] (Thread/sleep 500)))]
        ((deploy/dispatch-parallel-actions-fn
           :actions [action-a action-b])
         ["example" "stack.json"] {:params "params.json"})
        (is (= (->> (bond/calls action-a) first :args)
               [["example" "stack.json"]
                {:params "params.json"}])))

      (let [action-a (bond/spy (fn [& args] (Thread/sleep 500)))
            action-b (bond/spy (constantly nil))]
        ((deploy/dispatch-parallel-actions-fn
           :actions [action-a action-b])
         ["example" "stack.json"] {:params "params.json"})
        (is (= (->> (bond/calls action-b) first :args)
               [["example" "stack.json"]
                {:params "params.json"}]))))

    (testing "on error"
      (testing "throws the error"
        (let [action-a (bond/spy (fn [& args] (throw (Exception.))))
              action-b (bond/spy (fn [& args] (Thread/sleep 500)))]
          (is (thrown? Exception
                       ((deploy/dispatch-parallel-actions-fn
                          :actions [action-a action-b])
                        ["example" "stack.json"] {:params "params.json"}))))))))

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              deploy-fn (constantly nil)
              after-fn (constantly nil)]
          ((deploy/action-fn
             :error-fn error-fn
             :deploy-fn deploy-fn
             :after-fn after-fn)
           (vector)
           (hash-map))
          (is (re-find #"<stack-name>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "without a <template>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              deploy-fn (constantly nil)
              after-fn (constantly nil)]
          ((deploy/action-fn
             :error-fn error-fn
             :deploy-fn deploy-fn
             :after-fn after-fn)
           ["example-stack"]
           (hash-map))
          (is (re-find #"<template>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "with a <stack-name> and <template>"
      (testing "without --params"
        (testing "applies deploy-fn with empty params"
          (let [error-fn (constantly nil)
                deploy-fn (bond/spy (constantly nil))
                after-fn (constantly nil)]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn
               :after-fn after-fn)
             ["example-stack" sample-template-path]
             (hash-map))
            (is (= (-> (bond/calls deploy-fn) first :args)
                   ["example-stack"
                    sample-template
                    (hash-map)])))))

      (testing "with --params"
        (testing "applies deploy-fn with file contents"
          (let [error-fn (constantly nil)
                deploy-fn (bond/spy (constantly nil))
                after-fn (constantly nil)]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn
               :after-fn after-fn)
             ["example-stack" sample-template-path]
             {:params sample-params-path})
            (is (= (-> (bond/calls deploy-fn) first :args)
                   ["example-stack"
                    sample-template
                    sample-params])))))

      (testing "with key=value overrides"
        (testing "applies deploy-fn with merged params"
          (let [error-fn (constantly nil)
                deploy-fn (bond/spy (constantly nil))
                after-fn (constantly nil)]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn
               :after-fn after-fn)
             ["example-stack"
              sample-template-path
              "hostPort=81"]
             {:params sample-params-path})
            (is (= (-> (bond/calls deploy-fn) first :args)
                   ["example-stack"
                    sample-template
                    (merge sample-params
                           {:hostPort "81"})]))))

        (testing "with invalid syntax"
          (testing "applies error-fn"
            (let [error-fn (bond/spy (constantly nil))
                  deploy-fn (constantly nil)
                  after-fn (constantly nil)]
              ((deploy/action-fn
                 :error-fn error-fn
                 :deploy-fn deploy-fn
                 :after-fn after-fn)
               ["example-stack"
                sample-template-path
                "hostPort" " =81"]
               {:params sample-params-path})
              (is (re-find #"hostPort"
                           (-> (bond/calls error-fn)
                               first
                               :args
                               first)))))))

      (testing "applies after-fn with arguments and options"
        (let [error-fn (constantly nil)
              deploy-fn (constantly nil)
              after-fn (bond/spy (constantly nil))]
          ((deploy/action-fn
             :error-fn error-fn
             :deploy-fn deploy-fn
             :after-fn after-fn)
           ["example-stack" sample-template-path]
           (hash-map))
          (is (= (-> (bond/calls after-fn) first :args)
                 [["example-stack" sample-template-path]
                  (hash-map)])))))))
