(defproject stack "0.1.0-SNAPSHOT"
  :description "AWS CloudFormation Stack Deployer"
  :url "https://github.com/d11wtq/stack"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [amazonica "0.3.12"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/data.json "0.2.5"]
                 [bond "0.2.5"]]
  :main stack.core)
