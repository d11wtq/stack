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

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              deploy-fn (constantly nil)]
          ((deploy/action-fn
             :error-fn error-fn
             :deploy-fn deploy-fn)
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
              deploy-fn (constantly nil)]
          ((deploy/action-fn
             :error-fn error-fn
             :deploy-fn deploy-fn)
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
                deploy-fn (bond/spy (constantly nil))]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn)
             ["example-stack" sample-template-path]
             (hash-map))
            (is (= (-> (bond/calls deploy-fn) first :args)
                   ["example-stack"
                    sample-template
                    (hash-map)])))))

      (testing "with --params"
        (testing "applies deploy-fn with file contents"
          (let [error-fn (constantly nil)
                deploy-fn (bond/spy (constantly nil))]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn)
             ["example-stack" sample-template-path]
             {:params sample-params-path})
            (is (= (-> (bond/calls deploy-fn) first :args)
                   ["example-stack"
                    sample-template
                    sample-params])))))

      (testing "with key=value overrides"
        (testing "applies deploy-fn with merged params"
          (let [error-fn (constantly nil)
                deploy-fn (bond/spy (constantly nil))]
            ((deploy/action-fn
               :error-fn error-fn
               :deploy-fn deploy-fn)
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
                  deploy-fn (constantly nil)]
              ((deploy/action-fn
                 :error-fn error-fn
                 :deploy-fn deploy-fn)
               ["example-stack"
                sample-template-path
                "hostPort" " =81"]
               {:params sample-params-path})
              (is (re-find #"hostPort"
                           (-> (bond/calls error-fn)
                               first
                               :args
                               first))))))))))
