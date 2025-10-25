(ns vkm.db
  "Datomic interface for the Knowledge Graph Evolution System.

   Provides functions for:
   - Database initialization
   - Temporal queries
   - Patch storage and retrieval
   - Morphism tracking"
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [vkm.patch :as patch]
            [java-time :as jt]))

;; ============================================================
;; Database connection
;; ============================================================

(defonce conn (atom nil))

(defn init-db!
  "Initialize database connection and install schema.

   URI examples:
   - In-memory: datomic:mem://knowledge-graph
   - Local dev: datomic:dev://localhost:4334/knowledge-graph
   - Datomic Cloud: use client-cloud"
  [uri & {:keys [schema-path]
          :or {schema-path "resources/schema/datomic-schema.edn"}}]
  (try
    ;; Create database
    (d/create-database uri)

    ;; Connect
    (reset! conn (d/connect uri))

    ;; Load and transact schema
    (let [schema (edn/read-string (slurp schema-path))]
      (d/transact @conn schema)
      (log/info "Database initialized and schema installed"))

    {:success true
     :uri uri}

    (catch Exception e
      (log/error "Failed to initialize database:" (.getMessage e))
      {:success false
       :error (.getMessage e)})))

(defn get-conn
  "Get current database connection."
  []
  @conn)

(defn get-db
  "Get current database value."
  []
  (when @conn
    (d/db @conn)))

;; ============================================================
;; Entity conversion helpers
;; ============================================================

(defn fact->datomic
  "Convert a fact to Datomic transaction data."
  [fact patch-id]
  (let [base {:db/id (d/tempid :db.part/user)
              :claim/text (:claim/text fact)
              :claim/confidence (:claim/confidence fact)
              :claim/valid-from (:claim/valid-from fact)
              :claim/patch [:db/id patch-id]}]
    (cond-> base
      (:claim/topic fact)
      (assoc :claim/topic (:claim/topic fact))

      (:claim/extracted-from fact)
      (assoc :claim/extracted-from (:claim/extracted-from fact))

      (:claim/timestamp-in-video fact)
      (assoc :claim/timestamp-in-video (:claim/timestamp-in-video fact))

      (:claim/tags fact)
      (assoc :claim/tags (:claim/tags fact))

      (:claim/lod fact)
      (assoc :claim/lod (:claim/lod fact)))))

(defn edge->datomic
  "Convert an edge to Datomic transaction data."
  [edge fact-id-map patch-id]
  {:db/id (d/tempid :db.part/user)
   :edge/from (get fact-id-map (:edge/from edge))
   :edge/to (get fact-id-map (:edge/to edge))
   :edge/relation (:edge/relation edge)
   :edge/strength (:edge/strength edge)
   :edge/patch [:db/id patch-id]})

(defn embedding->datomic
  "Convert an embedding to Datomic transaction data."
  [embedding fact-id-map]
  {:db/id (d/tempid :db.part/user)
   :embedding/claim (get fact-id-map (:claim-ref embedding))
   :embedding/model (:model embedding)
   :embedding/vector (pr-str (:vector embedding))})

;; ============================================================
;; Patch storage
;; ============================================================

(defn store-patch!
  "Store a patch in Datomic.

   Returns the entity ID of the stored patch."
  [patch]
  (let [db (get-db)
        patch-tempid (d/tempid :db.part/user)

        ;; Create patch entity
        patch-tx {:db/id patch-tempid
                  :patch/timestamp (:patch/timestamp patch)
                  :patch/source (:patch/source patch)
                  :patch/metadata (pr-str (:patch/metadata patch {}))}

        patch-tx (if (:patch/source-id patch)
                  (assoc patch-tx :patch/source-id (:patch/source-id patch))
                  patch-tx)

        ;; Transact patch first to get ID
        patch-result (d/transact @conn [patch-tx])
        patch-id (d/resolve-tempid (:db-after patch-result)
                                   (:tempids patch-result)
                                   patch-tempid)

        ;; Now transact facts with reference to patch
        fact-txs (map #(fact->datomic % patch-id) (:patch/facts patch))
        fact-result (d/transact @conn fact-txs)

        ;; Build map from old IDs to new Datomic IDs
        fact-tempids (map :db/id fact-txs)
        fact-id-map (zipmap (map :db/id (:patch/facts patch))
                           (map #(d/resolve-tempid (:db-after fact-result)
                                                  (:tempids fact-result)
                                                  %)
                               fact-tempids))

        ;; Transact edges
        edge-txs (map #(edge->datomic % fact-id-map patch-id)
                     (:patch/edges patch))
        _ (when (seq edge-txs)
           (d/transact @conn edge-txs))

        ;; Transact embeddings
        emb-txs (map #(embedding->datomic % fact-id-map)
                    (:patch/embeddings patch []))
        _ (when (seq emb-txs)
           (d/transact @conn emb-txs))]

    (log/info "Stored patch" (:db/id patch) "with" (count fact-txs) "facts")
    patch-id))

