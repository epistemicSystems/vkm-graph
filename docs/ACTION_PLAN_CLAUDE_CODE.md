# Action Plan: The Knowledge Graph Evolution System
## 8-Week Shipping Sprint

**Philosophy:** Paul Graham's "Do Things That Don't Scale" meets agentic programming. Ship constantly. Get something on screen by end of day 1. Iterate ruthlessly. Use Claude Code as your primary development partner.

**Principles:**
1. **Bias toward shipping** - A working demo beats a perfect spec
2. **Human oversight at gates** - Claude Code does 90% of work, humans make 10% of decisions
3. **Iterative refinement** - First version is intentionally imperfect, version 2 is better
4. **Specifications as contracts** - Clear specs mean Claude Code doesn't waste time asking
5. **Atomic commits** - Each Claude Code run produces something deployable

---

## Overview: The 8-Week Strategy

### Weekly Shipping Goals

| Week | What Ships | Success Metric |
|------|-----------|-----------------|
| 1 | Beautiful prototype (figma → clickable) | Can click through 5-image timeline |
| 2 | Design polish + internal feedback | 8/10 people want to use it |
| 3 | Document upload + patch extraction | 3 users upload docs, see visualization |
| 4 | Timeline scrubber + smooth interaction | Users say "this is delightful" |
| 5 | Claude-generated patch gallery | 7/10 patches feel authentic |
| 6 | Multi-topic support + validation | Gallery of 10 topics is compelling |
| 7 | End-to-end video ingestion | One channel processed, visualized |
| 8 | Performance optimization + polish | System handles 100 videos smoothly |

### Team & Ownership

```
PM (You)           — Direction, unblocking, decisions
Designer          — Week 1-2 (then advisory)
Frontend Engineer — React + use.GPU (ongoing)
Backend Engineer  — Go CLI + Clojure processing (ongoing)
ML Engineer       — Claude integration + validation (ongoing)

Claude Code works with all of them
```

---

# PHASE 0: BEAUTY FIRST (WEEKS 1-2)

## Goal
Get something visually stunning on screen with synthetic data. Prove the concept resonates. No infrastructure, no ingestion, just pure UX.

## Week 1: Day-by-Day Sprint

### Monday: Concept → Figma Prototype

**Owner:** Designer + PM  
**Claude Code:** Not involved yet  
**Time Budget:** 1 day

**Task List:**
- [ ] Designer: Sketch 5 key screens (Figma)
  - Screen 1: Intro/title
  - Screen 2: Timeline with 5 snapshots
  - Screen 3: Concept cluster visualization
  - Screen 4: Transition detail
  - Screen 5: Insight moment
- [ ] Designer: Create design system (colors, typography, spacing)
- [ ] PM: Write microcopy for each screen
- [ ] Designer: Comp out hover/click states

**Acceptance Criteria:**
- [ ] 5 screens exist in Figma
- [ ] Design is "premium" (looks like Apple/Stripe quality)
- [ ] PM approves microcopy
- [ ] Colors chosen (confidence scale: gray → gold)

**Daily Standup Output:**
```
Ship: Figma file with 5 comps
Owner: Designer
Reviewers: PM
Decision: "Does this feel right?" (gate to proceed)
```

---

### Tuesday-Wednesday: Figma → Framer Prototype

**Owner:** Designer  
**Claude Code:** Can assist with interaction logic  
**Time Budget:** 1.5 days

**Claude Code Prompt 1: Interactive Prototype Specification**
```
You are building an interactive prototype in Framer (or similar tool).

Specification:
- 5 screens showing knowledge evolution
- Timeline scrubber that morphs visualization
- Concept clusters show/hide on scrub
- Confidence scores update (0-100 range)
- Color transitions from gray (low) to gold (high)
- Smooth animations (300ms transitions)

Design files: [Figma link]

Generate:
1. Component structure (5 React-like components)
2. State management (what changes on scrub?)
3. Animation specifications (CSS/Framer Motion)
4. Synthetic data structure (5 "patches" as JSON)

Output: Code ready for Framer or React.
```

**Task List:**
- [ ] Designer: Set up Framer project
- [ ] Claude Code: Generate component structure
- [ ] Designer: Translate design to Framer components
- [ ] Claude Code: Generate animation logic
- [ ] Designer: Test interactions
- [ ] Designer: Refine visual polish

**Acceptance Criteria:**
- [ ] Can scrub timeline left-right
- [ ] Visualization morphs smoothly
- [ ] Confidence scores update in real-time
- [ ] At least 2 people test it, say "cool"

**Daily Standup Output:**
```
Ship: Clickable prototype (Framer link or deployed)
Owner: Designer
Reviewers: PM, Frontend Engineer
Decision: "Does this feel magical?" (gate to proceed)
```

---

### Thursday: Internal Feedback Loop

**Owner:** PM  
**Time Budget:** 1 day

**Task List:**
- [ ] Gather internal feedback (10 people)
  - "Would you want to use this if the data was real?"
  - "What confused you?"
  - "What delighted you?"
- [ ] Collect sentiment scores (1-10)
- [ ] Document pain points
- [ ] Prioritize fixes (top 3)

**Acceptance Criteria:**
- [ ] 8/10 people say "yes" to "want to use"
- [ ] Identified top 3 issues
- [ ] Decision made: ship as-is or iterate

**Daily Standup Output:**
```
Ship: Feedback summary + decision
Owner: PM
Decision gate: "Should we iterate or ship?"
  If YES → Friday iteration
  If NO → Move to week 2 polish
```

---

### Friday: Polish & Lock In

**Owner:** Designer + Claude Code  
**Time Budget:** 0.5 day

**Task List:**
- [ ] Designer: Apply top 3 fixes
- [ ] Claude Code: Refine animations if needed
- [ ] Designer: Final visual polish
- [ ] PM: Write internal summary ("here's what we're building")

**Acceptance Criteria:**
- [ ] Prototype feels premium
- [ ] No obvious bugs
- [ ] Ready to show stakeholders

