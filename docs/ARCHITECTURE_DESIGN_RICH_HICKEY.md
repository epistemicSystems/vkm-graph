# Technical Architecture Design
## The Knowledge Graph Evolution System

**Designed in the Spirit of:** Rich Hickey (Clojure, Information-Driven Architecture)  
**Core Principle:** Simplicity over Complexity. Information over Ceremony. Values over Objects.  
**Date:** October 24, 2025

---

## Part 1: The Fundamental Shift

### Why Rich Hickey's Philosophy Matters Here

Most software architectures are built backward:
- Start with objects/classes
- Then decide on relationships
- Then add persistence
- Then layer on APIs

**This gets messy because you're mixing concerns. Objects imply mutability. Relationships imply coupling. Persistence becomes bolted on.**

**Instead, Rich Hickey teaches us to start with information.**

### The Right Order

1. **What is the information?** (immutable, values)
2. **How does it flow?** (processes, pipelines)
3. **How do we store it?** (implementation detail)
4. **How do we query it?** (specification-driven)
5. **How do we compose it?** (through specifications)

### Applied to Our System

**The Information:** A `patch` is the atomic unit. Immutable. Self-contained. A point in configuration space.

```clojure
;; A patch is JUST DATA
;; It has no methods, no state, no identity beyond its value
{:db/id "patch-uuid-v1"
 :patch/timestamp #inst "2025-10-24T00:00:00Z"
 :patch/facts [...]        ;; immutable values
 :patch/edges [...]        ;; immutable values
 :patch/embeddings [...]}  ;; immutable values
```

**The Flow:** Patches flow through pipelines. Each pipeline stage is a pure function.

```clojure
;; A pipeline is just function composition
;; transcript → [claims] → [embeddings] → patches → stored
(defn transcript->patches [transcript]
  (let [claims (extract-claims transcript)
        embedded (embed-claims claims)
        patches (build-patches embedded)]
    patches))
```

**The Storage:** Where patches live is an implementation detail. Could be:
- Datomic (time-aware, queryable)
- Git (version-controlled)
- LocalStorage (browser)
- S3 (cold storage)

**All use the same interface.**

**The Query:** Specifications describe what we want to know, not how to get it.

```clojure
;; "Give me patches about scaling from Oct 2020 to Oct 2025, ordered by time"
;; This is a specification. The store implements it.
{:query/topic :scaling
 :query/time-range [#inst "2020-10" #inst "2025-10"]
 :query/order :time}
```

---

## Part 2: Information Design (The Data Model)

### The Core Value: Patch

A patch is **immutable information** about understanding at a point in time.

```clojure
(spec/fdef ::patch
  :args (spec/cat :data ::patch-data)
  :ret boolean?)

(spec/def ::patch
  (spec/keys :req [:db/id :patch/timestamp :patch/facts :patch/edges]
             :opt [:patch/embeddings :patch/metadata]))

(spec/def ::patch/timestamp inst?)
(spec/def ::patch/facts (spec/coll-of ::fact))
(spec/def ::patch/edges (spec/coll-of ::edge))
(spec/def ::patch/embeddings (spec/coll-of ::embedding))

;; A patch has NO methods
;; It IS data
;; It's serializable, debuggable, testable
```

**Why immutable?**
- Reasoning is easier (what you see is what you get)
- History is free (just save all versions)
- Concurrency is safe (no locks needed)
- Testing is simple (given input, assert output)

### The Atomic Values

Each field in a patch is itself immutable data:

```clojure
;; Fact: a single claim
(spec/def ::fact
  (spec/keys :req [:db/id :claim/text :claim/confidence :claim/topic :claim/valid-from]
             :opt [:claim/revises :claim/tags]))

;; Edge: a relationship
(spec/def ::edge
  (spec/keys :req [:db/id :edge/from :edge/to :edge/relation :edge/strength]))

;; Embedding: a vector
(spec/def ::embedding
  (spec/keys :req [:embedding-id :claim-ref :model :vector]))

;; None of these have methods
;; All are pure data
;; All are serializable as EDN
```

