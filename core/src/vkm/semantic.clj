(ns vkm.semantic
  "Semantic analysis for the Knowledge Graph Evolution System.

   This namespace provides functions for:
   - Computing embeddings
   - Semantic similarity and clustering
   - Tropical skeleton extraction
   - Motive discovery"
  (:require [vkm.patch :as patch]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:import [java.util UUID]))

;; ============================================================
;; Configuration
;; ============================================================

(def ^:dynamic *openai-api-key* (System/getenv "OPENAI_API_KEY"))
(def ^:dynamic *claude-api-key* (System/getenv "CLAUDE_API_KEY"))

(def embedding-model "text-embedding-3-small")
(def embedding-dimensions 1536)

;; ============================================================
;; Embedding computation
;; ============================================================

(defn compute-embedding
  "Compute embedding vector for a text using OpenAI API.

   Returns a vector of floats."
  [text & {:keys [model api-key]
           :or {model embedding-model
                api-key *openai-api-key*}}]
  (try
    (let [response (http/post "https://api.openai.com/v1/embeddings"
                             {:headers {"Authorization" (str "Bearer " api-key)
                                       "Content-Type" "application/json"}
                              :body (json/generate-string
                                    {:input text
                                     :model model})
                              :as :json})
          embedding (get-in response [:body :data 0 :embedding])]
      (vec embedding))
    (catch Exception e
      (log/error "Failed to compute embedding:" (.getMessage e))
      nil)))

(defn batch-compute-embeddings
  "Compute embeddings for multiple texts in parallel.

   Returns a map of {text -> embedding-vector}"
  [texts & {:keys [batch-size]
            :or {batch-size 100}}]
  (let [batches (partition-all batch-size texts)
        results (atom {})]
    (doseq [batch batches]
      (let [batch-results (pmap compute-embedding batch)]
        (swap! results merge (zipmap batch batch-results))))
    @results))

(defn add-embeddings-to-patch
  "Add embeddings to all facts in a patch.

   Returns updated patch with :patch/embeddings populated."
  [patch & opts]
  (let [facts (:patch/facts patch)
        texts (map :claim/text facts)
        fact-ids (map :db/id facts)

        ;; Compute embeddings
        embeddings-map (apply batch-compute-embeddings texts opts)

        ;; Build embedding records
        embeddings (map (fn [fact-id text]
                         (when-let [vec (get embeddings-map text)]
                           {:embedding-id (str (UUID/randomUUID))
                            :claim-ref fact-id
                            :model embedding-model
                            :vector vec}))
                       fact-ids
                       texts)]

    (assoc patch :patch/embeddings (remove nil? embeddings))))

;; ============================================================
;; Similarity metrics
;; ============================================================

