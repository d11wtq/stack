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
  [{:keys [create-fn update-fn]} payload]
  (try
    (create-fn payload)
    (catch AlreadyExistsException e
      (update-fn payload))))

(defn deploy-stack
  "Create or update stack-name using template and params."
  [{:keys [apply-fn]} stack-name template params]
  (let [payload {:stack-name stack-name
                 :template-body (json/write-str template)
                 :parameters (expand-parameters params)}]
    (apply-fn payload)))

(defn list-stack-events-fn
  "Get a list of all events for stack-name."
  [& {:keys [events-fn]}]
  (fn list-stack-events
    ([stack-name]
     (list-stack-events stack-name (vector)))
    ([stack-name seen]
     (list-stack-events stack-name seen nil))
    ([stack-name seen next-token]
     (let [payload {:stack-name stack-name
                    :next-token next-token}
           response (events-fn
                      (if (nil? next-token)
                        (select-keys payload [:stack-name])
                        payload))
           new-seen (concat seen (:stack-events response))]
       (if-let [new-next-token (:next-token response)]
         (recur stack-name new-seen new-next-token)
         (reverse new-seen))))))

(defn stack-events-seq-fn
  "Get a (optionally) infinite lazy-seq of events for stack-name."
  [& {:keys [list-fn sleep-fn]}]
  (fn stack-events-seq
    [stack-name & {:keys [follow]}]
    (if follow
      (->> (iterate inc 0)
           (map (fn [i]
                  (if (> i 1)
                    (sleep-fn))
                  (list-fn stack-name)))
           flatten
           distinct)
      (list-fn stack-name))))