**Daily Standup Output:**
```
Ship: Final prototype + internal memo
Owner: Designer
Status: ✅ WEEK 1 COMPLETE - Beautiful prototype exists
```

---

## Week 2: Design Refinement & Stakeholder Buy-In

### Monday-Tuesday: Executive Presentation

**Owner:** PM + Designer  
**Time Budget:** 1 day

**Task List:**
- [ ] PM: Write 1-page executive summary
- [ ] Designer: Create 3-slide deck
  - Slide 1: The problem ("invisible understanding journey")
  - Slide 2: The solution (interactive prototype demo)
  - Slide 3: What's next (16-week roadmap, resource ask)
- [ ] PM: Schedule demo with leadership
- [ ] Designer: Practice demo (should take <5 minutes)
- [ ] PM: Prepare talking points

**Acceptance Criteria:**
- [ ] Executive leadership watches 5-minute demo
- [ ] Gets clear signal: "Yes, build this" or "Pivot"

**Daily Standup Output:**
```
Ship: Executive memo + approval
Owner: PM
Decision: Proceed to Phase 1
```

---

### Wednesday-Thursday: Design System Expansion

**Owner:** Designer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 2: Design System Spec**
```
Create a comprehensive design system for the Knowledge Graph Evolution app.

Components needed:
- Timeline component (horizontal scrubber)
- GraphNode component (concept cluster node)
- FactCard component (fact/claim card)
- EdgeLine component (relationship line)
- TooltipComponent (hover info)
- ButtonComponent (primary/secondary)
- InputComponent (search/filter)

For each component:
1. Visual spec (size, color, spacing)
2. State variants (default, hover, active, disabled)
3. Animation specs (if any)
4. Accessibility specs (ARIA labels, etc.)

Output: Storybook-ready component library spec
```

**Task List:**
- [ ] Claude Code: Generate design system spec
- [ ] Designer: Create Storybook entries (or Figma components)
- [ ] Frontend Engineer: Review for implementability
- [ ] Designer: Finalize component library

**Acceptance Criteria:**
- [ ] 10+ components defined
- [ ] All have visual + interaction specs
- [ ] Frontend engineer says "I can build this"

**Daily Standup Output:**
```
Ship: Component library + Storybook
Owner: Designer + Claude Code
Status: ✅ PHASE 0 COMPLETE
```

---

### Friday: Handoff to Engineering

**Owner:** PM + Designer + Frontend Engineer  
**Time Budget:** 0.5 day

**Task List:**
- [ ] Designer: Export all assets (SVGs, color palette, etc.)
- [ ] PM: Write handoff doc for engineering
- [ ] Frontend Engineer: Reviews and asks clarifying questions
- [ ] PM: Resolves blockers
- [ ] Team: Plan Phase 1 kick-off

**Acceptance Criteria:**
- [ ] Frontend engineer confident to build
- [ ] No unclear specs
- [ ] Ready to start Phase 1 Monday

**Daily Standup Output:**
```
Ship: Handoff doc + asset package
Owner: PM
Status: ✅ WEEK 2 COMPLETE - Design locked, beauty proven
Milestone: Internal team thinks this could work
```

---

# PHASE 1: LOCAL PROOF (WEEKS 3-4)

## Goal
Get working visualization with real user data (documents). Users upload, system extracts patches, visualization appears. Prove the entire flow end-to-end.

## Week 3: Document Upload + Extraction

### Monday: Spec → Codebase

**Owner:** Frontend Engineer + Backend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 3: Phase 1 Codebase Scaffold**
```
Initialize a production-ready codebase for Phase 1.

Requirements:
1. Frontend (React + TypeScript)
   - Directory structure
   - Vite config (fast dev server)
   - Component scaffold (based on design system from Phase 0)
   - State management (Context API or lightweight)

2. Backend (Clojure)
   - Project structure (deps.edn)
   - REPL config
   - Process definitions (from Architecture doc)
   - LocalStorage backend stub

3. Shared
   - EDN data structures (Patch, Fact, Edge specs)
   - Type definitions (TypeScript interfaces)
   - Test infrastructure

4. CI/CD stub
   - GitHub Actions (lint, test on push)
   - Deployment target (Vercel for frontend)

Output: 
- Git repository with all scaffolding
- README with "how to run" instructions
- Makefile with common commands (make dev, make test, etc.)

Architecture: Follow Rich Hickey principles (from ARCHITECTURE doc)
```

**Task List:**
- [ ] Claude Code: Generate codebase scaffold
- [ ] Backend Engineer: Review Clojure structure
- [ ] Frontend Engineer: Review React structure
- [ ] Both: Run locally, confirm it works
- [ ] PM: Create GitHub project board (8-week burndown)

**Acceptance Criteria:**
- [ ] Codebase runs locally
- [ ] Can start dev server
- [ ] Can start REPL
- [ ] Tests pass (even if trivial)

**Daily Standup Output:**
```
Ship: Initial codebase (GitHub repo)
Owner: Claude Code (Backend Engineer reviews)
Status: ✅ Foundation ready
Next: Start building components
```

---

### Tuesday: Upload Component

**Owner:** Frontend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 4: Document Upload UI**
```
Build a document upload interface for the Knowledge Graph app.

Requirements:
1. Visual: Premium drag-drop zone (from design system)
2. Behavior:
   - User drags documents (PDF, MD, TXT, Google Docs link)
   - Shows file list + upload progress
   - Accepts 1-5 documents
   - Shows "extracting..." status
3. Error handling:
   - File too large → error message
   - Invalid format → skip
   - Network error → retry UI
4. Success state:
   - "Patches ready!" → show spinner
   - After extraction, show visualization

Technical:
- React component
- TypeScript
- React hooks for state
- No external upload service (local processing)
- Follow design system from Phase 0

Output:
1. React component (UploadZone.tsx)
2. Acceptance tests (test cases)
3. Storybook entry (UI isolated testing)
```

**Task List:**
- [ ] Claude Code: Generate upload component
- [ ] Frontend Engineer: Review + integrate
- [ ] Frontend Engineer: Test locally
- [ ] Frontend Engineer: Add to Storybook

