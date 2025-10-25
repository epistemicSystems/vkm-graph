# Product Requirements Document
## The Knowledge Graph Evolution System

**Version:** 1.0  
**Date:** October 24, 2025  
**Status:** Proof of Concept (8-week program)  
**Owner:** Product Management  
**Stakeholders:** Design, Engineering, ML/Research, Executive Leadership

---

## Executive Summary

**The Knowledge Graph Evolution System** (working name: *Understand*) is a proof-of-concept product that visualizes how human understanding evolves over time. 

**Core Thesis:** Users don't just want to *know* facts—they want to *see themselves learning*. By recording the trajectory of how understanding shifts, clarifies, and deepens, we create a new category of tool: a *proof assistant for knowledge*.

**The Opportunity:** $50B+ education/research market lacks a product that makes the understanding journey visible. Researchers have commit histories, transcripts, and notes, but no system connects them into a coherent narrative of intellectual growth.

**This Program:** 8-week proof of concept that demonstrates the core magic. If successful, becomes the foundation for a $1B+ category.

**Success Definition:** By week 8, one real user should have an "aha moment"—an experience where they see how their thinking evolved and say, "I didn't realize I'd grown that much."

---

## Part 1: Problem Statement

### The Invisible Journey

When a researcher publishes a paper or a teacher releases a course, what's visible is the *destination*: polished facts, clean arguments, resolved conclusions.

What's invisible is the *journey*: the confusion, the false starts, the moment of breakthrough, the gradual accumulation of understanding.

**Example:** A researcher studies "AI scaling" for 5 years.

- **October 2020:** "Scaling = model size"
- **November 2021:** "Actually, it's model size + data"
- **March 2023:** "Wait, optimization regime matters too"
- **September 2025:** "These might be partially equivalent under certain regimes"

This researcher **learned deeply**. But that learning is invisible. A student reading the final position ("scaling depends on parameters, data, and regime") has no context. A colleague working on scaling independently loses years of discoveries because they can't see the decision tree.

### Current Tools Are Fragmented

- **Git/GitHub:** Records technical changes, not intellectual changes
- **YouTube:** Records raw footage, not structured knowledge
- **Google Docs/Notion:** Records final drafts, not evolution
- **Academic papers:** Record conclusions, not reasoning
- **Twitter/Discussion:** Records moments, not patterns

**No tool connects these into a coherent narrative of intellectual growth.**

### Who Suffers?

**Researchers:** Can't preserve institutional memory of how their field evolved. Each generation re-discovers the same insights.

**Students:** See polished final answers. Don't see how to *think*—only what to think.

**Teachers:** Want to show "real math" (messy, iterative) but can only show "textbook math" (clean, final).

**Organizations:** Lose epistemic continuity when team members leave. Discoveries aren't encoded in a way that new members can learn from.

---

## Part 2: Vision & Guiding Principles

### Vision Statement

> *We believe every person should be able to see their own intellectual growth. By making the trajectory of understanding visible, we transform learning from a private struggle into a shared, observable, beautiful process.*

### Three Guiding Principles

**1. Design-First (Not Data-First)**
The visualization and interaction are the entire product. Everything else—data ingestion, processing, storage—serves the experience.

**Why:** Most knowledge tools fail because they optimize for data collection. We optimize for insight. The visualization will teach us what data we need.

**2. Beauty as a Feature**
The system must be genuinely delightful to use. Users should *want* to see how they've learned, the way they want to look at old photos.

**Why:** In education/research, tools are often utilitarian and ugly. Beauty signals that this is different. It signals that understanding matters.

**3. Proof Over Scale**
In this phase, we care about proving the magic works for ONE person with ONE concept over months/years. We don't care about 1000 videos or 100K users.

**Why:** The insight is fragile. It only works if the experience is pristine. Better to have 5 people deeply moved than 500 people mildly interested.

---

## Part 3: User Personas & Use Cases

### Primary Persona: The Learning Researcher

