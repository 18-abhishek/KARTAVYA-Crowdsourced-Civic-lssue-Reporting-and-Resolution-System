import React, { useState } from 'react';
import { TabType, CivicIssue, DistrictMetric, UserProfile } from './types';
import { initialIssues, districtList, currentUser } from './data/mockData';
import { Header } from './components/Header';
import { DashboardView } from './components/DashboardView';
import { WeeklyReportView } from './components/WeeklyReportView';
import { DetailsView } from './components/DetailsView';
import { NotificationsView } from './components/NotificationsView';
import { WarningNoticeModal } from './components/WarningNoticeModal';
import { ReassignModal } from './components/ReassignModal';
import { ResolveModal } from './components/ResolveModal';
import { PenaltyModal } from './components/PenaltyModal';
import { DistrictModal } from './components/DistrictModal';
import { ExportReportModal } from './components/ExportReportModal';
import { Sparkles, CheckCircle2, AlertTriangle, ShieldCheck } from 'lucide-react';

export function App() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const [issues, setIssues] = useState<CivicIssue[]>(initialIssues);
  const [districts, setDistricts] = useState<DistrictMetric[]>(districtList);
  const [selectedDistrict, setSelectedDistrict] = useState<DistrictMetric | null>(districtList[0]);
  const [selectedIssueId, setSelectedIssueId] = useState<string>('#JH-9821');
  const [user] = useState<UserProfile>(currentUser);

  // Modals state
  const [warningModalIssue, setWarningModalIssue] = useState<CivicIssue | null>(null);
  const [penaltyModalIssue, setPenaltyModalIssue] = useState<CivicIssue | null>(null);
  const [reassignModalIssue, setReassignModalIssue] = useState<CivicIssue | null>(null);
  const [resolveModalIssue, setResolveModalIssue] = useState<CivicIssue | null>(null);
  const [districtModalData, setDistrictModalData] = useState<DistrictMetric | null>(null);
  const [showExportPdfModal, setShowExportPdfModal] = useState<boolean>(false);

  // Global toast
  const [globalToast, setGlobalToast] = useState<{ message: string; type: 'success' | 'warning' | 'info' } | null>(null);

  const showToast = (message: string, type: 'success' | 'warning' | 'info' = 'success') => {
    setGlobalToast({ message, type });
    setTimeout(() => setGlobalToast(null), 4000);
  };

  // Reassign issue handler
  const handleConfirmReassign = (issueId: string, newDept: string, newContractor: string, reason: string) => {
    setIssues((prev) =>
      prev.map((item) =>
        item.id === issueId
          ? {
              ...item,
              assignedDept: newDept,
              assignedContractor: newContractor,
              status: 'Assigned',
            }
          : item
      )
    );
    showToast(`Issue ${issueId} reassigned to ${newContractor} (${newDept})`, 'info');
  };

  // Mark as resolved handler
  const handleConfirmResolve = (
    issueId: string,
    resolutionData: { notes: string; photoUrl: string; resolvedBy: string }
  ) => {
    setIssues((prev) =>
      prev.map((item) =>
        item.id === issueId
          ? {
              ...item,
              status: 'Resolved',
              timeElapsedPercent: 100,
            }
          : item
      )
    );
    showToast(`Issue ${issueId} certified as Resolved with field photo proof!`, 'success');
  };

  // Issue formal warning handler
  const handleConfirmWarning = (issueId: string, penaltyAmount: number, noticeText: string) => {
    setIssues((prev) =>
      prev.map((item) =>
        item.id === issueId
          ? {
              ...item,
              warningsIssued: (item.warningsIssued || 0) + 1,
            }
          : item
      )
    );
    showToast(
      `Official Show Cause Notice dispatched for ${issueId} with ₹${penaltyAmount.toLocaleString()} penalty clause.`,
      'warning'
    );
  };

  // Impose penalty handler
  const handleConfirmPenalty = (issueId: string, amount: number, clause: string) => {
    showToast(`Penalty fine of ₹${amount.toLocaleString()} imposed on defaulting contractor for ${issueId}.`, 'warning');
  };

  // Escalate to commissioner handler
  const handleEscalateToCommissioner = (issue: CivicIssue) => {
    showToast(`Grievance ${issue.id} directly escalated to Municipal Commissioner with high-priority memo.`, 'warning');
  };

  // Handle direct filter from district modal
  const handleFilterByDistrict = (districtName: string) => {
    const dist = districts.find((d) => d.name.toLowerCase().includes(districtName.toLowerCase()));
    if (dist) setSelectedDistrict(dist);
    setActiveTab('details');
  };

  const unreadNotificationsCount = issues.filter((i) => i.timeElapsedPercent >= 80 && i.status !== 'Resolved').length;

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#fef4ec] via-[#fbeade] to-[#f3d4c3] text-stone-900 flex flex-col font-sans selection:bg-[#ea580c]/20 selection:text-[#9a3412] relative overflow-x-clip">
      {/* ─── Traditional Tribal Art Background with Soft Orange Blur & Transparency ─── */}
      <div
        className="fixed inset-0 pointer-events-none z-0 opacity-25 mix-blend-multiply filter blur-[0.5px] bg-repeat bg-center"
        style={{
          backgroundImage: "url('/tribal_bg.png')",
          backgroundSize: '750px auto',
        }}
      />

      {/* ─── Ambient Warm Orange Glowing Blur Orbs ─── */}
      <div className="fixed inset-0 bg-gradient-to-br from-amber-500/15 via-orange-500/10 to-transparent pointer-events-none z-0" />
      <div className="fixed -top-40 -left-40 w-[700px] h-[700px] bg-gradient-to-br from-orange-500/30 via-amber-400/25 to-transparent rounded-full blur-3xl pointer-events-none z-0" />
      <div className="fixed top-1/3 -right-40 w-[650px] h-[650px] bg-gradient-to-bl from-orange-400/35 via-rose-400/20 to-transparent rounded-full blur-3xl pointer-events-none z-0" />
      <div className="fixed -bottom-40 left-1/4 w-[750px] h-[750px] bg-gradient-to-tr from-amber-400/25 via-orange-300/30 to-transparent rounded-full blur-3xl pointer-events-none z-0" />

      {/* Top Header */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={user}
        unreadNotificationsCount={unreadNotificationsCount}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-[1750px] w-full mx-auto px-4 sm:px-6 lg:px-8 pt-22 sm:pt-24 pb-6 sm:pb-8 relative z-10">
        {activeTab === 'dashboard' && (
          <DashboardView
            issues={issues}
            districts={districts}
            selectedDistrict={selectedDistrict}
            onSelectDistrict={(d) => setSelectedDistrict(d)}
            onOpenDistrictModal={(d) => setDistrictModalData(d || selectedDistrict)}
            setActiveTab={setActiveTab}
            onSelectIssueForDetails={(id) => {
              setSelectedIssueId(id);
              setActiveTab('details');
            }}
          />
        )}

        {activeTab === 'weekly-report' && (
          <WeeklyReportView
            onExportPdf={() => setShowExportPdfModal(true)}
          />
        )}

        {activeTab === 'details' && (
          <DetailsView
            issues={issues}
            selectedIssueId={selectedIssueId}
            onSelectIssue={(id) => setSelectedIssueId(id)}
            onReassign={(issue) => setReassignModalIssue(issue)}
            onResolve={(issue) => setResolveModalIssue(issue)}
            onEscalate={(issue) => handleEscalateToCommissioner(issue)}
          />
        )}

        {activeTab === 'notifications' && (
          <NotificationsView
            issues={issues}
            onIssueFormalWarning={(issue) => setWarningModalIssue(issue)}
            onEscalateCommissioner={(issue) => handleEscalateToCommissioner(issue)}
            onImposePenalty={(issue) => setPenaltyModalIssue(issue)}
            onSelectIssueForDetails={(id) => {
              setSelectedIssueId(id);
              setActiveTab('details');
            }}
            onNavigateToDetails={() => setActiveTab('details')}
          />
        )}
      </main>

      {/* Global Toast Alert */}
      {globalToast && (
        <div
          className={`fixed bottom-6 right-6 z-50 px-4 py-3 rounded-2xl shadow-2xl border text-xs font-bold flex items-center gap-3 animate-in slide-in-from-bottom-4 ${
            globalToast.type === 'success'
              ? 'bg-stone-900 text-white border-emerald-500'
              : globalToast.type === 'warning'
              ? 'bg-[#450a0a] text-white border-red-500'
              : 'bg-stone-900 text-white border-orange-500'
          }`}
        >
          {globalToast.type === 'success' ? (
            <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          ) : globalToast.type === 'warning' ? (
            <AlertTriangle className="w-5 h-5 text-red-400 shrink-0" />
          ) : (
            <ShieldCheck className="w-5 h-5 text-amber-400 shrink-0" />
          )}
          <span>{globalToast.message}</span>
        </div>
      )}

      {/* Modals */}
      {warningModalIssue && (
        <WarningNoticeModal
          issue={warningModalIssue}
          onClose={() => setWarningModalIssue(null)}
          onConfirmWarning={handleConfirmWarning}
        />
      )}

      {reassignModalIssue && (
        <ReassignModal
          issue={reassignModalIssue}
          onClose={() => setReassignModalIssue(null)}
          onConfirmReassign={handleConfirmReassign}
        />
      )}

      {resolveModalIssue && (
        <ResolveModal
          issue={resolveModalIssue}
          onClose={() => setResolveModalIssue(null)}
          onConfirmResolve={handleConfirmResolve}
        />
      )}

      {penaltyModalIssue && (
        <PenaltyModal
          issue={penaltyModalIssue}
          onClose={() => setPenaltyModalIssue(null)}
          onConfirmPenalty={handleConfirmPenalty}
        />
      )}

      {districtModalData && (
        <DistrictModal
          district={districtModalData}
          onClose={() => setDistrictModalData(null)}
          onFilterByDistrict={handleFilterByDistrict}
        />
      )}

      {showExportPdfModal && (
        <ExportReportModal
          onClose={() => setShowExportPdfModal(false)}
        />
      )}
    </div>
  );
}

export default App;
