/**
 * API Client for VKM Graph Backend
 *
 * Communicates with the Clojure HTTP API server.
 */

import { Patch } from '@/types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000';

// ============================================================
// API Response Types
// ============================================================

interface ApiResponse<T> {
  data?: T;
  error?: string;
}

interface UploadResponse {
  'patch-id': string;
  'facts-count': number;
  message: string;
}

interface PatchesResponse {
  patches: Patch[];
  count: number;
}

interface StatsResponse {
  patches: number;
  facts: number;
  edges: number;
  morphisms: number;
  motives: number;
}

// ============================================================
// Helper Functions
// ============================================================

async function fetchJson<T>(
  url: string,
  options?: RequestInit
): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      const errorData = await response.json();
      return { error: errorData.error || 'Request failed' };
    }

    const data = await response.json();
    return { data };
  } catch (error) {
    console.error('API request failed:', error);
    return { error: error instanceof Error ? error.message : 'Network error' };
  }
}

// ============================================================
// API Methods
// ============================================================

/**
 * Health check
 */
export async function checkHealth(): Promise<ApiResponse<{ status: string }>> {
  return fetchJson('/health');
}

/**
 * Get database statistics
 */
export async function getStats(): Promise<ApiResponse<StatsResponse>> {
  return fetchJson('/api/stats');
}

/**
 * Upload a document for processing
 */
export async function uploadDocument(
  content: string,
  filename: string
): Promise<ApiResponse<UploadResponse>> {
  return fetchJson('/api/upload', {
    method: 'POST',
    body: JSON.stringify({
      content,
      filename,
    }),
  });
}

/**
 * Get all patches for a source
 */
export async function getPatches(
  sourceId?: string
): Promise<ApiResponse<PatchesResponse>> {
  const url = sourceId
    ? `/api/patches?source-id=${encodeURIComponent(sourceId)}`
    : '/api/patches';
  return fetchJson(url);
}

/**
 * Get a specific patch by ID
 */
export async function getPatch(patchId: string): Promise<ApiResponse<Patch>> {
  return fetchJson(`/api/patches/${patchId}`);
}

/**
 * Query patches with filters
 */
export async function queryPatches(filters: {
  topic?: string;
  'from-date'?: string;
  'to-date'?: string;
  'min-confidence'?: number;
}): Promise<ApiResponse<{ facts: any[]; count: number }>> {
  return fetchJson('/api/patches/query', {
    method: 'POST',
    body: JSON.stringify(filters),
  });
}

/**
 * Process transcripts directory
 */
export async function processTranscripts(
  sourceId: string,
  transcriptsDir: string
): Promise<ApiResponse<{ 'patch-id': string; 'motives-count': number }>> {
  return fetchJson('/api/process', {
    method: 'POST',
    body: JSON.stringify({
      'source-id': sourceId,
      'transcripts-dir': transcriptsDir,
    }),
  });
}

// ============================================================
// Export API Object
// ============================================================

export const api = {
  checkHealth,
  getStats,
  uploadDocument,
  getPatches,
  getPatch,
  queryPatches,
  processTranscripts,
};

export default api;
