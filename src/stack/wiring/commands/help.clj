(ns stack.wiring.commands.help
  (:require [stack.commands.help :as help]
            [stack.wiring.commands.deploy :as deploy]
            [stack.util :as util]
            [clojure.tools.cli :refer [parse-opts]]))

(def action
  (partial help/action
           {:error-fn util/error-fn
            :subcommands [[:deploy deploy/dispatch]]}))

(def handle-args
  (partial help/handle-args
           {:error-fn util/error-fn
            :action-fn action
            :usage-fn (util/make-print-usage-fn help/usage)}))

(def dispatch
  (partial help/dispatch
           {:parse-fn parse-opts
            :handler-fn handle-args}))
