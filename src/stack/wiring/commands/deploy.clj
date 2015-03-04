(ns stack.wiring.commands.deploy
  (:require [stack.commands.deploy :as deploy]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [clojure.tools.cli :refer  [parse-opts]]))

(def action
  (partial deploy/action
           cloudformation/deploy-stack))

(def handle-args
  (partial deploy/handle-args
           action
           deploy/usage))

(def dispatch
  (partial deploy/dispatch
           parse-opts
           handle-args))
