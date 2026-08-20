import React, { useState } from 'react';
import { CivicIssue } from '../types';
import confetti from 'canvas-confetti';
import { CheckCircle2, X, Upload, Camera, FileCheck, Sparkles } from 'lucide-react';

interface ResolveModalProps {
  issue: CivicIssue;
  onClose: () => void;
  onConfirmResolve: (issueId: string, resolutionData: { notes: string; photoUrl: string; resolvedBy: string }) => void;
}

export const ResolveModal: React.FC<ResolveModalProps> = ({
  issue,
  onClose,
  onConfirmResolve,
}) => {
  const [resolutionNotes, setResolutionNotes] = useState(
    'Site inspection completed. Roadway pothole filled with hot-mix bitumen, leveled and roller compacted. Normal traffic flow restored.'
  );
  const [resolvedBy, setResolvedBy] = useState('Er. Sanjay Mishra (Assistant Engineer, PWD)');
  const [proofPhotoUrl, setProofPhotoUrl] = useState(
    '/resource/sewage 1.jpg'
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Fire celebration confetti
    try {
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 },
        colors: ['#ea580c', '#16a34a', '#0284c7', '#f59e0b'],
      });
    } catch (e) {
      // confetti fallback
    }

    setTimeout(() => {
      onConfirmResolve(issue.id, {
        notes: resolutionNotes,
        photoUrl: proofPhotoUrl,
        resolvedBy,
      });
      setIsSubmitting(false);
      onClose();
    }, 500);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-lg w-full overflow-hidden animate-in zoom-in-95">
        {/* Header */}
        <div className="bg-emerald-50 px-6 py-4 border-b border-emerald-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-600 text-white flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-stone-900">
                Mark Grievance as Resolved
              </h3>
              <p className="text-[11px] text-emerald-800 font-mono">
                {issue.id} &bull; {issue.city}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-7 h-7 rounded-lg hover:bg-emerald-100 flex items-center justify-center text-stone-500"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {/* Issue summary */}
          <div className="p-3 bg-stone-50 border border-stone-200 rounded-xl">
            <span className="text-[10px] uppercase font-bold text-stone-400">Issue</span>
            <p className="font-bold text-stone-900 mt-0.5">{issue.title}</p>
            <p className="text-stone-500 text-[11px]">{issue.ward}</p>
          </div>

          {/* Completion Proof Photo Preview */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Field Completion Photo Proof
            </label>
            <div className="flex items-center gap-3">
              <img
                src={proofPhotoUrl}
                alt="Proof Preview"
                className="w-24 h-16 rounded-xl object-cover ring-1 ring-stone-200"
              />
              <div className="flex-1 space-y-1.5">
                <input
                  type="text"
                  value={proofPhotoUrl}
                  onChange={(e) => setProofPhotoUrl(e.target.value)}
                  placeholder="Photo proof URL..."
                  className="w-full bg-stone-50 border border-stone-200 rounded-lg p-1.5 text-xs text-stone-800 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                />
                <p className="text-[10px] text-stone-400">
                  Geo-tagged timestamp proof automatically captured from Field Staff App.
                </p>
              </div>
            </div>
          </div>

          {/* Engineer Sign-off */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Inspecting Officer / Certifying Engineer
            </label>
            <input
              type="text"
              value={resolvedBy}
              onChange={(e) => setResolvedBy(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
            />
          </div>

          {/* Resolution Notes */}
          <div>
            <label className="block font-bold text-stone-700 mb-1">
              Resolution Remarks &amp; Citizen Closure Memo
            </label>
            <textarea
              rows={3}
              value={resolutionNotes}
              onChange={(e) => setResolutionNotes(e.target.value)}
              className="w-full bg-stone-50 border border-stone-200 rounded-xl p-2.5 text-xs text-stone-800 focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
            />
          </div>

          {/* Citizen SMS confirmation checkbox */}
          <div className="p-3 bg-emerald-50/50 rounded-xl border border-emerald-100 flex items-center gap-2">
            <input
              type="checkbox"
              defaultChecked
              id="notifyCitizen"
              className="rounded text-emerald-600 focus:ring-emerald-500"
            />
            <label htmlFor="notifyCitizen" className="text-stone-700 font-medium cursor-pointer">
              Send Automated SMS &amp; WhatsApp resolution confirmation to {issue.reportedBy.name}
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
              disabled={isSubmitting}
              className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>{isSubmitting ? 'Finalizing...' : 'Certify & Resolve'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
