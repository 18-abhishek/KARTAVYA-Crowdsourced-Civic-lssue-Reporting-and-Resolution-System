import React, { useState } from 'react';
import {
  topDistrictsComparison,
  departmentStats,
  weeklySlaTrends,
  citizenRatings,
} from '../data/mockData';
import {
  LayoutDashboard,
  Inbox,
  CheckCircle2,
  Activity,
  Building2,
  MessageSquareQuote,
  GitCompare,
  Lightbulb,
  Calendar,
  Download,
  Info,
  ChevronDown,
  ArrowUpRight,
  ArrowDownRight,
  TrendingUp,
  Award,
  Sparkles,
} from 'lucide-react';

interface WeeklyReportViewProps {
  onExportPdf: () => void;
}

export const WeeklyReportView: React.FC<WeeklyReportViewProps> = ({ onExportPdf }) => {
  const [sidebarTab, setSidebarTab] = useState<'overview' | 'inflow' | 'resolution' | 'sla' | 'departments' | 'feedback' | 'comparative'>('overview');
  const [viewScope, setViewScope] = useState<'state' | 'city'>('state');
  const [selectedTopDistrictsCount, setSelectedTopDistrictsCount] = useState('Top 5 Districts');
  const [selectedDateRange, setSelectedDateRange] = useState('Aug 08 - Aug 15, 2025');
  const [hoveredBarIndex, setHoveredBarIndex] = useState<number | null>(null);
  const [hoveredSlice, setHoveredSlice] = useState<string | null>(null);

  const sidebarItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'inflow', label: 'Issues Inflow', icon: Inbox },
    { id: 'resolution', label: 'Resolution', icon: CheckCircle2 },
    { id: 'sla', label: 'SLA Performance', icon: Activity },
    { id: 'departments', label: 'Departments', icon: Building2 },
    { id: 'feedback', label: 'Citizen Feedback', icon: MessageSquareQuote },
    { id: 'comparative', label: 'Comparative View', icon: GitCompare },
  ];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start animate-in fade-in duration-300">
      {/* Left Secondary Sidebar (col-span-3 on lg) */}
      <div className="lg:col-span-3 space-y-6">
        <div className="bg-white rounded-2xl border border-stone-200 p-3 shadow-xs space-y-1">
          {sidebarItems.map((item) => {
            const Icon = item.icon;
            const isActive = sidebarTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setSidebarTab(item.id as any)}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-colors text-left ${
                  isActive
                    ? 'bg-[#c2410c] text-white shadow-2xs'
                    : 'text-stone-600 hover:text-stone-900 hover:bg-stone-50'
                }`}
              >
                <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-white' : 'text-stone-400'}`} />
                <span className="truncate">{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* Bottom Data Insights Banner Card */}
        <div className="bg-[#fffbf6] rounded-2xl border border-[#fed7aa]/60 p-4 shadow-xs">
          <div className="flex items-center gap-2 text-[#ea580c] font-bold text-xs mb-1.5">
            <Lightbulb className="w-4 h-4 text-[#ea580c]" />
            <span>Data Insights</span>
          </div>
          <p className="text-xs text-stone-600 leading-relaxed font-medium">
            Resolution rate improved by <span className="font-bold text-stone-900">15.3%</span> compared to last week across urban municipal zones.
          </p>
        </div>
      </div>

      {/* Main Analytics Content (col-span-9 on lg) */}
      <div className="lg:col-span-9 space-y-6">
        {/* Top Controls Bar */}
        <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h2 className="text-base sm:text-lg font-bold text-stone-900 tracking-tight">
              Weekly Performance &amp; Analytics Report
            </h2>
            <p className="text-xs text-stone-500 font-medium mt-0.5">
              Comprehensive overview of civic issue management across Jharkhand
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            {/* Date Range Selector */}
            <div className="relative">
              <button className="inline-flex items-center gap-2 px-3 py-1.5 bg-stone-50 hover:bg-stone-100 border border-stone-200 rounded-xl text-xs font-semibold text-stone-700">
                <Calendar className="w-3.5 h-3.5 text-stone-400" />
                <span>{selectedDateRange}</span>
                <ChevronDown className="w-3 h-3 text-stone-400" />
              </button>
            </div>

            {/* Scope Toggle: State-wide / City View */}
            <div className="inline-flex p-0.5 bg-stone-100 rounded-xl border border-stone-200 text-xs font-semibold">
              <button
                onClick={() => setViewScope('state')}
                className={`px-3 py-1 rounded-lg transition-all ${
                  viewScope === 'state'
                    ? 'bg-white text-stone-900 shadow-2xs font-bold'
                    : 'text-stone-500 hover:text-stone-900'
                }`}
              >
                State-wide
              </button>
              <button
                onClick={() => setViewScope('city')}
                className={`px-3 py-1 rounded-lg transition-all ${
                  viewScope === 'city'
                    ? 'bg-white text-stone-900 shadow-2xs font-bold'
                    : 'text-stone-500 hover:text-stone-900'
                }`}
              >
                City View
              </button>
            </div>

            {/* Export PDF Button */}
            <button
              onClick={onExportPdf}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-white hover:bg-stone-50 text-stone-800 border border-stone-300 rounded-xl text-xs font-bold shadow-2xs transition-colors"
            >
              <Download className="w-3.5 h-3.5 text-stone-500" />
              <span>Export PDF</span>
            </button>
          </div>
        </div>

        {/* Charts Grid: 2x2 Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 1. Reported vs Resolved Civic Issues (Bar Chart) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-3">
                <div>
                  <div className="flex items-center gap-1.5">
                    <h3 className="text-sm font-bold text-stone-900">
                      Reported vs Resolved Civic Issues
                    </h3>
                    <Info className="w-3.5 h-3.5 text-stone-400" />
                  </div>
                  <p className="text-xs text-stone-500 font-medium mt-0.5">
                    Comparison across top districts
                  </p>
                </div>

                <div className="relative">
                  <select
                    value={selectedTopDistrictsCount}
                    onChange={(e) => setSelectedTopDistrictsCount(e.target.value)}
                    className="appearance-none bg-stone-50 border border-stone-200 rounded-lg px-2.5 py-1 text-[11px] font-semibold text-stone-700 pr-6 focus:outline-none"
                  >
                    <option value="Top 5 Districts">Top 5 Districts</option>
                    <option value="Top 10 Districts">Top 10 Districts</option>
                    <option value="All 24 Districts">All 24 Districts</option>
                  </select>
                  <ChevronDown className="w-3 h-3 text-stone-400 absolute right-1.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Chart Legend */}
              <div className="flex items-center gap-4 text-xs font-medium text-stone-600 mt-2 mb-4">
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded-xs bg-[#c2410c]" />
                  <span>Reported Issues</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-3 rounded-xs bg-[#78716c]" />
                  <span>Resolved Issues</span>
                </div>
              </div>

              {/* Interactive SVG Bar Chart */}
              <div className="h-56 w-full relative">
                <svg viewBox="0 0 500 220" className="w-full h-full">
                  {/* Horizontal Grid lines */}
                  <line x1="35" y1="20" x2="480" y2="20" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="5" y="24" fill="#94a3b8" fontSize="9">2.5K</text>

                  <line x1="35" y1="65" x2="480" y2="65" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="69" fill="#94a3b8" fontSize="9">2.0K</text>

                  <line x1="35" y1="110" x2="480" y2="110" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="114" fill="#94a3b8" fontSize="9">1.5K</text>

                  <line x1="35" y1="155" x2="480" y2="155" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="159" fill="#94a3b8" fontSize="9">500</text>

                  <line x1="35" y1="190" x2="480" y2="190" stroke="#cbd5e1" strokeWidth="1" />
                  <text x="25" y="193" fill="#94a3b8" fontSize="9">0</text>

                  {/* District Pairs */}
                  {topDistrictsComparison.map((item, idx) => {
                    const startX = 65 + idx * 85;
                    const reportedHeight = (item.reported / 2500) * 170;
                    const resolvedHeight = (item.resolved / 2500) * 170;
                    const isHovered = hoveredBarIndex === idx;

                    return (
                      <g
                        key={item.district}
                        onMouseEnter={() => setHoveredBarIndex(idx)}
                        onMouseLeave={() => setHoveredBarIndex(null)}
                        className="cursor-pointer"
                      >
                        {/* Reported Bar */}
                        <rect
                          x={startX}
                          y={190 - reportedHeight}
                          width="24"
                          height={reportedHeight}
                          rx="2"
                          fill={isHovered ? '#ea580c' : '#c2410c'}
                          className="transition-all duration-150"
                        />
                        {/* Resolved Bar */}
                        <rect
                          x={startX + 27}
                          y={190 - resolvedHeight}
                          width="24"
                          height={resolvedHeight}
                          rx="2"
                          fill={isHovered ? '#57534e' : '#78716c'}
                          className="transition-all duration-150"
                        />
                        {/* District Label */}
                        <text
                          x={startX + 25}
                          y="208"
                          textAnchor="middle"
                          fill="#475569"
                          fontSize="10"
                          fontWeight={isHovered ? '700' : '500'}
                        >
                          {item.district}
                        </text>
                      </g>
                    );
                  })}
                </svg>
              </div>
            </div>

            {/* Bottom 3 Metric summary cards */}
            <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-stone-100">
              <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                <p className="text-[10px] font-semibold text-stone-500">Total Reported</p>
                <div className="flex items-baseline gap-1 mt-0.5">
                  <span className="text-sm font-bold text-stone-900 font-mono">7,850</span>
                  <span className="text-[10px] font-semibold text-red-500">↓ 6.2%</span>
                </div>
              </div>

              <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                <p className="text-[10px] font-semibold text-stone-500">Total Resolved</p>
                <div className="flex items-baseline gap-1 mt-0.5">
                  <span className="text-sm font-bold text-stone-900 font-mono">6,470</span>
                  <span className="text-[10px] font-semibold text-emerald-600">↑ 14.8%</span>
                </div>
              </div>

              <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                <p className="text-[10px] font-semibold text-stone-500">Resolution Rate</p>
                <div className="flex items-baseline gap-1 mt-0.5">
                  <span className="text-sm font-bold text-stone-900 font-mono">82.6%</span>
                  <span className="text-[10px] font-semibold text-emerald-600">↑ 7.3%</span>
                </div>
              </div>
            </div>
          </div>

          {/* 2. Department-wise Distribution (Donut Chart) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-2">
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-bold text-stone-900">
                    Department-wise Distribution
                  </h3>
                  <Info className="w-3.5 h-3.5 text-stone-400" />
                </div>
              </div>
              <p className="text-xs text-stone-500 font-medium">
                Share of issues reported this week
              </p>

              {/* Donut & Legend side-by-side */}
              <div className="flex flex-col sm:flex-row items-center justify-around gap-4 mt-4">
                {/* Donut Chart SVG */}
                <div className="relative w-44 h-44 shrink-0 flex items-center justify-center">
                  <svg viewBox="0 0 160 160" className="w-full h-full -rotate-90">
                    {/* Circle segments:
                        Total 100%: circumference = 2 * PI * 60 ≈ 377
                        Waste 38% -> 143.2
                        Roadways 27% -> 101.8
                        Sewage 20% -> 75.4
                        Electricity 15% -> 56.55
                    */}
                    {/* Waste Management (38%) */}
                    <circle
                      cx="80"
                      cy="80"
                      r="60"
                      fill="transparent"
                      stroke="#c2410c"
                      strokeWidth="22"
                      strokeDasharray="143.2 377"
                      strokeDashoffset="0"
                      className="cursor-pointer hover:opacity-85 transition-opacity"
                    />
                    {/* Roadways (27%) */}
                    <circle
                      cx="80"
                      cy="80"
                      r="60"
                      fill="transparent"
                      stroke="#78716c"
                      strokeWidth="22"
                      strokeDasharray="101.8 377"
                      strokeDashoffset="-143.2"
                      className="cursor-pointer hover:opacity-85 transition-opacity"
                    />
                    {/* Sewage (20%) */}
                    <circle
                      cx="80"
                      cy="80"
                      r="60"
                      fill="transparent"
                      stroke="#0284c7"
                      strokeWidth="22"
                      strokeDasharray="75.4 377"
                      strokeDashoffset="-245"
                      className="cursor-pointer hover:opacity-85 transition-opacity"
                    />
                    {/* Electricity (15%) */}
                    <circle
                      cx="80"
                      cy="80"
                      r="60"
                      fill="transparent"
                      stroke="#d97706"
                      strokeWidth="22"
                      strokeDasharray="56.5 377"
                      strokeDashoffset="-320.4"
                      className="cursor-pointer hover:opacity-85 transition-opacity"
                    />
                  </svg>

                  {/* Center Total Count Text */}
                  <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                    <span className="text-xl font-bold text-stone-900 font-mono leading-none">
                      7,850
                    </span>
                    <span className="text-[10px] font-semibold text-stone-500 mt-1">
                      Total Issues
                    </span>
                  </div>
                </div>

                {/* Right Legend List */}
                <div className="space-y-3 w-full sm:w-auto">
                  {departmentStats.map((dept) => (
                    <div key={dept.name} className="flex items-center justify-between gap-6 text-xs">
                      <div className="flex items-center gap-2">
                        <span
                          className="w-2.5 h-2.5 rounded-full shrink-0"
                          style={{ backgroundColor: dept.color }}
                        />
                        <span className="text-stone-700 font-medium">{dept.name}</span>
                      </div>
                      <div className="flex items-center gap-1.5 font-mono">
                        <span className="font-bold text-stone-900">{dept.count.toLocaleString()}</span>
                        <span className="text-stone-400 text-[11px]">({dept.percentage}%)</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Bottom Insight notice banner */}
            <div className="mt-4 p-3 rounded-xl bg-[#fffbf6] border border-[#fed7aa]/50 flex items-start gap-2.5">
              <Info className="w-4 h-4 text-[#ea580c] shrink-0 mt-0.5" />
              <p className="text-xs text-stone-600 leading-relaxed font-medium">
                <strong className="text-stone-900">Waste Management</strong> issues are the highest contributor this week, showing a 5% increase in urban zones.
              </p>
            </div>
          </div>

          {/* 3. SLA Performance (Average Resolution Time) (Line Chart) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-2">
                <div>
                  <div className="flex items-center gap-1.5">
                    <h3 className="text-sm font-bold text-stone-900">
                      SLA Performance (Average Resolution Time)
                    </h3>
                    <Info className="w-3.5 h-3.5 text-stone-400" />
                  </div>
                  <p className="text-xs text-stone-500 font-medium mt-0.5">
                    Average time taken to resolve issues (in hours)
                  </p>
                </div>

                <div className="relative">
                  <select className="appearance-none bg-stone-50 border border-stone-200 rounded-lg px-2.5 py-1 text-[11px] font-semibold text-stone-700 pr-6 focus:outline-none">
                    <option>Last 7 Days</option>
                    <option>Last 14 Days</option>
                    <option>Last 30 Days</option>
                  </select>
                  <ChevronDown className="w-3 h-3 text-stone-400 absolute right-1.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Legend */}
              <div className="flex items-center gap-4 text-xs font-medium text-stone-600 mt-2 mb-3">
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-0.5 bg-[#c2410c]" />
                  <span>Average Resolution Time (hrs)</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-0.5 bg-[#dc2626] border-b border-dashed border-red-500" />
                  <span className="text-red-700 font-semibold">SLA Limit (72 hrs)</span>
                </div>
              </div>

              {/* Line Graph SVG */}
              <div className="h-56 w-full relative">
                <svg viewBox="0 0 500 200" className="w-full h-full">
                  {/* Grid Lines */}
                  <line x1="30" y1="20" x2="480" y2="20" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="5" y="24" fill="#94a3b8" fontSize="9">100</text>

                  <line x1="30" y1="50" x2="480" y2="50" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="54" fill="#94a3b8" fontSize="9">80</text>

                  {/* 72 hrs SLA Threshold line (dashed red) */}
                  <line x1="30" y1="62" x2="480" y2="62" stroke="#dc2626" strokeWidth="1.5" strokeDasharray="4,4" />
                  <text x="482" y="65" fill="#dc2626" fontSize="9" fontWeight="700">72</text>

                  <line x1="30" y1="80" x2="480" y2="80" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="84" fill="#94a3b8" fontSize="9">60</text>

                  <line x1="30" y1="120" x2="480" y2="120" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="124" fill="#94a3b8" fontSize="9">40</text>

                  <line x1="30" y1="160" x2="480" y2="160" stroke="#f1f5f9" strokeWidth="1" />
                  <text x="10" y="164" fill="#94a3b8" fontSize="9">20</text>

                  <line x1="30" y1="180" x2="480" y2="180" stroke="#cbd5e1" strokeWidth="1" />
                  <text x="15" y="184" fill="#94a3b8" fontSize="9">0</text>

                  {/* Polyline Path for SLA trends:
                      Y scale: 0 = 180, 100 = 20 (delta 160 px for 100 hrs -> 1.6 px per hr)
                      Mon (28.4) -> 180 - (28.4*1.6) = 134.5
                      Tue (35.2) -> 180 - 56.3 = 123.7
                      Wed (31.0) -> 180 - 49.6 = 130.4
                      Thu (24.8) -> 180 - 39.6 = 140.4
                      Fri (33.5) -> 180 - 53.6 = 126.4
                      Sat (26.1) -> 180 - 41.7 = 138.3
                      Sun (22.4) -> 180 - 35.8 = 144.2
                  */}
                  <polyline
                    fill="none"
                    stroke="#c2410c"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    points="60,134 125,123 190,130 255,140 320,126 385,138 450,144"
                  />

                  {/* Plot Dots and Day labels */}
                  {weeklySlaTrends.map((pt, i) => {
                    const cx = 60 + i * 65;
                    const cy = 180 - pt.avgHours * 1.6;
                    return (
                      <g key={pt.day} className="cursor-pointer group">
                        <circle cx={cx} cy={cy} r="5" fill="#c2410c" stroke="#ffffff" strokeWidth="2" />
                        <text x={cx} y="196" textAnchor="middle" fill="#64748b" fontSize="10" fontWeight="600">
                          {pt.day}
                        </text>
                      </g>
                    );
                  })}
                </svg>
              </div>
            </div>
          </div>

          {/* 4. Citizen Satisfaction Score (Radial Gauge & Ratings) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-2">
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-bold text-stone-900">
                    Citizen Satisfaction Score
                  </h3>
                  <Info className="w-3.5 h-3.5 text-stone-400" />
                </div>
              </div>
              <p className="text-xs text-stone-500 font-medium">
                Feedback from citizens on resolved issues
              </p>

              {/* Gauge and Breakdown Row */}
              <div className="grid grid-cols-1 sm:grid-cols-12 gap-4 items-center mt-3">
                {/* Left Semi-Circle Radial Gauge */}
                <div className="sm:col-span-5 flex flex-col items-center justify-center relative">
                  <div className="relative w-36 h-24 overflow-hidden">
                    <svg viewBox="0 0 140 80" className="w-full h-full">
                      {/* Background Arch */}
                      <path
                        d="M 15 75 A 55 55 0 0 1 125 75"
                        fill="none"
                        stroke="#f1f5f9"
                        strokeWidth="16"
                        strokeLinecap="round"
                      />
                      {/* Filled Arch (4.2 out of 5 = 84%) */}
                      <path
                        d="M 15 75 A 55 55 0 0 1 125 75"
                        fill="none"
                        stroke="#c2410c"
                        strokeWidth="16"
                        strokeDasharray="172 172"
                        strokeDashoffset="27.5"
                        strokeLinecap="round"
                      />
                    </svg>
                  </div>

                  <div className="text-center -mt-8">
                    <span className="text-3xl font-extrabold text-stone-900 font-mono">
                      4.2
                    </span>
                    <span className="text-xs font-semibold text-stone-400"> / 5</span>
                    <p className="text-[11px] font-semibold text-emerald-600 mt-0.5">
                      ★ 84% Positive Feedback
                    </p>
                  </div>
                </div>

                {/* Right Star Breakdown */}
                <div className="sm:col-span-7 space-y-2">
                  <div className="text-[11px] font-semibold text-stone-400 uppercase tracking-wider mb-1">
                    Rating Distribution
                  </div>
                  {citizenRatings.breakdown.map((r) => (
                    <div key={r.stars} className="flex items-center gap-2 text-xs">
                      <span className="w-12 text-stone-600 font-medium shrink-0">{r.label}</span>
                      <div className="flex-1 h-2.5 bg-stone-100 rounded-full overflow-hidden">
                        <div
                          className="h-full rounded-full transition-all duration-500"
                          style={{
                            width: `${r.percentage}%`,
                            backgroundColor: r.color,
                          }}
                        />
                      </div>
                      <span className="w-8 text-right font-mono font-semibold text-stone-700 text-[11px]">
                        {r.percentage}%
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