**Name:** Dr. Sarah Chen  
**Age:** 35  
**Context:** PhD in computer science, researching "emergent capabilities in large language models," 5 years into research.

**Goals:**
- See how her understanding of emergence evolved over 5 years
- Share that journey with collaborators
- Use it to mentor students on how research actually works

**Pain Points:**
- Her insights are scattered across papers, GitHub, notebooks, conversations
- No way to show the narrative arc ("here's how I went from confused to confident")
- Hard to onboard new researchers on why she believes what she believes

**Use Case:** Sarah has 50 papers/experiments over 5 years on emergence. She uploads them. She sees a timeline showing how her position shifted. She has a moment: "Oh, I *did* discover something fundamental back in 2022, I just didn't realize it." She shares this with a student. Student says: "Now I understand how you think."

### Secondary Persona: The Teacher

**Name:** Prof. James Rodriguez  
**Age:** 42  
**Context:** Teaching "Linear Algebra" for 15 years, wants to show students the real history.

**Goals:**
- Show how the concept of "eigenvectors" emerged historically
- Make mathematics feel alive, not handed down from on high
- Connect abstract math to human discovery

**Pain Points:**
- Has 10 years of notes on how he teaches/thinks about linear algebra
- Can't make the evolution visible in a classroom
- Students see final formalism, not the conceptual journey

**Use Case:** James uploads his lecture notes from years 1, 5, 10, 15. System shows how his *explanation* of eigenvectors shifted. He plays this in class. Students see: "Oh, even the teacher didn't always explain it this way. It's evolving."

### Tertiary Persona: The Organization

**Name:** Anthropic Research Teams  
**Age:** Varies  
**Context:** Conducting research on alignment, capabilities, interpretability—domains where deep understanding matters.

**Goals:**
- Preserve how the team's collective understanding evolved
- Make research intuitions legible to new team members
- Reduce re-discovery and accelerate onboarding

**Pain Points:**
- Institutional knowledge lives in peoples' heads and Slack
- When researchers leave, insights leave with them
- New hires don't know "why" decisions were made

**Use Case:** Team uploads 2 years of research artifacts. System shows motive evolution: "We used to think alignment was about [X]. Then we learned [Y]. Now we're exploring [Z]." New hire watches this, understands the intellectual journey, gets up to speed 3x faster.

---

## Part 4: Solution Overview

### Core Experience (Phase 0-1)

User opens the system and sees:

```
╔════════════════════════════════════════════════════════════════╗
║                                                                ║
║     How my understanding of "Scaling" evolved                 ║
║                                                                ║
║     Oct 2020 ──→ Nov 2021 ──→ Mar 2023 ──→ Sep 2025          ║
║        [1]        [2]         [3]          [4]                ║
║                                                                ║
║  [Scrubber shows: 20% → 45% → 72% → 89% confidence]         ║
║                                                                ║
║  ┌──────────────────────────────────────────────────────────┐ ║
║  │  Oct 2020                                                │ ║
║  │  ────────────────────────────────────────────────────── │ ║
║  │  Scaling = Parameters increase → Models learn better    │ ║
║  │                                                          │ ║
║  │  Concept Cluster:                                       │ ║
║  │     • parameters (high confidence)                      │ ║
║  │     • learning (high confidence)                        │ ║
║  │     • capacity (medium)                                 │ ║
║  │     • data (low)                                        │ ║
║  │     • efficiency (low)                                  │ ║
║  │                                                          │ ║
║  │  [Source: Scaling Laws for Neural Language Models]     │ ║
║  └──────────────────────────────────────────────────────────┘ ║
║                                                                ║
║  ← Prev                                              Next →   ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
```

**The Magic Interaction:**

User scrubs the timeline left-right. As they do:
- Concept clusters *transform* in real-time
- New concepts fade in or out
- Confidence scores update
- Underlying beliefs shift

**The Insight Moment:**

User sees clearly: "I was wrong before. Then I learned this. Now I know that." Not as an abstract fact, but as a *visual narrative*.