;; ============================================================
;; Patch retrieval
;; ============================================================

(defn pull-patch
  "Pull a complete patch entity from Datomic."
  [patch-id]
  (let [db (get-db)
        pattern '[* {:claim/_patch [*]
                     :edge/_patch [*]}]
        entity (d/pull db pattern patch-id)]
    entity))

(defn find-patches-by-source
  "Find all patches from a specific source."
  [source-id]
  (let [db (get-db)
        results (d/q '[:find ?e
                      :in $ ?source-id
                      :where [?e :patch/source-id ?source-id]]
                    db
                    source-id)]
    (map first results)))

(defn find-latest-patch
  "Find the most recent patch for a source."
  [source-id]
  (let [db (get-db)
        results (d/q '[:find (max ?timestamp) ?e
                      :in $ ?source-id
                      :where
                      [?e :patch/source-id ?source-id]
                      [?e :patch/timestamp ?timestamp]]
                    db
                    source-id)]
    (when-let [[_ patch-id] (first results)]
      patch-id)))

;; ============================================================
;; Temporal queries
;; ============================================================

(defn query-facts-at-time
  "Query facts that were valid at a specific time.

   Returns facts where :claim/valid-from <= target-time"
  [target-time]
  (let [db (get-db)
        results (d/q '[:find ?e ?text ?confidence ?topic
                      :in $ ?target-time
                      :where
                      [?e :claim/text ?text]
                      [?e :claim/confidence ?confidence]
                      [?e :claim/valid-from ?vf]
                      [(< ?vf ?target-time)]
                      (or-join [?e ?topic]
                              [?e :claim/topic ?topic]
                              [(ground :unknown) ?topic])]
                    db
                    target-time)]
    (map (fn [[e text conf topic]]
          {:db/id e
           :claim/text text
           :claim/confidence conf
           :claim/topic topic})
        results)))

(defn query-facts-by-topic
  "Query all facts about a specific topic."
  [topic]
  (let [db (get-db)
        results (d/q '[:find ?e ?text ?confidence ?valid-from
                      :in $ ?topic
                      :where
                      [?e :claim/topic ?topic]
                      [?e :claim/text ?text]
                      [?e :claim/confidence ?confidence]
                      [?e :claim/valid-from ?valid-from]]
                    db
                    topic)]
    (map (fn [[e text conf vf]]
          {:db/id e
           :claim/text text
           :claim/confidence conf
           :claim/valid-from vf})
        results)))

(defn query-revision-history
  "Get the full revision history of a claim.

   Follows the :claim/revises chain."
  [claim-id]
  (let [db (get-db)
        results (d/q '[:find ?text ?valid-from ?confidence
                      :in $ ?original
                      :where
                      [?e :claim/revises* ?original]
                      [?e :claim/text ?text]
                      [?e :claim/valid-from ?valid-from]
                      [?e :claim/confidence ?confidence]]
                    db
                    claim-id)]
    (sort-by second results)))

;; ============================================================
;; Morphism storage
;; ============================================================

(defn store-morphism!
  "Store a morphism in Datomic."
  [morphism]
  (let [morph-tx {:db/id (d/tempid :db.part/user)
                  :morphism/from [:db/id (:morphism/from morphism)]
                  :morphism/to [:db/id (:morphism/to morphism)]
                  :morphism/type (:morphism/type morphism)
                  :morphism/timestamp (:morphism/timestamp morphism)
                  :morphism/author (:morphism/author morphism "system")
                  :morphism/reason (:morphism/reason morphism "")
                  :morphism/operations (pr-str (:morphism/operations morphism))
                  :morphism/delta (pr-str (:morphism/delta morphism))
                  :morphism/information-gain (:morphism/information-gain morphism 0.0)}

        morph-tx (if (:morphism/commit-hash morphism)
                  (assoc morph-tx :morphism/commit-hash (:morphism/commit-hash morphism))
                  morph-tx)

        result (d/transact @conn [morph-tx])]

    (log/info "Stored morphism from"
             (:morphism/from morphism)
             "to"
             (:morphism/to morphism))
    result))

