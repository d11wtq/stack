(ns stack.wiring.commands.signal
  (:require [stack.commands.signal :as signal]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [stack.wiring.aws.elasticloadbalancing :as elasticloadbalancing]))

(def instance-states-seq
  (signal/instance-states-seq-fn
    :physical-id-fn cloudformation/wait-for-resource
    :status-fn cloudformation/stack-status
    :seq-fn elasticloadbalancing/list-instance-states))

(def handle-instance-state
  (signal/handle-instance-state-fn
    :report-fn signal/report-instance-state
    :signal-fn cloudformation/signal-resource-success))

(def action
  (signal/action-fn
    :error-fn util/error-fn
    :instance-states-fn instance-states-seq
    :handler-fn handle-instance-state))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn signal/usage)}))

(def dispatch
  (util/make-dispatch-fn
    :flags signal/flags
    :handler-fn handle-args))