### Architectural Philosophy

**Layers (Bottom to Top):**

1. **Data Layer:** Immutable patches (EDN) stored in Datomic
2. **Query Layer:** Clojure-based transformations
3. **Aggregation Layer:** Semantic motives (initially hand-curated)
4. **Visualization Layer:** use.GPU + React for beautiful rendering
5. **Interaction Layer:** Timeline scrubber, zoom, query

**Key Design Choices:**

- **Start with 2D graphs** (not 3D). Force-directed layout is simpler and more legible.
- **No 3D until 3D is essential.** Many teams spend 80% of time on 3D rendering when 2D would work.
- **Synthetic data first.** Build visualization with hand-crafted data. Prove magic works. Then build ingestion.
- **Manual motive curation initially.** Don't try to learn motives from data. Hand-label them. You'll learn what motives should be.
- **Pre-compute everything.** No real-time ingestion initially. Videos are processed offline, patches built offline, visualization served static-ish.

---

## Part 5: Detailed Requirements by Phase

### Phase 0: Beautiful First (Weeks 1-2)

**Objective:** Build a gorgeous visualization with synthetic data. Prove the concept resonates internally.

**Deliverables:**

1. **Clickable Prototype (Figma/Framer)**
   - Timeline with 5 snapshots
   - Each snapshot shows concept cluster + underlying beliefs
   - Scrubber shows smooth transition between snapshots
   - No code, just interaction design

2. **Visual Design System**
   - Color palette for confidence (gray → gold)
   - Typography for beliefs vs. metadata
   - Spacing/layout that feels "premium"
   - Animation curves for transitions

3. **Storyboard (5 scenes)**
   - User opens system
   - User sees timeline
   - User scrubs through evolution
   - User sees a transition
   - User has insight ("I didn't know I'd learned that")

4. **Internal Feedback Loop**
   - Show to 10 internal people
   - Measure: "Would you want to use this if data was real?" Target: 8/10 YES
   - Iterate on design based on feedback

**Team:** 1 PM + 1 Designer  
**Resources:** Figma, Framer, design tools  
**Success Metric:** Internal consensus that visualization is beautiful and compelling

---

### Phase 1: Local Proof (Weeks 3-4)

**Objective:** Build working visualization with real user data (documents). Prove data ingestion + visualization pipeline works.

**Deliverables:**

1. **Document Upload Interface**
   - Users can drag-drop 5 documents (Google Docs links, markdown, PDFs)
   - System fetches versions/history (if available)
   - UI shows: "Preparing to extract timeline..."

2. **Patch Building**
   - For each document, extract key claims/concepts using Claude
   - Build 3-5 "patches" representing understanding at different times
   - Store as EDN (immutable)
   - Store metadata: timestamp, source document, extraction confidence

3. **Visualization (React + D3/Three.js)**
   - Parse patches from storage
   - Render timeline with 3-5 snapshots
   - Force-directed graph of concept clusters
   - Interactive scrubber (drag to change time)
   - Concept labels, confidence indicators

4. **Interaction Patterns**
   - Hover over concept → see related claims
   - Click transition → see "what changed?"
   - Zoom in → see underlying facts
   - Share button → generate shareable link

5. **Local Storage**
   - No backend initially—everything local (browser storage + session)
   - Users can export as JSON
   - Users can save visualization as PNG

**Team:** 1 Engineer (React/frontend) + 1 ML person (Claude integration)  
**Technology Stack:**
- React + TypeScript (frontend)
- Claude API (fact extraction)
- D3.js or Three.js (visualization)
- LocalStorage (persistence)

**Success Metrics:**
- 3 beta users upload real documents
- Each user completes: upload → visualization → interact with timeline
- Survey: "Did you learn something about your own thinking?" Target: 2/3 YES

---

### Phase 2: Claude-Generated Patches (Weeks 5-6)

**Objective:** Prove that Claude can generate realistic patch progressions for a topic.

