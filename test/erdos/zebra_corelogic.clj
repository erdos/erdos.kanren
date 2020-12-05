(ns erdos.zebra-corelogic
  (:refer-clojure :exclude [==])
  (:require [clojure.test :refer [deftest testing is are]]
            [clojure.walk :refer [postwalk-replace]]
            [clojure.core.logic :refer [run* == conde defne all lvar firsto membero]]))

;; !!! Source: https://gist.github.com/rm-hull/6952960

(defne righto [x y l]
  ([_ _ [x y . r]])
  ([_ _ [_ . r]] (righto x y r)))

(defn nexto [x y l]
  (conde
    [(righto x y l)]
    [(righto y x l)]))

(defmacro with-vars [body]
  (postwalk-replace {'_ `(lvar)} body))

;; who drinks water? who owns the zebra?
(defn zebrao [hs]
  (with-vars
   (all
    (== [_ _  [_ _ :milk _ _] _ _] hs)
    (firsto hs [:norwegian _ _ _ _])
    (nexto [:norwegian _ _ _ _] [_ _ _ _ :blue] hs)
    (righto [_ _ _ _ :ivory] [_ _ _ _ :green] hs)
    (membero [:englishman _ _ _ :red] hs)
    (membero [_ :kools _ _ :yellow] hs)
    (membero [:spaniard _ _ :dog _] hs)
    (membero [_ _ :coffee _ :green] hs)
    (membero [:ukrainian _ :tea _ _] hs)
    (membero [_ :lucky-strikes :oj _ _] hs)
    (membero [:japanese :parliaments _ _ _] hs)
    (membero [_ :oldgolds _ :snails _] hs)
    (nexto [_ _ _ :horse _] [_ :kools _ _ _] hs)
    (nexto [_ _ _ :fox _] [_ :chesterfields _ _ _] hs))))

(defn zebra []
  (run* [q] (zebrao q)))

(deftest test-zebra
  (println "Testing core.logic:")
  (time (println (zebra))))
