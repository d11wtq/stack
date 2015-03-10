(ns stack.commands.deploy
  (:require [clojure.data.json :as json]
            [stack.params :refer [parse-params]]))

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
  [[-- template] options]
  (if (nil? template)
    "<template> required"))

(defn validate-params
  [[-- -- & keys=values] options]
  (-> (parse-params keys=values)
      :errors
      first))

(defn validate-all
  [arguments options]
  (letfn [(validate [f]
            (f arguments options))]
    (some validate
          [validate-stack-name
           validate-template
           validate-params])))

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

(defn action
  [{:keys [deploy-fn error-fn]} arguments options]
  (if-let [msg (validate-all arguments options)]
    (error-fn msg)
    (let [[stack-name template & keys=values] arguments
          {:keys [params]} options]
      (deploy-fn stack-name
                 (slurp-json template)
                 (merge-params params keys=values)))))

(defn dispatch
  [{:keys [parse-fn handler-fn]} & args]
  (handler-fn (parse-fn args flags)))