**Acceptance Criteria:**
- [ ] Can drag-drop file
- [ ] Shows progress
- [ ] No errors on upload
- [ ] Storybook renders component

**Daily Standup Output:**
```
Ship: UploadZone component (Storybook link)
Owner: Frontend Engineer
Status: ✅ Users can upload documents
```

---

### Wednesday: Extraction Process

**Owner:** Backend Engineer + ML Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 5: Document Extraction Pipeline**
```
Build a document extraction pipeline using Claude API.

Input: File content (text)
Output: Structured claims (EDN)

Pipeline:
1. Read file
2. Parse to text (handle MD, TXT, PDF)
3. Chunk text (5-10 minute equivalents)
4. For each chunk:
   - Call Claude: "Extract claims about [topic]"
   - Parse response to Claim EDN
   - Assign IDs, timestamps
5. Merge results into single claim list
6. Return as JSON

Claude prompt for extraction:
"Given this text, extract all factual claims in this format:
{
  \"claims\": [
    {\"text\": \"...\", \"confidence\": 0.85, \"topic\": \"scaling\"},
    ...
  ]
}"

Implementation:
- Clojure function: (extract-claims-process file-content options)
- Error handling: timeouts, API errors
- Async: no blocking
- Return: EDN map with :claims list

Testing: Provide 3 test documents + expected outputs
```

**Task List:**
- [ ] Claude Code: Generate extraction pipeline
- [ ] ML Engineer: Test with real Claude API
- [ ] Backend Engineer: Review for composability
- [ ] ML Engineer: Adjust Claude prompt if needed

**Acceptance Criteria:**
- [ ] Can extract from test document
- [ ] Returns structured EDN
- [ ] No errors on API call
- [ ] Results are reasonable

**Daily Standup Output:**
```
Ship: Extraction pipeline (working function)
Owner: ML Engineer
Status: ✅ Documents → Claims works
```

---

### Thursday: Patch Builder

**Owner:** Backend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 6: Patch Building**
```
Build a patch construction pipeline.

Input: Extracted claims list
Output: 3-5 patches (time-distributed)

Algorithm:
1. Group claims by time (if timestamps differ)
2. If single timestamp, create patches at intervals:
   - Patch 1: First 40% of claims
   - Patch 2: First 60% of claims
   - Patch 3: First 80% of claims
   - Patch 4: All claims
3. For each patch:
   - Assign UUID
   - Set :patch/timestamp
   - Set :patch/facts (claims in this patch)
   - Build :patch/edges (relationships)
   - Assign confidence (average of claims)

Implementation:
- Clojure function: (build-patches-process claims options)
- Returns list of patches as EDN
- No side effects (pure function)

Testing: Use extracted claims from Wednesday, verify patches
```

**Task List:**
- [ ] Claude Code: Generate patch builder
- [ ] Backend Engineer: Review logic
- [ ] Backend Engineer: Test with extracted claims
- [ ] Backend Engineer: Verify patch structure

**Acceptance Criteria:**
- [ ] Can build patches from claims
- [ ] Patches have correct structure
- [ ] Timestamps are sensible
- [ ] Can serialize to JSON

**Daily Standup Output:**
```
Ship: Patch builder (working function)
Owner: Backend Engineer
Status: ✅ Claims → Patches works
Next: Wire up to frontend
```

---

### Friday: End-to-End Wiring

**Owner:** Frontend Engineer + Backend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 7: Upload → Patches → Visualization**
```
Wire up the end-to-end flow:
1. User uploads document (UI)
2. Frontend sends file to backend
3. Backend extracts claims
4. Backend builds patches
5. Backend returns patches to frontend
6. Frontend visualizes patches

Implementation:
1. Backend: HTTP endpoint
   POST /api/upload
   Body: { file: File }
   Response: { patches: [...] }

2. Frontend: Call endpoint on upload
   - Show loading spinner
   - Handle errors
   - Receive patches

3. Integration: Connect all pieces

Testing: Upload real document, see patches appear

Output:
- API endpoint (backend)
- API call (frontend)
- Integration test (document → patches → visible)
```

**Task List:**
- [ ] Claude Code: Generate HTTP endpoint
- [ ] Frontend Engineer: Generate API client
- [ ] Both: Wire up integration
- [ ] Both: Test end-to-end locally
- [ ] Both: Fix any issues

**Acceptance Criteria:**
- [ ] Can upload document
- [ ] Backend extracts claims
- [ ] Patches returned to frontend
- [ ] No errors

**Daily Standup Output:**
```
Ship: Working upload → extraction → patches flow
Owner: Frontend Engineer + Backend Engineer
Status: ✅ End-to-end flow works (local)
Milestone: Real data flows through system
```

---

## Week 4: Visualization + Interaction

### Monday: Visualize Patches

**Owner:** Frontend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 8: Patch Visualization Component**
```
Build a component to visualize patches as graphs.

Input: patches (array of patch objects)
Output: Rendered graph visualization

Requirements:
1. Visual:
   - Each patch is a "snapshot" on a timeline
   - Each snapshot shows concept clusters
   - Clusters are nodes in force-directed layout
   - Concepts are labeled
   - Confidence is shown by color (gray→gold)

2. Interaction:
   - Hover node → show related facts
   - Click node → show details
   - Zoom in → see underlying facts

3. Technology:
   - React component
   - D3.js or Three.js for layout
   - Canvas rendering for performance

4. Data mapping:
   - Patch → snapshot
   - Claim → node
   - Edge → connection
   - Confidence → color

Implementation:
- React component: <PatchGraph patches={patches} />
- Use D3 force layout
- Color scale: gray (0.0) → gold (1.0)
- Export to Storybook

Testing: Test with synthetic patches from Phase 0
```

**Task List:**
- [ ] Claude Code: Generate visualization component
- [ ] Frontend Engineer: Integrate D3/Three.js
- [ ] Frontend Engineer: Test with real patches
- [ ] Designer: Verify visual matches design system

