(ns ^:benchmark erdos.kanren.zebra
  (:require [clojure.test :refer [deftest]]
            [criterium.core :refer [quick-bench]]
            [erdos.kanren.zebra.test :as current]
            [erdos.kanren.zebra.corelogic :as corelogic]))

(deftest test-current-logic
  (println "Running library tests.")
  (quick-bench (current/run-test)))

(deftest test-core-logic
  (println "Running core.logic tests.")
  (quick-bench (corelogic/run-test)))
