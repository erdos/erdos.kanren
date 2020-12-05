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

(declare unify)

(defn- unify-seq [u v substitutions]
  (cond
    (= u v)          substitutions
    (= :& (first u)) (unify (second u) v substitutions)
    (= :& (first v)) (unify u (second v) substitutions)

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
      (sequential? u) (unify-seq u v substitutions)
      :else     nil ;; can not unify two scalars
      )))

(defn === [u v]
  (fn [rf]
    (fn
      ([] (rf))
      ([a] (rf a))
      ([a s]
       (if-let [s2 (unify u v s)]
         (rf a s2)
         a)))))

;; parameter is a function: lvar -> transducer
;;
;; original implementation without metas:
;; (goal-ctor (lvar))
(defn call-fresh [goal-ctor]
  (assert (fn? goal-ctor))
  (fn [rf]
    (fn
      ([] (rf))
      ([a] (rf a))
      ([a s]
       (assert (some? s)) ;; TODO: turn it on!
       (let [new-var (if-let [cnt (-> s meta ::vars count)]
                       (lvar (str cnt))
                       (lvar "0"))]
         (((goal-ctor new-var) rf)
          a
          (vary-meta s update ::vars (fnil conj #{}) new-var)))))))

(defmacro fresh [var-vec & clauses]
  (assert (vector? var-vec))
  (assert (every? symbol? var-vec))
  `(-> (lconj+ ~@clauses)
       ~@(for [v var-vec]
           `(->> (fn [~v]) (call-fresh)))))

;; always fails
(defn fail [& comment]
  (fn [rf] (fn ([] (rf)) ([a] (rf a)) ([a s] a))))

;; always successful
(defn succeed [& comment]
  identity)

;; returns a goal that succeeds whenever goal1 or goal2 succeeds
(defmacro ldisj+ [& goals]
  (let [s  (gensym "s")
        rf (gensym "rs")]
    `(fn [~rf]
       (fn
         ([] (~rf))
         ([a#] (~rf a#))
         ([a# ~s]
          (-> a#
              ~@(for [g goals]
                  (list (list g rf) s))))))))

;; returns a goal that succeeds when all goals succeed
(defmacro lconj+ [& clauses] `(comp ~@clauses))

(defmacro conde [& clauses]
  `(ldisj+ ~@(map (fn [clause]
                    `(lconj+ ~@clause))
                  clauses)))

(defn- deep-walk [e m]
  (cond (lvar? e)       (recur (get m lvar) m)
        (sequential? e) (mapv (fn [x] (deep-walk x m)) e)
        :else           e))

(defn map-and-extract [key]
  (map (fn [s]
         (println "Substitutions: " s)
         (if (contains? s key)
           (deep-walk (get s key) s)
           s))))

(defmacro run* [fresh-var-vec & goals]
  `(sequence
    (comp (fresh [~@fresh-var-vec] ~@goals)
          (map-and-extract :0))
    [{}]))

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
