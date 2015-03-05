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
  summary)

(defn action
  [{:keys [subcommands error-fn]} [subcommand-name] options]
  (if (nil? subcommand-name)
    (println (full-usage subcommands))
    (if-let [cmd (get (commands->map subcommands)
                      (keyword subcommand-name))]
      (cmd "--help")
      (error-fn (str "No such command: " subcommand-name)))))

; FIXME: Extract this out to util
(defn handle-args
  [{:keys [action-fn usage-fn error-fn]}
   {:keys [arguments options summary errors]}]
  (if-let [msg (first errors)]
    (error-fn msg)
    (if (:help options)
      (usage-fn summary)
      (action-fn arguments options))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
