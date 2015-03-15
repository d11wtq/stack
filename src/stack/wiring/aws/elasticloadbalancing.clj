(ns stack.wiring.aws.elasticloadbalancing
  (:require [stack.aws.elasticloadbalancing :as elasticloadbalancing]
            [amazonica.aws.elasticloadbalancing :refer [describe-instance-health]]))

(def list-instance-states
  (elasticloadbalancing/list-instance-states-fn
    :health-fn describe-instance-health))
