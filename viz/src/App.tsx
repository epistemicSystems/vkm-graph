/**
 * VKM Graph - Main Application
 *
 * The Knowledge Graph Evolution System visualization
 * Demonstrates how understanding evolves over time
 */

import React, { useState, useEffect, useRef, useMemo } from 'react';
import { PatchGraph } from '@/components/PatchGraph';
import { Timeline } from '@/components/Timeline';
import { UploadZone } from '@/components/UploadZone';
import { SearchPanel, SearchFilters } from '@/components/SearchPanel';
import { ExportPanel } from '@/components/ExportPanel';
import { FactDetailPanel } from '@/components/FactDetailPanel';
import { syntheticTimeline, createTimelineSnapshots } from '@/data/syntheticData';
import { visualizationConfig } from '@/config';
import { getPatches, checkHealth } from '@/api/client';
import { Patch, Fact, GraphData } from '@/types';
import './App.css';

type DataSource = 'synthetic' | 'backend';

function App() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);
  const [dataSource, setDataSource] = useState<DataSource>('synthetic');
  const [backendPatches, setBackendPatches] = useState<Patch[]>([]);
  const [isLoadingPatches, setIsLoadingPatches] = useState(false);
  const [backendStatus, setBackendStatus] = useState<'checking' | 'online' | 'offline'>('checking');
  const [uploadCount, setUploadCount] = useState(0);
  const [selectedFact, setSelectedFact] = useState<Fact | null>(null);
  const [showExportPanel, setShowExportPanel] = useState(false);
  const [searchFilters, setSearchFilters] = useState<SearchFilters>({
    textQuery: '',
    minConfidence: 0,
    maxConfidence: 1,
    topics: [],
  });

  const svgRef = useRef<SVGSVGElement>(null);

  // Check backend health on mount
  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await checkHealth();
        if (response.data?.status === 'ok') {
          setBackendStatus('online');
        } else {
          setBackendStatus('offline');
        }
      } catch (error) {
        setBackendStatus('offline');
      }
    };
    checkBackend();
  }, []);

  // Load patches from backend when switching to backend mode
  useEffect(() => {
    if (dataSource === 'backend') {
      loadBackendPatches();
    }
  }, [dataSource, uploadCount]);

  const loadBackendPatches = async () => {
    setIsLoadingPatches(true);
    try {
      const response = await getPatches();
      if (response.data) {
        setBackendPatches(response.data.patches);
      }
    } catch (error) {
      console.error('Failed to load patches:', error);
    } finally {
      setIsLoadingPatches(false);
    }
  };

  const handleUploadSuccess = (patchId: string, factsCount: number) => {
    console.log('Upload successful:', patchId, 'with', factsCount, 'facts');
    // Trigger reload of backend patches
    setUploadCount((prev) => prev + 1);
    // Switch to backend data source
    setDataSource('backend');
  };

  const handleUploadError = (error: string) => {
    console.error('Upload failed:', error);
  };

  // Determine which timeline to use
  const allTimeline = dataSource === 'backend' && backendPatches.length > 0
    ? createTimelineSnapshots(backendPatches)
    : syntheticTimeline;

  // Get all patches for current data source
  const allPatches = dataSource === 'backend' ? backendPatches : syntheticTimeline.map(s => {
    // Reconstruct patch from synthetic data
    return {
      'db/id': s.patchId,
      'patch/timestamp': s.timestamp,
      'patch/source': { type: 'synthetic' },
      'patch/facts': [], // Will be populated from graph nodes
      'patch/edges': [],
    } as Patch;
  });

  // Extract all available topics
  const availableTopics = useMemo(() => {
    const topics = new Set<string>();
    allTimeline.forEach(snapshot => {
      snapshot.graph.nodes.forEach(node => {
        if (node.topic) topics.add(node.topic);
      });
    });
    return Array.from(topics);
  }, [allTimeline]);

  // Apply search filters to current snapshot
  const filteredSnapshot = useMemo(() => {
    const snapshot = allTimeline[currentIndex];
    if (!snapshot) return null;

    const filteredNodes = snapshot.graph.nodes.filter(node => {
      // Text search
      if (searchFilters.textQuery && !node.label.toLowerCase().includes(searchFilters.textQuery.toLowerCase())) {
        return false;
      }

      // Confidence range
      if (node.confidence < searchFilters.minConfidence || node.confidence > searchFilters.maxConfidence) {
        return false;
      }

      // Topic filter
      if (searchFilters.topics.length > 0 && !searchFilters.topics.includes(node.topic || '')) {
        return false;
      }

      return true;
    });

    const filteredNodeIds = new Set(filteredNodes.map(n => n.id));
    const filteredLinks = snapshot.graph.links.filter(
      link => filteredNodeIds.has(link.source) && filteredNodeIds.has(link.target)
    );

    return {
      ...snapshot,
      graph: {
        nodes: filteredNodes,
        links: filteredLinks,
      },
    };
  }, [allTimeline, currentIndex, searchFilters]);

  const currentSnapshot = filteredSnapshot || allTimeline[currentIndex];

  const handleNodeClick = (nodeId: string) => {
    // Find the fact corresponding to this node
    const node = currentSnapshot.graph.nodes.find((n) => n.id === nodeId);
    if (node) {
      // Create a fact object from the node
      const fact: Fact = {
        'db/id': nodeId,
        'claim/text': node.label,
        'claim/confidence': node.confidence,
        'claim/topic': node.topic || 'general',
        'claim/valid-from': currentSnapshot.timestamp,
        'claim/lod': 0,
      };
      setSelectedFact(fact);
    }
  };

  const handleNodeHover = (nodeId: string | null) => {
    setHoveredNodeId(nodeId);
  };

  const handleTimelineChange = (index: number) => {
    setCurrentIndex(index);
  };

  const handleClearFilters = () => {
    setSearchFilters({
      textQuery: '',
      minConfidence: 0,
      maxConfidence: 1,
      topics: [],
    });
  };

  return (
    <div className="app">
      {/* Header */}
      <header className="app-header">
        <div className="header-content">
          <div>
            <h1 className="app-title">VKM Graph: Knowledge Evolution</h1>
            <p className="app-subtitle">
              {dataSource === 'synthetic'
                ? 'Watch how understanding of "AI Scaling" evolved from 2020 to 2025'
                : `Visualizing ${backendPatches.length} patches from backend`}
            </p>
          </div>
          <div className="header-controls">
            <div className="backend-status">
              <span className={`status-indicator ${backendStatus}`} />
              <span className="status-text">
                {backendStatus === 'checking' && 'Checking backend...'}
                {backendStatus === 'online' && 'Backend online'}
                {backendStatus === 'offline' && 'Backend offline'}
              </span>
            </div>
            <div className="data-source-toggle">
              <button
                className={`toggle-btn ${dataSource === 'synthetic' ? 'active' : ''}`}
                onClick={() => setDataSource('synthetic')}
              >
                Synthetic Data
              </button>
              <button
                className={`toggle-btn ${dataSource === 'backend' ? 'active' : ''}`}
                onClick={() => setDataSource('backend')}
                disabled={backendStatus !== 'online'}
              >
                Backend Data
              </button>
              <button
                className="toggle-btn"
                onClick={() => setShowExportPanel(!showExportPanel)}
              >
                {showExportPanel ? 'Hide' : 'Export'}
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Upload Zone */}
      {backendStatus === 'online' && (
        <div className="upload-section fade-in">
          <UploadZone
            onUploadSuccess={handleUploadSuccess}
            onUploadError={handleUploadError}
          />
        </div>
      )}

      {/* Main Content */}
      <main className="app-main">
        {isLoadingPatches ? (
          <div className="loading-state">
            <div className="spinner" />
            <p>Loading patches from backend...</p>
          </div>
        ) : (
          <>
            {/* Search Panel */}
            <SearchPanel
              filters={searchFilters}
              onChange={setSearchFilters}
              availableTopics={availableTopics}
              onClear={handleClearFilters}
            />

            {/* Export Panel */}
            {showExportPanel && (
              <div className="fade-in" style={{ marginBottom: 'var(--spacing-lg)' }}>
                <ExportPanel
                  patches={allPatches}
                  currentGraph={currentSnapshot.graph}
                  svgRef={svgRef}
                />
              </div>
            )}

            {/* Graph Visualization */}
            <div className="fade-in">
              <PatchGraph
                data={currentSnapshot.graph}
                width={800}
                height={500}
                onNodeClick={handleNodeClick}
                onNodeHover={handleNodeHover}
                svgRef={svgRef}
              />
            </div>

            {/* Timeline Scrubber */}
            <div className="fade-in">
              <Timeline
                snapshots={allTimeline}
                currentIndex={currentIndex}
                onChange={handleTimelineChange}
              />
            </div>
          </>
        )}
      </main>

      {/* Fact Detail Panel */}
      <FactDetailPanel
        fact={selectedFact}
        onClose={() => setSelectedFact(null)}
      />
    </div>
  );
}

export default App;
