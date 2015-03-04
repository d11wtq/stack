(ns stack.commands.deploy)

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-p" "--params FILE"
    "Read stack parameters from FILE"]])

(defn usage
  [summary])

(defn action
  [deploy-fn error-fn arguments options])

(defn handle-args
  [action-fn usage-fn {:keys [arguments options summary errors]}]
  (let [error-fn (fn [msg]
                   (println msg)
                   (usage-fn summary))]
    (if-let [msg (first errors)]
      (error-fn msg)
      (if (:help options)
        (usage-fn summary)
        (action-fn error-fn arguments options)))))

(defn dispatch
  [parse-fn handler-fn & args]
  (handler-fn (parse-fn args flags)))