**Deliverables:**

1. **Topic-Based Generation**
   - User enters topic: "machine learning scalability"
   - User selects time range: 2018-2024
   - System calls Claude 5 times: "What would an expert understanding of [topic] have been in [year]?"
   - Claude returns structured claims + confidence
   - System builds patches from responses

2. **Authenticity Testing**
   - Show 10 internal people: 5 patches + concept evolution
   - Don't tell them it's Claude-generated
   - Ask: "Does this feel like real intellectual progression?"
   - Target: 7/10 say YES

3. **Refinement**
   - If patches feel fake, refine Claude prompts
   - Add metadata: "reasonable confidence levels" vs. "too confident"
   - Test edge cases (domains where progress is rapid vs. slow)

4. **Gallery**
   - Build 5-10 example topics with pre-generated patches
   - Users can browse: "Evolution of understanding in AI," "History of eigenvectors," etc.
   - Gallery serves as proof that it works across domains

**Team:** 1 ML person + 1 Engineer  
**Technology:** Claude API, prompt engineering, validation framework

**Success Metrics:**
- 7/10 internal users rate patches as "authentic"
- Gallery of 10 topics is compelling and diverse
- Can handle edge cases (rapidly evolving vs. stable domains)

---

### Phase 3: Single-Source Proof (Weeks 7-8)

**Objective:** Prove end-to-end pipeline works with real source material.

**Deliverables:**

1. **Go CLI: Download + Transcribe**
   - Command: `understand download --channel UC_id --date-range 2020-2025`
   - Downloads videos from channel
   - Runs Whisper transcription
   - Outputs JSON with timestamps
   - Logs progress

2. **Clojure Patch Builder**
   - Ingests transcripts
   - Calls Claude to extract facts at intervals (e.g., every 5 minutes)
   - Builds patches representing "understanding at this point"
   - Computes information gain between patches
   - Outputs EDN patches

3. **End-to-End Test: One Real Channel**
   - Pick a YouTube channel with 30-50 videos over 2-3 years
   - Ingest all videos
   - Build 20-30 patches
   - Visualize evolution
   - User should be able to say: "I can see how this channel's understanding evolved"

4. **Performance Requirements**
   - Ingestion: <5 minutes per video
   - Patch building: <30 seconds per patch
   - Visualization load: <2 seconds
   - Interaction (scrubber): <16ms latency (60fps)

5. **Error Handling**
   - Graceful degradation if transcription fails
   - Can resume interrupted ingestions
   - Clear error messages for users

**Team:** 1 Backend engineer (Go) + 1 ML person (Claude) + 1 Frontend engineer (visualization)  
**Technology Stack:**
- Go (CLI)
- Clojure (processing)
- Whisper (transcription)
- Claude API (extraction)
- React/Three.js (visualization)

**Success Metrics:**
- End-to-end pipeline runs without errors
- Produces 20+ coherent patches from real source
- One real user can navigate and understand evolution
- Performance targets met (5min/video, 30s/patch, 2s load, 16ms interaction)

---

### Phase 4: Generalize (Weeks 9-12, optional extension)

**Objective:** Prove system scales to multiple sources and can handle 5K+ facts.

**Deliverables:**

1. **Multi-Channel Support**
   - Can ingest from multiple YouTube channels simultaneously
   - Can merge/compare understanding across channels
   - Can filter by topic

2. **Datomic Integration**
   - Move from local storage to Datomic
   - Enable time-travel queries ("What did we believe on Oct 15?")
   - Enable fact tracking across versions

3. **Motive Extraction (Manual→Automated)**
   - Instead of hand-labeling motives, cluster facts automatically
   - Extract concept words from semantic intersections
   - Build motive graph

4. **Performance at Scale**
   - 100+ videos
   - 5K+ facts
   - <5 second visualization load
   - Smooth interaction with massive graphs

5. **Web Deployment**
   - Move from local to web-deployed
   - Users can create accounts, save visualizations
   - Shareable links

