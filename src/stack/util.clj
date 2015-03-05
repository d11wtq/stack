(ns stack.util)

(defn error-fn
  "Handle an error by throwing an Exception."
  [msg]
  (throw (Exception. msg)))

(defn make-print-usage-fn
  "Create a fn that applies usage-fn with summary and prints the result."
  [usage-fn]
  (fn [summary]
    (println (usage-fn summary))))

(defn make-handler-fn
  "Create a fn dispatching to an appropriate action based on parsed args."
  [{:keys [action-fn usage-fn error-fn]}]
  (fn [{:keys [arguments options summary errors]}]
    (if-let [msg (first errors)]
      (error-fn msg)
      (if (:help options)
        (usage-fn summary)
        (action-fn arguments options)))))
