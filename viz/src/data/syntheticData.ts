/**
 * Synthetic Data Generator for VKM Graph
 *
 * Creates example patches showing knowledge evolution over time.
 * This demonstrates the "understanding journey" with realistic progressions.
 */

import { Patch, Fact, Edge, TimelineSnapshot, GraphData, GraphNode, GraphLink } from '@/types';

// ============================================================
// Synthetic Patch Generation
// ============================================================

function createFact(
  id: string,
  text: string,
  confidence: number,
  topic: string,
  validFrom: string
): Fact {
  return {
    'db/id': id,
    'claim/text': text,
    'claim/confidence': confidence,
    'claim/topic': topic,
    'claim/valid-from': validFrom,
    'claim/lod': 0,
  };
}

function createEdge(
  id: string,
  from: string,
  to: string,
  relation: 'supports' | 'contradicts' | 'revises',
  strength: number
): Edge {
  return {
    'db/id': id,
    'edge/from': from,
    'edge/to': to,
    'edge/relation': relation,
    'edge/strength': strength,
  };
}

// ============================================================
// Patch 1: Early Understanding (October 2020)
// ============================================================

export const patch1: Patch = {
  'db/id': 'patch-2020-10',
  'patch/timestamp': '2020-10-01T00:00:00Z',
  'patch/source': 'manual',
  'patch/source-id': 'synthetic-ai-scaling',
  'patch/facts': [
    createFact(
      'fact-1-1',
      'Large models learn better than small models',
      0.75,
      'scaling',
      '2020-10-01T00:00:00Z'
    ),
    createFact(
      'fact-1-2',
      'Model capacity increases with parameters',
      0.70,
      'scaling',
      '2020-10-01T00:00:00Z'
    ),
    createFact(
      'fact-1-3',
      'Training requires significant compute resources',
      0.80,
      'compute',
      '2020-10-01T00:00:00Z'
    ),
  ],
  'patch/edges': [
    createEdge('edge-1-1', 'fact-1-1', 'fact-1-2', 'supports', 0.7),
  ],
  'patch/metadata': {
    'synthetic': true,
    'topic': 'AI Scaling',
    'description': 'Early understanding - parameters matter',
  },
};

// ============================================================
// Patch 2: Growing Understanding (April 2021)
// ============================================================

export const patch2: Patch = {
  'db/id': 'patch-2021-04',
  'patch/timestamp': '2021-04-01T00:00:00Z',
  'patch/source': 'manual',
  'patch/source-id': 'synthetic-ai-scaling',
  'patch/facts': [
    createFact(
      'fact-2-1',
      'Large models learn better than small models',
      0.85, // Increased confidence
      'scaling',
      '2021-04-01T00:00:00Z'
    ),
    createFact(
      'fact-2-2',
      'Model capacity increases with parameters',
      0.80, // Increased confidence
      'scaling',
      '2021-04-01T00:00:00Z'
    ),
    createFact(
      'fact-2-3',
      'Training requires significant compute resources',
      0.85, // Increased confidence
      'compute',
      '2021-04-01T00:00:00Z'
    ),
    createFact(
      'fact-2-4',
      'Data quality matters as much as quantity',
      0.70,
      'data',
      '2021-04-01T00:00:00Z'
    ),
    createFact(
      'fact-2-5',
      'Scaling follows predictable power laws',
      0.75,
      'scaling',
      '2021-04-01T00:00:00Z'
    ),
  ],
  'patch/edges': [
    createEdge('edge-2-1', 'fact-2-1', 'fact-2-2', 'supports', 0.8),
    createEdge('edge-2-2', 'fact-2-2', 'fact-2-5', 'supports', 0.7),
    createEdge('edge-2-3', 'fact-2-4', 'fact-2-1', 'supports', 0.6),
  ],
  'patch/metadata': {
    'synthetic': true,
    'topic': 'AI Scaling',
    'description': 'Growing understanding - data quality emerges',
  },
};

// ============================================================
// Patch 3: Deepening Insights (November 2022)
// ============================================================

