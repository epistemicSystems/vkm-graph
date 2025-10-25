#!/bin/bash

# Example workflow for the VKM Graph Knowledge Evolution System
# This script demonstrates a complete end-to-end pipeline

set -e  # Exit on error

echo "=================================================="
echo "VKM Graph - Knowledge Evolution System"
echo "Example Workflow"
echo "=================================================="
echo ""

# Configuration
SOURCE_ID="example-channel"
VIDEO_DIR="data/videos"
TRANSCRIPT_DIR="data/transcripts/${SOURCE_ID}"
OUTPUT_DATA="data/viz-data.edn"

# Step 0: Check prerequisites
echo "Step 0: Checking prerequisites..."
echo "------------------------------------------------"

# Check for required tools
command -v clj >/dev/null 2>&1 || { echo "Error: Clojure not found. Please install Clojure." >&2; exit 1; }
command -v go >/dev/null 2>&1 || { echo "Error: Go not found. Please install Go." >&2; exit 1; }

# Check environment variables
if [ -z "$OPENAI_API_KEY" ]; then
    echo "Warning: OPENAI_API_KEY not set. Embeddings will fail."
fi

if [ -z "$CLAUDE_API_KEY" ]; then
    echo "Warning: CLAUDE_API_KEY not set. Fact extraction will fail."
fi

echo "✓ Prerequisites checked"
echo ""

# Step 1: Build CLI
echo "Step 1: Building CLI..."
echo "------------------------------------------------"
cd cli
go build -o vkm main.go
echo "✓ CLI built successfully"
echo ""

# Step 2: Create example transcript (synthetic data)
echo "Step 2: Creating example transcript..."
echo "------------------------------------------------"
mkdir -p "../${TRANSCRIPT_DIR}"

cat > "../${TRANSCRIPT_DIR}/example-video-1.json" <<'EOF'
{
  "video_id": "example-video-1",
  "title": "Introduction to AI Scaling",
  "published_at": "2023-01-15",
  "transcript": [
    {
      "timestamp": 0.0,
      "text": "In this video, we explore how AI models scale with parameters.",
      "duration": 5.0
    },
    {
      "timestamp": 5.0,
      "text": "Large language models with billions of parameters learn better than smaller ones.",
      "duration": 5.0
    },
    {
      "timestamp": 10.0,
      "text": "The relationship between model size and performance follows predictable scaling laws.",
      "duration": 5.0
    },
    {
      "timestamp": 15.0,
      "text": "However, data quality and compute efficiency also matter significantly.",
      "duration": 5.0
    }
  ]
}
EOF

cat > "../${TRANSCRIPT_DIR}/example-video-2.json" <<'EOF'
{
  "video_id": "example-video-2",
  "title": "Scaling Laws Deep Dive",
  "published_at": "2023-06-20",
  "transcript": [
    {
      "timestamp": 0.0,
      "text": "Our understanding of scaling has evolved significantly.",
      "duration": 5.0
    },
    {
      "timestamp": 5.0,
      "text": "We now know that scaling depends on three factors: parameters, data, and optimization.",
      "duration": 5.0
    },
    {
      "timestamp": 10.0,
      "text": "These factors may be partially equivalent under certain training regimes.",
      "duration": 5.0
    },
    {
      "timestamp": 15.0,
      "text": "This represents a fundamental shift in how we think about model capacity.",
      "duration": 5.0
    }
  ]
}
EOF

echo "✓ Example transcripts created"
echo ""

# Step 3: Initialize database
echo "Step 3: Initializing database..."
echo "------------------------------------------------"
cd ../core

# Create a minimal example without API calls
cat > src/vkm/example.clj <<'EOF'
(ns vkm.example
  "Example workflow without requiring API keys"
  (:require [vkm.patch :as patch]
            [vkm.morphism :as morphism]
            [vkm.db :as db]
            [clojure.tools.logging :as log]))

(defn run-example []
  (println "Initializing database...")
  (db/init-db! "datomic:mem://knowledge-graph"
               :schema-path "resources/schema/datomic-schema.edn")

  (println "\nCreating patches...")

  ;; Patch 1: Early understanding (Jan 2023)
  (def fact1-1 (patch/make-fact
                {:text "Large models learn better"
                 :confidence 0.75
                 :topic :scaling}))

  (def fact1-2 (patch/make-fact
                {:text "Parameters matter for capacity"
                 :confidence 0.7
                 :topic :scaling}))

  (def patch1 (patch/make-patch
               {:source :manual
                :source-id "example-timeline"
                :facts [fact1-1 fact1-2]
                :edges []}))

  (def patch1-id (db/store-patch! patch1))
  (println "✓ Patch 1 stored (ID:" patch1-id ")")

  ;; Patch 2: Evolved understanding (Jun 2023)
  (def fact2-1 (patch/make-fact
                {:text "Large models learn better"
                 :confidence 0.85  ;; Increased confidence
                 :topic :scaling}))

  (def fact2-2 (patch/make-fact
                {:text "Parameters matter for capacity"
                 :confidence 0.8  ;; Increased confidence
                 :topic :scaling}))

  (def fact2-3 (patch/make-fact
                {:text "Scaling depends on parameters, data, and optimization"
                 :confidence 0.9  ;; New insight
                 :topic :scaling}))

  (def edge2-1 (patch/make-edge
                {:from-id (:db/id fact2-1)
                 :to-id (:db/id fact2-3)
                 :relation :supports
                 :strength 0.8}))

  (def patch2 (patch/make-patch
               {:source :manual
                :source-id "example-timeline"
                :facts [fact2-1 fact2-2 fact2-3]
                :edges [edge2-1]}))

  (def patch2-id (db/store-patch! patch2))
  (println "✓ Patch 2 stored (ID:" patch2-id ")")

  ;; Compute morphism
  (println "\nComputing morphism...")
  (def morph (morphism/compute-morphism
              patch1
              patch2
              :reason "Evolved understanding of scaling"))

  (def morph-with-gain (morphism/add-information-gain morph patch1 patch2))
  (db/store-morphism! morph-with-gain)

  (println "✓ Morphism computed")
  (println "  Type:" (:morphism/type morph-with-gain))
  (println "  Information gain:" (:morphism/information-gain morph-with-gain))

  ;; Show results
  (println "\n=== Database Statistics ===")
  (let [stats (db/db-stats)]
    (doseq [[k v] stats]
      (println "  " (name k) ":" v)))

  (println "\n=== Facts by Topic: :scaling ===")
  (doseq [fact (db/query-facts-by-topic :scaling)]
    (println "  •" (:claim/text fact)
             "(confidence:" (:claim/confidence fact) ")"))

  (println "\n✓ Example complete!"))
EOF

clj -e "(require 'vkm.example) (vkm.example/run-example)"

echo ""
echo "✓ Example workflow completed successfully!"
echo ""

# Step 4: Show what's next
echo "=================================================="
echo "Next Steps:"
echo "=================================================="
echo ""
echo "1. View the data:"
echo "   cd core"
echo "   clj -M:repl"
echo "   (require '[vkm.db :as db])"
echo "   (db/db-stats)"
echo ""
echo "2. To process real transcripts (requires API keys):"
echo "   clj -M:run process --source ${SOURCE_ID} --transcripts ../${TRANSCRIPT_DIR}"
echo ""
echo "3. Export for visualization:"
echo "   clj -M:run export --output ../${OUTPUT_DATA}"
echo ""
echo "4. Read the documentation:"
echo "   docs/QUICKSTART.md"
echo ""
echo "=================================================="
