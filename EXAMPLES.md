# VKM Graph - Example Workflows

Real-world examples demonstrating the complete knowledge graph evolution system.

---

## Quick Start (30 seconds)

```bash
# Automated setup and launch
./scripts/quickstart.sh

# Opens browser to http://localhost:5173
# Backend at http://localhost:3000
```

**What you'll see:**
- ✅ Synthetic AI scaling data visualization
- 📊 Timeline showing knowledge evolution 2020-2025
- 🎨 Interactive force-directed graph

---

## Example 1: Upload Documents

### Generate Test Data

```bash
./scripts/generate-test-data.sh
```

Creates 6 documents in `data/test-documents/`:
- AI Scaling
- Architectures
- Training Techniques
- Evaluation
- Deployment
- Emerging Trends

### Upload and Visualize

1. **Open Frontend**
   - Navigate to http://localhost:5173
   - Ensure "Backend online" indicator is green

2. **Upload Document**
   - Drag `data/test-documents/01-ai-scaling.txt` onto upload zone
   - Watch extraction progress
   - See "Document processed successfully (N facts extracted)"

3. **View Results**
   - Automatically switches to "Backend Data"
   - Graph updates with extracted facts
   - Click nodes to see details

4. **Upload More Documents**
   - Upload `02-architectures.txt`
   - Upload `03-training.txt`
   - Watch knowledge graph grow!

5. **Explore Timeline**
   - Drag timeline scrubber
   - See how knowledge evolved
   - Each upload creates a new patch

---

## Example 2: Process YouTube Video

### Prerequisites

```bash
# Install yt-dlp
pip install yt-dlp

# Set API keys
export OPENAI_API_KEY=sk-...
export CLAUDE_API_KEY=sk-ant-...
```

### Process Single Video

```bash
cd cli
go build

# Process a short educational video
./vkm-cli pipeline "https://youtube.com/watch?v=VIDEO_ID"
```

**Pipeline Steps:**
```
[1/4] Downloading...
  ✓ Downloaded: video.mp4

[2/4] Transcribing with Whisper...
  ✓ Transcribed: 1,234 characters

[3/4] Extracting facts with Claude...
  ✓ Extracted: 12 facts

[4/4] Complete!
  → Patch ID: 01234567...
  → View at: http://localhost:5173
```

### Process Playlist

```bash
./vkm-cli pipeline "https://youtube.com/playlist?list=PLxxx..."
```

Processes each video sequentially, creating patches over time.

---

## Example 3: Search and Filter

### Search by Text

1. Open http://localhost:5173
2. Type "transformer" in search box
3. See only facts mentioning transformers

### Filter by Confidence

1. Click filter icon (funnel)
2. Set min confidence to 80%
3. Graph shows only high-confidence facts

### Filter by Topic

1. Expand filters
2. Click topic chips: "scaling", "architecture"
3. See only facts in those topics

### Combine Filters

```
Search: "model"
Min Confidence: 70%
Topics: ["scaling", "training"]
```

Shows models facts about scaling/training with 70%+ confidence.

---

## Example 4: Export Data

### Export Visualizations

1. Click "Export" button in header
2. Choose export format:

**Vector Graphics (SVG)**
- Click "Vector (SVG)"
- Opens in design tools (Figma, Illustrator)
- Perfect for presentations

**High-Res Image (PNG)**
- Click "Image (PNG)"
- 2x resolution for quality
- Ready for slides/papers

### Export Data

**All Patches (JSON)**
```json
[
  {
    "db/id": "patch-1",
    "patch/timestamp": "2025-10-25T12:00:00Z",
    "patch/facts": [...],
    "patch/edges": [...]
  }
]
```

**Facts CSV** (Excel-ready)
```csv
Timestamp,Fact,Confidence,Topic,Source
2025-10-25T12:00:00Z,"Large models learn better",0.85,scaling,"doc.txt"
```

**Markdown Report**
```markdown
# VKM Graph Knowledge Evolution

## Patch 1: 2025-10-25T12:00:00Z

### Facts
1. **Large models learn better** (85% confidence, topic: scaling)
```

---

## Example 5: Fact Details

### View Metadata

1. Click any node in graph
2. Modal shows:
   - Full fact text
   - Confidence (badge + bar)
   - Topic
   - Timestamp
   - Fact ID

### Copy to Clipboard

- **Copy Text**: Just the fact claim
- **Copy JSON**: Complete fact object

Perfect for:
- Citing facts in papers
- Sharing with team
- Debugging

---

## Example 6: Track Evolution

### Scenario: AI Safety Research

**Week 1** - Upload initial paper
```
Facts: 5
Topics: ["safety", "alignment"]
Avg Confidence: 72%
```

**Week 2** - Add new research
```
Facts: 12 (+7)
Topics: ["safety", "alignment", "interpretability"]
Avg Confidence: 78% (+6%)
```

