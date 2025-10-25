/**
 * Visualization Configuration
 *
 * Design system and interaction parameters for the VKM Graph visualization
 */

import { VisualizationConfig } from '@/types';

export const visualizationConfig: VisualizationConfig = {
  layout: {
    type: 'force-directed',
    dimensions: 2,
    chargeStrength: -300,
    linkDistance: 100,
  },

  colors: {
    // Confidence gradient: gray (low) → gold (high)
    confidence: {
      low: '#9CA3AF', // Gray-400
      medium: '#FCD34D', // Yellow-300
      high: '#F59E0B', // Amber-500 (gold)
    },

    // Topic colors
    topic: {
      scaling: '#3B82F6', // Blue-500
      data: '#10B981', // Green-500
      compute: '#8B5CF6', // Purple-500
      emergence: '#EC4899', // Pink-500
      optimization: '#F97316', // Orange-500
      efficiency: '#14B8A6', // Teal-500
      unknown: '#6B7280', // Gray-500
    },
  },

  animation: {
    duration: 500, // milliseconds
    easing: 'cubic-bezier(0.4, 0, 0.2, 1)', // ease-in-out
  },

  physics: {
    chargeStrength: -300,
    linkDistance: 100,
    alphaDecay: 0.02,
  },
};

// Color interpolation function
export function getConfidenceColor(confidence: number): string {
  const { low, medium, high } = visualizationConfig.colors.confidence;

  if (confidence < 0.5) {
    // Interpolate between low and medium
    const t = confidence * 2; // 0-0.5 → 0-1
    return interpolateColor(low, medium, t);
  } else {
    // Interpolate between medium and high
    const t = (confidence - 0.5) * 2; // 0.5-1 → 0-1
    return interpolateColor(medium, high, t);
  }
}

// Simple linear color interpolation
function interpolateColor(color1: string, color2: string, t: number): string {
  const c1 = hexToRgb(color1);
  const c2 = hexToRgb(color2);

  if (!c1 || !c2) return color1;

  const r = Math.round(c1.r + (c2.r - c1.r) * t);
  const g = Math.round(c1.g + (c2.g - c1.g) * t);
  const b = Math.round(c1.b + (c2.b - c1.b) * t);

  return rgbToHex(r, g, b);
}

function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : null;
}

function rgbToHex(r: number, g: number, b: number): string {
  return '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

export function getTopicColor(topic?: string): string {
  if (!topic) return visualizationConfig.colors.topic.unknown;
  return (
    visualizationConfig.colors.topic[
      topic as keyof typeof visualizationConfig.colors.topic
    ] || visualizationConfig.colors.topic.unknown
  );
}
