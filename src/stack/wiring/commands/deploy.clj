(ns stack.wiring.commands.deploy
  (:require [stack.commands.deploy :as deploy]
            [stack.wiring.aws.cloudformation :as cloudformation]
            [clojure.tools.cli :refer  [parse-opts]]))

; FIXME: Move this to a util ns, test it
(defn error
  [msg]
  (throw (Exception. msg)))

; FIXME: Move this to a util ns, test it
(defn usage
  [summary]
  (println (deploy/usage summary)))

(def action
  (partial deploy/action
           {:error-fn error
            :deploy-fn cloudformation/deploy-stack}))

(def handle-args
  (partial deploy/handle-args
           {:error-fn error
            :action-fn action
            :usage-fn usage}))

(def dispatch
  (partial deploy/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