export const patch3: Patch = {
  'db/id': 'patch-2022-11',
  'patch/timestamp': '2022-11-01T00:00:00Z',
  'patch/source': 'manual',
  'patch/source-id': 'synthetic-ai-scaling',
  'patch/facts': [
    createFact(
      'fact-3-1',
      'Large models learn better than small models',
      0.90,
      'scaling',
      '2022-11-01T00:00:00Z'
    ),
    createFact(
      'fact-3-2',
      'Scaling depends on parameters, data, and optimization',
      0.85,
      'scaling',
      '2022-11-01T00:00:00Z'
    ),
    createFact(
      'fact-3-3',
      'Data quality matters as much as quantity',
      0.85,
      'data',
      '2022-11-01T00:00:00Z'
    ),
    createFact(
      'fact-3-4',
      'Scaling follows predictable power laws',
      0.88,
      'scaling',
      '2022-11-01T00:00:00Z'
    ),
    createFact(
      'fact-3-5',
      'Emergent capabilities appear at specific scales',
      0.80,
      'emergence',
      '2022-11-01T00:00:00Z'
    ),
    createFact(
      'fact-3-6',
      'Optimization regimes affect scaling efficiency',
      0.75,
      'optimization',
      '2022-11-01T00:00:00Z'
    ),
  ],
  'patch/edges': [
    createEdge('edge-3-1', 'fact-3-1', 'fact-3-2', 'supports', 0.9),
    createEdge('edge-3-2', 'fact-3-2', 'fact-3-4', 'supports', 0.8),
    createEdge('edge-3-3', 'fact-3-3', 'fact-3-2', 'supports', 0.7),
    createEdge('edge-3-4', 'fact-3-4', 'fact-3-5', 'supports', 0.75),
    createEdge('edge-3-5', 'fact-3-6', 'fact-3-2', 'supports', 0.6),
  ],
  'patch/metadata': {
    'synthetic': true,
    'topic': 'AI Scaling',
    'description': 'Deepening - emergence and optimization understood',
  },
};

// ============================================================
// Patch 4: Mature Understanding (June 2024)
// ============================================================

export const patch4: Patch = {
  'db/id': 'patch-2024-06',
  'patch/timestamp': '2024-06-01T00:00:00Z',
  'patch/source': 'manual',
  'patch/source-id': 'synthetic-ai-scaling',
  'patch/facts': [
    createFact(
      'fact-4-1',
      'Scaling is a multi-dimensional phenomenon',
      0.92,
      'scaling',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-2',
      'Parameters, data, and compute are partially equivalent',
      0.88,
      'scaling',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-3',
      'Data quality matters more than raw quantity',
      0.92,
      'data',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-4',
      'Scaling laws are predictable within regimes',
      0.93,
      'scaling',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-5',
      'Emergent capabilities appear at predictable scales',
      0.87,
      'emergence',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-6',
      'Optimization algorithms significantly affect efficiency',
      0.85,
      'optimization',
      '2024-06-01T00:00:00Z'
    ),
    createFact(
      'fact-4-7',
      'Transfer learning reduces effective compute requirements',
      0.83,
      'efficiency',
      '2024-06-01T00:00:00Z'
    ),
  ],
  'patch/edges': [
    createEdge('edge-4-1', 'fact-4-1', 'fact-4-2', 'supports', 0.9),
    createEdge('edge-4-2', 'fact-4-2', 'fact-4-4', 'supports', 0.85),
    createEdge('edge-4-3', 'fact-4-3', 'fact-4-1', 'supports', 0.8),
    createEdge('edge-4-4', 'fact-4-4', 'fact-4-5', 'supports', 0.82),
    createEdge('edge-4-5', 'fact-4-6', 'fact-4-2', 'supports', 0.75),
    createEdge('edge-4-6', 'fact-4-7', 'fact-4-6', 'supports', 0.7),
    createEdge('edge-4-7', 'fact-4-5', 'fact-4-1', 'supports', 0.8),
  ],
  'patch/metadata': {
    'synthetic': true,
    'topic': 'AI Scaling',
    'description': 'Mature - multidimensional understanding',
  },
};

