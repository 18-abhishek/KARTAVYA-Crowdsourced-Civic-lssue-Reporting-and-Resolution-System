import React, { useState } from 'react';
import { CivicIssue } from '../types';
import {
  AlertTriangle,
  Clock,
  CheckCircle,
  Building2,
  Phone,
  ArrowRight,
  RotateCcw,
  ChevronDown,
  ShieldAlert,
  ArrowUpRight,
  IndianRupee,
  MoreVertical,
  SlidersHorizontal,
  FileWarning,
  Flame,
  Check,
  Building,
  User,
  ExternalLink,
  ChevronRight,
  Inbox,
  CheckCircle2,
  Activity,
  MessageSquareQuote,
  LayoutDashboard,
} from 'lucide-react';

interface NotificationsViewProps {
  issues: CivicIssue[];
  onIssueFormalWarning: (issue: CivicIssue) => void;
  onEscalateCommissioner: (issue: CivicIssue) => void;
  onImposePenalty: (issue: CivicIssue) => void;
  onSelectIssueForDetails: (issueId: string) => void;
}

export const NotificationsView: React.FC<NotificationsViewProps> = ({
  issues,
  onIssueFormalWarning,
  onEscalateCommissioner,
  onImposePenalty,
  onSelectIssueForDetails,
}) => {
  const [selectedDeptFilter, setSelectedDeptFilter] = useState('All Departments');
  const [selectedBreachFilter, setSelectedBreachFilter] = useState('> 80% Time Elapsed');
  const [selectedStatusFilter, setSelectedStatusFilter] = useState('All Status');
  const [sortBy, setSortBy] = useState('Time Elapsed (High to Low)');
  const [activeSidebarTab, setActiveSidebarTab] = useState('alerts');

  // Filter critical / high breach items
  const breachIssues = issues.filter((issue) => {
    if (selectedDeptFilter !== 'All Departments' && !issue.assignedDept.includes(selectedDeptFilter)) {
      return false;
    }
    if (selectedBreachFilter === '> 80% Time Elapsed' && issue.timeElapsedPercent < 80) {
      return false;
    }
    return true;
  });

  const sidebarLinks = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'inflow', label: 'Issues Inflow', icon: Inbox },
    { id: 'resolution', label: 'Resolution', icon: CheckCircle2 },
    { id: 'sla', label: 'SLA Performance', icon: Activity },
    { id: 'departments', label: 'Departments', icon: Building2 },
    { id: 'feedback', label: 'Citizen Feedback', icon: MessageSquareQuote },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Header & Subtitle */}
      <div>
        <h2 className="text-xl font-bold text-stone-900 tracking-tight">
          SLA Escalation &amp; Warning Notification Center
        </h2>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Real-time alerts for critical SLA breaches and zero-progress issues
        </p>
      </div>

      {/* Critical SLA Red Banner */}
      <div className="bg-[#fef2f2] border border-[#fecaca] rounded-2xl p-5 shadow-xs flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-[#dc2626] text-white flex items-center justify-center shrink-0 shadow-sm">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-stone-900 leading-snug">
              <span className="text-[#dc2626] font-mono text-lg mr-1">12</span>
              Critical Issues Exceeded 80% SLA Time Limit with No Progress
            </h3>
            <p className="text-xs text-[#991b1b] font-medium mt-0.5">
              Immediate action required to avoid service penalties and citizen dissatisfaction.
            </p>
          </div>
        </div>

        <button
          onClick={() => {
            setSelectedBreachFilter('> 80% Time Elapsed');
          }}
          className="inline-flex items-center gap-2 px-4 py-2 bg-white hover:bg-red-50 text-[#dc2626] border border-[#fca5a5] rounded-xl text-xs font-bold shrink-0 transition-colors shadow-2xs"
        >
          <span>View All Critical Issues</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* 4 SLA Tier Status Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Critical Card */}
        <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs flex items-center gap-4">
          <div className="w-11 h-11 rounded-xl bg-red-100/70 border border-red-200 flex items-center justify-center text-red-600 shrink-0">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-stone-900 font-mono">12</span>
            </div>
            <p className="text-xs font-bold text-stone-700">Critical ( &gt; 80% )</p>
            <p className="text-[11px] font-semibold text-red-600">No Progress</p>
          </div>
        </div>

        {/* High Card */}
        <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs flex items-center gap-4">
          <div className="w-11 h-11 rounded-xl bg-amber-100/70 border border-amber-200 flex items-center justify-center text-amber-600 shrink-0">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-stone-900 font-mono">18</span>
            </div>
            <p className="text-xs font-bold text-stone-700">High ( 60% - 80% )</p>
            <p className="text-[11px] font-semibold text-amber-600">At Risk</p>
          </div>
        </div>

        {/* Medium Card */}
        <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs flex items-center gap-4">
          <div className="w-11 h-11 rounded-xl bg-yellow-100/70 border border-yellow-200 flex items-center justify-center text-yellow-700 shrink-0">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-stone-900 font-mono">25</span>
            </div>
            <p className="text-xs font-bold text-stone-700">Medium ( 40% - 60% )</p>
            <p className="text-[11px] font-semibold text-yellow-700">Monitor</p>
          </div>
        </div>

        {/* Within SLA Card */}
        <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs flex items-center gap-4">
          <div className="w-11 h-11 rounded-xl bg-emerald-100/70 border border-emerald-200 flex items-center justify-center text-emerald-600 shrink-0">
            <CheckCircle className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-stone-900 font-mono">98</span>
            </div>
            <p className="text-xs font-bold text-stone-700">Within SLA</p>
            <p className="text-[11px] font-semibold text-emerald-600">On Track</p>
          </div>
        </div>
      </div>

      {/* Main Split Content: Left Filters & Summaries + Right Breach Cards List */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column (col-span-4 on lg) */}
        <div className="lg:col-span-4 space-y-6">
          {/* Card 1: Alert Filters */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
            <h3 className="text-sm font-bold text-stone-900">Alert Filters</h3>

            {/* Department */}
            <div>
              <label className="block text-xs font-semibold text-stone-600 mb-1">
                Department
              </label>
              <div className="relative">
                <select
                  value={selectedDeptFilter}
                  onChange={(e) => setSelectedDeptFilter(e.target.value)}
                  className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20"
                >
                  <option value="All Departments">All Departments</option>
                  <option value="Road Works">Road Works Department</option>
                  <option value="Water & Sanitation">Water &amp; Sanitation Dept.</option>
                  <option value="Electricity">Electricity &amp; Lighting</option>
                  <option value="Sanitation">RMC (Sanitation)</option>
                </select>
                <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>

            {/* SLA Breach Level */}
            <div>
              <label className="block text-xs font-semibold text-stone-600 mb-1">
                SLA Breach Level
              </label>
              <div className="relative">
                <select
                  value={selectedBreachFilter}
                  onChange={(e) => setSelectedBreachFilter(e.target.value)}
                  className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20"
                >
                  <option value="> 80% Time Elapsed">&gt; 80% Time Elapsed</option>
                  <option value="60% - 80% Time Elapsed">60% - 80% Time Elapsed</option>
                  <option value="All Breaches">All Breaches</option>
                </select>
                <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>

            {/* Status */}
            <div>
              <label className="block text-xs font-semibold text-stone-600 mb-1">
                Status
              </label>
              <div className="relative">
                <select
                  value={selectedStatusFilter}
                  onChange={(e) => setSelectedStatusFilter(e.target.value)}
                  className="w-full appearance-none bg-stone-50 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20"
                >
                  <option value="All Status">All Status</option>
                  <option value="Assigned">Assigned</option>
                  <option value="In Progress">In Progress</option>
                  <option value="Open">Open</option>
                </select>
                <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>

            {/* Reset Filters Link */}
            <div className="pt-1">
              <button
                onClick={() => {
                  setSelectedDeptFilter('All Departments');
                  setSelectedBreachFilter('> 80% Time Elapsed');
                  setSelectedStatusFilter('All Status');
                }}
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-stone-500 hover:text-[#ea580c] transition-colors"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Reset Filters</span>
              </button>
            </div>
          </div>

          {/* Card 2: Department Escalation Summary */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-3">
            <h3 className="text-sm font-bold text-stone-900">
              Department Escalation Summary
            </h3>

            <div className="space-y-2 pt-1">
              {/* Road Works */}
              <div
                onClick={() => setSelectedDeptFilter('Road Works')}
                className="flex items-center justify-between p-3 rounded-xl hover:bg-stone-50 transition-colors border border-stone-100 cursor-pointer group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-800 font-bold text-xs flex items-center justify-center shrink-0">
                    R
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-stone-900 group-hover:text-[#ea580c] transition-colors">
                      Road Works Department
                    </h4>
                    <p className="text-[11px] text-red-600 font-semibold mt-0.5">
                      7 Critical
                    </p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-stone-400 group-hover:text-stone-700" />
              </div>

              {/* Water & Sanitation */}
              <div
                onClick={() => setSelectedDeptFilter('Water & Sanitation')}
                className="flex items-center justify-between p-3 rounded-xl hover:bg-stone-50 transition-colors border border-stone-100 cursor-pointer group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-sky-100 text-sky-800 font-bold text-xs flex items-center justify-center shrink-0">
                    W
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-stone-900 group-hover:text-[#ea580c] transition-colors">
                      Water &amp; Sanitation Dept.
                    </h4>
                    <p className="text-[11px] text-red-600 font-semibold mt-0.5">
                      3 Critical
                    </p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-stone-400 group-hover:text-stone-700" />
              </div>

              {/* Electrical */}
              <div
                onClick={() => setSelectedDeptFilter('Electricity')}
                className="flex items-center justify-between p-3 rounded-xl hover:bg-stone-50 transition-colors border border-stone-100 cursor-pointer group"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-amber-100 text-amber-800 font-bold text-xs flex items-center justify-center shrink-0">
                    E
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-stone-900 group-hover:text-[#ea580c] transition-colors">
                      Electricity &amp; Lighting
                    </h4>
                    <p className="text-[11px] text-red-600 font-semibold mt-0.5">
                      2 Critical
                    </p>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-stone-400 group-hover:text-stone-700" />
              </div>
            </div>
          </div>

          {/* Need Support Callout Card */}
          <div className="bg-stone-100/80 rounded-2xl border border-stone-200 p-4 shadow-2xs">
            <p className="text-xs text-stone-500 font-medium">Need Support?</p>
            <h4 className="text-sm font-bold text-stone-900 mt-0.5">
              Contact Control Room
            </h4>
            <div className="flex items-center gap-2 mt-2">
              <Phone className="w-4 h-4 text-[#ea580c]" />
              <span className="text-base font-extrabold text-[#c2410c] font-mono tracking-tight">
                1800-123-4567
              </span>
            </div>
          </div>
        </div>

        {/* Right Column: Critical SLA Breach Issues List (col-span-8 on lg) */}
        <div className="lg:col-span-8 space-y-4">
          <div className="flex items-center justify-between pb-1">
            <div className="flex items-center gap-2">
              <h3 className="text-base font-bold text-stone-900">
                Critical SLA Breach Issues
              </h3>
              <span className="w-6 h-6 rounded-full bg-[#dc2626] text-white font-bold text-xs flex items-center justify-center font-mono">
                12
              </span>
            </div>

            {/* Sort Dropdown */}
            <div className="relative">
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="appearance-none bg-white border border-stone-200 rounded-xl px-3 py-1.5 text-xs font-semibold text-stone-700 pr-7 focus:outline-none shadow-2xs"
              >
                <option value="Time Elapsed (High to Low)">
                  Sort by: Time Elapsed (High to Low)
                </option>
                <option value="Time Remaining (Low to High)">
                  Sort by: Time Remaining (Urgent)
                </option>
                <option value="Warnings Count">Sort by: Warnings Count</option>
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          {/* Breach Cards List */}
          <div className="space-y-4">
            {breachIssues.map((issue) => {
              // Calculate circular gauge stroke
              const radius = 24;
              const circumference = 2 * Math.PI * radius;
              const strokeDashoffset =
                circumference - (issue.timeElapsedPercent / 100) * circumference;

              return (
                <div
                  key={issue.id}
                  className="bg-white rounded-2xl border-l-4 border-l-[#dc2626] border border-stone-200 p-5 shadow-xs hover:shadow-md transition-shadow"
                >
                  <div className="grid grid-cols-1 md:grid-cols-12 gap-5 items-center">
                    {/* Left details + Photo (col-span-5) */}
                    <div className="md:col-span-5 flex items-start gap-3.5">
                      <img
                        src={issue.photoUrl}
                        alt={issue.title}
                        onClick={() => onSelectIssueForDetails(issue.id)}
                        className="w-20 h-18 rounded-xl object-cover ring-1 ring-stone-200 shrink-0 cursor-pointer hover:scale-105 transition-transform"
                      />
                      <div className="min-w-0">
                        <span className="text-xs font-bold text-[#c2410c] font-mono">
                          {issue.id}
                        </span>
                        <h4
                          onClick={() => onSelectIssueForDetails(issue.id)}
                          className="text-xs font-bold text-stone-900 truncate hover:text-[#ea580c] cursor-pointer"
                        >
                          {issue.title}
                        </h4>
                        <div className="flex items-center gap-1 text-[11px] text-stone-500 mt-1 truncate">
                          <Building className="w-3 h-3 text-stone-400 shrink-0" />
                          <span className="truncate">{issue.ward}</span>
                        </div>
                        <div className="flex items-center gap-1 text-[10px] text-stone-400 mt-0.5">
                          <Clock className="w-3 h-3 text-stone-400 shrink-0" />
                          <span>Reported: {issue.reportedAt}</span>
                        </div>
                      </div>
                    </div>

                    {/* Middle Department & Contractor (col-span-3) */}
                    <div className="md:col-span-3 text-xs space-y-1.5 border-l border-stone-100 pl-3">
                      <div>
                        <span className="text-[10px] font-semibold text-stone-400 uppercase tracking-wider block">
                          Department
                        </span>
                        <span className="font-bold text-stone-800 truncate block">
                          {issue.assignedDept}
                        </span>
                      </div>

                      <div className="pt-1">
                        <span className="text-[10px] font-semibold text-stone-400 uppercase tracking-wider block">
                          Assigned Contractor
                        </span>
                        <div className="flex items-center gap-1 text-stone-800 font-semibold truncate">
                          <User className="w-3 h-3 text-stone-400 shrink-0" />
                          <span className="truncate">{issue.assignedContractor || 'Government Unit'}</span>
                        </div>
                      </div>
                    </div>

                    {/* Circular Radial Meter (col-span-2) */}
                    <div className="md:col-span-2 flex flex-col items-center justify-center text-center">
                      <div className="relative w-14 h-14 flex items-center justify-center">
                        <svg className="w-full h-full -rotate-90">
                          <circle
                            cx="28"
                            cy="28"
                            r={radius}
                            fill="transparent"
                            stroke="#f1f5f9"
                            strokeWidth="5"
                          />
                          <circle
                            cx="28"
                            cy="28"
                            r={radius}
                            fill="transparent"
                            stroke="#dc2626"
                            strokeWidth="5"
                            strokeDasharray={circumference}
                            strokeDashoffset={strokeDashoffset}
                            strokeLinecap="round"
                          />
                        </svg>
                        <span className="absolute font-mono font-bold text-xs text-stone-900">
                          {issue.timeElapsedPercent}%
                        </span>
                      </div>
                      <span className="text-[10px] font-bold text-stone-600 mt-1">
                        Time Elapsed
                      </span>
                      <span className="text-[9px] font-semibold text-red-600">
                        (No Progress)
                      </span>
                    </div>

                    {/* SLA Time Remaining & Warning Buttons (col-span-2) */}
                    <div className="md:col-span-2 flex flex-col justify-between items-end gap-2 text-xs">
                      <div className="text-right">
                        <span className="text-[10px] text-stone-400 block">
                          SLA: {issue.slaTotalHours} Hours
                        </span>
                        <div className="flex items-center gap-1 font-mono font-bold text-red-600 text-xs">
                          <span>{issue.timeRemainingFormatted}</span>
                          <Clock className="w-3.5 h-3.5" />
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Bottom 3 Action Buttons */}
                  <div className="mt-4 pt-3 border-t border-stone-100 flex flex-wrap items-center justify-end gap-2.5">
                    {/* 1. Issue Formal Warning */}
                    <button
                      onClick={() => onIssueFormalWarning(issue)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-[#9a3412] hover:bg-[#7c2d12] text-white rounded-xl text-xs font-bold shadow-2xs transition-colors"
                    >
                      <AlertTriangle className="w-3.5 h-3.5" />
                      <span>Issue Formal Warning</span>
                    </button>

                    {/* 2. Escalate to Commissioner */}
                    <button
                      onClick={() => onEscalateCommissioner(issue)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white hover:bg-stone-50 text-[#c2410c] border border-[#ea580c]/40 rounded-xl text-xs font-bold transition-colors"
                    >
                      <ArrowUpRight className="w-3.5 h-3.5" />
                      <span>Escalate to Commissioner</span>
                    </button>

                    {/* 3. Impose Penalty Fine */}
                    <button
                      onClick={() => onImposePenalty(issue)}
                      className="inline-flex items-center gap-1 px-3 py-1.5 bg-white hover:bg-red-50 text-red-700 border border-red-200 rounded-xl text-xs font-bold transition-colors"
                    >
                      <IndianRupee className="w-3.5 h-3.5" />
                      <span>Impose Penalty Fine</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
