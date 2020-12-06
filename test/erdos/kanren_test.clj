(ns erdos.kanren-test
  (:require [clojure.test :refer :all]
            [erdos.kanren :refer :all]))

(def test-smap {::a 1})

(deftest test-unify
  (testing "can not unify")

  (testing "nil values"
    (is (= {:0 nil} (unify :0 nil {})))
    (is (= {:0 nil} (unify nil :0 {})))
    (is (= {:0 nil} (unify [:0 1] [nil 1] {})))
    (is (= {:0 nil} (unify [1 :0] [1 nil] {}))))

  (testing "sequentials"
    (is (= test-smap (unify [] [] test-smap)))
    (is (= test-smap (unify [1 2 3] [1 2 3] test-smap)))
    (is (nil? (unify [] [1] test-smap)))
    (is (nil? (unify [1] [1 2] test-smap)))

    (testing "to variable"
      (is (map? (unify (lvar) [1 2 3] test-smap)))
      (is (map? (unify [1 2 3] (lvar) test-smap)))

      (is (= {:0 []} (unify :0 [] {})))
      (is (= {:0 []} (unify [] :0 {}))))

    (testing "splitting lists"
      (is (= nil (unify [:0 :& :1] [] {}))))))

(deftest test-===
  (testing "Can not unify"
    (is (= []
           (sequence (=== 1 2) [{}]))))
  (testing "Can unify"
    ;; need a fresh var to make sure
    (is (= [{(lvar :x) 1}]
           (sequence (=== (lvar :x) 1) [{}])))))

(deftest test-call-fresh
  (doto (sequence (call-fresh (fn [x] (=== x 1))) [{}])
    (-> count (= 1) assert)
    (-> first count (= 1) assert)))

(deftest test-fresh
  (testing "Empty var list"
    (is (empty? (sequence (fresh [] (=== 1 2)) [{}]))))
  (testing "Empty body"
    (is (= [{}]
           (sequence (fresh [a b]) [{}]))))
  (is (not-empty
       (sequence
        (fresh [a b]
          (=== a 1)
          (=== b 2))
        [{}]))))

(deftest test-all
  (is (empty? (run* [a b]
                (all
                 (=== a 1)
                 (=== a b)
                 (=== b 2))))))
#_
(deftest test-anyo
  (is (= [1 2 3 1 2 3 1 2 3 1]
         (run 10 [q]
           (anyo (conde [(=== 1 q)]
                        [(=== 2 q)]
                        [(=== 3 q)]))) )))

(deftest simple-tests
  (is (= ["hello"]
         (run* [q] (=== q "hello")))))

(deftest test-conso
  (is (= [1]
         (run* [q] (conso q [] [1]))))
  (is (= [1]
         (run* [q] (conso q [2 3] [1 2 3]))))
  (is (= [2]
         (run* [q] (conso 1 [q 3] [1 2 3]))))
  (is (= [[3]]
         (run* [q] (conso 1 [2 3] [1 2 :& q]))))
  (testing "can not match"
    (is (= [] (run* [q] (conso 1 [2 3] [1 2]))))
    (is (= [] (run* [q] (conso 1 [2 3] [2 3]))))
    (is (= [] (run* [q] (conso 1 [2 3] []))))
    (is (= [] (run* [q] (conso 1 [2 3] [1 2 3 4]))))))

(deftest test-firsto
  (testing "match on first element of list"
    (is (= [1] (run* [q] (firsto q [1 2 3]))))
    (is (= [1] (run* [q] (firsto q [1]))))
    (is (= [] (run* [q] (firsto q [])))))
  (testing "match on list itself"
    (is (= [] (run* [q] (firsto 1 [2 q]))))
    (is (= [:0] (run* [q] (firsto 1 [1 q]))))
    (is (= [[1 :& :1]] (run* [q] (firsto 1 q)))))
  (testing "not a list"
    (is (= [] (run* [q] (firsto 1 2))))))

(deftest test-emptyo
  (is (= [()] (run* [q] (emptyo q))))
  (is (= [[1 ()]] (run* [q x]
                    (emptyo x)
                    (=== q [1 x])
                    )))
  (is (= [[1 ()]] (run* [q x]
                    (=== q [1 x])
                    (emptyo x)))))

(deftest test-nilo
  (is (= [nil] (run* [q] (nilo q)))))

(deftest test-membero
  (testing "not in list"
    (is (= [] (run* [q] (membero 1 []))))
    (is (= [] (run* [q] (membero 1 [2 3]))))
    (is (= [] (run* [q] (membero 1 1)))))
  (testing "member of list"
    (is (= [1] (run* [q] (membero 1 [q 3]))))
    (is (= [1] (run* [q] (membero 1 [2 q]))))
    (is (= [1 2 3] (run* [q] (membero q [1 2 3]))))))

(deftest text-bit-xoro
  (is (= [[0 0] [1 1]]
         (run* [s]
           (fresh [x y]
             (bit-xoro x y 0)
             (=== [x y] s))))))

(deftest test-full-addero
  (is (= [[0 1]]
         (run* [s]
           (fresh [r c]
             (full-addero 0 1 1 r c)
             (=== [r c] s))))))

#_
(deftest test-poso
  (is (= [] (run* [q] (poso []) (=== "t" q))))
  (is (= [[:0 :& :1]] (run* [r] (poso r))))
  )
