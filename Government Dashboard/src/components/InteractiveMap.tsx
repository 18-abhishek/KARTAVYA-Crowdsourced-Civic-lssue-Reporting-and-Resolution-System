import React, { useState } from 'react';
import { DistrictMetric } from '../types';
import { Plus, Minus, Maximize2, Crosshair, Info, MapPin, AlertCircle, CheckCircle2, ChevronRight } from 'lucide-react';

interface InteractiveMapProps {
  districts: DistrictMetric[];
  selectedDistrict: DistrictMetric | null;
  onSelectDistrict: (district: DistrictMetric) => void;
  onOpenDistrictModal: (district?: DistrictMetric) => void;
}

export const InteractiveMap: React.FC<InteractiveMapProps> = ({
  districts,
  selectedDistrict,
  onSelectDistrict,
  onOpenDistrictModal,
}) => {
  const [zoomLevel, setZoomLevel] = useState(1);
  const [hoveredDistrict, setHoveredDistrict] = useState<DistrictMetric | null>(null);
  const [mapCenter, setMapCenter] = useState({ x: 0, y: 0 });

  const handleZoomIn = () => setZoomLevel((prev) => Math.min(prev + 0.25, 2.5));
  const handleZoomOut = () => setZoomLevel((prev) => Math.max(prev - 0.25, 0.8));
  const handleResetZoom = () => {
    setZoomLevel(1);
    setMapCenter({ x: 0, y: 0 });
  };

  const getDensityColor = (density: 'High' | 'Medium' | 'Low') => {
    switch (density) {
      case 'High':
        return { bg: '#dc2626', ring: 'rgba(220, 38, 38, 0.35)', badge: 'bg-red-100 text-red-700' };
      case 'Medium':
        return { bg: '#ea580c', ring: 'rgba(234, 88, 12, 0.35)', badge: 'bg-amber-100 text-amber-800' };
      case 'Low':
        return { bg: '#16a34a', ring: 'rgba(22, 163, 74, 0.35)', badge: 'bg-emerald-100 text-emerald-800' };
    }
  };

  return (
    <div className="relative bg-[#eaf1ec] rounded-2xl border border-stone-200 overflow-hidden shadow-xs h-[480px] lg:h-[540px] flex flex-col">
      {/* Top Header info bar */}
      <div className="absolute top-4 left-4 z-20 bg-white/95 backdrop-blur-xs px-4 py-2.5 rounded-xl border border-stone-200 shadow-sm pointer-events-auto">
        <div className="flex items-center gap-1.5">
          <h2 className="text-sm font-bold text-stone-900">Jharkhand Municipal Map</h2>
          <Info className="w-3.5 h-3.5 text-stone-400 cursor-help" />
        </div>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Live issue density & status by district
        </p>
      </div>

      {/* Left Map Controls */}
      <div className="absolute top-20 left-4 z-20 flex flex-col gap-1 bg-white/95 backdrop-blur-xs p-1 rounded-xl border border-stone-200 shadow-sm">
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
        <div className="h-px bg-stone-200 my-0.5 mx-1" />
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

      {/* SVG Canvas Map Surface */}
      <div className="flex-1 w-full h-full relative overflow-hidden select-none bg-[#e8f0e9]">
        <div
          className="w-full h-full transition-transform duration-300 ease-out origin-center"
          style={{
            transform: `scale(${zoomLevel}) translate(${mapCenter.x}px, ${mapCenter.y}px)`,
          }}
        >
          {/* Map Vector Graphic: Jharkhand Geographic Contour & Terrain */}
          <svg
            viewBox="0 0 1000 650"
            className="w-full h-full object-cover"
            style={{ filter: 'drop-shadow(0 2px 8px rgba(0,0,0,0.06))' }}
          >
            {/* Background Roads / Terrain Texture */}
            <defs>
              <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#d5e3d7" strokeWidth="0.8" strokeDasharray="2,2" />
              </pattern>
              {/* Water Gradient */}
              <linearGradient id="damWater" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#93c5fd" stopOpacity="0.7" />
                <stop offset="100%" stopColor="#60a5fa" stopOpacity="0.9" />
              </linearGradient>
            </defs>

            <rect width="1000" height="650" fill="#e9f2eb" />
            <rect width="1000" height="650" fill="url(#grid)" />

            {/* Jharkhand State Boundary Shape */}
            <path
              d="M 120 180 
                 Q 240 120 380 140 
                 T 620 90 
                 Q 780 80 880 130 
                 T 950 250 
                 Q 940 380 880 440 
                 T 750 560 
                 Q 650 630 520 610 
                 T 320 560 
                 Q 200 520 130 420 
                 T 90 280 Z"
              fill="#dceadc"
              stroke="#b5ceb8"
              strokeWidth="2.5"
            />

            {/* Internal district boundary lines */}
            <path d="M 380 140 L 450 300 L 520 610" stroke="#c8decb" strokeWidth="1.5" strokeDasharray="4,4" fill="none" />
            <path d="M 620 90 L 600 320 L 750 560" stroke="#c8decb" strokeWidth="1.5" strokeDasharray="4,4" fill="none" />
            <path d="M 130 420 L 450 300 L 880 440" stroke="#c8decb" strokeWidth="1.5" strokeDasharray="4,4" fill="none" />

            {/* Major Water Bodies (e.g. Patratu Reservoir, Subarnarekha, Damodar River) */}
            <path
              d="M 180 320 Q 350 340 480 370 T 700 390 T 910 430"
              stroke="#93c5fd"
              strokeWidth="5"
              strokeLinecap="round"
              fill="none"
              opacity="0.85"
            />
            {/* Patratu Dam Lake graphic */}
            <ellipse cx="440" cy="390" rx="32" ry="18" fill="url(#damWater)" />
            <text x="440" y="415" textAnchor="middle" fill="#0284c7" fontSize="10" fontWeight="600">
              Patratu Dam
            </text>

            {/* Secondary Water tributaries */}
            <path
              d="M 600 120 Q 640 220 680 330 T 740 520"
              stroke="#93c5fd"
              strokeWidth="3.5"
              strokeLinecap="round"
              fill="none"
              opacity="0.75"
            />

            {/* Major Highways (NH-33, NH-2, GT Road, Ranchi-Jamshedpur Expressway) */}
            <path d="M 100 240 L 480 360 L 710 480 L 890 540" stroke="#fde047" strokeWidth="2.5" fill="none" opacity="0.9" />
            <path d="M 320 110 L 480 360 L 520 580" stroke="#fed7aa" strokeWidth="2" fill="none" opacity="0.8" />
            <path d="M 620 100 L 720 280 L 710 480" stroke="#fed7aa" strokeWidth="2" fill="none" opacity="0.8" />

            {/* Prominent Local Towns Labels printed on map like in screenshot */}
            <g fill="#718096" fontSize="11" fontWeight="500">
              <text x="210" y="240">Urej</text>
              <text x="290" y="160">Sohedi</text>
              <text x="420" y="170">Ronhe / Paseria</text>
              <text x="560" y="180">Chano</text>
              <text x="470" y="270">Jarjara</text>
              <text x="360" y="300">Potanga</text>
              <text x="230" y="380">Chapra</text>
              <text x="180" y="450">Binja</text>
              <text x="280" y="490">Ukrid</text>
              <text x="540" y="320">Garsula</text>
              <text x="610" y="380">Gidi Religar</text>
              <text x="590" y="480">Bhurkunda</text>
              <text x="460" y="520">Rasda</text>
              <text x="490" y="470">Balkudra</text>
              <text x="640" y="550">Chikor</text>
            </g>

            {/* Interactive District Hotspots */}
            {districts.map((district) => {
              const cx = (district.coordinates.x / 100) * 1000;
              const cy = (district.coordinates.y / 100) * 650;
              const colors = getDensityColor(district.density);
              const isSelected = selectedDistrict?.id === district.id;
              const isHovered = hoveredDistrict?.id === district.id;

              return (
                <g
                  key={district.id}
                  className="cursor-pointer transition-all duration-200"
                  onClick={() => onSelectDistrict(district)}
                  onMouseEnter={() => setHoveredDistrict(district)}
                  onMouseLeave={() => setHoveredDistrict(null)}
                >
                  {/* Outer Pulsing Aura for High Density */}
                  {district.density === 'High' && (
                    <circle
                      cx={cx}
                      cy={cy}
                      r={isSelected || isHovered ? '28' : '22'}
                      fill={colors.ring}
                      className="animate-pulse"
                    />
                  )}

                  {/* Base Circle */}
                  <circle
                    cx={cx}
                    cy={cy}
                    r={isSelected || isHovered ? '18' : '14'}
                    fill={colors.bg}
                    stroke="#ffffff"
                    strokeWidth="3"
                    className="transition-all duration-200"
                  />

                  {/* Icon / Inner Dot */}
                  <circle cx={cx} cy={cy} r="4" fill="#ffffff" />

                  {/* Label tag */}
                  <g transform={`translate(${cx}, ${cy - 24})`}>
                    <rect
                      x="-45"
                      y="-12"
                      width="90"
                      height="20"
                      rx="10"
                      fill={isSelected ? '#1c1917' : '#ffffff'}
                      stroke={isSelected ? '#1c1917' : '#e2e8f0'}
                      strokeWidth="1.5"
                      className="filter drop-shadow-xs"
                    />
                    <text
                      x="0"
                      y="2"
                      textAnchor="middle"
                      fill={isSelected ? '#ffffff' : '#1e293b'}
                      fontSize="10.5"
                      fontWeight="700"
                    >
                      {district.name}
                    </text>
                  </g>
                </g>
              );
            })}
          </svg>
        </div>

        {/* Hover Tooltip Card */}
        {hoveredDistrict && (
          <div
            className="absolute z-30 bg-stone-900 text-white p-3 rounded-xl shadow-xl pointer-events-none text-xs w-56 animate-in fade-in"
            style={{
              left: `calc(${hoveredDistrict.coordinates.x}% - 110px)`,
              top: `calc(${hoveredDistrict.coordinates.y}% - 100px)`,
            }}
          >
            <div className="flex items-center justify-between border-b border-stone-800 pb-1.5 mb-1.5">
              <span className="font-bold text-sm text-amber-400">{hoveredDistrict.name}</span>
              <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${getDensityColor(hoveredDistrict.density).badge}`}>
                {hoveredDistrict.density}
              </span>
            </div>
            <div className="space-y-1">
              <div className="flex justify-between">
                <span className="text-stone-400">Open Grievances:</span>
                <span className="font-bold text-white">{hoveredDistrict.openIssuesCount.toLocaleString()}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-stone-400">Resolution Rate:</span>
                <span className="font-semibold text-emerald-400">{hoveredDistrict.resolutionRate}%</span>
              </div>
              <div className="flex justify-between">
                <span className="text-stone-400">Critical Hotspots:</span>
                <span className="font-semibold text-red-400">{hoveredDistrict.criticalIssuesCount}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Left Legend Box */}
      <div className="absolute bottom-4 left-4 z-20 bg-white/95 backdrop-blur-xs p-3.5 rounded-xl border border-stone-200 shadow-sm w-48 pointer-events-auto">
        <h4 className="text-xs font-bold text-stone-900 mb-2">Issue Density</h4>
        <div className="space-y-1.5 text-xs text-stone-600">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-[#dc2626] shrink-0" />
            <span className="font-medium text-stone-700">High (500+)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-[#ea580c] shrink-0" />
            <span className="font-medium text-stone-700">Medium (100-500)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-[#16a34a] shrink-0" />
            <span className="font-medium text-stone-700">Low (&lt;100)</span>
          </div>
        </div>
        <div className="mt-2.5 pt-2 border-t border-stone-100 flex items-center justify-between text-xs text-stone-500 font-medium">
          <span>Total Municipal Areas:</span>
          <span className="font-bold text-stone-900">24</span>
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
