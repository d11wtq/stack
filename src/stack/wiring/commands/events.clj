(ns stack.wiring.commands.events
  (:require [stack.commands.events :as events]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [clojure.tools.cli :refer [parse-opts]]))

(def events-seq
  (partial events/events-seq
           {:seq-fn cloudformation/list-stack-events
            :sleep-fn #(Thread/sleep 10000)}))

(def action
  (partial events/action
           {:error-fn util/error-fn
            :events-fn events-seq
            :report-fn events/report-event}))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn events/usage)}))

(def dispatch
  (partial events/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