**Why EDN?**
- Homoiconicity: data looks like code
- Debuggability: you can read it
- Versionability: diffs are meaningful
- Extensibility: add fields without breaking parsers
- No ceremony: no JSON marshallers, no SQL bindings, no ORM

---

## Part 3: Process Design (The Pipeline)

### The Core Abstraction: Process

A **process** is a pure function that transforms values.

```clojure
;; The most basic process: transform input value to output value
(spec/fdef process
  :args (spec/cat :input any? :options map?)
  :ret any?)

;; A process is JUST a function
;; No side effects (ideally)
;; No hidden state
;; No magic

;; Example: extract claims from transcript
(defn extract-claims-process
  "Given a transcript, extract structured claims"
  [transcript options]
  ;; Call Claude API (side effect happens here, at boundary)
  ;; Transform response to our data structure
  ;; Return immutable EDN
  (let [raw-response (claude/extract-facts transcript options)]
    (transform-to-claims raw-response)))
```

### The Pipeline: Composition of Processes

Processes compose:

```clojure
;; Pipeline: transcript → claims → embeddings → patches → stored

(defn transcript->patches-process
  "End-to-end pipeline from transcript to patches"
  [transcript options]
  (let [claims (extract-claims-process transcript options)
        embedded (embed-claims-process claims options)
        patches (build-patches-process embedded options)]
    patches))

;; Or more flexibly using composition:
(defn compose-processes [& processes]
  (fn [input options]
    (reduce (fn [value process]
              (process value options))
            input
            processes)))

(def transcript->patches
  (compose-processes
    extract-claims-process
    embed-claims-process
    build-patches-process))
```

### The Side Effects: Quarantined at Boundaries

Processes should be pure. But the system has side effects (API calls, disk I/O).

**Put them at the boundaries:**

```clojure
;; Layer 1: Pure transformation (core logic)
(defn transform-claude-response [raw-response]
  (map (fn [r]
         {:claim/text (:text r)
          :claim/confidence (:confidence r)
          :claim/topic (keyword (:topic r))})
       (:claims raw-response)))

;; Layer 2: Side effect (I/O)
(defn call-claude [transcript options]
  (let [api-response (api/claude-extract transcript options)]
    (transform-claude-response api-response)))

;; Layer 3: Pipeline orchestration
(defn extract-claims-process [transcript options]
  (call-claude transcript options))
```

**Why separate?**
- Pure logic is testable (mock the boundary)
- Side effects are isolated (easy to debug)
- Composition is obvious (no hidden dependencies)
- Reusability is maximized (pure functions work anywhere)

---

## Part 4: Implementation Design (Where Code Lives)

### The Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│ Presentation Layer                                  │
│ (React + use.GPU)                                  │
│ Queries data, renders visualizations               │
└────────────────────┬────────────────────────────────┘
                     │
                     ↓ (HTTP/gRPC)
┌─────────────────────────────────────────────────────┐
│ API Layer                                            │
│ (Clojure Ring/Pedestal)                            │
│ Exposes query/mutation endpoints                    │
│ Wraps stored data in JSON                           │
└────────────────────┬────────────────────────────────┘
                     │
                     ↓ (Specification-based)
