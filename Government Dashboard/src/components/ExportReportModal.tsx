import React, { useEffect } from 'react';
import { Download, Printer, X, FileText, CheckCircle2, ShieldCheck, ArrowDownToLine } from 'lucide-react';
import { topDistrictsComparison, departmentStats } from '../data/mockData';
import logoImg from '../assets/logo.jpeg';

interface ExportReportModalProps {
  onClose: () => void;
}

export const ExportReportModal: React.FC<ExportReportModalProps> = ({ onClose }) => {
  // Listen for Escape key to close modal
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleDownloadPdf = () => {
    // Trigger system print / save as PDF dialog with optimized print styles
    window.print();
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-3 sm:p-6 animate-in fade-in overflow-y-auto"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-4xl w-full my-auto overflow-hidden animate-in zoom-in-95 print:m-0 print:border-none print:shadow-none flex flex-col max-h-[92vh]">
        {/* Sticky Top Header Bar with Prominent Action Controls */}
        <div className="bg-[#9a3412] text-white px-5 sm:px-6 py-3.5 flex items-center justify-between shrink-0 shadow-md print:hidden">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-white/10 flex items-center justify-center">
              <FileText className="w-4 h-4 text-white" />
            </div>
            <div>
              <h3 className="text-sm font-bold leading-tight">Weekly Performance PDF Briefing</h3>
              <p className="text-[11px] text-amber-200 font-medium">Official Government of Jharkhand Report</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* Download PDF Button */}
            <button
              type="button"
              onClick={handleDownloadPdf}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-white text-[#9a3412] hover:bg-amber-50 rounded-xl text-xs font-bold transition-all shadow-sm active:scale-95"
              title="Download or Save as PDF"
            >
              <ArrowDownToLine className="w-3.5 h-3.5 text-[#9a3412]" />
              <span>Download PDF</span>
            </button>

            {/* Print Button */}
            <button
              type="button"
              onClick={handleDownloadPdf}
              className="hidden sm:inline-flex items-center gap-1.5 px-3 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl text-xs font-semibold transition-colors"
              title="Print Document"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>Print</span>
            </button>

            {/* Close Button */}
            <button
              type="button"
              onClick={onClose}
              className="inline-flex items-center gap-1 px-3 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl text-xs font-bold transition-colors ml-1"
              title="Close report preview (Esc)"
            >
              <X className="w-4 h-4" />
              <span className="hidden sm:inline">Close</span>
            </button>
          </div>
        </div>

        {/* Scrollable Printable Document Body */}
        <div className="flex-1 overflow-y-auto p-6 sm:p-10 space-y-6 text-stone-800 text-xs bg-[#fafaf9] print:bg-white print:p-0">
          <div className="bg-white p-6 sm:p-8 rounded-xl border border-stone-200 shadow-xs space-y-6 print:border-none print:shadow-none print:p-0">
            {/* Government Official Letterhead */}
            <div className="border-b-2 border-amber-800/30 pb-4 text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-white border border-stone-300 shadow-xs mb-2 p-1.5">
                <img
                  src={logoImg}
                  alt="Government of Jharkhand Emblem"
                  className="w-full h-full object-contain"
                />
              </div>
              <h1 className="text-base sm:text-lg font-extrabold uppercase tracking-wide text-stone-900 font-serif">
                Government of Jharkhand
              </h1>
              <p className="text-xs font-semibold text-stone-700">
                Department of Urban Development &amp; Housing &bull; State Grievance Redressal Command Center
              </p>
              <p className="text-[11px] text-stone-400 font-mono mt-1">
                Doc Ref: JH-UDH/WKLY-REPORT/2025-W33 &bull; Generated on 15 Aug 2025, 10:00 AM IST
              </p>
            </div>

            {/* Executive Summary */}
            <div>
              <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2.5 flex items-center justify-between">
                <span>1. Executive Summary &amp; State Highlights</span>
                <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                  Verified Audit
                </span>
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-center my-3">
                <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                  <span className="text-[10px] text-stone-500 font-medium">Total Inflow</span>
                  <p className="text-base font-bold text-stone-900 font-mono">7,850</p>
                </div>
                <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                  <span className="text-[10px] text-stone-500 font-medium">Total Resolved</span>
                  <p className="text-base font-bold text-emerald-700 font-mono">6,470</p>
                </div>
                <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                  <span className="text-[10px] text-stone-500 font-medium">Resolution Rate</span>
                  <p className="text-base font-bold text-emerald-700 font-mono">82.6%</p>
                </div>
                <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                  <span className="text-[10px] text-stone-500 font-medium">Citizen CSAT</span>
                  <p className="text-base font-bold text-stone-900 font-mono">4.2 / 5.0</p>
                </div>
              </div>
              <p className="text-stone-600 leading-relaxed font-medium">
                During the audit cycle (Aug 08 – Aug 15, 2025), municipal resolution velocity increased by 14.8%. 
                Bokaro maintained the highest on-time SLA closure rate (93.3%), while Ranchi Municipal Corporation 
                handled the maximum volume of incoming civic grievances (2,300 reports resolved at 91.3% efficiency).
              </p>
            </div>

            {/* District Breakdown Table */}
            <div>
              <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2.5">
                2. Top Urban Local Bodies Performance Breakdown
              </h2>
              <table className="w-full text-left border border-stone-200 rounded-lg overflow-hidden">
                <thead className="bg-stone-100 text-[10px] font-bold text-stone-700 uppercase">
                  <tr>
                    <th className="py-2 px-3">District / ULB</th>
                    <th className="py-2 px-3 text-right">Reported</th>
                    <th className="py-2 px-3 text-right">Resolved</th>
                    <th className="py-2 px-3 text-right">Resolution %</th>
                    <th className="py-2 px-3 text-center">SLA Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-stone-200 text-[11px]">
                  {topDistrictsComparison.map((d) => (
                    <tr key={d.district} className="hover:bg-stone-50">
                      <td className="py-2 px-3 font-bold text-stone-900">{d.district}</td>
                      <td className="py-2 px-3 text-right font-mono">{d.reported.toLocaleString()}</td>
                      <td className="py-2 px-3 text-right font-mono text-emerald-700 font-semibold">{d.resolved.toLocaleString()}</td>
                      <td className="py-2 px-3 text-right font-mono font-bold">{d.rate}</td>
                      <td className="py-2 px-3 text-center">
                        <span className="inline-block text-[10px] font-semibold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                          Compliant
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Department Breakdown */}
            <div>
              <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2.5">
                3. Departmental Distribution &amp; Grievance Volume
              </h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {departmentStats.map((dept) => (
                  <div key={dept.name} className="p-2.5 bg-stone-50 border border-stone-200 rounded-lg flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: dept.color }} />
                      <div>
                        <span className="font-bold text-stone-900">{dept.name}</span>
                        <span className="text-stone-400 block text-[10px]">{dept.percentage}% of statewide issues</span>
                      </div>
                    </div>
                    <span className="font-mono font-bold text-stone-900">{dept.count.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Signatory Block */}
            <div className="pt-6 border-t border-stone-200 flex items-end justify-between">
              <div>
                <p className="text-[10px] text-stone-400">Electronic Verification Hash:</p>
                <p className="text-[9px] font-mono text-stone-500 flex items-center gap-1">
                  <ShieldCheck className="w-3 h-3 text-emerald-600 inline" />
                  <span>SHA256: 8f4a2b90d3e811c79a29e4b7c19a08e1</span>
                </p>
              </div>

              <div className="text-right">
                <div className="font-serif italic text-sm text-[#9a3412] font-bold">
                  Ajay Kumar, IAS
                </div>
                <p className="text-xs font-bold text-stone-900">Principal Secretary</p>
                <p className="text-[10px] text-stone-500">Dept. of Urban Development &amp; Housing, Ranchi</p>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom Footer Action Bar */}
        <div className="bg-stone-50 border-t border-stone-200 px-6 py-3.5 flex flex-col sm:flex-row items-center justify-between gap-3 shrink-0 print:hidden">
          <p className="text-xs text-stone-500 font-medium text-center sm:text-left">
            Press <kbd className="px-1.5 py-0.5 bg-stone-200 rounded text-[10px] font-mono text-stone-700">Esc</kbd> or click Close to return to dashboard.
          </p>

          <div className="flex items-center gap-2.5 w-full sm:w-auto justify-end">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 sm:flex-none px-4 py-2 bg-white hover:bg-stone-100 text-stone-700 border border-stone-300 rounded-xl text-xs font-bold transition-colors shadow-2xs text-center"
            >
              Close Window
            </button>
            <button
              type="button"
              onClick={handleDownloadPdf}
              className="flex-1 sm:flex-none inline-flex items-center justify-center gap-2 px-5 py-2 bg-[#c2410c] hover:bg-[#9a3412] text-white rounded-xl text-xs font-bold transition-all shadow-sm active:scale-95"
            >
              <ArrowDownToLine className="w-4 h-4" />
              <span>Download PDF Report</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