**Note:** Phase 4 is contingent on Phase 0-3 success. If magic emerges by week 8, we extend. If not, we pivot.

---

## Part 6: Success Metrics & Milestones

### Phase-by-Phase Milestones

| Phase | Week | Milestone | Success Criteria |
|-------|------|-----------|------------------|
| 0 | 1-2 | Beautiful design complete | 8/10 internal YES on "want to use" |
| 1 | 3-4 | Working prototype with real data | 2/3 users learned from visualization |
| 2 | 5-6 | Claude-generated patches validated | 7/10 find patches authentic |
| 3 | 7-8 | End-to-end with real source | 1 user has "aha moment" |
| 4 | 9-12 | Scale to 100 videos, 5K facts | System handles scale smoothly |

### North Star Metrics

**Primary (Most Important):**
- User Insight Rate: % of users who have a genuine insight from using the system
- Magic Moment: Users spontaneously saying "I didn't realize I'd learned that"

**Secondary:**
- Visualization Load Time: <2 seconds (proof of performance)
- Interaction Smoothness: 60fps on standard hardware
- Data Coherence: % of patches that feel authentic to domain experts

**Tertiary:**
- User Retention: Do users come back after first use?
- Share Rate: Do users share visualizations?
- Sentiment: NPS score (if applicable)

### Gating Criteria

**Go/No-Go at Week 4:**
- Does internal feedback suggest "this is beautiful"?
- Can we successfully extract patches from real documents?
- Do 3 beta users complete the flow without friction?

**If any answer is NO:** Pivot on interaction design or data extraction approach.

**Go/No-Go at Week 8:**
- Did one real user have a genuine insight?
- Can we process a real YouTube source end-to-end?
- Is visualization performance acceptable?

**If any answer is NO:** Re-evaluate product direction. Might pivot to simpler use case (e.g., document-only, no video).

---

## Part 7: Timeline & Resource Plan

### 8-Week Timeline

```
Week 1-2:  Design + Prototype (2 FTE)
           PM + Designer
           Output: Beautiful clickable prototype

Week 3-4:  Local Proof (2.5 FTE)
           Engineer + ML person + part-time designer
           Output: Working visualization with real documents

Week 5-6:  Claude Validation (2 FTE)
           ML person + Engineer
           Output: 10-topic gallery with Claude-generated patches

Week 7-8:  End-to-End (3 FTE)
           Backend engineer + ML person + Frontend engineer
           Output: One real channel ingested, visualized, tested

Week 9-12: Scale (Optional, contingent on success)
           Full team + 1 additional engineer
           Output: Multi-source support, Datomic integration
```

### Staffing

**Committed (Required for Phases 0-3):**
- 1 Product Manager (0.5 FTE—approx 20 hrs/week)
- 1 Designer (1 FTE)
- 1 Frontend Engineer (1 FTE)
- 1 Backend Engineer (0.5 FTE after week 2)
- 1 ML/Research Engineer (1 FTE)

**Total: ~3.5 FTE for 8 weeks**

**Optional (Phase 4):**
- 1 Additional Backend Engineer (0.5 FTE)
- 1 DevOps/Infrastructure (0.25 FTE)

### Budget Estimate (8-week program)

| Category | Cost |
|----------|------|
| Personnel (3.5 FTE × 8 weeks) | $70K-100K |
| Infrastructure (Datomic, storage, compute) | $5K-10K |
| Third-party APIs (Claude, Whisper, embeddings) | $3K-5K |
| Design tools, software licenses | $1K-2K |
| Contingency (20%) | $15K-22K |
| **Total** | **$94K-139K** |

---

## Part 8: Risks & Mitigation

### Risk 1: Magic Doesn't Emerge (High Impact, High Probability)

**Risk:** Users see visualizations but don't have "aha moments." The concept doesn't resonate.

