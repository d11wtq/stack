(ns stack.commands.help
  (:require [clojure.string :as string]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]])

(defn commands->map
  [commands]
  (apply hash-map (flatten commands)))

(defn make-command-list
  [subcommands]
  (->> subcommands
       (map first)
       (map name)
       (map #(str "  " %))
       (string/join \newline)))

(defn full-usage
  [subcommands]
  (str "Usage: stack <command> [args...] [opts...]"
       \newline \newline
       "Available commands:"
       \newline
       (make-command-list subcommands)))

(defn usage
  [summary]
  (str "Usage: stack help [command]"))

(defn action-fn
  [& {:keys [subcommands error-fn]}]
  (fn action
    [[subcommand-name] options]
    (if (nil? subcommand-name)
      (println (full-usage subcommands))
      (if-let [cmd (get (commands->map subcommands)
                        (keyword subcommand-name))]
        (cmd "--help")
        (error-fn (str "No such command: " subcommand-name))))))
