(ns vkm.pipeline
  "Main orchestration pipeline for the Knowledge Graph Evolution System.

   This namespace coordinates:
   - Fact extraction from sources
   - Patch construction
   - Morphism computation
   - Motive extraction
   - Visualization generation"
  (:require [vkm.patch :as patch]
            [vkm.morphism :as morphism]
            [vkm.semantic :as semantic]
            [vkm.db :as db]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [aero.core :as aero]
            [java-time :as jt])
  (:gen-class))

;; ============================================================
;; Configuration
;; ============================================================

(defn load-config
  "Load configuration from EDN file."
  [& {:keys [config-path]
      :or {config-path "resources/config.edn"}}]
  (aero/read-config (io/resource "config.edn")))

(def ^:dynamic *config* (atom nil))

(defn init-config!
  "Initialize configuration."
  [& opts]
  (reset! *config* (apply load-config opts)))

(defn get-config
  "Get current configuration."
  []
  @*config*)

;; ============================================================
;; Transcript processing
;; ============================================================

(defn load-transcript
  "Load a transcript JSON file."
  [filepath]
  (when (.exists (io/file filepath))
    (edn/read-string (slurp filepath))))

(defn chunk-transcript
  "Split transcript into manageable chunks for processing.

   Each chunk is approximately chunk-duration-minutes long."
  [transcript chunk-duration-minutes]
  (let [chunk-duration-seconds (* chunk-duration-minutes 60)
        segments (:transcript transcript [])

        ;; Group segments by time chunks
        chunks (reduce (fn [acc segment]
                        (let [timestamp (:timestamp segment)
                              chunk-idx (int (/ timestamp chunk-duration-seconds))
                              current-chunks (or acc {})]
                          (update current-chunks chunk-idx
                                 (fnil conj []) segment)))
                      {}
                      segments)]

    ;; Convert to vector and sort by chunk index
    (vec (map second (sort-by first chunks)))))

(defn chunk->text
  "Convert a transcript chunk to plain text."
  [chunk]
  (clojure.string/join " " (map :text chunk)))

;; ============================================================
;; Fact extraction
;; ============================================================

(defn extract-facts-from-chunk
  "Extract facts from a single transcript chunk using Claude."
  [chunk video-id chunk-idx config]
  (let [text (chunk->text chunk)
        extraction-config (get-in config [:ingestion :fact-extraction])
        confidence-threshold (:confidence-threshold extraction-config 0.5)

        ;; Extract facts using Claude
        raw-facts (semantic/extract-facts-from-text text)

        ;; Add metadata
        facts (map (fn [fact]
                    (assoc fact
                           :claim/extracted-from video-id
                           :claim/timestamp-in-video
                           (* chunk-idx
                              (get-in config [:ingestion :fact-extraction :chunk-duration-minutes] 10)
                              60)))
                  raw-facts)]

    ;; Filter by confidence
    (filter #(>= (:claim/confidence %) confidence-threshold)
           facts)))

(defn extract-facts-from-transcript
  "Extract all facts from a transcript."
  [transcript video-id config]
  (let [chunk-duration (get-in config [:ingestion :fact-extraction :chunk-duration-minutes] 10)
        chunks (chunk-transcript transcript chunk-duration)

        ;; Process chunks in parallel
        all-facts (pmap-indexed
                   (fn [idx chunk]
                     (extract-facts-from-chunk chunk video-id idx config))
                   chunks)]

    (vec (apply concat all-facts))))

(defn pmap-indexed
  "Like pmap but provides index to the function."
  [f coll]
  (pmap (fn [[idx item]] (f idx item))
       (map-indexed vector coll)))

;; ============================================================
;; Patch construction from transcripts
;; ============================================================

(defn build-patch-from-transcripts
  "Build a patch from a collection of transcript files.

   source-id: Identifier for the source (e.g., YouTube channel ID)
   transcript-files: Vector of transcript file paths
   config: Configuration map"
  [source-id transcript-files config]
  (log/info "Building patch from" (count transcript-files) "transcripts")

  (let [;; Load all transcripts
        transcripts (map load-transcript transcript-files)

        ;; Extract facts from each transcript
        all-facts (apply concat
                        (pmap (fn [t]
                               (extract-facts-from-transcript
                                t
                                (:video_id t "unknown")
                                config))
                             transcripts))

        ;; Create patch
        patch (patch/make-patch
               {:source :youtube-channel
                :source-id source-id
                :facts (vec all-facts)
                :edges []  ;; Edges will be inferred
                :metadata {:num-transcripts (count transcripts)
                          :processed-at (jt/instant)}})]

    (log/info "Created patch with" (count all-facts) "facts")
    patch))

;; ============================================================
;; Edge inference
;; ============================================================

(defn infer-edges
  "Infer edges between facts based on semantic similarity and patterns.

   This is a simple implementation. In practice, use more sophisticated
   relationship extraction."
  [patch config]
  (let [facts (:patch/facts patch)
        ;; For now, connect facts from the same topic
        topic-groups (group-by :claim/topic facts)

        edges (apply concat
                    (for [[topic group] topic-groups
                          :when (> (count group) 1)]
                      ;; Connect each fact to the next in the group
                      (map (fn [f1 f2]
                            (patch/make-edge
                             {:from-id (:db/id f1)
                              :to-id (:db/id f2)
                              :relation :supports
                              :strength 0.5}))
                          group
                          (rest group))))]

    (assoc patch :patch/edges (vec edges))))

;; ============================================================
;; Complete pipeline
;; ============================================================

