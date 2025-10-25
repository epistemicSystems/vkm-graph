# VKM Graph: The Knowledge Graph Evolution System

A categorical framework for understanding how human knowledge evolves over time.

## Vision

This system treats knowledge patches as points in a moduli stack, with commits as morphisms that trace a trajectory through configuration space. Built on category theory, homotopy type theory, and Grothendieck's philosophy of mathematics.

**Core Insight:** Commit histories are proofs, patches are theorems, and understanding is the process of watching structure emerge.

## Architecture

The system consists of four main components:

### 1. Ingestion Pipeline (Go)
- Downloads videos from YouTube channels
- Transcribes audio using Whisper
- Outputs structured JSON with timestamps

### 2. Processing Core (Clojure)
- Extracts facts using Claude API
- Constructs immutable EDN patches
- Computes semantic embeddings
- Builds morphism structure
- Stores in Datomic with temporal queries

### 3. Semantic Analysis (Clojure + Python)
- Clusters facts by semantic similarity
- Extracts motives (essential concepts)
- Builds motive graph
- Infers topos structure

### 4. Visualization (React + use.GPU)
- Interactive moduli stack explorer
- Motive graph with force-directed layout
- Timeline scrubber for evolution playback
- Query interface for semantic search

## Mathematical Foundation

### The Moduli Stack of Knowledge

```
M = Stack of knowledge configurations
  Objects: Patches (EDN structures encoding facts + relationships)
  Morphisms: Transitions (git commits, representing understanding evolution)
  Equivalences: Patches with identical morphism neighborhoods
  Automorphisms: Non-trivial symmetries (reorganizations without knowledge gain)
```

### Yoneda & Univalence

- **Yoneda Embedding:** A patch is completely defined by its morphism neighborhood
- **Univalence:** Equivalence IS equality (up to homotopy)
- **Practical Result:** Two patches connected by transformations represent equivalent knowledge

### Motives as Universal Properties

A motive is the universal property of a semantic cluster:
- Intersection of semantic fields
- Tropical skeleton extraction
- Linguistic basis (concept words)

## Project Structure

```
vkm-graph/
├── cli/           # Go CLI for ingestion
├── core/          # Clojure processing pipeline
├── viz/           # React visualization
├── data/          # Storage (transcripts, patches, embeddings)
├── docs/          # Documentation
└── scripts/       # Utilities
```

## Quick Start

### Prerequisites

- Go 1.21+
- Clojure 1.11+
- Node.js 20+
- Datomic (or Datomic Cloud)
- OpenAI API key (for Claude and embeddings)

### Installation

```bash
# Clone the repository
git clone https://github.com/epistemicSystems/vkm-graph.git
cd vkm-graph

# Run setup script
./scripts/setup.sh

# Configure API keys
cp .env.example .env
# Edit .env with your API keys
```

### Usage

#### 1. Download and Transcribe Videos

```bash
cd cli
go run main.go download --channel UC_xyz --date-range 2020-2025
go run main.go transcribe --input data/videos/
```

#### 2. Build Patches

```bash
cd core
clj -M:run pipeline --source youtube --channel UC_xyz
```

#### 3. Launch Visualization

```bash
cd viz
npm install
npm run dev
```

## Configuration

Configuration is managed via EDN files in `core/resources/`:

- `config.edn`: Global settings
- `patch.edn`: Patch schema definition
- `datomic-schema.edn`: Database schema

Example `config.edn`:

```clojure
{:claude {:api-key "YOUR_API_KEY"
          :model "claude-sonnet-4.5"}
 :embeddings {:provider :openai
              :model "text-embedding-3-small"}
 :datomic {:uri "datomic:mem://knowledge-graph"}
 :visualization {:layout :force-directed
                 :dimensions 2
                 :show-motives true}}
```

## Philosophical Principles

### 1. Knowledge as Dialogue (Grothendieck)

Each patch represents a temporary settlement in the dialogue between the knower and the thing being studied.

### 2. Concepts Over Facts (Riehl & McLarty)

The system stores concepts (universal abstractions) rather than mere facts.

### 3. Structure Speaks for Itself (Yoneda)

Structure emerges naturally from morphism neighborhoods rather than being imposed externally.

## Implementation Phases

- **Phase 0 (Weeks 1-4):** Foundation - core infrastructure, data model, basic visualization
- **Phase 1 (Weeks 5-8):** Semantic Search & Clustering - equivalence checking, clustering, query interface
- **Phase 2 (Weeks 9-12):** Motives & Topos - motive extraction, topos inference, hierarchical visualization
- **Phase 3 (Weeks 13-16):** Temporal Dynamics - evolution timeline, animation system, historical analysis
- **Phase 4 (Weeks 17+):** Advanced Features - formal verification, symbolic regression, federation

## Data Model

### Patch Structure

```clojure
{:db/id "patch-uuid-v1"
 :patch/timestamp #inst "2025-10-24T00:00:00Z"
 :patch/source :youtube-channel
 :patch/facts
 [{:db/id "claim-uuid-1"
   :claim/text "Large models learn better"
   :claim/confidence 0.85
   :claim/topic :scaling}]
 :patch/edges
 [{:edge/from "claim-uuid-1"
   :edge/to "claim-uuid-2"
   :edge/relation :supports
   :edge/strength 0.9}]}
```

### Morphism Structure

```clojure
{:db/id "morphism-uuid-1"
 :morphism/from "patch-v1-uuid"
 :morphism/to "patch-v2-uuid"
 :morphism/type :transition
 :morphism/commit-hash "abc123def456"
 :morphism/information-gain 0.15}
```

## Key Algorithms

- **Observational Equivalence:** Tests patch equivalence via randomized queries
- **Tropical Skeleton Extraction:** Extracts piecewise-linear structure from smooth regions
- **Yoneda Morphism Neighborhood:** Computes all morphisms relevant to a patch
- **Information Gain Calculation:** Quantifies understanding advancement

## Contributing

This project is in active development. Contributions welcome!

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

MIT License - see [LICENSE](LICENSE) for details.

## References

- Grothendieck, A. (1957). "Éléments de géométrie algébrique"
- Riehl, E. (2022). "Elements of ∞-Category Theory"
- Lurie, J. (2009). "Higher Topos Theory"
- Maclagan, D. & Sturmfels, B. (2015). "Introduction to Tropical Geometry"

## Contact

For questions or collaboration:
- GitHub Issues: [vkm-graph/issues](https://github.com/epistemicSystems/vkm-graph/issues)
- Email: research@epistemicsystems.org

---

**Status:** Proof of Concept (Phase 0)
**Last Updated:** October 25, 2025