**Acceptance Criteria:**
- [ ] Component renders without errors
- [ ] Visual matches design
- [ ] Nodes are labeled
- [ ] Colors represent confidence
- [ ] Smooth rendering (60fps target)

**Daily Standup Output:**
```
Ship: PatchGraph component (Storybook link)
Owner: Frontend Engineer
Status: ✅ Patches visualize beautifully
```

---

### Tuesday-Wednesday: Timeline Scrubber

**Owner:** Frontend Engineer + Claude Code  
**Time Budget:** 1.5 days

**Claude Code Prompt 9: Timeline Scrubber UI**
```
Build an interactive timeline scrubber.

Requirements:
1. Visual:
   - Horizontal slider showing time
   - Markers for each patch (5 snapshots)
   - Current position highlighted
   - Smooth animations

2. Behavior:
   - User drags slider
   - Graph morphs in real-time
   - Confidence scores update
   - New concepts fade in/out
   - Transitions are smooth (300ms)

3. Technical:
   - React component with state
   - Animation via Framer Motion or CSS
   - Responsive to keyboard (arrow keys)
   - Shows current time label

Implementation:
- Component: <TimelineScrubber />
- Manages: currentTime state
- Callback: onChange={(time) => updateVisualization(time)}
- Integrates with PatchGraph

Animation spec:
- On time change, graph transitions over 300ms
- Color change over 300ms
- Node positions update via force layout
```

**Task List:**
- [ ] Claude Code: Generate scrubber component
- [ ] Frontend Engineer: Integrate with visualization
- [ ] Frontend Engineer: Test interactions
- [ ] Designer: Verify animation timing

**Acceptance Criteria:**
- [ ] Can drag scrubber
- [ ] Graph updates in real-time
- [ ] Animations are smooth
- [ ] Keyboard controls work

**Daily Standup Output:**
```
Ship: TimelineScrubber component + integration
Owner: Frontend Engineer
Status: ✅ Users can scrub through understanding evolution
```

---

### Thursday: Polish & Performance

**Owner:** Frontend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 10: Performance Optimization**
```
Optimize React rendering for smooth 60fps interaction.

Profiling:
- Profile React component (use DevTools)
- Identify slow re-renders
- Check D3 layout performance
- Measure animation frame rate

Optimizations:
- Memoize components (React.memo)
- Use useMemo for expensive calculations
- Batch state updates
- Debounce scrubber input
- Use Canvas instead of SVG if needed

Testing:
- Measure before/after FPS
- Test on low-end hardware
- Verify smooth interaction

Output:
- Performance report (FPS before/after)
- Optimized component code
- Performance notes for future
```

**Task List:**
- [ ] Claude Code: Profile current performance
- [ ] Frontend Engineer: Implement optimizations
- [ ] Claude Code: Re-profile
- [ ] Frontend Engineer: Verify improvements

**Acceptance Criteria:**
- [ ] 60fps on standard hardware
- [ ] Scrubber is smooth (no jank)
- [ ] Animations are fluid

**Daily Standup Output:**
```
Ship: Optimized visualization (performance report)
Owner: Frontend Engineer
Status: ✅ PHASE 1 COMPLETE
```

---

### Friday: User Testing

**Owner:** PM + Frontend Engineer  
**Time Budget:** 1 day

**Task List:**
- [ ] PM: Recruit 3 beta users (researchers/teachers)
- [ ] Frontend Engineer: Set up testing environment
- [ ] PM: Conduct user tests (30 mins each)
  - Can you upload a document?
  - Can you scrub through the timeline?
  - Do you learn something?
- [ ] PM: Document feedback
- [ ] PM: Identify top 2 issues

**Acceptance Criteria:**
- [ ] 3 users complete upload → visualization flow
- [ ] All can scrub timeline
- [ ] 2+ users report learning something
- [ ] No major blockers identified

**Daily Standup Output:**
```
Ship: User testing report + feedback summary
Owner: PM
Status: ✅ PHASE 1 COMPLETE - Local proof works!
Milestone: Users can see their understanding evolve
```

---

# PHASE 2: CLAUDE VALIDATION (WEEKS 5-6)

## Goal
Prove Claude can generate realistic patch progressions. Build gallery of 10 pre-generated topics. Validate "magic" works with synthetic data.

## Week 5: Topic-Based Generation

### Monday-Tuesday: Claude Generation Spec

**Owner:** ML Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 11: Topic Generation Pipeline**
```
Build a pipeline to generate realistic patch progressions for a topic.

Input: Topic (string), years (2020-2025)
Output: 5 patches showing understanding evolution

Algorithm:
1. For each year in range:
   - Call Claude: "What would an expert understanding of [topic] have been in [year]?"
   - Parse response to claims
   - Assign confidence (0.5 in early years, 0.9 in recent)
2. Group claims into patches (one per year)
3. Add edges (implications between claims)
4. Return as patch list

Claude prompt template:
"It's [year]. You're an expert in [topic]. 
What's the current understanding of [topic]?
Format:
{
  \"summary\": \"...\",
  \"claims\": [
    {\"text\": \"...\", \"confidence\": 0.8}
  ]
}"

Implementation:
- Clojure function: (generate-topic-patches topic years-range)
- Async (calls Claude multiple times)
- Returns patches (one per year)
- No side effects (pure data)

Testing: Generate patches for 3 topics (AI scaling, eigenvectors, alignment)
```

**Task List:**
- [ ] Claude Code: Generate topic generation pipeline
- [ ] ML Engineer: Test with 3 topics
- [ ] ML Engineer: Validate patches are reasonable
- [ ] ML Engineer: Adjust Claude prompts if needed

**Acceptance Criteria:**
- [ ] Can generate patches for topic + year range
- [ ] Patches show realistic progression
- [ ] Confidence values are sensible
- [ ] No API errors

**Daily Standup Output:**
```
Ship: Topic generation pipeline (working function)
Owner: ML Engineer
Status: ✅ Can generate topic progressions
```

---