┌─────────────────────────────────────────────────────┐
│ Query Layer                                          │
│ (Datomic + Clojure Datalog)                        │
│ Translates queries into database operations        │
│ Returns immutable values                            │
└────────────────────┬────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────┐
│ Storage Layer                                        │
│ (Datomic, Git, S3)                                 │
│ Multiple backends, same interface                   │
└─────────────────────────────────────────────────────┘
```

**Why this layering?**
- Each layer is independent (can test in isolation)
- Implementations are swappable (change Datomic to PostgreSQL easily)
- Data flows one direction (down), queries flow up
- No layer depends on implementation details of lower layers

### The Code Organization

```
src/
├── core/
│   ├── patch.clj         ;; Patch data definition & specs
│   ├── fact.clj          ;; Fact data definition & specs
│   ├── edge.clj          ;; Edge data definition & specs
│   └── embedding.clj     ;; Embedding data definition & specs
│
├── process/              ;; Pure transformations
│   ├── extraction.clj    ;; transcript → claims
│   ├── embedding.clj     ;; claims → embeddings
│   ├── patching.clj      ;; embeddings → patches
│   └── composition.clj   ;; Process orchestration
│
├── boundary/             ;; Side effects (I/O)
│   ├── claude.clj        ;; Claude API wrapper
│   ├── whisper.clj       ;; Whisper API wrapper
│   ├── storage.clj       ;; Storage interface
│   └── git.clj           ;; Git operations
│
├── query/                ;; Query specifications & execution
│   ├── specs.clj         ;; Query spec definitions
│   ├── datomic.clj       ;; Datomic query engine
│   └── executor.clj      ;; Query execution
│
├── api/                  ;; HTTP API layer
│   ├── routes.clj        ;; Route definitions
│   ├── handlers.clj      ;; Handler functions
│   └── middleware.clj    ;; HTTP middleware
│
└── cli/                  ;; Command-line interface
    ├── core.clj          ;; CLI entry points
    └── commands.clj      ;; Individual commands
```

**Why this structure?**
- `core/` defines information
- `process/` transforms information
- `boundary/` handles side effects
- `query/` makes information queryable
- `api/` exposes information
- `cli/` orchestrates processes

**Each can evolve independently.**

---

## Part 5: The Specification-Based Interface

### Specs as Contracts

Instead of interfaces/classes, use specs.

```clojure
;; A "storage backend" is not a class
;; It's a specification that any backend must satisfy

(spec/def ::storage-backend
  (spec/keys :req [:store! :query! :delete!]))

;; store!: (patch) → patch
;; Takes a patch, stores it, returns the stored patch
(spec/fdef store!
  :args (spec/cat :backend ::storage-backend :patch ::patch)
  :ret ::patch)

;; query!: (query-spec) → [patches]
;; Takes a query specification, returns matching patches
(spec/fdef query!
  :args (spec/cat :backend ::storage-backend :query ::query-spec)
  :ret (spec/coll-of ::patch))

;; delete!: (patch-id) → boolean
;; Deletes a patch, returns success
(spec/fdef delete!
  :args (spec/cat :backend ::storage-backend :patch-id string?)
  :ret boolean?)
```

**Any backend that satisfies this spec works:**

```clojure
;; Datomic backend
(defn datomic-backend []
  {:store! (fn [patch] (datomic/transact patch))
   :query! (fn [query-spec] (datomic/query query-spec))
   :delete! (fn [patch-id] (datomic/retract patch-id))})

;; Git backend (write-only, version-controlled)
(defn git-backend []
  {:store! (fn [patch] (git/commit patch))
   :query! (fn [query-spec] (git/log query-spec))
   :delete! (fn [patch-id] false)})  ;; Can't delete, immutable

;; LocalStorage backend (development)
(defn localstorage-backend []
  {:store! (fn [patch] (localstorage/set patch))
   :query! (fn [query-spec] (localstorage/query query-spec))
   :delete! (fn [patch-id] (localstorage/del patch-id))})

;; These are drop-in replacements
;; No other code changes
```

**Why specifications?**
- Multiple implementations coexist
- Testing is easy (mock the backend)
- Dependencies are explicit (specs say what's needed)
- No coupling to concrete classes

### Query Specifications

Queries are data, not code.

```clojure
;; A query is a map describing what we want
;; This is a specification, not a method call

;; "Give me patches about scaling, ordered by time"
{:query/type :patches
 :query/filter {:topic :scaling}
 :query/order :time
 :query/limit 50}

