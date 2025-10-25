/**
 * TypeScript types for the VKM Graph Knowledge Evolution System
 *
 * These types mirror the EDN schemas defined in core/resources/schema/patch.edn
 */

// ============================================================
// Core Data Types
// ============================================================

export interface Fact {
  'db/id': string;
  'claim/text': string;
  'claim/confidence': number; // 0-1
  'claim/topic'?: string;
  'claim/valid-from': string; // ISO timestamp
  'claim/extracted-from'?: string;
  'claim/timestamp-in-video'?: number;
  'claim/revises'?: string;
  'claim/tags'?: string[];
  'claim/lod'?: number; // 0-3
}

export interface Edge {
  'db/id': string;
  'edge/from': string;
  'edge/to': string;
  'edge/relation': EdgeRelation;
  'edge/strength': number; // 0-1
}

export type EdgeRelation =
  | 'supports'
  | 'contradicts'
  | 'revises'
  | 'refines'
  | 'generalizes'
  | 'specializes'
  | 'causes'
  | 'correlates';

export interface Embedding {
  'embedding-id': string;
  'claim-ref': string;
  model: string;
  vector: number[];
}

export interface Patch {
  'db/id': string;
  'patch/timestamp': string; // ISO timestamp
  'patch/source': PatchSource;
  'patch/source-id'?: string;
  'patch/facts': Fact[];
  'patch/edges': Edge[];
  'patch/embeddings'?: Embedding[];
  'patch/metadata'?: Record<string, unknown>;
}

export type PatchSource =
  | 'youtube-channel'
  | 'git-history'
  | 'document'
  | 'manual';

export interface Morphism {
  'db/id': string;
  'morphism/from': string;
  'morphism/to': string;
  'morphism/type': MorphismType;
  'morphism/timestamp': string; // ISO timestamp
  'morphism/author'?: string;
  'morphism/reason'?: string;
  'morphism/information-gain': number; // 0-1
  'morphism/operations'?: Operation[];
  'morphism/delta'?: {
    'facts-added': number;
    'facts-removed': number;
    'edges-added': number;
    'edges-removed': number;
  };
}

export type MorphismType =
  | 'additive'
  | 'refinement'
  | 'reorganization'
  | 'refutation';

export interface Operation {
  op: 'add-fact' | 'remove-fact' | 'update-confidence' | 'add-edge' | 'remove-edge';
  'fact-id'?: string;
  'edge-id'?: string;
  old?: number;
  new?: number;
  fact?: Fact;
  edge?: Edge;
}

export interface Motive {
  id: string;
  'concept-words': string[];
  centroid: number[];
  confidence: number; // 0-1
  'cluster-size': number;
  'member-claim-ids': string[];
}

export interface MotiveMorphism {
  from: string;
  to: string;
  relation: 'generalizes' | 'specializes' | 'analogous' | 'contrasts' | 'enables';
  strength: number; // 0-1
}

// ============================================================
// Visualization Data Types
// ============================================================

export interface GraphNode {
  id: string;
  label: string;
  confidence: number;
  topic?: string;
  x?: number;
  y?: number;
  vx?: number;
  vy?: number;
}

export interface GraphLink {
  source: string | GraphNode;
  target: string | GraphNode;
  relation: EdgeRelation;
  strength: number;
}

export interface GraphData {
  nodes: GraphNode[];
  links: GraphLink[];
}

export interface TimelineSnapshot {
  timestamp: string;
  label: string;
  patchId: string;
  graph: GraphData;
  stats: {
    numFacts: number;
    avgConfidence: number;
    topics: Record<string, number>;
  };
}

// ============================================================
// Configuration Types
// ============================================================

export interface VisualizationConfig {
  layout: {
    type: 'force-directed' | 'hierarchical' | 'circular';
    dimensions: 2 | 3;
    chargeStrength: number;
    linkDistance: number;
  };
  colors: {
    confidence: {
      low: string;
      medium: string;
      high: string;
    };
    topic: Record<string, string>;
  };
  animation: {
    duration: number;
    easing: string;
  };
  physics: {
    chargeStrength: number;
    linkDistance: number;
    alphaDecay: number;
  };
}

export interface AppState {
  patches: Patch[];
  currentPatchIndex: number;
  currentPatch: Patch | null;
  timeline: TimelineSnapshot[];
  isLoading: boolean;
  error: string | null;
}

// ============================================================
// Component Props Types
// ============================================================

export interface PatchGraphProps {
  patch: Patch;
  config: VisualizationConfig;
  onNodeClick?: (nodeId: string) => void;
  onNodeHover?: (nodeId: string | null) => void;
}

export interface TimelineProps {
  snapshots: TimelineSnapshot[];
  currentIndex: number;
  onChange: (index: number) => void;
}

export interface UploadZoneProps {
  onUpload: (files: File[]) => void;
  maxFiles?: number;
  acceptedFormats?: string[];
}
