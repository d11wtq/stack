(ns stack.commands.events)

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-f" "--follow"
    "Keep polling for new events"]])

(defn usage
  [summary]
  (str "Usage: stack events <stack-name> [opts...]"
       \newline
       \newline
       "Options:"
       \newline
       summary))

(defn validate-stack-name
  [[stack-name] options]
  (if (nil? stack-name)
    "<stack-name> required"))

(defn report-event
  [event]
  (println (str (:timestamp event))
           (str (:logical-resource-id event))
           (str "[" (:resource-status event) "]")
           (str (:resource-status-reason event))))

(defn events-seq
  [{:keys [seq-fn sleep-fn]} stack-name & {:keys [follow]}]
  (if follow
    (-> (repeatedly
          (fn []
            (sleep-fn)
            (seq-fn stack-name)))
        flatten
        distinct)
    (seq-fn stack-name)))

; FIXME: Extract this to util
(defn validate-all
  [arguments options]
  (letfn [(validate [f] (f arguments options))]
    (some validate
          [validate-stack-name])))

(defn action
  [{:keys [events-fn report-fn error-fn]} arguments options]
  (if-let [msg (validate-all arguments options)]
    (error-fn msg)
    (let [[stack-name] arguments
          {:keys [follow]} options]
      (doseq [evt (events-fn stack-name :follow follow)]
        (report-fn evt)))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
