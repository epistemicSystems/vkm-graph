/**
 * FactDetailPanel Component
 *
 * Displays detailed information about a selected fact
 */

import React from 'react';
import { Fact } from '@/types';

export interface FactDetailPanelProps {
  fact: Fact | null;
  onClose: () => void;
}

export const FactDetailPanel: React.FC<FactDetailPanelProps> = ({
  fact,
  onClose,
}) => {
  if (!fact) return null;

  const confidence = (fact['claim/confidence'] * 100).toFixed(1);
  const validFrom = new Date(fact['claim/valid-from']).toLocaleString();

  // Calculate confidence level
  const getConfidenceLevel = (conf: number): string => {
    if (conf >= 0.8) return 'high';
    if (conf >= 0.5) return 'medium';
    return 'low';
  };

  const confidenceLevel = getConfidenceLevel(fact['claim/confidence']);

  return (
    <div className="fact-detail-overlay" onClick={onClose}>
      <div className="fact-detail-panel" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="fact-detail-header">
          <h2 className="fact-detail-title">Fact Details</h2>
          <button className="fact-detail-close" onClick={onClose} aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Fact Text */}
        <div className="fact-detail-section">
          <label className="fact-detail-label">Claim</label>
          <p className="fact-detail-claim">{fact['claim/text']}</p>
        </div>

        {/* Metadata Grid */}
        <div className="fact-detail-metadata">
          {/* Confidence */}
          <div className="metadata-item">
            <label className="metadata-label">Confidence</label>
            <div className="confidence-display">
              <div className={`confidence-badge ${confidenceLevel}`}>
                {confidence}%
              </div>
              <div className="confidence-bar-container">
                <div
                  className={`confidence-bar ${confidenceLevel}`}
                  style={{ width: `${confidence}%` }}
                />
              </div>
            </div>
          </div>

          {/* Topic */}
          <div className="metadata-item">
            <label className="metadata-label">Topic</label>
            <span className="topic-badge">{fact['claim/topic']}</span>
          </div>

          {/* Valid From */}
          <div className="metadata-item">
            <label className="metadata-label">Valid From</label>
            <span className="metadata-value">{validFrom}</span>
          </div>

          {/* Fact ID */}
          <div className="metadata-item">
            <label className="metadata-label">ID</label>
            <code className="metadata-code">{fact['db/id']}</code>
          </div>
        </div>

        {/* Additional Info */}
        {fact['claim/source'] && (
          <div className="fact-detail-section">
            <label className="fact-detail-label">Source</label>
            <p className="metadata-value">{fact['claim/source']}</p>
          </div>
        )}

        {fact['claim/evidence'] && (
          <div className="fact-detail-section">
            <label className="fact-detail-label">Evidence</label>
            <p className="metadata-value">{fact['claim/evidence']}</p>
          </div>
        )}

        {/* Actions */}
        <div className="fact-detail-actions">
          <button
            className="action-btn secondary"
            onClick={() => {
              navigator.clipboard.writeText(fact['claim/text']);
              alert('Fact copied to clipboard!');
            }}
          >
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
              />
            </svg>
            Copy Text
          </button>
          <button
            className="action-btn secondary"
            onClick={() => {
              const json = JSON.stringify(fact, null, 2);
              navigator.clipboard.writeText(json);
              alert('Fact JSON copied to clipboard!');
            }}
          >
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
              />
            </svg>
            Copy JSON
          </button>
        </div>
      </div>
    </div>
  );
};

export default FactDetailPanel;
