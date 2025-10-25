# Running the VKM Graph Visualization

## Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Open browser to http://localhost:5173
```

## What You'll See

The visualization shows how understanding of "AI Scaling" evolved from 2020 to 2025:

### **The Graph**
- **Nodes** represent facts/claims
- **Colors** indicate confidence (gray = low, gold = high)
- **Borders** show topic categories
- **Lines** show relationships between facts

### **The Timeline**
- **Scrub** left-right to travel through time
- **Click** markers to jump to specific snapshots
- **Keyboard** arrows to navigate step-by-step

### **Interactions**
- **Drag** nodes to rearrange the graph
- **Hover** over nodes to see full text
- **Click** nodes to see details
- **Zoom** with scroll wheel

## The Journey

### October 2020 - Early Understanding
- 3 facts
- Basic insight: "large models learn better"
- Focus on parameters

### April 2021 - Growing
- 5 facts
- New insight: "data quality matters"
- Scaling laws emerge

### November 2022 - Deepening
- 6 facts
- New insight: "emergent capabilities"
- Optimization regimes understood

### June 2024 - Maturing
- 7 facts
- New insight: "parameters, data, compute are equivalent"
- Transfer learning recognized

### October 2025 - Current
- 8 facts
- Unified framework: "information efficiency"
- Phase transitions understood

## Architecture

### Data Flow
```
syntheticData.ts → Timeline → App → PatchGraph → D3 Visualization
```

### Components

**PatchGraph** (`components/PatchGraph.tsx`)
- Force-directed graph with D3
- GPU-accelerated (via canvas)
- Interactive drag & zoom

**Timeline** (`components/Timeline.tsx`)
- Horizontal scrubber
- Smooth transitions
- Keyboard navigation

**App** (`App.tsx`)
- State management
- Component integration
- Event handling

### Synthetic Data

See `src/data/syntheticData.ts` for the 5 patches showing evolution.

Each patch contains:
- Facts with confidence scores
- Edges showing relationships
- Metadata (topic, description)

## Customization

### Change Colors

Edit `src/config.ts`:

```typescript
export const visualizationConfig = {
  colors: {
    confidence: {
      low: '#YOUR_COLOR',    // Low confidence
      medium: '#YOUR_COLOR', // Medium
      high: '#YOUR_COLOR',   // High confidence
    },
  },
};
```

### Change Physics

Edit `src/config.ts`:

```typescript
export const visualizationConfig = {
  physics: {
    chargeStrength: -300,  // Node repulsion
    linkDistance: 100,     // Edge length
    alphaDecay: 0.02,      // Simulation cooling
  },
};
```

### Add Your Own Data

Create a new file `src/data/yourData.ts`:

```typescript
import { Patch } from '@/types';

export const yourPatches: Patch[] = [
  {
    'db/id': 'patch-1',
    'patch/timestamp': '2020-01-01T00:00:00Z',
    'patch/source': 'manual',
    'patch/facts': [
      {
        'db/id': 'fact-1',
        'claim/text': 'Your claim here',
        'claim/confidence': 0.8,
        'claim/topic': 'your-topic',
        'claim/valid-from': '2020-01-01T00:00:00Z',
      },
    ],
    'patch/edges': [],
  },
  // ... more patches
];
```

Then import in `App.tsx`:

```typescript
import { yourPatches } from '@/data/yourData';
import { createTimelineSnapshots } from '@/data/syntheticData';

const yourTimeline = createTimelineSnapshots(yourPatches);
```

## Performance

### Current Performance
- **60fps** on standard hardware
- **Smooth animations** at 500ms
- **Instant load** with synthetic data

### If You Experience Slowness

**Option 1: Reduce nodes**
```typescript
// In syntheticData.ts, limit facts per patch
const facts = allFacts.slice(0, 10); // Max 10 facts
```

**Option 2: Disable animations**
```typescript
// In config.ts
animation: {
  duration: 0, // Instant
}
```

**Option 3: Use Canvas instead of SVG**
```typescript
// In PatchGraph.tsx, switch to canvas rendering
// (requires refactoring, but much faster for 100+ nodes)
```

## Deployment

### Build for Production

```bash
npm run build
```

Output will be in `dist/` directory.

### Deploy to Vercel

```bash
npm install -g vercel
vercel
```

### Deploy to Netlify

```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

### Deploy to GitHub Pages

```bash
npm run build
git subtree push --prefix viz/dist origin gh-pages
```

## Troubleshooting

### "Module not found" errors

```bash
rm -rf node_modules package-lock.json
npm install
```

### Graph doesn't appear

Check browser console for errors. Make sure D3 is installed:

```bash
npm install d3 @types/d3
```

### Slow performance

1. Reduce number of nodes
2. Disable animations
3. Switch to canvas rendering

### TypeScript errors

```bash
npm run build
```

This will show specific type errors to fix.

## Next Steps

1. **Connect to Real Data**
   - Replace `syntheticData.ts` with API calls
   - Load patches from Clojure backend
   - Real-time updates via WebSocket

2. **Add More Interactions**
   - Search/filter facts
   - Highlight paths between nodes
   - Export to PNG/SVG

3. **Advanced Visualizations**
   - 3D graph with Three.js
   - Temporal animation (auto-play)
   - Motive clustering view

4. **Production Features**
   - User authentication
   - Save/share visualizations
   - Collaborative annotations

## Resources

- [D3.js Documentation](https://d3js.org/)
- [React Docs](https://react.dev/)
- [Vite Guide](https://vitejs.dev/guide/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

## Support

For questions or issues:
- GitHub Issues: [vkm-graph/issues](https://github.com/epistemicSystems/vkm-graph/issues)
- Email: research@epistemicsystems.org

---

**Built with:** React, TypeScript, D3.js, Vite
**Philosophy:** Beauty First, Information-Driven Architecture
**Inspiration:** Rich Hickey, Paul Graham, Bret Victor
