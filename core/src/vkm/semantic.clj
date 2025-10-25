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

   Returns a vector of fact maps."
  [text & {:keys [api-key model]
           :or {api-key *claude-api-key*
                model "claude-sonnet-4.5"}}]
  (try
    (let [prompt (str "Extract structured facts from the following text. "
                     "For each fact, provide:\n"
                     "- text: the claim\n"
                     "- confidence: how certain (0-1)\n"
                     "- topic: a keyword categorization\n\n"
                     "Text:\n" text "\n\n"
                     "Return as JSON array: [{\"text\": \"...\", \"confidence\": 0.8, \"topic\": \"scaling\"}]")

          response (http/post "https://api.anthropic.com/v1/messages"
                             {:headers {"x-api-key" api-key
                                       "anthropic-version" "2023-06-01"
                                       "Content-Type" "application/json"}
                              :body (json/generate-string
                                    {:model model
                                     :max_tokens 4000
                                     :messages [{:role "user"
                                                :content prompt}]})
                              :as :json})

          content (get-in response [:body :content 0 :text])

          ;; Parse JSON from Claude's response
          facts-data (json/parse-string content true)]

      ;; Convert to fact maps
      (map (fn [fact-data]
            (patch/make-fact
             {:text (:text fact-data)
              :confidence (:confidence fact-data 0.5)
              :topic (keyword (:topic fact-data "general"))}))
          facts-data))

    (catch Exception e
      (log/error "Failed to extract facts from text:" (.getMessage e))
      [])))

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
  (def motive-graph (build-motive-graph motives)))