// ============================================================
// Patch 5: Current Understanding (October 2025)
// ============================================================

export const patch5: Patch = {
  'db/id': 'patch-2025-10',
  'patch/timestamp': '2025-10-01T00:00:00Z',
  'patch/source': 'manual',
  'patch/source-id': 'synthetic-ai-scaling',
  'patch/facts': [
    createFact(
      'fact-5-1',
      'Scaling is fundamentally about information efficiency',
      0.94,
      'scaling',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-2',
      'Parameters, data, and compute form a unified framework',
      0.91,
      'scaling',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-3',
      'Data curation is more important than scale',
      0.95,
      'data',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-4',
      'Scaling laws predict performance across regimes',
      0.96,
      'scaling',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-5',
      'Emergent capabilities are phase transitions',
      0.89,
      'emergence',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-6',
      'Optimization and architecture co-evolve',
      0.88,
      'optimization',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-7',
      'Transfer and distillation enable efficient scaling',
      0.90,
      'efficiency',
      '2025-10-01T00:00:00Z'
    ),
    createFact(
      'fact-5-8',
      'Inference efficiency matters as much as training',
      0.87,
      'efficiency',
      '2025-10-01T00:00:00Z'
    ),
  ],
  'patch/edges': [
    createEdge('edge-5-1', 'fact-5-1', 'fact-5-2', 'supports', 0.92),
    createEdge('edge-5-2', 'fact-5-2', 'fact-5-4', 'supports', 0.9),
    createEdge('edge-5-3', 'fact-5-3', 'fact-5-1', 'supports', 0.85),
    createEdge('edge-5-4', 'fact-5-4', 'fact-5-5', 'supports', 0.88),
    createEdge('edge-5-5', 'fact-5-6', 'fact-5-2', 'supports', 0.8),
    createEdge('edge-5-6', 'fact-5-7', 'fact-5-6', 'supports', 0.75),
    createEdge('edge-5-7', 'fact-5-8', 'fact-5-7', 'supports', 0.82),
    createEdge('edge-5-8', 'fact-5-5', 'fact-5-1', 'supports', 0.85),
  ],
  'patch/metadata': {
    'synthetic': true,
    'topic': 'AI Scaling',
    'description': 'Current - unified theoretical framework',
  },
};

// ============================================================
// All Patches Collection
// ============================================================

export const syntheticPatches: Patch[] = [
  patch1,
  patch2,
  patch3,
  patch4,
  patch5,
];

// ============================================================
// Helper Functions: Convert Patches to Graph Data
// ============================================================

function patchToGraphData(patch: Patch): GraphData {
  const nodes: GraphNode[] = patch['patch/facts'].map(fact => ({
    id: fact['db/id'],
    label: fact['claim/text'],
    confidence: fact['claim/confidence'],
    topic: fact['claim/topic'],
  }));

  const links: GraphLink[] = patch['patch/edges'].map(edge => ({
    source: edge['edge/from'],
    target: edge['edge/to'],
    relation: edge['edge/relation'],
    strength: edge['edge/strength'],
  }));

  return { nodes, links };
}

export function createTimelineSnapshots(patches?: Patch[]): TimelineSnapshot[] {
  const patchesToProcess = patches || syntheticPatches;

  return patchesToProcess.map((patch, index) => {
    const graph = patchToGraphData(patch);
    const facts = patch['patch/facts'];
    const avgConfidence =
      facts.reduce((sum, f) => sum + f['claim/confidence'], 0) / facts.length;

    const topics: Record<string, number> = {};
    facts.forEach(fact => {
      const topic = fact['claim/topic'] || 'unknown';
      topics[topic] = (topics[topic] || 0) + 1;
    });

    const date = new Date(patch['patch/timestamp']);
    const label = date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
    });

    return {
      timestamp: patch['patch/timestamp'],
      label,
      patchId: patch['db/id'],
      graph,
      stats: {
        numFacts: facts.length,
        avgConfidence,
        topics,
      },
    };
  });
}

// ============================================================
// Export Default Data
// ============================================================

export const syntheticTimeline = createTimelineSnapshots();
