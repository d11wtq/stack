(ns stack.aws.cloudformation
  (:require [clojure.data.json :as json]
            [clojure.walk :refer [stringify-keys]])
  (:import [com.amazonaws.services.cloudformation.model
            AlreadyExistsException]))

(defn expand-parameters
  [params]
  (let [key-names [:parameter-key :parameter-value]]
    (map #(zipmap key-names %)
         (stringify-keys params))))

(defn apply-stack
  "Create or update a stack using the given payload."
  [create-fn update-fn payload]
  (try
    (create-fn payload)
    (catch AlreadyExistsException e
      (update-fn payload))))

(defn deploy-stack
  "Create or update stack-name using template and params."
  [apply-fn stack-name template params]
  (let [payload {:stack-name stack-name
                 :template-body (json/write-str template)
                 :parameters (expand-parameters params)}]
    (apply-fn payload)))
