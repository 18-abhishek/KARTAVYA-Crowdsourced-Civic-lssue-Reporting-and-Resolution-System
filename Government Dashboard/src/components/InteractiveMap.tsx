import React, { useState, useRef, useCallback } from 'react';
import { DistrictMetric } from '../types';
import { districtPaths } from '../data/districtPaths';
import { Plus, Minus, Maximize2, Crosshair, Info, MapPin, ChevronRight } from 'lucide-react';

interface InteractiveMapProps {
  districts: DistrictMetric[];
  selectedDistrict: DistrictMetric | null;
  onSelectDistrict: (district: DistrictMetric) => void;
  onOpenDistrictModal: (district?: DistrictMetric) => void;
}

// Mapping from district name in data to GeoJSON district name key
const DISTRICT_NAME_MAP: Record<string, string> = {
  'Ranchi': 'Ranchi',
  'Dhanbad': 'Dhanbad',
  'Jamshedpur (East Singhbhum)': 'Purba Singhbhum',
  'Purba Singhbhum': 'Purba Singhbhum',
  'Bokaro': 'Bokaro',
  'Deoghar': 'Deoghar',
  'Hazaribagh': 'Hazaribag',
  'Hazaribag': 'Hazaribag',
  'Giridih': 'Giridih',
  'Ramgarh': 'Ramgarh',
  'Dumka': 'Dumka',
  'Palamu (Medininagar)': 'Palamu',
  'Palamu': 'Palamu',
  'West Singhbhum (Chaibasa)': 'Pashchimi Singhbhum',
  'Pashchimi Singhbhum': 'Pashchimi Singhbhum',
  'Godda': 'Godda',
  'Gumla': 'Gumla',
  'Simdega': 'Simdega',
  'Latehar': 'Latehar',
  'Koderma': 'Kodarma',
  'Kodarma': 'Kodarma',
  'Lohardaga': 'Lohardaga',
  'Sahebganj': 'Sahibganj',
  'Sahibganj': 'Sahibganj',
  'Pakur': 'Pakur',
  'Jamtara': 'Jamtara',
  'Seraikela Kharsawan': 'Saraikela-Kharsawan',
  'Saraikela-Kharsawan': 'Saraikela-Kharsawan',
  'Khunti': 'Khunti',
  'Garhwa': 'Garhwa',
  'Chatra': 'Chatra',
};

// Accurate centroids in SVG coords (viewBox 0 0 1000 650)
const DISTRICT_CENTROIDS: Record<string, { cx: number; cy: number }> = {
  'Bokaro':               { cx: 588, cy: 317 },
  'Chatra':               { cx: 342, cy: 240 },
  'Deoghar':              { cx: 749, cy: 195 },
  'Dhanbad':              { cx: 684, cy: 289 },
  'Dumka':                { cx: 864, cy: 199 },
  'Garhwa':               { cx: 80,  cy: 249 },
  'Giridih':              { cx: 608, cy: 195 },
  'Godda':                { cx: 862, cy: 92  },
  'Gumla':                { cx: 261, cy: 421 },
  'Hazaribag':            { cx: 468, cy: 241 },
  'Jamtara':              { cx: 775, cy: 262 },
  'Kodarma':              { cx: 499, cy: 154 },
  'Latehar':              { cx: 248, cy: 319 },
  'Lohardaga':            { cx: 292, cy: 359 },
  'Pakur':                { cx: 942, cy: 156 },
  'Palamu':               { cx: 190, cy: 215 },
  'Pashchimi Singhbhum':  { cx: 477, cy: 562 },
  'Purba Singhbhum':      { cx: 687, cy: 526 },
  'Ranchi':               { cx: 448, cy: 394 },
  'Sahibganj':            { cx: 948, cy: 60  },
  'Saraikela-Kharsawan':  { cx: 556, cy: 485 },
  'Simdega':              { cx: 264, cy: 531 },
  'Khunti':               { cx: 429, cy: 467 },
  'Ramgarh':              { cx: 482, cy: 320 },
};

// Color mapping: High is Red (including Ranchi, Dhanbad, Purba Singhbhum), Medium is Orange, Low is Green
const getDensityFill = (density: 'High' | 'Medium' | 'Low', selected: boolean, hovered: boolean) => {
  if (hovered) {
    switch (density) {
      case 'High':   return '#f87171'; // lively light red on hover
      case 'Medium': return '#fb923c'; // lively orange on hover
      case 'Low':    return '#86efac'; // lively green on hover
    }
  }
  if (selected) {
    switch (density) {
      case 'High':   return '#fca5a5'; // crisp red matching Dhanbad & Purba
      case 'Medium': return '#fed7aa'; // crisp orange
      case 'Low':    return '#bbf7d0'; // crisp green
    }
  }
  switch (density) {
    case 'High':   return '#fecaca'; // soft clean red
    case 'Medium': return '#ffedd5'; // soft clean amber/orange
    case 'Low':    return '#dcfce7'; // soft clean green
  }
};

