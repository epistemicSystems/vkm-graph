/**
 * Timeline Component
 *
 * Interactive horizontal scrubber for navigating through knowledge evolution.
 * Shows snapshots over time with smooth transitions.
 */

import React, { useState, useRef, useEffect } from 'react';
import { TimelineSnapshot } from '@/types';
import { visualizationConfig } from '@/config';

interface TimelineProps {
  snapshots: TimelineSnapshot[];
  currentIndex: number;
  onChange: (index: number) => void;
}

export const Timeline: React.FC<TimelineProps> = ({
  snapshots,
  currentIndex,
  onChange,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // Handle keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft' && currentIndex > 0) {
        onChange(currentIndex - 1);
      } else if (e.key === 'ArrowRight' && currentIndex < snapshots.length - 1) {
        onChange(currentIndex + 1);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentIndex, snapshots.length, onChange]);

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    updatePosition(e.clientX);
  };

  const handleMouseMove = (e: MouseEvent) => {
    if (isDragging) {
      updatePosition(e.clientX);
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  useEffect(() => {
    if (isDragging) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
      return () => {
        window.removeEventListener('mousemove', handleMouseMove);
        window.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDragging]);

  const updatePosition = (clientX: number) => {
    if (!containerRef.current) return;

    const rect = containerRef.current.getBoundingClientRect();
    const x = clientX - rect.left;
    const percentage = Math.max(0, Math.min(1, x / rect.width));
    const index = Math.round(percentage * (snapshots.length - 1));

    if (index !== currentIndex) {
      onChange(index);
    }
  };

  const handleSnapshotClick = (index: number) => {
    onChange(index);
  };

  const progressPercentage = (currentIndex / (snapshots.length - 1)) * 100;

  return (
    <div className="timeline-container">
      {/* Stats Display */}
      <div className="timeline-stats">
        <div className="stat">
          <span className="stat-label">Facts:</span>
          <span className="stat-value">{snapshots[currentIndex].stats.numFacts}</span>
        </div>
        <div className="stat">
          <span className="stat-label">Confidence:</span>
          <span className="stat-value">
            {(snapshots[currentIndex].stats.avgConfidence * 100).toFixed(0)}%
          </span>
        </div>
        <div className="stat">
          <span className="stat-label">Topics:</span>
          <span className="stat-value">
            {Object.keys(snapshots[currentIndex].stats.topics).length}
          </span>
        </div>
      </div>

      {/* Timeline Track */}
      <div
        ref={containerRef}
        className="timeline-track"
        onMouseDown={handleMouseDown}
        style={{ cursor: isDragging ? 'grabbing' : 'grab' }}
      >
        {/* Progress Bar */}
        <div className="timeline-progress">
          <div
            className="timeline-progress-fill"
            style={{
              width: `${progressPercentage}%`,
              transition: isDragging ? 'none' : `width ${visualizationConfig.animation.duration}ms ${visualizationConfig.animation.easing}`,
            }}
          />
        </div>

        {/* Snapshot Markers */}
        <div className="timeline-markers">
          {snapshots.map((snapshot, index) => {
            const position = (index / (snapshots.length - 1)) * 100;
            const isActive = index === currentIndex;

            return (
              <div
                key={snapshot.patchId}
                className={`timeline-marker ${isActive ? 'active' : ''}`}
                style={{ left: `${position}%` }}
                onClick={() => handleSnapshotClick(index)}
              >
                <div className="marker-dot" />
                <div className="marker-label">{snapshot.label}</div>
              </div>
            );
          })}
        </div>

        {/* Current Position Indicator */}
        <div
          className="timeline-handle"
          style={{
            left: `${progressPercentage}%`,
            transition: isDragging ? 'none' : `left ${visualizationConfig.animation.duration}ms ${visualizationConfig.animation.easing}`,
          }}
        />
      </div>

      {/* Description */}
      <div className="timeline-description">
        <h3>{snapshots[currentIndex].label}</h3>
        <p className="description-text">
          {snapshots[currentIndex].graph.nodes.length} concepts,{' '}
          {snapshots[currentIndex].graph.links.length} relationships
        </p>
      </div>
    </div>
  );
};