### Wednesday-Thursday: Authenticity Testing

**Owner:** ML Engineer + PM  
**Time Budget:** 1 day

**Claude Code Prompt 12: Authenticity Validation**
```
Validate that Claude-generated patches feel authentic.

Process:
1. Generate patches for 5 topics
2. Format as JSON (hide that they're Claude-generated)
3. Show to 10 domain experts
4. Ask: "Does this reflect real understanding progression?"
5. Collect ratings (1-10 scale)
6. Identify fake-sounding patches
7. Refine Claude prompts based on feedback

Metrics:
- Average rating (target: 7+)
- % patches rated "feels authentic" (target: 70%+)
- Common complaints (identify pattern)

Output:
- Authenticity report (ratings by topic)
- Refined Claude prompts (version 2)
- Recommendations (topics to focus on, topics to skip)
```

**Task List:**
- [ ] ML Engineer: Generate patches for 5 topics
- [ ] PM: Send to 10 reviewers
- [ ] PM: Collect feedback
- [ ] ML Engineer: Analyze results
- [ ] ML Engineer: Refine prompts if needed

**Acceptance Criteria:**
- [ ] 7+ average rating
- [ ] 70%+ rated as "authentic"
- [ ] Identified problematic topics
- [ ] Refined prompts created

**Daily Standup Output:**
```
Ship: Authenticity report + refined prompts
Owner: ML Engineer
Status: ✅ Claude patches validated as realistic
```

---

### Friday: Integration & First Gallery Build

**Owner:** Frontend Engineer + ML Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 13: Gallery Integration**
```
Integrate topic generation into frontend.

UI:
- Gallery page showing 10 pre-generated topics
- Each topic card shows:
  - Topic name
  - Brief description
  - Timeline preview (5 dots representing 5 years)
  - "Explore" button

Backend:
- Endpoint: GET /api/gallery
  Response: { topics: [ {...}, {...}, ... ] }
- Topics are pre-generated (not on-demand)
- Stored as JSON fixtures

Frontend:
- Gallery component renders topic cards
- Click card → navigate to visualization
- Visualization shows topic's patches
- Full scrubber experience

Implementation:
1. Generate 10 topics (offline)
2. Store as JSON fixtures
3. Frontend loads fixtures
4. User can browse + explore
5. Each topic shows understanding evolution

Topics to generate (for proof):
1. Machine learning scalability
2. Eigenvectors (linear algebra)
3. Alignment (AI safety)
4. Photosynthesis (biology)
5. Capitalism (economics)
6. Quantum computing
7. Roman history
8. Jazz music
9. Depression (mental health)
10. Climate change

All should work smoothly without Claude-generated patch generation being obvious
```

**Task List:**
- [ ] Claude Code: Generate 10 topic progressions
- [ ] Frontend Engineer: Build gallery component
- [ ] Frontend Engineer: Integrate with visualization
- [ ] Both: Test end-to-end (click topic → scrub timeline)

**Acceptance Criteria:**
- [ ] Gallery page renders 10 topics
- [ ] Can click topic → see visualization
- [ ] Timeline scrubber works
- [ ] No errors

**Daily Standup Output:**
```
Ship: Gallery page + 10 pre-generated topics
Owner: Frontend Engineer + ML Engineer
Status: ✅ PHASE 2 WEEK 1 COMPLETE
```

---

## Week 6: Gallery Polish & Validation

### Monday-Tuesday: Gallery Polish

**Owner:** Designer + Frontend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 14: Gallery UI Polish**
```
Polish the gallery for visual beauty.

Current state: Functional, not beautiful
Target state: Premium, delightful to browse

Improvements:
1. Visual:
   - Add hero section ("Explore understanding evolution")
   - Topic cards: better visuals, preview graphs
   - Timeline preview: show confidence progression
   - Hover effects (scale, shadow, underline)

2. Interaction:
   - Smooth page transitions
   - Loading states (skeleton screens)
   - Error states (if topic fails to load)

3. Copy:
   - Each topic has description
   - Enticing text ("Watch how we discovered...")
   - Call-to-action ("Explore this journey")

4. Responsive:
   - Mobile layout (stacked cards)
   - Tablet layout (2 columns)
   - Desktop layout (3-4 columns)

Implementation:
- Update TopicCard component
- Add hover animations
- Add responsive layout
- Improve visual hierarchy

Output: Premium-looking gallery
```

**Task List:**
- [ ] Designer: Design improvements
- [ ] Claude Code: Generate updated components
- [ ] Frontend Engineer: Integrate improvements
- [ ] Designer: Visual review

**Acceptance Criteria:**
- [ ] Gallery looks premium
- [ ] All topics visible on page
- [ ] Hover effects are smooth
- [ ] Responsive on mobile/tablet/desktop

**Daily Standup Output:**
```
Ship: Polished gallery page
Owner: Designer + Frontend Engineer
Status: ✅ Gallery is beautiful
```

---

### Wednesday-Thursday: Internal Demo

**Owner:** PM + Designer  
**Time Budget:** 0.5 day

**Task List:**
- [ ] PM: Schedule demo with leadership
- [ ] Designer: Prepare walkthrough
  - Intro: "Here's the problem we're solving"
  - Demo: "Watch me explore eigenvectors through history"
  - Scrub timeline, show evolution
  - Visit 2-3 more topics
- [ ] PM: Prep talking points
- [ ] Team: Practice demo (should be <10 mins)

**Acceptance Criteria:**
- [ ] Demo is smooth (no crashes)
- [ ] Leadership is impressed
- [ ] Gets green light to continue

**Daily Standup Output:**
```
Ship: Internal demo + exec approval
Owner: PM
Status: ✅ Leadership bought in
Milestone: Proof of concept is working
```

---

### Friday: Phase 2 Wrap-Up + Phase 3 Planning

**Owner:** PM + Team  
**Time Budget:** 1 day

