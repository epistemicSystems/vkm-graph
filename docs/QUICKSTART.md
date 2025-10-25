# Quick Start Guide

This guide will help you get started with the VKM Graph Knowledge Evolution System.

## Overview

The VKM Graph system visualizes how knowledge evolves over time by:

1. **Ingesting** sources (YouTube videos, documents)
2. **Extracting** facts and claims
3. **Building** immutable knowledge patches
4. **Computing** morphisms (transitions between patches)
5. **Discovering** motives (essential concepts)
6. **Visualizing** the evolution

## Prerequisites

### Required

- **Clojure** 1.11+ ([Install guide](https://clojure.org/guides/getting_started))
- **Go** 1.21+ ([Install guide](https://go.dev/doc/install))
- **Node.js** 20+ ([Install guide](https://nodejs.org/))
- **Datomic** ([Free version](https://www.datomic.com/get-datomic.html))

### API Keys

- **OpenAI API Key** (for embeddings)
- **Claude API Key** (for fact extraction)

### Optional

- **Whisper** (for transcription): `pip install openai-whisper`
- **ffmpeg** (for audio processing): `brew install ffmpeg` or `apt install ffmpeg`

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/epistemicSystems/vkm-graph.git
cd vkm-graph
```

### 2. Set Up Environment Variables

Create a `.env` file in the root directory:

```bash
# API Keys
export OPENAI_API_KEY="your-openai-api-key"
export CLAUDE_API_KEY="your-claude-api-key"

# Datomic (optional, uses in-memory by default)
export DATOMIC_URI="datomic:mem://knowledge-graph"
```

Load the environment:

```bash
source .env
```

### 3. Install Dependencies

#### Clojure

```bash
cd core
clj -P  # Download dependencies
```

#### Go CLI

```bash
cd cli
go mod download
go build -o vkm
```

#### Visualization (optional)

```bash
cd viz
npm install
```

## Basic Usage

### Example 1: Process a YouTube Video

#### Step 1: Download and Transcribe

```bash
cd cli

# Download a single video (requires video ID)
./vkm download YourVideoID

# Or download from channel (requires YouTube Data API)
# ./vkm download --channel UCxxx --max-videos 10

# Transcribe the audio
./vkm transcribe --input ../data/videos --output ../data/transcripts
```

#### Step 2: Initialize Database

```bash
cd ../core

# Initialize Datomic and schema
clj -M:run init
```

#### Step 3: Process Transcripts

```bash
# Process transcripts into patches
clj -M:run process --source test-channel --transcripts ../data/transcripts
```

This will:
- Extract facts using Claude
- Build knowledge patches
- Compute embeddings
- Store in Datomic
- Extract motives

#### Step 4: Export for Visualization

```bash
# Export data for visualization
clj -M:run export --output ../data/viz-data.edn
```

#### Step 5: View Statistics

```bash
# View database statistics
clj -M:run stats
```

### Example 2: Working with the REPL

For interactive development:

```bash
cd core

# Start REPL
clj -M:repl

# In the REPL:
```

```clojure
(require '[vkm.pipeline :as pipeline])
(require '[vkm.db :as db])
(require '[vkm.patch :as patch])

;; Initialize
(pipeline/init-config!)
(db/init-db! "datomic:mem://knowledge-graph")

;; Create a simple patch manually
(def fact1 (patch/make-fact {:text "Large models learn better"
                              :confidence 0.85
                              :topic :scaling}))

(def fact2 (patch/make-fact {:text "Scaling depends on data and parameters"
                              :confidence 0.9
                              :topic :scaling}))

(def my-patch (patch/make-patch {:source :manual
                                  :facts [fact1 fact2]
                                  :edges []}))

;; Store it
(db/store-patch! my-patch)

;; Query
(db/db-stats)
(db/query-facts-by-topic :scaling)
```

### Example 3: Understanding the Data Model

#### Patches

A **patch** is an immutable snapshot of knowledge:

```clojure
{:db/id "patch-uuid-123"
 :patch/timestamp #inst "2025-10-25"
 :patch/source :youtube-channel
 :patch/facts [...]  ;; Vector of facts
 :patch/edges [...]  ;; Relationships between facts
 :patch/embeddings [...]}  ;; Semantic embeddings
```

#### Facts

A **fact** is a single claim:

```clojure
{:db/id "claim-uuid-456"
 :claim/text "Large models learn better"
 :claim/confidence 0.85  ;; How certain we are (0-1)
 :claim/topic :scaling
 :claim/valid-from #inst "2025-10-20"}
```

#### Morphisms

A **morphism** is a transition between patches:

```clojure
{:db/id "morphism-uuid-789"
 :morphism/from "patch-v1"
 :morphism/to "patch-v2"
 :morphism/type :additive  ;; or :refinement, :reorganization, :refutation
 :morphism/information-gain 0.15  ;; How much understanding advanced
 :morphism/operations [...]}  ;; What changed
```

#### Motives

A **motive** is an essential concept extracted from clusters:

```clojure
{:id "motive-uuid-abc"
 :concept-words ["scale" "parameter" "capacity"]
 :centroid [0.1 0.2 ...]  ;; Semantic centroid
 :confidence 0.8
 :cluster-size 5}  ;; Number of facts in cluster
```

## Directory Structure

```
vkm-graph/
├── cli/              # Go CLI for ingestion
│   ├── main.go
│   └── cmd/
│       ├── download.go
│       ├── transcribe.go
│       └── process.go
├── core/             # Clojure processing pipeline
│   ├── deps.edn
│   ├── src/vkm/
│   │   ├── patch.clj       # Patch operations
│   │   ├── morphism.clj    # Morphism computation
│   │   ├── semantic.clj    # Embeddings & clustering
│   │   ├── db.clj          # Datomic interface
│   │   └── pipeline.clj    # Orchestration
│   └── resources/
│       ├── config.edn
│       └── schema/
├── viz/              # React visualization
├── data/             # Storage
│   ├── videos/
│   ├── transcripts/
│   └── patches/
└── docs/             # Documentation
```

## Configuration

Edit `core/resources/config.edn`:

```clojure
{:claude {:api-key #env CLAUDE_API_KEY
          :model "claude-sonnet-4.5"}

 :embeddings {:provider :openai
              :model "text-embedding-3-small"}

 :datomic {:uri "datomic:mem://knowledge-graph"}

 :semantic {:clustering {:similarity-threshold 0.75}}}
```

## Common Workflows

### Workflow 1: Process a YouTube Channel

```bash
# 1. Download videos
cd cli
./vkm download --channel UCxxx --max-videos 20

# 2. Transcribe
./vkm transcribe --input ../data/videos --output ../data/transcripts

# 3. Process
cd ../core
clj -M:run init
clj -M:run process --source my-channel --transcripts ../data/transcripts

# 4. View results
clj -M:run stats
clj -M:run export --output ../data/viz-data.edn
```

### Workflow 2: Analyze Knowledge Evolution

```bash
# In Clojure REPL:
```

```clojure
(require '[vkm.db :as db])
(require '[vkm.morphism :as morphism])

;; Get all morphisms
(def morphisms (db/find-all-morphisms))

;; Find highest information gain
(->> morphisms
     (sort-by :morphism/information-gain)
     (reverse)
     (take 5))

;; Query knowledge at a specific time
(db/query-facts-at-time #inst "2025-10-01")
```

### Workflow 3: Extract Motives

```bash
# In Clojure REPL:
```

```clojure
(require '[vkm.semantic :as semantic])
(require '[vkm.db :as db])

;; Get all patches
(def patch-ids (db/find-patches-by-source "my-channel"))
(def patch (db/pull-patch (first patch-ids)))

;; Extract motives
(def motives (semantic/extract-all-motives patch))

;; View concept words
(map :concept-words motives)
```

## Troubleshooting

### "Database not initialized"

Run `clj -M:run init` to initialize the database.

### "API key not found"

Make sure environment variables are set:

```bash
export OPENAI_API_KEY="your-key"
export CLAUDE_API_KEY="your-key"
source .env  # if using .env file
```

### "Whisper not found"

Install Whisper:

```bash
pip install openai-whisper
```

### "Out of memory during embedding computation"

Reduce batch size in `config.edn`:

```clojure
:embeddings {:batch-size 50}  ;; Reduce from 100
```

## Next Steps

1. Read the [Architecture Guide](ARCHITECTURE.md)
2. Explore the [API Documentation](API.md)
3. Learn about the [Mathematical Foundation](PHILOSOPHY.md)
4. Try the [Visualization](../viz/README.md)

## Getting Help

- **Issues**: [GitHub Issues](https://github.com/epistemicSystems/vkm-graph/issues)
- **Discussions**: [GitHub Discussions](https://github.com/epistemicSystems/vkm-graph/discussions)
- **Email**: research@epistemicsystems.org
