(ns stack.commands.deploy
  (:require [clojure.data.json :as json]))

(def flags
  "Supported command line flags"
  [["-h" "--help"
    "Show this usage info"]
   ["-p" "--params FILE"
    "Read stack parameters from FILE"]])

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
  [[anything template] options]
  (if (nil? template)
    "<template> required"))

(defn validate-all
  [arguments options]
  (letfn [(validate [f]
            (f arguments options))]
    (some validate
          [validate-stack-name
           validate-template])))

(defn slurp-json
  [path]
  (json/read-str (slurp path)
                 :key-fn keyword))

(defn action
  [{:keys [deploy-fn error-fn]} arguments options]
  (if-let [msg (validate-all arguments options)]
    (error-fn msg)
    (let [[stack-name template] arguments]
      (deploy-fn stack-name
                 (slurp-json template)
                 (hash-map)))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
