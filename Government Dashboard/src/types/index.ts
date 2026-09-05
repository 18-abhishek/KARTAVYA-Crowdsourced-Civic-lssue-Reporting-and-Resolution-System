export type TabType = 'dashboard' | 'weekly-report' | 'details' | 'notifications';

export type CategoryType = 'Roadways' | 'Electricity' | 'Sewage' | 'Waste Management' | 'Water Supply' | 'Street Lights' | 'Public Health';

export type IssueStatus = 'Open' | 'In Progress' | 'Assigned' | 'Resolved' | 'Escalated';

export type PriorityType = 'Low' | 'Medium' | 'High' | 'Critical';

export interface TimelineEvent {
  time: string;
  status: string;
  by: string;
  note?: string;
}

export interface VoiceNote {
  duration: string;
  audioWaves: number[];
  hindiText: string;
  englishText: string;
}

export interface CivicIssue {
  id: string; // e.g. '#JH-9821'
  title: string;
  category: CategoryType;
  categoryLabel?: string;
  city: string; // e.g. 'Ranchi Municipal Corporation'
  ward: string; // e.g. 'Ward 29, Kanke Road, Ranchi'
  reportedAt: string; // e.g. '15 Aug 2025, 09:42 AM'
  timeAgo: string;
  assignedDept: string; // e.g. 'Road Works Department' / 'PWD (Roads)'
  assignedContractor?: string; // e.g. 'Shree Infra Solutions'
  contractorContact?: string;
  status: IssueStatus;
  priority: PriorityType;
  photoUrl: string;
  miniMapUrl?: string;
  coordinates: {
    lat: number;
    lng: number;
    formatted: string; // '23.3441° N, 85.3096° E'
  };
  reportedBy: {
    name: string;
    type: string; // 'Citizen (Verified)'
    isVerified: boolean;
    phone?: string;
  };
  description: string;
  voiceNote?: VoiceNote;
  slaTotalHours: number; // e.g. 72
  timeElapsedPercent: number; // e.g. 92
  timeRemainingHours: number; // e.g. 3.75 -> '3h 45m'
  timeRemainingFormatted: string;
  warningsIssued?: number;
  penaltyImposed?: number; // in INR
  timeline: TimelineEvent[];
  resolutionProof?: {
    photoUrl?: string;
    resolvedAt?: string;
    resolvedBy?: string;
    notes?: string;
  };
}

export interface DistrictMetric {
  id: string;
  name: string;
  hindiName?: string;
  coordinates: { x: number; y: number; lat: number; lng: number };
  totalIssues: number;
  resolved: number;
  inProgress: number;
  reportedThisWeek: number;
  resolvedThisWeek?: number;
  resolutionRate: number;
  density: 'High' | 'Medium' | 'Low';
  openIssuesCount: number;
  criticalIssuesCount: number;
  nodalOfficer: {
    name: string;
    designation: string;
    phone: string;
  };
  topDepartmentIssue: string;
}

export interface DepartmentStat {
  name: string;
  count: number;
  percentage: number;
  color: string;
  iconName: string;
}

export interface SLADataPoint {
  day: string;
  avgHours: number;
  slaLimit: number;
  resolvedCount: number;
}

export interface UserProfile {
  name: string;
  role: string;
  cadre: string;
  department: string;
  avatarUrl: string;
}