**Task List:**
- [ ] PM: Create Phase 3 spec document
- [ ] Backend Engineer: Review Go CLI requirements
- [ ] ML Engineer: Review video ingestion requirements
- [ ] Frontend Engineer: Review performance needs for 100 videos
- [ ] Team: Identify risks for Phase 3
- [ ] PM: Create Phase 3 sprint plan

**Acceptance Criteria:**
- [ ] Phase 3 spec is clear
- [ ] Team confident in approach
- [ ] Risks are documented
- [ ] Sprint plan is agreed

**Daily Standup Output:**
```
Ship: Phase 3 spec + sprint plan
Owner: PM
Status: ✅ PHASE 2 COMPLETE - Gallery validates concept!
Milestone: Magic exists. Real data ingestion is next.
```

---

# PHASE 3: END-TO-END (WEEKS 7-8)

## Goal
Process one real YouTube channel end-to-end. Download videos, transcribe, extract, build patches, visualize. Prove production-quality pipeline.

## Week 7: Go CLI + Clojure Pipeline

### Monday: Go CLI Skeleton

**Owner:** Backend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 15: Go CLI for YouTube**
```
Build a Go CLI for downloading and transcribing YouTube videos.

Command: understand download --channel UC_id --date-range 2020-2025

Requirements:
1. Download videos from channel
2. Use yt-dlp (or youtube-dl)
3. Extract audio → MP3
4. Transcribe with Whisper
5. Output JSON with timestamps
6. Handle errors gracefully

Implementation:
1. Main CLI using cobra library
2. Command structure:
   - understand download (main command)
   - Flags: --channel, --date-range, --output
3. Workflow:
   - Fetch channel videos
   - Download each (with progress)
   - Transcribe each (batch or parallel)
   - Save JSON to output directory
4. Error handling:
   - Network errors → retry
   - Missing Whisper → helpful message
   - Quota limits → graceful degradation

Output format:
{
  "video_id": "abc123",
  "title": "...",
  "published_at": "2025-01-01",
  "duration": 3600,
  "transcript": [
    {"timestamp": 0, "text": "...", "duration": 5},
    ...
  ]
}

Progress: Show "Downloaded X/Y videos", "Transcribed X/Y videos"

Testing: Test with real YouTube channel (even if just 5 videos)
```

**Task List:**
- [ ] Claude Code: Generate Go CLI skeleton
- [ ] Backend Engineer: Review code
- [ ] Backend Engineer: Test with real YouTube channel
- [ ] Backend Engineer: Fix any issues

**Acceptance Criteria:**
- [ ] CLI builds without errors
- [ ] Can download 5 videos
- [ ] Transcription works
- [ ] JSON output is structured
- [ ] Progress is shown

**Daily Standup Output:**
```
Ship: Go CLI (working on local machine)
Owner: Backend Engineer
Status: ✅ Can download + transcribe YouTube videos
```

---

### Tuesday-Wednesday: Full Pipeline Orchestration

**Owner:** Backend Engineer + Claude Code  
**Time Budget:** 1.5 days

**Claude Code Prompt 16: End-to-End Pipeline**
```
Build full pipeline: transcripts → patches → stored.

Flow:
1. Read transcripts from Go CLI output
2. For each transcript:
   a. Extract claims (Claude)
   b. Embed claims (Nomic embeddings)
   c. Build patches (group by time)
   d. Store patches (Datomic or LocalStorage)
3. Output: Summary (X patches created, Y facts extracted)

Implementation:
- Clojure function: (process-channel-transcripts config)
- Reads JSON from directory
- Processes sequentially (or in parallel, if needed)
- Reports progress
- Returns count of patches created

Composition:
- Use existing functions:
  - extract-claims-process
  - embed-claims-process
  - build-patches-process
  - store-patches
- Combine them into orchestration

Error handling:
- Skip broken transcripts (log error, continue)
- Retry on API failure (with backoff)
- Graceful shutdown

Output:
- Clojure script that can be run:
  (process-channel-transcripts {:channel-id "UC..." :output-dir "/transcripts"})
- Reports: "Created 150 patches from 50 videos"

Testing: Process the 5 downloaded videos from Monday
```

**Task List:**
- [ ] Claude Code: Generate orchestration pipeline
- [ ] Backend Engineer: Review composition
- [ ] Backend Engineer: Test with downloaded videos
- [ ] Backend Engineer: Verify patches in storage
- [ ] Backend Engineer: Performance test

**Acceptance Criteria:**
- [ ] Can process all videos without errors
- [ ] Creates correct number of patches
- [ ] Patches are stored (can query them back)
- [ ] Performance is acceptable (<30s per video)

**Daily Standup Output:**
```
Ship: End-to-end pipeline (working function)
Owner: Backend Engineer
Status: ✅ Transcripts → Patches fully automated
```

---

### Thursday: Integration + Scale Test

**Owner:** Backend Engineer + Frontend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 17: Integration Test + Scale**
```
Integrate Go CLI + Clojure pipeline + Frontend visualization.

Test scenario:
1. Run Go CLI: Download 50 videos (or more)
2. Run Clojure pipeline: Extract 200+ patches
3. Query patches from frontend
4. Visualize in app
5. Scrub timeline
6. Verify performance

Requirements:
- CLI handles 50+ videos without errors
- Pipeline creates 200+ patches in <5 minutes
- Frontend loads 200 patches in <2 seconds
- Timeline scrubber is smooth (60fps)
- No memory leaks
- Graceful error handling

Output:
- Integration test script
- Performance report (timings for each stage)
- Recommendations (if scaling is needed)

Success criteria:
- All 50 videos processed
- All patches visualized
- No crashes
- Scrubber is responsive
```

**Task List:**
- [ ] Claude Code: Generate integration test
- [ ] Backend Engineer: Run pipeline
- [ ] Frontend Engineer: Load patches + visualize
- [ ] Team: Performance testing
- [ ] Team: Fix any issues

**Acceptance Criteria:**
- [ ] 50+ videos downloaded
- [ ] 200+ patches created
- [ ] Frontend loads smoothly
- [ ] No errors in processing

