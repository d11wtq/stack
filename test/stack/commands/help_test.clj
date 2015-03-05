(ns stack.commands.help-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond]
            [stack.commands.help :as help]))

(deftest action-test
  (testing "#'action"
    (testing "prints a program usage: line"
      (let [output (with-out-str
                     (help/action {:subcommands []
                                   :error-fn (constantly nil)}
                                  []
                                  {}))]
        (is (.contains output "Usage: "))))

    (testing "prints a list of available commands"
      (let [output (with-out-str
                     (help/action {:subcommands [[:foo (constantly nil)]
                                                 [:bar (constantly nil)]]
                                   :error-fn (constantly nil)}
                                  []
                                  {}))]
        (doseq [c ["foo" "bar"]]
          (is (.contains output c)))))

    (testing "with a subcommand-name"
      (testing "dispatches --help to subcommand"
        (let [subcommand (bond/spy (constantly nil))]
          (help/action {:subcommands [[:foo (constantly nil)]
                                      [:bar subcommand]]
                        :error-fn (constantly nil)}
                       ["bar"]
                       {})
          (is (= (-> (bond/calls subcommand) first :args)
                 ["--help"]))))

      (testing "for an invalid command"
        (testing "invokes error-fn"
          (let [error-fn (bond/spy (constantly nil))]
            (help/action {:subcommands []
                          :error-fn error-fn}
                         ["bad"]
                         {})
            (is (pos? (-> (bond/calls error-fn) count)))))))))
