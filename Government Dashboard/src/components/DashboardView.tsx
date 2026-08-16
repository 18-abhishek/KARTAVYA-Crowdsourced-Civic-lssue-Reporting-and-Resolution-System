import React, { useState } from 'react';
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
  SlidersHorizontal,
  ChevronDown,
  Calendar,
  Sparkles,
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
  const [filterDistrict, setFilterDistrict] = useState('All Districts');
  const [filterCategory, setFilterCategory] = useState('All Categories');
  const [filterPriority, setFilterPriority] = useState('All Priorities');
  const [filterDateRange, setFilterDateRange] = useState('Last 7 Days');
  const [filterAppliedToast, setFilterAppliedToast] = useState(false);

  const handleApplyFilters = () => {
    setFilterAppliedToast(true);
    setTimeout(() => setFilterAppliedToast(false), 3000);
    // If district filter is chosen, select it
    if (filterDistrict !== 'All Districts') {
      const d = districts.find((dist) => dist.name.toLowerCase().includes(filterDistrict.toLowerCase()));
      if (d) onSelectDistrict(d);
    }
  };

  const getPriorityBadgeClass = (priority: string) => {
    switch (priority) {
      case 'High':
      case 'Critical':
        return 'bg-red-50 text-red-700 border border-red-200';
      case 'Medium':
        return 'bg-amber-50 text-amber-800 border border-amber-200';
      case 'Low':
      default:
        return 'bg-orange-50 text-orange-700 border border-orange-200';
    }
  };

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'Water Supply':
        return <Droplets className="w-4 h-4 text-red-500" />;
      case 'Roadways':
        return <Wrench className="w-4 h-4 text-amber-600" />;
      case 'Waste Management':
        return <Trash2 className="w-4 h-4 text-orange-600" />;
      case 'Electricity':
        return <Zap className="w-4 h-4 text-amber-500" />;
      default:
        return <Droplets className="w-4 h-4 text-blue-500" />;
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top 5 KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Total Issues Card */}
        <div className="bg-white p-5 rounded-2xl border border-stone-200 shadow-xs hover:border-stone-300 transition-colors flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#ea580c] shrink-0">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-500">Total Issues</p>
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
        <div className="bg-white p-5 rounded-2xl border border-stone-200 shadow-xs hover:border-stone-300 transition-colors flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600 shrink-0">
            <CheckCircle className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-500">Resolved</p>
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
        <div className="bg-white p-5 rounded-2xl border border-stone-200 shadow-xs hover:border-stone-300 transition-colors flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 border border-amber-100 flex items-center justify-center text-amber-600 shrink-0">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-500">In Progress</p>
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
        <div className="bg-white p-5 rounded-2xl border border-stone-200 shadow-xs hover:border-stone-300 transition-colors flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600 shrink-0">
            <Trophy className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-500">Top Performer</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5">
              Bokaro
            </h3>
            <p className="mt-1 text-xs font-medium text-emerald-600">
              <span className="font-bold">124</span> open issues
            </p>
          </div>
        </div>

        {/* Critical Hotspot Card */}
        <div className="bg-white p-5 rounded-2xl border border-stone-200 shadow-xs hover:border-stone-300 transition-colors flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-red-50 border border-red-100 flex items-center justify-center text-red-600 shrink-0">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold text-stone-500">Critical Hotspot</p>
            <h3 className="text-2xl font-bold text-stone-900 tracking-tight mt-0.5">
              Ranchi
            </h3>
            <p className="mt-1 text-xs font-medium text-red-600">
              <span className="font-bold">1,420</span> open issues
            </p>
          </div>
        </div>
      </div>

      {/* Main Row: Map on Left (65%) + Live Feed & Quick Filters on Right (35%) */}
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

        {/* Right Column: Live Issue Feed & Quick Filters */}
        <div className="lg:col-span-4 space-y-6">
          {/* Card 1: Live Issue Feed */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-stone-100">
              <div>
                <h3 className="text-sm font-bold text-stone-900">Live Issue Feed</h3>
                <p className="text-xs text-stone-500 font-medium mt-0.5">Real-time incoming issues</p>
              </div>
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping shrink-0" />
                Live
              </span>
            </div>

            {/* Live Feed List */}
            <div className="divide-y divide-stone-100 mt-2">
              {/* Item 1 */}
              <div
                onClick={() => {
                  onSelectIssueForDetails('#JH-9821');
                  setActiveTab('details');
                }}
                className="py-3 group cursor-pointer hover:bg-stone-50/80 -mx-2 px-2 rounded-xl transition-colors"
              >
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-red-50 flex items-center justify-center shrink-0 mt-0.5">
                    <Droplets className="w-4 h-4 text-red-500" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1">
                      <h4 className="text-xs font-bold text-stone-900 truncate group-hover:text-[#ea580c] transition-colors">
                        Water Supply Problem
                      </h4>
                      <span className="text-[10px] text-stone-400 shrink-0 whitespace-nowrap">2 min ago</span>
                    </div>
                    <p className="text-[11px] text-stone-500 truncate mt-0.5">
                      Ranchi Municipal Corporation
                    </p>
                    <div className="mt-1.5">
                      <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded-md bg-red-50 text-red-700 border border-red-100">
                        High Priority
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Item 2 */}
              <div
                onClick={() => {
                  onSelectIssueForDetails('#JH-9530');
                  setActiveTab('details');
                }}
                className="py-3 group cursor-pointer hover:bg-stone-50/80 -mx-2 px-2 rounded-xl transition-colors"
              >
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-amber-50 flex items-center justify-center shrink-0 mt-0.5">
                    <Wrench className="w-4 h-4 text-amber-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1">
                      <h4 className="text-xs font-bold text-stone-900 truncate group-hover:text-[#ea580c] transition-colors">
                        Road Damage
                      </h4>
                      <span className="text-[10px] text-stone-400 shrink-0 whitespace-nowrap">5 min ago</span>
                    </div>
                    <p className="text-[11px] text-stone-500 truncate mt-0.5">
                      Jamshedpur Notified Area
                    </p>
                    <div className="mt-1.5">
                      <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded-md bg-amber-50 text-amber-800 border border-amber-100">
                        Medium Priority
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Item 3 */}
              <div
                onClick={() => {
                  onSelectIssueForDetails('#JH-9819');
                  setActiveTab('details');
                }}
                className="py-3 group cursor-pointer hover:bg-stone-50/80 -mx-2 px-2 rounded-xl transition-colors"
              >
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-full bg-orange-50 flex items-center justify-center shrink-0 mt-0.5">
                    <Trash2 className="w-4 h-4 text-orange-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1">
                      <h4 className="text-xs font-bold text-stone-900 truncate group-hover:text-[#ea580c] transition-colors">
                        Garbage Collection Missed
                      </h4>
                      <span className="text-[10px] text-stone-400 shrink-0 whitespace-nowrap">7 min ago</span>
                    </div>
                    <p className="text-[11px] text-stone-500 truncate mt-0.5">
                      Dhanbad Municipal Corporation
                    </p>
                    <div className="mt-1.5">
                      <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded-md bg-orange-50 text-orange-700 border border-orange-100">
                        Low Priority
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Bottom View All Link */}
            <div className="mt-3 pt-3 border-t border-stone-100 text-center">
              <button
                onClick={() => setActiveTab('details')}
                className="inline-flex items-center gap-1.5 text-xs font-bold text-[#ea580c] hover:text-[#c2410c] transition-colors"
              >
                <span>View All Issues</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          {/* Card 2: Quick Filters */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-stone-100">
              <h3 className="text-sm font-bold text-stone-900">Quick Filters</h3>
              <SlidersHorizontal className="w-4 h-4 text-stone-400" />
            </div>

            <div className="space-y-3 mt-3">
              {/* Row 1: Districts & Categories */}
              <div className="grid grid-cols-2 gap-3">
                <div className="relative">
                  <select
                    value={filterDistrict}
                    onChange={(e) => setFilterDistrict(e.target.value)}
                    className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
                  >
                    <option value="All Districts">All Districts</option>
                    {districts.map((d) => (
                      <option key={d.id} value={d.name}>
                        {d.name}
                      </option>
                    ))}
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <div className="relative">
                  <select
                    value={filterCategory}
                    onChange={(e) => setFilterCategory(e.target.value)}
                    className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
                  >
                    <option value="All Categories">All Categories</option>
                    <option value="Roadways">Roadways / Potholes</option>
                    <option value="Electricity">Electricity & Lighting</option>
                    <option value="Sewage">Sewage & Drainage</option>
                    <option value="Waste Management">Waste Management</option>
                    <option value="Water Supply">Water Supply</option>
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Row 2: Priorities & Date */}
              <div className="grid grid-cols-2 gap-3">
                <div className="relative">
                  <select
                    value={filterPriority}
                    onChange={(e) => setFilterPriority(e.target.value)}
                    className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
                  >
                    <option value="All Priorities">All Priorities</option>
                    <option value="Critical">Critical</option>
                    <option value="High">High</option>
                    <option value="Medium">Medium</option>
                    <option value="Low">Low</option>
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                <div className="relative">
                  <select
                    value={filterDateRange}
                    onChange={(e) => setFilterDateRange(e.target.value)}
                    className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
                  >
                    <option value="Last 7 Days">Last 7 Days</option>
                    <option value="Today">Today (24 Hours)</option>
                    <option value="Last 30 Days">Last 30 Days</option>
                    <option value="This Quarter">This Quarter</option>
                  </select>
                  <Calendar className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Apply Filters Button */}
              <button
                onClick={handleApplyFilters}
                className="w-full mt-2 py-2.5 px-4 bg-[#ea580c] hover:bg-[#c2410c] text-white text-xs font-bold rounded-xl shadow-xs transition-all flex items-center justify-center gap-2 active:scale-98"
              >
                <SlidersHorizontal className="w-3.5 h-3.5" />
                <span>Apply Filters</span>
              </button>

              {filterAppliedToast && (
                <p className="text-[11px] font-semibold text-emerald-600 text-center animate-in fade-in">
                  ✓ Filter criteria applied to live map & feeds
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Summary Bar: Issue Summary This Week */}
      <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col lg:flex-row items-center justify-between gap-6">
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
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 w-full lg:w-auto lg:divide-x lg:divide-stone-100">
          {/* New Issues */}
          <div className="flex items-center gap-3 lg:px-6">
            <div className="w-10 h-10 rounded-xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#ea580c] shrink-0">
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
            <div className="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600 shrink-0">
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
            <div className="w-10 h-10 rounded-xl bg-stone-100 border border-stone-200 flex items-center justify-center text-stone-700 shrink-0">
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
            <div className="w-10 h-10 rounded-xl bg-red-50 border border-red-100 flex items-center justify-center text-red-600 shrink-0">
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
