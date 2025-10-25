# VKM Graph - End-to-End MVP Guide

**Complete workflow: Document Upload → Fact Extraction → Knowledge Graph Visualization**

---

## Quick Start (3 Steps)

### 1. Start the Backend (Terminal 1)

```bash
cd core
clj -M:server
```

You should see:
```
INFO  vkm.api.server - Starting server on port 3000
INFO  vkm.api.server - Server started on http://localhost:3000
```

### 2. Start the Frontend (Terminal 2)

```bash
cd viz
npm install  # First time only
npm run dev
```

You should see:
```
  VITE ready in 300 ms

  ➜  Local:   http://localhost:5173/
```

### 3. Open Your Browser

Navigate to **http://localhost:5173**

You should see:
- ✅ Green "Backend online" indicator
- 📤 Upload zone (drag & drop area)
- 📊 Synthetic data visualization (default)

---

## Testing the End-to-End Flow

### Upload a Document

1. **Create a test document** (`test-ai.txt`):
   ```
   Large language models demonstrate emergent capabilities at scale.
   Training with more data improves model performance.
   Transformer architectures enable parallel processing.
   ```

2. **Upload it** (choose one method):
   - **Drag & drop** the file onto the upload zone
   - **Click** the upload zone and select the file

3. **Watch the magic happen**:
   - ⏳ "Processing document..." appears
   - ✅ Success message shows: `Document processed successfully (N facts extracted)`
   - 🔄 Visualization automatically switches to "Backend Data"
   - 📈 Your extracted facts appear in the graph!

### What Just Happened?

1. **Frontend** → Sent document text to backend
2. **Backend** (`vkm.api.server/upload-document-handler`)
   - Called `vkm.semantic/extract-facts-from-text`
   - Created a patch with `vkm.patch/make-patch`
   - Stored in Datomic with `vkm.db/store-patch!`
3. **Frontend** → Fetched patches from backend
4. **Visualization** → Rendered knowledge graph with facts

---

## Architecture Overview

### Backend Stack

**Clojure HTTP API Server** (`core/src/vkm/api/server.clj`)

Endpoints:
- `GET /health` - Health check
- `GET /api/stats` - Database statistics
- `GET /api/patches` - List all patches
- `GET /api/patches/:id` - Get specific patch
- `POST /api/upload` - Upload document for processing
- `POST /api/process` - Process transcripts directory
- `POST /api/patches/query` - Query patches with filters

Key dependencies:
- Ring/Jetty - HTTP server
- Compojure - Routing
- Datomic - Temporal database
- ring-cors - CORS middleware

### Frontend Stack

**React + TypeScript + Vite** (`viz/`)

Components:
- `UploadZone.tsx` - Drag & drop file upload
- `PatchGraph.tsx` - D3 force-directed graph
- `Timeline.tsx` - Interactive timeline scrubber
- `App.tsx` - Main application with state management

API Client (`viz/src/api/client.ts`):
- Typed TypeScript methods
- Automatic error handling
- Maps EDN responses to TypeScript types

### Data Flow

```
┌─────────────┐
│   Browser   │
│ (localhost: │
│    5173)    │
└──────┬──────┘
       │
       │ HTTP (fetch)
       ▼
┌─────────────┐
│  API Client │
│  (client.ts)│
└──────┬──────┘
       │
       │ POST /api/upload
       │ { content, filename }
       ▼
┌─────────────────────────────────┐
│   Clojure HTTP Server           │
│   (localhost:3000)              │
│                                 │
│   upload-document-handler       │
│   ├─ extract-facts-from-text    │
│   ├─ make-patch                 │
│   └─ store-patch!               │
└──────┬──────────────────────────┘
       │
       │ Datalog transactions
       ▼
┌─────────────┐
│   Datomic   │
│  (in-memory)│
└─────────────┘
```

---

## Advanced Usage

### API Client Methods

All available in `viz/src/api/client.ts`:

```typescript
import { api } from '@/api/client';

// Health check
const health = await api.checkHealth();

// Get statistics
const stats = await api.getStats();
// { patches: 5, facts: 42, edges: 18, ... }

// Upload document
const result = await api.uploadDocument(
  "Large models learn better.",
  "test.txt"
);
// { patch-id: "...", facts-count: 1, message: "..." }

// Get all patches
const patches = await api.getPatches();
// { patches: [...], count: 5 }

// Get specific patch
const patch = await api.getPatch("patch-id");

// Query patches
const results = await api.queryPatches({
  topic: "scaling",
  "min-confidence": 0.8
});
```

### Backend REPL

```clojure
cd core
clj

;; Start server programmatically
(require '[vkm.api.server :as server])
(server/start-server!)

;; Check server status
@server/server
;; => #<Server@...>

;; Stop server
(server/stop-server!)

;; Restart server
(server/restart-server!)

;; Test database operations
(require '[vkm.db :as db])
(db/db-stats)
;; => {:patches 5, :facts 42, ...}
```

### Environment Configuration

**Frontend** (`viz/.env`):
```bash
# Change API URL for production
VITE_API_URL=http://localhost:3000
```

**Backend** (`core/src/vkm/api/server.clj`):
```clojure
(def server-config
  {:port 3000      ;; Change port here
   :join? false})
```

---

## Troubleshooting

### "Backend offline" indicator

**Symptoms**: Red "Backend offline" status, no upload zone visible

