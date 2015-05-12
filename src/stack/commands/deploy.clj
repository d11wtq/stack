(ns stack.commands.deploy
  (:require [clojure.data.json :as json]
            [stack.util :as util]
            [stack.params :refer [parse-params]]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-p" "--params FILE"
    "Read stack parameters from FILE"]
   ["-s" "--signal ELB:ASG"
    "Signal ASG using instances on ELB"]])

(defn usage
  [summary]
  (str "Usage: stack deploy <stack-name> <template> [opts...] [key=value...]"
       \newline
       \newline
       "Options:"
       \newline
       summary))

(defn validate-stack-name
  [[stack-name] options]
  (if (nil? stack-name)
    "<stack-name> required"))

(defn validate-template
  [[-- template] options]
  (if (nil? template)
    "<template> required"))

(defn validate-params
  [[-- -- & keys=values] options]
  (-> (parse-params keys=values)
      :errors
      first))

(def validate-all
  (util/make-validate-fn [validate-stack-name
                          validate-template
                          validate-params]))

(defn slurp-json
  [path]
  (json/read-str (slurp path)
                 :key-fn keyword))

(defn merge-params
  [path keys=values]
  (let [overrides (:params (parse-params keys=values))
        params (if (nil? path)
                 (hash-map)
                 (slurp-json path))]
    (merge params overrides)))

(defn dispatch-events-fn
  [& {:keys [events-fn]}]
  (fn dispatch-events
    ([[arguments options]]
     (dispatch-events arguments options))
    ([arguments options]
     (events-fn (first arguments)
                "--follow"
                "--update"))))

(defn dispatch-signal-fn
  [& {:keys [signal-fn]}]
  (fn dispatch-signal
    ([[arguments options]]
     (dispatch-signal arguments options))
    ([arguments options]
     (if (:signal options)
       (signal-fn (first arguments)
                  (:signal options)
                  "--update")))))

(defn make-futures
  [actions args done]
  (map (fn [f]
         (future
           (try
             (f args)
             (catch Exception e
               (deliver done [:err e])))))
       actions))

(defn wait-for-futures
  [futs done]
  (future
    (doseq [f futs]
      (try
        (deref f)
        (catch Exception e
          (deliver done [:err e]))))
    (deliver done [:ok]))

  (let [[status e] @done]
    (case status
      :ok nil
      :err (throw e))))

(defn dispatch-parallel-actions-fn
  [& {:keys [actions]}]
  (fn dispatch-parallel-actions
    [& args]
    (let [done (promise)]
      (wait-for-futures
        (make-futures actions args done)
        done))))

(defn action-fn
  [& {:keys [deploy-fn after-fn error-fn]}]
  (fn action
    [arguments options]
    (if-let [msg (validate-all arguments options)]
      (error-fn msg)
      (let [[stack-name template & keys=values] arguments]
        (deploy-fn stack-name
                   (slurp-json template)
                   (merge-params (:params options) keys=values))
        (after-fn arguments options)))))
