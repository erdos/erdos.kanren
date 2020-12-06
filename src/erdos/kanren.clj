(ns erdos.kanren)

(set! *warn-on-reflection* true)

(defn lvar
  ([] (lvar (gensym)))
  ([n] (-> n name keyword)))

(def lvar? keyword?)

(defn- walk [u substitutions]
  (if (lvar? u)
    (if-let [val (get substitutions u)]
      (recur val substitutions)
      u)
    u))

(declare unify)

(defn- unify-seq [u v substitutions]
  (cond
    ; (nil? u) nil (nil? v) nil
    (= u v)          substitutions
    (= :& (first u)) (unify (second u) v substitutions)
    (= :& (first v)) (unify u (second v) substitutions)

    (empty? u)        nil
    (empty? v)        nil

    :else
    (some->> substitutions
             (unify (first u) (first v))
             (unify-seq (rest u) (rest v)))))

(defn unify [u v substitutions]
  (assert substitutions)
  (let [u (walk u substitutions)
        v (walk v substitutions)]
    (cond
      (= u v)   substitutions
      (lvar? u) (assoc substitutions u v)
      (lvar? v) (assoc substitutions v u)
      (and (sequential? u) (sequential? v)) (unify-seq u v substitutions)
      :else     nil ;; can not unify two scalars
      )))

(defmacro ->transducer [[rf a s] body]
  (assert (every? symbol? [rf a s]))
  `(fn [~rf]
     (fn
       ([] (~rf))
       ([~a] (~rf ~a))
       ([~a ~s] ~body))))

(defn === [u v]
  (->transducer [rf a s]
    (if-let [s2 (unify u v s)]
      (rf a s2)
      a)))

;; parameter is a function: lvar -> transducer
;;
;; original implementation without metas:
;; (goal-ctor (lvar))
(defn call-fresh [goal-ctor]
  (assert (fn? goal-ctor))
  (->transducer [rf a s]
    (let [new-var (if-let [cnt (-> s meta ::vars count)]
                    (lvar (str cnt))
                    (lvar :0))
          new-red ((goal-ctor new-var) rf)]
      (new-red
       a
       (vary-meta s update ::vars (fnil conj #{}) new-var)))))

;; introduces a new variable
(defmacro fresh [var-vec & clauses]
  (assert (vector? var-vec))
  (assert (every? symbol? var-vec))
  `(-> (lconj+ ~@clauses)
       ~@(for [v (reverse var-vec)]
           `(->> (fn [~v]) (call-fresh)))))

;; succeeds if any goals succeed
(defmacro ldisj+ [& goals]
  (let [s  (gensym "s")
        rf (gensym "rs")]
    `(->transducer [~rf a# ~s]
       (-> a#
           ~@(for [g goals]
               (list (list g rf) s))))))

;; returns a goal that succeeds when all goals succeed
(defmacro lconj+ [& clauses] `(comp ~@clauses))

(defmacro conde [& clauses]
  `(ldisj+ ~@(map (fn [clause]
                    `(lconj+ ~@clause))
                  clauses)))

(defn- deep-walk [e m]
  (cond (lvar? e)       (if (contains? m e)
                          (recur (get m e) m)
                          e)
        (sequential? e) (mapv (fn [x] (deep-walk x m)) e)
        :else           e))

(defn map-and-extract [key]
  (map (partial deep-walk key)))

(defmacro run* [fresh-var-vec & goals]
  `(sequence
    (comp (fresh [~@fresh-var-vec] ~@goals)
          (map-and-extract :0))
    [{}]))

(defmacro run [n fresh-var-vec & goals]
  (assert (pos-int? n))
  `(sequence
    (comp (fresh [~@fresh-var-vec] ~@goals)
          (take ~n)
          (map-and-extract :0))
    [{}]))

(defmacro run1 [fresh-var-vec & goals]
  `(run 1 ~fresh-var-vec ~@goals))

(defmacro all [& clauses] `(lconj+ ~@clauses))
(defmacro any [& clauses] `(ldisj+ ~@clauses))


                                        ; LIST FUNCTIONS

(defn conso [head tail out]
  (if (lvar? tail)
    (=== [head :& tail] out)
    (=== (cons head tail) out)))

(defn firsto [head out]
  (fresh [tail]
    (conso head tail out)))

(defn resto [tail out]
  (fresh [head]
    (conso head tail out)))

(defn emptyo [s]
  (=== '() s))

(defn nilo [s]
  (=== nil s))

(defn appendo [seq1 seq2 out]
  (conde
   [(emptyo seq1) (=== seq2 out)]
   [(fresh [first rest rec]
      (conso first rest seq1)
      (conso first rec out)
      (appendo rest seq2 rec))]))

(defn membero [elem list]
  (any (firsto elem list)
       (fresh [d] (resto d list) (membero elem d))))

;; Chapter 7. A Bit Too Much

(defn bit-xoro [x y r]
  (conde [(=== 0 x) (=== 0 y) (=== 0 r)]
         [(=== 1 x) (=== 0 y) (=== 1 r)]
         [(=== 0 x) (=== 1 y) (=== 1 r)]
         [(=== 1 x) (=== 1 y) (=== 0 r)]))

(defn bit-ando [x y r]
  (conde [(=== 0 x) (=== 0 y) (=== 0 r)]
         [(=== 1 x) (=== 0 y) (=== 0 r)]
         [(=== 0 x) (=== 1 y) (=== 0 r)]
         [(=== 1 x) (=== 1 y) (=== 1 r)]))

(defn half-addero [x y r c]
  (all (bit-xoro x y r)
       (bit-ando x y c)))

(defn full-addero [b x y r c]
  (fresh [w xy wz]
    (half-addero x y w xy)
    (half-addero w b r wz)
    (bit-xoro xy wz c)))

(defn build-num [n]
  (cond (zero? n) []
        (even? n) (cons 0 (build-num (/ n 2)))
        (odd? n)  (cons 1 (build-num (/ (- n 1) 2)))))
