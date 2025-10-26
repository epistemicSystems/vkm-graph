/**
 * PatchGraph Component
 *
 * Renders a force-directed graph visualization of a knowledge patch.
 * Uses D3.js for physics simulation and layout.
 */

import React, { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import { GraphData, GraphNode, GraphLink } from '@/types';
import { getConfidenceColor, getTopicColor, visualizationConfig } from '@/config';

interface PatchGraphProps {
  data: GraphData;
  width?: number;
  height?: number;
  onNodeClick?: (nodeId: string) => void;
  onNodeHover?: (nodeId: string | null) => void;
  svgRef?: React.RefObject<SVGSVGElement>;
}

export const PatchGraph: React.FC<PatchGraphProps> = ({
  data,
  width = 800,
  height = 600,
  onNodeClick,
  onNodeHover,
  svgRef: externalSvgRef,
}) => {
  const internalSvgRef = useRef<SVGSVGElement>(null);
  const svgRef = externalSvgRef || internalSvgRef;
  const [hoveredNode, setHoveredNode] = useState<string | null>(null);

  useEffect(() => {
    if (!svgRef.current || !data.nodes.length) return;

    // Clear previous visualization
    d3.select(svgRef.current).selectAll('*').remove();

    const svg = d3
      .select(svgRef.current)
      .attr('width', width)
      .attr('height', height)
      .attr('viewBox', [0, 0, width, height]);

    // Create container for zoom
    const container = svg.append('g');

    // Add zoom behavior
    const zoom = d3
      .zoom<SVGSVGElement, unknown>()
      .scaleExtent([0.5, 3])
      .on('zoom', (event) => {
        container.attr('transform', event.transform);
      });

    svg.call(zoom);

    // Create force simulation
    const simulation = d3
      .forceSimulation<GraphNode>(data.nodes)
      .force(
        'link',
        d3
          .forceLink<GraphNode, GraphLink>(data.links)
          .id((d) => d.id)
          .distance(visualizationConfig.physics.linkDistance)
      )
      .force('charge', d3.forceManyBody().strength(visualizationConfig.physics.chargeStrength))
      .force('center', d3.forceCenter(width / 2, height / 2))
      .force('collision', d3.forceCollide().radius(30))
      .alphaDecay(visualizationConfig.physics.alphaDecay);

    // Create arrow markers for directed edges
    const defs = svg.append('defs');

    defs
      .selectAll('marker')
      .data(['supports', 'contradicts', 'revises'])
      .enter()
      .append('marker')
      .attr('id', (d) => `arrow-${d}`)
      .attr('viewBox', '0 -5 10 10')
      .attr('refX', 20)
      .attr('refY', 0)
      .attr('markerWidth', 6)
      .attr('markerHeight', 6)
      .attr('orient', 'auto')
      .append('path')
      .attr('d', 'M0,-5L10,0L0,5')
      .attr('fill', (d) => {
        switch (d) {
          case 'supports':
            return '#10B981'; // Green
          case 'contradicts':
            return '#EF4444'; // Red
          case 'revises':
            return '#F59E0B'; // Amber
          default:
            return '#6B7280'; // Gray
        }
      });

    // Draw links (edges)
    const link = container
      .append('g')
      .attr('class', 'links')
      .selectAll('line')
      .data(data.links)
      .enter()
      .append('line')
      .attr('stroke', (d) => {
        switch (d.relation) {
          case 'supports':
            return '#10B981';
          case 'contradicts':
            return '#EF4444';
          case 'revises':
            return '#F59E0B';
          default:
            return '#6B7280';
        }
      })
      .attr('stroke-opacity', (d) => d.strength * 0.6)
      .attr('stroke-width', (d) => Math.sqrt(d.strength) * 2)
      .attr('marker-end', (d) => `url(#arrow-${d.relation})`);

    // Draw nodes
    const node = container
      .append('g')
      .attr('class', 'nodes')
      .selectAll('circle')
      .data(data.nodes)
      .enter()
      .append('circle')
      .attr('r', 12)
      .attr('fill', (d) => getConfidenceColor(d.confidence))
      .attr('stroke', (d) => getTopicColor(d.topic))
      .attr('stroke-width', 2)
      .style('cursor', 'pointer')
      .call(
        d3
          .drag<SVGCircleElement, GraphNode>()
          .on('start', dragstarted)
          .on('drag', dragged)
          .on('end', dragended)
      )
      .on('click', (event, d) => {
        event.stopPropagation();
        if (onNodeClick) onNodeClick(d.id);
      })
      .on('mouseenter', (event, d) => {
        setHoveredNode(d.id);
        if (onNodeHover) onNodeHover(d.id);
      })
      .on('mouseleave', () => {
        setHoveredNode(null);
        if (onNodeHover) onNodeHover(null);
      });

    // Add node labels
    const labels = container
      .append('g')
      .attr('class', 'labels')
      .selectAll('text')
      .data(data.nodes)
      .enter()
      .append('text')
      .text((d) => {
        // Truncate long labels
        const maxLength = 30;
        return d.label.length > maxLength
          ? d.label.substring(0, maxLength) + '...'
          : d.label;
      })
      .attr('font-size', 10)
      .attr('dx', 15)
      .attr('dy', 4)
      .attr('fill', '#374151')
      .style('pointer-events', 'none')
      .style('user-select', 'none');

    // Update positions on each tick
    simulation.on('tick', () => {
      link
        .attr('x1', (d) => (d.source as GraphNode).x || 0)
        .attr('y1', (d) => (d.source as GraphNode).y || 0)
        .attr('x2', (d) => (d.target as GraphNode).x || 0)
        .attr('y2', (d) => (d.target as GraphNode).y || 0);

      node.attr('cx', (d) => d.x || 0).attr('cy', (d) => d.y || 0);

      labels.attr('x', (d) => d.x || 0).attr('y', (d) => d.y || 0);
    });

    // Drag functions
    function dragstarted(event: d3.D3DragEvent<SVGCircleElement, GraphNode, GraphNode>) {
      if (!event.active) simulation.alphaTarget(0.3).restart();
      event.subject.fx = event.subject.x;
      event.subject.fy = event.subject.y;
    }

    function dragged(event: d3.D3DragEvent<SVGCircleElement, GraphNode, GraphNode>) {
      event.subject.fx = event.x;
      event.subject.fy = event.y;
    }

    function dragended(event: d3.D3DragEvent<SVGCircleElement, GraphNode, GraphNode>) {
      if (!event.active) simulation.alphaTarget(0);
      event.subject.fx = null;
      event.subject.fy = null;
    }

    // Cleanup
    return () => {
      simulation.stop();
    };
  }, [data, width, height, onNodeClick, onNodeHover]);

  return (
    <div className="patch-graph-container">
      <svg ref={svgRef} className="patch-graph" />
      {hoveredNode && (
        <div className="node-tooltip">
          {data.nodes.find((n) => n.id === hoveredNode)?.label}
        </div>
      )}
    </div>
  );
};