**Week 3** - Contradictory findings
```
Facts: 18 (+6)
New edges: 3 contradicts relationships
Avg Confidence: 75% (-3%)
```

**Timeline View**:
- Drag scrubber through weeks
- Watch facts appear/disappear
- See confidence changes
- Identify contradictions

---

## Example 7: Docker Deployment

### Local Docker Compose

```bash
# Set API keys
export CLAUDE_API_KEY=sk-ant-...
export OPENAI_API_KEY=sk-...

# Build and run
docker-compose up --build

# Access
open http://localhost:5173
```

### Production Deployment

```bash
# Build images
docker-compose -f docker-compose.prod.yml build

# Deploy to cloud
docker-compose -f docker-compose.prod.yml up -d

# Check health
curl https://api.your-domain.com/health
```

---

## Example 8: API Integration

### Upload via API

```bash
curl -X POST http://localhost:3000/api/upload \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Large models show emergent capabilities.",
    "filename": "test.txt"
  }'
```

**Response:**
```json
{
  "patch-id": "01234567-89ab-cdef-0123-456789abcdef",
  "facts-count": 1,
  "message": "Document processed successfully"
}
```

### Query Patches

```bash
curl http://localhost:3000/api/patches
```

**Response:**
```json
{
  "patches": [...],
  "count": 10
}
```

### Get Statistics

```bash
curl http://localhost:3000/api/stats
```

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

---

## Example 9: Real Research Workflow

### Day 1: Literature Review

```bash
# Create project directory
mkdir my-research
cd my-research

# Collect papers
cat > paper1.txt << EOF
[Abstract of paper 1...]
EOF

cat > paper2.txt << EOF
[Abstract of paper 2...]
EOF

# Upload papers
for file in *.txt; do
    curl -X POST http://localhost:3000/api/upload \
      -H "Content-Type: application/json" \
      -d "{\"content\": \"$(cat $file)\", \"filename\": \"$file\"}"
    sleep 2
done
```

### Day 2: Add Videos

```bash
# Process lecture series
vkm-cli pipeline "https://youtube.com/playlist?list=PLxxx..."
```

### Day 3: Analysis

1. Open visualization
2. Filter by topic: "methodology"
3. Find contradictions
4. Export high-confidence facts
5. Write literature review

### Week End: Share Results

```bash
# Export all data
curl http://localhost:3000/api/patches > research-patches.json

# Export visualization
# Click "Export" → "Image (PNG)"

# Export report
# Click "Export" → "Report (Markdown)"
```

---

## Example 10: CI/CD Integration

### GitHub Actions Workflow

```yaml
name: Process Research Papers

on:
  push:
    paths:
      - 'papers/**/*.txt'

jobs:
  process:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Upload to VKM Graph
        env:
          API_URL: ${{ secrets.VKM_API_URL }}
        run: |
          for file in papers/*.txt; do
            curl -X POST $API_URL/api/upload \
              -H "Content-Type: application/json" \
              -d "{\"content\": \"$(cat $file)\", \"filename\": \"$(basename $file)\"}"
          done
```

---

## Troubleshooting Examples

### Example: Backend Not Responding

```bash
# Check backend health
curl http://localhost:3000/health

# Check logs
tail -f /tmp/vkm-backend.log

# Restart backend
cd core
clj -M:server
```

### Example: Upload Fails

```bash
# Check API key
echo $CLAUDE_API_KEY

# Test API directly
curl -X POST http://localhost:3000/api/upload \
  -H "Content-Type: application/json" \
  -d '{"content": "test", "filename": "test.txt"}'

# Check backend logs for errors
tail -f /tmp/vkm-backend.log
```

### Example: No Facts Extracted

**Cause**: CLAUDE_API_KEY not set

**Solution**:
```bash
export CLAUDE_API_KEY=sk-ant-...
# Restart backend
cd core && clj -M:server
```

---

## Performance Examples

### Batch Processing

```bash
# Process 100 documents
time for i in {1..100}; do
    curl -X POST http://localhost:3000/api/upload \
      -H "Content-Type: application/json" \
      -d "{\"content\": \"Document $i content\", \"filename\": \"doc$i.txt\"}"
done

# Typical: ~2-3 seconds per document
# Total: ~5-8 minutes for 100 documents
```

### Large Dataset

```bash
# Process 1000-fact corpus
# Memory usage: ~500MB backend
# Response time: <100ms for queries
# Visualization: Smooth at 1000 nodes
```

---

## Next Steps

1. **Try Quick Start**: `./scripts/quickstart.sh`
2. **Upload Test Data**: `./scripts/generate-test-data.sh`
3. **Process YouTube**: See Example 2
4. **Explore Features**: Search, filter, export
5. **Deploy Production**: See `PRODUCTION_GUIDE.md`

---

**Happy Knowledge Graphing!** 🎉

For more help:
- Quick Start: `MVP_GUIDE.md`
- Production: `PRODUCTION_GUIDE.md`
- API Reference: `PRODUCTION_GUIDE.md#api-reference`
