import React, { useState, useRef, useEffect } from 'react';
import { CivicIssue, CategoryType, IssueStatus } from '../types';
import {
  Search,
  SlidersHorizontal,
  Download,
  Share2,
  Maximize2,
  Copy,
  Check,
  CheckCircle2,
  Clock,
  Building,
  UserCheck,
  MapPin,
  FileText,
  Play,
  Pause,
  Volume2,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  RotateCcw,
  Zap,
  Hammer,
  Droplets,
  Trash2,
  PhoneCall,
  ArrowRight,
  CheckCircle,
  AlertTriangle,
  Send,
  Printer,
  Sparkles,
} from 'lucide-react';

interface DetailsViewProps {
  issues: CivicIssue[];
  selectedIssueId: string;
  onSelectIssue: (issueId: string) => void;
  onReassign: (issue: CivicIssue) => void;
  onResolve: (issue: CivicIssue) => void;
  onEscalate: (issue: CivicIssue) => void;
}

export const DetailsView: React.FC<DetailsViewProps> = ({
  issues,
  selectedIssueId,
  onSelectIssue,
  onReassign,
  onResolve,
  onEscalate,
}) => {
  const [selectedCity, setSelectedCity] = useState('Ranchi Municipal Corporation');
  const [selectedCategories, setSelectedCategories] = useState<string[]>(['Roadways']);
  const [selectedStatus, setSelectedStatus] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const rowsPerPage = 10;
  const [copiedCoords, setCopiedCoords] = useState(false);
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const [audioProgress, setAudioProgress] = useState(0);
  const [showEnglishTranslation, setShowEnglishTranslation] = useState(false);
  const [moreActionsOpen, setMoreActionsOpen] = useState(false);
  const [fullscreenPhoto, setFullscreenPhoto] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const audioTimerRef = useRef<any>(null);

  const selectedIssue =
    issues.find((i) => i.id === selectedIssueId) || issues[0] || null;

  // Toggle category in multi-select
  const toggleCategory = (cat: string) => {
    if (selectedCategories.includes(cat)) {
      setSelectedCategories(selectedCategories.filter((c) => c !== cat));
    } else {
      setSelectedCategories([...selectedCategories, cat]);
    }
  };

  const handleClearFilters = () => {
    setSelectedCity('All Cities');
    setSelectedCategories([]);
    setSelectedStatus('All');
    setSearchQuery('');
  };

  // Filter issues
  const filteredIssues = issues.filter((issue) => {
    if (selectedCity !== 'All Cities' && !issue.city.includes(selectedCity)) {
      // If user chose specific city
    }
    if (
      selectedCategories.length > 0 &&
      !selectedCategories.includes(issue.category)
    ) {
      // return false; // allow soft filter for demo rich view
    }
    if (selectedStatus !== 'All' && issue.status !== selectedStatus) {
      return false;
    }
    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      const match =
        issue.id.toLowerCase().includes(q) ||
        issue.title.toLowerCase().includes(q) ||
        issue.ward.toLowerCase().includes(q) ||
        issue.description.toLowerCase().includes(q);
      if (!match) return false;
    }
    return true;
  });

  const handleCopyCoordinates = () => {
    if (selectedIssue) {
      navigator.clipboard.writeText(selectedIssue.coordinates.formatted);
      setCopiedCoords(true);
      setTimeout(() => setCopiedCoords(false), 2000);
    }
  };

  // Simulated Web Audio synthesizer playback for citizen voice note
  const togglePlayAudio = () => {
    if (isPlayingAudio) {
      setIsPlayingAudio(false);
      clearInterval(audioTimerRef.current);
    } else {
      setIsPlayingAudio(true);
      // Play a subtle web audio chime / harmonic wave
      try {
        const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioContextClass) {
          const ctx = new AudioContextClass();
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          osc.type = 'sine';
          osc.frequency.setValueAtTime(320, ctx.currentTime);
          osc.frequency.exponentialRampToValueAtTime(440, ctx.currentTime + 0.15);
          gain.gain.setValueAtTime(0.08, ctx.currentTime);
          gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start();
          osc.stop(ctx.currentTime + 0.3);
        }
      } catch (e) {
        // audio context optional
      }

      setAudioProgress(0);
      audioTimerRef.current = setInterval(() => {
        setAudioProgress((prev) => {
          if (prev >= 18) {
            clearInterval(audioTimerRef.current);
            setIsPlayingAudio(false);
            return 0;
          }
          return prev + 1;
        });
      }, 1000);
    }
  };

  useEffect(() => {
    return () => {
      if (audioTimerRef.current) clearInterval(audioTimerRef.current);
    };
  }, []);

  const handleExportCSV = () => {
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      ['Issue ID,Title,Category,City,Ward,Status,Priority,Reported At,Department']
        .concat(
          filteredIssues.map(
            (i) =>
              `"${i.id}","${i.title}","${i.category}","${i.city}","${i.ward}","${i.status}","${i.priority}","${i.reportedAt}","${i.assignedDept}"`
          )
        )
        .join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `Jharkhand_Civic_Grievances_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const getStatusBadge = (status: IssueStatus) => {
    switch (status) {
      case 'Assigned':
      case 'In Progress':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-amber-50 text-amber-800 border border-amber-200">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
            {status}
          </span>
        );
      case 'Resolved':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
            Resolved
          </span>
        );
      case 'Open':
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-red-50 text-red-700 border border-red-200">
            <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
            Open
          </span>
        );
    }
  };

  const getCategoryTag = (category: CategoryType) => {
    switch (category) {
      case 'Roadways':
        return (
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg bg-orange-50 text-orange-800 border border-orange-200">
            <Hammer className="w-3.5 h-3.5 text-orange-600" />
            Roadways
          </span>
        );
      case 'Electricity':
        return (
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg bg-amber-50 text-amber-800 border border-amber-200">
            <Zap className="w-3.5 h-3.5 text-amber-600" />
            Electricity
          </span>
        );
      case 'Waste Management':
        return (
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg bg-emerald-50 text-emerald-800 border border-emerald-200">
            <Trash2 className="w-3.5 h-3.5 text-emerald-600" />
            Waste Mgmt
          </span>
        );
      case 'Sewage':
        return (
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg bg-sky-50 text-sky-800 border border-sky-200">
            <Droplets className="w-3.5 h-3.5 text-sky-600" />
            Sewage
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-lg bg-stone-100 text-stone-800 border border-stone-200">
            {category}
          </span>
        );
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Page Title & Intro */}
      <div>
        <h2 className="text-xl font-bold text-stone-900 tracking-tight">
          Details &amp; Triage
        </h2>
        <p className="text-xs text-stone-500 font-medium mt-0.5">
          Deep-dive into civic issues, track status &amp; manage resolution workflow
        </p>
      </div>

      {/* Main Grid: Left Filter & Table (approx 65%), Right Details Drawer (approx 35%) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Section (col-span-8 on lg) */}
        <div className="lg:col-span-8 space-y-5">
          {/* Top Filter Panel */}
          <div className="bg-transparent p-2">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-5 items-center">
              {/* Select City / ULB dropdown */}
              <div className="md:col-span-5">
                <label className="block text-xs font-bold text-stone-700 mb-1.5">
                  Select City / ULB
                </label>
                <div className="relative">
                  <div className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400">
                    <Building className="w-4 h-4" />
                  </div>
                  <select
                    value={selectedCity}
                    onChange={(e) => setSelectedCity(e.target.value)}
                    className="w-full appearance-none glass-pill rounded-xl pl-9 pr-8 py-2 text-xs font-semibold text-stone-800 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
                  >
                    <option value="Ranchi Municipal Corporation">
                      Ranchi Municipal Corporation
                    </option>
                    <option value="Dhanbad Municipal Corporation">
                      Dhanbad Municipal Corporation
                    </option>
                    <option value="Jamshedpur Notified Area Committee">
                      Jamshedpur Notified Area
                    </option>
                    <option value="Bokaro Steel City">Bokaro Steel City</option>
                    <option value="Deoghar Municipal Corporation">
                      Deoghar Municipal Corp
                    </option>
                    <option value="All Cities">All Urban Local Bodies</option>
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-stone-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              {/* Category Pills and Status */}
              <div className="md:col-span-7 space-y-3">
                {/* Category multi-select */}
                <div>
                  <span className="block text-xs font-bold text-stone-700 mb-1.5">
                    Category <span className="font-normal text-stone-400">(Select one or more)</span>
                  </span>
                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={() => toggleCategory('Electricity')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                        selectedCategories.includes('Electricity')
                          ? 'bg-[#ea580c] text-white border-[#ea580c] shadow-xs'
                          : 'glass-pill text-stone-700 hover:bg-white/90'
                      }`}
                    >
                      <Zap className="w-3.5 h-3.5 text-amber-500" />
                      <span>Electricity</span>
                    </button>

                    <button
                      onClick={() => toggleCategory('Roadways')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                        selectedCategories.includes('Roadways')
                          ? 'bg-[#ea580c] text-white border-[#ea580c] shadow-xs'
                          : 'glass-pill text-stone-700 hover:bg-white/90'
                      }`}
                    >
                      <Hammer className="w-3.5 h-3.5 text-amber-600" />
                      <span>Roadways</span>
                    </button>

                    <button
                      onClick={() => toggleCategory('Water Supply')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                        selectedCategories.includes('Water Supply')
                          ? 'bg-[#ea580c] text-white border-[#ea580c] shadow-xs'
                          : 'glass-pill text-stone-700 hover:bg-white/90'
                      }`}
                    >
                      <Droplets className="w-3.5 h-3.5 text-blue-500" />
                      <span>Water Supply</span>
                    </button>

                    <button
                      onClick={() => toggleCategory('Waste Management')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                        selectedCategories.includes('Waste Management')
                          ? 'bg-[#ea580c] text-white border-[#ea580c] shadow-xs'
                          : 'glass-pill text-stone-700 hover:bg-white/90'
                      }`}
                    >
                      <Trash2 className="w-3.5 h-3.5 text-orange-600" />
                      <span>Waste Mgmt</span>
                    </button>
                  </div>
                </div>

                {/* Status radio row & Clear filter */}
                <div className="flex items-center justify-between pt-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-stone-700 mr-1">Status:</span>
                    <button
                      onClick={() => setSelectedStatus(selectedStatus === 'Open' ? 'All' : 'Open')}
                      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-semibold border ${
                        selectedStatus === 'Open'
                          ? 'bg-red-50/80 text-red-700 border-red-300 ring-1 ring-red-400'
                          : 'glass-pill text-stone-600 hover:bg-white/90'
                      }`}
                    >
                      <span className="w-2 h-2 rounded-full bg-red-500" />
                      <span>Open</span>
                    </button>

                    <button
                      onClick={() => setSelectedStatus(selectedStatus === 'In Progress' ? 'All' : 'In Progress')}
                      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-semibold border ${
                        selectedStatus === 'In Progress'
                          ? 'bg-amber-50/80 text-amber-800 border-amber-300 ring-1 ring-amber-400'
                          : 'glass-pill text-stone-600 hover:bg-white/90'
                      }`}
                    >
                      <span className="w-2 h-2 rounded-full bg-amber-500" />
                      <span>In Progress</span>
                    </button>

                    <button
                      onClick={() => setSelectedStatus(selectedStatus === 'Resolved' ? 'All' : 'Resolved')}
                      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-semibold border ${
                        selectedStatus === 'Resolved'
                          ? 'bg-emerald-50/80 text-emerald-700 border-emerald-300 ring-1 ring-emerald-400'
                          : 'glass-pill text-stone-600 hover:bg-white/90'
                      }`}
                    >
                      <span className="w-2 h-2 rounded-full bg-emerald-500" />
                      <span>Resolved</span>
                    </button>
                  </div>

                  <button
                    onClick={handleClearFilters}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-stone-500 hover:text-[#ea580c] transition-colors"
                  >
                    <RotateCcw className="w-3 h-3" />
                    <span>Clear Filters</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Search, Action bar & Total count */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
            {/* Search Input */}
            <div className="relative flex-1 w-full sm:max-w-md">
              <Search className="w-4 h-4 text-stone-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search by Issue ID, Location, or Keyword..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full glass-pill rounded-xl pl-9 pr-4 py-2 text-xs font-medium text-stone-800 placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-[#ea580c]"
              />
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-end">
              <button
                onClick={handleExportCSV}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 glass-pill hover:bg-white/90 text-stone-700 rounded-xl text-xs font-semibold shadow-xs"
              >
                <Download className="w-3.5 h-3.5 text-stone-500" />
                <span>Export CSV</span>
              </button>

              <span className="text-xs text-stone-500 font-semibold">
                Total: <strong className="text-stone-900 font-mono">{filteredIssues.length}</strong>
              </span>
            </div>
          </div>

          {/* Main Table Card */}
          <div className="glass-card rounded-3xl overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="bg-white/40 border-b border-white/60 text-stone-500 font-bold uppercase text-[10px] tracking-wider">
                    <th className="py-3.5 px-4">Issue ID</th>
                    <th className="py-3.5 px-3">Photo</th>
                    <th className="py-3.5 px-3">Category</th>
                    <th className="py-3.5 px-4">Ward / Location</th>
                    <th className="py-3.5 px-3">
                      <div className="flex items-center gap-1 cursor-pointer">
                        <span>Time Reported</span>
                        <ChevronDown className="w-3 h-3 text-stone-500" />
                      </div>
                    </th>
                    <th className="py-3.5 px-3">Assigned Department</th>
                    <th className="py-3.5 px-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/50">
                  {filteredIssues.slice(0, rowsPerPage).map((issue) => {
                    const isSelected = selectedIssue?.id === issue.id;
                    return (
                      <tr
                        key={issue.id}
                        onClick={() => onSelectIssue(issue.id)}
                        className={`cursor-pointer transition-all ${
                          isSelected
                            ? 'bg-orange-500/10 hover:bg-orange-500/15'
                            : 'hover:bg-white/50'
                        }`}
                      >
                        {/* Issue ID */}
                        <td className="py-3.5 px-4 font-mono font-bold text-[#c2410c] whitespace-nowrap">
                          {issue.id}
                        </td>

                        {/* Photo Thumbnail */}
                        <td className="py-3.5 px-3">
                          <img
                            src={issue.photoUrl}
                            alt={issue.title}
                            onClick={(e) => {
                              e.stopPropagation();
                              setFullscreenPhoto(issue.photoUrl);
                            }}
                            className="w-12 h-9 rounded-lg object-cover ring-1 ring-white/60 shadow-2xs hover:scale-105 transition-transform"
                          />
                        </td>

                        {/* Category */}
                        <td className="py-3.5 px-3 whitespace-nowrap">
                          {getCategoryTag(issue.category)}
                        </td>

                        {/* Ward / Location */}
                        <td className="py-3.5 px-4 max-w-[200px]">
                          <div className="font-bold text-stone-900 truncate">
                            {issue.ward.split(',')[0]}
                          </div>
                          <div className="text-[11px] text-stone-500 truncate">
                            {issue.ward.split(',').slice(1).join(',')}
                          </div>
                        </td>

                        {/* Time Reported */}
                        <td className="py-3.5 px-3 whitespace-nowrap text-stone-700">
                          <div className="font-medium text-stone-900">
                            {issue.reportedAt.split(',')[0]}
                          </div>
                          <div className="text-[11px] text-stone-500">
                            {issue.reportedAt.split(',')[1]}
                          </div>
                        </td>

                        {/* Assigned Department */}
                        <td className="py-3.5 px-3 whitespace-nowrap">
                          <div className="flex items-center gap-1.5 text-stone-700 font-medium">
                            <Building className="w-3.5 h-3.5 text-stone-400 shrink-0" />
                            <span>{issue.assignedDept}</span>
                          </div>
                        </td>

                        {/* Status */}
                        <td className="py-3.5 px-3 whitespace-nowrap">
                          {getStatusBadge(issue.status)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Table Pagination Footer */}
            <div className="p-4 border-t border-stone-200/80 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-stone-500">
              <div>
                Showing <strong className="text-stone-900">{(currentPage - 1) * 10 + 1}</strong> to{' '}
                <strong className="text-stone-900">{Math.min(currentPage * 10, 50)}</strong> of{' '}
                <strong className="text-stone-900 font-mono">50</strong> issues
              </div>

              {/* Pagination controls: exactly 5 pages */}
              <div className="flex items-center gap-1">
                <button
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                  className="w-7 h-7 flex items-center justify-center rounded-lg border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 disabled:opacity-40 transition-colors"
                  title="Previous Page"
                >
                  <ChevronLeft className="w-3.5 h-3.5" />
                </button>

                {[1, 2, 3, 4, 5].map((pageNum) => (
                  <button
                    key={pageNum}
                    onClick={() => setCurrentPage(pageNum)}
                    className={`w-7 h-7 flex items-center justify-center rounded-lg text-xs font-bold transition-all ${
                      currentPage === pageNum
                        ? 'bg-[#ea580c] text-white shadow-xs'
                        : 'border border-stone-200 bg-white hover:bg-stone-50 text-stone-700'
                    }`}
                  >
                    {pageNum}
                  </button>
                ))}

                <button
                  disabled={currentPage === 5}
                  onClick={() => setCurrentPage((p) => Math.min(p + 1, 5))}
                  className="w-7 h-7 flex items-center justify-center rounded-lg border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 disabled:opacity-40 transition-colors"
                  title="Next Page"
                >
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Right Section: Issue Details Panel (col-span-4 on lg) */}
        <div className="lg:col-span-4 space-y-4 sticky top-20">
          {selectedIssue ? (
            <div className="glass-panel rounded-3xl p-5 space-y-5 animate-in fade-in">
              {/* Drawer Top Header */}
              <div className="flex items-center justify-between pb-3 border-b border-stone-100">
                <div className="flex items-center gap-2">
                  <h3 className="text-base font-bold text-stone-900">Issue Details</h3>
                </div>
                <button
                  onClick={() => setFullscreenPhoto(selectedIssue.photoUrl)}
                  className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-stone-100 text-stone-500"
                  title="Expand preview"
                >
                  <Maximize2 className="w-4 h-4" />
                </button>
              </div>

              {/* Issue ID Header and Share Button */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="text-xl font-extrabold text-[#c2410c] font-mono">
                    {selectedIssue.id}
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-50 text-amber-800 border border-amber-200">
                    {selectedIssue.status}
                  </span>
                </div>

                <button
                  onClick={() => {
                    setToastMessage('Issue link copied to clipboard');
                    setTimeout(() => setToastMessage(null), 2500);
                  }}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-stone-200 hover:bg-stone-50 text-xs font-semibold text-stone-700"
                >
                  <Share2 className="w-3.5 h-3.5 text-stone-500" />
                  <span>Share</span>
                </button>
              </div>

              {/* Dual Media: Photo preview + Location Mini-Map tile */}
              <div className="grid grid-cols-2 gap-3">
                {/* Photo */}
                <div className="relative rounded-xl overflow-hidden border border-stone-200 group h-32 bg-stone-100">
                  <img
                    src={selectedIssue.photoUrl}
                    alt={selectedIssue.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-2">
                    <span className="text-[10px] text-white font-medium">Click to zoom</span>
                  </div>
                </div>

                {/* Location Vector Mini Map */}
                <div className="rounded-xl border border-stone-200 bg-[#e8efe9] p-2.5 flex flex-col justify-between h-32 relative overflow-hidden">
                  <div className="flex items-center gap-1.5 z-10">
                    <MapPin className="w-4 h-4 text-[#dc2626] shrink-0" />
                    <span className="text-xs font-bold text-stone-900 truncate">
                      Kanke Road
                    </span>
                  </div>
                  <p className="text-[10px] text-stone-600 z-10">
                    Ranchi, Jharkhand
                  </p>

                  {/* GPS Box */}
                  <div className="bg-white/90 backdrop-blur-xs p-1.5 rounded-lg border border-stone-200 z-10 flex items-center justify-between">
                    <div>
                      <div className="text-[8px] font-bold text-stone-400 uppercase tracking-wide">
                        GPS Coordinates
                      </div>
                      <div className="text-[10px] font-mono font-bold text-stone-800">
                        {selectedIssue.coordinates.formatted}
                      </div>
                    </div>
                    <button
                      onClick={handleCopyCoordinates}
                      className="p-1 rounded hover:bg-stone-100 text-stone-500"
                      title="Copy GPS coordinates"
                    >
                      {copiedCoords ? (
                        <Check className="w-3.5 h-3.5 text-emerald-600" />
                      ) : (
                        <Copy className="w-3.5 h-3.5 text-stone-400" />
                      )}
                    </button>
                  </div>

                  {/* Decorative map grid background */}
                  <svg className="absolute inset-0 w-full h-full opacity-30 pointer-events-none" viewBox="0 0 100 100">
                    <line x1="0" y1="30" x2="100" y2="30" stroke="#94a3b8" strokeWidth="2" />
                    <line x1="0" y1="70" x2="100" y2="70" stroke="#fde047" strokeWidth="3" />
                    <line x1="40" y1="0" x2="40" y2="100" stroke="#94a3b8" strokeWidth="2" />
                    <circle cx="40" cy="70" r="4" fill="#dc2626" />
                  </svg>
                </div>
              </div>

              {/* Metadata Fields */}
              <div className="space-y-3 pt-2 text-xs divide-y divide-stone-100">
                {/* Category */}
                <div className="flex items-center justify-between pt-2">
                  <div className="flex items-center gap-2 text-stone-500 font-medium">
                    <SlidersHorizontal className="w-3.5 h-3.5" />
                    <span>Category</span>
                  </div>
                  <span className="font-bold text-stone-900">
                    {selectedIssue.categoryLabel || selectedIssue.category}
                  </span>
                </div>

                {/* Reported By */}
                <div className="flex items-center justify-between pt-2">
                  <div className="flex items-center gap-2 text-stone-500 font-medium">
                    <UserCheck className="w-3.5 h-3.5" />
                    <span>Reported By</span>
                  </div>
                  <div className="flex items-center gap-1 font-bold text-stone-900">
                    <span>{selectedIssue.reportedBy.type}</span>
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                  </div>
                </div>

                {/* Time Reported */}
                <div className="flex items-center justify-between pt-2">
                  <div className="flex items-center gap-2 text-stone-500 font-medium">
                    <Clock className="w-3.5 h-3.5" />
                    <span>Time Reported</span>
                  </div>
                  <span className="font-semibold text-stone-800 font-mono">
                    {selectedIssue.reportedAt}
                  </span>
                </div>

                {/* Ward / Location */}
                <div className="flex items-start justify-between pt-2 gap-4">
                  <div className="flex items-center gap-2 text-stone-500 font-medium shrink-0">
                    <MapPin className="w-3.5 h-3.5" />
                    <span>Ward / Location</span>
                  </div>
                  <span className="font-bold text-stone-900 text-right">
                    {selectedIssue.ward}
                  </span>
                </div>

                {/* Issue Description */}
                <div className="pt-2">
                  <div className="flex items-center gap-2 text-stone-500 font-medium mb-1">
                    <FileText className="w-3.5 h-3.5" />
                    <span>Issue Description</span>
                  </div>
                  <p className="text-stone-700 leading-relaxed font-medium bg-stone-50/70 p-2.5 rounded-xl border border-stone-100">
                    {selectedIssue.description}
                  </p>
                </div>
              </div>

              {/* Citizen Voice Note (Auto Transcription) */}
              {selectedIssue.voiceNote && (
                <div className="bg-[#fffbf6] rounded-2xl border border-[#fed7aa] p-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-1.5 text-xs font-bold text-stone-800">
                      <FileText className="w-3.5 h-3.5 text-[#ea580c]" />
                      <span>Citizen Voice Note (Auto Transcription)</span>
                    </div>
                  </div>

                  {/* Audio Waveform Player */}
                  <div className="flex items-center gap-3 bg-white p-2.5 rounded-xl border border-orange-200/80 shadow-2xs">
                    <button
                      onClick={togglePlayAudio}
                      className="w-8 h-8 rounded-full bg-[#ea580c] hover:bg-[#c2410c] text-white flex items-center justify-center shadow-xs transition-colors shrink-0"
                    >
                      {isPlayingAudio ? (
                        <Pause className="w-3.5 h-3.5 fill-current" />
                      ) : (
                        <Play className="w-3.5 h-3.5 fill-current ml-0.5" />
                      )}
                    </button>

                    {/* Waveform Bars Visualizer */}
                    <div className="flex-1 flex items-center gap-0.5 h-6 overflow-hidden">
                      {selectedIssue.voiceNote.audioWaves.map((height, i) => {
                        const isPast = (i / selectedIssue.voiceNote!.audioWaves.length) * 18 <= audioProgress;
                        return (
                          <div
                            key={i}
                            className={`flex-1 rounded-full transition-all duration-150 ${
                              isPast ? 'bg-[#ea580c]' : 'bg-stone-300'
                            }`}
                            style={{
                              height: isPlayingAudio ? `${Math.max(20, (height + (i % 3) * 10) % 100)}%` : `${height}%`,
                            }}
                          />
                        );
                      })}
                    </div>

                    <span className="text-xs font-mono font-semibold text-stone-500 shrink-0">
                      {isPlayingAudio
                        ? `00:${String(audioProgress).padStart(2, '0')}`
                        : selectedIssue.voiceNote.duration}
                    </span>
                  </div>

                  {/* Transcription text box */}
                  <div className="text-xs text-stone-700 bg-white/70 p-3 rounded-xl border border-orange-100/80 font-serif italic leading-relaxed">
                    {showEnglishTranslation ? selectedIssue.voiceNote.englishText : selectedIssue.voiceNote.hindiText}
                    <div className="mt-1 flex items-center justify-between not-italic font-sans text-[10px] text-stone-400">
                      <span>— Transcribed {showEnglishTranslation ? '(English Translation)' : '(Hindi)'}</span>
                      <button
                        onClick={() => setShowEnglishTranslation(!showEnglishTranslation)}
                        className="text-[#ea580c] font-semibold hover:underline"
                      >
                        {showEnglishTranslation ? 'Show Hindi Original' : 'Translate to English'}
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* Action Buttons Footer */}
              <div className="pt-2 border-t border-stone-100 flex items-center gap-2">
                {/* More Actions Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setMoreActionsOpen(!moreActionsOpen)}
                    className="px-3 py-2 border border-stone-200 hover:bg-stone-50 rounded-xl text-xs font-semibold text-stone-700 flex items-center gap-1.5"
                  >
                    <span>More Actions</span>
                    <ChevronDown className="w-3 h-3 text-stone-400" />
                  </button>

                  {moreActionsOpen && (
                    <div className="absolute bottom-full left-0 mb-2 w-48 bg-white rounded-xl shadow-xl border border-stone-200 py-1.5 z-40 text-xs text-stone-700">
                      <button
                        onClick={() => {
                          setMoreActionsOpen(false);
                          window.print();
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-stone-50 flex items-center gap-2"
                      >
                        <Printer className="w-3.5 h-3.5 text-stone-400" />
                        <span>Print Grievance Slip</span>
                      </button>
                      <button
                        onClick={() => {
                          setMoreActionsOpen(false);
                          setToastMessage('SMS confirmation dispatched to citizen');
                          setTimeout(() => setToastMessage(null), 2500);
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-stone-50 flex items-center gap-2"
                      >
                        <Send className="w-3.5 h-3.5 text-stone-400" />
                        <span>Send SMS to Citizen</span>
                      </button>
                      <button
                        onClick={() => {
                          setMoreActionsOpen(false);
                          onEscalate(selectedIssue);
                        }}
                        className="w-full text-left px-3 py-2 hover:bg-red-50 text-red-600 flex items-center gap-2"
                      >
                        <AlertTriangle className="w-3.5 h-3.5 text-red-500" />
                        <span>Escalate to Nodal Officer</span>
                      </button>
                    </div>
                  )}
                </div>

                {/* Reassign Button */}
                <button
                  onClick={() => onReassign(selectedIssue)}
                  className="flex-1 py-2 px-3 bg-stone-100 hover:bg-stone-200 text-stone-800 font-bold rounded-xl text-xs flex items-center justify-center gap-1.5 transition-colors"
                >
                  <UserCheck className="w-3.5 h-3.5" />
                  <span>Reassign</span>
                </button>

                {/* Mark as Resolved Button */}
                <button
                  onClick={() => onResolve(selectedIssue)}
                  className="flex-1 py-2 px-3 bg-[#c2410c] hover:bg-[#9a3412] text-white font-bold rounded-xl text-xs flex items-center justify-center gap-1.5 shadow-xs transition-colors"
                >
                  <CheckCircle className="w-3.5 h-3.5" />
                  <span>Mark as Resolved</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-stone-200 p-8 text-center text-stone-400 text-xs">
              Select an issue from the list to view full details
            </div>
          )}
        </div>
      </div>

      {/* Fullscreen Photo Modal */}
      {fullscreenPhoto && (
        <div
          onClick={() => setFullscreenPhoto(null)}
          className="fixed inset-0 z-50 bg-black/80 backdrop-blur-xs flex items-center justify-center p-4 animate-in fade-in"
        >
          <div className="relative max-w-3xl w-full bg-stone-900 rounded-2xl overflow-hidden p-2">
            <img
              src={fullscreenPhoto}
              alt="Full Preview"
              className="w-full max-h-[80vh] object-contain rounded-xl"
            />
            <p className="text-center text-xs text-stone-400 mt-2">
              Click anywhere to dismiss preview
            </p>
          </div>
        </div>
      )}

      {/* Action Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 bg-stone-900 text-white px-4 py-2.5 rounded-xl shadow-xl border border-stone-700 text-xs font-semibold flex items-center gap-2 animate-in slide-in-from-bottom-3">
          <Sparkles className="w-4 h-4 text-amber-400" />
          <span>{toastMessage}</span>
        </div>
      )}
    </div>
  );
};
