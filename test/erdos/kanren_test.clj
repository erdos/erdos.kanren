(ns erdos.kanren-test
  (:require [clojure.test :refer :all]
            [erdos.kanren :refer :all]))

(deftest test-walk
  (testing "only variable")
  )

(def test-smap {::a 1})

(deftest test-unify
  (testing "can not unify")

  (testing "sequentials"
    (is (= test-smap (unify [] [] test-smap)))
    (is (= test-smap (unify [1 2 3] [1 2 3] test-smap)))
    (is (nil? (unify [] [1] test-smap)))
    (is (nil? (unify [1] [1 2] test-smap)))

    (is (map? (unify (lvar) [1 2 3] test-smap)))
    (is (map? (unify [1 2 3] (lvar) test-smap)))
    )
  )

#_
(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


#_
(deftest test-fresh
  (is (= {(lvar :x) 1
          (lvar :y) 2}
         (fresh [x y]
           (=== x 1)
           (=== y 2)))))

#_
(deftest test-run*
  (is (= [1 7]
         (run* [q]
           (conde [(=== q 1)]
                  [(=== q 7)])))))
#_
(deftest test-call-fresh
  (is (fn? (call-fresh (fn [x] (=== x 1)))))
  )

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

(deftest simple-tests
  (is (= ["hello"]
         (run* [q] (=== q "hello")))))
