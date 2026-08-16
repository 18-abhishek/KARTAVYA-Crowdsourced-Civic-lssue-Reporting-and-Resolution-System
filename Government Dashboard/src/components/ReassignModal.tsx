import React, { useState } from 'react';
import { CivicIssue } from '../types';
import { UserCheck, X, Building, CheckCircle2, AlertCircle } from 'lucide-react';

interface ReassignModalProps {
  issue: CivicIssue;
  onClose: () => void;
  onConfirmReassign: (issueId: string, newDept: string, newContractor: string, priorityReason: string) => void;
}

export const ReassignModal: React.FC<ReassignModalProps> = ({
  issue,
  onClose,
  onConfirmReassign,
}) => {
  const [targetDept, setTargetDept] = useState(issue.assignedDept);
  const [targetContractor, setTargetContractor] = useState(
    'Jharkhand State Rapid Emergency Response Taskforce'
  );
  const [reason, setReason] = useState(
    'Reassigned due to SLA threshold breach and non-responsiveness of previous contractor.'
  );
  const [priorityEscalation, setPriorityEscalation] = useState(true);

  const contractorOptions = [
    'Jharkhand State Rapid Emergency Response Taskforce',
    'Swachh Bharat Urban Engineering Unit',
    'State PWD Heavy Machinery Wing',
    'Urja Vikas Special Projects Team',
    'RMC Zonal Standby Crew 4',
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onConfirmReassign(issue.id, targetDept, targetContractor, reason);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-lg w-full overflow-hidden animate-in zoom-in-95">
        {/* Header */}
        <div className="bg-stone-50 px-6 py-4 border-b border-stone-200 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#ea580c] text-white flex items-center justify-center">
              <UserCheck className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-stone-900">
                Reassign Department &amp; Contractor
              </h3>
              <p className="text-[11px] text-stone-500 font-mono">
                Grievance: {issue.id}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 rounded-lg hover:bg-stone-200 flex items-center justify-center text-stone-500"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {/* Current Assignment */}
          <div className="p-3 bg-amber-50/70 border border-amber-200 rounded-xl text-stone-800">
            <span className="text-[10px] font-bold uppercase text-amber-800 block">
              Current Assignment
            </span>
            <div className="flex justify-between items-center mt-1">
              <div>
                <p className="font-bold">{issue.assignedDept}</p>
                <p className="text-[11px] text-stone-600">{issue.assignedContractor || 'Unassigned Contractor'}</p>
              </div>
              <span className="text-xs font-mono font-bold text-red-600">
                {issue.timeElapsedPercent}% SLA Elapsed
              </span>
            </div>
          </div>

          {/* New Department */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Transfer to Department
            </label>
            <select
              value={targetDept}
              onChange={(e) => setTargetDept(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 font-semibold focus:outline-none focus:ring-2 focus:ring-orange-500/20"
            >
              <option value="Road Works Department">Road Works Department / PWD</option>
              <option value="Water & Sanitation Dept.">Water &amp; Sanitation Department</option>
              <option value="Electricity & Lighting">Electricity &amp; Lighting Division</option>
              <option value="RMC (Sanitation)">RMC (Sanitation &amp; Waste)</option>
              <option value="State Disaster Rapid Taskforce">State Disaster Rapid Taskforce</option>
            </select>
          </div>

          {/* New Contractor */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Designate Rapid Deployment Contractor
            </label>
            <select
              value={targetContractor}
              onChange={(e) => setTargetContractor(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 font-semibold focus:outline-none focus:ring-2 focus:ring-orange-500/20"
            >
              {contractorOptions.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          {/* Reason */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Administrative Reason for Transfer
            </label>
            <textarea
              rows={2}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 focus:outline-none focus:ring-2 focus:ring-orange-500/20"
            />
          </div>

          {/* Fast-track toggle */}
          <div className="flex items-center gap-2.5 pt-1">
            <input
              type="checkbox"
              id="priority"
              checked={priorityEscalation}
              onChange={(e) => setPriorityEscalation(e.target.checked)}
              className="rounded text-[#ea580c] focus:ring-orange-500"
            />
            <label htmlFor="priority" className="font-semibold text-stone-700 cursor-pointer">
              Set SLA to Fast-Track Emergency 12-Hour Window
            </label>
          </div>

          {/* Footer Actions */}
          <div className="pt-3 border-t border-stone-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border border-stone-200 rounded-xl font-bold text-stone-600 hover:bg-stone-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2 bg-[#ea580c] hover:bg-[#c2410c] text-white font-bold rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Confirm Reassignment</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
