import React from 'react';
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

        {/* Right Column: Live Issue Feed */}
        <div className="lg:col-span-4 h-[520px] lg:h-[580px] flex flex-col">
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex-1 flex flex-col justify-between overflow-hidden">
            <div>
              <div className="flex items-center justify-between pb-3 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Live Issue Feed</h3>
                  <p className="text-xs text-stone-500 font-medium mt-0.5">Real-time crowdsourced grievances</p>
                </div>
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping shrink-0" />
                  Live
                </span>
              </div>

              {/* Live Feed List */}
              <div className="divide-y divide-stone-100 overflow-y-auto max-h-[380px] lg:max-h-[420px] pr-1 custom-scrollbar mt-1">
                {issues.slice(0, 6).map((issue) => {
                  let IconComponent = Wrench;
                  let iconBg = 'bg-amber-50 text-amber-600';
                  if (issue.category === 'Water Supply') {
                    IconComponent = Droplets;
                    iconBg = 'bg-blue-50 text-blue-600';
                  } else if (issue.category === 'Waste Management') {
                    IconComponent = Trash2;
                    iconBg = 'bg-orange-50 text-orange-600';
                  } else if (issue.category === 'Electricity') {
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
                      className="py-3 group cursor-pointer hover:bg-stone-50/80 -mx-1 px-2 rounded-xl transition-colors"
                    >
                      <div className="flex items-start gap-3">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5 ${iconBg}`}>
                          <IconComponent className="w-4 h-4" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-1">
                            <h4 className="text-xs font-bold text-stone-900 truncate group-hover:text-[#ea580c] transition-colors">
                              {issue.title}
                            </h4>
                            <span className="text-[10px] text-stone-400 shrink-0 whitespace-nowrap">{issue.timeAgo}</span>
                          </div>
                          <p className="text-[11px] text-stone-500 truncate mt-0.5">
                            {issue.ward || issue.city}
                          </p>
                          <div className="mt-1.5 flex items-center gap-2">
                            <span className={`inline-block text-[10px] font-semibold px-2 py-0.5 rounded-md border ${getPriorityBadgeClass(issue.priority)}`}>
                              {issue.priority} Priority
                            </span>
                            <span className="text-[10px] font-mono text-stone-400">
                              {issue.id}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Bottom View All Link */}
            <div className="pt-3 border-t border-stone-100 text-center shrink-0">
              <button
                onClick={() => setActiveTab('details')}
                className="inline-flex items-center gap-1.5 text-xs font-bold text-[#ea580c] hover:text-[#c2410c] transition-colors"
              >
                <span>View All Active Issues ({issues.length})</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
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
