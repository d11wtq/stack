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

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
