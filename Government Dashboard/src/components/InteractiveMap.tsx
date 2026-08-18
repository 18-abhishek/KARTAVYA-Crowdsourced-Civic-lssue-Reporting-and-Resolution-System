import React, { useState, useRef, useCallback } from 'react';
import { DistrictMetric } from '../types';
import { districtPaths } from '../data/districtPaths';
import {
  Info,
  MapPin,
  ChevronRight,
  Plus,
  Minus,
  Maximize2,
  Crosshair,
  ClipboardList,
  AlertCircle,
} from 'lucide-react';

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

// District Pin configuration matching reference layout exactly
interface DistrictPinConfig {
  id: string;
  name: string;
  count: string;
  rawCount: number;
  density: 'High' | 'Medium' | 'Low';
  pinColor: string;
  haloColor: string;
  haloRadius: number;
  cx: number;
  cy: number;
  badgeWidth: number;
}

const DISTRICT_PINS: DistrictPinConfig[] = [
  {
    id: 'Garhwa',
    name: 'Garhwa',
    count: '78',
    rawCount: 78,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 40,
    cx: 80,
    cy: 220,
    badgeWidth: 54,
  },
  {
    id: 'Palamu',
    name: 'Palamu',
    count: '132',
    rawCount: 132,
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.32)',
    haloRadius: 45,
    cx: 190,
    cy: 190,
    badgeWidth: 54,
  },
  {
    id: 'Chatra',
    name: 'Chatra',
    count: '64',
    rawCount: 64,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 38,
    cx: 342,
    cy: 220,
    badgeWidth: 52,
  },
  {
    id: 'Kodarma',
    name: 'Kodarma',
    count: '98',
    rawCount: 98,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 40,
    cx: 499,
    cy: 135,
    badgeWidth: 58,
  },
  {
    id: 'Hazaribag',
    name: 'Hazaribagh',
    count: '210',
    rawCount: 210,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.32)',
    haloRadius: 48,
    cx: 468,
    cy: 235,
    badgeWidth: 68,
  },
  {
    id: 'Deoghar',
    name: 'Deoghar',
    count: '156',
    rawCount: 156,
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.40)',
    haloRadius: 55,
    cx: 749,
    cy: 175,
    badgeWidth: 58,
  },
  {
    id: 'Dumka',
    name: 'Deoghar',
    count: '36',
    rawCount: 36,
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.28)',
    haloRadius: 36,
    cx: 864,
    cy: 185,
    badgeWidth: 58,
  },
  {
    id: 'Godda',
    name: 'Godda',
    count: '87',
    rawCount: 87,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 40,
    cx: 862,
    cy: 78,
    badgeWidth: 52,
  },
  {
    id: 'Latehar',
    name: 'Latehar',
    count: '58',
    rawCount: 58,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 38,
    cx: 248,
    cy: 305,
    badgeWidth: 54,
  },
  {
    id: 'Ranchi',
    name: 'Ranchi',
    count: '1,420',
    rawCount: 1420,
    density: 'High',
    pinColor: '#ef4444',
    haloColor: 'rgba(239, 68, 68, 0.45)',
    haloRadius: 95,
    cx: 448,
    cy: 375,
    badgeWidth: 56,
  },
  {
    id: 'Dhanbad',
    name: 'Dhanbad',
    count: '412',
    rawCount: 412,
    density: 'High',
    pinColor: '#ef4444',
    haloColor: 'rgba(239, 68, 68, 0.40)',
    haloRadius: 75,
    cx: 684,
    cy: 275,
    badgeWidth: 58,
  },
  {
    id: 'Jamtara',
    name: 'Jamtara',
    count: '66',
    rawCount: 66,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 36,
    cx: 775,
    cy: 250,
    badgeWidth: 54,
  },
  {
    id: 'Giridih',
    name: 'Giridih',
    count: '95',
    rawCount: 95,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 36,
    cx: 608,
    cy: 185,
    badgeWidth: 52,
  },
  {
    id: 'Gumla',
    name: 'Gumla',
    count: '55',
    rawCount: 55,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 38,
    cx: 261,
    cy: 405,
    badgeWidth: 52,
  },
  {
    id: 'Khunti',
    name: 'Khunti',
    count: '73',
    rawCount: 73,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 38,
    cx: 429,
    cy: 450,
    badgeWidth: 52,
  },
  {
    id: 'Simdega',
    name: 'Simdega',
    count: '44',
    rawCount: 44,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 36,
    cx: 264,
    cy: 515,
    badgeWidth: 56,
  },
  {
    id: 'Bokaro',
    name: 'Bokaro',
    count: '124',
    rawCount: 124,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 38,
    cx: 588,
    cy: 315,
    badgeWidth: 54,
  },
  {
    id: 'Saraikela-Kharsawan',
    name: 'Seraikela\nKharsawan',
    count: '102',
    rawCount: 102,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 38,
    cx: 556,
    cy: 475,
    badgeWidth: 70,
  },
  {
    id: 'Purba Singhbhum',
    name: 'Jamshedpur',
    count: '320',
    rawCount: 320,
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.45)',
    haloRadius: 75,
    cx: 687,
    cy: 515,
    badgeWidth: 72,
  },
];

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  districts,
  selectedDistrict,
  onSelectDistrict,
  onOpenDistrictModal,
}) => {
  const [zoomLevel, setZoomLevel] = useState(1);
  const [mapCenter, setMapCenter] = useState({ x: 0, y: 0 });
  const [hoveredDistrict, setHoveredDistrict] = useState<DistrictMetric | null>(null);
  const [hoveredPinId, setHoveredPinId] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });
  const [containerSize, setContainerSize] = useState({ width: 900, height: 550 });

  const containerRef = useRef<HTMLDivElement>(null);

  const handleZoomIn = () => setZoomLevel((prev) => Math.min(prev + 0.25, 2.5));
  const handleZoomOut = () => setZoomLevel((prev) => Math.max(prev - 0.25, 0.8));
  const handleResetZoom = () => {
    setZoomLevel(1);
    setMapCenter({ x: 0, y: 0 });
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
  }, []);

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
    >
      {/* ─── Top-Left Header ─── */}
      <div className="absolute top-4 left-4 z-30 bg-white/95 backdrop-blur-md px-4 py-2.5 rounded-2xl border border-stone-200/80 shadow-xs pointer-events-auto">
        <div className="flex items-center gap-1.5">
          <h2 className="text-sm font-extrabold text-stone-900 tracking-tight">Jharkhand Municipal Map</h2>
          <Info
            className="w-3.5 h-3.5 text-stone-400 hover:text-stone-700 cursor-pointer transition-colors"
            title="Overview of civic issue density across Jharkhand districts"
          />
        </div>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Live issue density &amp; status by district
        </p>
      </div>

      {/* ─── Left-Side Floating Controls (Vertical Toolbar) ─── */}
      <div className="absolute top-20 left-4 z-30 flex flex-col items-center bg-white/95 backdrop-blur-md rounded-2xl border border-stone-200/80 shadow-md p-1 space-y-1 pointer-events-auto">
        <button
          onClick={handleZoomIn}
          className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-stone-100 text-stone-700 transition-colors"
          title="Zoom In"
        >
          <Plus className="w-4 h-4" />
        </button>
        <button
          onClick={handleZoomOut}
          className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-stone-100 text-stone-700 transition-colors"
          title="Zoom Out"
        >
          <Minus className="w-4 h-4" />
        </button>
        <div className="w-5 h-px bg-stone-200 my-0.5" />
        <button
          onClick={() => alert('Full screen view mode activated')}
          className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-stone-100 text-stone-700 transition-colors"
          title="Full Screen"
        >
          <Maximize2 className="w-3.5 h-3.5" />
        </button>
        <button
          onClick={handleResetZoom}
          className="w-8 h-8 flex items-center justify-center rounded-xl hover:bg-stone-100 text-stone-700 transition-colors"
          title="Recenter Map"
        >
          <Crosshair className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* ─── Bottom-Left Legend Card ─── */}
      <div className="absolute bottom-4 left-4 z-30 bg-white/95 backdrop-blur-md px-4 py-3.5 rounded-2xl border border-stone-200/80 shadow-md pointer-events-auto min-w-[175px]">
        <h4 className="text-xs font-bold text-stone-900 mb-2">Issue Density</h4>
        <div className="space-y-1.5 text-xs text-stone-700">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-[#ef4444] shrink-0 ring-2 ring-red-100" />
            <span className="font-medium text-[11px]">High (500+)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-[#f97316] shrink-0 ring-2 ring-orange-100" />
            <span className="font-medium text-[11px]">Medium (100-500)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-[#22c55e] shrink-0 ring-2 ring-green-100" />
            <span className="font-medium text-[11px]">Low (&lt;100)</span>
          </div>
        </div>

        <div className="mt-3 pt-2.5 border-t border-stone-100 flex items-center justify-between gap-2">
          <span className="text-[11px] text-stone-500 font-medium">Total Municipal Areas:</span>
          <span className="text-[11px] font-bold px-2 py-0.5 bg-blue-50 text-blue-600 border border-blue-200/60 rounded-lg">
            24
          </span>
        </div>
      </div>

      {/* ─── Bottom-Center Floating Action Button ─── */}
      <div className="absolute bottom-4 left-1/2 -translate-x-1/2 z-30 pointer-events-auto">
        <button
          onClick={() => onOpenDistrictModal(selectedDistrict || undefined)}
          className="inline-flex items-center gap-2 px-5 py-2 bg-white hover:bg-stone-50 text-stone-800 border border-stone-200/90 rounded-2xl text-xs font-bold shadow-md hover:shadow-lg transition-all active:scale-98"
        >
          <ClipboardList className="w-3.5 h-3.5 text-stone-600" />
          <span>View District Details</span>
        </button>
      </div>

      {/* ─── Map Canvas with 3D Extruded Isometric Surface & Landscape ─── */}
      <div className="flex-1 w-full h-full relative overflow-hidden bg-slate-50">
        {/* Soft country landscape terrain background */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <img
            src="/terrain_bg.jpg"
            alt="Terrain Landscape"
            className="w-full h-full object-cover opacity-60"
          />
          <div className="absolute inset-0 bg-white/70 backdrop-blur-[0.5px]" />
          <div className="absolute inset-0 bg-gradient-to-t from-white/60 via-transparent to-white/40" />
        </div>

        {/* 3D Map Transform Container */}
        <div
          className="w-full h-full origin-center relative z-10 transition-transform duration-200 ease-out"
          style={{
            transform: `translate(${mapCenter.x}px, ${mapCenter.y}px) scale(${zoomLevel})`,
          }}
        >
          <svg
            viewBox="0 0 1000 650"
            className="w-full h-full"
            style={{
              filter: 'drop-shadow(0 20px 35px rgba(15, 23, 42, 0.16))',
            }}
          >
            <defs>
              {/* Radial Heatmap Glow Gradients */}
              <radialGradient id="glow-ranchi" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#ef4444" stopOpacity="0.55" />
                <stop offset="35%" stopColor="#ef4444" stopOpacity="0.32" />
                <stop offset="70%" stopColor="#ef4444" stopOpacity="0.10" />
                <stop offset="100%" stopColor="#ef4444" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-dhanbad" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#ef4444" stopOpacity="0.48" />
                <stop offset="40%" stopColor="#f97316" stopOpacity="0.25" />
                <stop offset="75%" stopColor="#f97316" stopOpacity="0.08" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-jamshedpur" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#f97316" stopOpacity="0.50" />
                <stop offset="40%" stopColor="#f97316" stopOpacity="0.28" />
                <stop offset="75%" stopColor="#f97316" stopOpacity="0.08" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-deoghar" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#f97316" stopOpacity="0.42" />
                <stop offset="45%" stopColor="#f97316" stopOpacity="0.20" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-green-sm" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#22c55e" stopOpacity="0.35" />
                <stop offset="50%" stopColor="#22c55e" stopOpacity="0.15" />
                <stop offset="100%" stopColor="#22c55e" stopOpacity="0" />
              </radialGradient>
            </defs>

            {/* ─── 1. Multi-Layer 3D Extrusion Slabs Beneath ─── */}
            {/* Ambient Base Shadow */}
            <g transform="translate(0, 24)" opacity="0.28" filter="blur(12px)">
              {allGeoKeys.map((geoKey) => (
                <path key={`shadow-${geoKey}`} d={districtPaths[geoKey]} fill="#0f172a" />
              ))}
            </g>

            {/* Extruded Layer 5 (Deep Slate) */}
            <g transform="translate(0, 18)">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext5-${geoKey}`} d={districtPaths[geoKey]} fill="#94a3b8" />
              ))}
            </g>

            {/* Extruded Layer 4 */}
            <g transform="translate(0, 14)">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext4-${geoKey}`} d={districtPaths[geoKey]} fill="#cbd5e1" />
              ))}
            </g>

            {/* Extruded Layer 3 */}
            <g transform="translate(0, 10)">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext3-${geoKey}`} d={districtPaths[geoKey]} fill="#dbeafe" />
              ))}
            </g>

            {/* Extruded Layer 2 */}
            <g transform="translate(0, 6)">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext2-${geoKey}`} d={districtPaths[geoKey]} fill="#e2e8f0" />
              ))}
            </g>

            {/* Extruded Layer 1 (Just beneath surface) */}
            <g transform="translate(0, 2)">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext1-${geoKey}`} d={districtPaths[geoKey]} fill="#f1f5f9" />
              ))}
            </g>

            {/* ─── 2. Top Land Surface with Clean Subtle District Boundaries ─── */}
            <g>
              {allGeoKeys.map((geoKey) => {
                const pathD = districtPaths[geoKey];
                const district = districtByGeoKey[geoKey];
                const isSelected = district ? selectedDistrict?.id === district.id : false;
                const isHovered = district ? hoveredDistrict?.id === district.id : false;

                return (
                  <path
                    key={`top-${geoKey}`}
                    d={pathD}
                    fill={isSelected ? '#fef3c7' : isHovered ? '#f1f5f9' : '#fafafa'}
                    stroke={isSelected ? '#f59e0b' : '#cbd5e1'}
                    strokeWidth={isSelected ? 1.8 : 1.1}
                    className="cursor-pointer transition-colors duration-150"
                    onClick={() => {
                      if (district) onSelectDistrict(district);
                    }}
                    onMouseEnter={() => {
                      if (district) setHoveredDistrict(district);
                    }}
                    onMouseLeave={() => setHoveredDistrict(null)}
                  />
                );
              })}
            </g>

            {/* ─── 3. Heatmap Radial Glow Overlays ─── */}
            {/* Ranchi Intensive Red Heatmap Rings */}
            <circle cx="448" cy="385" r="105" fill="url(#glow-ranchi)" pointerEvents="none" />
            <circle cx="448" cy="385" r="60" fill="url(#glow-ranchi)" opacity="0.6" pointerEvents="none" />

            {/* Dhanbad Red/Orange Heatmap Rings */}
            <circle cx="684" cy="280" r="75" fill="url(#glow-dhanbad)" pointerEvents="none" />

            {/* Jamshedpur Orange Heatmap Rings */}
            <circle cx="687" cy="520" r="85" fill="url(#glow-jamshedpur)" pointerEvents="none" />

            {/* Deoghar Orange Heatmap */}
            <circle cx="749" cy="180" r="55" fill="url(#glow-deoghar)" pointerEvents="none" />

            {/* Palamu & Hazaribagh Heatmap Overlays */}
            <circle cx="190" cy="195" r="48" fill="url(#glow-deoghar)" opacity="0.65" pointerEvents="none" />
            <circle cx="468" cy="240" r="50" fill="url(#glow-green-sm)" opacity="0.85" pointerEvents="none" />

            {/* Green Low Density Area Halos */}
            {DISTRICT_PINS.filter((p) => p.density === 'Low').map((pin) => (
              <circle
                key={`halo-${pin.id}`}
                cx={pin.cx}
                cy={pin.cy + 5}
                r={pin.haloRadius}
                fill={pin.haloColor}
                pointerEvents="none"
              />
            ))}

            {/* ─── 4. Distinct 3D Pin Markers & District Labels ─── */}
            {DISTRICT_PINS.map((pin) => {
              const district = districtByGeoKey[pin.id];
              const isHovered = hoveredPinId === pin.id || (district && hoveredDistrict?.id === district.id);
              const isSelected = district && selectedDistrict?.id === district.id;

              return (
                <g
                  key={`pin-${pin.id}`}
                  className="cursor-pointer group"
                  onMouseEnter={() => {
                    setHoveredPinId(pin.id);
                    if (district) setHoveredDistrict(district);
                  }}
                  onMouseLeave={() => {
                    setHoveredPinId(null);
                    setHoveredDistrict(null);
                  }}
                  onClick={() => {
                    if (district) onSelectDistrict(district);
                  }}
                >
                  {/* Pin Group */}
                  <g
                    transform={`translate(${pin.cx}, ${pin.cy - 12}) ${
                      isHovered ? 'scale(1.18)' : isSelected ? 'scale(1.12)' : 'scale(1)'
                    }`}
                    className="transition-transform duration-150 origin-bottom"
                  >
                    {/* Pin Drop Shadow */}
                    <ellipse cx="0" cy="1" rx="5" ry="2" fill="#0f172a" opacity="0.3" />

                    {/* Teardrop Location Marker */}
                    <path
                      d="M 0,-24 C -8,-24 -12,-18 -12,-10 C -12,-1 0,1 0,1 C 0,1 12,-1 12,-10 C 12,-18 8,-24 0,-24 Z"
                      fill={pin.pinColor}
                      filter="drop-shadow(0 3px 6px rgba(0,0,0,0.22))"
                    />
                    {/* Inner White Center Dot */}
                    <circle cx="0" cy="-12" r="3.5" fill="#ffffff" />
                  </g>

                  {/* Clean White Pill Badge underneath Pin */}
                  <g
                    transform={`translate(${pin.cx}, ${pin.cy + 2})`}
                    className="transition-all duration-150"
                  >
                    {/* Badge Background */}
                    <rect
                      x={-(pin.badgeWidth / 2)}
                      y="0"
                      width={pin.badgeWidth}
                      height="26"
                      rx="6"
                      fill="#ffffff"
                      stroke={isHovered ? pin.pinColor : '#e2e8f0'}
                      strokeWidth={isHovered ? 1.5 : 1}
                      filter="drop-shadow(0 2px 5px rgba(15,23,42,0.14))"
                    />

                    {/* District Name */}
                    <text
                      x="0"
                      y="11"
                      textAnchor="middle"
                      fill="#0f172a"
                      fontSize="9"
                      fontWeight="700"
                      className="tracking-tight"
                    >
                      {pin.name.split('\n')[0]}
                    </text>

                    {/* Issue Count */}
                    <text
                      x="0"
                      y="21"
                      textAnchor="middle"
                      fill="#334155"
                      fontSize="8.5"
                      fontWeight="800"
                      fontFamily="monospace"
                    >
                      {pin.count}
                    </text>
                  </g>
                </g>
              );
            })}
          </svg>
        </div>
      </div>

      {/* ─── Hover Tooltip Card ─── */}
      {hoveredDistrict && (
        <div
          style={{
            position: 'absolute',
            left: `${tooltipLeft}px`,
            top: `${tooltipTop}px`,
            width: `${tooltipWidth}px`,
            pointerEvents: 'none',
          }}
          className="z-50 bg-white/95 backdrop-blur-md rounded-2xl border border-stone-200/90 shadow-2xl p-3.5 space-y-2.5 animate-in fade-in zoom-in-95 duration-150"
        >
          <div className="flex items-start justify-between gap-2 border-b border-stone-100 pb-2">
            <div>
              <div className="flex items-center gap-1 text-stone-900 font-bold text-xs">
                <MapPin className="w-3.5 h-3.5 text-[#ea580c] shrink-0" />
                <span className="truncate">{hoveredDistrict.name}</span>
              </div>
              <p className="text-[11px] text-stone-500 font-medium truncate mt-0.5">
                {hoveredDistrict.nodalOfficer?.name || 'Administrative DC'} • {hoveredDistrict.nodalOfficer?.designation || 'District HQ'}
              </p>
            </div>
            <span
              className={`text-[10px] font-extrabold px-2 py-0.5 rounded-md shrink-0 ${
                hoveredDistrict.density === 'High'
                  ? 'bg-red-50 text-red-700 border border-red-200'
                  : hoveredDistrict.density === 'Medium'
                  ? 'bg-amber-50 text-amber-800 border border-amber-200'
                  : 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              }`}
            >
              {hoveredDistrict.density}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-2 text-center">
            <div className="bg-stone-50 p-2 rounded-xl border border-stone-100">
              <span className="text-[10px] text-stone-500 font-semibold block">Total Issues</span>
              <span className="text-xs font-bold text-stone-900 font-mono">
                {hoveredDistrict.totalIssues?.toLocaleString() || 0}
              </span>
            </div>
            <div className="bg-stone-50 p-2 rounded-xl border border-stone-100">
              <span className="text-[10px] text-stone-500 font-semibold block">Resolved</span>
              <span className="text-xs font-bold text-emerald-700 font-mono">
                {hoveredDistrict.resolved?.toLocaleString() || 0} ({hoveredDistrict.resolutionRate || 0}%)
              </span>
            </div>
          </div>

          <div className="flex items-center justify-between text-[11px] pt-1 text-stone-600">
            <span className="font-semibold text-stone-500">Top Grievance:</span>
            <span className="font-bold text-stone-800 truncate max-w-[140px]">
              {hoveredDistrict.topDepartmentIssue || 'Civic Infrastructure'}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};
