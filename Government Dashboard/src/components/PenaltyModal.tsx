import React, { useState } from 'react';
import { CivicIssue } from '../types';
import { IndianRupee, X, AlertTriangle, CheckCircle, ShieldAlert } from 'lucide-react';

interface PenaltyModalProps {
  issue: CivicIssue;
  onClose: () => void;
  onConfirmPenalty: (issueId: string, amount: number, clause: string) => void;
}

export const PenaltyModal: React.FC<PenaltyModalProps> = ({
  issue,
  onClose,
  onConfirmPenalty,
}) => {
  const [fineAmount, setFineAmount] = useState(25000);
  const [penaltyClause, setPenaltyClause] = useState(
    'Clause 14.2: Liquidated damages for failure to remedy critical public safety road hazard within 72 hours of citizen grievance notification.'
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onConfirmPenalty(issue.id, fineAmount, penaltyClause);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-md w-full overflow-hidden animate-in zoom-in-95">
        <div className="bg-red-50 px-6 py-4 border-b border-red-200 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-red-600 text-white flex items-center justify-center">
              <IndianRupee className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-stone-900">Impose Contractor Penalty Fine</h3>
              <p className="text-[11px] text-red-700 font-mono">{issue.id}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg hover:bg-red-100 text-stone-500">
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          <div className="bg-stone-50 p-3 rounded-xl border border-stone-200">
            <span className="text-[10px] uppercase font-bold text-stone-400">Target Contractor</span>
            <p className="font-bold text-stone-900">{issue.assignedContractor || 'Government Agency'}</p>
            <p className="text-[11px] text-stone-500">{issue.assignedDept}</p>
          </div>

          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Penalty Amount (₹ INR)
            </label>
            <div className="relative">
              <IndianRupee className="w-4 h-4 text-stone-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="number"
                step="5000"
                value={fineAmount}
                onChange={(e) => setFineAmount(Number(e.target.value))}
                className="w-full bg-stone-50 border border-stone-200 rounded-xl pl-9 pr-4 py-2 text-xs font-mono font-bold text-stone-900 focus:outline-none focus:ring-2 focus:ring-red-500/20"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Statutory Penalty Reference Clause
            </label>
            <textarea
              rows={3}
              value={penaltyClause}
              onChange={(e) => setPenaltyClause(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 focus:outline-none focus:ring-2 focus:ring-red-500/20"
            />
          </div>

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
              className="px-5 py-2 bg-[#dc2626] hover:bg-[#b91c1c] text-white font-bold rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <IndianRupee className="w-3.5 h-3.5" />
              <span>Impose ₹{fineAmount.toLocaleString()} Penalty</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
