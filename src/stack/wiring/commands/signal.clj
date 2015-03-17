(ns stack.wiring.commands.signal
  (:require [stack.commands.signal :as signal]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [stack.wiring.aws.elasticloadbalancing :as elasticloadbalancing]
            [clojure.tools.cli :refer [parse-opts]]))

(def instance-states-seq
  (partial signal/instance-states-seq
           {:physical-id-fn cloudformation/wait-for-resource
            :seq-fn (util/streaming-seq-fn
                      :seq-fn elasticloadbalancing/list-instance-states
                      :sleep-fn #(Thread/sleep 5000))}))
(def action
  (partial signal/action
           {:error-fn util/error-fn
            :instance-states-fn instance-states-seq
            :signal-fn (constantly nil)}))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn signal/usage)}))

(def dispatch
  (partial signal/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
