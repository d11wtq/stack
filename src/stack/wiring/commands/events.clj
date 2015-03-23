(ns stack.wiring.commands.events
  (:require [stack.commands.events :as events]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]))

(def action
  (events/action-fn
    :error-fn util/error-fn
    :events-fn cloudformation/stack-events-seq
    :report-fn events/report-event))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn events/usage)}))

(def dispatch
  (util/make-dispatch-fn
    :flags events/flags
    :handler-fn handle-args))
