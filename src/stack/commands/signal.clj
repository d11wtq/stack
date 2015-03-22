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

(defn instance-states-seq
  [{:keys [physical-id-fn seq-fn]} stack-name elb]
  (seq-fn (physical-id-fn stack-name elb)))

(defn report-instance-state
  [{:keys [instance-id state]}]
  (println "Instance"
           instance-id
           "is now"
           state))

(defn success?
  [state]
  (= "InService" state))

(defn handle-instance-state
  [{:keys [report-fn signal-fn]} stack-name asg-name state]
  (report-fn state)
  (if (success? (:state state))
    (signal-fn stack-name
               asg-name
               (:instance-id state))))

(defn action
  [{:keys [instance-states-fn handler-fn error-fn]} arguments options]
  (if-let [msg (validate-all arguments options)]
    (error-fn msg)
    (let [[stack-name elb-asg] arguments]
      (doseq [s (instance-states-fn stack-name
                                    (-> elb-asg
                                        (string/split #":")
                                        first))]
        (handler-fn stack-name
                    (-> elb-asg
                        (string/split #":")
                        last)
                    s)))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
