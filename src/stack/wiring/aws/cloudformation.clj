(ns stack.wiring.aws.cloudformation
  (:require [stack.aws.cloudformation :as cloudformation]
            [amazonica.aws.cloudformation :refer [create-stack
                                                  update-stack
                                                  describe-stack-events
                                                  describe-stack-resource]]))

(def apply-stack
  (partial cloudformation/apply-stack
           {:create-fn create-stack
            :update-fn update-stack}))

(def deploy-stack
  (partial cloudformation/deploy-stack
           {:apply-fn apply-stack}))

(def list-stack-events
  (cloudformation/list-stack-events-fn
    :events-fn describe-stack-events))

(def stack-events-seq
  (cloudformation/stack-events-seq-fn
    :list-fn list-stack-events
    :sleep-fn #(Thread/sleep 5000)))

(def physical-resource-id
  (cloudformation/physical-resource-id-fn
    :stack-resource-fn describe-stack-resource))