(defn find-morphisms-from
  "Find all morphisms originating from a patch."
  [patch-id]
  (let [db (get-db)
        results (d/q '[:find ?e
                      :in $ ?patch-id
                      :where [?e :morphism/from ?patch-id]]
                    db
                    patch-id)]
    (map first results)))

(defn find-morphisms-to
  "Find all morphisms pointing to a patch."
  [patch-id]
  (let [db (get-db)
        results (d/q '[:find ?e
                      :in $ ?patch-id
                      :where [?e :morphism/to ?patch-id]]
                    db
                    patch-id)]
    (map first results)))

(defn find-all-morphisms
  "Get all morphisms in the database."
  []
  (let [db (get-db)
        results (d/q '[:find ?e ?from ?to ?type ?gain
                      :where
                      [?e :morphism/from ?from]
                      [?e :morphism/to ?to]
                      [?e :morphism/type ?type]
                      [?e :morphism/information-gain ?gain]]
                    db)]
    (map (fn [[e from to type gain]]
          {:db/id e
           :morphism/from from
           :morphism/to to
           :morphism/type type
           :morphism/information-gain gain})
        results)))

;; ============================================================
;; Motive storage
;; ============================================================

(defn store-motive!
  "Store a motive in Datomic."
  [motive]
  (let [motive-tx {:db/id (d/tempid :db.part/user)
                   :motive/concept-words (:concept-words motive)
                   :motive/centroid (pr-str (:centroid motive))
                   :motive/confidence (:confidence motive)
                   :motive/cluster-size (:cluster-size motive)
                   :motive/members (map (fn [id] [:db/id id])
                                      (:member-claim-ids motive))
                   :motive/created-at (jt/instant)}

        result (d/transact @conn [motive-tx])]

    (log/info "Stored motive with" (:cluster-size motive) "members")
    result))

(defn find-all-motives
  "Get all motives in the database."
  []
  (let [db (get-db)
        results (d/q '[:find ?e ?words ?confidence ?size
                      :where
                      [?e :motive/concept-words ?words]
                      [?e :motive/confidence ?confidence]
                      [?e :motive/cluster-size ?size]]
                    db)]
    (map (fn [[e words conf size]]
          {:db/id e
           :concept-words words
           :confidence conf
           :cluster-size size})
        results)))

;; ============================================================
;; Database utilities
;; ============================================================

(defn reset-db!
  "Reset the database (delete and recreate).
   WARNING: This deletes all data!"
  [uri]
  (d/delete-database uri)
  (init-db! uri))

(defn db-stats
  "Get statistics about the database."
  []
  (let [db (get-db)
        num-patches (d/q '[:find (count ?e) .
                          :where [?e :patch/timestamp]]
                        db)
        num-facts (d/q '[:find (count ?e) .
                        :where [?e :claim/text]]
                      db)
        num-edges (d/q '[:find (count ?e) .
                        :where [?e :edge/relation]]
                      db)
        num-morphisms (d/q '[:find (count ?e) .
                            :where [?e :morphism/type]]
                          db)
        num-motives (d/q '[:find (count ?e) .
                          :where [?e :motive/concept-words]]
                        db)]
    {:patches num-patches
     :facts num-facts
     :edges num-edges
     :morphisms num-morphisms
     :motives num-motives}))

;; ============================================================
;; Example usage
;; ============================================================

(comment
  ;; Initialize database
  (init-db! "datomic:mem://knowledge-graph")

  ;; Create and store a patch
  (def patch (patch/make-patch
              {:source :manual
               :source-id "test-1"
               :facts [(patch/make-fact
                       {:text "Test fact"
                        :confidence 0.8
                        :topic :test})]}))

  (def patch-id (store-patch! patch))

  ;; Query
  (find-patches-by-source "test-1")
  (query-facts-by-topic :test)
  (db-stats))
