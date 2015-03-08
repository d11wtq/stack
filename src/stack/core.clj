(ns stack.core
  (:require [stack.util :as util]
            [stack.wiring.commands.deploy :as deploy]
            [stack.wiring.commands.help :as help]))

(def flags
  [["-h" "--help"
    "Show this usage info"]])

(defn make-dispatch-fn
  "Return a fn accepting ([& args]) and dispatching to a subcommand like git."
  [commands error-fn]
  (fn [& args]
    (let [[cmd-name & remaining] args]
      (if-let [dispatch-fn (get commands (keyword cmd-name))]
        (apply dispatch-fn remaining)
        (error-fn (str "Unknown command: " cmd-name))))))

(def -main
  (make-dispatch-fn
    {:deploy deploy/dispatch
     :help help/dispatch}
    util/error-fn))
