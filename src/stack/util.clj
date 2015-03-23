(ns stack.util
  (:require [clojure.tools.cli :as cli]))

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

(defn make-validate-fn
  "Create a fn implementing a strategy across many validators."
  [validators]
  (fn [arguments options]
    (some #(% arguments options)
          validators)))

(defn make-dispatch-fn
  "Make a command dispatcher function using handler-fn."
  [& {:keys [handler-fn flags]}]
  (fn dispatch
    [& args]
    (handler-fn (cli/parse-opts args flags))))

(defn streaming-seq-fn
  "Continually apply seq-fn to make an infinite lazy-seq of distinct entries."
  [& {:keys [seq-fn sleep-fn]}]
  (fn [& args]
    (->> (iterate inc 0)
         (map (fn [i]
                (if (> i 1)
                  (sleep-fn))
                (apply seq-fn args)))
         flatten
         distinct)))
