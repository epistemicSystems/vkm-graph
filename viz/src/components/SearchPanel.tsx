/**
 * SearchPanel Component
 *
 * Advanced search and filtering for facts in the knowledge graph
 */

import React, { useState } from 'react';

export interface SearchFilters {
  textQuery: string;
  minConfidence: number;
  maxConfidence: number;
  topics: string[];
  dateRange?: {
    start: Date | null;
    end: Date | null;
  };
}

export interface SearchPanelProps {
  filters: SearchFilters;
  onChange: (filters: SearchFilters) => void;
  availableTopics: string[];
  onClear: () => void;
}

export const SearchPanel: React.FC<SearchPanelProps> = ({
  filters,
  onChange,
  availableTopics,
  onClear,
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const handleTextQueryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...filters, textQuery: e.target.value });
  };

  const handleMinConfidenceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...filters, minConfidence: parseFloat(e.target.value) });
  };

  const handleMaxConfidenceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange({ ...filters, maxConfidence: parseFloat(e.target.value) });
  };

  const handleTopicToggle = (topic: string) => {
    const newTopics = filters.topics.includes(topic)
      ? filters.topics.filter((t) => t !== topic)
      : [...filters.topics, topic];
    onChange({ ...filters, topics: newTopics });
  };

  const handleClear = () => {
    onClear();
  };

  const hasActiveFilters =
    filters.textQuery.length > 0 ||
    filters.minConfidence > 0 ||
    filters.maxConfidence < 1 ||
    filters.topics.length > 0;

  return (
    <div className="search-panel">
      {/* Search Bar */}
      <div className="search-bar">
        <input
          type="text"
          className="search-input"
          placeholder="Search facts..."
          value={filters.textQuery}
          onChange={handleTextQueryChange}
        />
        <button
          className="search-toggle-btn"
          onClick={() => setIsExpanded(!isExpanded)}
          aria-label={isExpanded ? 'Hide filters' : 'Show filters'}
        >
          <svg
            className={`filter-icon ${isExpanded ? 'active' : ''}`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
            />
          </svg>
        </button>
        {hasActiveFilters && (
          <button className="clear-filters-btn" onClick={handleClear}>
            Clear
          </button>
        )}
      </div>

      {/* Advanced Filters */}
      {isExpanded && (
        <div className="filters-panel">
          {/* Confidence Range */}
          <div className="filter-section">
            <label className="filter-label">Confidence Range</label>
            <div className="confidence-sliders">
              <div className="slider-group">
                <span className="slider-label">Min: {(filters.minConfidence * 100).toFixed(0)}%</span>
                <input
                  type="range"
                  className="confidence-slider"
                  min="0"
                  max="1"
                  step="0.05"
                  value={filters.minConfidence}
                  onChange={handleMinConfidenceChange}
                />
              </div>
              <div className="slider-group">
                <span className="slider-label">Max: {(filters.maxConfidence * 100).toFixed(0)}%</span>
                <input
                  type="range"
                  className="confidence-slider"
                  min="0"
                  max="1"
                  step="0.05"
                  value={filters.maxConfidence}
                  onChange={handleMaxConfidenceChange}
                />
              </div>
            </div>
          </div>

          {/* Topic Filters */}
          {availableTopics.length > 0 && (
            <div className="filter-section">
              <label className="filter-label">Topics</label>
              <div className="topic-chips">
                {availableTopics.map((topic) => (
                  <button
                    key={topic}
                    className={`topic-chip ${
                      filters.topics.includes(topic) ? 'active' : ''
                    }`}
                    onClick={() => handleTopicToggle(topic)}
                  >
                    {topic}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Active Filters Summary */}
          {hasActiveFilters && (
            <div className="active-filters-summary">
              {filters.textQuery && (
                <span className="active-filter">Text: "{filters.textQuery}"</span>
              )}
              {filters.minConfidence > 0 && (
                <span className="active-filter">
                  Min confidence: {(filters.minConfidence * 100).toFixed(0)}%
                </span>
              )}
              {filters.maxConfidence < 1 && (
                <span className="active-filter">
                  Max confidence: {(filters.maxConfidence * 100).toFixed(0)}%
                </span>
              )}
              {filters.topics.map((topic) => (
                <span key={topic} className="active-filter">
                  Topic: {topic}
                </span>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SearchPanel;
