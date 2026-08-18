import React, { useState, useRef, useCallback } from 'react';
import { DistrictMetric } from '../types';
import { districtPaths } from '../data/districtPaths';
import {
  Info,
  MapPin,
  ClipboardList,
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
  'Bokaro': 'Bokaro',
  'Deoghar': 'Deoghar',
  'Hazaribagh': 'Hazaribag',
  'Hazaribag': 'Hazaribag',
  'Giridih': 'Giridih',
  'Ramgarh': 'Ramgarh',
  'Dumka': 'Dumka',
  'Palamu (Medininagar)': 'Palamu',
  'Palamu': 'Palamu',
  'West Singhbhum': 'Pashchimi Singhbhum',
  'West Singhbhum (Chaibasa)': 'Pashchimi Singhbhum',
  'Pashchimi Singhbhum': 'Pashchimi Singhbhum',
  'East Singhbhum': 'Purba Singhbhum',
  'Jamshedpur (East Singhbhum)': 'Purba Singhbhum',
  'Purba Singhbhum': 'Purba Singhbhum',
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

// Color logic: >500 is Red (#ef4444), 100-500 is Orange (#f97316), <100 is Green (#22c55e)
const getPinSeverityColor = (count: number): string => {
  if (count >= 500) return '#ef4444';
  if (count >= 100) return '#f97316';
  return '#22c55e';
};

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
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.32)',
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
    cx: 740,
    cy: 195,
    badgeWidth: 58,
  },
  {
    id: 'Dumka',
    name: 'Dumka',
    count: '60',
    rawCount: 60,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 40,
    cx: 860,
    cy: 200,
    badgeWidth: 54,
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
    cy: 90,
    badgeWidth: 52,
  },
  {
    id: 'Pakur',
    name: 'Pakur',
    count: '48',
    rawCount: 48,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 36,
    cx: 942,
    cy: 160,
    badgeWidth: 52,
  },
  {
    id: 'Sahibganj',
    name: 'Sahebganj',
    count: '52',
    rawCount: 52,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.28)',
    haloRadius: 38,
    cx: 948,
    cy: 60,
    badgeWidth: 62,
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
    cx: 450,
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
    cy: 260,
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
    id: 'Lohardaga',
    name: 'Lohardaga',
    count: '42',
    rawCount: 42,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 36,
    cx: 295,
    cy: 355,
    badgeWidth: 60,
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
    cx: 255,
    cy: 430,
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
    cx: 430,
    cy: 475,
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
    id: 'Pashchimi Singhbhum',
    name: 'West Singhbhum',
    count: '48',
    rawCount: 48,
    density: 'Low',
    pinColor: '#22c55e',
    haloColor: 'rgba(34, 197, 94, 0.26)',
    haloRadius: 40,
    cx: 475,
    cy: 565,
    badgeWidth: 84,
  },
  {
    id: 'Purba Singhbhum',
    name: 'East Singhbhum',
    count: '320',
    rawCount: 320,
    density: 'Medium',
    pinColor: '#f97316',
    haloColor: 'rgba(249, 115, 22, 0.45)',
    haloRadius: 75,
    cx: 687,
    cy: 515,
    badgeWidth: 82,
  },
];

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  districts,
  selectedDistrict,
  onSelectDistrict,
  onOpenDistrictModal,
}) => {
  const [hoveredDistrict, setHoveredDistrict] = useState<DistrictMetric | null>(null);
  const [hoveredPinId, setHoveredPinId] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });
  const [containerSize, setContainerSize] = useState({ width: 900, height: 550 });

  const containerRef = useRef<HTMLDivElement>(null);

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
  const tooltipWidth = 280;
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

  // All 24 GeoJSON keys sorted so active/selected/hovered district renders LAST (on top of adjacent paths)
  const allGeoKeys = Object.keys(districtPaths);
  const sortedGeoKeys = [...allGeoKeys].sort((a, b) => {
    const districtA = districtByGeoKey[a];
    const districtB = districtByGeoKey[b];
    const isSelectedA = districtA && selectedDistrict?.id === districtA.id;
    const isSelectedB = districtB && selectedDistrict?.id === districtB.id;
    const isHoveredA = districtA && hoveredDistrict?.id === districtA.id;
    const isHoveredB = districtB && hoveredDistrict?.id === districtB.id;

    if (isSelectedA || isHoveredA) return 1;
    if (isSelectedB || isHoveredB) return -1;
    return 0;
  });

  return (
    <div
      ref={containerRef}
      className="relative bg-transparent h-[520px] lg:h-[580px] flex flex-col select-none"
      onMouseMove={handleMouseMove}
    >
      {/* ─── Top-Left Header ─── */}
      <div className="absolute top-4 left-4 z-30 glass-pill px-4 py-2.5 rounded-2xl shadow-xs pointer-events-auto">
        <div className="flex items-center gap-1.5">
          <h2 className="text-sm font-extrabold text-stone-900 tracking-tight">Jharkhand Municipal Map</h2>
          <span title="Overview of civic issue density across Jharkhand districts">
            <Info className="w-3.5 h-3.5 text-stone-400 hover:text-stone-700 cursor-pointer transition-colors" />
          </span>
        </div>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Live issue density &amp; status by district
        </p>
      </div>

      {/* ─── Compact Bottom-Left Legend Pill (Minimized) ─── */}
      <div className="absolute bottom-3 left-3 z-30 glass-pill px-3 py-1.5 rounded-2xl shadow-xs pointer-events-auto flex items-center gap-3 text-[10px] text-stone-600 font-medium">
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#ef4444] shrink-0" />
          <span>High 500+</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#f97316] shrink-0" />
          <span>Medium 100-500</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#22c55e] shrink-0" />
          <span>Low &lt;100</span>
        </div>
        <div className="w-px h-3 bg-stone-200" />
        <span className="font-bold text-blue-600 bg-blue-50/80 backdrop-blur-xs px-1.5 py-0.5 rounded text-[9.5px]">
          24 Areas
        </span>
      </div>

      {/* ─── Bottom-Right Floating Action Button ─── */}
      <div className="absolute bottom-3.5 right-3.5 z-30 pointer-events-auto">
        <button
          onClick={() => onOpenDistrictModal(selectedDistrict || undefined)}
          className="inline-flex items-center gap-2 px-4 py-2 glass-pill hover:bg-white/90 text-stone-800 rounded-2xl text-xs font-bold shadow-xs hover:shadow-md transition-all active:scale-98"
        >
          <ClipboardList className="w-3.5 h-3.5 text-stone-600" />
          <span>View District Details</span>
        </button>
      </div>

      {/* ─── Map Canvas with 3D Extruded Isometric Surface (Transparent BG) ─── */}
      <div className="flex-1 w-full h-full relative bg-transparent">
        {/* 3D Map Container */}
        <div className="w-full h-full origin-center relative z-10">
          <svg
            viewBox="-60 -35 1120 720"
            className="w-full h-full"
            style={{
              filter: 'drop-shadow(0 14px 28px rgba(15, 23, 42, 0.12))',
            }}
          >
            <defs>
              {/* Radial Heatmap Glow Gradients */}
              <radialGradient id="glow-ranchi" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#ef4444" stopOpacity="0.25" />
                <stop offset="35%" stopColor="#ef4444" stopOpacity="0.12" />
                <stop offset="70%" stopColor="#ef4444" stopOpacity="0.04" />
                <stop offset="100%" stopColor="#ef4444" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-dhanbad" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#ef4444" stopOpacity="0.20" />
                <stop offset="40%" stopColor="#f97316" stopOpacity="0.10" />
                <stop offset="75%" stopColor="#f97316" stopOpacity="0.03" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-jamshedpur" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#f97316" stopOpacity="0.18" />
                <stop offset="40%" stopColor="#f97316" stopOpacity="0.08" />
                <stop offset="75%" stopColor="#f97316" stopOpacity="0.02" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-deoghar" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#f97316" stopOpacity="0.16" />
                <stop offset="45%" stopColor="#f97316" stopOpacity="0.06" />
                <stop offset="100%" stopColor="#f97316" stopOpacity="0" />
              </radialGradient>

              <radialGradient id="glow-green-soft" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#22c55e" stopOpacity="0.30" />
                <stop offset="35%" stopColor="#22c55e" stopOpacity="0.15" />
                <stop offset="70%" stopColor="#22c55e" stopOpacity="0.04" />
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
            <g transform="translate(0, 18)" fill="#94a3b8" stroke="#94a3b8" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext5-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* Extruded Layer 4 */}
            <g transform="translate(0, 14)" fill="#cbd5e1" stroke="#cbd5e1" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext4-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* Extruded Layer 3 */}
            <g transform="translate(0, 10)" fill="#dbeafe" stroke="#dbeafe" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext3-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* Extruded Layer 2 */}
            <g transform="translate(0, 6)" fill="#e2e8f0" stroke="#e2e8f0" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext2-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* Extruded Layer 1 (Just beneath surface) */}
            <g transform="translate(0, 2)" fill="#f1f5f9" stroke="#f1f5f9" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`ext1-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* ─── Seamless Base Landmass Silhouette (Prevents Gaps Between Districts) ─── */}
            <g fill="#fafafa" stroke="#cbd5e1" strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round">
              {allGeoKeys.map((geoKey) => (
                <path key={`base-${geoKey}`} d={districtPaths[geoKey]} />
              ))}
            </g>

            {/* ─── 2. Top Land Surface with Clean Subtle District Boundaries ─── */}
            <g strokeLinejoin="round" strokeLinecap="round">
              {sortedGeoKeys.map((geoKey) => {
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
                    strokeWidth={isSelected ? 2.2 : 1.1}
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
            {/* Ranchi Intensive Red Heatmap Rings directly over Ranchi centroid */}
            <circle cx="450" cy="375" r="105" fill="url(#glow-ranchi)" opacity="0.6" pointerEvents="none" />
            <circle cx="450" cy="375" r="60" fill="url(#glow-ranchi)" opacity="0.25" pointerEvents="none" />

            {/* Dhanbad Red/Orange Heatmap Rings */}
            <circle cx="684" cy="275" r="75" fill="url(#glow-dhanbad)" opacity="0.5" pointerEvents="none" />

            {/* Jamshedpur Orange Heatmap Rings */}
            <circle cx="687" cy="515" r="85" fill="url(#glow-jamshedpur)" opacity="0.5" pointerEvents="none" />

            {/* Deoghar Orange Heatmap */}
            <circle cx="740" cy="195" r="55" fill="url(#glow-deoghar)" opacity="0.5" pointerEvents="none" />

            {/* Palamu & Hazaribagh Heatmap Overlays */}
            <circle cx="190" cy="190" r="48" fill="url(#glow-deoghar)" opacity="0.3" pointerEvents="none" />
            <circle cx="468" cy="235" r="50" fill="url(#glow-green-soft)" opacity="0.5" pointerEvents="none" />

            {/* Soft Green Glow Overlays for all Low Density Districts (Seamless Feather Edge) */}
            {DISTRICT_PINS.filter((p) => p.density === 'Low').map((pin) => (
              <circle
                key={`glow-green-${pin.id}`}
                cx={pin.cx}
                cy={pin.cy - 5}
                r={pin.haloRadius + 6}
                fill="url(#glow-green-soft)"
                pointerEvents="none"
              />
            ))}

            {/* ─── 4. Distinct 3D Pin Markers & District Labels ─── */}
            {DISTRICT_PINS.map((pin) => {
              const district = districtByGeoKey[pin.id];
              const isHovered = hoveredPinId === pin.id || (district && hoveredDistrict?.id === district.id);
              const isSelected = district && selectedDistrict?.id === district.id;
              const computedPinColor = getPinSeverityColor(pin.rawCount);

              return (
                <g
                  key={`pin-${pin.id}`}
                  className="cursor-pointer group"
                  transform={`translate(${pin.cx}, ${pin.cy})`}
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
                  {/* Pin Group (Anchor locked directly above badge - zero displacement on hover) */}
                  <g transform="translate(0, -5)">
                    {/* Pin Drop Shadow */}
                    <ellipse
                      cx="0"
                      cy="1"
                      rx={isHovered ? 5.5 : 4.5}
                      ry={isHovered ? 2.2 : 1.8}
                      fill="#0f172a"
                      opacity={isHovered ? 0.45 : 0.3}
                    />

                    {/* Teardrop Location Marker */}
                    <path
                      d="M 0,0 C -2,-4 -12,-12 -12,-20 C -12,-27 -6,-33 0,-33 C 6,-33 12,-27 12,-20 C 12,-12 2,-4 0,0 Z"
                      fill={computedPinColor}
                      stroke={isHovered ? '#ffffff' : 'none'}
                      strokeWidth={isHovered ? 1.5 : 0}
                      filter={
                        isHovered
                          ? 'drop-shadow(0 4px 10px rgba(0,0,0,0.38))'
                          : 'drop-shadow(0 3px 6px rgba(0,0,0,0.25))'
                      }
                      className="transition-all duration-150"
                    />
                    {/* Inner White Center Dot */}
                    <circle cx="0" cy="-20" r={isHovered ? 4.5 : 4} fill="#ffffff" />
                  </g>

                  {/* Clean White Pill Badge underneath Pin locked to same position */}
                  <g transform="translate(0, 0)" className="transition-all duration-150">
                    {/* Badge Background */}
                    <rect
                      x={-(pin.badgeWidth / 2)}
                      y="0"
                      width={pin.badgeWidth}
                      height="26"
                      rx="6"
                      fill="#ffffff"
                      stroke={isHovered ? computedPinColor : '#e2e8f0'}
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
          className="z-50 glass-modal rounded-3xl p-4 space-y-2.5 animate-in fade-in zoom-in-95 duration-150"
        >
          <div className="flex items-start justify-between gap-2.5 border-b border-stone-100 pb-2">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-1.5 text-stone-900 font-bold text-xs">
                <MapPin className="w-3.5 h-3.5 text-[#ea580c] shrink-0" />
                <span className="truncate">{hoveredDistrict.name}</span>
              </div>
              <p className="text-[11px] text-stone-500 font-medium truncate mt-0.5">
                {hoveredDistrict.nodalOfficer?.name || 'Administrative DC'} • {hoveredDistrict.nodalOfficer?.designation || 'District HQ'}
              </p>
            </div>
            <span
              className={`text-[10px] font-extrabold px-2 py-0.5 rounded-md shrink-0 whitespace-nowrap ${
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
