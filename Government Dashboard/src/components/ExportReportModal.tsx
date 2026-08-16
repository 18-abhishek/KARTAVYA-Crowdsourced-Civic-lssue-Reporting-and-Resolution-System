import React from 'react';
import { Download, Printer, X, CheckCircle, Shield, FileText } from 'lucide-react';
import { topDistrictsComparison, departmentStats } from '../data/mockData';
import logoImg from '../assets/logo.jpeg';

interface ExportReportModalProps {
  onClose: () => void;
}

export const ExportReportModal: React.FC<ExportReportModalProps> = ({ onClose }) => {
  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in overflow-y-auto">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-3xl w-full my-8 overflow-hidden animate-in zoom-in-95 print:m-0 print:border-none print:shadow-none">
        {/* Printable Header Bar */}
        <div className="bg-[#9a3412] text-white px-6 py-4 flex items-center justify-between print:hidden">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5" />
            <h3 className="text-sm font-bold">State Weekly Performance PDF Briefing</h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={handlePrint}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white rounded-lg text-xs font-bold transition-colors"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>Print / Save PDF</span>
            </button>
            <button
              onClick={onClose}
              className="w-7 h-7 rounded-lg hover:bg-white/10 flex items-center justify-center text-white"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Printable Document Body */}
        <div className="p-8 space-y-6 text-stone-800 text-xs">
          {/* Government Official Letterhead */}
          <div className="border-b-2 border-amber-800/30 pb-4 text-center">
            <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-white border border-stone-300 shadow-xs mb-2 p-1">
              <img
                src={logoImg}
                alt="Government of Jharkhand Emblem"
                className="w-full h-full object-contain"
              />
            </div>
            <h1 className="text-base font-extrabold uppercase tracking-wide text-stone-900 font-serif">
              Government of Jharkhand
            </h1>
            <p className="text-xs font-medium text-stone-600">
              Department of Urban Development &amp; Housing &bull; State Command Center
            </p>
            <p className="text-[11px] text-stone-400 font-mono mt-1">
              Doc Ref: JH-UDH/WKLY-REPORT/2025-W33 &bull; Generated on 15 Aug 2025, 10:00 AM IST
            </p>
          </div>

          {/* Executive Summary */}
          <div>
            <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2">
              1. Executive Summary &amp; Key Highlights
            </h2>
            <div className="grid grid-cols-4 gap-3 text-center my-3">
              <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                <span className="text-[10px] text-stone-500 font-medium">Total Inflow</span>
                <p className="text-base font-bold text-stone-900 font-mono">7,850</p>
              </div>
              <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                <span className="text-[10px] text-stone-500 font-medium">Resolved</span>
                <p className="text-base font-bold text-emerald-700 font-mono">6,470</p>
              </div>
              <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                <span className="text-[10px] text-stone-500 font-medium">State Resolution %</span>
                <p className="text-base font-bold text-emerald-700 font-mono">82.6%</p>
              </div>
              <div className="bg-stone-50 p-2.5 rounded-xl border border-stone-200">
                <span className="text-[10px] text-stone-500 font-medium">Citizen CSAT</span>
                <p className="text-base font-bold text-stone-900 font-mono">4.2 / 5.0</p>
              </div>
            </div>
            <p className="text-stone-600 leading-relaxed font-medium">
              During the review period (Aug 08 - Aug 15, 2025), municipal resolution velocity increased by 14.8%. 
              Bokaro maintained the highest on-time SLA closure rate (88.4%), while Ranchi Municipal Corporation 
              handled the maximum volume of incoming civic grievances (2,340 reports).
            </p>
          </div>

          {/* District Breakdown Table */}
          <div>
            <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2">
              2. Top Urban Local Bodies Performance Breakdown
            </h2>
            <table className="w-full text-left border border-stone-200 rounded-lg overflow-hidden">
              <thead className="bg-stone-100 text-[10px] font-bold text-stone-700 uppercase">
                <tr>
                  <th className="py-2 px-3">District / ULB</th>
                  <th className="py-2 px-3 text-right">Reported</th>
                  <th className="py-2 px-3 text-right">Resolved</th>
                  <th className="py-2 px-3 text-right">Resolution %</th>
                  <th className="py-2 px-3 text-center">SLA Compliance</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-stone-200">
                {topDistrictsComparison.map((d) => (
                  <tr key={d.district}>
                    <td className="py-2 px-3 font-bold text-stone-900">{d.district}</td>
                    <td className="py-2 px-3 text-right font-mono">{d.reported.toLocaleString()}</td>
                    <td className="py-2 px-3 text-right font-mono text-emerald-700">{d.resolved.toLocaleString()}</td>
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
            <h2 className="text-sm font-bold text-stone-900 border-b border-stone-200 pb-1 mb-2">
              3. Departmental Distribution &amp; Action Plan
            </h2>
            <div className="grid grid-cols-2 gap-3">
              {departmentStats.map((dept) => (
                <div key={dept.name} className="p-2.5 bg-stone-50 border border-stone-200 rounded-lg flex items-center justify-between">
                  <div>
                    <span className="font-bold text-stone-900">{dept.name}</span>
                    <span className="text-stone-400 block text-[10px]">{dept.percentage}% of total issues</span>
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
              <p className="text-[9px] font-mono text-stone-500">SHA256: 8f4a2b90d3e811c79a29e4</p>
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
    </div>
  );
};
