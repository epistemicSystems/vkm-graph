(ns vkm.morphism
  "Morphism operations for the Knowledge Graph Evolution System.

   A morphism is a transition from one patch to another, representing
   how understanding evolved. This namespace provides functions for
   constructing morphisms, computing information gain, and analyzing
   equivalences."
  (:require [vkm.patch :as patch]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [java-time :as jt])
  (:import [java.util UUID]))

;; ============================================================
;; Morphism construction
;; ============================================================

(defn- fact-operations
  "Compute the atomic operations on facts between two patches."
  [patch1 patch2]
  (let [facts1 (:patch/facts patch1)
        facts2 (:patch/facts patch2)
        ids1 (set (map :db/id facts1))
        ids2 (set (map :db/id facts2))

        added (set/difference ids2 ids1)
        removed (set/difference ids1 ids2)
        common (set/intersection ids1 ids2)

        ;; Build lookup maps
        fact-map1 (into {} (map (fn [f] [(:db/id f) f]) facts1))
        fact-map2 (into {} (map (fn [f] [(:db/id f) f]) facts2))

        ;; Find confidence updates
        confidence-updates
        (keep (fn [id]
                (let [f1 (get fact-map1 id)
                      f2 (get fact-map2 id)
                      old-conf (:claim/confidence f1)
                      new-conf (:claim/confidence f2)]
                  (when (not= old-conf new-conf)
                    {:op :update-confidence
                     :fact-id id
                     :old old-conf
                     :new new-conf})))
              common)]

    (concat
     (map (fn [id] {:op :add-fact
                    :fact-id id
                    :fact (get fact-map2 id)})
          added)
     (map (fn [id] {:op :remove-fact
                    :fact-id id})
          removed)
     confidence-updates)))

(defn- edge-operations
  "Compute the atomic operations on edges between two patches."
  [patch1 patch2]
  (let [edges1 (:patch/edges patch1)
        edges2 (:patch/edges patch2)
        ids1 (set (map :db/id edges1))
        ids2 (set (map :db/id edges2))

        added (set/difference ids2 ids1)
        removed (set/difference ids1 ids2)

        edge-map2 (into {} (map (fn [e] [(:db/id e) e]) edges2))]

    (concat
     (map (fn [id] {:op :add-edge
                    :edge (get edge-map2 id)})
          added)
     (map (fn [id] {:op :remove-edge
                    :edge-id id})
          removed))))

(defn- classify-morphism-type
  "Classify the type of morphism based on operations."
  [operations]
  (let [ops (group-by :op operations)
        has-adds? (contains? ops :add-fact)
        has-removes? (contains? ops :remove-fact)
        has-updates? (contains? ops :update-confidence)
        has-edge-changes? (or (contains? ops :add-edge)
                             (contains? ops :remove-edge))]
    (cond
      ;; Refutation: facts were removed
      has-removes? :refutation

      ;; Additive: new facts without structural change
      (and has-adds? (not has-edge-changes?)) :additive

      ;; Refinement: only confidence updates
      (and has-updates? (not has-adds?) (not has-edge-changes?)) :refinement

      ;; Reorganization: structural changes without new facts
      (and has-edge-changes? (not has-adds?)) :reorganization

      ;; Default: transition
      :else :transition)))