**Mitigation:**
- Weekly internal demos starting week 2
- Pivot quickly to different interaction patterns if needed
- Have backup use case ready: "If visualization doesn't work, shift to conversation-based proof"

**Contingency:** By week 4, if internal feedback is lukewarm, pause and redesign UX.

---

### Risk 2: Data Extraction Is Lossy (High Impact, Medium Probability)

**Risk:** Claude/Whisper extraction loses important context. Patches feel inauthentic.

**Mitigation:**
- Phase 2 (Claude validation) happens before Phase 3 (real sources)
- Test on diverse domains (tech, academia, arts)
- Have domain experts validate patch authenticity
- Be willing to add human-in-the-loop (users can edit patches)

**Contingency:** If patches consistently feel fake, shift to semi-manual approach where users help curate.

---

### Risk 3: Performance at Scale (Medium Impact, Medium Probability)

**Risk:** System bogs down with 5K+ facts. Visualization becomes sluggish.

**Mitigation:**
- Benchmark early (week 4)
- Use GPU acceleration (use.GPU) from the start
- Implement progressive rendering (show important facts first)
- Consider LOD system (zoom out to see summary, zoom in to see details)

**Contingency:** If performance is bad, reduce scope to 1K facts initially. Scale later.

---

### Risk 4: Scope Creep (Medium Impact, High Probability)

**Risk:** Team wants to add motives, topoi, symbolic regression, etc. Program gets derailed.

**Mitigation:**
- Weekly scope reviews
- Clear "Phase 4+" label for anything beyond core
- PM maintains ruthless prioritization

**Contingency:** If scope creep happens, pause program to reset.

---

### Risk 5: User Adoption (Medium Impact, Medium Probability)

**Risk:** Even if magic exists, users don't know how to use it or don't want to upload documents.

**Mitigation:**
- Start with internal beta (Anthropic researchers)
- Build upload experience to be frictionless
- Provide pre-loaded examples (gallery)
- Invest in onboarding UX

**Contingency:** If adoption is low, pivot to embedded use (integrate into existing tools rather than standalone product).

---

## Part 9: Success Looks Like

### Week 2 Success
- Beautiful prototype that makes people say "I want to see this with real data"

### Week 4 Success
- 3 real users successfully upload documents
- System extracts patches and visualizes evolution
- Users navigate timeline without confusion
- Feedback: "This is cool, show me more"

### Week 6 Success
- Claude-generated patch gallery exists
- Domain experts say patches feel authentic
- Visualization is noticeably beautiful
- Team is excited about the direction

### Week 8 Success (The Real North Star)
- One researcher uses system with their own work
- They have a moment: "I didn't realize I'd grown that much"
- They show a colleague
- Colleague says: "I want to try this with my work"

**That moment—genuine insight, desire to share, willingness to try—is proof.**

---

## Part 10: What Happens After Week 8

### If Success (Insight Moments Happen)
- **Decision:** Invest in Phase 4 (scale, multi-source, Datomic)
- **Timeline:** 4 more weeks to productionize
- **Vision:** "Understand" becomes usable tool for researchers
- **Next:** Public beta with 50-100 users

### If Partial Success (Magic Works, But Scoped)
- **Decision:** Pick one specific use case (e.g., teachers only, or researchers only)
- **Timeline:** 2 weeks to focus scope
- **Vision:** Build the best-in-class product for that use case first
- **Next:** Own that niche before expanding

### If No Success (Magic Doesn't Emerge)
- **Decision:** Pivot or sunset
- **Options:**
  - Shift to conversation-based learning (AI co-pilot that shows your thinking evolve)
  - Shift to document collaboration (focus on collaborative writing + understanding)
  - Archive learnings and try different direction
- **Timeline:** 1 week to decide, then new program

---

## Appendix A: Technology Stack (Final)

### Frontend
- **React + TypeScript**
- **use.GPU** (GPU-accelerated visualization)
- **D3.js or Three.js** (graph rendering)
- **Framer Motion** (animations)
- **TailwindCSS** (styling)

