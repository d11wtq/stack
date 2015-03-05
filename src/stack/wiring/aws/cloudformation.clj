(ns stack.wiring.aws.cloudformation
  (:require [stack.aws.cloudformation :as cloudformation]
            [amazonica.aws.cloudformation :refer [create-stack
                                                  update-stack]]))

(def apply-stack
  (partial cloudformation/apply-stack
           {:create-fn create-stack
            :update-fn update-stack}))

(def deploy-stack
  (partial cloudformation/deploy-stack
           {:apply-fn apply-stack}))