(defn cosine-similarity
  "Compute cosine similarity between two vectors."
  [v1 v2]
  (when (and v1 v2 (= (count v1) (count v2)))
    (let [dot-product (reduce + (map * v1 v2))
          magnitude1 (Math/sqrt (reduce + (map #(* % %) v1)))
          magnitude2 (Math/sqrt (reduce + (map #(* % %) v2)))]
      (if (and (pos? magnitude1) (pos? magnitude2))
        (/ dot-product (* magnitude1 magnitude2))
        0.0))))

(defn euclidean-distance
  "Compute Euclidean distance between two vectors."
  [v1 v2]
  (when (and v1 v2 (= (count v1) (count v2)))
    (Math/sqrt (reduce + (map (fn [a b] (* (- a b) (- a b))) v1 v2)))))

(defn semantic-similarity
  "Compute semantic similarity between two texts.

   Uses cached embeddings if available, otherwise computes them."
  [text1 text2 & {:keys [embeddings-cache]
                  :or {embeddings-cache {}}}]
  (let [emb1 (or (get embeddings-cache text1)
                (compute-embedding text1))
        emb2 (or (get embeddings-cache text2)
                (compute-embedding text2))]
    (cosine-similarity emb1 emb2)))

;; ============================================================
;; Clustering
;; ============================================================

(defn build-similarity-graph
  "Build a similarity graph from embeddings.

   Returns a graph where edges connect facts with similarity >= threshold."
  [embeddings threshold]
  (let [pairs (for [e1 embeddings
                    e2 embeddings
                    :when (not= (:claim-ref e1) (:claim-ref e2))]
                [e1 e2])

        edges (keep (fn [[e1 e2]]
                     (let [sim (cosine-similarity (:vector e1) (:vector e2))]
                       (when (>= sim threshold)
                         {:from (:claim-ref e1)
                          :to (:claim-ref e2)
                          :weight sim})))
                   pairs)]
    {:nodes (set (map :claim-ref embeddings))
     :edges edges}))

(defn connected-components
  "Find connected components in a graph (basic implementation).

   Returns a vector of clusters, where each cluster is a set of node IDs."
  [graph]
  (let [nodes (:nodes graph)
        edges (:edges graph)

        ;; Build adjacency list
        adj (reduce (fn [acc edge]
                     (-> acc
                         (update (:from edge) (fnil conj #{}) (:to edge))
                         (update (:to edge) (fnil conj #{}) (:from edge))))
                   {}
                   edges)

        ;; DFS to find component
        dfs (fn dfs [node visited component]
              (if (contains? visited node)
                [visited component]
                (let [visited' (conj visited node)
                      component' (conj component node)
                      neighbors (get adj node #{})]
                  (reduce (fn [[v c] neighbor]
                           (dfs neighbor v c))
                         [visited' component']
                         neighbors))))

        ;; Find all components
        [_ components]
        (reduce (fn [[visited comps] node]
                 (if (contains? visited node)
                   [visited comps]
                   (let [[visited' comp] (dfs node visited #{})]
                     [visited' (conj comps comp)])))
               [#{} []]
               nodes)]

    components))

(defn cluster-by-similarity
  "Cluster facts by semantic similarity.

   Returns a vector of clusters, where each cluster contains fact IDs."
  [patch threshold]
  (let [embeddings (:patch/embeddings patch)]
    (if (empty? embeddings)
      (do
        (log/warn "No embeddings found in patch, cannot cluster")
        [])
      (let [graph (build-similarity-graph embeddings threshold)
            clusters (connected-components graph)]
        ;; Filter out singleton clusters
        (filter #(> (count %) 1) clusters)))))

;; ============================================================
;; Motive extraction
;; ============================================================

(defn semantic-centroid
  "Compute the centroid of a set of embedding vectors."
  [vectors]
  (when (seq vectors)
    (let [n (count vectors)
          dim (count (first vectors))
          sums (reduce (fn [acc v]
                        (map + acc v))
                      (repeat dim 0.0)
                      vectors)]
      (vec (map #(/ % n) sums)))))

(defn extract-concept-words
  "Extract concept words from a cluster of facts.

   This is a simple implementation that finds common words.
   In production, use Claude for more sophisticated extraction."
  [facts top-k]
  (let [all-texts (map :claim/text facts)
        ;; Tokenize (simple word splitting)
        all-words (mapcat #(-> %
                             str/lower-case
                             (str/split #"\W+"))
                         all-texts)
        ;; Filter stopwords (basic list)
        stopwords #{"the" "a" "an" "and" "or" "but" "in" "on" "at"
                   "to" "for" "of" "with" "by" "is" "are" "was" "were"}
        filtered (remove stopwords all-words)

        ;; Count frequencies
        freqs (frequencies filtered)

        ;; Get top K
        top-words (take top-k (sort-by (comp - val) freqs))]

    (vec (map first top-words))))

(defn extract-motive
  "Extract a motive from a cluster of facts.

   A motive represents the essential concept of a semantic cluster."
  [patch cluster-ids & {:keys [concept-words-count]
                        :or {concept-words-count 5}}]
  (let [;; Get facts in cluster
        facts (filter #(contains? cluster-ids (:db/id %))
                     (:patch/facts patch))

        ;; Get embeddings for cluster
        embeddings (filter #(contains? cluster-ids (:claim-ref %))
                          (:patch/embeddings patch))
        vectors (map :vector embeddings)

        ;; Compute centroid
        centroid (semantic-centroid vectors)

        ;; Extract concept words
        concept-words (extract-concept-words facts concept-words-count)

        ;; Compute confidence (coverage across cluster)
        confidence (if (seq concept-words) 0.8 0.3)]

    {:id (str (UUID/randomUUID))
     :concept-words concept-words
     :centroid centroid
     :confidence confidence
     :cluster-size (count cluster-ids)
     :member-claim-ids (vec cluster-ids)}))

(defn extract-all-motives
  "Extract all motives from a patch using clustering."
  [patch & {:keys [similarity-threshold concept-words-count]
            :or {similarity-threshold 0.75
                 concept-words-count 5}}]
  (let [clusters (cluster-by-similarity patch similarity-threshold)
        motives (map (fn [cluster]
                      (extract-motive patch cluster
                                     :concept-words-count concept-words-count))
                    clusters)]
    (vec motives)))

;; ============================================================
;; Motive relationships
;; ============================================================

(defn motive-similarity
  "Compute similarity between two motives based on centroids."
  [motive1 motive2]
  (cosine-similarity (:centroid motive1) (:centroid motive2)))

(defn build-motive-graph
  "Build a graph of relationships between motives."
  [motives & {:keys [threshold]
              :or {threshold 0.6}}]
  (let [pairs (for [m1 motives
                    m2 motives
                    :when (not= (:id m1) (:id m2))]
                [m1 m2])

        edges (keep (fn [[m1 m2]]
                     (let [sim (motive-similarity m1 m2)]
                       (when (>= sim threshold)
                         {:from (:id m1)
                          :to (:id m2)
                          :relation :analogous
                          :strength sim})))
                   pairs)]

    {:nodes motives
     :edges edges}))

;; ============================================================
;; Tropical geometry (placeholder)
;; ============================================================

(defn tropicalize
  "Extract tropical skeleton from a continuous region.

   This is a placeholder implementation. Full tropical geometry
   requires more sophisticated algorithms.

   For now, we approximate by finding linear pieces in the embedding space."
  [vectors target-dimension]
  (log/info "Tropicalization placeholder - using PCA approximation")
  ;; In practice, implement:
  ;; 1. Sample the space densely
  ;; 2. Compute gradient field
  ;; 3. Identify piecewise-linear structure
  ;; 4. Extract polyhedral complex
  ;; For now, return centroid as approximation
  (let [centroid (semantic-centroid vectors)]
    {:skeleton centroid
     :dimension target-dimension
     :vertices [centroid]
     :edges []
     :faces []}))

;; ============================================================
;; Fact extraction using Claude
;; ============================================================

(defn extract-facts-from-text
  "Extract structured facts from text using Claude API.

   Returns a vector of fact maps with enhanced error handling and retry logic."
  [text & {:keys [api-key model max-retries]
           :or {api-key *claude-api-key*
                model "claude-sonnet-4-20250514"
                max-retries 3}}]
  (if (or (nil? api-key) (str/blank? api-key))
    (do
      (log/warn "CLAUDE_API_KEY not set, returning empty facts")
      [])
    (letfn [(attempt [retry-count]
              (try
                (let [prompt (str "You are a knowledge extraction system for a temporal knowledge graph. "
                                 "Extract structured factual claims from the following text.\n\n"
                                 "For each fact, provide:\n"
                                 "- text: A clear, atomic claim (one fact per entry)\n"
                                 "- confidence: Your certainty this is factual (0.0-1.0)\n"
                                 "- topic: A single keyword category (e.g., 'scaling', 'architecture', 'performance')\n\n"
                                 "Guidelines:\n"
                                 "- Break complex statements into atomic facts\n"
                                 "- Only extract verifiable claims, not opinions\n"
                                 "- Use confidence < 0.6 for uncertain claims\n"
                                 "- Keep text concise and clear\n\n"
                                 "Text to analyze:\n---\n" text "\n---\n\n"
                                 "Respond ONLY with a valid JSON array, nothing else:\n"
                                 "[{\"text\": \"...\", \"confidence\": 0.85, \"topic\": \"scaling\"}]")

                      response (http/post "https://api.anthropic.com/v1/messages"
                                         {:headers {"x-api-key" api-key
                                                   "anthropic-version" "2023-06-01"
                                                   "Content-Type" "application/json"}
                                          :body (json/generate-string
                                                {:model model
                                                 :max_tokens 4096
                                                 :temperature 0.0
                                                 :messages [{:role "user"
                                                            :content prompt}]})
                                          :as :json
                                          :socket-timeout 30000
                                          :connection-timeout 10000})

                      content (get-in response [:body :content 0 :text])

                      ;; Extract JSON from markdown code blocks if present
                      json-str (if (str/includes? content "```")
                                 (second (re-find #"```(?:json)?\s*\n([\s\S]*?)\n```" content))
                                 content)

                      ;; Parse JSON from Claude's response
                      facts-data (json/parse-string (or json-str content) true)]

                  (log/info "Extracted" (count facts-data) "facts from text")

                  ;; Convert to fact maps
                  (vec
                   (map (fn [fact-data]
                         (patch/make-fact
                          {:text (:text fact-data)
                           :confidence (or (:confidence fact-data) 0.5)
                           :topic (keyword (or (:topic fact-data) "general"))}))
                       facts-data)))

                (catch clojure.lang.ExceptionInfo e
                  (let [status (-> e ex-data :status)]
                    (cond
                      ;; Rate limit - wait and retry
                      (= status 429)
                      (if (< retry-count max-retries)
                        (do
                          (log/warn "Rate limited, retrying in" (* 2 retry-count) "seconds...")
                          (Thread/sleep (* 2000 retry-count))
                          (attempt (inc retry-count)))
                        (do
                          (log/error "Max retries exceeded for rate limiting")
                          []))

                      ;; Other HTTP errors
                      :else
                      (do
                        (log/error "HTTP error" status ":" (.getMessage e))
                        []))))

                (catch com.fasterxml.jackson.core.JsonParseException e
                  (log/error "Failed to parse JSON response from Claude:" (.getMessage e))
                  [])

                (catch Exception e
                  (log/error "Failed to extract facts from text:" (.getMessage e))
                  (when (< retry-count max-retries)
                    (log/info "Retrying... (" (inc retry-count) "/" max-retries ")")
                    (Thread/sleep 1000)
                    (attempt (inc retry-count)))
                  [])))]
      (attempt 0))))

;; ============================================================
;; Enhanced AI: Contradiction Detection
;; ============================================================

(defn detect-contradictions
  "Detect contradicting facts using embeddings and Claude.

   Returns a vector of contradiction relationships."
  [patch & {:keys [similarity-threshold api-key]
            :or {similarity-threshold 0.75
                 api-key *claude-api-key*}}]
  (if (or (nil? api-key) (str/blank? api-key))
    []
    (let [facts (:patch/facts patch)
          embeddings (:patch/embeddings patch)

          ;; Build fact-id -> embedding map
          embedding-map (into {} (map (fn [emb]
                                       [(:claim-ref emb) (:vector emb)])
                                     embeddings))

          ;; Find fact pairs with high semantic similarity (potential contradictions)
          candidates (for [f1 facts
                          f2 facts
                          :when (not= (:db/id f1) (:db/id f2))
                          :let [emb1 (get embedding-map (:db/id f1))
                                emb2 (get embedding-map (:db/id f2))
                                sim (when (and emb1 emb2) (cosine-similarity emb1 emb2))]
                          :when (and sim (> sim similarity-threshold))]
                      [f1 f2 sim])]

      ;; Use Claude to verify contradictions
      (log/info "Checking" (count candidates) "candidate pairs for contradictions")

      (vec
       (keep (fn [[f1 f2 sim]]
               (try
                 (let [prompt (str "Analyze if these two statements contradict each other.\n\n"
                                  "Statement 1: " (:claim/text f1) "\n"
                                  "Statement 2: " (:claim/text f2) "\n\n"
                                  "Respond with JSON:\n"
                                  "{\"contradicts\": true/false, "
                                  "\"reason\": \"brief explanation\", "
                                  "\"strength\": 0.0-1.0}")

                       response (http/post "https://api.anthropic.com/v1/messages"
                                          {:headers {"x-api-key" api-key
                                                    "anthropic-version" "2023-06-01"
                                                    "Content-Type" "application/json"}
                                           :body (json/generate-string
                                                 {:model "claude-sonnet-4-20250514"
                                                  :max_tokens 500
                                                  :temperature 0.0
                                                  :messages [{:role "user"
                                                             :content prompt}]})
                                           :as :json
                                           :socket-timeout 15000})

                       content (get-in response [:body :content 0 :text])
                       json-str (if (str/includes? content "```")
                                 (second (re-find #"```(?:json)?\s*\n([\s\S]*?)\n```" content))
                                 content)
                       result (json/parse-string (or json-str content) true)]

                   (when (:contradicts result)
                     (patch/make-edge
                      {:from (:db/id f1)
                       :to (:db/id f2)
                       :relation :contradicts
                       :strength (:strength result 0.8)})))

                 (catch Exception e
                   (log/warn "Failed to check contradiction:" (.getMessage e))
                   nil)))
             candidates)))))

;; ============================================================
;; Enhanced AI: Relationship Extraction
;; ============================================================

(defn extract-relationships
  "Extract semantic relationships between facts using Claude.

   Returns a vector of edges (supports, contradicts, revises)."
  [patch & {:keys [api-key batch-size]
            :or {api-key *claude-api-key*
                 batch-size 10}}]
  (if (or (nil? api-key) (str/blank? api-key))
    []
    (let [facts (:patch/facts patch)
          fact-texts (map :claim/text facts)
          fact-ids (map :db/id facts)]

      (log/info "Extracting relationships between" (count facts) "facts")

      ;; Process facts in batches
      (vec
       (mapcat
        (fn [batch-facts]
          (try
            (let [;; Create numbered list of facts
                  facts-list (str/join "\n"
                                      (map-indexed
                                       (fn [i f] (str (inc i) ". " (:claim/text f)))
                                       batch-facts))

                  prompt (str "Analyze relationships between these factual claims.\n\n"
                             "Facts:\n" facts-list "\n\n"
                             "For each pair of related facts, identify the relationship type:\n"
                             "- 'supports': One fact provides evidence for another\n"
                             "- 'contradicts': Facts conflict with each other\n"
                             "- 'revises': One fact updates/refines another\n\n"
                             "Respond with JSON array:\n"
                             "[{\"from\": 1, \"to\": 2, \"relation\": \"supports\", \"strength\": 0.8}]\n"
                             "Only include strong relationships (strength > 0.6).")

                  response (http/post "https://api.anthropic.com/v1/messages"
                                     {:headers {"x-api-key" api-key
                                               "anthropic-version" "2023-06-01"
                                               "Content-Type" "application/json"}
                                      :body (json/generate-string
                                            {:model "claude-sonnet-4-20250514"
                                             :max_tokens 2000
                                             :temperature 0.0
                                             :messages [{:role "user"
                                                        :content prompt}]})
                                      :as :json
                                      :socket-timeout 30000})

                  content (get-in response [:body :content 0 :text])
                  json-str (if (str/includes? content "```")
                            (second (re-find #"```(?:json)?\s*\n([\s\S]*?)\n```" content))
                            content)
                  relationships (json/parse-string (or json-str content) true)]

              ;; Convert to edges
              (map (fn [rel]
                    (let [from-idx (dec (:from rel))
                          to-idx (dec (:to rel))
                          from-fact (nth batch-facts from-idx)
                          to-fact (nth batch-facts to-idx)]
                      (patch/make-edge
                       {:from (:db/id from-fact)
                        :to (:db/id to-fact)
                        :relation (keyword (:relation rel))
                        :strength (:strength rel 0.7)})))
                   relationships))

            (catch Exception e
              (log/warn "Failed to extract relationships for batch:" (.getMessage e))
              [])))
        (partition-all batch-size facts))))))

;; ============================================================
;; Enhanced AI: Semantic Deduplication
;; ============================================================

(defn find-duplicates
  "Find semantically duplicate facts using embeddings.

   Returns a map of {canonical-id -> [duplicate-ids]}."
  [patch & {:keys [similarity-threshold]
            :or {similarity-threshold 0.92}}]
  (let [facts (:patch/facts patch)
        embeddings (:patch/embeddings patch)

        ;; Build fact-id -> embedding map
        embedding-map (into {} (map (fn [emb]
                                     [(:claim-ref emb) (:vector emb)])
                                   embeddings))

        ;; Find similar fact pairs
        duplicates (atom {})]

    (doseq [f1 facts
            f2 facts
            :when (not= (:db/id f1) (:db/id f2))
            :let [emb1 (get embedding-map (:db/id f1))
                  emb2 (get embedding-map (:db/id f2))
                  sim (when (and emb1 emb2) (cosine-similarity emb1 emb2))]
            :when (and sim (>= sim similarity-threshold))]

      ;; Choose canonical fact (higher confidence wins)
      (let [canonical (if (>= (:claim/confidence f1) (:claim/confidence f2))
                       (:db/id f1)
                       (:db/id f2))
            duplicate (if (= canonical (:db/id f1))
                       (:db/id f2)
                       (:db/id f1))]

        (swap! duplicates update canonical
               (fn [dups] (conj (or dups []) duplicate)))))

    @duplicates))

(defn merge-duplicates
  "Merge duplicate facts, keeping the highest confidence version.

   Returns updated patch with duplicates removed."
  [patch duplicates]
  (let [all-duplicate-ids (set (mapcat second duplicates))

        ;; Keep only non-duplicate facts and canonical versions
        kept-facts (filter #(not (contains? all-duplicate-ids (:db/id %)))
                          (:patch/facts patch))

        ;; Update edges to point to canonical facts
        updated-edges (map (fn [edge]
                            (let [from (:edge/from edge)
                                  to (:edge/to edge)

                                  ;; Find canonical IDs
                                  canonical-from (or (some (fn [[canon dups]]
                                                            (when (some #{from} dups) canon))
                                                          duplicates)
                                                    from)
                                  canonical-to (or (some (fn [[canon dups]]
                                                          (when (some #{to} dups) canon))
                                                        duplicates)
                                                  to)]
                              (assoc edge
                                     :edge/from canonical-from
                                     :edge/to canonical-to)))
                          (:patch/edges patch))]

    (log/info "Merged" (count all-duplicate-ids) "duplicate facts into" (count duplicates) "canonical versions")

    (assoc patch
           :patch/facts kept-facts
           :patch/edges updated-edges)))

;; ============================================================
;; Enhanced AI: Multi-modal Processing
;; ============================================================

(defn extract-from-image
  "Extract text from images using OCR and analyze with Claude.

   Returns extracted facts."
  [image-path & {:keys [api-key]
                 :or {api-key *claude-api-key*}}]
  (if (or (nil? api-key) (str/blank? api-key))
    []
    (try
      ;; Read image as base64
      (let [image-bytes (with-open [in (java.io.FileInputStream. image-path)]
                         (let [bytes (byte-array (.available in))]
                           (.read in bytes)
                           bytes))

            image-b64 (-> (java.util.Base64/getEncoder)
                         (.encodeToString image-bytes))

            ;; Detect image type
            image-type (cond
                        (str/ends-with? image-path ".png") "image/png"
                        (str/ends-with? image-path ".jpg") "image/jpeg"
                        (str/ends-with? image-path ".jpeg") "image/jpeg"
                        :else "image/png")

            prompt "Extract all factual claims from this image (diagrams, charts, text, tables). Return as JSON array: [{\"text\": \"...\", \"confidence\": 0.8, \"topic\": \"...\""}]"

            response (http/post "https://api.anthropic.com/v1/messages"
                               {:headers {"x-api-key" api-key
                                         "anthropic-version" "2023-06-01"
                                         "Content-Type" "application/json"}
                                :body (json/generate-string
                                      {:model "claude-sonnet-4-20250514"
                                       :max_tokens 4096
                                       :temperature 0.0
                                       :messages [{:role "user"
                                                  :content [{:type "image"
                                                            :source {:type "base64"
                                                                    :media_type image-type
                                                                    :data image-b64}}
                                                           {:type "text"
                                                            :text prompt}]}]})
                                :as :json
                                :socket-timeout 45000})

            content (get-in response [:body :content 0 :text])
            json-str (if (str/includes? content "```")
                      (second (re-find #"```(?:json)?\s*\n([\s\S]*?)\n```" content))
                      content)
            facts-data (json/parse-string (or json-str content) true)]

        (log/info "Extracted" (count facts-data) "facts from image:" image-path)

        (vec
         (map (fn [fact-data]
               (patch/make-fact
                {:text (:text fact-data)
                 :confidence (or (:confidence fact-data) 0.6)
                 :topic (keyword (or (:topic fact-data) "visual"))
                 :source image-path}))
              facts-data)))

      (catch Exception e
        (log/error "Failed to extract from image:" (.getMessage e))
        []))))

;; ============================================================
;; Example usage
;; ============================================================

(comment
  ;; Create a patch with facts
  (def patch
    (patch/make-patch
     {:source :manual
      :facts [(patch/make-fact {:text "Large models learn better"
                                :confidence 0.85
                                :topic :scaling})
              (patch/make-fact {:text "Scaling depends on parameters"
                                :confidence 0.9
                                :topic :scaling})
              (patch/make-fact {:text "Data quality matters"
                                :confidence 0.75
                                :topic :data})]}))

  ;; Add embeddings
  (def patch-with-emb (add-embeddings-to-patch patch))

  ;; Cluster facts
  (def clusters (cluster-by-similarity patch-with-emb 0.75))

  ;; Extract motives
  (def motives (extract-all-motives patch-with-emb))

  ;; Build motive graph
  (def motive-graph (build-motive-graph motives))

  ;; Enhanced AI features
  (def contradictions (detect-contradictions patch-with-emb))
  (def relationships (extract-relationships patch))
  (def duplicates (find-duplicates patch-with-emb))
  (def merged-patch (merge-duplicates patch duplicates))
  (def image-facts (extract-from-image "diagram.png")))
