(ns stack.commands.signal
  (:require [stack.util :as util]
            [clojure.string :as string]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-u" "--update"
    "Stop once the stack update completes"]])

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

(defn success?
  [state]
  (= "InService" state))

(defn instance-states-seq-fn
  [& {:keys [seq-fn status-fn physical-id-fn]}]
  (fn instance-states-seq
    [stack-name elb-name & {:keys [update]}]
    ((util/streaming-seq-fn
       :seq-fn seq-fn
       :more-fn (if update
                  (fn []
                    (not (re-find #"(COMPLETE|FAILED)$"
                                  (status-fn stack-name))))
                  (constantly true))
       :sleep-fn #(Thread/sleep 5000))
     (physical-id-fn stack-name elb-name))))

(defn report-instance-state
  [{:keys [instance-id state]}]
  (println "Instance"
           instance-id
           "is now"
           state))

(defn handle-instance-state-fn
  [& {:keys [report-fn signal-fn]}]
  (fn handle-instance-state-fn
    [stack-name asg-name state]
    (report-fn state)
    (if (success? (:state state))
      (signal-fn stack-name
                 asg-name
                 (:instance-id state)))))

(defn action-fn
  [& {:keys [instance-states-fn handler-fn error-fn]}]
  (fn action
    [arguments options]
    (if-let [msg (validate-all arguments options)]
      (error-fn msg)
      (let [[stack-name elb-asg] arguments]
        (doseq [s (instance-states-fn stack-name
                                      (-> elb-asg
                                          (string/split #":")
                                          first)
                                      :update (:update options))]
          (handler-fn stack-name
                      (-> elb-asg
                          (string/split #":")
                          last)
                      s))))))
