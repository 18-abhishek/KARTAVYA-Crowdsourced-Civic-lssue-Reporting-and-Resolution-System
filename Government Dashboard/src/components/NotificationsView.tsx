import React, { useState, useMemo } from 'react';
import { CivicIssue } from '../types';
import {
  AlertTriangle,
  Clock,
  CheckCircle,
  MapPin,
  User,
  ChevronDown,
  ChevronRight,
  ArrowRight,
  RotateCcw,
  Phone,
  MoreVertical,
  ExternalLink,
  FileWarning,
  IndianRupee,
  Zap,
  Trash2,
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
  const [activeMenuId, setActiveMenuId] = useState<string | null>(null);

  // Filter issues based on selections
  const filteredIssues = useMemo(() => {
    let list = issues.filter((issue) => {
      // Dept filter
      if (
        selectedDeptFilter !== 'All Departments' &&
        !issue.assignedDept.toLowerCase().includes(selectedDeptFilter.toLowerCase())
      ) {
        return false;
      }
      // Breach filter
      if (selectedBreachFilter === '> 80% Time Elapsed' && issue.timeElapsedPercent < 80) {
        return false;
      }
      if (
        selectedBreachFilter === '60% - 80% Time Elapsed' &&
        (issue.timeElapsedPercent < 60 || issue.timeElapsedPercent >= 80)
      ) {
        return false;
      }
      // Status filter
      if (selectedStatusFilter !== 'All Status' && issue.status !== selectedStatusFilter) {
        return false;
      }
      return true;
    });

    if (sortBy === 'Time Remaining (Urgent)') {
      return [...list].sort((a, b) => a.timeRemainingHours - b.timeRemainingHours);
    }
    if (sortBy === 'Reported Date') {
      return [...list].sort((a, b) => b.id.localeCompare(a.id));
    }
    // Default / 'Time Elapsed (High to Low)'
    return [...list];
  }, [issues, selectedDeptFilter, selectedBreachFilter, selectedStatusFilter, sortBy]);

  // Helper for Department Icon matching the reference screenshot
  const renderDepartmentIcon = (dept: string) => {
    const d = dept.toLowerCase();
    if (d.includes('water') || d.includes('sanitation')) {
      return (
        <svg
          className="w-4 h-4 text-[#0284c7] shrink-0"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z" />
        </svg>
      );
    }
    if (d.includes('electr') || d.includes('urja') || d.includes('light')) {
      return <Zap className="w-4 h-4 text-stone-800 shrink-0" />;
    }
    if (d.includes('waste') || d.includes('garbage') || d.includes('clean')) {
      return <Trash2 className="w-4 h-4 text-emerald-600 shrink-0" />;
    }
    // Road Works / Default: arch/milestone letter A from reference
    return (
      <div className="w-4 h-4 rounded border border-stone-400/90 flex items-center justify-center text-stone-700 font-bold text-[10px] shrink-0 leading-none">
        A
      </div>
    );
  };

  return (
    <div
      className="space-y-6 w-full pb-8 animate-in fade-in duration-300"
      onClick={() => setActiveMenuId(null)}
    >
      {/* ─── 1. Top Header & Subtitle ─── */}
      <div>
        <h2 className="text-xl sm:text-2xl font-bold text-stone-900 tracking-tight">
          SLA Escalation &amp; Warning Notification Center
        </h2>
        <p className="text-xs sm:text-sm text-stone-500 font-medium mt-0.5">
          Real-time alerts for critical SLA breaches and zero-progress issues
        </p>
      </div>

      {/* ─── 2. Critical SLA Red Banner ─── */}
      <div className="bg-[#fef2f2] border border-[#fecaca] rounded-2xl p-4 sm:p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-3.5">
          <div className="w-11 h-11 rounded-full bg-[#dc2626] text-white flex items-center justify-center shrink-0 shadow-sm">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm sm:text-base font-bold text-stone-900 leading-snug">
              <span className="text-[#dc2626] font-mono text-base sm:text-lg mr-1.5 font-bold">12</span>
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
            setSelectedDeptFilter('All Departments');
            setSelectedStatusFilter('All Status');
          }}
          className="inline-flex items-center gap-2 px-4 py-2 bg-white hover:bg-red-50 text-[#dc2626] border border-[#fca5a5] rounded-xl text-xs font-bold shrink-0 transition-colors shadow-2xs cursor-pointer"
        >
          <span>View All Critical Issues</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* ─── 3. 4 SLA Tier Status Cards ─── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Critical Card */}
        <div
          onClick={() => setSelectedBreachFilter('> 80% Time Elapsed')}
          className={`p-3.5 rounded-2xl transition-all flex items-center gap-3.5 cursor-pointer border ${
            selectedBreachFilter === '> 80% Time Elapsed'
              ? 'bg-white/80 shadow-xs border-red-300 ring-2 ring-red-400/20'
              : 'bg-white/40 hover:bg-white/60 border-white/60 shadow-2xs'
          }`}
        >
          <div className="w-10 h-10 rounded-xl bg-white/90 border border-stone-200/60 flex items-center justify-center text-red-600 shrink-0 shadow-2xs">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl sm:text-2xl font-bold text-stone-900 font-mono leading-none block">
              12
            </span>
            <p className="text-xs font-bold text-stone-700 mt-0.5">Critical ( &gt; 80% )</p>
            <p className="text-[11px] font-semibold text-red-600">No Progress</p>
          </div>
        </div>

        {/* High Card */}
        <div
          onClick={() => setSelectedBreachFilter('60% - 80% Time Elapsed')}
          className={`p-3.5 rounded-2xl transition-all flex items-center gap-3.5 cursor-pointer border ${
            selectedBreachFilter === '60% - 80% Time Elapsed'
              ? 'bg-white/80 shadow-xs border-amber-300 ring-2 ring-amber-400/20'
              : 'bg-white/40 hover:bg-white/60 border-white/60 shadow-2xs'
          }`}
        >
          <div className="w-10 h-10 rounded-xl bg-white/90 border border-stone-200/60 flex items-center justify-center text-amber-600 shrink-0 shadow-2xs">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl sm:text-2xl font-bold text-stone-900 font-mono leading-none block">
              18
            </span>
            <p className="text-xs font-bold text-stone-700 mt-0.5">High ( 60% - 80% )</p>
            <p className="text-[11px] font-semibold text-amber-600">At Risk</p>
          </div>
        </div>

        {/* Medium Card */}
        <div
          onClick={() => setSelectedBreachFilter('All Breaches')}
          className={`p-3.5 rounded-2xl transition-all flex items-center gap-3.5 cursor-pointer border ${
            selectedBreachFilter === 'All Breaches'
              ? 'bg-white/80 shadow-xs border-yellow-300 ring-2 ring-yellow-400/20'
              : 'bg-white/40 hover:bg-white/60 border-white/60 shadow-2xs'
          }`}
        >
          <div className="w-10 h-10 rounded-xl bg-white/90 border border-stone-200/60 flex items-center justify-center text-yellow-600 shrink-0 shadow-2xs">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl sm:text-2xl font-bold text-stone-900 font-mono leading-none block">
              25
            </span>
            <p className="text-xs font-bold text-stone-700 mt-0.5">Medium ( 40% - 60% )</p>
            <p className="text-[11px] font-semibold text-yellow-700">Monitor</p>
          </div>
        </div>

        {/* Within SLA Card */}
        <div className="p-3.5 rounded-2xl bg-white/40 border border-white/60 shadow-2xs flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-white/90 border border-stone-200/60 flex items-center justify-center text-emerald-600 shrink-0 shadow-2xs">
            <CheckCircle className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl sm:text-2xl font-bold text-stone-900 font-mono leading-none block">
              98
            </span>
            <p className="text-xs font-bold text-stone-700 mt-0.5">Within SLA</p>
            <p className="text-[11px] font-semibold text-emerald-600">On Track</p>
          </div>
        </div>
      </div>

      {/* ─── 4. Main Split Content: Left Sidebar + Right Breach Cards List ─── */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* ─── LEFT COLUMN (col-span-4 on lg) ─── */}
        <div className="lg:col-span-4 space-y-5 lg:sticky lg:top-22 self-start">
          {/* Card 1: Alert Filters */}
          <div className="bg-white/60 backdrop-blur-md p-4.5 rounded-3xl border border-white/60 shadow-xs space-y-4">
            <h3 className="text-sm font-bold text-stone-900">Alert Filters</h3>

            {/* Department */}
            <div>
              <label className="block text-xs font-bold text-stone-700 mb-1.5">
                Department
              </label>
              <div className="relative">
                <select
                  value={selectedDeptFilter}
                  onChange={(e) => setSelectedDeptFilter(e.target.value)}
                  className="w-full appearance-none bg-white/90 border border-stone-200 rounded-xl px-3 py-2 text-xs font-semibold text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 shadow-2xs cursor-pointer"
                >
                  <option value="All Departments">All Departments</option>
                  <option value="Road Works">Road Works Department</option>
                  <option value="Water & Sanitation">Water &amp; Sanitation Dept.</option>
                  <option value="Electricity">Electricity Board</option>
                  <option value="Solid Waste">Solid Waste Management</option>
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
                  className="w-full appearance-none bg-white/90 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 shadow-2xs cursor-pointer"
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
                  className="w-full appearance-none bg-white/90 border border-stone-200 rounded-xl px-3 py-2 text-xs font-medium text-stone-800 pr-8 focus:outline-none focus:ring-2 focus:ring-orange-500/20 shadow-2xs cursor-pointer"
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
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-stone-500 hover:text-[#ea580c] transition-colors cursor-pointer"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Reset Filters</span>
              </button>
            </div>
          </div>

          {/* Card 2: Department Escalation Summary */}
          <div className="bg-white/60 backdrop-blur-md p-4.5 rounded-3xl border border-white/60 shadow-xs space-y-3">
            <h3 className="text-sm font-bold text-stone-900">
              Department Escalation Summary
            </h3>

            <div className="space-y-2 pt-1">
              {/* Road Works */}
              <div
                onClick={() => setSelectedDeptFilter('Road Works')}
                className="flex items-center justify-between p-3 rounded-2xl bg-white/50 hover:bg-white/80 transition-colors border border-white/70 cursor-pointer group shadow-2xs"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-emerald-500/10 text-emerald-800 font-bold text-xs flex items-center justify-center shrink-0 border border-emerald-500/20">
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
                className="flex items-center justify-between p-3 rounded-2xl bg-white/50 hover:bg-white/80 transition-colors border border-white/70 cursor-pointer group shadow-2xs"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-sky-500/10 text-sky-800 font-bold text-xs flex items-center justify-center shrink-0 border border-sky-500/20">
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

              {/* Electricity */}
              <div
                onClick={() => setSelectedDeptFilter('Electricity')}
                className="flex items-center justify-between p-3 rounded-2xl bg-white/50 hover:bg-white/80 transition-colors border border-white/70 cursor-pointer group shadow-2xs"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-xl bg-amber-500/10 text-amber-800 font-bold text-xs flex items-center justify-center shrink-0 border border-amber-500/20">
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

          {/* Card 3: 24x7 Command Control */}
          <div className="bg-white/70 backdrop-blur-md rounded-3xl p-5 border-l-4 border-l-[#ea580c] shadow-xs border border-white/60">
            <p className="text-xs font-semibold text-stone-500">24x7 Command Control</p>
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

        {/* ─── RIGHT COLUMN: Breach Issues List (col-span-8 on lg) ─── */}
        <div className="lg:col-span-8 space-y-3.5">
          {/* Header Row: Title + Sort */}
          <div className="flex items-center justify-between pb-1">
            <div className="flex items-center gap-2.5">
              <h3 className="text-base font-bold text-stone-900">
                Critical SLA Breach Issues
              </h3>
              <span className="w-5 h-5 rounded-full bg-[#dc2626] text-white text-xs font-bold flex items-center justify-center font-mono shrink-0 shadow-2xs">
                {filteredIssues.length}
              </span>
            </div>

            {/* Sort Dropdown */}
            <div className="flex items-center gap-2">
              <span className="text-xs text-stone-500 font-medium">Sort by:</span>
              <div className="relative">
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="appearance-none bg-white border border-stone-200 text-stone-800 text-xs font-medium py-1.5 pl-3 pr-8 rounded-lg shadow-2xs hover:border-stone-300 focus:outline-none focus:ring-1 focus:ring-stone-400 cursor-pointer"
                >
                  <option value="Time Elapsed (High to Low)">
                    Time Elapsed (High to Low)
                  </option>
                  <option value="Time Remaining (Urgent)">
                    Time Remaining (Urgent)
                  </option>
                  <option value="Reported Date">Reported Date</option>
                </select>
                <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              </div>
            </div>
          </div>

          {/* Cards List */}
          <div className="space-y-3">
            {filteredIssues.map((issue) => {
              // Circular gauge
              const radius = 28;
              const circumference = 2 * Math.PI * radius; // ~175.93
              const strokeDashoffset =
                circumference - (issue.timeElapsedPercent / 100) * circumference;

              return (
                <div
                  key={issue.id}
                  className="bg-white rounded-2xl border border-stone-200/80 border-l-[5px] border-l-[#dc2626] p-3.5 sm:p-4 shadow-xs transition-shadow hover:shadow-sm overflow-x-auto"
                >
                  <div className="min-w-[880px] flex items-center gap-4 xl:gap-6">
                    {/* 1. Left Zone: Photo, ID, Title, Location, Reported date */}
                    <div className="flex items-center gap-3 w-[260px] xl:w-[280px] shrink-0">
                      <img
                        src={issue.photoUrl}
                        alt={issue.title}
                        onClick={() => onSelectIssueForDetails(issue.id)}
                        className="w-18 h-18 sm:w-20 sm:h-20 rounded-xl object-cover ring-1 ring-stone-200/70 shrink-0 cursor-pointer hover:opacity-95 hover:scale-[1.02] transition-all shadow-2xs"
                      />
                      <div className="min-w-0 space-y-0.5">
                        <span className="text-xs sm:text-[13px] font-bold text-[#dc2626] font-mono tracking-tight block">
                          {issue.id}
                        </span>
                        <h4
                          onClick={() => onSelectIssueForDetails(issue.id)}
                          className="text-xs sm:text-sm font-bold text-stone-900 truncate hover:text-[#ea580c] cursor-pointer leading-tight"
                          title={issue.title}
                        >
                          {issue.title}
                        </h4>
                        <div className="flex items-center gap-1 text-[11px] text-stone-500 truncate pt-0.5">
                          <MapPin className="w-3 h-3 text-stone-400 shrink-0" />
                          <span className="truncate">{issue.ward}</span>
                        </div>
                        <div className="text-[11px] text-stone-500 font-normal pt-0.5 truncate">
                          Reported: {issue.reportedAt}
                        </div>
                      </div>
                    </div>

                    {/* 2. Department & Assigned Contractor */}
                    <div className="space-y-2 w-[165px] xl:w-[185px] shrink-0">
                      <div>
                        <span className="text-[10px] font-medium text-stone-400 block mb-0.5">
                          Department
                        </span>
                        <div className="flex items-center gap-1.5 text-xs font-semibold text-stone-800 truncate">
                          {renderDepartmentIcon(issue.assignedDept)}
                          <span className="truncate">{issue.assignedDept}</span>
                        </div>
                      </div>
                      <div>
                        <span className="text-[10px] font-medium text-stone-400 block mb-0.5">
                          Assigned Contractor
                        </span>
                        <div className="flex items-center gap-1.5 text-xs font-semibold text-stone-800 truncate">
                          <User className="w-3.5 h-3.5 text-stone-500 shrink-0" />
                          <span className="truncate">
                            {issue.assignedContractor || 'Government Unit'}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* 3. Circular Radial Gauge */}
                    <div className="flex flex-col items-center justify-center text-center w-[95px] xl:w-[105px] shrink-0">
                      <div className="relative w-16 h-16 flex items-center justify-center">
                        <svg className="w-full h-full -rotate-90">
                          <circle
                            cx="32"
                            cy="32"
                            r="25"
                            fill="transparent"
                            stroke="#f1f5f9"
                            strokeWidth="4"
                          />
                          <circle
                            cx="32"
                            cy="32"
                            r="25"
                            fill="transparent"
                            stroke="#dc2626"
                            strokeWidth="4"
                            strokeDasharray={157.08}
                            strokeDashoffset={157.08 - (issue.timeElapsedPercent / 100) * 157.08}
                            strokeLinecap="round"
                          />
                        </svg>
                        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
                          <span className="text-sm sm:text-base font-bold text-stone-900 leading-tight">
                            {issue.timeElapsedPercent}%
                          </span>
                          <span className="text-[8px] font-medium text-stone-500 leading-tight">
                            Time Elapsed
                          </span>
                        </div>
                      </div>
                      <span className="text-[10px] font-medium text-stone-500 mt-0.5">
                        (No Progress)
                      </span>
                    </div>

                    {/* 4. SLA Time & Time Remaining */}
                    <div className="space-y-2 w-[95px] xl:w-[105px] shrink-0">
                      <div>
                        <span className="text-[10px] font-medium text-stone-400 block mb-0.5">
                          SLA Time
                        </span>
                        <span className="text-xs font-bold text-stone-800 block">
                          {issue.slaTotalHours} Hours
                        </span>
                      </div>
                      <div>
                        <span className="text-[10px] font-medium text-stone-400 block mb-0.5">
                          Time Remaining
                        </span>
                        <div className="flex items-center gap-1.5">
                          <span className="text-xs font-bold text-[#dc2626] font-mono">
                            {issue.timeRemainingFormatted}
                          </span>
                          <Clock className="w-3.5 h-3.5 text-[#dc2626] shrink-0" />
                        </div>
                      </div>
                    </div>

                    {/* 5. Rightmost Corner: 3 Options Stacked + 3 Dots Menu */}
                    <div className="ml-auto flex items-center gap-2 shrink-0">
                      {/* Stack of 3 action buttons */}
                      <div className="flex flex-col gap-1.5 w-[175px] xl:w-[190px]">
                        {/* 1. Issue Formal Warning (Top) */}
                        <button
                          onClick={() => onIssueFormalWarning(issue)}
                          className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-[#dc2626] hover:bg-[#b91c1c] text-white rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
                        >
                          <AlertTriangle className="w-3.5 h-3.5 shrink-0 text-white" />
                          <span>Issue Formal Warning</span>
                        </button>

                        {/* 2. Escalate to Commissioner (Middle) */}
                        <button
                          onClick={() => onEscalateCommissioner(issue)}
                          className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-white hover:bg-red-50/50 text-[#dc2626] border border-red-300 rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
                        >
                          <svg
                            className="w-3.5 h-3.5 text-[#dc2626] shrink-0"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <circle cx="12" cy="12" r="10" />
                            <path d="m16 12-4-4-4 4" />
                            <path d="M12 16V8" />
                          </svg>
                          <span>Escalate to Commissioner</span>
                        </button>

                        {/* 3. Impose Penalty Fine (Bottom) */}
                        <button
                          onClick={() => onImposePenalty(issue)}
                          className="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 bg-white hover:bg-red-50/50 text-[#dc2626] border border-red-300 rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
                        >
                          <span className="text-xs font-bold leading-none">₹</span>
                          <span>Impose Penalty Fine</span>
                        </button>
                      </div>

                      {/* Vertical 3 dots menu */}
                      <div className="relative">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setActiveMenuId(activeMenuId === issue.id ? null : issue.id);
                          }}
                          className="p-1 text-stone-400 hover:text-stone-700 hover:bg-stone-100 rounded-md transition-colors cursor-pointer"
                          title="More options"
                        >
                          <MoreVertical className="w-4 h-4" />
                        </button>

                        {/* Context Menu Dropdown */}
                        {activeMenuId === issue.id && (
                          <div
                            onClick={(e) => e.stopPropagation()}
                            className="absolute right-0 top-8 z-30 w-48 bg-white rounded-xl shadow-xl border border-stone-200 py-1 text-xs text-stone-700 animate-in fade-in zoom-in-95"
                          >
                            <button
                              onClick={() => {
                                onSelectIssueForDetails(issue.id);
                                setActiveMenuId(null);
                              }}
                              className="w-full text-left px-3 py-2 hover:bg-stone-50 flex items-center gap-2 cursor-pointer font-medium"
                            >
                              <ExternalLink className="w-3.5 h-3.5 text-stone-400" />
                              <span>View Full Grievance</span>
                            </button>
                            <button
                              onClick={() => {
                                onIssueFormalWarning(issue);
                                setActiveMenuId(null);
                              }}
                              className="w-full text-left px-3 py-2 hover:bg-stone-50 flex items-center gap-2 cursor-pointer font-medium text-amber-700"
                            >
                              <FileWarning className="w-3.5 h-3.5" />
                              <span>Dispatch Show Cause</span>
                            </button>
                            <button
                              onClick={() => {
                                onImposePenalty(issue);
                                setActiveMenuId(null);
                              }}
                              className="w-full text-left px-3 py-2 hover:bg-stone-50 flex items-center gap-2 cursor-pointer font-medium text-red-700"
                            >
                              <IndianRupee className="w-3.5 h-3.5" />
                              <span>Levy Default Penalty</span>
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Bottom Note Banner */}
          <div className="bg-[#fffbeb] border border-[#fef08a] rounded-xl p-3 sm:p-3.5 flex items-center gap-3 text-xs text-stone-700 shadow-2xs mt-2">
            <div className="w-4.5 h-4.5 rounded-full border border-stone-400/90 text-stone-700 flex items-center justify-center shrink-0 font-serif font-bold text-[11px] italic">
              i
            </div>
            <p className="text-stone-700 text-xs leading-relaxed">
              <span className="font-bold text-stone-900">Note:</span> Zero progress reported on
              these issues. Immediate escalation will help avoid SLA breach penalties and ensure
              faster citizen resolution.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