const getDensityStroke = (density: 'High' | 'Medium' | 'Low', selected: boolean, hovered: boolean) => {
  switch (density) {
    case 'High':   return selected || hovered ? '#dc2626' : '#ef4444';
    case 'Medium': return selected || hovered ? '#ea580c' : '#f97316';
    case 'Low':    return selected || hovered ? '#16a34a' : '#22c55e';
  }
};

const getDensityBadge = (density: 'High' | 'Medium' | 'Low') => {
  switch (density) {
    case 'High':   return 'bg-red-50 text-red-700 border border-red-200';
    case 'Medium': return 'bg-amber-50 text-amber-800 border border-amber-200';
    case 'Low':    return 'bg-emerald-50 text-emerald-800 border border-emerald-200';
  }
};

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  districts,
  selectedDistrict,
  onSelectDistrict,
  onOpenDistrictModal,
}) => {
  const [zoomLevel, setZoomLevel] = useState(1);
  const [hoveredDistrict, setHoveredDistrict] = useState<DistrictMetric | null>(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });
  const [containerSize, setContainerSize] = useState({ width: 900, height: 550 });
  const [mapCenter, setMapCenter] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const svgRef = useRef<SVGSVGElement>(null);
  const dragStartRef = useRef({ x: 0, y: 0 });
  const initialCenterRef = useRef({ x: 0, y: 0 });
  const hasDraggedRef = useRef(false);

  const handleZoomIn = () => setZoomLevel((prev) => Math.min(prev + 0.3, 4));
  const handleZoomOut = () => setZoomLevel((prev) => Math.max(prev - 0.3, 0.6));
  const handleResetZoom = () => {
    setZoomLevel(1);
    setMapCenter({ x: 0, y: 0 });
  };

  // Mouse wheel zoom support
  const handleWheel = useCallback((e: React.WheelEvent) => {
    e.preventDefault();
    const zoomFactor = e.deltaY < 0 ? 1.15 : 0.85;
    setZoomLevel((prev) => Math.min(Math.max(prev * zoomFactor, 0.6), 4));
  }, []);

  // Mouse drag pan support (Up, Down, Left, Right)
  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.button !== 0) return; // only left click
    setIsDragging(true);
    hasDraggedRef.current = false;
    dragStartRef.current = { x: e.clientX, y: e.clientY };
    initialCenterRef.current = { ...mapCenter };
  };

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      setContainerSize({ width: rect.width, height: rect.height });
      setTooltipPos({
        x: e.clientX - rect.left,
        y: e.clientY - rect.top,
      });
    }

    if (isDragging) {
      const dx = e.clientX - dragStartRef.current.x;
      const dy = e.clientY - dragStartRef.current.y;
      if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
        hasDraggedRef.current = true;
      }
      setMapCenter({
        x: initialCenterRef.current.x + dx,
        y: initialCenterRef.current.y + dy,
      });
    }
  }, [isDragging]);

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Calculate boundary-safe tooltip coordinates so it never gets cut off
  const tooltipWidth = 250;
  const tooltipHeight = 175;
  const showOnLeft = tooltipPos.x + tooltipWidth + 24 > containerSize.width;
  const tooltipLeft = showOnLeft
    ? Math.max(16, tooltipPos.x - tooltipWidth - 14)
    : Math.min(containerSize.width - tooltipWidth - 16, tooltipPos.x + 14);

  const showBelow = tooltipPos.y - tooltipHeight - 16 < 0;
  const tooltipTop = showBelow
    ? Math.min(containerSize.height - tooltipHeight - 16, tooltipPos.y + 16)
    : Math.max(16, tooltipPos.y - tooltipHeight - 14);

  // Build a lookup: geoJSON key -> district metric
  const districtByGeoKey: Record<string, DistrictMetric> = {};
  districts.forEach((d) => {
    const geoKey = DISTRICT_NAME_MAP[d.name];
    if (geoKey) districtByGeoKey[geoKey] = d;
  });

  // All 24 GeoJSON keys
  const allGeoKeys = Object.keys(districtPaths);

  return (
    <div
      ref={containerRef}
      className="relative bg-white rounded-2xl border border-stone-200 overflow-hidden shadow-xs h-[520px] lg:h-[580px] flex flex-col select-none"
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      {/* Top Header info bar */}
      <div className="absolute top-4 left-4 z-20 bg-white/95 backdrop-blur-xs px-4 py-2.5 rounded-xl border border-stone-200 shadow-sm pointer-events-auto">
        <div className="flex items-center gap-1.5">
          <h2 className="text-sm font-bold text-stone-900">Jharkhand District Map</h2>
          <Info className="w-3.5 h-3.5 text-stone-400 cursor-help" />
        </div>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Live issue density &amp; status across all 24 districts • Drag to pan
        </p>
      </div>

      {/* Top Right Map Controls - Sleek horizontal bar leaving all districts completely unobstructed */}
      <div className="absolute top-4 right-4 z-20 flex items-center gap-1 bg-white/95 backdrop-blur-xs p-1 rounded-xl border border-stone-200 shadow-sm pointer-events-auto">
        <button
          onClick={handleZoomIn}
          className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-stone-100 text-stone-700 transition-colors"
          title="Zoom In"
        >
          <Plus className="w-4 h-4" />
        </button>
        <button
          onClick={handleZoomOut}
          className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-stone-100 text-stone-700 transition-colors"
          title="Zoom Out"
        >
          <Minus className="w-4 h-4" />
        </button>
        <div className="w-px h-5 bg-stone-200 mx-0.5" />
        <button
          onClick={() => alert('Full screen view mode activated')}
          className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-stone-100 text-stone-700 transition-colors"
          title="Full Screen"
        >
          <Maximize2 className="w-4 h-4" />
        </button>
        <button
          onClick={handleResetZoom}
          className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-stone-100 text-stone-700 transition-colors"
          title="Center State Map"
        >
          <Crosshair className="w-4 h-4" />
        </button>
      </div>

      {/* Map Surface with Realistic Aerial Green Land Backdrop */}
      <div
        className={`flex-1 w-full h-full relative overflow-hidden bg-stone-50 ${
          isDragging ? 'cursor-grabbing' : 'cursor-grab'
        }`}
        onMouseDown={handleMouseDown}
        onWheel={handleWheel}
      >
        {/* Sharp & Whitened Aerial Countryside Terrain Background */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <img
            src="/terrain_bg.jpg"
            alt="Terrain Background"
            className="w-full h-full object-cover opacity-70"
          />
          {/* Whitened light wash overlay */}
          <div className="absolute inset-0 bg-white/60" />
          <div className="absolute inset-0 bg-gradient-to-t from-white/50 via-transparent to-white/30" />
        </div>

        {/* 2D Panning & Zooming Map Canvas */}
        <div
          className={`w-full h-full origin-center relative z-10 ${
            isDragging ? 'transition-none' : 'transition-transform duration-200 ease-out'
          }`}
          style={{
            transform: `translate(${mapCenter.x}px, ${mapCenter.y}px) scale(${zoomLevel})`,
          }}
        >
          <svg
            ref={svgRef}
            viewBox="0 0 1000 650"
            className="w-full h-full"
            style={{ filter: 'drop-shadow(0 10px 28px rgba(15, 23, 42, 0.09))' }}
          >
            {/* ─── District Polygons from GeoJSON ─── */}
            {allGeoKeys.map((geoKey) => {
              const pathD = districtPaths[geoKey];
              const district = districtByGeoKey[geoKey];
              const isSelected = district ? selectedDistrict?.id === district.id : false;
              const isHovered = district ? hoveredDistrict?.id === district.id : false;
              const centroid = DISTRICT_CENTROIDS[geoKey];

              const fillColor = district
                ? getDensityFill(district.density, isSelected, isHovered)
                : '#f1f5f9';
              const strokeColor = district
                ? getDensityStroke(district.density, isSelected, isHovered)
                : '#cbd5e1';
              const strokeWidth = isSelected ? 1.8 : isHovered ? 1.6 : 1.2;

              return (
                <g
                  key={geoKey}
                  className="cursor-pointer"
                  onClick={() => {
                    if (hasDraggedRef.current) return; // Ignore clicks if dragging/panning
                    if (district) onSelectDistrict(district);
                  }}
                  onMouseEnter={() => {
                    if (!isDragging && district) setHoveredDistrict(district);
                  }}
                  onMouseLeave={() => setHoveredDistrict(null)}
                >
                  {/* District polygon shape */}
                  <path
                    d={pathD}
                    fill={fillColor}
                    stroke={strokeColor}
                    strokeWidth={strokeWidth}
                    strokeLinejoin="round"
                    strokeLinecap="round"
                    className="transition-colors duration-150"
                  />

                  {/* District name label at centroid */}
                  {centroid && district && (
                    <g transform={`translate(${centroid.cx}, ${centroid.cy})`}>
                      {/* Label pill background - clean white for all */}
                      <rect
                        x={-28}
                        y={-8}
                        width={56}
                        height={16}
                        rx={8}
                        fill="#ffffff"
                        stroke={isSelected ? strokeColor : '#e2e8f0'}
                        strokeWidth={isSelected ? '1.5' : '1'}
                        className="shadow-xs"
                      />
                      <text
                        textAnchor="middle"
                        dominantBaseline="central"
                        y={-0.5}
                        fontSize="7.5"
                        fontWeight={isSelected ? '700' : '600'}
                        fill={isSelected ? '#0f172a' : '#334155'}
                        fontFamily="'Plus Jakarta Sans', sans-serif"
                      >
                        {geoKey.length > 9 ? geoKey.slice(0, 8) + '…' : geoKey}
                      </text>
                    </g>
                  )}
                </g>
              );
            })}
          </svg>
        </div>

        {/* Hover Tooltip with Smart Boundary Collision Handling */}
        {hoveredDistrict && (
          <div
            className="absolute z-30 bg-stone-900/95 text-white p-3.5 rounded-xl shadow-xl pointer-events-none text-xs w-[250px] backdrop-blur-xs transition-all duration-75 border border-stone-800/80"
            style={{
              left: `${tooltipLeft}px`,
              top: `${tooltipTop}px`,
            }}
          >
            <div className="flex items-center justify-between border-b border-stone-700/80 pb-2 mb-2">
              <span className="font-bold text-sm text-amber-400">{hoveredDistrict.name}</span>
              <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${getDensityBadge(hoveredDistrict.density)}`}>
                {hoveredDistrict.density}
              </span>
            </div>
            <div className="space-y-1.5">
              <div className="flex justify-between items-center">
                <span className="text-stone-400">Open Grievances:</span>
                <span className="font-bold text-white">{hoveredDistrict.openIssuesCount.toLocaleString()}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-stone-400">Resolution Rate:</span>
                <span className="font-semibold text-emerald-400">{hoveredDistrict.resolutionRate}%</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-stone-400">Critical Issues:</span>
                <span className="font-semibold text-red-400">{hoveredDistrict.criticalIssuesCount}</span>
              </div>
              <div className="flex justify-between items-start pt-0.5">
                <span className="text-stone-400 shrink-0">Top Issue:</span>
                <span className="font-medium text-stone-300 text-right pl-2 leading-tight">{hoveredDistrict.topDepartmentIssue}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Minimized Bottom Left Legend Bar - Compact horizontal layout ensuring Simdega is 100% visible */}
      <div className="absolute bottom-3.5 left-3.5 z-20 bg-white/95 backdrop-blur-xs px-3.5 py-1.5 rounded-xl border border-stone-200 shadow-sm pointer-events-auto flex items-center gap-3.5 text-xs">
        <span className="font-bold text-stone-900 text-[11px]">Issue Density:</span>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-xs bg-[#fecaca] border border-[#ef4444] shrink-0" />
          <span className="font-medium text-stone-700 text-[11px]">High (500+)</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-xs bg-[#ffedd5] border border-[#f97316] shrink-0" />
          <span className="font-medium text-stone-700 text-[11px]">Med (100–500)</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-xs bg-[#dcfce7] border border-[#22c55e] shrink-0" />
          <span className="font-medium text-stone-700 text-[11px]">Low (&lt;100)</span>
        </div>
      </div>

      {/* Floating Pill: View District Details Button */}
      <div className="absolute bottom-4 right-4 z-20 pointer-events-auto">
        <button
          onClick={() => onOpenDistrictModal(selectedDistrict || districts[0])}
          className="inline-flex items-center gap-2 px-4 py-2 bg-white hover:bg-stone-50 text-stone-800 text-xs font-bold rounded-xl border border-stone-300 shadow-md transition-all hover:scale-102 active:scale-98"
        >
          <MapPin className="w-4 h-4 text-[#ea580c]" />
          <span>View District Details</span>
          <ChevronRight className="w-3.5 h-3.5 text-stone-400" />
        </button>
      </div>
    </div>
  );
};