**Daily Standup Output:**
```
Ship: Integration test report + performance metrics
Owner: Backend Engineer + Frontend Engineer
Status: ✅ PHASE 3 WEEK 1 COMPLETE
```

---

### Friday: Real Channel Processing

**Owner:** Backend Engineer + PM  
**Time Budget:** 1 day

**Task List:**
- [ ] PM: Choose real YouTube channel (20-100 videos, 2-5 year span)
  - Suggestion: MIT OpenCourseWare, academic lecture series, etc.
- [ ] Backend Engineer: Run full pipeline
- [ ] Backend Engineer: Process all videos
- [ ] PM: Load visualization
- [ ] PM: Scrub through timeline
- [ ] PM: Verify evolution makes sense

**Acceptance Criteria:**
- [ ] All videos downloaded
- [ ] All patches created
- [ ] Visualization shows coherent understanding evolution
- [ ] No crashes

**Daily Standup Output:**
```
Ship: Processed real YouTube channel (ready for demo)
Owner: Backend Engineer
Status: ✅ End-to-end with real data works!
```

---

## Week 8: Performance Polish + Final Demo

### Monday-Tuesday: Performance Optimization

**Owner:** Frontend Engineer + Backend Engineer + Claude Code  
**Time Budget:** 1 day

**Claude Code Prompt 18: Performance Optimization at Scale**
```
Optimize system to handle 100+ videos, 5K+ facts smoothly.

Profiling areas:
1. Frontend:
   - Patch loading time (<2s target)
   - Graph rendering (60fps target)
   - Scrubber interaction (no jank)
   - Memory usage

2. Backend:
   - Transcript processing (<30s per video)
   - Patch creation (<30s per patch)
   - Storage queries (<500ms)

Optimizations:
1. Frontend:
   - Progressive rendering (show important facts first)
   - LOD system (zoom out = fewer nodes rendered)
   - Memoization (cache expensive computations)
   - Canvas rendering (if DOM is slow)

2. Backend:
   - Batch processing (Claude API batching)
   - Caching (don't re-extract same transcript)
   - Indexing (fast queries on large datasets)
   - Parallel processing (multi-threaded)

3. Data:
   - Compression (store as compressed JSON)
   - Pruning (remove duplicate edges)
   - Sampling (for visualization, use subset of facts)

Output:
- Performance report (before/after timings)
- Optimized code
- Deployment recommendations
```

**Task List:**
- [ ] Claude Code: Profile current performance
- [ ] Frontend Engineer: Implement frontend optimizations
- [ ] Backend Engineer: Implement backend optimizations
- [ ] Team: Re-profile + verify improvements

**Acceptance Criteria:**
- [ ] Patch loading <2s
- [ ] Graph rendering 60fps
- [ ] No memory leaks
- [ ] Scrubber is smooth

**Daily Standup Output:**
```
Ship: Optimized system (performance report)
Owner: Frontend Engineer + Backend Engineer
Status: ✅ System handles 5K+ facts smoothly
```

---

### Wednesday-Thursday: Polish & Bug Fixes

**Owner:** Frontend Engineer + Backend Engineer + Designer  
**Time Budget:** 1 day

**Claude Code Prompt 19: Bug Hunt + Polish**
```
Comprehensive testing to find and fix bugs.

Test areas:
1. Upload flow:
   - Different file types (PDF, MD, TXT, DOCX)
   - Large files (>10MB)
   - Invalid files (not a document)
   - Network interruption mid-upload

2. Extraction:
   - Empty documents
   - Non-English text
   - Documents with images
   - Very long documents

3. Visualization:
   - 10 facts
   - 100 facts
   - 1000 facts
   - Empty patches
   - Single fact

4. Interaction:
   - Rapid scrubber movement
   - Zoom in/out
   - Click on different elements
   - Window resize

Output:
- Bug report (severity levels)
- Fixes for critical/high bugs
- Notes for low priority (backlog)

Testing methodology:
- Automated tests (unit + integration)
- Manual testing (try to break it)
- Edge case testing (unusual inputs)
```

**Task List:**
- [ ] Claude Code: Generate comprehensive test suite
- [ ] Team: Run tests
- [ ] Team: Fix identified bugs
- [ ] Designer: Visual review (any polish needed?)

**Acceptance Criteria:**
- [ ] No crashes on any test
- [ ] Handles edge cases gracefully
- [ ] Error messages are helpful
- [ ] Visual is polished

**Daily Standup Output:**
```
Ship: Bug-free system + comprehensive tests
Owner: Frontend Engineer + Backend Engineer
Status: ✅ System is production-ready
```

---

### Friday: Grand Demo + Retrospective

**Owner:** PM + Team  
**Time Budget:** 1 day

**Task List:**
- [ ] PM: Schedule demo with stakeholders + potential users
- [ ] Designer: Prepare clean demo environment
- [ ] PM: Create demo script:
  1. Intro: "This is how understanding evolves"
  2. Demo: Scrub through real channel's evolution
  3. Show gallery with 10 topics
  4. Upload document demo
  5. Q&A
- [ ] Team: Do final walkthrough
- [ ] PM: Conduct demo (15-20 minutes)

Post-Demo:
- [ ] Team: Gather feedback
- [ ] PM: Conduct retrospective
  - What went well?
  - What was hard?
  - What's next?
- [ ] PM: Create roadmap for Phase 4+

**Acceptance Criteria:**
- [ ] Demo is smooth (no crashes)
- [ ] Users are impressed
- [ ] Feedback is collected
- [ ] Next steps are clear

**Daily Standup Output:**
```
Ship: Grand demo + user feedback
Owner: PM + Team
Status: ✅ PHASE 3 COMPLETE - PROOF OF CONCEPT SUCCESSFUL!

Milestone: We shipped a working system in 8 weeks.
Users see understanding evolve. Magic exists.

Next: Decide on Phase 4 (productionize, scale, or pivot)
```

---

# APPENDIX: CLAUDE CODE INTEGRATION PATTERNS

## How Claude Code Works in This Plan

### Pattern 1: Spec → Code Generation

