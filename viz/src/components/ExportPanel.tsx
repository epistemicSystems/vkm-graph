/**
 * ExportPanel Component
 *
 * Export visualization and data in multiple formats
 */

import React, { useRef } from 'react';
import { Patch, GraphData } from '@/types';

export interface ExportPanelProps {
  patches: Patch[];
  currentGraph: GraphData;
  svgRef?: React.RefObject<SVGSVGElement>;
}

export const ExportPanel: React.FC<ExportPanelProps> = ({
  patches,
  currentGraph,
  svgRef,
}) => {
  const exportJSON = () => {
    const data = JSON.stringify(patches, null, 2);
    downloadFile(data, 'vkm-graph-patches.json', 'application/json');
  };

  const exportCurrentPatchJSON = () => {
    if (patches.length === 0) return;
    const data = JSON.stringify(patches[patches.length - 1], null, 2);
    downloadFile(data, 'vkm-graph-current-patch.json', 'application/json');
  };

  const exportGraphData = () => {
    const data = JSON.stringify(currentGraph, null, 2);
    downloadFile(data, 'vkm-graph-data.json', 'application/json');
  };

  const exportSVG = () => {
    if (!svgRef?.current) {
      alert('SVG export not available. Make sure the graph is rendered.');
      return;
    }

    const svgElement = svgRef.current;
    const serializer = new XMLSerializer();
    const svgString = serializer.serializeToString(svgElement);

    // Add XML declaration and styling
    const fullSVG = `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
${svgString}`;

    downloadFile(fullSVG, 'vkm-graph.svg', 'image/svg+xml');
  };

  const exportPNG = async () => {
    if (!svgRef?.current) {
      alert('PNG export not available. Make sure the graph is rendered.');
      return;
    }

    try {
      const svgElement = svgRef.current;
      const serializer = new XMLSerializer();
      const svgString = serializer.serializeToString(svgElement);

      // Create a blob and canvas
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        throw new Error('Could not get canvas context');
      }

      // Get SVG dimensions
      const bbox = svgElement.getBoundingClientRect();
      canvas.width = bbox.width * 2; // 2x for better quality
      canvas.height = bbox.height * 2;

      // Create image from SVG
      const img = new Image();
      const svgBlob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
      const url = URL.createObjectURL(svgBlob);

      img.onload = () => {
        // Draw with white background
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Draw SVG
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
        URL.revokeObjectURL(url);

        // Convert to PNG
        canvas.toBlob((blob) => {
          if (blob) {
            const pngUrl = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.download = 'vkm-graph.png';
            link.href = pngUrl;
            link.click();
            URL.revokeObjectURL(pngUrl);
          }
        }, 'image/png');
      };

      img.onerror = () => {
        URL.revokeObjectURL(url);
        alert('Failed to export PNG. Please try SVG export instead.');
      };

      img.src = url;
    } catch (error) {
      console.error('PNG export error:', error);
      alert('Failed to export PNG. Please try SVG export instead.');
    }
  };

  const exportCSV = () => {
    if (patches.length === 0) {
      alert('No patches to export');
      return;
    }

    const rows: string[] = [];
    rows.push('Timestamp,Fact,Confidence,Topic,Source');

    patches.forEach((patch) => {
      const timestamp = patch['patch/timestamp'];
      const source = JSON.stringify(patch['patch/source']);

      patch['patch/facts'].forEach((fact) => {
        const text = escapeCsvValue(fact['claim/text']);
        const confidence = fact['claim/confidence'];
        const topic = fact['claim/topic'];

        rows.push(`${timestamp},"${text}",${confidence},${topic},"${source}"`);
      });
    });

    const csv = rows.join('\n');
    downloadFile(csv, 'vkm-graph-facts.csv', 'text/csv');
  };

  const exportMarkdown = () => {
    if (patches.length === 0) {
      alert('No patches to export');
      return;
    }

    const lines: string[] = [];
    lines.push('# VKM Graph Knowledge Evolution');
    lines.push('');
    lines.push(`Exported: ${new Date().toISOString()}`);
    lines.push(`Total Patches: ${patches.length}`);
    lines.push('');

    patches.forEach((patch, index) => {
      lines.push(`## Patch ${index + 1}: ${patch['patch/timestamp']}`);
      lines.push('');
      lines.push(`**Source:** ${JSON.stringify(patch['patch/source'])}`);
      lines.push('');
      lines.push('### Facts');
      lines.push('');

      patch['patch/facts'].forEach((fact, factIndex) => {
        const confidence = (fact['claim/confidence'] * 100).toFixed(0);
        lines.push(
          `${factIndex + 1}. **${fact['claim/text']}** (${confidence}% confidence, topic: ${fact['claim/topic']})`
        );
      });

      lines.push('');
    });

    const markdown = lines.join('\n');
    downloadFile(markdown, 'vkm-graph-report.md', 'text/markdown');
  };

  return (
    <div className="export-panel">
      <div className="export-section">
        <h3 className="export-title">Export Data</h3>
        <div className="export-buttons">
          <button className="export-btn" onClick={exportJSON}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            All Patches (JSON)
          </button>
          <button className="export-btn" onClick={exportCurrentPatchJSON}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
              />
            </svg>
            Current Patch (JSON)
          </button>
          <button className="export-btn" onClick={exportGraphData}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
            Graph Data (JSON)
          </button>
          <button className="export-btn" onClick={exportCSV}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            Facts (CSV)
          </button>
          <button className="export-btn" onClick={exportMarkdown}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            Report (Markdown)
          </button>
        </div>
      </div>

      <div className="export-section">
        <h3 className="export-title">Export Visualization</h3>
        <div className="export-buttons">
          <button className="export-btn" onClick={exportSVG}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            Vector (SVG)
          </button>
          <button className="export-btn" onClick={exportPNG}>
            <svg className="export-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            Image (PNG)
          </button>
        </div>
      </div>
    </div>
  );
};

// Helper functions

function downloadFile(content: string, filename: string, mimeType: string) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.download = filename;
  link.href = url;
  link.click();
  URL.revokeObjectURL(url);
}

function escapeCsvValue(value: string): string {
  return value.replace(/"/g, '""');
}

export default ExportPanel;
