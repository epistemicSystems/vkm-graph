# VKM Graph Visualization

Interactive visualization layer for the Knowledge Graph Evolution System.

## Overview

This visualization allows you to:
- **Explore** the moduli stack of knowledge patches
- **Navigate** through temporal evolution
- **Discover** semantic motives and their relationships
- **Analyze** how understanding evolved

## Features

### Moduli Stack View

Visualize patches as points in the moduli stack:
- **X-axis**: Time
- **Y-axis**: Information level
- **Color**: Confidence or topic
- **Size**: Number of facts

### Motive Graph

Force-directed graph of semantic motives:
- **Nodes**: Motives (concept clusters)
- **Edges**: Semantic relationships
- **Labels**: Concept words
- **Zoom**: Hierarchical detail

### Timeline Scrubber

Interactive timeline showing knowledge evolution:
- **Scrub** through time to see patches evolve
- **Transitions** show morphisms (reorganization vs. growth)
- **Playback** to watch understanding develop

## Getting Started

### Prerequisites

- Node.js 20+
- Data exported from the Clojure pipeline

### Installation

```bash
npm install
```

### Development

```bash
# Start development server
npm run dev
```

Visit `http://localhost:5173`

### Build

```bash
# Production build
npm run build

# Preview production build
npm run preview
```

## Usage

### Load Data

The visualization loads data from the Clojure pipeline export:

```bash
# In the core directory, export data
cd ../core
clj -M:run export --output ../viz/public/data.edn
```

Then in the visualization:

```typescript
import { loadData } from './utils/dataLoader';

const data = await loadData('/data.edn');
```

### Components

#### ModuliStack

```tsx
import { ModuliStack } from './components/ModuliStack';

<ModuliStack
  patches={patches}
  morphisms={morphisms}
  colorBy="confidence"
  sizeBy="time-extent"
/>
```

#### MotiveGraph

```tsx
import { MotiveGraph } from './components/MotiveGraph';

<MotiveGraph
  motives={motives}
  motiveGraph={motiveGraph}
  showRelationships={true}
/>
```

#### Timeline

```tsx
import { Timeline } from './components/Timeline';

<Timeline
  patches={patches}
  onTimeChange={(time) => setCurrentTime(time)}
/>
```

## Architecture

```
viz/
├── src/
│   ├── components/       # React components
│   │   ├── ModuliStack.tsx
│   │   ├── MotiveGraph.tsx
│   │   └── Timeline.tsx
│   ├── hooks/            # Custom React hooks
│   ├── utils/            # Utilities
│   └── App.tsx           # Main application
├── public/               # Static assets
└── package.json
```

## Configuration

Edit visualization parameters in `src/config.ts`:

```typescript
export const config = {
  layout: {
    type: 'force-directed',
    dimensions: 2,
    chargeStrength: -300,
    linkDistance: 100,
  },

  colors: {
    confidence: {
      low: '#ff6b6b',
      medium: '#ffd93d',
      high: '#6bcf7f',
    },
    topic: {
      scaling: '#4ecdc4',
      data: '#ff6b6b',
      optimization: '#ffe66d',
    },
  },

  animation: {
    duration: 500,
    easing: 'ease-in-out',
  },
};
```

## Development

### Hot Reload

Vite provides instant hot module replacement:

```bash
npm run dev
```

### Type Checking

```bash
# Check types
npm run build  # TypeScript compilation is part of build
```

### Linting

```bash
npm run lint
```

## Deployment

### Static Export

```bash
npm run build
```

Output is in `dist/` directory. Deploy to any static hosting:

- Vercel
- Netlify
- GitHub Pages
- AWS S3

### Docker

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "preview"]
```

## Roadmap

- [ ] 3D moduli stack visualization
- [ ] GPU-accelerated rendering with use.GPU
- [ ] Real-time updates via WebSocket
- [ ] Export to video/animation
- [ ] Collaborative annotation
- [ ] VR/AR support

## References

- [D3.js Documentation](https://d3js.org/)
- [Three.js Documentation](https://threejs.org/)
- [React Three Fiber](https://docs.pmnd.rs/react-three-fiber/)
- [Force-Directed Graphs](https://en.wikipedia.org/wiki/Force-directed_graph_drawing)

## License

MIT
