import React, { useState, useMemo } from 'react';
import { CivicIssue } from '../types';
import {
  AlertTriangle,
  Clock,
  MapPin,
  User,
  ChevronDown,
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
  const [sortBy, setSortBy] = useState('Time Elapsed (High to Low)');
  const [activeMenuId, setActiveMenuId] = useState<string | null>(null);

  // Filter critical SLA breach issues (>= 80% time elapsed)
  const breachIssues = useMemo(() => {
    const list = issues.filter((issue) => issue.timeElapsedPercent >= 80);

    if (sortBy === 'Time Remaining (Urgent)') {
      return [...list].sort((a, b) => a.timeRemainingHours - b.timeRemainingHours);
    }
    if (sortBy === 'Reported Date') {
      return [...list].sort((a, b) => b.id.localeCompare(a.id));
    }
    // Default / 'Time Elapsed (High to Low)'
    return [...list];
  }, [issues, sortBy]);

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
      className="space-y-3.5 max-w-[1260px] mx-auto pb-8 animate-in fade-in duration-300"
      onClick={() => setActiveMenuId(null)}
    >
      {/* ─── Top Header Zone ─── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-1 pb-1">
        <div className="flex items-center gap-2.5">
          <h2 className="text-base sm:text-lg font-bold text-stone-900 tracking-tight">
            Critical SLA Breach Issues
          </h2>
          <span className="w-5 h-5 rounded-full bg-[#dc2626] text-white text-xs font-bold flex items-center justify-center font-mono shrink-0 shadow-2xs">
            {breachIssues.length}
          </span>
        </div>

        {/* Sort by dropdown */}
        <div className="flex items-center gap-2 self-end sm:self-auto">
          <span className="text-xs text-stone-500 font-medium">Sort by:</span>
          <div className="relative">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="appearance-none bg-white border border-stone-200 text-stone-800 text-xs font-medium py-1.5 pl-3 pr-8 rounded-lg shadow-2xs hover:border-stone-300 focus:outline-none focus:ring-1 focus:ring-stone-400 cursor-pointer"
            >
              <option value="Time Elapsed (High to Low)">Time Elapsed (High to Low)</option>
              <option value="Time Remaining (Urgent)">Time Remaining (Urgent)</option>
              <option value="Reported Date">Reported Date</option>
            </select>
            <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>
        </div>
      </div>

      {/* ─── Cards List ─── */}
      <div className="space-y-3">
        {breachIssues.map((issue) => {
          // Circular radial gauge constants
          const radius = 28;
          const circumference = 2 * Math.PI * radius; // ~175.93
          const strokeDashoffset =
            circumference - (issue.timeElapsedPercent / 100) * circumference;

          return (
            <div
              key={issue.id}
              className="bg-white rounded-2xl border border-stone-200/80 border-l-[5px] border-l-[#dc2626] p-3.5 sm:p-4 shadow-xs transition-shadow hover:shadow-sm"
            >
              <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 lg:gap-5 xl:gap-7">
                {/* 1. Left Zone: Photo, ID, Title, Location, Reported date */}
                <div className="flex items-center gap-3 min-w-0 lg:w-[310px] xl:w-[330px] shrink-0">
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
                    <h3
                      onClick={() => onSelectIssueForDetails(issue.id)}
                      className="text-sm sm:text-[15px] font-bold text-stone-900 truncate hover:text-[#ea580c] cursor-pointer leading-tight"
                      title={issue.title}
                    >
                      {issue.title}
                    </h3>
                    <div className="flex items-center gap-1 text-xs text-stone-500 truncate pt-0.5">
                      <MapPin className="w-3.5 h-3.5 text-stone-400 shrink-0" />
                      <span className="truncate">{issue.ward}</span>
                    </div>
                    <div className="text-xs text-stone-500 font-normal pt-0.5">
                      Reported: {issue.reportedAt}
                    </div>
                  </div>
                </div>

                {/* 2. Department & Assigned Contractor (closely beside left zone) */}
                <div className="space-y-2 lg:w-[185px] xl:w-[200px] shrink-0">
                  <div>
                    <span className="text-[10px] font-medium text-stone-400 block mb-0.5 uppercase tracking-wide">
                      Department
                    </span>
                    <div className="flex items-center gap-2 text-xs sm:text-[13px] font-semibold text-stone-800 truncate">
                      {renderDepartmentIcon(issue.assignedDept)}
                      <span className="truncate">{issue.assignedDept}</span>
                    </div>
                  </div>
                  <div>
                    <span className="text-[10px] font-medium text-stone-400 block mb-0.5 uppercase tracking-wide">
                      Assigned Contractor
                    </span>
                    <div className="flex items-center gap-2 text-xs sm:text-[13px] font-semibold text-stone-800 truncate">
                      <User className="w-3.5 h-3.5 text-stone-500 shrink-0" />
                      <span className="truncate">
                        {issue.assignedContractor || 'Government Unit'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* 3. Circular Radial Gauge (closely beside department) */}
                <div className="flex flex-col items-center justify-center text-center lg:w-[105px] xl:w-[115px] shrink-0">
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

                {/* 4. SLA Time & Time Remaining (closely beside gauge) */}
                <div className="space-y-2 lg:w-[105px] xl:w-[115px] shrink-0">
                  <div>
                    <span className="text-[10px] font-medium text-stone-400 block mb-0.5 uppercase tracking-wide">
                      SLA Time
                    </span>
                    <span className="text-xs sm:text-sm font-bold text-stone-800 block">
                      {issue.slaTotalHours} Hours
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] font-medium text-stone-400 block mb-0.5 uppercase tracking-wide">
                      Time Remaining
                    </span>
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs sm:text-sm font-bold text-[#dc2626] font-mono">
                        {issue.timeRemainingFormatted}
                      </span>
                      <Clock className="w-3.5 h-3.5 text-[#dc2626] shrink-0" />
                    </div>
                  </div>
                </div>

                {/* 5. Rightmost Zone: 3 Stacked Buttons in exact order + 3-dots Menu */}
                <div className="flex items-center gap-1.5 shrink-0 lg:w-[205px]">
                  {/* Stack of 3 action buttons in exact vertical order */}
                  <div className="flex flex-col gap-1.5 w-[185px]">
                    {/* 1. Issue Formal Warning (Top) */}
                    <button
                      onClick={() => onIssueFormalWarning(issue)}
                      className="inline-flex items-center justify-center gap-2 px-3 py-1.5 bg-[#dc2626] hover:bg-[#b91c1c] text-white rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
                    >
                      <AlertTriangle className="w-3.5 h-3.5 shrink-0 text-white" />
                      <span>Issue Formal Warning</span>
                    </button>

                    {/* 2. Escalate to Commissioner (Middle) */}
                    <button
                      onClick={() => onEscalateCommissioner(issue)}
                      className="inline-flex items-center justify-center gap-2 px-3 py-1.5 bg-white hover:bg-red-50/50 text-[#dc2626] border border-red-300 rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
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
                      className="inline-flex items-center justify-center gap-2 px-3 py-1.5 bg-white hover:bg-red-50/50 text-[#dc2626] border border-red-300 rounded-lg text-xs font-semibold shadow-2xs transition-colors cursor-pointer active:scale-[0.99]"
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

      {/* ─── Bottom Note Banner ─── */}
      <div className="bg-[#fffbeb] border border-[#fef08a] rounded-xl p-3 sm:p-3.5 flex items-center gap-3 text-xs text-stone-700 shadow-2xs mt-2">
        <div className="w-4.5 h-4.5 rounded-full border border-stone-400/90 text-stone-700 flex items-center justify-center shrink-0 font-serif font-bold text-[11px] italic">
          i
        </div>
        <p className="text-stone-700 text-xs leading-relaxed">
          <span className="font-bold text-stone-900">Note:</span> Zero progress reported on
          these issues. Immediate escalation will help avoid SLA breach penalties and ensure faster
          citizen resolution.
        </p>
      </div>
    </div>
  );
};