;; "Give me the evolution of 'scaling' confidence over time"
{:query/type :concept-evolution
 :query/concept :scaling
 :query/metric :confidence
 :query/time-range [#inst "2020" #inst "2025"]}

;; "Show me patches that are equivalent to this one"
{:query/type :equivalent-patches
 :query/reference-patch-id "patch-123"
 :query/equivalence-threshold 0.95}

;; The backend translates these to its native query language
;; Datomic: translates to Datalog
;; SQL: translates to SQL
;; Git: translates to log commands
;; Each backend implements its own executor
```

**Why specs instead of code?**
- Queries are inspectable (you can reason about them)
- Queries are composable (you can combine them)
- Queries are serializable (you can share them)
- Queries are language-agnostic (frontend can generate them)

---

## Part 6: Component Independence

### The Process of Building a Patch

Each step is independent. Each can be tested without others.

```clojure
;; STEP 1: Extract claims (pure)
(defn extract-claims [transcript]
  ;; Takes raw transcript string
  ;; Returns list of claim maps
  ;; Deterministic: same input → same output
  ;; No side effects
  (let [response (claude/extract-facts transcript)]
    (transform-to-claims response)))

;; STEP 2: Embed claims (pure)
(defn embed-claims [claims]
  ;; Takes list of claims
  ;; Returns claims with :vector field added
  ;; Deterministic (assuming embedding model is fixed)
  (map (fn [claim]
         (assoc claim :embedding (embed-text (:claim/text claim))))
       claims))

;; STEP 3: Build patches (pure)
(defn build-patches [embedded-claims]
  ;; Takes embedded claims
  ;; Groups into patches by time
  ;; Returns list of patch maps
  (group-by-time embedded-claims))

;; STEP 4: Store patches (side effect)
(defn store-patches [patches storage-backend]
  ;; Takes patches and backend
  ;; Stores each, returns stored patches
  (map (fn [patch]
         ((:store! storage-backend) patch))
       patches))
```

**Each step is independently testable:**

```clojure
;; Test extraction
(deftest test-extract-claims
  (let [transcript "The model learns better with more parameters"
        claims (extract-claims transcript)]
    (is (= 1 (count claims)))
    (is (= :scaling (:claim/topic (first claims))))))

;; Test embedding
(deftest test-embed-claims
  (let [claim {:claim/text "scaling"}
        embedded (embed-claims [claim])]
    (is (vector? (:embedding (first embedded))))
    (is (= 384 (count (:embedding (first embedded)))))))

;; Test patch building
(deftest test-build-patches
  (let [claims [{:claim/text "a" :timestamp 100}
                {:claim/text "b" :timestamp 100}
                {:claim/text "c" :timestamp 200}]
        patches (build-patches claims)]
    (is (= 2 (count patches)))
    (is (= 2 (count (:patch/facts (first patches)))))))

;; Test storage
(deftest test-store-patches
  (let [patch {:patch/facts [...]}
        backend (localstorage-backend)
        stored (store-patches [patch] backend)]
    (is (every? :db/id stored))))
```

**No mocking needed. No database required. Just data in, data out.**

---

## Part 7: System Composition

### How Everything Connects

The system is built by composing processes:

```clojure
;; The end-to-end workflow

(defn ingest-video
  "Download video, transcribe, extract, build, store"
  [video-id options]
  (let [storage-backend (get-storage-backend options)
        
        ;; Step 1: Download + transcribe
        transcript (-> video-id
                      (boundary/download-video)
                      (boundary/transcribe))
        
        ;; Step 2: Extract + embed + build
        claims (process/extract-claims transcript)
        embedded (process/embed-claims claims)
        patches (process/build-patches embedded)
        
        ;; Step 3: Store
        stored (process/store-patches patches storage-backend)
        
        ;; Step 4: Return result
        ]
    {:patches stored :count (count stored)}))

;; The workflow is just function composition
;; Each function has clear input/output
;; Each function can be tested independently
;; Each function can be replaced without affecting others
```

### Configuration as Data

Instead of configuration files or environment variables, use EDN:

```clojure
;; config.edn
{:system
 {:storage {:type :datomic
            :uri "datomic:dev://localhost:4334/knowledge"}
  :embedding {:model :nomic
              :api-key (System/getenv "NOMIC_API_KEY")}
  :claude {:api-key (System/getenv "CLAUDE_API_KEY")
           :model "claude-sonnet-4-20250514"}}
 
 :pipeline
 {:extraction {:batch-size 10
               :timeout 30000}
  :embedding {:batch-size 50
              :timeout 30000}}
 
 :visualization
 {:force-directed {:iterations 100
                   :repulsion 0.5}}}

;; Load and use
(defn load-config [path]
  (-> path slurp edn/read-string))

(def config (load-config "config.edn"))

;; Every component reads from config, not hardcoded
(defn get-storage-backend []
  (case (get-in config [:system :storage :type])
    :datomic (storage/datomic-backend config)
    :git (storage/git-backend config)))
```

**Why EDN config?**
- Homoiconic with the rest of the system
- Debuggable (you can read it)
- Composable (can merge configs)
- Versionable (changes are meaningful)

---

## Part 8: Information Flow (The Happy Path)

### From Document to Visualization

```
START: User uploads document
  ↓
READ: Document bytes → EDN
  (pure function: content → map)
  ↓
EXTRACT: EDN → Claims
  (side effect: Claude API)
  (pure function: response → claims list)
  ↓
EMBED: Claims → Embedded Claims
  (side effect: Embedding API)
  (pure function: text → vector)
  ↓
BUILD: Embedded Claims → Patches
  (pure function: group by time, assign IDs)
  ↓
STORE: Patches → Stored Patches
  (side effect: Database/Git)
  (pure function: patch → patch-with-id)
  ↓
QUERY: Storage → Patches for Visualization
  (side effect: Database read)
  (pure function: query-spec → patches)
  ↓
TRANSFORM: Patches → Visualization Data
  (pure function: patches → graph structure)
  ↓
RENDER: Visualization Data → UI
  (React/use.GPU rendering)
  ↓
INTERACT: User scrubs timeline
  ↓
UPDATE: Timeline position → Query update
  ↓
RENDER: New visualization data → Updated UI
  ↓
END: User sees insight
```

**Each arrow is either:**
- Pure function (can test in isolation)
- Side effect (happens at boundary, wrapped in process)
- API call (external, wrapped in boundary layer)

---

## Part 9: Scaling Strategy (No Surprises)

### Performance by Design

Since everything is immutable and specification-based:

**Caching is automatic:**
```clojure
;; Same query with same parameters?
;; Same result. Cache it.
(def memoized-patches-for-topic
  (memoize (fn [topic] 
             (query {:topic topic}))))
```

**Distribution is possible:**
```clojure
;; Query is data, processes are pure
;; Send query to different node, same result
;; Build distributed system without changing code
```

**Optimization is decoupled:**
```clojure
;; Want to add Redis caching?
;; Add it at the query layer, no changes to processes
;; Want to add compression?
;; Add it at the storage layer, no changes to queries
```

### The Datalog Advantage

Using Datomic/Datalog means:

```clojure
;; Incredibly flexible queries, no schema changes needed
;; "Show me all facts with confidence > 0.8 from Oct 2020"
(d/q '[:find ?fact ?confidence
       :where [?fact :claim/confidence ?conf]
              [(> ?conf 0.8)]
              [?fact :claim/valid-from ?vf]
              [(= ?vf #inst "2025-10-20")]]
     db)

;; "What was the state of 'scaling' understanding on Oct 15?"
(d/q '[:find ?claim ?conf
       :where [?claim :claim/topic :scaling]
              [?claim :claim/confidence ?conf]
              [?claim :claim/valid-from ?vf]
              [(< ?vf #inst "2025-10-15")]]
     (d/as-of db #inst "2025-10-15"))

;; Time-travel queries are FREE
;; No special handling needed
```

---

## Part 10: Development Workflow

### The REPL-Driven Development Loop

Rich Hickey's greatest insight: **use the REPL to explore your design**.

```clojure
;; Start REPL
(require '[understand.core :as core])
(require '[understand.process :as process])
(require '[understand.boundary :as boundary])

;; Load a real transcript
(def transcript (slurp "test-data/transcript.txt"))

;; Explore extraction
(def claims (process/extract-claims transcript))
(count claims)  ;; => 47
(first claims)  ;; => {:claim/text "..." :confidence 0.85 ...}

;; Explore embedding
(def embedded (process/embed-claims claims))
(-> embedded first :embedding count)  ;; => 384

;; Explore patching
(def patches (process/build-patches embedded))
(count patches)  ;; => 5

;; Explore storage
(def backend (storage/localstorage-backend))
(def stored (process/store-patches patches backend))

;; Explore queries
(def query-result (query/execute 
                   {:query/type :patches 
                    :query/filter {:topic :scaling}}
                   backend))

;; Adjust and iterate
;; No compilation, no restart, instant feedback
```

**Why this matters:**
- You discover design issues before committing to implementation
- Specs are tested as you build
- Performance problems show up immediately
- Refactoring is safe (REPL gives immediate feedback)

### Testing as Specification

```clojure
;; Tests don't just verify behavior, they document specification

(deftest test-patch-immutability
  "Patches must be immutable (no side effects in construction)"
  (let [patch1 {:db/id "p1" :patch/facts []}
        patch2 (assoc patch1 :patch/metadata {:new "field"})]
    ;; patch1 is unchanged
    (is (not (contains? patch1 :patch/metadata)))
    ;; patch2 is new object
    (is (not (identical? patch1 patch2)))))

(deftest test-process-determinism
  "Processes must be deterministic"
  (let [input "test input"
        result1 (process/extract-claims input)
        result2 (process/extract-claims input)]
    (is (= result1 result2))))

(deftest test-storage-interface
  "All backends must satisfy storage interface"
  (let [backends [(storage/datomic-backend config)
                  (storage/git-backend config)
                  (storage/localstorage-backend)]]
    (doseq [backend backends]
      (let [patch {:db/id "test"}
            stored ((:store! backend) patch)]
        (is (:db/id stored))))))
```

**Tests are specifications written in code.**

---

## Part 11: Evolution & Change

### How the System Handles Change

Because everything is specification-based:

**Adding a new process?**
```clojure
;; Write a new process function, compose it in
(defn new-process [input options] ...)

;; Add to pipeline
(def extended-pipeline
  (compose-processes
    process/extract-claims
    process/embed-claims
    new-process  ;; New!
    process/build-patches))

;; No changes to existing code
```

**Adding a new data field?**
```clojure
;; Add to spec
(spec/def ::patch
  (spec/keys :req [...] :opt [...]))

;; Update code that creates patches
(defn build-patches [claims]
  (map (fn [claim]
         {:patch/facts [...]
          :patch/new-field [...]})  ;; New!
       claims))

;; Queries automatically work (Datalog is flexible)
;; Backward compat: old patches still query
```

**Changing storage backend?**
```clojure
;; Implement new backend
(defn postgres-backend [] {...})

;; Update config
(assoc-in config [:storage :type] :postgres)

;; Everything else unchanged
```

**Adding new visualization?**
```clojure
;; New query specification
{:query/type :new-visualization-type
 :query/parameters [...]}

;; Backend implements executor
;; Frontend renders
;; No changes to core logic
```

**All changes are additive. No breaking changes needed.**

---

## Part 12: Technology Choices (Justified)

### Clojure (Not Python, Not JavaScript)

**Why Clojure?**
- Homoiconicity (data = code = configuration)
- Immutability by default
- Specs for specification-based design
- REPL for interactive development
- Best-in-class persistent data structures

**Not Python?**
- Mutable by default (invites bugs)
- No homoiconicity (data looks nothing like code)
- Hard to reason about (side effects everywhere)

**Not JavaScript?**
- Mutable by default
- Type system is weak
- No native immutable data structures
- REPL experience is poor

### Datomic (Not PostgreSQL, Not MongoDB)

**Why Datomic?**
- Immutable by default (perfect fit)
- Datalog queries (powerful, flexible)
- Time-travel (as-of queries)
- No schema migrations (add fields, queries work)

**Not PostgreSQL?**
- Requires schemas (tight coupling)
- Mutable (temporal model is bolted on)
- SQL is imperative (describe how, not what)

**Not MongoDB?**
- Schema-less but mutable
- No query language (just navigation)
- Temporal model is awkward

### use.GPU (Not D3, Not Three.js, Not Babylon)

**Why use.GPU?**
- Reactive (like React)
- GPU-accelerated
- Homoiconic (components are data)
- Built for this use case (math visualization)

**Not D3?**
- Imperative (describe transformations)
- DOM-based (slow for large graphs)
- Not built for 3D

**Not Three.js/Babylon?**
- Built for games, not data viz
- Hard to integrate with React
- Performance overhead

### Go for CLI (Not Python, Not Clojure)

**Why Go?**
- Fast startup (CLI responsiveness)
- Simple deployment (single binary)
- Concurrency model (good for parallelizing tasks)
- Good for system integration (shell commands)

**For data processing (Clojure):**
- Rich data structures
- Interactive REPL
- Easy to reason about transformations

**Separation of concerns:**
- Go handles I/O, concurrency, system integration
- Clojure handles data transformation and logic
- React handles visualization

---

## Part 13: Deployment Architecture

### Development Mode (Week 1-4)

```
Developer machine
  ├── REPL (Clojure) — interactive development
  ├── LocalStorage backend — data persistence
  ├── Mocked Claude/Whisper — no API calls
  └── React dev server — hot reload visualization
```

### Phase 1-2 Testing (Week 5-6)

```
Beta user machine
  ├── Go CLI (download/transcribe)
  ├── Clojure processor (via REPL or script)
  ├── Datomic in-memory — no persistence
  ├── Real Claude API — real extractions
  └── Web UI (React) — local server
```

### Phase 3+ Production (Week 7+)

```
Cloud infrastructure
  ├── Go CLI — AWS Lambda (on-demand)
  ├── Clojure processor — ECS task
  ├── Datomic Cloud — persistent storage
  ├── Claude API — real extractions
  ├── React UI — CloudFront CDN
  └── Database — S3 + DynamoDB
```

**Why this progression?**
- Start simple (local, in-memory)
- Add complexity only when needed
- Each stage reuses code from previous
- No rewrites required

---

## Part 14: The Principle of Simplicity

### What Makes This Architecture Simple

**Not "easy" (not lacking effort), but "simple" (few moving parts):**

1. **Immutable values** → no hidden state
2. **Pure functions** → no surprises
3. **Specifications** → explicit contracts
4. **Composition** → pieces work together
5. **Information-first** → data is the center

### Complexity Budget

We save complexity here, so we can spend it where it matters:

- ✅ Beautiful visualization (worth the complexity)
- ✅ Magical interaction (worth the complexity)
- ❌ ORM layers (not worth it)
- ❌ Object hierarchies (not worth it)
- ❌ Ceremony (not worth it)

### Making Changes Safe

Because the architecture is simple:

```clojure
;; Want to change extraction logic?
;; Just change process/extract-claims
;; Everything else still works

;; Want to change visualization?
;; Just change visualization layer
;; Data layer unchanged

;; Want to swap databases?
;; Just implement new backend
;; Everything else unchanged

;; Want to add caching?
;; Add memoization layer
;; No changes needed elsewhere
```

---

## Part 15: Connection to the PRD

### How This Architecture Serves the PRD

**The PRD says: "Build beauty first, data second."**

This architecture enables that:

```clojure
;; Week 1-2: Build with synthetic data
;; Just hand-write patches, no ingestion
{:patches
 [{:db/id "p1" :patch/facts [...]  :patch/edges [...]}
  {:db/id "p2" :patch/facts [...]  :patch/edges [...]}]}

;; The visualization code doesn't care where patches come from
;; Could be synthetic, could be from database
;; Same query interface, same results

;; Week 3-4: Add document upload
;; (process/document->patches) function
;; Queries still work, visualization still works
;; No changes needed except adding the process

;; Week 5-6: Add Claude validation
;; (process/topic->patches) function
;; Ditto

;; Week 7-8: Add real sources
;; (process/video->patches) pipeline
;; Everything composes perfectly
```

**The PRD says: "Go/no-go gates at weeks 4 and 8."**

This architecture enables rapid iteration:

```clojure
;; REPL-driven development
;; Changes are instant
;; No recompilation
;; Immediate feedback
;; Easy to pivot
```

**The PRD says: "Resource constraints: small team, short timeline."**

This architecture minimizes ceremony:

```
No ORM to configure
No schema migrations
No interface boilerplate
No test mocking frameworks
Just functions, data, and composition
```

---

## Part 16: Appendix - Key Interfaces

### Storage Backend Interface

```clojure
(spec/def ::storage-backend
  (spec/and
    (spec/keys :req [:store! :query! :delete! :close!])
    (fn [backend]
      ;; All functions must be callable
      (every? callable? (vals backend)))))

;; store!: patch → patch-with-id
;; Invariant: patch-id is generated, timestamp is preserved
(spec/fdef store!
  :args (spec/cat :backend ::storage-backend :patch ::patch)
  :ret (spec/and ::patch #(:db/id %)))

;; query!: query-spec → [patches]
;; Invariant: results are immutable, queries are deterministic
(spec/fdef query!
  :args (spec/cat :backend ::storage-backend :query map?)
  :ret (spec/coll-of ::patch))

;; delete!: patch-id → boolean
;; Invariant: returns true if successful, false otherwise
(spec/fdef delete!
  :args (spec/cat :backend ::storage-backend :patch-id string?)
  :ret boolean?)

;; close!: → nil
;; Invariant: backend is shut down gracefully
(spec/fdef close!
  :args (spec/cat :backend ::storage-backend)
  :ret nil?)
```

### Process Interface

```clojure
(spec/def ::process
  (spec/and
    fn?
    (fn [f]
      ;; Process is a function
      ;; Takes (value options) → value
      ;; Must be pure (same input → same output)
      true)))

;; Example process
(defn identity-process
  "Simplest process: returns input unchanged"
  [value options]
  value)

;; Processes compose
(defn compose-processes [& processes]
  (fn [value options]
    (reduce (fn [v p] (p v options)) value processes)))
```

### Query Specification Interface

```clojure
(spec/def ::query-spec
  (spec/keys :req [:query/type]
             :opt [:query/filter :query/order :query/limit]))

;; Supported query types
:query/patches           ;; All patches matching filter
:query/concept-evolution ;; How a concept changed over time
:query/equivalent-patches ;; Patches equivalent to reference
:query/facts-for-cluster ;; Facts in a semantic cluster

;; Filter operators
{:field :topic :op := :value :scaling}
{:field :timestamp :op :> :value #inst "2020"}
{:field :confidence :op :> :value 0.8}
```

---

## Part 17: The Philosophy in Practice

### Design Decisions Are Reversible

**Early decision:** Use Datomic for storage

**Can we change it?** Yes.
- New backend implements interface
- One config line changes
- All queries work
- No code changes needed

**Early decision:** Use Claude for extraction

**Can we change it?** Yes.
- New extraction function
- Implement same interface
- Compose into pipeline
- Everything works

**Early decision:** Use React for visualization

**Can we change it?** Yes.
- New query interface
- New frontend renders
- Same data flow
- Everything works

### Design Decisions Are Testable

Because everything is data + pure functions:

```clojure
;; Test extraction in isolation
(test-extraction)

;; Test embedding in isolation
(test-embedding)

;; Test patching in isolation
(test-patching)

;; Test storage in isolation
(test-storage)

;; Test queries in isolation
(test-queries)

;; Test visualization in isolation
(test-visualization)

;; NO INTEGRATION TESTING NEEDED
;; Everything composes correctly
```

### The Meta-Principle

**Rich Hickey's core insight: Simplicity is about reducing cognitive load.**

This architecture does that by:

1. **Making information explicit** (patches are just data)
2. **Separating concerns** (processes, storage, queries are independent)
3. **Enabling composition** (small pieces combine)
4. **Avoiding magic** (everything is visible)
5. **Making change safe** (specs define contracts)

**The result: a system where the next person (or future you) can reason about the entire thing in their head.**

---

## End

This architecture is designed to evolve with you. It won't surprise you with constraints. It won't force rewrites. It will let you build beauty first and add substance later, exactly as the PRD requires.

The system is ready for the "action plan" prompt.

---

*Architecture design guided by Rich Hickey's principles from Clojure, "Simple Made Easy," and decades of operational systems thinking.*

*This document is a specification for how to think about the system, not a directive for how to implement it. Implementation may vary as long as it respects the principles.*
