(ns stack.util)

(defn error-fn
  "Handle an error by throwing an Exception."
  [msg]
  (throw (Exception. msg)))

(defn make-print-usage-fn
  "Create a function that applies usage-fn with summary and prints the result."
  [usage-fn]
  (fn [summary]
    (println (usage-fn summary))))
