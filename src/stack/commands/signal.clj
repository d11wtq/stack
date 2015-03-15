(ns stack.commands.signal
  (:require [stack.util :as util]
            [clojure.string :as string]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]])

(defn usage
  [summary]
  (str "Usage: stack signal <stack-name> <elb>:<asg> [opts...]"
       \newline
       \newline
       "Options:"
       \newline
       summary))

(defn validate-stack-name
  [[stack-name] options]
  (if (nil? stack-name)
    "<stack-name> required"))

(defn validate-elb-asg
  [[-- elb-asg] options]
  (if-not (re-find #"^[a-zA-Z0-9]+:[a-zA-Z0-9]+$" (str elb-asg))
    (if (nil? elb-asg)
      "<elb>:<asg> required"
      "<elb>:<asg> malformed")))

(def validate-all
  (util/make-validate-fn [validate-stack-name
                          validate-elb-asg]))


;; FIXME: Define two functions:
;; 1. resource-id (blocks until resource exists)
;; 2. instance-states-seq (depends on resource-id)

(defn action
  [{:keys [instance-states-fn signal-fn error-fn]} arguments options]
  (if-let [msg (validate-all arguments options)]
    (error-fn msg)
    (let [[stack-name elb-asg] arguments]
      (instance-states-fn stack-name
                          (-> elb-asg
                              (string/split #":")
                              first)))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
