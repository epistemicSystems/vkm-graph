# VKM Graph - Production Features Guide

**Complete production-ready knowledge graph evolution system**

---

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Core Features](#core-features)
4. [YouTube Pipeline](#youtube-pipeline)
5. [Advanced Frontend Features](#advanced-frontend-features)
6. [API Reference](#api-reference)
7. [Production Deployment](#production-deployment)
8. [Troubleshooting](#troubleshooting)

---

## Overview

The VKM Graph system is now production-ready with:

✅ **Claude API Integration** - Real fact extraction from text
✅ **Whisper Transcription** - Audio/video to text pipeline
✅ **End-to-End YouTube Processing** - Download → transcribe → extract → visualize
✅ **Advanced Search & Filtering** - Multi-dimensional fact discovery
✅ **Export Capabilities** - PNG, SVG, JSON, CSV, Markdown
✅ **Interactive Fact Details** - Rich metadata display
✅ **Production-Ready Backend** - Robust error handling & retries

---

## Quick Start

###  1. Set Up Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env and add your API keys
nano .env
```

Required keys:
```bash
CLAUDE_API_KEY=sk-ant-...     # From https://console.anthropic.com/
OPENAI_API_KEY=sk-...         # From https://platform.openai.com/
```

### 2. Start Backend

```bash
cd core
clj -M:server
```

### 3. Start Frontend

```bash
cd viz
npm install  # First time only
npm run dev
```

### 4. Test End-to-End Pipeline

```bash
cd cli
go build

# Process a YouTube video
./vkm-cli pipeline "https://youtube.com/watch?v=VIDEO_ID"

# Or test with document upload
# Open http://localhost:5173 and drag-drop a text file
```

---

## Core Features

### 1. Claude-Powered Fact Extraction

**Location:** `core/src/vkm/semantic.clj`

**Features:**
- Atomic fact extraction
- Confidence scoring (0.0-1.0)
- Topic categorization
- Automatic retry with exponential backoff
- Rate limit handling
- JSON parsing from markdown code blocks

**Example:**

```clojure
(require '[vkm.semantic :as semantic])

(def facts (semantic/extract-facts-from-text
  "Large language models show emergent capabilities at scale.
   Training with more data improves performance."))

; Returns:
; [{:db/id "fact-1"
;   :claim/text "Large language models show emergent capabilities at scale"
;   :claim/confidence 0.85
;   :claim/topic :scaling}
;  {:db/id "fact-2"
;   :claim/text "Training with more data improves performance"
;   :claim/confidence 0.90
;   :claim/topic :performance}]
```

**Configuration:**

```clojure
;; Custom model
(semantic/extract-facts-from-text text
  :model "claude-sonnet-4-20250514"
  :max-retries 3)
```

### 2. Whisper Transcription

**Location:** `cli/cmd/transcribe_whisper.go`

**Features:**
- OpenAI Whisper API integration
- Automatic language detection
- Batch processing
- 25MB file size validation
- Multiple format support (mp3, mp4, wav, webm, etc.)

**Usage:**

```bash
# Single file
vkm-cli transcribe-whisper video.mp4

# Multiple files
vkm-cli transcribe-whisper *.mp3 --output transcripts/

# Specify language
vkm-cli transcribe-whisper audio.mp3 --language en
```

**Options:**
- `--output, -o`: Output directory (default: `data/transcripts`)
- `--model, -m`: Whisper model (default: `whisper-1`)
- `--language, -l`: Language code (optional, auto-detected)

### 3. End-to-End YouTube Pipeline

**Location:** `cli/cmd/pipeline.go`

**Features:**
- Automated 4-step processing
- Backend health checking
- Progress tracking
- Automatic cleanup
- Error recovery

**Usage:**

```bash
# Process single video
vkm-cli pipeline "https://youtube.com/watch?v=dQw4w9WgXcQ"

# Process playlist
vkm-cli pipeline "https://youtube.com/playlist?list=..."

# Keep intermediate files
vkm-cli pipeline VIDEO_URL --keep-files

# Custom backend
vkm-cli pipeline VIDEO_URL --backend http://prod-server:3000
```

**Pipeline Steps:**

```
[1/4] Download
  ├─ Uses yt-dlp
  ├─ Saves to data/pipeline/videos/
  └─ Video file: video.mp4

[2/4] Transcribe
  ├─ Whisper API call
  ├─ Saves to data/pipeline/transcripts/
  └─ Transcript: video.txt

[3/4] Extract
  ├─ Send to backend /api/upload
  ├─ Claude extracts facts
  ├─ Creates patch in Datomic
  └─ Patch ID: 01234567...

[4/4] Visualize
  └─ View at http://localhost:5173
```

**Error Handling:**
- Network failures → automatic retry
- Rate limits → exponential backoff
- Backend down → clear error message
- Invalid files → skip and continue

---

## Advanced Frontend Features

### 1. Search & Filter Panel

**Location:** `viz/src/components/SearchPanel.tsx`

**Features:**
- Text search across all facts
- Confidence range sliders (min/max)
- Topic filtering (multi-select)
- Active filters summary
- Clear all filters

**Interface:**

```typescript
interface SearchFilters {
  textQuery: string;
  minConfidence: number;     // 0.0 - 1.0
  maxConfidence: number;     // 0.0 - 1.0
  topics: string[];          // Selected topics
}
```

**Usage:**

```tsx
const [filters, setFilters] = useState<SearchFilters>({
  textQuery: '',
  minConfidence: 0,
  maxConfidence: 1,
  topics: [],
});

<SearchPanel
  filters={filters}
  onChange={setFilters}
  availableTopics={['scaling', 'architecture', 'performance']}
  onClear={() => setFilters(defaultFilters)}
/>
```

### 2. Export Panel

**Location:** `viz/src/components/ExportPanel.tsx`

**Formats:**

**Data Exports:**
- **All Patches (JSON)** - Complete patch history
- **Current Patch (JSON)** - Active snapshot
- **Graph Data (JSON)** - Nodes and links for D3
- **Facts (CSV)** - Spreadsheet-friendly format
- **Report (Markdown)** - Human-readable documentation

**Visual Exports:**
- **Vector (SVG)** - Scalable graphics
- **Image (PNG)** - High-resolution raster (2x quality)

**Example CSV Output:**

```csv
Timestamp,Fact,Confidence,Topic,Source
2025-10-25T12:00:00Z,"Large models learn better",0.85,scaling,"youtube:abc123"
2025-10-25T12:00:00Z,"Transformers enable parallel processing",0.92,architecture,"youtube:abc123"
```

**Example Markdown Output:**

```markdown
# VKM Graph Knowledge Evolution

Exported: 2025-10-25T12:00:00Z
Total Patches: 5

## Patch 1: 2025-10-20T00:00:00Z

**Source:** {"type": "youtube", "id": "abc123"}

### Facts

1. **Large models learn better** (85% confidence, topic: scaling)
2. **Training requires more compute** (78% confidence, topic: resources)
```

### 3. Fact Detail Panel

**Location:** `viz/src/components/FactDetailPanel.tsx`

**Features:**
- Modal overlay with blur background
- Comprehensive metadata display
- Confidence visualization (badge + bar)
- Copy to clipboard (text & JSON)
- Keyboard navigation (ESC to close)

**Metadata Displayed:**
- Claim text
- Confidence percentage (with visual bar)
- Topic badge
- Valid-from timestamp
- Fact ID
- Source (if available)
- Evidence (if available)

**Actions:**
- Copy Text → clipboard
- Copy JSON → full fact object
- Close → ESC or click outside

---

## API Reference

### Backend Endpoints

All endpoints at `http://localhost:3000`

#### GET /health

Health check

**Response:**
```json
{
  "status": "ok",
  "service": "vkm-graph-api",
  "version": "0.1.0"
}
```

#### GET /api/stats

Database statistics

**Response:**
```json
{
  "patches": 10,
  "facts": 42,
  "edges": 18,
  "morphisms": 5,
  "motives": 3
}
```

#### POST /api/upload

Upload document for processing

**Request:**
```json
{
  "content": "Text to analyze...",
  "filename": "document.txt"
}
```

**Response:**
```json
{
  "patch-id": "01234567-89ab-cdef-0123-456789abcdef",
  "facts-count": 5,
  "message": "Document processed successfully"
}
```

#### GET /api/patches

List all patches

**Query Params:**
- `source-id` (optional): Filter by source

**Response:**
```json
{
  "patches": [...],
  "count": 10
}
```

#### GET /api/patches/:id

Get specific patch

**Response:**
```json
{
  "db/id": "patch-1",
  "patch/timestamp": "2025-10-25T12:00:00Z",
  "patch/source": {...},
  "patch/facts": [...],
  "patch/edges": [...]
}
```

#### POST /api/process

Process transcript directory

**Request:**
```json
{
  "source-id": "youtube:abc123",
  "transcript-dir": "/path/to/transcripts"
}
```

**Response:**
```json
{
  "patch-id": "...",
  "transcripts-processed": 5,
  "facts-extracted": 42
}
```

#### POST /api/patches/query

Query patches with filters

**Request:**
```json
{
  "topic": "scaling",
  "min-confidence": 0.8,
  "time-range": {
    "start": "2025-01-01T00:00:00Z",
    "end": "2025-12-31T23:59:59Z"
  }
}
```

---

## Production Deployment

### Backend (Clojure)

**Option 1: Uberjar**

```bash
cd core

# Build
clj -T:build uber

# Run
java -jar target/vkm-graph.jar

# With custom port
SERVER_PORT=8080 java -jar target/vkm-graph.jar
```

**Option 2: Docker**

```dockerfile
FROM clojure:temurin-21-tools-deps
WORKDIR /app
COPY core/ .
ENV CLAUDE_API_KEY=sk-ant-...
ENV OPENAI_API_KEY=sk-...
EXPOSE 3000
CMD ["clj", "-M:server"]
```

```bash
docker build -t vkm-graph-backend .
docker run -p 3000:3000 \
  -e CLAUDE_API_KEY=$CLAUDE_API_KEY \
  -e OPENAI_API_KEY=$OPENAI_API_KEY \
  vkm-graph-backend
```

**Option 3: Docker Compose**

```yaml
version: '3.8'
services:
  backend:
    build: ./core
    ports:
      - "3000:3000"
    environment:
      - CLAUDE_API_KEY
      - OPENAI_API_KEY
      - DATOMIC_URI=datomic:mem://vkm-graph
    volumes:
      - ./data:/app/data

  frontend:
    build: ./viz
    ports:
      - "5173:5173"
    depends_on:
      - backend
    environment:
      - VITE_API_URL=http://backend:3000
```

### Frontend (React)

**Build:**

```bash
cd viz
npm run build
# Output: viz/dist/
```

**Deploy to Vercel:**

```bash
cd viz
npm install -g vercel
vercel --prod
```

**Deploy to Netlify:**

```bash
cd viz
npm run build
netlify deploy --prod --dir=dist
```

**Environment Variables:**

```bash
# Vercel/Netlify
VITE_API_URL=https://api.your-domain.com
```

### CLI (Go)

**Build for Multiple Platforms:**

```bash
cd cli

# Linux
GOOS=linux GOARCH=amd64 go build -o vkm-cli-linux

# macOS
GOOS=darwin GOARCH=amd64 go build -o vkm-cli-macos

# Windows
GOOS=windows GOARCH=amd64 go build -o vkm-cli.exe
```

**Install Globally:**

```bash
go install
# Now available as: vkm
```

---

## Troubleshooting

### Claude API Issues

**Problem:** "CLAUDE_API_KEY not set"

**Solution:**
```bash
export CLAUDE_API_KEY=sk-ant-...
# Or add to .env file
```

**Problem:** Rate limited (429 error)

**Solution:** Automatic retries with backoff are built-in. If persistent:
```clojure
;; Increase retry delay in semantic.clj
(Thread/sleep (* 3000 retry-count))  ; Increase from 2000
```

### Whisper Transcription Issues

**Problem:** "File size exceeds 25MB limit"

**Solution:** Compress video before transcription:
```bash
ffmpeg -i large-video.mp4 -b:a 64k output.mp4
```

**Problem:** "OPENAI_API_KEY not set"

**Solution:**
```bash
export OPENAI_API_KEY=sk-...
```

### Pipeline Issues

**Problem:** "Backend not reachable"

**Solution:**
```bash
# Check backend is running
curl http://localhost:3000/health

# Start backend
cd core && clj -M:server
```

**Problem:** "yt-dlp not found"

**Solution:**
```bash
# Install yt-dlp
pip install yt-dlp

# Or via homebrew (macOS)
brew install yt-dlp
```

### Frontend Issues

**Problem:** "Backend offline" indicator

**Solution:**
1. Check `.env` has correct `VITE_API_URL`
2. Check backend is running
3. Check CORS is enabled (automatic in dev)

**Problem:** Export PNG fails

**Solution:**
- Try SVG export instead
- Ensure browser allows canvas operations
- Check console for CORS errors

---

## Performance Tuning

### Backend

**Increase Worker Threads:**

```clojure
;; In api/server.clj
(def server-config
  {:port 3000
   :join? false
   :max-threads 50})  ; Increase from default
```

**Enable Database Caching:**

```clojure
;; In db.clj
(def db-config
  {:cache-size (* 1024 1024 512)  ; 512MB cache
   :memory-index-threshold 32768})
```

### Frontend

**Enable Production Build:**

```bash
npm run build
# Minification, tree-shaking, code splitting
```

**Lazy Load Components:**

```typescript
const ExportPanel = lazy(() => import('@/components/ExportPanel'));
const FactDetailPanel = lazy(() => import('@/components/FactDetailPanel'));
```

### CLI

**Parallel Processing:**

```go
// In pipeline.go, process multiple URLs concurrently
var wg sync.WaitGroup
for _, url := range args {
  wg.Add(1)
  go func(u string) {
    defer wg.Done()
    processURL(u)
  }(url)
}
wg.Wait()
```

---

## Monitoring & Logging

### Backend Logs

```clojure
;; Set log level
(System/setenv "LOG_LEVEL" "debug")

;; View logs
tail -f logs/vkm-graph.log
```

### API Metrics

```bash
# Check processing stats
curl http://localhost:3000/api/stats

# Monitor health
watch -n 5 curl http://localhost:3000/health
```

### Error Tracking

```clojure
;; Add Sentry integration
(require '[sentry-clj.core :as sentry])

(sentry/init! "https://your-dsn@sentry.io/project")

;; Wrap handlers
(defn with-error-tracking [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (sentry/send-event {:message (.getMessage e)})
        (throw e)))))
```

---

## Security Best Practices

1. **Never commit API keys**
   - Use `.env` file (in `.gitignore`)
   - Use environment variables in production

2. **Enable HTTPS in production**
   ```clojure
   ;; Use reverse proxy (nginx, Caddy)
   ;; Or enable SSL in Ring
   ```

3. **Rate limiting**
   ```clojure
   ;; Add rate limiting middleware
   (require '[ring.middleware.rate-limit :as rate-limit])

   (def app
     (-> handler
         (rate-limit/wrap-rate-limit
           :limit 100
           :window 60)))  ; 100 requests per minute
   ```

4. **Input validation**
   - Already implemented in `upload-document-handler`
   - Validates content is non-empty
   - Sanitizes filenames

---

## Resources

- **Documentation:** `docs/`
- **Quick Start:** `MVP_GUIDE.md`
- **Visualization:** `viz/RUNNING.md`
- **API Examples:** `examples/`
- **Architecture:** `docs/ARCHITECTURE.md`

**External Links:**
- Claude API: https://docs.anthropic.com/
- Whisper API: https://platform.openai.com/docs/guides/speech-to-text
- Datomic: https://docs.datomic.com/
- React + D3: https://2019.wattenberger.com/blog/react-and-d3

---

**Built with Love ❤️ by the VKM Graph Team**

**License:** MIT
**Version:** 1.0.0
**Last Updated:** 2025-10-25