### Backend
- **Go** (CLI for download/transcribe)
- **Clojure** (data processing, pipeline orchestration)
- **Datomic** (knowledge storage, temporal queries)
- **Git** (version control for patches)

### AI/ML
- **Claude Sonnet 4.5** (fact extraction)
- **OpenAI Whisper** (transcription)
- **OpenAI/Nomic embeddings** (semantic understanding)

### Infrastructure
- **Docker** (containerization)
- **GitHub** (source control)
- **AWS/GCP** (compute, storage)

---

## Appendix B: Sample Data Flow

```
User uploads documents
    ↓
Clojure pipeline reads documents
    ↓
Extracts claims using Claude
    → claim: "scaling = parameters"
    → confidence: 0.85
    → timestamp: Oct 2020
    ↓
Builds patch EDN
    {
      :db/id "patch-1"
      :patch/timestamp oct-2020
      :patch/claims [claim1 claim2 ...]
      :patch/edges [edge1 edge2 ...]
    }
    ↓
Stores in Datomic / LocalStorage
    ↓
Frontend queries for patches
    ↓
use.GPU renders visualization
    ↓
User scrubs timeline
    ↓
Graph morphs in real-time
    ↓
User sees insight
```

---

## Appendix C: Interaction Flowchart

```
START
  ↓
[Beautiful Intro]
  "How did your understanding evolve?"
  ↓
[Three Options]
├─→ Upload Documents
├─→ Explore Topic (Claude-generated)
└─→ Browse Gallery

IF Upload Documents:
  Upload files → Extract patches → Visualize → Success

IF Explore Topic:
  Enter topic → Claude generates patches → Visualize → Success

IF Browse Gallery:
  Choose example (e.g., "Eigenvectors") → Visualize → Inspire

[Core Interaction Loop]
  Timeline scrubber ←→ Graph transformation
  Click concept ←→ See claims
  Click transition ←→ See what changed
  Share button ←→ Generate link

[End State]
  User has insight → User wants to share → Virtuous cycle
```

---

## Appendix D: Key Definitions

**Patch:** An immutable snapshot of understanding at a point in time. Contains claims, relationships, metadata.

**Morphism:** A transition from one patch to another. Represents how understanding evolved.

**Fact:** A single claim or assertion (e.g., "models with more parameters learn better").

**Confidence:** A score (0-1) indicating how certain the understanding is. Evolves over time.

**Concept Cluster:** A group of related facts that point to a common underlying concept (e.g., "scaling").

**Motive:** The essential concept that a cluster is about. Initially hand-labeled; later extracted automatically.

**Information Gain:** A measure of how much genuine understanding advanced (vs. mere reorganization).

---

## Appendix E: Success Storytelling

### Sarah's Story (Expected Outcome)

1. **Before:** Sarah has 50 papers about emergence. She's proud of her work but can't articulate her growth trajectory.

2. **Week 4:** Sarah uploads her papers to Understand. System processes them.

3. **Week 8:** Sarah opens visualization. She scrubs timeline.

4. **The Moment:** She sees her confidence in "neural scaling ≈ emergent capability" increase from 20% (2020) to 85% (2025). She sees related concepts fade in and out. She sees the exact moment (2022) when she discovered something new.

5. **The Insight:** "I didn't consciously realize how much I'd learned. But seeing it like this... I can tell I really did discover something fundamental."

6. **The Sharing:** Sarah shows her PhD student. Student says: "Now I understand how you think about emergence. I want to build on this."

7. **The Win:** Student feels connected to Sarah's intellectual journey. Onboarding time cut from 6 months to 2 months.

**This is success.**

---

## Document Information

**PRD Version:** 1.0  
**Last Updated:** October 24, 2025  
**Next Review:** Week 2 (design review)  
**Owner:** Product Management  
**Approval:** (Pending stakeholder sign-off)

---

*This PRD is a living document. Updates will reflect learnings from each phase. Any significant changes will bump version number and require re-review.*
