(ns stack.commands.deploy)

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-p" "--params FILE"
    "Read stack parameters from FILE"]])

(defn usage
  [summary]
  summary)

(defn action
  [{:keys [deploy-fn error-fn]} arguments options])

(defn handle-args
  [{:keys [action-fn usage-fn error-fn]}
   {:keys [arguments options summary errors]}]
  (if-let [msg (first errors)]
    (error-fn msg)
    (if (:help options)
      (usage-fn summary)
      (action-fn error-fn arguments options))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
