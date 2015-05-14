(ns stack.wiring.aws.cloudformation
  (:require [stack.aws.cloudformation :as cloudformation]
            [amazonica.aws.cloudformation :refer [create-stack
                                                  update-stack
                                                  delete-stack
                                                  describe-stacks
                                                  describe-stack-events
                                                  describe-stack-resource
                                                  signal-resource]]))

(def apply-stack
  (cloudformation/apply-stack-fn
    :create-fn create-stack
    :update-fn update-stack))

(def deploy-stack
  (cloudformation/deploy-stack-fn
    :apply-fn apply-stack))

(def destroy-stack
  (cloudformation/destroy-stack-fn
    :delete-fn delete-stack))

(def stack-status
  (cloudformation/stack-status-fn
    :describe-fn describe-stacks))

(def list-stack-events
  (cloudformation/list-stack-events-fn
    :events-fn describe-stack-events))

(def stack-events-seq
  (cloudformation/stack-events-seq-fn
    :list-fn list-stack-events
    :status-fn stack-status
    :sleep-fn #(Thread/sleep 5000)))

(def physical-resource-id
  (cloudformation/physical-resource-id-fn
    :stack-resource-fn describe-stack-resource))

(def wait-for-resource
  (cloudformation/wait-for-resource-fn
    :physical-id-fn physical-resource-id
    :sleep-fn #(Thread/sleep 5000)))

(def signal-resource-success
  (cloudformation/signal-resource-success-fn
    :signal-fn signal-resource))
