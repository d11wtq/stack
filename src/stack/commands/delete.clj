(ns stack.commands.delete
  (:require [stack.util :as util]
            [clojure.string :as string]
            [clj-time.core :as joda])
  (:import [com.amazonaws AmazonServiceException]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]])

(defn usage
  [summary]
  (str "Usage: stack delete <stack-name> [opts...]"
       \newline
       \newline
       "Options:"
       \newline
       summary))

(defn validate-stack-name
  [[stack-name] options]
  (if (nil? stack-name)
    "<stack-name> required"))

(def validate-all
  (util/make-validate-fn [validate-stack-name]))

(defn dispatch-events-fn
  [& {:keys [events-fn]}]
  (fn dispatch-events
    ([[arguments options]]
     (dispatch-events arguments options))
    ([arguments options]
     (let [[stack-name] arguments]
       (try
         (events-fn stack-name
                    "--follow"
                    "--update")
         (catch AmazonServiceException e
           (println (str (joda/now))
                    stack-name
                    "[DELETE_COMPLETE]")))))))

(defn action-fn
  [& {:keys [destroy-fn after-fn error-fn]}]
  (fn action
    [arguments options]
    (if-let [msg (validate-all arguments options)]
      (error-fn msg)
      (let [[stack-name] arguments]
        (destroy-fn stack-name)
        (after-fn arguments options)))))
