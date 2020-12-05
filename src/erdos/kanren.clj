(ns erdos.kanren)

(defn lvar
  ([] (lvar (gensym)))
  ([n] (-> n name keyword)))

(def lvar? keyword?)
(defn scalar? [x] (or (nil? x) (boolean? x) (number? x) (string? x) (char? x) (symbol? x)))

(defn walk [u substitutions]
  (if (lvar? u)
    (if-let [val (get substitutions u)]
      (recur val substitutions)
      u)
    u))

(defn unify [u v substitutions]
  (assert substitutions)
  (let [u (walk u substitutions)
        v (walk v substitutions)]
    (cond
      (= u v)   substitutions
      (nil? u)  nil
      (lvar? u) (assoc substitutions u v)
      (lvar? v) (assoc substitutions v u)
      (sequential? u) (some->> substitutions
                               (unify (first u) (first v))
                               (unify (next u) (next v)))
      :else     nil ;; can not unify two scalars
      )))

#_
(defmacro lconj+
  ([goal] `(delay-goal ~goal))
  ([goal & goals] `(lconj (delay-goal ~goal) (lconj+ ~@goals))))

(declare === fresh conde)

;; !!!
(defn === [u v]
  (fn [rf]
    (fn
      ([] (rf))
      ([a] (rf a))
      ([a s]
       (if-let [s2 (unify u v s)]
         (rf a s2)
         a)))))

;; goal-ctor: lvar -> transducer
(defn call-fresh [goal-ctor]
  (goal-ctor (lvar)))

;; TODO: copypaste
(defmacro fresh [var-vec & clauses]
  (if (empty? var-vec)
    `(lconj+ ~@clauses)
    `(call-fresh (fn [~(first var-vec)]
                   (fresh [~@(rest var-vec)]
                     ~@clauses)))))

;; returns a goal that succeeds whenever goal1 or goal2 succeeds
(defn ldisj [goal1 goal2]
  (fn [rf]
    (fn
      ([] (rf))
      ([a] (rf a))
      ([a s]
       (-> a
           ((goal1 rf) s)
           ((goal1 rf) s))))))

;; TODO: implement this!
(defmacro ldisj+
  ([g] g)
  ([a b] (ldisj a b))
  ([a b c] (ldisj a (ldisj b c)))
  ([a b c d] (ldisj a (ldisj b (ldisj c d)))))

;; returns a goal that succeeds when all goals succeed
(defmacro lconj+ [& clauses] `(comp ~@clauses))

(defmacro conde [& clauses]
  `(ldisj+ ~@(map (fn [clause]
                    `(lconj+ ~@clause))
                  clauses)))

(defmacro run* [fresh-var-vec & goals]
  `(sequence (fresh [~@fresh-var-vec] ~@goals) [{}]))

#_(defn call-fresh [goal-ctor]
  (fn [state]
    (let [goal (goal-ctor (lvar))]
      (goal state))))

;; (call-fresh (fn [x] (=== x 2)))
;; (comp (=== x 2) (call-fresh (lvar)))

;; (call-fresh (fn [x] (=== x 2)))

(comment

  (run*
    (call-fresh
     (fn [x]
       (call-fresh
        (fn [y]
          (=== x 2)
          (=== x y))))))

  )

;; user-level goals

#_
(defn conso [first rest out]
  (if (lvar? rest)
    (=== [first dot rest] out)
    (=== (cons first rest) out)))
#_
(defn resto [rest out]
  (fresh [first]
    (conso first rest out)))

; (defn emptyo [s] (=== '() s))