**You provide:**
```
[Task spec]
[Architecture constraints]
[Input/output examples]
```

**Claude Code:**
1. Generates complete implementation
2. Includes tests
3. Provides implementation notes

**You review:**
- Does it match the spec?
- Is it maintainable?
- Any security issues?

**Action:** Merge to main, move to next task

### Pattern 2: REPL-Driven Development (Clojure)

**You ask Claude Code:**
```
"Load these functions into REPL and test with sample data"
```

**Claude Code:**
1. Loads Clojure project
2. Starts REPL
3. Tests function with examples
4. Reports results

**Output:** "Extracted 42 claims from test document, average confidence 0.82"

### Pattern 3: Integration Testing

**You ask Claude Code:**
```
"Run end-to-end test: upload document → extract → visualize"
```

**Claude Code:**
1. Starts backend + frontend
2. Uploads test document
3. Runs through complete flow
4. Reports success/failure
5. Captures screenshots/video

**Output:** Integration test report with before/after screenshots

### Pattern 4: Performance Profiling

**You ask Claude Code:**
```
"Profile the visualization component with 1000 facts"
```

**Claude Code:**
1. Instruments code
2. Runs with 1000 facts
3. Measures memory, CPU, FPS
4. Identifies bottlenecks
5. Reports findings

**Output:** "Graph rendering: 45ms. Scrubber interaction: 16ms per frame. Memory: 142MB"

## Key Prompting Principles

### 1. Be Specific

**Bad:** "Build the visualization"

**Good:** "Build a React component that takes a list of patches and renders them as a force-directed graph using D3. Each node should be color-coded by confidence (gray=0, gold=1). Nodes should be draggable. Clicking a node should show underlying facts."

### 2. Provide Context

**Every prompt should include:**
```
Architecture doc section: [link/reference]
Data structure spec: [schema]
Success criteria: [specific, measurable]
Testing approach: [how to verify it works]
```

### 3. Show Examples

**Include:**
- Input data sample
- Expected output sample
- Edge cases
- Error handling expectations

### 4. Request Atomic Units

**Not:** "Build the entire app"

**But:** "Build the UploadZone component"
Then: "Build the extraction pipeline"
Then: "Wire them together"

### 5. Always Include Tests

**Prompt Claude Code to generate:**
- Unit tests (each function)
- Integration tests (pieces together)
- Manual testing instructions
- Acceptance criteria

## Sample Claude Code Workflow

### Day 1 Monday Morning:
```
claude code generate-scaffold --phase phase-1 --architecture rich-hickey

↓ (Claude Code creates initial codebase)

claude code run tests --target clojure

↓ (Claude Code runs tests, all pass)

Engineer reviews code, approves

↓ (Code merges to main)

First artifact ships
```

### Day 2 Tuesday Morning:
```
claude code build-component --component UploadZone --design-from figma-link

↓ (Claude Code generates React component)

claude code build-test --component UploadZone --test-scenarios upload-pdf,upload-md,upload-large,upload-invalid

↓ (Claude Code generates comprehensive tests)

Engineer reviews, adjusts UX, approves

↓ (Code ships)

Component in Storybook by Tuesday noon
```

### Day 3 Wednesday Morning:
```
claude code build-process --process extract-claims --using claude-api --test-with /test-data/

↓ (Claude Code generates extraction function + tests)

claude code repl-test --function extract-claims --inputs /test-data/

↓ (Claude Code shows results in REPL)

Engineer verifies results are good, approves

↓ (Code ships)

Extraction pipeline works by Wednesday noon
```

---

# EXECUTION CHECKLIST

## Pre-Launch (Before Week 1)

- [ ] Repository created (GitHub)
- [ ] Team access granted
- [ ] Claude Code configured
- [ ] Project board created (8-week sprint)
- [ ] Design files (Figma) created
- [ ] Stakeholder communication sent
- [ ] Daily standup time set
- [ ] First Claude Code prompts written

## Weekly Cadence

**Monday Morning:**
- [ ] Sprint planning (1hr)
- [ ] Assign tasks
- [ ] Identify blockers
- [ ] Run first Claude Code prompt

**Daily (5 mins):**
- [ ] Standup (what shipped, what's next, blockers)
- [ ] Claude Code artifacts reviewed
- [ ] Any merges to main

**Friday Afternoon:**
- [ ] Sprint retro (30 mins)
- [ ] Demo (to PM / stakeholders)
- [ ] Plan next week
- [ ] Document learnings

## Gate Criteria

### Week 4 Go/No-Go
- [ ] 3 beta users complete upload → visualization
- [ ] Internal feedback is positive (8/10 YES)
- [ ] Timeline scrubber is smooth
- [ ] Decision: Proceed to Phase 2 or Pivot

### Week 8 Go/No-Go
- [ ] Real YouTube channel processed end-to-end
- [ ] Visualization works smoothly with 5K+ facts
- [ ] One user has genuine insight moment
- [ ] Demo impresses stakeholders
- [ ] Decision: Extend to Phase 4, launch MVP, or pivot

---

# Key Success Factors

1. **Ruthless Prioritization**
   - Every task has a "ship" moment
   - Anything not shippable in a day gets split smaller
   - Nice-to-haves are marked "Phase 4+"

2. **Daily Shipping**
   - Code merges to main every day
   - Something new on screen every single day
   - Users see progress

3. **Claude Code as Developer**
   - Not a tool you use, a teammate you partner with
   - Give it good specs, it delivers
   - Review its work daily
   - Trust it with 90% of implementation

4. **Paul Graham's Bias**
   - Done is better than perfect
   - Get users feedback ASAP
   - Iterate based on real usage
   - Speed of learning > Speed of shipping (but ship to learn)

5. **Stay Focused**
   - One phase at a time
   - No half-building two things
   - Weekly gates ensure accountability
   - Big picture never gets lost

---

*This plan is designed to ship. Every day has a deliverable. Every week has a milestone. Claude Code does the code, humans make the decisions. Go fast, stay focused, ship constantly.*
