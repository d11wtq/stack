(ns stack.wiring.commands.deploy
  (:require [stack.commands.deploy :as deploy]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]))

(def action
  (deploy/action-fn
    :error-fn util/error-fn
    :deploy-fn cloudformation/deploy-stack))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn deploy/usage)}))

(def dispatch
  (util/make-dispatch-fn
    :flags deploy/flags
    :handler-fn handle-args))
