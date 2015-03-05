(ns stack.wiring.commands.deploy
  (:require [stack.commands.deploy :as deploy]
            [stack.util :as util]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [clojure.tools.cli :refer [parse-opts]]))

(def action
  (partial deploy/action
           {:error-fn util/error-fn
            :deploy-fn cloudformation/deploy-stack}))

(def handle-args
  (partial deploy/handle-args
           {:error-fn util/error-fn
            :action-fn action
            :usage-fn (util/make-print-usage-fn deploy/usage)}))

(def dispatch
  (partial deploy/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
