import React from 'react';
import { DistrictMetric } from '../types';
import {
  MapPin,
  X,
  Building,
  TrendingUp,
  AlertTriangle,
  CheckCircle,
  Phone,
  Truck,
  Users,
  Shield,
  FileText,
} from 'lucide-react';

interface DistrictModalProps {
  district: DistrictMetric | null;
  onClose: () => void;
  onFilterByDistrict: (districtName: string) => void;
}

export const DistrictModal: React.FC<DistrictModalProps> = ({
  district,
  onClose,
  onFilterByDistrict,
}) => {
  if (!district) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in">
      <div className="bg-white rounded-2xl border border-stone-200 shadow-2xl max-w-2xl w-full overflow-hidden animate-in zoom-in-95">
        {/* Header */}
        <div className="bg-stone-900 text-white px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-stone-800 border border-stone-700 flex items-center justify-center text-[#ea580c]">
              <MapPin className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white">
                  {district.name} District
                </h3>
                {district.hindiName && (
                  <span className="text-xs text-stone-400 font-serif">
                    ({district.hindiName})
                  </span>
                )}
              </div>
              <p className="text-xs text-stone-400">
                {district.name} &bull; Urban Local Bodies Command Center
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg hover:bg-stone-800 flex items-center justify-center text-stone-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-6 space-y-5 text-xs">
          {/* Key Metrics Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="bg-stone-50 p-3 rounded-xl border border-stone-200">
              <span className="text-[10px] text-stone-500 font-semibold block">
                Total Grievances
              </span>
              <span className="text-lg font-bold text-stone-900 font-mono">
                {district.totalIssues.toLocaleString()}
              </span>
            </div>

            <div className="bg-stone-50 p-3 rounded-xl border border-stone-200">
              <span className="text-[10px] text-stone-500 font-semibold block">
                Open Grievances
              </span>
              <span className="text-lg font-bold text-[#ea580c] font-mono">
                {district.openIssuesCount.toLocaleString()}
              </span>
            </div>

            <div className="bg-stone-50 p-3 rounded-xl border border-stone-200">
              <span className="text-[10px] text-stone-500 font-semibold block">
                Resolution Rate
              </span>
              <span className="text-lg font-bold text-emerald-600 font-mono">
                {district.resolutionRate}%
              </span>
            </div>

            <div className="bg-stone-50 p-3 rounded-xl border border-stone-200">
              <span className="text-[10px] text-stone-500 font-semibold block">
                Critical SLA Breaches
              </span>
              <span className="text-lg font-bold text-red-600 font-mono">
                {district.criticalIssuesCount}
              </span>
            </div>
          </div>

          {/* District Administration Contacts */}
          <div className="bg-[#fffbf6] rounded-xl border border-[#fed7aa] p-4 space-y-2">
            <h4 className="font-bold text-stone-900 text-xs flex items-center gap-1.5">
              <Shield className="w-3.5 h-3.5 text-[#ea580c]" />
              <span>District Nodal Administration</span>
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-stone-700">
              <div>
                <span className="text-[10px] text-stone-400 block font-semibold">
                  Deputy Commissioner (DC)
                </span>
                <span className="font-bold text-stone-900">
                  {district.nodalOfficer.name}
                </span>
                <div className="flex items-center gap-1 text-[11px] text-stone-500 mt-0.5">
                  <Phone className="w-3 h-3 text-stone-400" />
                  <span>{district.nodalOfficer.phone}</span>
                </div>
              </div>

              <div>
                <span className="text-[10px] text-stone-400 block font-semibold">
                  Municipal Corporation Helpline
                </span>
                <span className="font-bold text-stone-900">
                  Control Room Zone {district.name.substring(0, 2).toUpperCase()}
                </span>
                <div className="flex items-center gap-1 text-[11px] text-stone-500 mt-0.5">
                  <Phone className="w-3 h-3 text-stone-400" />
                  <span>0651-2400-888</span>
                </div>
              </div>
            </div>
          </div>

          {/* Active Field Machinery Deployed */}
          <div>
            <h4 className="font-bold text-stone-900 text-xs mb-2 flex items-center gap-1.5">
              <Truck className="w-3.5 h-3.5 text-stone-500" />
              <span>Active Municipal Assets Deployed on Field</span>
            </h4>
            <div className="grid grid-cols-3 gap-2 text-center">
              <div className="p-2.5 rounded-xl bg-stone-50 border border-stone-200">
                <span className="text-sm font-bold text-stone-900 font-mono">14</span>
                <p className="text-[10px] text-stone-500 mt-0.5">Compactor Trucks</p>
              </div>
              <div className="p-2.5 rounded-xl bg-stone-50 border border-stone-200">
                <span className="text-sm font-bold text-stone-900 font-mono">8</span>
                <p className="text-[10px] text-stone-500 mt-0.5">Suction Jetting Units</p>
              </div>
              <div className="p-2.5 rounded-xl bg-stone-50 border border-stone-200">
                <span className="text-sm font-bold text-stone-900 font-mono">22</span>
                <p className="text-[10px] text-stone-500 mt-0.5">Rapid Pothole Crews</p>
              </div>
            </div>
          </div>

          {/* Footer Actions */}
          <div className="pt-3 border-t border-stone-100 flex items-center justify-between">
            <span className="text-stone-400 text-[11px]">
              GPS Coordinates: {district.coordinates.x.toFixed(2)}° N, {district.coordinates.y.toFixed(2)}° E
            </span>

            <div className="flex items-center gap-2">
              <button
                onClick={onClose}
                className="px-4 py-2 border border-stone-200 rounded-xl font-bold text-stone-600 hover:bg-stone-50"
              >
                Close
              </button>
              <button
                onClick={() => {
                  onFilterByDistrict(district.name);
                  onClose();
                }}
                className="px-4 py-2 bg-[#ea580c] hover:bg-[#c2410c] text-white font-bold rounded-xl shadow-xs transition-colors flex items-center gap-1.5"
              >
                <FileText className="w-3.5 h-3.5" />
                <span>View All {district.name} Grievances</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