(defn compute-morphism
  "Compute a morphism from patch1 to patch2.

   Returns a morphism map with:
   - Operations performed
   - Delta statistics
   - Morphism type
   - Information gain (to be computed separately)"
  [patch1 patch2 & {:keys [reason author]
                    :or {reason "Transition"
                         author "system"}}]
  (let [fact-ops (fact-operations patch1 patch2)
        edge-ops (edge-operations patch1 patch2)
        all-ops (concat fact-ops edge-ops)

        morphism-type (classify-morphism-type all-ops)

        ;; Compute delta statistics
        facts-added (count (filter #(= (:op %) :add-fact) fact-ops))
        facts-removed (count (filter #(= (:op %) :remove-fact) fact-ops))
        edges-added (count (filter #(= (:op %) :add-edge) edge-ops))
        edges-removed (count (filter #(= (:op %) :remove-edge) edge-ops))

        morphism {:db/id (str (UUID/randomUUID))
                  :morphism/from (:db/id patch1)
                  :morphism/to (:db/id patch2)
                  :morphism/type morphism-type
                  :morphism/timestamp (jt/instant)
                  :morphism/author author
                  :morphism/reason reason
                  :morphism/operations all-ops
                  :morphism/delta {:facts-added facts-added
                                   :facts-removed facts-removed
                                   :edges-added edges-added
                                   :edges-removed edges-removed}}]
    morphism))

;; ============================================================
;; Information gain calculation
;; ============================================================

(defn- avg-confidence-change
  "Compute average confidence change across shared facts."
  [patch1 patch2]
  (let [common (patch/common-facts patch1 patch2)
        fact-map1 (into {} (map (fn [f] [(:db/id f) f])
                               (:patch/facts patch1)))
        fact-map2 (into {} (map (fn [f] [(:db/id f) f])
                               (:patch/facts patch2)))

        changes (map (fn [fact]
                      (let [id (:db/id fact)
                            conf1 (:claim/confidence (get fact-map1 id))
                            conf2 (:claim/confidence (get fact-map2 id))]
                        (- conf2 conf1)))
                    common)]

    (if (seq changes)
      (/ (reduce + changes) (count changes))
      0.0)))

(defn compute-information-gain
  "Compute information gain for a morphism.

   Information gain measures how much understanding advanced:
   - New facts added (30%)
   - Confidence increase (30%)
   - New motives discovered (40%)
   - Reorganization penalty (-10%)

   Returns a value between 0 and 1."
  [morphism patch1 patch2 & {:keys [motives1 motives2
                                     weight-new-facts 0.3
                                     weight-confidence 0.3
                                     weight-motives 0.4
                                     weight-reorg -0.1]}]
  (let [delta (:morphism/delta morphism)

        ;; Component 1: New facts (normalized by existing facts)
        facts-added (:facts-added delta)
        num-facts1 (count (:patch/facts patch1))
        new-facts-score (if (pos? num-facts1)
                         (min 1.0 (/ facts-added num-facts1))
                         (min 1.0 (/ facts-added 10.0)))

        ;; Component 2: Confidence gain
        avg-conf-gain (avg-confidence-change patch1 patch2)
        confidence-score (max 0.0 (min 1.0 avg-conf-gain))

        ;; Component 3: New motives (if provided)
        new-motives-score (if (and motives1 motives2)
                           (let [delta-motives (- (count motives2) (count motives1))]
                             (max 0.0 (min 1.0 (/ delta-motives 5.0))))
                           0.0)

        ;; Component 4: Reorganization penalty (if no new facts)
        reorg-penalty (if (and (zero? facts-added)
                              (pos? (:edges-added delta)))
                       (* weight-reorg
                          (min 1.0 (/ (:edges-added delta) 10.0)))
                       0.0)

        ;; Combined score
        total-gain (+ (* weight-new-facts new-facts-score)
                     (* weight-confidence confidence-score)
                     (* weight-motives new-motives-score)
                     reorg-penalty)]

    (max 0.0 (min 1.0 total-gain))))

(defn add-information-gain
  "Add information gain to a morphism."
  [morphism patch1 patch2 & opts]
  (let [info-gain (apply compute-information-gain morphism patch1 patch2 opts)]
    (assoc morphism :morphism/information-gain info-gain)))

;; ============================================================
;; Observational equivalence
;; ============================================================

(defn- random-semantic-query
  "Generate a random semantic query for testing.

   Queries test:
   - Topic-based filtering
   - Confidence thresholds
   - Relationship patterns"
  [seed]
  (let [rng (java.util.Random. seed)
        query-type (.nextInt rng 3)]
    (case query-type
      0 ;; Topic query
      (fn [patch]
        (let [topics (set (map :claim/topic (:patch/facts patch)))]
          (count topics)))

      1 ;; Confidence query
      (fn [patch]
        (let [threshold (+ 0.5 (* 0.3 (.nextDouble rng)))]
          (count (patch/get-facts-by-confidence patch threshold))))

      2 ;; Relationship query
      (fn [patch]
        (count (:patch/edges patch))))))

(defn observational-equivalent?
  "Test if two patches are observationally equivalent via randomized queries.

   Two patches are equivalent if they respond identically to a large
   number of random queries.

   Parameters:
   - num-tests: Number of random queries to test (default 1000)
   - confidence-threshold: Required pass rate (default 0.95)
   - seed: Random seed for reproducibility"
  [patch1 patch2 & {:keys [num-tests confidence-threshold seed]
                    :or {num-tests 1000
                         confidence-threshold 0.95
                         seed 42}}]
  (let [queries (map random-semantic-query (range seed (+ seed num-tests)))

        results (map (fn [query]
                      (let [r1 (query patch1)
                            r2 (query patch2)]
                        (= r1 r2)))
                    queries)

        pass-rate (/ (count (filter true? results))
                    num-tests)

        passes? (>= pass-rate confidence-threshold)]

    {:equivalent? passes?
     :pass-rate pass-rate
     :tests-run num-tests
     :confidence confidence-threshold}))

;; ============================================================
;; Yoneda morphism neighborhood
;; ============================================================

(defn morphism-neighborhood
  "Compute the Yoneda morphism neighborhood of a patch.

   The neighborhood consists of:
   - All morphisms INTO this patch (predecessors)
   - All morphisms FROM this patch (successors)
   - Semantic query responses
   - Structural properties

   This is the basis for equivalence checking via Yoneda."
  [patch & {:keys [morphisms semantic-queries]}]
  (let [patch-id (:db/id patch)

        ;; Find predecessors and successors
        predecessors (filter #(= (:morphism/to %) patch-id) (or morphisms []))
        successors (filter #(= (:morphism/from %) patch-id) (or morphisms []))

        ;; Compute semantic responses
        semantic-responses (when semantic-queries
                            (into {} (map (fn [q] [(:name q) ((:fn q) patch)])
                                        semantic-queries)))

        ;; Structural properties
        structural {:num-facts (count (:patch/facts patch))
                   :num-edges (count (:patch/edges patch))
                   :avg-confidence (:avg-confidence (patch/patch-stats patch))
                   :topics (:topics (patch/patch-stats patch))}]

    {:patch-id patch-id
     :predecessors (map :db/id predecessors)
     :successors (map :db/id successors)
     :semantic-responses semantic-responses
     :structural structural}))

(defn yoneda-equivalent?
  "Check if two patches have identical Yoneda neighborhoods.

   Two patches are Yoneda-equivalent if they have the same morphism
   neighborhoods - they map to and from the same patches in the same ways."
  [patch1 patch2 & opts]
  (let [n1 (apply morphism-neighborhood patch1 opts)
        n2 (apply morphism-neighborhood patch2 opts)]
    (= n1 n2)))

;; ============================================================
;; Morphism chains and paths
;; ============================================================

(defn find-path
  "Find a path of morphisms from patch1 to patch2.

   Returns a sequence of morphisms connecting the patches,
   or nil if no path exists.

   This implements path-finding in the moduli stack."
  [patch1-id patch2-id morphisms]
  (let [graph (group-by :morphism/from morphisms)]
    (loop [current patch1-id
           path []
           visited #{}]
      (cond
        ;; Found the target
        (= current patch2-id)
        path

        ;; Already visited this patch (cycle)
        (contains? visited current)
        nil

        ;; Explore neighbors
        :else
        (let [neighbors (get graph current)]
          (if (empty? neighbors)
            nil
            (first
             (keep (fn [morph]
                    (find-path (:morphism/to morph)
                              patch2-id
                              morphisms))
                  neighbors))))))))

(defn chain-morphisms
  "Chain multiple morphisms together.

   Returns a composite morphism representing the entire transformation."
  [morphisms]
  (when (seq morphisms)
    (let [first-morph (first morphisms)
          last-morph (last morphisms)

          all-operations (mapcat :morphism/operations morphisms)

          total-delta (reduce (fn [acc m]
                               (merge-with + acc (:morphism/delta m)))
                             {:facts-added 0
                              :facts-removed 0
                              :edges-added 0
                              :edges-removed 0}
                             morphisms)

          total-info-gain (reduce + (map :morphism/information-gain morphisms))]

      {:db/id (str (UUID/randomUUID))
       :morphism/from (:morphism/from first-morph)
       :morphism/to (:morphism/to last-morph)
       :morphism/type :composite
       :morphism/timestamp (jt/instant)
       :morphism/author "system"
       :morphism/reason (str "Composite of " (count morphisms) " morphisms")
       :morphism/operations all-operations
       :morphism/delta total-delta
       :morphism/information-gain total-info-gain
       :morphism/chain morphisms})))

;; ============================================================
;; Example usage
;; ============================================================

(comment
  ;; Create two patches
  (def patch1 (patch/make-patch {:source :manual
                                  :facts [(patch/make-fact
                                          {:text "Fact 1"
                                           :confidence 0.7})]
                                  :edges []}))

  (def patch2 (patch/make-patch {:source :manual
                                  :facts [(patch/make-fact
                                          {:text "Fact 1"
                                           :confidence 0.9})
                                         (patch/make-fact
                                          {:text "Fact 2"
                                           :confidence 0.8})]
                                  :edges []}))

  ;; Compute morphism
  (def m (compute-morphism patch1 patch2
                          :reason "Added new fact and increased confidence"))

  ;; Add information gain
  (def m-with-gain (add-information-gain m patch1 patch2))

  ;; Test observational equivalence
  (observational-equivalent? patch1 patch2)

  ;; Compute Yoneda neighborhood
  (morphism-neighborhood patch1 :morphisms [m]))
