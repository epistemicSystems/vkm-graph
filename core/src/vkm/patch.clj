(ns vkm.patch
  "Core patch operations for the Knowledge Graph Evolution System.

   A patch is an immutable snapshot of knowledge at a point in time.
   This namespace provides functions for constructing, querying, and
   manipulating patches."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [java-time :as jt]
            [clojure.tools.logging :as log])
  (:import [java.util UUID]))

;; ============================================================
;; Specs for validation
;; ============================================================

(s/def ::uuid (s/and string? #(re-matches #"[0-9a-f-]{36}" %)))
(s/def ::instant inst?)
(s/def ::confidence (s/and number? #(<= 0 % 1)))

(s/def ::db/id ::uuid)
(s/def ::patch/timestamp ::instant)
(s/def ::patch/source keyword?)
(s/def ::patch/source-id string?)
(s/def ::patch/facts (s/coll-of ::fact))
(s/def ::patch/edges (s/coll-of ::edge))
(s/def ::patch/embeddings (s/coll-of ::embedding))
(s/def ::patch/metadata map?)

(s/def ::patch
  (s/keys :req [:db/id
                :patch/timestamp
                :patch/source
                :patch/facts
                :patch/edges]
          :opt [:patch/source-id
                :patch/embeddings
                :patch/metadata]))

(s/def ::claim/text string?)
(s/def ::claim/topic keyword?)
(s/def ::claim/confidence ::confidence)
(s/def ::claim/valid-from ::instant)

(s/def ::fact
  (s/keys :req [:db/id
                :claim/text
                :claim/confidence
                :claim/valid-from]
          :opt [:claim/topic
                :claim/extracted-from
                :claim/timestamp-in-video
                :claim/revises
                :claim/tags
                :claim/lod]))

(s/def ::edge/from ::uuid)
(s/def ::edge/to ::uuid)
(s/def ::edge/relation keyword?)
(s/def ::edge/strength ::confidence)

(s/def ::edge
  (s/keys :req [:db/id
                :edge/from
                :edge/to
                :edge/relation
                :edge/strength]))

;; ============================================================
;; Constructors
;; ============================================================

(defn uuid
  "Generate a new UUID string."
  []
  (str (UUID/randomUUID)))

(defn now
  "Get current instant."
  []
  (jt/instant))

(defn make-fact
  "Create a new fact/claim.

   Required:
   - text: The claim text
   - confidence: Confidence level (0-1)

   Optional:
   - topic: Keyword topic
   - extracted-from: Source identifier
   - timestamp-in-video: Position in video (seconds)
   - tags: Vector of keywords
   - lod: Level of detail (0-3)"
  [{:keys [text confidence topic extracted-from timestamp-in-video tags lod]
    :or {lod 0}}]
  (let [fact {:db/id (uuid)
              :claim/text text
              :claim/confidence confidence
              :claim/valid-from (now)
              :claim/lod lod}]
    (cond-> fact
      topic (assoc :claim/topic topic)
      extracted-from (assoc :claim/extracted-from extracted-from)
      timestamp-in-video (assoc :claim/timestamp-in-video timestamp-in-video)
      tags (assoc :claim/tags tags))))

(defn make-edge
  "Create an edge between two facts.

   Required:
   - from-id: Source fact ID
   - to-id: Target fact ID
   - relation: Keyword relation type (:supports, :contradicts, etc.)
   - strength: Relationship strength (0-1)"
  [{:keys [from-id to-id relation strength]}]
  {:db/id (uuid)
   :edge/from from-id
   :edge/to to-id
   :edge/relation relation
   :edge/strength strength})

(defn make-patch
  "Create a new patch.

   Required:
   - source: Source type keyword
   - facts: Vector of facts
   - edges: Vector of edges

   Optional:
   - source-id: Source identifier
   - embeddings: Vector of embeddings
   - metadata: Additional metadata map"
  [{:keys [source facts edges source-id embeddings metadata]
    :or {edges [] embeddings [] metadata {}}}]
  (let [patch {:db/id (uuid)
               :patch/timestamp (now)
               :patch/source source
               :patch/facts facts
               :patch/edges edges
               :patch/metadata metadata}]
    (cond-> patch
      source-id (assoc :patch/source-id source-id)
      (seq embeddings) (assoc :patch/embeddings embeddings))))

;; ============================================================
;; Query operations
;; ============================================================

(defn get-fact
  "Get a fact by ID from a patch."
  [patch fact-id]
  (first (filter #(= (:db/id %) fact-id) (:patch/facts patch))))

(defn get-facts-by-topic
  "Get all facts with a specific topic."
  [patch topic]
  (filter #(= (:claim/topic %) topic) (:patch/facts patch)))

(defn get-facts-by-confidence
  "Get facts with confidence >= threshold."
  [patch threshold]
  (filter #(>= (:claim/confidence %) threshold) (:patch/facts patch)))

(defn get-edges-from
  "Get all edges originating from a fact."
  [patch fact-id]
  (filter #(= (:edge/from %) fact-id) (:patch/edges patch)))

(defn get-edges-to
  "Get all edges pointing to a fact."
  [patch fact-id]
  (filter #(= (:edge/to %) fact-id) (:patch/edges patch)))

(defn get-related-facts
  "Get all facts directly connected to a given fact (either direction)."
  [patch fact-id]
  (let [outgoing (map :edge/to (get-edges-from patch fact-id))
        incoming (map :edge/from (get-edges-to patch fact-id))
        related-ids (concat outgoing incoming)]
    (map #(get-fact patch %) related-ids)))

;; ============================================================
;; Statistics and analysis
;; ============================================================

(defn patch-stats
  "Compute basic statistics about a patch."
  [patch]
  (let [facts (:patch/facts patch)
        edges (:patch/edges patch)]
    {:num-facts (count facts)
     :num-edges (count edges)
     :avg-confidence (if (seq facts)
                       (/ (reduce + (map :claim/confidence facts))
                          (count facts))
                       0.0)
     :topics (frequencies (map :claim/topic facts))
     :edge-types (frequencies (map :edge/relation edges))}))

(defn high-confidence-facts
  "Get facts with confidence >= 0.8."
  [patch]
  (get-facts-by-confidence patch 0.8))

(defn uncertain-facts
  "Get facts with confidence < 0.5."
  [patch]
  (filter #(< (:claim/confidence %) 0.5) (:patch/facts patch)))

;; ============================================================
;; Patch transformations
;; ============================================================

(defn add-fact
  "Add a new fact to a patch (returns new patch)."
  [patch fact]
  (update patch :patch/facts conj fact))

(defn add-edge
  "Add a new edge to a patch (returns new patch)."
  [patch edge]
  (update patch :patch/edges conj edge))

(defn update-fact-confidence
  "Update the confidence of a fact (returns new patch)."
  [patch fact-id new-confidence]
  (update patch :patch/facts
          (fn [facts]
            (map (fn [f]
                   (if (= (:db/id f) fact-id)
                     (assoc f :claim/confidence new-confidence)
                     f))
                 facts))))

(defn remove-fact
  "Remove a fact and all associated edges (returns new patch)."
  [patch fact-id]
  (-> patch
      (update :patch/facts
              (fn [facts] (remove #(= (:db/id %) fact-id) facts)))
      (update :patch/edges
              (fn [edges]
                (remove #(or (= (:edge/from %) fact-id)
                           (= (:edge/to %) fact-id))
                       edges)))))

;; ============================================================
;; Level of Detail (LOD) operations
;; ============================================================

(defn summarize-text
  "Summarize text for higher LOD levels.
   This is a placeholder - in practice, use Claude for summarization."
  [text target-lod]
  (case target-lod
    0 text
    1 (str/join " " (take 2 (str/split text #"\.")))
    2 (first (str/split text #"\."))
    3 (str/join " " (take 5 (str/split text #" ")))))

(defn create-lod-version
  "Create a new version of a fact at a specific LOD level."
  [fact target-lod]
  (assoc fact
         :claim/text (summarize-text (:claim/text fact) target-lod)
         :claim/lod target-lod))

(defn patch-at-lod
  "Get a version of the patch at a specific LOD level."
  [patch target-lod]
  (update patch :patch/facts
          (fn [facts]
            (map #(create-lod-version % target-lod) facts))))

;; ============================================================
;; Validation
;; ============================================================

(defn valid-patch?
  "Check if a patch is valid according to specs."
  [patch]
  (s/valid? ::patch patch))

(defn validate-patch
  "Validate a patch and return explanation if invalid."
  [patch]
  (if (valid-patch? patch)
    {:valid? true}
    {:valid? false
     :explanation (s/explain-str ::patch patch)}))

;; ============================================================
;; Serialization
;; ============================================================

(defn patch->edn
  "Convert a patch to EDN string."
  [patch]
  (pr-str patch))

(defn edn->patch
  "Parse EDN string to patch."
  [edn-str]
  (read-string edn-str))

(defn save-patch
  "Save a patch to a file."
  [patch filepath]
  (spit filepath (patch->edn patch))
  (log/info "Saved patch" (:db/id patch) "to" filepath))

(defn load-patch
  "Load a patch from a file."
  [filepath]
  (when (.exists (io/file filepath))
    (edn->patch (slurp filepath))))

;; ============================================================
;; Comparison utilities
;; ============================================================

(defn fact-ids
  "Get all fact IDs from a patch."
  [patch]
  (set (map :db/id (:patch/facts patch))))

(defn new-facts
  "Get facts in patch2 that are not in patch1."
  [patch1 patch2]
  (let [ids1 (fact-ids patch1)
        ids2 (fact-ids patch2)]
    (filter #(not (contains? ids1 (:db/id %)))
            (:patch/facts patch2))))

(defn removed-facts
  "Get facts in patch1 that are not in patch2."
  [patch1 patch2]
  (new-facts patch2 patch1))

(defn common-facts
  "Get facts that appear in both patches."
  [patch1 patch2]
  (let [ids1 (fact-ids patch1)
        ids2 (fact-ids patch2)
        common-ids (clojure.set/intersection ids1 ids2)]
    (filter #(contains? common-ids (:db/id %))
            (:patch/facts patch1))))

;; ============================================================
;; Example usage
;; ============================================================

(comment
  ;; Create some facts
  (def fact1 (make-fact {:text "Large models learn better"
                         :confidence 0.85
                         :topic :scaling}))

  (def fact2 (make-fact {:text "Scaling depends on parameters and data"
                         :confidence 0.92
                         :topic :scaling}))

  ;; Create an edge
  (def edge1 (make-edge {:from-id (:db/id fact1)
                         :to-id (:db/id fact2)
                         :relation :supports
                         :strength 0.8}))

  ;; Create a patch
  (def patch (make-patch {:source :youtube-channel
                          :source-id "UC123"
                          :facts [fact1 fact2]
                          :edges [edge1]}))

  ;; Query the patch
  (patch-stats patch)
  (get-facts-by-topic patch :scaling)
  (high-confidence-facts patch)

  ;; Save and load
  (save-patch patch "data/patches/test-patch.edn")
  (load-patch "data/patches/test-patch.edn"))