**Solutions**:
1. Check if backend is running:
   ```bash
   curl http://localhost:3000/health
   # Should return: {"status":"ok","service":"vkm-graph-api","version":"0.1.0"}
   ```

2. Restart backend:
   ```bash
   cd core
   clj -M:server
   ```

3. Check for port conflicts:
   ```bash
   lsof -i :3000
   # Kill any existing process on port 3000
   ```

### Upload fails with "Network error"

**Symptoms**: Upload shows error message, no facts extracted

**Solutions**:
1. Check browser console (F12) for errors
2. Verify CORS is configured (should be automatic)
3. Check backend logs for errors
4. Test upload manually:
   ```bash
   curl -X POST http://localhost:3000/api/upload \
     -H "Content-Type: application/json" \
     -d '{"content":"Test fact.","filename":"test.txt"}'
   ```

### No facts extracted

**Symptoms**: Upload succeeds but "0 facts extracted"

**Expected behavior**: The current implementation uses a placeholder for fact extraction. To see real extraction:

1. Integrate Claude API in `core/src/vkm/semantic.clj`:
   ```clojure
   (defn extract-facts-from-text [text]
     ;; TODO: Call Claude API here
     ;; For now returns empty list
     [])
   ```

2. Or add manual facts for testing:
   ```clojure
   (defn extract-facts-from-text [text]
     [{:claim/text text
       :claim/confidence 0.8
       :claim/topic :test
       :claim/valid-from (java.util.Date.)}])
   ```

### Frontend doesn't update after upload

**Symptoms**: Upload succeeds but graph doesn't show new data

**Solutions**:
1. Click "Backend Data" toggle button
2. Check browser console for errors
3. Verify patches were stored:
   ```bash
   curl http://localhost:3000/api/patches
   ```

### TypeScript errors

**Symptoms**: Frontend shows type errors in browser console

**Solutions**:
```bash
cd viz
npm run build
# Fix any type errors shown
```

---

## Next Steps

### 1. Integrate Claude API for Real Fact Extraction

Currently `extract-facts-from-text` is a placeholder. To get real extraction:

**Edit** `core/src/vkm/semantic.clj`:

```clojure
(defn extract-facts-from-text
  "Extract facts from text using Claude API.

   Returns: Vector of fact maps"
  [text]
  (let [response (call-claude-api
                   {:model "claude-sonnet-4-5"
                    :prompt (str "Extract factual claims from: " text)})
        facts (parse-claude-response response)]
    (map (fn [fact-text]
           {:claim/text fact-text
            :claim/confidence 0.8
            :claim/topic :extracted
            :claim/valid-from (java.util.Date.)})
         facts)))
```

### 2. Process YouTube Videos

**Go CLI** is ready for YouTube downloads:

```bash
cd cli
go build

# Download single video
./vkm-cli download-simple "https://youtube.com/watch?v=..."

# Download playlist
./vkm-cli download-playlist "https://youtube.com/playlist?list=..."
```

**Next**: Add Whisper transcription integration

### 3. Add Real-Time Updates

**WebSocket integration** for live updates:

1. Backend: Add WebSocket endpoint
2. Frontend: Subscribe to patch updates
3. Auto-refresh visualization on new patches

### 4. Enhanced Visualization

**Interactive features**:
- Search/filter facts
- Highlight relationships
- Export to PNG/SVG
- Share visualizations

---

## Production Deployment

### Backend

**Option 1: Uberjar**
```bash
cd core
clj -T:build uber
java -jar target/vkm-graph.jar
```

**Option 2: Docker**
```dockerfile
FROM clojure:temurin-21-tools-deps
WORKDIR /app
COPY . .
CMD ["clj", "-M:server"]
```

### Frontend

**Build for production**:
```bash
cd viz
npm run build
# Output: viz/dist/
```

**Deploy to Vercel**:
```bash
cd viz
vercel
```

**Deploy to Netlify**:
```bash
cd viz
netlify deploy --prod --dir=dist
```

---

## API Reference

### POST /api/upload

**Request**:
```json
{
  "content": "Large models learn better.",
  "filename": "document.txt"
}
```

**Response**:
```json
{
  "patch-id": "01234567-89ab-cdef-0123-456789abcdef",
  "facts-count": 5,
  "message": "Document processed successfully"
}
```

### GET /api/patches

**Query params**:
- `source-id` (optional): Filter by source ID

**Response**:
```json
{
  "patches": [
    {
      "db/id": "patch-1",
      "patch/timestamp": "2025-10-25T12:00:00Z",
      "patch/source": "document",
      "patch/facts": [...],
      "patch/edges": [...]
    }
  ],
  "count": 5
}
```

### GET /api/stats

**Response**:
```json
{
  "patches": 10,
  "facts": 42,
  "edges": 18,
  "morphisms": 5,
  "motives": 3
}
```

---

## Resources

- **Clojure Backend**: `core/src/vkm/api/server.clj`
- **React Frontend**: `viz/src/App.tsx`
- **API Client**: `viz/src/api/client.ts`
- **Upload Component**: `viz/src/components/UploadZone.tsx`
- **Visualization**: `viz/RUNNING.md`

---

**Built with:** Clojure, React, TypeScript, Datomic, D3.js
**Philosophy:** Information-First Architecture, Beauty First Design
**Inspiration:** Rich Hickey, Category Theory, Homotopy Type Theory
