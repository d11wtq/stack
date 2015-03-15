(ns stack.commands.events-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [bond.james :as bond]
            [stack.commands.events :as events]))

(deftest report-event-test
  (testing "#'report-event"
    (testing "prints the event info"
      (let [output (with-out-str
                     (events/report-event
                       {:timestamp "2015-01-01T00:00:00Z"
                        :logical-resource-id "elb"
                        :resource-status "UPDATE_IN_PROGRESS"
                        :resource-status-reason "test"}))]
        (is (= "2015-01-01T00:00:00Z elb [UPDATE_IN_PROGRESS] test"
               (string/trim output)))))))

(deftest action-test
  (testing "#'action"
    (testing "without a <stack-name>"
      (testing "applies error-fn"
        (let [error-fn (bond/spy (constantly nil))
              events-fn (constantly nil)
              report-fn (constantly nil)]
          (events/action {:error-fn error-fn
                          :events-fn events-fn
                          :report-fn report-fn}
                         (vector)
                         (hash-map))
          (is (re-find #"<stack-name>"
                       (-> (bond/calls error-fn)
                           first
                           :args
                           first))))))

    (testing "with a <stack-name>"
      (testing "without --follow"
        (testing "applies events-fn with :follow false"
          (let [error-fn (constantly nil)
                events-fn (bond/spy (constantly nil))
                report-fn (constantly nil)]
            (events/action {:error-fn error-fn
                            :events-fn events-fn
                            :report-fn report-fn}
                           ["example-stack"]
                           {:follow false})
            (is (= (-> (bond/calls events-fn) first :args)
                   ["example-stack" :follow false]))))

        (testing "applies report-fn for each event"
          (let [event-1 {:timestamp "2015-01-01T00:00:00Z"}
                event-2 {:timestamp "2015-01-01T00:05:00Z"}
                error-fn (constantly nil)
                events-fn (constantly [event-1 event-2])
                report-fn (bond/spy (constantly nil))]
            (events/action {:error-fn error-fn
                            :events-fn events-fn
                            :report-fn report-fn}
                           ["example-stack"]
                           {:follow false})
            (is (= (->> (bond/calls report-fn) (map :args))
                   [[event-1] [event-2]]))))))))
