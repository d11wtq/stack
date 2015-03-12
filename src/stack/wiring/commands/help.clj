(ns stack.wiring.commands.help
  (:require [stack.commands.help :as help]
            [stack.wiring.commands.deploy :as deploy]
            [stack.wiring.commands.events :as events]
            [stack.util :as util]
            [clojure.tools.cli :refer [parse-opts]]))

(def action
  (partial help/action
           {:error-fn util/error-fn
            :subcommands [[:deploy deploy/dispatch
                           :events events/dispatch]]}))

(def handle-args
  (util/make-handler-fn
    {:error-fn util/error-fn
     :action-fn action
     :usage-fn (util/make-print-usage-fn help/usage)}))

(def dispatch
  (partial help/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
