(ns erdos.kanren.zebra.micrologic
  (:require [criterium.core :refer [quick-bench]]))

(comment

  (require '[micro-logic.sequence :refer :all]
           '[micro-logic.core :refer :all]
           '[clojure.walk :refer [postwalk-replace]])

  (defmacro with-vars [body]
    (postwalk-replace {'_ `(lvar (gensym))} body))

  (defn righto [x y l]
    (with-vars
      (conde
       [(=== [x y dot _] l)]
       [(fresh [r]
          (=== [_ dot r] l)
          (righto x y r))])))

  ; (run* [q] (conde [(=== [1 2] q)] [(fresh [c] (=== q c))]))

  (assert (= [2] (run* [q] (righto 1 q [1 2 3 4]))))
  (assert (= [3] (run* [q] (righto 2 q [1 2 3 4]))))
  (assert (= [4] (run* [q] (righto 3 q [1 2 3 4]))))

  (defn nexto [x y l]
    (conde [(righto x y l)] [(righto y x l)]))

  (defn membero [elem list]
    (conde [(firsto elem list)]
           [(fresh [d] (resto d list) (membero elem d))]))

  (assert (= [1 2 3] (run* [q] (membero q [1 2 3]))))

  (defn zebrao [hs]
    (with-vars
      (conde
       [
        (=== [_ _  [_ _ "milk" _ _] _ _] hs)
        (firsto ["norwegian" _ _ _ _] hs)
        (nexto ["norwegian" _ _ _ _] [_ _ _ _ "blue"] hs)
        (righto [_ _ _ _ "ivory"] [_ _ _ _ "green"] hs)
        (membero ["englishman" _ _ _ "red"] hs)
        (membero [_ "kools" _ _ "yellow"] hs)
        (membero ["spaniard" _ _ "dog" _] hs)
        (membero [_ _ "coffee" _ "green"] hs)
        (membero ["ukrainian" _ "tea" _ _] hs)
        (membero [_ "lucky-strikes" "oj" _ _] hs)
        (membero ["japanese" "parliaments" _ _ _] hs)
        (membero [_ "oldgolds" _ "snails" _] hs)
        (nexto [_ _ _ "horse" _] [_ "kools" _ _ _] hs)
        (nexto [_ _ _ "fox" _] [_ "chesterfields" _ _ _] hs)])))

;; who drinks water? who owns the zebra?
(defn run-test []
  (doall (run* [q] (zebrao q))))

(time (run-test))
(quick-bench (run-test))


  )
