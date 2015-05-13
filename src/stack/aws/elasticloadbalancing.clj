(ns stack.aws.elasticloadbalancing)

(defn list-instance-states-fn
  "Get a map of all instance-ids and associated states on load-balancer-name."
  [& {:keys [health-fn]}]
  (fn list-instance-states
    [load-balancer-name]
    (let [response (health-fn {:load-balancer-name load-balancer-name})]
      (:instance-states response))))
