/**
 * VKM Graph - Main Application
 *
 * The Knowledge Graph Evolution System visualization
 * Demonstrates how understanding evolves over time
 */

import React, { useState } from 'react';
import { PatchGraph } from '@/components/PatchGraph';
import { Timeline } from '@/components/Timeline';
import { syntheticTimeline } from '@/data/syntheticData';
import { visualizationConfig } from '@/config';
import './App.css';

function App() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);

  const currentSnapshot = syntheticTimeline[currentIndex];

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
        <h1 className="app-title">VKM Graph: Knowledge Evolution</h1>
        <p className="app-subtitle">
          Watch how understanding of "AI Scaling" evolved from 2020 to 2025
        </p>
      </header>

      {/* Main Content */}
      <main className="app-main">
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
            snapshots={syntheticTimeline}
            currentIndex={currentIndex}
            onChange={handleTimelineChange}
          />
        </div>
      </main>
    </div>
  );
}

export default App;
