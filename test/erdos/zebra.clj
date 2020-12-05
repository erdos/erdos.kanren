(ns erdos.zebra
  (:require [clojure.test :refer :all]
            [clojure.walk :refer [postwalk-replace]]
            [erdos.kanren :refer :all]))

;; original source:
;;
;; https://gist.github.com/rm-hull/6952960

(defmacro with-vars [body]
  (postwalk-replace {'_ `(lvar)} body))

(defn righto [x y l]
  (with-vars
    (conde
     [(=== [x y :& _] l)]
     [(fresh [r]
        (=== [_ :& r] l)
        (righto x y r))])))

(deftest test-righto
  (is (= [2] (run* [q] (righto 1 q [1 2 3 4]))))
  (is (= [3] (run* [q] (righto 2 q [1 2 3 4]))))
  (is (= [4] (run* [q] (righto 3 q [1 2 3 4])))))

(defn nexto [x y l]
  (any (righto x y l) (righto y x l)))

(defn zebrao [hs]
  (with-vars
   (all
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
    (nexto [_ _ _ "fox" _] [_ "chesterfields" _ _ _] hs))))

;; who drinks water? who owns the zebra?
(defn zebra []
  (run* [q] (zebrao q)))

(deftest test-zebra
  (println "Testing own code:")
  (time (println (zebra))))
