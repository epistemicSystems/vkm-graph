/**
 * VKM Graph - Main Application
 *
 * The Knowledge Graph Evolution System visualization
 * Demonstrates how understanding evolves over time
 */

import React, { useState, useEffect } from 'react';
import { PatchGraph } from '@/components/PatchGraph';
import { Timeline } from '@/components/Timeline';
import { UploadZone } from '@/components/UploadZone';
import { syntheticTimeline, createTimelineSnapshots } from '@/data/syntheticData';
import { visualizationConfig } from '@/config';
import { getPatches, checkHealth } from '@/api/client';
import { Patch } from '@/types';
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
  const timeline = dataSource === 'backend' && backendPatches.length > 0
    ? createTimelineSnapshots(backendPatches)
    : syntheticTimeline;

  const currentSnapshot = timeline[currentIndex];

  const handleNodeClick = (nodeId: string) => {
    console.log('Node clicked:', nodeId);
    const node = currentSnapshot.graph.nodes.find((n) => n.id === nodeId);
    if (node) {
      alert(`Fact: ${node.label}\nConfidence: ${(node.confidence * 100).toFixed(0)}%`);
    }
  };

  const handleNodeHover = (nodeId: string | null) => {
    setHoveredNodeId(nodeId);
  };

  const handleTimelineChange = (index: number) => {
    setCurrentIndex(index);
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
            {/* Graph Visualization */}
            <div className="fade-in">
              <PatchGraph
                data={currentSnapshot.graph}
                width={800}
                height={500}
                onNodeClick={handleNodeClick}
                onNodeHover={handleNodeHover}
              />
            </div>

            {/* Timeline Scrubber */}
            <div className="fade-in">
              <Timeline
                snapshots={timeline}
                currentIndex={currentIndex}
                onChange={handleTimelineChange}
              />
            </div>
          </>
        )}
      </main>
    </div>
  );
}

export default App;
