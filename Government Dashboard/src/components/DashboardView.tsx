import React, { useMemo } from 'react';
import { CivicIssue, DistrictMetric, TabType } from '../types';
import { InteractiveMap } from './InteractiveMap';
import {
  FileText,
  CheckCircle,
  Clock,
  Trophy,
  AlertTriangle,
  Droplets,
  Wrench,
  Trash2,
  Zap,
  ArrowUpRight,
  ArrowDownRight,
  ArrowRight,
  RefreshCw,
  TrendingUp,
} from 'lucide-react';

interface DashboardViewProps {
  issues: CivicIssue[];
  districts: DistrictMetric[];
  selectedDistrict: DistrictMetric | null;
  onSelectDistrict: (district: DistrictMetric) => void;
  onOpenDistrictModal: (district?: DistrictMetric) => void;
  setActiveTab: (tab: TabType) => void;
  onSelectIssueForDetails: (issueId: string) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  issues,
  districts,
  selectedDistrict,
  onSelectDistrict,
  onOpenDistrictModal,
  setActiveTab,
  onSelectIssueForDetails,
}) => {
  const getPriorityBadgeClass = (priority: string) => {
    switch (priority) {
      case 'High':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'Critical':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'Medium':
        return 'bg-amber-50 text-amber-800 border-amber-200';
      case 'Low':
      default:
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    }
  };

  // Determine active district (default to Ranchi / state capital if none clicked)
  const activeDistrict = useMemo(() => {
    if (selectedDistrict) return selectedDistrict;
    return (
      districts.find((d) => d.id === 'ranchi' || d.name === 'Ranchi') ||
      districts[0] || {
        id: 'ranchi',
        name: 'Ranchi',
        totalIssues: 1420,
        resolved: 1110,
        inProgress: 310,
        openIssuesCount: 310,
        resolutionRate: 78.2,
        density: 'High',
        nodalOfficer: {
          name: 'Rahul Kumar Purwar, IAS',
          designation: 'Municipal Commissioner, RMC',
          phone: '+91 651 220 0011',
        },
      }
    );
  }, [selectedDistrict, districts]);

  // Filter issues for selected district, with high-quality localized fallback items
  const displayIssues = useMemo(() => {
    const normDist = activeDistrict.name.toLowerCase().replace(/[^a-z0-9]/g, '');
    const matched = issues.filter((issue) => {
      const normCity = (issue.city || '').toLowerCase().replace(/[^a-z0-9]/g, '');
      const normWard = (issue.ward || '').toLowerCase().replace(/[^a-z0-9]/g, '');
      return normCity.includes(normDist) || normWard.includes(normDist);
    });

    if (matched.length > 0) {
      return matched;
    }

    // Generate realistic localized issues for smaller municipal zones
    return [
      {
        id: `#JH-${Math.floor(1000 + Math.random() * 9000)}`,
        title: `Pothole & Surface Damage on Main Arterial Road`,
        category: 'Roadways' as const,
        ward: `Ward 04, Station Road, ${activeDistrict.name}`,
        timeAgo: '4 min ago',
        priority: 'High' as const,
      },
      {
        id: `#JH-${Math.floor(1000 + Math.random() * 9000)}`,
        title: `High Tension Cable Sagging Near Market Complex`,
        category: 'Electricity' as const,
        ward: `Ward 09, Commercial Chowk, ${activeDistrict.name}`,
        timeAgo: '18 min ago',
        priority: 'Medium' as const,
      },
      {
        id: `#JH-${Math.floor(1000 + Math.random() * 9000)}`,
        title: `Municipal Solid Waste Overflow Near Community Bin`,
        category: 'Waste Management' as const,
        ward: `Ward 02, Residential Colony, ${activeDistrict.name}`,
        timeAgo: '42 min ago',
        priority: 'Medium' as const,
      },
      {
        id: `#JH-${Math.floor(1000 + Math.random() * 9000)}`,
        title: `Underground Sewer Choke & Drainage Backflow`,
        category: 'Sewage' as const,
        ward: `Ward 11, Bypass Road, ${activeDistrict.name}`,
        timeAgo: '2 hours ago',
        priority: 'Critical' as const,
      },
      {
        id: `#JH-${Math.floor(1000 + Math.random() * 9000)}`,
        title: `LED Streetlight Circuit Breakdown on School Road`,
        category: 'Electricity' as const,
        ward: `Ward 07, Civil Lines, ${activeDistrict.name}`,
        timeAgo: '5 hours ago',
        priority: 'Low' as const,
      },
    ];
  }, [activeDistrict, issues]);

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top 5 KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Total Issues Card */}
        <div className="bg-white/95 backdrop-blur-md p-4.5 rounded-3xl border border-stone-200/50 transition-all hover:bg-white flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl glass-pill flex items-center justify-center text-[#ea580c] shrink-0">
            <FileText className="w-5.5 h-5.5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-600">Total Issues</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5 font-mono">
              14,820
            </h3>
            <div className="flex items-center gap-1 mt-1 text-xs font-semibold text-emerald-600">
              <ArrowUpRight className="w-3.5 h-3.5" />
              <span>12.5%</span>
              <span className="text-stone-400 font-normal text-[11px]">vs last week</span>
            </div>
          </div>
        </div>

        {/* Resolved Card */}
        <div className="bg-white/95 backdrop-blur-md p-4.5 rounded-3xl border border-stone-200/50 transition-all hover:bg-white flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl glass-pill flex items-center justify-center text-emerald-600 shrink-0">
            <CheckCircle className="w-5.5 h-5.5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-600">Resolved</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5 font-mono">
              11,240
            </h3>
            <div className="flex items-center gap-1 mt-1 text-xs font-semibold text-emerald-600">
              <ArrowUpRight className="w-3.5 h-3.5" />
              <span>15.3%</span>
              <span className="text-stone-400 font-normal text-[11px]">vs last week</span>
            </div>
          </div>
        </div>

        {/* In Progress Card */}
        <div className="bg-white/95 backdrop-blur-md p-4.5 rounded-3xl border border-stone-200/50 transition-all hover:bg-white flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl glass-pill flex items-center justify-center text-amber-600 shrink-0">
            <Clock className="w-5.5 h-5.5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-600">In Progress</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5 font-mono">
              3,580
            </h3>
            <div className="flex items-center gap-1 mt-1 text-xs font-semibold text-red-500">
              <ArrowDownRight className="w-3.5 h-3.5" />
              <span>3.1%</span>
              <span className="text-stone-400 font-normal text-[11px]">vs last week</span>
            </div>
          </div>
        </div>

        {/* Top Performer Card */}
        <div className="bg-white/95 backdrop-blur-md p-4.5 rounded-3xl border border-stone-200/50 transition-all hover:bg-white flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl glass-pill flex items-center justify-center text-emerald-600 shrink-0">
            <Trophy className="w-5.5 h-5.5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-600">Top Performer</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5">
              Bokaro
            </h3>
            <p className="mt-1 text-xs font-medium text-emerald-600">
              <span className="font-bold">124</span> open issues
            </p>
          </div>
        </div>

        {/* Critical Hotspot Card */}
        <div className="bg-white/95 backdrop-blur-md p-4.5 rounded-3xl border border-stone-200/50 transition-all hover:bg-white flex items-start gap-3.5">
          <div className="w-11 h-11 rounded-2xl glass-pill flex items-center justify-center text-red-600 shrink-0">
            <AlertTriangle className="w-5.5 h-5.5" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-600">Critical Hotspot</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5">
              Ranchi
            </h3>
            <p className="mt-1 text-xs font-medium text-red-600">
              <span className="font-bold">1,420</span> open issues
            </p>
          </div>
        </div>
      </div>

      {/* Main Row: Map on Left (65%) + Dynamic City Telemetry & Local Live Grievances on Right (35%) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Interactive Map */}
        <div className="lg:col-span-8">
          <InteractiveMap
            districts={districts}
            selectedDistrict={selectedDistrict}
            onSelectDistrict={onSelectDistrict}
            onOpenDistrictModal={onOpenDistrictModal}
          />
        </div>

        {/* Right Column: Dynamic City Telemetry & Local Live Grievances */}
        <div className="lg:col-span-4 h-[520px] lg:h-[580px] flex flex-col">
          <div className="bg-white/95 backdrop-blur-md rounded-3xl border border-stone-200/80 shadow-xs flex-1 flex flex-col overflow-hidden">
            {/* ─── SECTION 1: City Overview & Telemetry ─── */}
            <div className="p-4 bg-stone-50/80 border-b border-stone-200/80 shrink-0">
              {/* Header with City Name & Reset */}
              <div className="flex items-center justify-between gap-2 mb-3">
                <div className="min-w-0">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#ea580c] shrink-0" />
                    <h3 className="text-sm font-extrabold text-stone-900 tracking-tight truncate">
                      {activeDistrict.name} Municipal Corp
                    </h3>
                  </div>
                  <p className="text-[11px] text-stone-500 font-medium truncate mt-0.5">
                    {selectedDistrict
                      ? 'Local Civic Telemetry & Grievance Feed'
                      : 'State Capital • Click map to switch district'}
                  </p>
                </div>

                {selectedDistrict && selectedDistrict.id !== 'ranchi' && selectedDistrict.name !== 'Ranchi' ? (
                  <button
                    onClick={() => {
                      const ranchi = districts.find((d) => d.id === 'ranchi' || d.name === 'Ranchi') || districts[0];
                      onSelectDistrict(ranchi);
                    }}
                    className="text-[10px] font-bold px-2 py-1 bg-white hover:bg-stone-100 text-stone-700 border border-stone-200 rounded-lg shadow-xs transition-colors shrink-0 flex items-center gap-1"
                    title="Reset to State Capital (Ranchi)"
                  >
                    <RefreshCw className="w-2.5 h-2.5" />
                    <span>Reset</span>
                  </button>
                ) : (
                  <span className="text-[10px] font-extrabold px-2 py-0.5 bg-blue-50 text-blue-700 border border-blue-200/70 rounded-md shrink-0">
                    Capital
                  </span>
                )}
              </div>

              {/* 2x2 Quick Metric Micro-Cards */}
              <div className="grid grid-cols-2 gap-2 mb-3">
                {/* Total Reported */}
                <div className="bg-white p-2 rounded-xl border border-stone-200/80 shadow-xs">
                  <span className="text-[10px] font-semibold text-stone-500 block">Total Reported</span>
                  <span className="text-sm font-bold text-stone-900 font-mono">
                    {activeDistrict.totalIssues?.toLocaleString() || '1,420'}
                  </span>
                </div>

                {/* Resolved */}
                <div className="bg-white p-2 rounded-xl border border-emerald-200/70 bg-emerald-50/20 shadow-xs">
                  <span className="text-[10px] font-semibold text-emerald-700 block">Resolved Rate</span>
                  <span className="text-sm font-bold text-emerald-700 font-mono">
                    {activeDistrict.resolved?.toLocaleString() || '1,110'}{' '}
                    <span className="text-[10px] font-medium text-emerald-600">
                      ({activeDistrict.resolutionRate || 78.4}%)
                    </span>
                  </span>
                </div>

                {/* Active Pending */}
                <div className="bg-white p-2 rounded-xl border border-amber-200/70 bg-amber-50/20 shadow-xs">
                  <span className="text-[10px] font-semibold text-amber-700 block">Active Pending</span>
                  <span className="text-sm font-bold text-amber-700 font-mono">
                    {(activeDistrict.openIssuesCount || activeDistrict.inProgress || 310)?.toLocaleString()}
                  </span>
                </div>

                {/* Avg Turnaround Time */}
                <div className="bg-white p-2 rounded-xl border border-stone-200/80 shadow-xs">
                  <span className="text-[10px] font-semibold text-stone-500 block">Avg Resolution</span>
                  <span className="text-sm font-bold text-stone-800 font-mono">
                    1.8 Days
                  </span>
                </div>
              </div>

              {/* Category Breakdown Stacked Mini-Bar */}
              <div className="space-y-1.5 mb-2.5">
                <div className="flex items-center justify-between text-[10px] font-bold text-stone-600">
                  <span>Category Breakdown</span>
                  <span className="text-stone-400 font-normal">Civic Split</span>
                </div>
                {/* Progress bar */}
                <div className="h-2 w-full bg-stone-200 rounded-full overflow-hidden flex">
                  <div className="bg-blue-600 h-full" style={{ width: '38%' }} title="Roadways: 38%" />
                  <div className="bg-amber-500 h-full" style={{ width: '28%' }} title="Electricity: 28%" />
                  <div className="bg-orange-500 h-full" style={{ width: '20%' }} title="Waste: 20%" />
                  <div className="bg-sky-500 h-full" style={{ width: '14%' }} title="Sewage & Water: 14%" />
                </div>
                {/* Mini Legend */}
                <div className="flex items-center justify-between text-[9.5px] text-stone-500 font-medium">
                  <span className="flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-600" />
                    Roads 38%
                  </span>
                  <span className="flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
                    Power 28%
                  </span>
                  <span className="flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-orange-500" />
                    Waste 20%
                  </span>
                  <span className="flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-sky-500" />
                    Water 14%
                  </span>
                </div>
              </div>

              {/* Responsible Authority / Officer in Charge */}
              <div className="pt-2 border-t border-stone-200/60 flex items-center justify-between text-[10.5px]">
                <span className="text-stone-500 font-semibold truncate">Responsible Authority:</span>
                <span className="font-bold text-stone-800 truncate ml-1">
                  {activeDistrict.nodalOfficer?.name || 'Rahul Kumar Purwar, IAS'}
                </span>
              </div>
            </div>

            {/* ─── SECTION 2: Live Issues in Selected City ─── */}
            <div className="flex-1 flex flex-col min-h-0">
              {/* Header */}
              <div className="px-4 py-2.5 bg-white flex items-center justify-between border-b border-stone-100 shrink-0">
                <div className="flex items-center gap-1.5">
                  <h4 className="text-xs font-bold text-stone-900">
                    Live Grievances: {activeDistrict.name}
                  </h4>
                  <span className="text-[10px] font-mono text-stone-400 font-bold">
                    ({displayIssues.length})
                  </span>
                </div>
                <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10.5px] font-extrabold text-emerald-700 bg-emerald-50 border border-emerald-200/60">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-ping shrink-0" />
                  Live Feed
                </span>
              </div>

              {/* Scrollable Issue List */}
              <div className="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-2">
                {displayIssues.map((issue) => {
                  let IconComponent = Wrench;
                  let iconBg = 'bg-stone-100 text-stone-700';
                  if (issue.category === 'Water Supply' || issue.category === 'Sewage') {
                    IconComponent = Droplets;
                    iconBg = 'bg-blue-50 text-blue-600';
                  } else if (issue.category === 'Waste Management') {
                    IconComponent = Trash2;
                    iconBg = 'bg-orange-50 text-orange-600';
                  } else if (issue.category === 'Electricity' || issue.category === 'Street Lights') {
                    IconComponent = Zap;
                    iconBg = 'bg-amber-50 text-amber-600';
                  } else if (issue.category === 'Roadways') {
                    IconComponent = Wrench;
                    iconBg = 'bg-stone-100 text-stone-700';
                  }

                  return (
                    <div
                      key={issue.id}
                      onClick={() => {
                        onSelectIssueForDetails(issue.id);
                        setActiveTab('details');
                      }}
                      className="p-2.5 group cursor-pointer bg-white hover:bg-stone-50/90 rounded-2xl border border-stone-200/70 shadow-xs hover:shadow-md transition-all"
                    >
                      <div className="flex items-start gap-2.5">
                        <div className={`w-7.5 h-7.5 rounded-xl flex items-center justify-center shrink-0 mt-0.5 ${iconBg}`}>
                          <IconComponent className="w-3.5 h-3.5" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-1">
                            <h5 className="text-xs font-bold text-stone-900 truncate group-hover:text-[#ea580c] transition-colors">
                              {issue.title}
                            </h5>
                            <span className="text-[9.5px] text-stone-400 shrink-0 whitespace-nowrap">
                              {issue.timeAgo}
                            </span>
                          </div>
                          <p className="text-[10.5px] text-stone-500 truncate mt-0.5">
                            {issue.ward}
                          </p>
                          <div className="mt-1.5 flex items-center justify-between gap-2">
                            <span className={`inline-block text-[9px] font-bold px-1.5 py-0.5 rounded-md border ${getPriorityBadgeClass(issue.priority)}`}>
                              {issue.priority} Priority
                            </span>
                            <span className="text-[9.5px] font-mono text-stone-400 font-bold">
                              {issue.id}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Bottom Footer Link */}
              <div className="p-2.5 bg-stone-50/80 border-t border-stone-200/80 text-center shrink-0">
                <button
                  onClick={() => setActiveTab('details')}
                  className="inline-flex items-center gap-1.5 text-xs font-bold text-[#ea580c] hover:text-[#c2410c] transition-colors"
                >
                  <span>View All {activeDistrict.name} Issues ({activeDistrict.totalIssues || displayIssues.length})</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Summary Bar: Issue Summary This Week */}
      <div className="bg-white/95 backdrop-blur-md rounded-3xl p-5 flex flex-col lg:flex-row items-center justify-between gap-6 border border-stone-200/50">
        {/* Left Title */}
        <div className="shrink-0 text-center lg:text-left">
          <h3 className="text-sm font-bold text-stone-900">
            Issue Summary <span className="font-medium text-stone-500">This Week</span>
          </h3>
          <p className="text-xs text-stone-500 font-medium mt-0.5">
            Overview of key metrics across Jharkhand
          </p>
        </div>

        {/* 4 Metric items */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 w-full lg:w-auto lg:divide-x lg:divide-stone-900/10">
          {/* New Issues */}
          <div className="flex items-center gap-3 lg:px-6">
            <div className="w-10 h-10 rounded-2xl glass-pill flex items-center justify-center text-[#ea580c] shrink-0">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-semibold text-stone-500">New Issues</p>
              <div className="flex items-baseline gap-1.5 mt-0.5">
                <span className="text-lg font-bold text-stone-900 font-mono">2,450</span>
                <span className="text-[11px] font-semibold text-emerald-600">↑ 18.2%</span>
              </div>
            </div>
          </div>

          {/* Resolved */}
          <div className="flex items-center gap-3 lg:px-6">
            <div className="w-10 h-10 rounded-2xl glass-pill flex items-center justify-center text-emerald-600 shrink-0">
              <CheckCircle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-semibold text-stone-500">Resolved</p>
              <div className="flex items-baseline gap-1.5 mt-0.5">
                <span className="text-lg font-bold text-stone-900 font-mono">1,890</span>
                <span className="text-[11px] font-semibold text-emerald-600">↑ 22.1%</span>
              </div>
            </div>
          </div>

          {/* Avg Resolution Time */}
          <div className="flex items-center gap-3 lg:px-6">
            <div className="w-10 h-10 rounded-2xl glass-pill flex items-center justify-center text-stone-700 shrink-0">
              <RefreshCw className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-semibold text-stone-500">Avg. Resolution Time</p>
              <div className="flex items-baseline gap-1.5 mt-0.5">
                <span className="text-lg font-bold text-stone-900 font-mono">2.4 Days</span>
                <span className="text-[11px] font-semibold text-emerald-600">↓ 8.5%</span>
              </div>
            </div>
          </div>

          {/* Escalated Issues */}
          <div className="flex items-center gap-3 lg:px-6">
            <div className="w-10 h-10 rounded-2xl glass-pill flex items-center justify-center text-red-600 shrink-0">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[11px] font-semibold text-stone-500">Escalated Issues</p>
              <div className="flex items-baseline gap-1.5 mt-0.5">
                <span className="text-lg font-bold text-stone-900 font-mono">320</span>
                <span className="text-[11px] font-semibold text-red-600">↑ 12.4%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
