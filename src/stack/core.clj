(ns stack.core
  (:require [stack.wiring.commands.deploy]))

(defn dispatch
  "Accept a two-dimensional command vector and apply a subcommand using args."
  [commands & args])
