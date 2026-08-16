import React, { useState } from 'react';
import { CivicIssue } from '../types';
import {
  AlertTriangle,
  X,
  Send,
  FileWarning,
  Building2,
  ShieldAlert,
  IndianRupee,
  CheckCircle2,
  Clock,
} from 'lucide-react';

interface WarningNoticeModalProps {
  issue: CivicIssue;
  onClose: () => void;
  onConfirmWarning: (issueId: string, penaltyAmount: number, noticeText: string) => void;
}

export const WarningNoticeModal: React.FC<WarningNoticeModalProps> = ({
  issue,
  onClose,
  onConfirmWarning,
}) => {
  const [penaltyAmount, setPenaltyAmount] = useState(25000);
  const [noticeReason, setNoticeReason] = useState(
    `Contractor has failed to mobilize repair machinery within the statutory 72-hour SLA window. Critical time elapsed at ${issue.timeElapsedPercent}% with zero documented progress on site.`
  );
  const [dispatchMethod, setDispatchMethod] = useState<'both' | 'sms' | 'email'>('both');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const referenceNo = `JH-CIVIC/WARN/${new Date().getFullYear()}/${issue.id.replace('#', '')}`;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setTimeout(() => {
      onConfirmWarning(issue.id, penaltyAmount, noticeReason);
      setIsSubmitting(false);
      onClose();
    }, 600);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-xl w-full overflow-hidden animate-in zoom-in-95">
        {/* Modal Header */}
        <div className="bg-[#fef2f2] px-6 py-4 border-b border-[#fecaca] flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#dc2626] text-white flex items-center justify-center">
              <FileWarning className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-stone-900">
                Issue Formal Government Show-Cause Notice
              </h3>
              <p className="text-[11px] text-red-700 font-mono">
                Ref: {referenceNo}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 rounded-lg hover:bg-red-100 flex items-center justify-center text-stone-500 hover:text-stone-800"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {/* Target Contractor Info */}
          <div className="bg-stone-50 p-3.5 rounded-xl border border-stone-200 flex items-center justify-between">
            <div>
              <span className="text-[10px] uppercase font-bold text-stone-400">
                Defaulting Agency / Contractor
              </span>
              <h4 className="text-sm font-bold text-stone-900">
                {issue.assignedContractor || 'Assigned Zonal Department'}
              </h4>
              <p className="text-stone-500 text-[11px]">
                Contract Scope: {issue.assignedDept} &bull; {issue.city}
              </p>
            </div>
            <div className="text-right">
              <span className="text-[10px] uppercase font-bold text-stone-400">
                SLA Breach
              </span>
              <div className="text-sm font-extrabold text-red-600 font-mono">
                {issue.timeElapsedPercent}% Time Elapsed
              </div>
              <span className="text-[10px] text-stone-500 font-semibold">
                Only {issue.timeRemainingFormatted} remaining
              </span>
            </div>
          </div>

          {/* Grievance Summary */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Grievance Under Notice
            </label>
            <div className="p-2.5 bg-stone-50 rounded-xl border border-stone-200 text-stone-800 font-medium">
              <strong className="text-[#c2410c]">{issue.id}:</strong> {issue.title} ({issue.ward})
            </div>
          </div>

          {/* Show cause reason */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Legal Show-Cause Grounds (Jharkhand Municipal Act Sec. 142)
            </label>
            <textarea
              rows={3}
              value={noticeReason}
              onChange={(e) => setNoticeReason(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-3 text-xs text-stone-800 focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500"
            />
          </div>

          {/* Penalty Clause Amount */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Statutory Penalty Deduction (₹ INR)
            </label>
            <div className="relative">
              <IndianRupee className="w-4 h-4 text-stone-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="number"
                step="5000"
                value={penaltyAmount}
                onChange={(e) => setPenaltyAmount(Number(e.target.value))}
                className="w-full bg-stone-50 border border-stone-200 rounded-xl pl-9 pr-4 py-2 text-xs font-mono font-bold text-stone-900 focus:outline-none focus:ring-2 focus:ring-red-500/20"
              />
            </div>
            <p className="text-[10px] text-stone-400 mt-1">
              Amount will be deducted from next monthly departmental escrow disbursement.
            </p>
          </div>

          {/* Dispatch Mode */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Dispatch Mode
            </label>
            <div className="flex gap-3">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  checked={dispatchMethod === 'both'}
                  onChange={() => setDispatchMethod('both')}
                  className="text-red-600 focus:ring-red-500"
                />
                <span className="text-stone-700 font-medium">Digital Notice + SMS Alert</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  checked={dispatchMethod === 'sms'}
                  onChange={() => setDispatchMethod('sms')}
                  className="text-red-600 focus:ring-red-500"
                />
                <span className="text-stone-700 font-medium">SMS Warning Only</span>
              </label>
            </div>
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
              disabled={isSubmitting}
              className="px-5 py-2 bg-[#dc2626] hover:bg-[#b91c1c] text-white font-bold rounded-xl shadow-sm flex items-center gap-2 transition-all disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>{isSubmitting ? 'Dispatching...' : 'Dispatch Official Warning'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
