(ns stack.params
  (:require [clojure.string :refer [split]]))

(defn make-result
  ([] (make-result {} []))
  ([params errors]
   {:params params
    :errors errors}))

(defn collect
  [{:keys [params errors]} s]
  (let [[key value] (split s #"=" 2)]
    (if (and key value)
      (make-result (assoc params (keyword key) value)
                   errors)
      (make-result params
                   (conj errors
                         (str "Invalid key-value param: " s))))))

(defn parse-params
  "Given a list of key=value strings, produce a map of {:key value}."
  [args]
  (reduce collect
          (make-result)
          args))