(defn process-source
  "Complete pipeline for processing a source.

   Steps:
   1. Load transcripts
   2. Extract facts
   3. Build patch
   4. Add embeddings
   5. Infer edges
   6. Store in Datomic
   7. Compute morphism (if previous patch exists)
   8. Extract motives

   Returns the processed patch."
  [source-id transcript-dir & {:keys [config]
                                :or {config (get-config)}}]
  (log/info "Processing source:" source-id)

  ;; Step 1: Find all transcript files
  (let [transcript-files (vec (.listFiles (io/file transcript-dir)))
        transcript-paths (map #(.getPath %) transcript-files)

        ;; Step 2-3: Build patch
        patch (build-patch-from-transcripts source-id transcript-paths config)

        ;; Step 4: Add embeddings
        patch-with-emb (semantic/add-embeddings-to-patch
                        patch
                        :batch-size (get-in config [:embeddings :batch-size] 100))

        ;; Step 5: Infer edges
        patch-complete (infer-edges patch-with-emb config)

        ;; Step 6: Store in Datomic
        patch-id (db/store-patch! patch-complete)

        ;; Step 7: Compute morphism if previous patch exists
        prev-patch-id (db/find-latest-patch source-id)
        morphism (when (and prev-patch-id (not= prev-patch-id patch-id))
                  (let [prev-patch (db/pull-patch prev-patch-id)
                        morph (morphism/compute-morphism
                               prev-patch
                               patch-complete
                               :reason (str "Processed new transcripts: "
                                          (count transcript-files)))]
                    (morphism/add-information-gain morph prev-patch patch-complete)
                    (db/store-morphism! morph)
                    morph))

        ;; Step 8: Extract motives
        motives (semantic/extract-all-motives
                 patch-complete
                 :similarity-threshold (get-in config [:semantic :clustering :similarity-threshold] 0.75))

        ;; Store motives
        _ (doseq [motive motives]
           (db/store-motive! motive))]

    (log/info "Processing complete."
             "Patch ID:" patch-id
             "Morphism:" (if morphism "created" "none")
             "Motives:" (count motives))

    {:patch patch-complete
     :patch-id patch-id
     :morphism morphism
     :motives motives}))

;; ============================================================
;; Batch processing
;; ============================================================

(defn process-all-sources
  "Process multiple sources in batch.

   sources: Map of {source-id -> transcript-dir}"
  [sources & opts]
  (let [config (get-config)
        results (pmap (fn [[source-id dir]]
                       (try
                         (apply process-source source-id dir opts)
                         (catch Exception e
                           (log/error "Failed to process source" source-id ":"
                                     (.getMessage e))
                           {:error (.getMessage e)})))
                     sources)]
    results))

;; ============================================================
;; Visualization data export
;; ============================================================

(defn export-for-visualization
  "Export patches and motives in a format suitable for visualization.

   Returns a map with:
   - patches: All patches with their facts
   - morphisms: All transitions
   - motives: All semantic motives
   - motive-graph: Relationships between motives"
  []
  (let [db-stats (db/db-stats)
        all-morphisms (db/find-all-morphisms)
        all-motives (db/find-all-motives)

        ;; Build motive graph
        motive-graph (semantic/build-motive-graph all-motives)]

    {:stats db-stats
     :morphisms all-morphisms
     :motives all-motives
     :motive-graph motive-graph}))

(defn save-visualization-data
  "Save visualization data to a file."
  [output-path]
  (let [data (export-for-visualization)]
    (spit output-path (pr-str data))
    (log/info "Saved visualization data to" output-path)
    data))

;; ============================================================
;; CLI entry point
;; ============================================================

(defn -main
  "Main entry point for CLI.

   Usage:
   clj -M:run process --source <source-id> --transcripts <dir>
   clj -M:run export --output <file>
   clj -M:run stats"
  [& args]
  (let [[command & opts] args]
    (case command
      "init"
      (do
        (init-config!)
        (db/init-db! (get-in (get-config) [:datomic :uri]))
        (println "Initialized database and configuration"))

      "process"
      (let [{:keys [source transcripts]} (apply hash-map opts)]
        (init-config!)
        (db/init-db! (get-in (get-config) [:datomic :uri]))
        (let [result (process-source source transcripts)]
          (println "Processing complete:")
          (println "  Patch ID:" (:patch-id result))
          (println "  Motives:" (count (:motives result)))))

      "export"
      (let [{:keys [output]} (apply hash-map opts)]
        (db/init-db! (get-in (get-config) [:datomic :uri]))
        (save-visualization-data output)
        (println "Exported visualization data to" output))

      "stats"
      (do
        (db/init-db! (get-in (get-config) [:datomic :uri]))
        (let [stats (db/db-stats)]
          (println "Database statistics:")
          (doseq [[k v] stats]
            (println "  " (name k) ":" v))))

      ;; Default
      (println "Usage: clj -M:run <command> [options]"
               "\nCommands:"
               "\n  init                       - Initialize database"
               "\n  process --source <id> --transcripts <dir> - Process source"
               "\n  export --output <file>     - Export visualization data"
               "\n  stats                      - Show database statistics"))))

;; ============================================================
;; Example usage
;; ============================================================

(comment
  ;; Initialize
  (init-config!)
  (db/init-db! "datomic:mem://knowledge-graph")

  ;; Process a source
  (def result (process-source "test-channel-1"
                              "data/transcripts/test-channel-1"))

  ;; Export for visualization
  (save-visualization-data "data/viz-data.edn")

  ;; Get stats
  (db/db-stats))
