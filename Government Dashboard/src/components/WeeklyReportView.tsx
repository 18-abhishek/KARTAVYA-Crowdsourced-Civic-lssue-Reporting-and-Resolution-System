import React, { useState, useRef, useEffect, useMemo } from 'react';
import { citizenRatings, districtList } from '../data/mockData';
import {
  LayoutDashboard,
  Inbox,
  CheckCircle2,
  Activity,
  Building2,
  MessageSquareQuote,
  GitCompare,
  Lightbulb,
  Calendar,
  Download,
  Info,
  ChevronDown,
  Clock,
  History,
  MapPin,
  Layers,
  ArrowRightLeft,
  ArrowUpRight,
  ArrowDownRight,
  TrendingUp,
  TrendingDown,
  Award,
  Sparkles,
  Filter,
  Search,
  Star,
  AlertTriangle,
  Check,
  Users,
  Wrench,
  ShieldAlert,
  FileText,
  BarChart3,
  PieChart,
  Zap,
  Droplets,
  Trash2,
  Flame,
  ThumbsUp,
  PhoneCall,
  Smartphone,
  Globe,
  MessageCircle,
  Building,
  CheckCircle,
  ExternalLink,
  ChevronRight,
  RefreshCw,
  SlidersHorizontal,
} from 'lucide-react';

interface WeeklyReportViewProps {
  onExportPdf: () => void;
}

interface WardData {
  name: string;
  reported: number;
  resolved: number;
  rate: string;
}

interface CityReportData {
  cityName: string;
  corporationName: string;
  totalReported: number;
  totalResolved: number;
  resolutionRate: number;
  reportedChange: string;
  reportedIsDown?: boolean;
  resolvedChange: string;
  resolvedIsUp?: boolean;
  rateChange: string;
  rateIsUp?: boolean;
  dataInsight: string;
  avgSlaHours: number;
  citizenScore: number;
  positiveRatingPercent: number;
  wards: WardData[];
  deptStats: { name: string; count: number; percentage: number; color: string }[];
  slaPoints: { day: string; avgHours: number }[];
}

interface WeeklyReportOption {
  id: string;
  label: string;
  weekNumber: string;
  month: string;
  isLatest?: boolean;
  totalReported: number;
  totalResolved: number;
  resolutionRate: number;
  reportedChange: string;
  reportedIsDown?: boolean;
  resolvedChange: string;
  resolvedIsUp?: boolean;
  rateChange: string;
  rateIsUp?: boolean;
  dataInsight: string;
  avgSlaHours: number;
  citizenScore: number;
  positiveRatingPercent: number;
  deptStats: { name: string; count: number; percentage: number; color: string }[];
  slaPoints: { day: string; avgHours: number }[];
}

const CITY_METRICS_DATABASE: Record<string, CityReportData> = {
  Ranchi: {
    cityName: 'Ranchi',
    corporationName: 'Ranchi Municipal Corporation (RMC)',
    totalReported: 2300,
    totalResolved: 2100,
    resolutionRate: 91.3,
    reportedChange: '↓ 8.4%',
    reportedIsDown: true,
    resolvedChange: '↑ 18.2%',
    resolvedIsUp: true,
    rateChange: '↑ 9.1%',
    dataInsight: 'Ranchi Municipal Corporation achieved a 91.3% resolution rate this week, with Ward 29 leading in rapid pothole repairs.',
    avgSlaHours: 22.8,
    citizenScore: 4.4,
    positiveRatingPercent: 88,
    wards: [
      { name: 'Ward 29 (Kanke)', reported: 490, resolved: 460, rate: '93.8%' },
      { name: 'Ward 12 (Main Rd)', reported: 460, resolved: 420, rate: '91.3%' },
      { name: 'Ward 18 (Harmu)', reported: 420, resolved: 380, rate: '90.4%' },
      { name: 'Ward 35 (Doranda)', reported: 380, resolved: 340, rate: '89.4%' },
      { name: 'Ward 4 (Bariatu)', reported: 320, resolved: 300, rate: '93.7%' },
      { name: 'Ward 22 (Lalpur)', reported: 230, resolved: 200, rate: '86.9%' },
    ],
    deptStats: [
      { name: 'Roadways / Potholes', count: 782, percentage: 34, color: '#6366f1' },
      { name: 'Waste Management', count: 690, percentage: 30, color: '#ec4899' },
      { name: 'Sewage & Drainage', count: 483, percentage: 21, color: '#06b6d4' },
      { name: 'Electricity & Lighting', count: 345, percentage: 15, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 24.1 },
      { day: 'Tue', avgHours: 28.5 },
      { day: 'Wed', avgHours: 23.0 },
      { day: 'Thu', avgHours: 19.4 },
      { day: 'Fri', avgHours: 26.2 },
      { day: 'Sat', avgHours: 21.0 },
      { day: 'Sun', avgHours: 17.5 },
    ],
  },
  Dhanbad: {
    cityName: 'Dhanbad',
    corporationName: 'Dhanbad Municipal Corporation (DMC)',
    totalReported: 2000,
    totalResolved: 1700,
    resolutionRate: 85.0,
    reportedChange: '↓ 5.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 12.0%',
    resolvedIsUp: true,
    rateChange: '↑ 6.4%',
    dataInsight: 'Dhanbad municipal teams resolved 1,700 issues with significant speed improvements in coal belt public lighting repairs.',
    avgSlaHours: 29.5,
    citizenScore: 4.1,
    positiveRatingPercent: 82,
    wards: [
      { name: 'Ward 14 (Station)', reported: 450, resolved: 390, rate: '86.6%' },
      { name: 'Ward 22 (Bank More)', reported: 420, resolved: 360, rate: '85.7%' },
      { name: 'Ward 8 (Saraidhela)', reported: 390, resolved: 340, rate: '87.1%' },
      { name: 'Ward 31 (Jharia Rd)', reported: 380, resolved: 310, rate: '81.5%' },
      { name: 'Ward 5 (Hirapur)', reported: 360, resolved: 300, rate: '83.3%' },
    ],
    deptStats: [
      { name: 'Electricity & Lighting', count: 620, percentage: 31, color: '#f59e0b' },
      { name: 'Waste Management', count: 560, percentage: 28, color: '#ec4899' },
      { name: 'Roadways / Potholes', count: 480, percentage: 24, color: '#6366f1' },
      { name: 'Sewage & Drainage', count: 340, percentage: 17, color: '#06b6d4' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 32.0 },
      { day: 'Tue', avgHours: 36.5 },
      { day: 'Wed', avgHours: 31.2 },
      { day: 'Thu', avgHours: 27.0 },
      { day: 'Fri', avgHours: 33.4 },
      { day: 'Sat', avgHours: 25.6 },
      { day: 'Sun', avgHours: 21.0 },
    ],
  },
  Jamshedpur: {
    cityName: 'Jamshedpur',
    corporationName: 'Jamshedpur Notified Area Committee (JNAC)',
    totalReported: 1800,
    totalResolved: 1500,
    resolutionRate: 83.3,
    reportedChange: '↓ 6.1%',
    reportedIsDown: true,
    resolvedChange: '↑ 14.5%',
    resolvedIsUp: true,
    rateChange: '↑ 7.2%',
    dataInsight: 'JNAC along with TSUISL achieved an 83.3% resolution rate, with Sakchi and Bistupur waste clearance operating at peak efficiency.',
    avgSlaHours: 26.4,
    citizenScore: 4.3,
    positiveRatingPercent: 86,
    wards: [
      { name: 'Sakchi Sector 4', reported: 440, resolved: 380, rate: '86.3%' },
      { name: 'Bistupur Market', reported: 410, resolved: 350, rate: '85.3%' },
      { name: 'Kadma Zone 2', reported: 360, resolved: 290, rate: '80.5%' },
      { name: 'Sonari Town', reported: 320, resolved: 260, rate: '81.2%' },
      { name: 'Golmuri Sector 1', reported: 270, resolved: 220, rate: '81.4%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 648, percentage: 36, color: '#ec4899' },
      { name: 'Roadways / Potholes', count: 486, percentage: 27, color: '#6366f1' },
      { name: 'Sewage & Drainage', count: 378, percentage: 21, color: '#06b6d4' },
      { name: 'Electricity & Lighting', count: 288, percentage: 16, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 27.5 },
      { day: 'Tue', avgHours: 31.0 },
      { day: 'Wed', avgHours: 28.2 },
      { day: 'Thu', avgHours: 23.5 },
      { day: 'Fri', avgHours: 29.0 },
      { day: 'Sat', avgHours: 24.1 },
      { day: 'Sun', avgHours: 21.8 },
    ],
  },
  Bokaro: {
    cityName: 'Bokaro',
    corporationName: 'Chas Municipal Corporation & Bokaro Steel City',
    totalReported: 1500,
    totalResolved: 1400,
    resolutionRate: 93.3,
    reportedChange: '↓ 11.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 20.4%',
    resolvedIsUp: true,
    rateChange: '↑ 12.1%',
    dataInsight: 'Bokaro recorded the highest SLA compliance across all urban centers with 93.3% of drainage and road repair orders delivered on time.',
    avgSlaHours: 19.6,
    citizenScore: 4.6,
    positiveRatingPercent: 92,
    wards: [
      { name: 'Sector 4 City Centre', reported: 380, resolved: 360, rate: '94.7%' },
      { name: 'Chas Ward 11', reported: 350, resolved: 330, rate: '94.2%' },
      { name: 'Sector 1 Market', reported: 310, resolved: 290, rate: '93.5%' },
      { name: 'Chas Ward 4', reported: 260, resolved: 240, rate: '92.3%' },
      { name: 'Sector 9 Ext', reported: 200, resolved: 180, rate: '90.0%' },
    ],
    deptStats: [
      { name: 'Roadways / Potholes', count: 480, percentage: 32, color: '#6366f1' },
      { name: 'Waste Management', count: 420, percentage: 28, color: '#ec4899' },
      { name: 'Sewage & Drainage', count: 360, percentage: 24, color: '#06b6d4' },
      { name: 'Electricity & Lighting', count: 240, percentage: 16, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 21.0 },
      { day: 'Tue', avgHours: 24.2 },
      { day: 'Wed', avgHours: 20.1 },
      { day: 'Thu', avgHours: 17.5 },
      { day: 'Fri', avgHours: 22.0 },
      { day: 'Sat', avgHours: 18.2 },
      { day: 'Sun', avgHours: 14.5 },
    ],
  },
  Deoghar: {
    cityName: 'Deoghar',
    corporationName: 'Deoghar Municipal Corporation (DMC)',
    totalReported: 900,
    totalResolved: 800,
    resolutionRate: 88.8,
    reportedChange: '↓ 4.8%',
    reportedIsDown: true,
    resolvedChange: '↑ 15.6%',
    resolvedIsUp: true,
    rateChange: '↑ 8.3%',
    dataInsight: 'Special Shravani pilgrimage sanitation protocol enabled Deoghar Nagar Nigam to clear 800+ waste and streetlight tickets.',
    avgSlaHours: 23.4,
    citizenScore: 4.3,
    positiveRatingPercent: 87,
    wards: [
      { name: 'Tower Chowk Sector', reported: 260, resolved: 240, rate: '92.3%' },
      { name: 'Baidyanath Dham Rd', reported: 240, resolved: 220, rate: '91.6%' },
      { name: 'Jasidih Jn Area', reported: 180, resolved: 155, rate: '86.1%' },
      { name: 'Castairs Town', reported: 130, resolved: 110, rate: '84.6%' },
      { name: 'Shivganga Ward 2', reported: 90, resolved: 75, rate: '83.3%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 378, percentage: 42, color: '#ec4899' },
      { name: 'Roadways / Potholes', count: 234, percentage: 26, color: '#6366f1' },
      { name: 'Electricity & Lighting', count: 162, percentage: 18, color: '#f59e0b' },
      { name: 'Sewage & Drainage', count: 126, percentage: 14, color: '#06b6d4' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 25.0 },
      { day: 'Tue', avgHours: 29.2 },
      { day: 'Wed', avgHours: 24.5 },
      { day: 'Thu', avgHours: 20.8 },
      { day: 'Fri', avgHours: 27.1 },
      { day: 'Sat', avgHours: 22.0 },
      { day: 'Sun', avgHours: 15.5 },
    ],
  },
  Hazaribagh: {
    cityName: 'Hazaribagh',
    corporationName: 'Hazaribagh Municipal Corporation (HMC)',
    totalReported: 620,
    totalResolved: 535,
    resolutionRate: 86.3,
    reportedChange: '↓ 3.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 11.4%',
    resolvedIsUp: true,
    rateChange: '↑ 5.8%',
    dataInsight: 'HMC resolved 535 grievances with quick action on water pipeline repairs along Main Road Ward 7.',
    avgSlaHours: 27.2,
    citizenScore: 4.2,
    positiveRatingPercent: 85,
    wards: [
      { name: 'Main Road Ward 7', reported: 180, resolved: 160, rate: '88.8%' },
      { name: 'Matwari Ward 14', reported: 150, resolved: 130, rate: '86.6%' },
      { name: 'Korrah Ward 9', reported: 130, resolved: 110, rate: '84.6%' },
      { name: 'Bada Bazar Ward 3', reported: 90, resolved: 75, rate: '83.3%' },
      { name: 'NawabGanj Ward 2', reported: 70, resolved: 60, rate: '85.7%' },
    ],
    deptStats: [
      { name: 'Water Supply', count: 180, percentage: 29, color: '#0284c7' },
      { name: 'Waste Management', count: 174, percentage: 28, color: '#ec4899' },
      { name: 'Roadways / Potholes', count: 148, percentage: 24, color: '#6366f1' },
      { name: 'Electricity & Lighting', count: 118, percentage: 19, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 29.0 },
      { day: 'Tue', avgHours: 33.2 },
      { day: 'Wed', avgHours: 28.5 },
      { day: 'Thu', avgHours: 24.8 },
      { day: 'Fri', avgHours: 31.0 },
      { day: 'Sat', avgHours: 26.2 },
      { day: 'Sun', avgHours: 18.0 },
    ],
  },
};

const AVAILABLE_CITIES = Object.keys(CITY_METRICS_DATABASE);

const HISTORICAL_WEEKLY_REPORTS: WeeklyReportOption[] = [
  {
    id: 'w33',
    label: 'Aug 08 - Aug 15, 2025 (Week 33)',
    weekNumber: 'Week 33',
    month: 'August 2025',
    isLatest: true,
    totalReported: 7850,
    totalResolved: 6470,
    resolutionRate: 82.6,
    reportedChange: '↓ 6.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 14.8%',
    resolvedIsUp: true,
    rateChange: '↑ 7.3%',
    dataInsight: 'Resolution rate improved by 15.3% compared to last week across urban municipal zones with 6,470 issues resolved.',
    avgSlaHours: 28.4,
    citizenScore: 4.2,
    positiveRatingPercent: 84,
    deptStats: [
      { name: 'Waste Management', count: 2983, percentage: 38, color: '#ec4899' },
      { name: 'Roadways / Potholes', count: 2120, percentage: 27, color: '#6366f1' },
      { name: 'Sewage & Drainage', count: 1570, percentage: 20, color: '#06b6d4' },
      { name: 'Electricity & Lighting', count: 1177, percentage: 15, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 28.4 },
      { day: 'Tue', avgHours: 35.2 },
      { day: 'Wed', avgHours: 31.0 },
      { day: 'Thu', avgHours: 24.8 },
      { day: 'Fri', avgHours: 33.5 },
      { day: 'Sat', avgHours: 26.1 },
      { day: 'Sun', avgHours: 22.4 },
    ],
  },
  {
    id: 'w32',
    label: 'Aug 01 - Aug 07, 2025 (Week 32)',
    weekNumber: 'Week 32',
    month: 'August 2025',
    totalReported: 8370,
    totalResolved: 6280,
    resolutionRate: 75.0,
    reportedChange: '↑ 4.1%',
    reportedIsDown: false,
    resolvedChange: '↑ 6.3%',
    resolvedIsUp: true,
    rateChange: '↑ 2.1%',
    dataInsight: 'Monsoon waterlogging caused a surge in drainage and road erosion complaints across Ranchi and Dhanbad.',
    avgSlaHours: 34.2,
    citizenScore: 4.0,
    positiveRatingPercent: 79,
    deptStats: [
      { name: 'Sewage & Drainage', count: 3013, percentage: 36, color: '#06b6d4' },
      { name: 'Roadways / Potholes', count: 2511, percentage: 30, color: '#6366f1' },
      { name: 'Waste Management', count: 1841, percentage: 22, color: '#ec4899' },
      { name: 'Electricity & Lighting', count: 1005, percentage: 12, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 38.2 },
      { day: 'Tue', avgHours: 42.0 },
      { day: 'Wed', avgHours: 36.5 },
      { day: 'Thu', avgHours: 31.0 },
      { day: 'Fri', avgHours: 39.4 },
      { day: 'Sat', avgHours: 30.2 },
      { day: 'Sun', avgHours: 25.8 },
    ],
  },
  {
    id: 'w31',
    label: 'Jul 25 - Jul 31, 2025 (Week 31)',
    weekNumber: 'Week 31',
    month: 'July 2025',
    totalReported: 8040,
    totalResolved: 5860,
    resolutionRate: 72.9,
    reportedChange: '↑ 2.8%',
    reportedIsDown: false,
    resolvedChange: '↑ 4.1%',
    resolvedIsUp: true,
    rateChange: '↑ 1.5%',
    dataInsight: 'Weekly dispatch speed in Jamshedpur and Bokaro increased by 11% following deployment of mobile pothole repair vans.',
    avgSlaHours: 37.8,
    citizenScore: 3.9,
    positiveRatingPercent: 76,
    deptStats: [
      { name: 'Roadways / Potholes', count: 2814, percentage: 35, color: '#6366f1' },
      { name: 'Waste Management', count: 2412, percentage: 30, color: '#ec4899' },
      { name: 'Sewage & Drainage', count: 1608, percentage: 20, color: '#06b6d4' },
      { name: 'Electricity & Lighting', count: 1206, percentage: 15, color: '#f59e0b' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 41.0 },
      { day: 'Tue', avgHours: 45.2 },
      { day: 'Wed', avgHours: 39.0 },
      { day: 'Thu', avgHours: 33.4 },
      { day: 'Fri', avgHours: 42.1 },
      { day: 'Sat', avgHours: 34.0 },
      { day: 'Sun', avgHours: 28.5 },
    ],
  },
  {
    id: 'w30',
    label: 'Jul 18 - Jul 24, 2025 (Week 30)',
    weekNumber: 'Week 30',
    month: 'July 2025',
    totalReported: 7820,
    totalResolved: 5580,
    resolutionRate: 71.4,
    reportedChange: '↑ 8.5%',
    reportedIsDown: false,
    resolvedChange: '↑ 3.2%',
    resolvedIsUp: true,
    rateChange: '↓ 1.4%',
    dataInsight: 'Heavy rainfall across Santhal Pargana led to 782 new electricity and transformer outage complaints.',
    avgSlaHours: 41.5,
    citizenScore: 3.8,
    positiveRatingPercent: 74,
    deptStats: [
      { name: 'Electricity & Lighting', count: 2502, percentage: 32, color: '#f59e0b' },
      { name: 'Roadways / Potholes', count: 2190, percentage: 28, color: '#6366f1' },
      { name: 'Waste Management', count: 1877, percentage: 24, color: '#ec4899' },
      { name: 'Sewage & Drainage', count: 1251, percentage: 16, color: '#06b6d4' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 44.5 },
      { day: 'Tue', avgHours: 49.0 },
      { day: 'Wed', avgHours: 43.2 },
      { day: 'Thu', avgHours: 38.0 },
      { day: 'Fri', avgHours: 46.8 },
      { day: 'Sat', avgHours: 38.5 },
      { day: 'Sun', avgHours: 32.8 },
    ],
  },
];

type SidebarTabType = 'overview' | 'inflow' | 'resolution' | 'sla' | 'departments' | 'feedback' | 'comparative';

export const WeeklyReportView: React.FC<WeeklyReportViewProps> = ({ onExportPdf }) => {
  const [sidebarTab, setSidebarTab] = useState<SidebarTabType>('overview');
  const [viewScope, setViewScope] = useState<'state' | 'city'>('state');
  const [selectedCity, setSelectedCity] = useState<string>('Ranchi');
  const [selectedTopDistrictsCount, setSelectedTopDistrictsCount] = useState<string>('Top 5 Districts');
  const [selectedWeekId, setSelectedWeekId] = useState<string>('w33');
  const [isDateDropdownOpen, setIsDateDropdownOpen] = useState(false);
  const [isCityDropdownOpen, setIsCityDropdownOpen] = useState(false);
  const [hoveredBarIndex, setHoveredBarIndex] = useState<number | null>(null);

  // Inflow Tab State
  const [inflowChannelFilter, setInflowChannelFilter] = useState<'all' | 'mobile' | 'web' | 'whatsapp' | 'helpline'>('all');

  // Resolution Tab State
  const [resolutionSortKey, setResolutionSortKey] = useState<'rate' | 'resolved' | 'total'>('rate');

  // Departments Tab State
  const [selectedDeptId, setSelectedDeptId] = useState<string>('waste');

  // Feedback Tab State
  const [feedbackStarFilter, setFeedbackStarFilter] = useState<number | 'all'>('all');
  const [feedbackSearch, setFeedbackSearch] = useState<string>('');

  // Comparative Tab State
  const [compDistrictA, setCompDistrictA] = useState<string>('ranchi');
  const [compDistrictB, setCompDistrictB] = useState<string>('dhanbad');

  const dateDropdownRef = useRef<HTMLDivElement>(null);
  const cityDropdownRef = useRef<HTMLDivElement>(null);

  const currentReport = HISTORICAL_WEEKLY_REPORTS.find((w) => w.id === selectedWeekId) || HISTORICAL_WEEKLY_REPORTS[0];
  const activeCityData = CITY_METRICS_DATABASE[selectedCity] || CITY_METRICS_DATABASE['Ranchi'];

  // Close dropdowns on outside click
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dateDropdownRef.current && !dateDropdownRef.current.contains(event.target as Node)) {
        setIsDateDropdownOpen(false);
      }
      if (cityDropdownRef.current && !cityDropdownRef.current.contains(event.target as Node)) {
        setIsCityDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const isCityMode = viewScope === 'city';

  // Active Metrics depending on State-wide vs City View
  const displayTotalReported = isCityMode ? activeCityData.totalReported : currentReport.totalReported;
  const displayTotalResolved = isCityMode ? activeCityData.totalResolved : currentReport.totalResolved;
  const displayResolutionRate = isCityMode ? activeCityData.resolutionRate : currentReport.resolutionRate;
  const displayReportedChange = isCityMode ? activeCityData.reportedChange : currentReport.reportedChange;
  const displayReportedIsDown = isCityMode ? activeCityData.reportedIsDown : currentReport.reportedIsDown;
  const displayResolvedChange = isCityMode ? activeCityData.resolvedChange : currentReport.resolvedChange;
  const displayResolvedIsUp = isCityMode ? activeCityData.resolvedIsUp : currentReport.resolvedIsUp;
  const displayRateChange = isCityMode ? activeCityData.rateChange : currentReport.rateChange;
  const displayRateIsUp = isCityMode ? activeCityData.rateIsUp : currentReport.rateIsUp;
  const displayDataInsight = isCityMode ? activeCityData.dataInsight : currentReport.dataInsight;
  const rawDeptStats = isCityMode ? activeCityData.deptStats : currentReport.deptStats;
  const DEPT_FIXED_WEIGHTS: Record<string, number> = {
    waste: 1,
    road: 2,
    pothole: 2,
    sewage: 3,
    drain: 3,
    electric: 4,
    light: 4,
    water: 5,
  };
  const getDeptWeight = (name: string) => {
    const n = name.toLowerCase();
    for (const [key, weight] of Object.entries(DEPT_FIXED_WEIGHTS)) {
      if (n.includes(key)) return weight;
    }
    return 99;
  };
  const displayDeptStats = [...rawDeptStats].sort((a, b) => getDeptWeight(a.name) - getDeptWeight(b.name));
  const displaySlaPoints = isCityMode ? activeCityData.slaPoints : currentReport.slaPoints;
  const displayAvgSlaHours = isCityMode ? activeCityData.avgSlaHours : currentReport.avgSlaHours;
  const displayCitizenScore = isCityMode ? activeCityData.citizenScore : currentReport.citizenScore;
  const displayPositivePercent = isCityMode ? activeCityData.positiveRatingPercent : currentReport.positiveRatingPercent;

  // Build All 24 Districts dataset proportional to the active week
  const weekRatio = currentReport.totalReported / 7850;
  const all24DistrictsData = districtList.map((d) => {
    const rawReported = d.reportedThisWeek || Math.round(d.totalIssues * 0.6);
    const rawResolved =
      d.resolvedThisWeek ||
      (d.resolutionRate ? Math.round(rawReported * (d.resolutionRate / 100)) : Math.round(rawReported * 0.85));

    // Ensure rawResolved is strictly less than rawReported
    const boundedResolved = Math.min(rawResolved, Math.max(1, rawReported - 1));

    const adjustedReported = Math.max(20, Math.round(rawReported * weekRatio));
    const adjustedResolved = Math.min(
      Math.max(1, adjustedReported - 1),
      Math.max(15, Math.round(boundedResolved * weekRatio))
    );
    const rate = ((adjustedResolved / adjustedReported) * 100).toFixed(1);
    return {
      district: d.name.replace(/ \(.*?\)/, ''), // e.g. "Jamshedpur", "Palamu", "West Singhbhum"
      reported: adjustedReported,
      resolved: adjustedResolved,
      rate: `${rate}%`,
    };
  });

  // Filter district list based on selectedTopDistrictsCount dropdown
  let stateDistrictItems = all24DistrictsData;
  if (selectedTopDistrictsCount === 'Top 5 Districts') {
    stateDistrictItems = all24DistrictsData.slice(0, 5);
  } else if (selectedTopDistrictsCount === 'Top 10 Districts') {
    stateDistrictItems = all24DistrictsData.slice(0, 10);
  } else {
    // All 24 Districts
    stateDistrictItems = all24DistrictsData;
  }

  // Active Bar Chart Dataset (Districts in State Mode vs Wards in City Mode)
  const barChartItems = isCityMode
    ? activeCityData.wards.map((w) => ({
        label: w.name,
        reported: w.reported,
        resolved: w.resolved,
        rate: w.rate,
      }))
    : stateDistrictItems.map((d) => ({
        label: d.district,
        reported: d.reported,
        resolved: d.resolved,
        rate: d.rate,
      }));

  const totalBarsCount = barChartItems.length;

  // Dynamic bar chart layout dimensions
  const maxBarValue = Math.max(...barChartItems.map((item) => item.reported), 100);
  const barScaleCeiling = isCityMode
    ? Math.ceil(maxBarValue / 100) * 100 + 50
    : Math.max(2500, Math.ceil(maxBarValue / 500) * 500);

  // SVG total width and spacing calculation for 5, 10, or 24 bars
  let svgCanvasWidth = 500;
  let barSpacing = 85;
  let barWidth = 24;
  let gapBetweenBars = 27;

  if (totalBarsCount <= 5) {
    svgCanvasWidth = 500;
    barSpacing = 85;
    barWidth = 24;
    gapBetweenBars = 27;
  } else if (totalBarsCount <= 10) {
    svgCanvasWidth = 780;
    barSpacing = 72;
    barWidth = 20;
    gapBetweenBars = 22;
  } else {
    // 24 districts
    svgCanvasWidth = 1720;
    barSpacing = 68;
    barWidth = 16;
    gapBetweenBars = 18;
  }

  const sidebarItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'inflow', label: 'Issues Inflow', icon: Inbox },
    { id: 'resolution', label: 'Resolution', icon: CheckCircle2 },
    { id: 'sla', label: 'SLA Performance', icon: Activity },
    { id: 'departments', label: 'Departments', icon: Building2 },
    { id: 'feedback', label: 'Citizen Feedback', icon: MessageSquareQuote },
    { id: 'comparative', label: 'Comparative View', icon: GitCompare },
  ];

  return (
    <div className="flex flex-col lg:flex-row gap-6 items-start">
      {/* Left Secondary Sidebar (w-56 breadth, sticky position) */}
      <div className="w-full lg:w-60 shrink-0 space-y-4 lg:sticky lg:top-22 self-start">
        <div className="glass-panel rounded-3xl p-3 border border-stone-200/50 space-y-1 bg-white">
          {sidebarItems.map((item) => {
            const Icon = item.icon;
            const isActive = sidebarTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setSidebarTab(item.id as SidebarTabType)}
                className={`w-full flex items-center gap-2.5 px-3.5 py-2.5 rounded-2xl text-xs font-semibold transition-all text-left ${
                  isActive
                    ? 'bg-[#c2410c] text-white shadow-md'
                    : 'text-stone-600 hover:text-stone-900 hover:bg-stone-50'
                }`}
              >
                <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-white' : 'text-stone-400'}`} />
                <span className="truncate">{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* Bottom Data Insights Banner Card */}
        <div className="bg-white/95 backdrop-blur-md rounded-3xl p-4 border border-stone-200/50 shadow-xs">
          <div className="flex items-center gap-1.5 text-[#ea580c] font-bold text-xs mb-1">
            <Lightbulb className="w-4 h-4 text-[#ea580c]" />
            <span>Data Insights</span>
          </div>
          <p className="text-[11px] text-stone-600 leading-relaxed font-medium">
            {displayDataInsight}
          </p>
        </div>
      </div>

      {/* Main Analytics Content */}
      <div className="w-full flex-1 min-w-0 space-y-6">
        {/* Top Controls Bar */}
        <div className="glass-panel rounded-3xl p-5 border border-stone-200/50 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white shadow-xs">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 text-[10px] font-bold rounded-md bg-[#ea580c]/10 text-[#ea580c] uppercase tracking-wider">
                {sidebarItems.find((s) => s.id === sidebarTab)?.label}
              </span>
              <span className="text-xs text-stone-400">•</span>
              <span className="text-xs font-semibold text-stone-500">
                {isCityMode ? `${selectedCity} Municipal Zone` : 'State of Jharkhand'}
              </span>
            </div>
            <h2 className="text-base sm:text-lg font-bold text-stone-900 tracking-tight mt-0.5">
              {sidebarTab === 'overview' && 'Weekly Performance & Analytics Report'}
              {sidebarTab === 'inflow' && 'Civic Issues Inflow & Channel Analytics'}
              {sidebarTab === 'resolution' && 'Resolution Velocity & Quality Scorecard'}
              {sidebarTab === 'sla' && 'SLA Compliance & Escalation Tracker'}
              {sidebarTab === 'departments' && 'Departmental Matrix & Resource Allocation'}
              {sidebarTab === 'feedback' && 'Citizen Feedback & Sentiment Intelligence'}
              {sidebarTab === 'comparative' && 'Multi-District Benchmarking & WoW Delta'}
            </h2>
            <p className="text-xs text-stone-500 font-medium">
              {isCityMode
                ? `Municipal performance breakdown for ${activeCityData.corporationName} • ${currentReport.label}`
                : `Comprehensive state-wide overview across all 24 districts • ${currentReport.label}`}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            {/* Interactive Date Range Dropdown Selector */}
            <div className="relative" ref={dateDropdownRef}>
              <button
                type="button"
                onClick={() => setIsDateDropdownOpen((prev) => !prev)}
                className={`inline-flex items-center gap-2 px-3 py-1.5 border rounded-xl text-xs font-bold transition-all shadow-2xs ${
                  isDateDropdownOpen
                    ? 'bg-[#fff5ee] border-[#c2410c] text-[#c2410c] ring-2 ring-[#c2410c]/20'
                    : 'bg-stone-50 hover:bg-stone-100 border-stone-200 text-stone-800'
                }`}
                title="Select historical report week"
              >
                <Calendar className="w-3.5 h-3.5 text-[#c2410c]" />
                <span className="font-semibold">{currentReport.label}</span>
                <ChevronDown
                  className={`w-3.5 h-3.5 text-stone-400 transition-transform duration-200 ${
                    isDateDropdownOpen ? 'rotate-180 text-[#c2410c]' : ''
                  }`}
                />
              </button>

              {/* Date Dropdown Menu */}
              {isDateDropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-72 bg-white rounded-2xl border border-stone-200 shadow-2xl z-50 p-2 animate-in fade-in zoom-in-95 origin-top-right">
                  <div className="px-3 py-2 border-b border-stone-100 mb-1 flex items-center justify-between">
                    <div className="flex items-center gap-1.5">
                      <History className="w-3.5 h-3.5 text-[#c2410c]" />
                      <h4 className="text-xs font-bold text-stone-900">Select Report Week</h4>
                    </div>
                    <span className="text-[10px] font-bold text-stone-500">Q3 2025</span>
                  </div>

                  <div className="space-y-1 py-1">
                    {HISTORICAL_WEEKLY_REPORTS.map((report) => {
                      const isSelected = report.id === selectedWeekId;
                      return (
                        <button
                          key={report.id}
                          type="button"
                          onClick={() => {
                            setSelectedWeekId(report.id);
                            setIsDateDropdownOpen(false);
                          }}
                          className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-left transition-all ${
                            isSelected
                              ? 'bg-[#fff5ee] text-[#c2410c] font-bold border border-[#fed7aa]'
                              : 'hover:bg-stone-50 text-stone-700 font-medium border border-transparent'
                          }`}
                        >
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="text-xs">{report.weekNumber}</span>
                              {report.isLatest && (
                                <span className="text-[9px] font-extrabold uppercase px-1.5 py-0.2 bg-[#c2410c] text-white rounded-full">
                                  Current
                                </span>
                              )}
                            </div>
                            <p className="text-[10px] text-stone-400 font-normal mt-0.5">
                              {report.label.split('(')[0].trim()}
                            </p>
                          </div>
                          <span
                            className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded ${
                              isSelected ? 'bg-[#c2410c] text-white' : 'bg-stone-100 text-stone-600'
                            }`}
                          >
                            {report.resolutionRate}%
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>

            {/* Scope Toggle: State-wide / City View */}
            <div className="inline-flex p-0.5 bg-stone-100 rounded-xl border border-stone-200 text-xs font-semibold">
              <button
                type="button"
                onClick={() => setViewScope('state')}
                className={`px-3 py-1 rounded-lg transition-all ${
                  viewScope === 'state'
                    ? 'bg-white text-stone-900 shadow-2xs font-bold'
                    : 'text-stone-500 hover:text-stone-900'
                }`}
              >
                State-wide
              </button>
              <button
                type="button"
                onClick={() => setViewScope('city')}
                className={`px-3 py-1 rounded-lg transition-all ${
                  viewScope === 'city'
                    ? 'bg-white text-stone-900 shadow-2xs font-bold'
                    : 'text-stone-500 hover:text-stone-900'
                }`}
              >
                City View
              </button>
            </div>

            {/* City Selector Dropdown (Active when City View is chosen) */}
            {isCityMode && (
              <div className="relative animate-in fade-in zoom-in-95" ref={cityDropdownRef}>
                <button
                  type="button"
                  onClick={() => setIsCityDropdownOpen((prev) => !prev)}
                  className={`inline-flex items-center gap-2 px-3 py-1.5 border rounded-xl text-xs font-bold transition-all shadow-2xs ${
                    isCityDropdownOpen
                      ? 'bg-[#fff5ee] border-[#c2410c] text-[#c2410c] ring-2 ring-[#c2410c]/20'
                      : 'bg-white hover:bg-stone-50 border-amber-300 text-stone-900'
                  }`}
                  title="Select city to filter municipal report"
                >
                  <MapPin className="w-3.5 h-3.5 text-[#c2410c]" />
                  <span>City: {selectedCity}</span>
                  <ChevronDown
                    className={`w-3.5 h-3.5 text-stone-400 transition-transform duration-200 ${
                      isCityDropdownOpen ? 'rotate-180 text-[#c2410c]' : ''
                    }`}
                  />
                </button>

                {/* City Dropdown Menu */}
                {isCityDropdownOpen && (
                  <div className="absolute right-0 top-full mt-2 w-64 bg-white rounded-2xl border border-stone-200 shadow-2xl z-50 p-2 animate-in fade-in zoom-in-95 origin-top-right">
                    <div className="px-3 py-2 border-b border-stone-100 mb-1 flex items-center justify-between">
                      <div className="flex items-center gap-1.5">
                        <Building2 className="w-3.5 h-3.5 text-[#c2410c]" />
                        <h4 className="text-xs font-bold text-stone-900">Select Municipal Zone</h4>
                      </div>
                      <span className="text-[10px] font-bold text-stone-500">
                        {AVAILABLE_CITIES.length} Cities
                      </span>
                    </div>

                    <div className="max-h-56 overflow-y-auto space-y-1 py-1 pr-1 custom-scrollbar">
                      {AVAILABLE_CITIES.map((city) => {
                        const cityData = CITY_METRICS_DATABASE[city];
                        const isSelected = city === selectedCity;
                        return (
                          <button
                            key={city}
                            type="button"
                            onClick={() => {
                              setSelectedCity(city);
                              setIsCityDropdownOpen(false);
                            }}
                            className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-left transition-all ${
                              isSelected
                                ? 'bg-[#fff5ee] text-[#c2410c] font-bold border border-[#fed7aa]'
                                : 'hover:bg-stone-50 text-stone-700 font-medium border border-transparent'
                            }`}
                          >
                            <div>
                              <span className="text-xs font-semibold">{city}</span>
                              <p className="text-[10px] text-stone-400 font-normal truncate max-w-[140px]">
                                {cityData.corporationName}
                              </p>
                            </div>
                            <span
                              className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded ${
                                isSelected ? 'bg-[#c2410c] text-white' : 'bg-stone-100 text-stone-600'
                              }`}
                            >
                              {cityData.resolutionRate}%
                            </span>
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Export PDF Button */}
            <button
              onClick={onExportPdf}
              className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-white hover:bg-stone-50 text-stone-800 border border-stone-300 rounded-xl text-xs font-bold shadow-2xs transition-colors"
            >
              <Download className="w-3.5 h-3.5 text-stone-500" />
              <span>Export PDF</span>
            </button>
          </div>
        </div>

        {/* City Filter Notification Banner when in City Mode */}
        {isCityMode && (
          <div className="p-3.5 rounded-xl bg-[#fffbf6] border border-[#fed7aa] flex items-center justify-between text-xs text-amber-950 animate-in fade-in">
            <div className="flex items-center gap-2.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[#ea580c] animate-pulse" />
              <span>
                Filtering weekly analytics exclusively for <strong>{activeCityData.corporationName}</strong>. Showing ward-level metrics and local SLA turnaround.
              </span>
            </div>
            <button
              onClick={() => setViewScope('state')}
              className="font-bold underline text-[#c2410c] hover:text-[#9a3412] ml-4 shrink-0"
            >
              Switch to State-wide View
            </button>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 1: OVERVIEW */}
        {/* ===================================================================== */}
        {sidebarTab === 'overview' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            {/* Charts Grid: 2x2 Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* 1. Comparative Bar Chart (District-wise in State View VS Ward-wise in City View) */}
              <div className="glass-panel rounded-3xl p-5 flex flex-col justify-between bg-white border border-stone-200 shadow-xs">
                <div>
                  <div className="flex items-center justify-between pb-3 flex-wrap gap-2">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <h3 className="text-sm font-bold text-stone-900">
                          {isCityMode
                            ? `Ward-wise Breakdown: ${selectedCity}`
                            : 'Reported vs Resolved Civic Issues'}
                        </h3>
                        <Info className="w-3.5 h-3.5 text-stone-400" />
                      </div>
                      <p className="text-xs text-stone-500 font-medium mt-0.5">
                        {isCityMode
                          ? `Issue resolution performance across municipal wards in ${selectedCity}`
                          : `Comparison across ${selectedTopDistrictsCount.toLowerCase()}`}
                      </p>
                    </div>

                    {!isCityMode ? (
                      <div className="relative">
                        <select
                          value={selectedTopDistrictsCount}
                          onChange={(e) => setSelectedTopDistrictsCount(e.target.value)}
                          className="appearance-none bg-stone-50 hover:bg-stone-100 border border-stone-200 rounded-xl px-3 py-1.5 text-xs font-bold text-stone-800 pr-7 focus:outline-none focus:ring-2 focus:ring-[#c2410c]/20 cursor-pointer shadow-2xs transition-all"
                        >
                          <option value="Top 5 Districts">Top 5 Districts</option>
                          <option value="Top 10 Districts">Top 10 Districts</option>
                          <option value="All 24 Districts">All 24 Districts</option>
                        </select>
                        <ChevronDown className="w-3.5 h-3.5 text-stone-500 absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none" />
                      </div>
                    ) : (
                      <div className="flex items-center gap-1 text-[11px] font-semibold text-[#c2410c] bg-[#fff5ee] border border-[#fed7aa] px-2.5 py-1 rounded-lg">
                        <Layers className="w-3 h-3" />
                        <span>{barChartItems.length} Municipal Wards</span>
                      </div>
                    )}
                  </div>

                  {/* Chart Legend */}
                  <div className="flex items-center justify-between text-xs font-medium text-stone-600 mt-2 mb-4">
                    <div className="flex items-center gap-4">
                      <div className="flex items-center gap-1.5">
                        <span className="w-3 h-3 rounded-full bg-[#c2410c]" />
                        <span className="font-semibold text-stone-800">Reported Issues</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <span className="w-3 h-3 rounded-full bg-[#78716c]" />
                        <span className="font-semibold text-stone-800">Resolved Issues</span>
                      </div>
                    </div>

                    {!isCityMode && totalBarsCount > 5 && (
                      <span className="text-[11px] text-stone-400 font-normal flex items-center gap-1">
                        <ArrowRightLeft className="w-3 h-3" />
                        <span>Scroll horizontally to view all ({totalBarsCount})</span>
                      </span>
                    )}
                  </div>

                  {/* Interactive SVG Bar Chart with Horizontal Scroll Container */}
                  <div className="h-56 w-full relative overflow-x-auto custom-scrollbar pb-2">
                    <svg
                      viewBox={`0 0 ${svgCanvasWidth} 220`}
                      style={{ width: `${svgCanvasWidth}px`, minWidth: `${svgCanvasWidth}px`, height: '220px' }}
                      className="block"
                    >
                      {/* Horizontal Grid lines */}
                      <line x1="35" y1="20" x2={svgCanvasWidth - 20} y2="20" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="5" y="24" fill="#94a3b8" fontSize="9">
                        {isCityMode ? Math.round(barScaleCeiling) : '2.5K'}
                      </text>

                      <line x1="35" y1="65" x2={svgCanvasWidth - 20} y2="65" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="5" y="69" fill="#94a3b8" fontSize="9">
                        {isCityMode ? Math.round(barScaleCeiling * 0.75) : '2.0K'}
                      </text>

                      <line x1="35" y1="110" x2={svgCanvasWidth - 20} y2="110" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="5" y="114" fill="#94a3b8" fontSize="9">
                        {isCityMode ? Math.round(barScaleCeiling * 0.5) : '1.5K'}
                      </text>

                      <line x1="35" y1="155" x2={svgCanvasWidth - 20} y2="155" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="10" y="159" fill="#94a3b8" fontSize="9">
                        {isCityMode ? Math.round(barScaleCeiling * 0.25) : '500'}
                      </text>

                      <line x1="35" y1="190" x2={svgCanvasWidth - 20} y2="190" stroke="#cbd5e1" strokeWidth="1" />
                      <text x="25" y="193" fill="#94a3b8" fontSize="9">0</text>

                      {/* Dynamic Items (5, 10, or All 24 Districts / City Wards) */}
                      {barChartItems.map((item, idx) => {
                        const startX = 40 + idx * barSpacing;
                        const reportedHeight = Math.min((item.reported / barScaleCeiling) * 165, 160);
                        const resolvedHeight = Math.min((item.resolved / barScaleCeiling) * 165, 160);
                        const isHovered = hoveredBarIndex === idx;

                        return (
                          <g
                            key={item.label}
                            onMouseEnter={() => setHoveredBarIndex(idx)}
                            onMouseLeave={() => setHoveredBarIndex(null)}
                            className="cursor-pointer group"
                          >
                            {/* Hover Highlight Column Background */}
                            {isHovered && (
                              <rect
                                x={startX - 6}
                                y="15"
                                width={gapBetweenBars + barWidth + 12}
                                height="180"
                                fill="#f8fafc"
                                rx="6"
                                opacity="0.9"
                              />
                            )}

                            {/* Reported Bar */}
                            <rect
                              x={startX}
                              y={190 - reportedHeight}
                              width={barWidth}
                              height={reportedHeight}
                              rx="3"
                              fill={isHovered ? '#ea580c' : '#c2410c'}
                              className="transition-all duration-150"
                            />

                            {/* Resolved Bar */}
                            <rect
                              x={startX + gapBetweenBars}
                              y={190 - resolvedHeight}
                              width={barWidth}
                              height={resolvedHeight}
                              rx="3"
                              fill={isHovered ? '#57534e' : '#78716c'}
                              className="transition-all duration-150"
                            />

                            {/* Hover Rate Pill Tooltip */}
                            {isHovered && (
                              <g>
                                <rect
                                  x={startX - 10}
                                  y={Math.max(10, 190 - Math.max(reportedHeight, resolvedHeight) - 22)}
                                  width={gapBetweenBars + barWidth + 20}
                                  height="18"
                                  rx="4"
                                  fill="#0f172a"
                                />
                                <text
                                  x={startX + (gapBetweenBars + barWidth) / 2}
                                  y={Math.max(22, 190 - Math.max(reportedHeight, resolvedHeight) - 10)}
                                  textAnchor="middle"
                                  fill="#ffffff"
                                  fontSize="9"
                                  fontWeight="bold"
                                >
                                  {item.rate}
                                </text>
                              </g>
                            )}

                            {/* Label (District or Ward name) */}
                            <text
                              x={startX + (gapBetweenBars + barWidth) / 2}
                              y="208"
                              textAnchor="middle"
                              fill={isHovered ? '#0f172a' : '#475569'}
                              fontSize={totalBarsCount > 10 ? '9' : '10'}
                              fontWeight={isHovered ? '700' : '500'}
                            >
                              {item.label.length > 11 ? `${item.label.slice(0, 10)}…` : item.label}
                            </text>
                          </g>
                        );
                      })}
                    </svg>
                  </div>
                </div>

                {/* Bottom 3 Metric summary cards */}
                <div className="grid grid-cols-3 gap-2 mt-4 pt-3 border-t border-stone-100">
                  <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                    <p className="text-[10px] font-semibold text-stone-500">
                      {isCityMode ? `${selectedCity} Reported` : 'Total Reported'}
                    </p>
                    <div className="flex items-baseline gap-1 mt-0.5">
                      <span className="text-sm font-bold text-stone-900 font-mono">
                        {displayTotalReported.toLocaleString()}
                      </span>
                      <span
                        className={`text-[10px] font-semibold ${
                          displayReportedIsDown ? 'text-red-500' : 'text-emerald-600'
                        }`}
                      >
                        {displayReportedChange}
                      </span>
                    </div>
                  </div>

                  <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                    <p className="text-[10px] font-semibold text-stone-500">
                      {isCityMode ? `${selectedCity} Resolved` : 'Total Resolved'}
                    </p>
                    <div className="flex items-baseline gap-1 mt-0.5">
                      <span className="text-sm font-bold text-stone-900 font-mono">
                        {displayTotalResolved.toLocaleString()}
                      </span>
                      <span
                        className={`text-[10px] font-semibold ${
                          displayResolvedIsUp ? 'text-emerald-600' : 'text-red-500'
                        }`}
                      >
                        {displayResolvedChange}
                      </span>
                    </div>
                  </div>

                  <div className="bg-stone-50/80 p-2.5 rounded-xl border border-stone-200/60">
                    <p className="text-[10px] font-semibold text-stone-500">Resolution Rate</p>
                    <div className="flex items-baseline gap-1 mt-0.5">
                      <span className="text-sm font-bold text-stone-900 font-mono">
                        {displayResolutionRate}%
                      </span>
                      <span
                        className={`text-[10px] font-semibold ${
                          displayRateIsUp ? 'text-emerald-600' : 'text-red-500'
                        }`}
                      >
                        {displayRateChange}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* 2. Department-wise Distribution (Donut Chart) */}
              <div className="glass-panel rounded-3xl p-5 flex flex-col justify-between bg-white border border-stone-200 shadow-xs">
                <div>
                  <div className="flex items-center justify-between pb-2">
                    <div className="flex items-center gap-1.5">
                      <h3 className="text-sm font-bold text-stone-900">
                        {isCityMode ? `${selectedCity} Category Breakdown` : 'Department-wise Distribution'}
                      </h3>
                      <Info className="w-3.5 h-3.5 text-stone-400" />
                    </div>
                  </div>
                  <p className="text-xs text-stone-500 font-medium">
                    {isCityMode
                      ? `Share of issues reported across ${selectedCity} municipal departments`
                      : `Share of issues reported statewide in ${currentReport.weekNumber}`}
                  </p>

                  {/* Donut & Legend side-by-side */}
                  <div className="flex flex-col sm:flex-row items-center justify-around gap-4 mt-4">
                    {/* Donut Chart SVG */}
                    <div className="relative w-44 h-44 shrink-0 flex items-center justify-center">
                      <svg viewBox="0 0 160 160" className="w-full h-full -rotate-90">
                        {(() => {
                          const totalCircumference = 2 * Math.PI * 60; // ~376.99
                          let accumulatedOffset = 0;
                          return displayDeptStats.map((dept) => {
                            const dashLength = (dept.percentage / 100) * totalCircumference;
                            const dashArray = `${dashLength} ${totalCircumference}`;
                            const offset = -accumulatedOffset;
                            accumulatedOffset += dashLength;
                            return (
                              <circle
                                key={dept.name}
                                cx="80"
                                cy="80"
                                r="60"
                                fill="transparent"
                                stroke={dept.color}
                                strokeWidth="22"
                                strokeDasharray={dashArray}
                                strokeDashoffset={offset}
                                className="transition-all duration-300 hover:opacity-85 cursor-pointer"
                              />
                            );
                          });
                        })()}
                      </svg>

                      {/* Center Total Count Text */}
                      <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                        <span className="text-xl font-bold text-stone-900 font-mono leading-none">
                          {displayTotalReported.toLocaleString()}
                        </span>
                        <span className="text-[10px] font-semibold text-stone-500 mt-1">
                          {isCityMode ? `${selectedCity} Total` : 'Total Issues'}
                        </span>
                      </div>
                    </div>

                    {/* Right Legend List */}
                    <div className="space-y-3 w-full sm:w-auto">
                      {displayDeptStats.map((dept) => (
                        <div key={dept.name} className="flex items-center justify-between gap-6 text-xs">
                          <div className="flex items-center gap-2">
                            <span
                              className="w-2.5 h-2.5 rounded-full shrink-0"
                              style={{ backgroundColor: dept.color }}
                            />
                            <span className="text-stone-700 font-medium">{dept.name}</span>
                          </div>
                          <div className="flex items-center gap-1.5 font-mono">
                            <span className="font-bold text-stone-900">{dept.count.toLocaleString()}</span>
                            <span className="text-stone-400 text-[11px]">({dept.percentage}%)</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Bottom Insight notice banner */}
                <div className="mt-4 p-3 rounded-xl bg-[#fffbf6] border border-[#fed7aa]/50 flex items-start gap-2.5">
                  <Info className="w-4 h-4 text-[#ea580c] shrink-0 mt-0.5" />
                  <p className="text-xs text-stone-600 leading-relaxed font-medium">
                    {(() => {
                      const primaryDept = [...displayDeptStats].sort((a, b) => b.count - a.count)[0];
                      return (
                        <>
                          <strong className="text-stone-900">{primaryDept?.name || 'Waste Management'}</strong> is the primary grievance driver in {isCityMode ? selectedCity : currentReport.weekNumber} ({primaryDept?.percentage || 38}% of local volume).
                        </>
                      );
                    })()}
                  </p>
                </div>
              </div>

              {/* 3. SLA Performance (Average Resolution Time) (Line Chart) */}
              <div className="glass-panel rounded-3xl p-5 flex flex-col justify-between bg-white border border-stone-200 shadow-xs">
                <div>
                  <div className="flex items-center justify-between pb-2">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <h3 className="text-sm font-bold text-stone-900">
                          {isCityMode ? `${selectedCity} SLA Turnaround Time` : 'SLA Performance (Average Resolution Time)'}
                        </h3>
                        <Info className="w-3.5 h-3.5 text-stone-400" />
                      </div>
                      <p className="text-xs text-stone-500 font-medium mt-0.5">
                        {isCityMode
                          ? `Weekly turnaround trend for ${activeCityData.corporationName}`
                          : 'Average time taken to resolve issues (in hours)'}
                      </p>
                    </div>
                  </div>

                  {/* Legend */}
                  <div className="flex items-center gap-4 text-xs font-medium text-stone-600 mt-2 mb-3">
                    <div className="flex items-center gap-1.5">
                      <span className="w-3.5 h-1 rounded-full bg-[#c2410c]" />
                      <span className="font-semibold text-stone-800">{isCityMode ? `${selectedCity} Avg (hrs)` : 'State Avg (hrs)'}</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <span className="w-3.5 h-0.5 bg-[#dc2626] border-b border-dashed border-red-500" />
                      <span className="text-red-700 font-semibold">SLA Limit (72 hrs)</span>
                    </div>
                  </div>

                  {/* Line Graph SVG */}
                  <div className="h-56 w-full relative">
                    <svg viewBox="0 0 500 200" className="w-full h-full">
                      {/* Grid Lines */}
                      <line x1="30" y1="20" x2="480" y2="20" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="5" y="24" fill="#94a3b8" fontSize="9">100</text>

                      <line x1="30" y1="50" x2="480" y2="50" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="10" y="54" fill="#94a3b8" fontSize="9">80</text>

                      {/* 72 hrs SLA Threshold line (dashed red) */}
                      <line x1="30" y1="62" x2="480" y2="62" stroke="#dc2626" strokeWidth="1.5" strokeDasharray="4,4" />
                      <text x="482" y="65" fill="#dc2626" fontSize="9" fontWeight="700">72</text>

                      <line x1="30" y1="80" x2="480" y2="80" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="10" y="84" fill="#94a3b8" fontSize="9">60</text>

                      <line x1="30" y1="120" x2="480" y2="120" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="10" y="124" fill="#94a3b8" fontSize="9">40</text>

                      <line x1="30" y1="160" x2="480" y2="160" stroke="#f1f5f9" strokeWidth="1" />
                      <text x="10" y="164" fill="#94a3b8" fontSize="9">20</text>

                      <line x1="30" y1="180" x2="480" y2="180" stroke="#cbd5e1" strokeWidth="1" />
                      <text x="15" y="184" fill="#94a3b8" fontSize="9">0</text>

                      {/* Polyline Path for SLA trends */}
                      <polyline
                        fill="none"
                        stroke="#c2410c"
                        strokeWidth="3"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        points={displaySlaPoints
                          .map((pt, i) => `${60 + i * 65},${180 - pt.avgHours * 1.6}`)
                          .join(' ')}
                      />

                      {/* Plot Dots and Day labels */}
                      {displaySlaPoints.map((pt, i) => {
                        const cx = 60 + i * 65;
                        const cy = 180 - pt.avgHours * 1.6;
                        return (
                          <g key={pt.day} className="cursor-pointer group">
                            <circle cx={cx} cy={cy} r="5" fill="#c2410c" stroke="#ffffff" strokeWidth="2" />
                            <text x={cx} y="196" textAnchor="middle" fill="#64748b" fontSize="10" fontWeight="600">
                              {pt.day}
                            </text>
                          </g>
                        );
                      })}
                    </svg>
                  </div>
                </div>
              </div>

              {/* 4. Citizen Satisfaction Score (Radial Gauge & Ratings) */}
              <div className="glass-panel rounded-3xl p-5 flex flex-col justify-between bg-white border border-stone-200 shadow-xs">
                <div>
                  <div className="flex items-center justify-between pb-2">
                    <div className="flex items-center gap-1.5">
                      <h3 className="text-sm font-bold text-stone-900">
                        {isCityMode ? `${selectedCity} Citizen Satisfaction` : 'Citizen Satisfaction Score'}
                      </h3>
                      <Info className="w-3.5 h-3.5 text-stone-400" />
                    </div>
                  </div>
                  <p className="text-xs text-stone-500 font-medium">
                    {isCityMode
                      ? `Citizen feedback on issues resolved by ${activeCityData.corporationName}`
                      : `Feedback from citizens on resolved issues in ${currentReport.weekNumber}`}
                  </p>

                  {/* Gauge and Breakdown Row */}
                  <div className="grid grid-cols-1 sm:grid-cols-12 gap-4 items-center mt-3">
                    {/* Left Semi-Circle Radial Gauge */}
                    <div className="sm:col-span-5 flex flex-col items-center justify-center relative">
                      <div className="relative w-36 h-24 overflow-hidden">
                        <svg viewBox="0 0 140 80" className="w-full h-full">
                          {/* Background Arch */}
                          <path
                            d="M 15 75 A 55 55 0 0 1 125 75"
                            fill="none"
                            stroke="#f1f5f9"
                            strokeWidth="16"
                            strokeLinecap="round"
                          />
                          {/* Filled Arch proportional to score */}
                          <path
                            d="M 15 75 A 55 55 0 0 1 125 75"
                            fill="none"
                            stroke="#c2410c"
                            strokeWidth="16"
                            strokeDasharray="172 172"
                            strokeDashoffset={`${172 - (displayCitizenScore / 5) * 172}`}
                            strokeLinecap="round"
                          />
                        </svg>
                      </div>

                      <div className="text-center -mt-8">
                        <span className="text-3xl font-extrabold text-stone-900 font-mono">
                          {displayCitizenScore}
                        </span>
                        <span className="text-xs font-semibold text-stone-400"> / 5</span>
                        <p className="text-[11px] font-semibold text-emerald-600 mt-0.5">
                          ★ {displayPositivePercent}% Positive Feedback
                        </p>
                      </div>
                    </div>

                    {/* Right Star Breakdown */}
                    <div className="sm:col-span-7 space-y-2">
                      <div className="text-[11px] font-semibold text-stone-400 uppercase tracking-wider mb-1">
                        Rating Distribution
                      </div>
                      {citizenRatings.breakdown.map((r) => (
                        <div key={r.stars} className="flex items-center gap-2 text-xs">
                          <span className="w-12 text-stone-600 font-medium shrink-0">{r.label}</span>
                          <div className="flex-1 h-2.5 bg-stone-100 rounded-full overflow-hidden">
                            <div
                              className="h-full rounded-full transition-all duration-500"
                              style={{
                                width: `${r.percentage}%`,
                                backgroundColor: r.color,
                              }}
                            />
                          </div>
                          <span className="w-8 text-right font-mono font-semibold text-stone-700 text-[11px]">
                            {r.percentage}%
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 2: ISSUES INFLOW */}
        {/* ===================================================================== */}
        {sidebarTab === 'inflow' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="flex items-center justify-between text-stone-500 mb-1">
                  <span className="text-xs font-semibold">Weekly Inflow</span>
                  <Smartphone className="w-4 h-4 text-[#c2410c]" />
                </div>
                <div className="text-xl font-bold font-mono text-stone-900">{displayTotalReported.toLocaleString()}</div>
                <span className="text-[10px] text-emerald-600 font-semibold">{displayReportedChange} vs last week</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="flex items-center justify-between text-stone-500 mb-1">
                  <span className="text-xs font-semibold">Peak Influx Day</span>
                  <Calendar className="w-4 h-4 text-blue-600" />
                </div>
                <div className="text-xl font-bold font-mono text-stone-900">Wednesday</div>
                <span className="text-[10px] text-stone-500 font-medium">{Math.round(displayTotalReported * 0.18)} tickets</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="flex items-center justify-between text-stone-500 mb-1">
                  <span className="text-xs font-semibold">Mobile App Share</span>
                  <Smartphone className="w-4 h-4 text-purple-600" />
                </div>
                <div className="text-xl font-bold font-mono text-stone-900">68.0%</div>
                <span className="text-[10px] text-stone-500 font-medium">{Math.round(displayTotalReported * 0.68).toLocaleString()} app reports</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="flex items-center justify-between text-stone-500 mb-1">
                  <span className="text-xs font-semibold">Critical Priority</span>
                  <AlertTriangle className="w-4 h-4 text-red-600" />
                </div>
                <div className="text-xl font-bold font-mono text-stone-900">8.4%</div>
                <span className="text-[10px] text-red-600 font-semibold">{Math.round(displayTotalReported * 0.084)} urgent triage</span>
              </div>
            </div>

            {/* Channels Breakdown */}
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Intake Channels Breakdown</h3>
                  <p className="text-xs text-stone-500">Citizen touchpoints across {isCityMode ? selectedCity : 'Jharkhand'}</p>
                </div>
                <div className="flex flex-wrap gap-1.5 text-xs font-semibold">
                  {[
                    { id: 'all', label: 'All Channels' },
                    { id: 'mobile', label: 'Mobile App' },
                    { id: 'web', label: 'Web Portal' },
                    { id: 'whatsapp', label: 'WhatsApp Bot' },
                    { id: 'helpline', label: 'Helpline 181' },
                  ].map((ch) => (
                    <button
                      key={ch.id}
                      onClick={() => setInflowChannelFilter(ch.id as any)}
                      className={`px-3 py-1 rounded-lg border transition-all ${
                        inflowChannelFilter === ch.id
                          ? 'bg-[#c2410c] text-white border-[#c2410c] shadow-2xs font-bold'
                          : 'bg-stone-50 text-stone-600 border-stone-200 hover:bg-stone-100'
                      }`}
                    >
                      {ch.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                  { name: 'Mobile App (Kartavya)', count: Math.round(displayTotalReported * 0.68), percent: '68.0%', icon: Smartphone, color: '#c2410c', trend: '↑ 14%' },
                  { name: 'Web Portal (Citizen Desk)', count: Math.round(displayTotalReported * 0.16), percent: '16.0%', icon: Globe, color: '#0284c7', trend: '↑ 3%' },
                  { name: 'WhatsApp Grievance Bot', count: Math.round(displayTotalReported * 0.10), percent: '10.0%', icon: MessageCircle, color: '#16a34a', trend: '↑ 24%' },
                  { name: 'Helpline 181 Call Center', count: Math.round(displayTotalReported * 0.06), percent: '6.0%', icon: PhoneCall, color: '#d97706', trend: '↓ 2%' },
                ]
                  .filter((item) => {
                    if (inflowChannelFilter === 'mobile') return item.name.includes('Mobile');
                    if (inflowChannelFilter === 'web') return item.name.includes('Web');
                    if (inflowChannelFilter === 'whatsapp') return item.name.includes('WhatsApp');
                    if (inflowChannelFilter === 'helpline') return item.name.includes('Helpline');
                    return true;
                  })
                  .map((item) => {
                    const Icon = item.icon;
                    return (
                      <div key={item.name} className="p-4 rounded-xl border border-stone-200/80 bg-stone-50/50 space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="p-2 rounded-lg bg-white border border-stone-200" style={{ color: item.color }}>
                            <Icon className="w-4 h-4" />
                          </span>
                          <span className="text-[11px] font-bold text-stone-500 font-mono">{item.trend}</span>
                        </div>
                        <div>
                          <div className="text-lg font-bold text-stone-900 font-mono">{item.count.toLocaleString()}</div>
                          <div className="text-xs font-semibold text-stone-600">{item.name}</div>
                        </div>
                        <div className="w-full bg-stone-200 h-1.5 rounded-full overflow-hidden">
                          <div className="h-full rounded-full" style={{ width: item.percent, backgroundColor: item.color }} />
                        </div>
                        <div className="text-[10px] text-stone-400 font-semibold text-right">{item.percent} share</div>
                      </div>
                    );
                  })}
              </div>
            </div>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 3: RESOLUTION */}
        {/* ===================================================================== */}
        {sidebarTab === 'resolution' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Total Resolved</div>
                <div className="text-xl font-bold font-mono text-stone-900">{displayTotalResolved.toLocaleString()}</div>
                <span className="text-[10px] text-emerald-600 font-semibold">{displayResolutionRate}% completion rate</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">First-Time Resolution</div>
                <div className="text-xl font-bold font-mono text-stone-900">78.2%</div>
                <span className="text-[10px] text-stone-500 font-medium">Without re-dispatch</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Reopened by Citizen</div>
                <div className="text-xl font-bold font-mono text-stone-900">4.1%</div>
                <span className="text-[10px] text-emerald-600 font-semibold">↓ 1.8% lowest in Q3</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Photo Proof Verified</div>
                <div className="text-xl font-bold font-mono text-stone-900">96.8%</div>
                <span className="text-[10px] text-stone-500 font-medium">GPS-watermarked closure</span>
              </div>
            </div>

            {/* Turnaround Speed Distribution */}
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Resolution Turnaround Speed</h3>
                  <p className="text-xs text-stone-500">Distribution of resolution completion time</p>
                </div>
                <span className="text-xs font-bold text-stone-600 bg-stone-100 px-2.5 py-1 rounded-lg">
                  Avg {displayAvgSlaHours}h
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                {[
                  { label: '< 12 Hours', count: Math.round(displayTotalResolved * 0.284), pct: '28.4%', sub: 'Instant & Emergency fixes', color: '#16a34a' },
                  { label: '12 - 24 Hours', count: Math.round(displayTotalResolved * 0.328), pct: '32.8%', sub: 'Standard daily workflow', color: '#0284c7' },
                  { label: '24 - 48 Hours', count: Math.round(displayTotalResolved * 0.252), pct: '25.2%', sub: 'Civil work & paving', color: '#d97706' },
                  { label: '48 - 72 Hours', count: Math.round(displayTotalResolved * 0.136), pct: '13.6%', sub: 'Complex line overhauls', color: '#c2410c' },
                ].map((tier) => (
                  <div key={tier.label} className="p-4 rounded-xl border border-stone-200 bg-stone-50/60 space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-bold text-stone-700">
                      <span>{tier.label}</span>
                      <span style={{ color: tier.color }}>{tier.pct}</span>
                    </div>
                    <div className="text-lg font-bold font-mono text-stone-900">{tier.count.toLocaleString()}</div>
                    <div className="w-full bg-stone-200 h-1.5 rounded-full overflow-hidden">
                      <div className="h-full rounded-full" style={{ width: tier.pct, backgroundColor: tier.color }} />
                    </div>
                    <p className="text-[10px] text-stone-400 font-medium">{tier.sub}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Resolution Leaderboard */}
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">
                    {isCityMode ? `${selectedCity} Ward Resolution Table` : 'District Resolution Leaderboard'}
                  </h3>
                  <p className="text-xs text-stone-500">
                    {isCityMode ? `Ward performance across ${selectedCity}` : 'Rankings across all 24 Jharkhand municipal districts'}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-stone-500">Sort by:</span>
                  <select
                    value={resolutionSortKey}
                    onChange={(e) => setResolutionSortKey(e.target.value as any)}
                    className="bg-stone-50 border border-stone-200 rounded-lg px-2.5 py-1 text-xs font-semibold text-stone-700 focus:outline-none cursor-pointer"
                  >
                    <option value="rate">Resolution Rate (%)</option>
                    <option value="resolved">Total Resolved</option>
                    <option value="total">Total Inflow</option>
                  </select>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead>
                    <tr className="border-b border-stone-200 text-stone-400 uppercase text-[10px] font-bold">
                      <th className="py-2.5 px-3">Rank &amp; Zone</th>
                      <th className="py-2.5 px-3">Officer in Charge</th>
                      <th className="py-2.5 px-3 text-right">Inflow</th>
                      <th className="py-2.5 px-3 text-right">Resolved</th>
                      <th className="py-2.5 px-3 text-right">Rate</th>
                      <th className="py-2.5 px-3 text-right">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-stone-100 font-medium">
                    {(isCityMode ? activeCityData.wards.map((w, i) => ({
                      id: `ward-${i}`,
                      name: w.name,
                      officer: `Zonal Supervisor, ${selectedCity}`,
                      inflow: w.reported,
                      resolved: w.resolved,
                      rate: parseFloat(w.rate.replace('%', '')),
                    })) : [...districtList].map((d) => {
                      const rawReported = d.reportedThisWeek || Math.round(d.totalIssues * 0.6);
                      const rawResolved =
                        d.resolvedThisWeek ||
                        (d.resolutionRate ? Math.round(rawReported * (d.resolutionRate / 100)) : Math.round(rawReported * 0.85));
                      const boundedResolved = Math.min(rawResolved, Math.max(1, rawReported - 1));
                      const adjustedReported = Math.max(20, Math.round(rawReported * weekRatio));
                      const adjustedResolved = Math.min(
                        Math.max(1, adjustedReported - 1),
                        Math.max(15, Math.round(boundedResolved * weekRatio))
                      );
                      const rate = Number(((adjustedResolved / adjustedReported) * 100).toFixed(1));
                      return {
                        id: d.id,
                        name: d.name,
                        officer: d.nodalOfficer.name,
                        inflow: adjustedReported,
                        resolved: adjustedResolved,
                        rate,
                      };
                    }))
                      .sort((a, b) => {
                        if (resolutionSortKey === 'rate') return b.rate - a.rate;
                        if (resolutionSortKey === 'resolved') return b.resolved - a.resolved;
                        return b.inflow - a.inflow;
                      })
                      .slice(0, 8)
                      .map((item, index) => (
                        <tr key={item.id} className="hover:bg-stone-50 transition-colors">
                          <td className="py-2.5 px-3">
                            <div className="flex items-center gap-2">
                              <span className="w-5 h-5 rounded-full bg-stone-100 text-stone-700 text-[10px] font-bold flex items-center justify-center font-mono">
                                {index + 1}
                              </span>
                              <span className="font-bold text-stone-900">{item.name}</span>
                            </div>
                          </td>
                          <td className="py-2.5 px-3 text-stone-600 truncate max-w-[150px]">
                            {item.officer}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono text-stone-700">{item.inflow}</td>
                          <td className="py-2.5 px-3 text-right font-mono font-bold text-stone-900">{item.resolved}</td>
                          <td className="py-2.5 px-3 text-right font-mono font-bold text-emerald-600">{item.rate}%</td>
                          <td className="py-2.5 px-3 text-right">
                            <span
                              className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                item.rate >= 90
                                    ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                    : item.rate >= 85
                                    ? 'bg-blue-50 text-blue-700 border border-blue-200'
                                    : 'bg-amber-50 text-amber-700 border border-amber-200'
                              }`}
                            >
                              {item.rate >= 90 ? 'Top Performer' : item.rate >= 85 ? 'On Target' : 'Needs Focus'}
                            </span>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 4: SLA PERFORMANCE */}
        {/* ===================================================================== */}
        {sidebarTab === 'sla' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">SLA Adherence Rate</div>
                <div className="text-xl font-bold font-mono text-emerald-600">89.2%</div>
                <span className="text-[10px] text-stone-500 font-medium">Inside 72h window</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Breached Escalations</div>
                <div className="text-xl font-bold font-mono text-red-600">{Math.round(displayTotalReported * 0.108)}</div>
                <span className="text-[10px] text-red-600 font-semibold">10.8% overdue tickets</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Show Cause Notices</div>
                <div className="text-xl font-bold font-mono text-stone-900">42 Issued</div>
                <span className="text-[10px] text-amber-600 font-semibold">To defaulting contractors</span>
              </div>
              <div className="bg-white p-4 rounded-2xl border border-stone-200 shadow-xs">
                <div className="text-xs font-semibold text-stone-500 mb-1">Penalties Levied</div>
                <div className="text-xl font-bold font-mono text-[#c2410c]">₹2,85,000</div>
                <span className="text-[10px] text-stone-500 font-medium">Under Municipal Penalty Rules</span>
              </div>
            </div>

            {/* Department SLA Matrix */}
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Department-wise SLA Compliance Matrix</h3>
                  <p className="text-xs text-stone-500">Benchmark comparison vs statutory 72-hour ceiling</p>
                </div>
                <span className="text-xs font-bold text-stone-600">Statutory SLA: 72 hrs</span>
              </div>

              <div className="space-y-3.5">
                {[
                  { dept: 'Waste Management (Sanitation)', compliance: 92.4, avgHrs: 18.6, target: '24h SLA', color: '#16a34a' },
                  { dept: 'Road Works & Potholes (PWD)', compliance: 86.8, avgHrs: 38.2, target: '72h SLA', color: '#0284c7' },
                  { dept: 'Sewage & Drainage Systems', compliance: 81.5, avgHrs: 46.5, target: '72h SLA', color: '#d97706' },
                  { dept: 'Electricity & Public Lighting', compliance: 89.0, avgHrs: 24.1, target: '48h SLA', color: '#0284c7' },
                  { dept: 'Drinking Water Supply (Jal Shakti)', compliance: 78.6, avgHrs: 52.0, target: '48h SLA', color: '#dc2626' },
                ].map((item) => {
                  const compScale = displayResolutionRate / 82.6;
                  const compliance = Math.min(99, Math.max(60, Number((item.compliance * compScale).toFixed(1))));
                  const avgScale = displayAvgSlaHours / 28.4;
                  const avgHrs = Number((item.avgHrs * avgScale).toFixed(1));
                  return (
                    <div key={item.dept} className="p-3.5 rounded-xl border border-stone-200/80 bg-stone-50/50 space-y-2">
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-1 text-xs">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-stone-900">{item.dept}</span>
                          <span className="text-[10px] bg-stone-200 text-stone-700 px-2 py-0.5 rounded font-semibold">{item.target}</span>
                        </div>
                        <div className="flex items-center gap-3 font-mono">
                          <span className="text-stone-500">Avg: <strong className="text-stone-900">{avgHrs}h</strong></span>
                          <span className="font-bold" style={{ color: item.color }}>{compliance}% on-time</span>
                        </div>
                      </div>
                      <div className="w-full bg-stone-200 h-2 rounded-full overflow-hidden">
                        <div className="h-full rounded-full" style={{ width: `${compliance}%`, backgroundColor: item.color }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 5: DEPARTMENTS */}
        {/* ===================================================================== */}
        {sidebarTab === 'departments' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            {(() => {
              const getDeptConfig = (name: string) => {
                const n = name.toLowerCase();
                if (n.includes('waste')) {
                  return {
                    id: 'waste',
                    shortName: 'Waste Management',
                    icon: Trash2,
                    fullName: 'Urban Waste Management & Sanitation Department',
                    head: 'Dr. Neha Verma, IAS (Mission Director, Swachh Bharat)',
                    teams: '68 Compactor Units, 142 Sweeper Squads',
                    baseRating: 4.4,
                    baseSlaHours: 18.6,
                    rateFactor: 1.087,
                    subCategories: [
                      { issue: 'Overflowing Secondary Garbage Dumps', ratio: 0.416 },
                      { issue: 'Uncollected Door-to-Door Household Waste', ratio: 0.298 },
                      { issue: 'Commercial Market Debris Dumping', ratio: 0.174 },
                      { issue: 'Animal Carcass & Bio-waste Disposal', ratio: 0.112 },
                    ],
                  };
                }
                if (n.includes('road') || n.includes('pothole')) {
                  return {
                    id: 'roads',
                    shortName: 'Roads & Potholes (PWD)',
                    icon: Wrench,
                    fullName: 'Road Construction Department (PWD Roads)',
                    head: 'Er. Sunil Kumar Singh (Engineer-in-Chief, PWD)',
                    teams: '32 Cold-Mix Bitumen Vans, 14 Roller Squads',
                    baseRating: 4.1,
                    baseSlaHours: 38.2,
                    rateFactor: 0.994,
                    subCategories: [
                      { issue: 'Arterial Road Potholes > 1 foot depth', ratio: 0.462 },
                      { issue: 'Trench Digging without Barricades', ratio: 0.255 },
                      { issue: 'Broken Culvert & Bridge Joint Cracks', ratio: 0.184 },
                      { issue: 'Paver Block Sinking on Footpaths', ratio: 0.099 },
                    ],
                  };
                }
                if (n.includes('sewage') || n.includes('drain')) {
                  return {
                    id: 'sewage',
                    shortName: 'Sewage & Drainage',
                    icon: Droplets,
                    fullName: 'Drinking Water & Sanitation Department (Drainage Wing)',
                    head: 'Shri Manoj Kumar, IAS (Secretary, DW&S)',
                    teams: '24 Super-Sucker Jetting Tankers, 8 Desilting Cranes',
                    baseRating: 3.9,
                    baseSlaHours: 46.5,
                    rateFactor: 0.987,
                    subCategories: [
                      { issue: 'Underground Sewer Line Choke & Backflow', ratio: 0.459 },
                      { issue: 'Open Storm Drain Overflowing during Rain', ratio: 0.306 },
                      { issue: 'Broken Concrete Manhole Slabs', ratio: 0.153 },
                      { issue: 'Stagnant Water Vector Breeding Sites', ratio: 0.082 },
                    ],
                  };
                }
                if (n.includes('electric') || n.includes('light')) {
                  return {
                    id: 'electricity',
                    shortName: 'Electricity & Lighting (JSEB)',
                    icon: Zap,
                    fullName: 'Jharkhand State Electricity Board (JSEB Distribution)',
                    head: 'Shri Rahul Purwar, IAS (Managing Director, JBVNL)',
                    teams: '48 Lineman Mobile Emergency Units',
                    baseRating: 4.3,
                    baseSlaHours: 24.1,
                    rateFactor: 1.077,
                    subCategories: [
                      { issue: 'Street Light Inoperative > 48 Hours', ratio: 0.493 },
                      { issue: 'Sagging 11kV Overhead Power Cables', ratio: 0.272 },
                      { issue: 'Distribution Transformer Oil Leakage', ratio: 0.153 },
                      { issue: 'Damaged Electric Feeder Poles', ratio: 0.082 },
                    ],
                  };
                }
                return {
                  id: 'water',
                  shortName: 'Drinking Water Supply',
                  icon: Droplets,
                  fullName: 'Drinking Water & Public Health Engineering Department',
                  head: 'Shri Rajesh Sharma, IAS (Director, DW&S)',
                  teams: '18 Tanker Fleets, 26 Valve Repair Squads',
                  baseRating: 4.2,
                  baseSlaHours: 32.4,
                  rateFactor: 1.02,
                  subCategories: [
                    { issue: 'Main Pipeline Leakage & Water Wastage', ratio: 0.440 },
                    { issue: 'Low Pressure in Municipal Supply', ratio: 0.280 },
                    { issue: 'Contaminated or Turbid Tap Water', ratio: 0.180 },
                    { issue: 'Defective Public Standpost Tap', ratio: 0.100 },
                  ],
                };
              };

              // Fixed stable order so department tabs and sections never jump around when changing weeks
              const FIXED_DEPT_ORDER = ['waste', 'roads', 'sewage', 'electricity', 'water'];

              // Map active departments in displayDeptStats to dynamic department data
              const activeDepts = displayDeptStats
                .map((stat) => {
                  const config = getDeptConfig(stat.name);
                  const inflow = stat.count;

                  // Department resolution rate tied to the active week/city resolution rate
                  const calculatedRate = Math.min(
                    96.0,
                    Math.max(55.0, Number((displayResolutionRate * config.rateFactor).toFixed(1)))
                  );
                  // Resolved count is strictly less than inflow (rate < 100%)
                  const resolved = Math.min(Math.max(1, inflow - 1), Math.round(inflow * (calculatedRate / 100)));
                  const rate = `${((resolved / inflow) * 100).toFixed(1)}%`;

                  const slaTime = (config.baseSlaHours * (displayAvgSlaHours / 28.4)).toFixed(1);
                  const rating = Math.min(4.9, Math.max(3.5, Number((config.baseRating * (displayCitizenScore / 4.2)).toFixed(1)))).toFixed(1);

                  // Subcategory breakdown scaled dynamically to the current inflow
                  let remainingCount = inflow;
                  const topIssues = config.subCategories.map((sub, idx) => {
                    const isLast = idx === config.subCategories.length - 1;
                    const count = isLast ? Math.max(0, remainingCount) : Math.round(inflow * sub.ratio);
                    remainingCount -= count;
                    const pct = `${((count / inflow) * 100).toFixed(1)}%`;
                    return { issue: sub.issue, count, pct };
                  });

                  return {
                    id: config.id,
                    name: config.shortName,
                    fullName: config.fullName,
                    icon: config.icon,
                    head: config.head,
                    teams: config.teams,
                    inflow,
                    resolved,
                    rate,
                    slaAvg: `${slaTime} hrs`,
                    rating: `${rating} / 5`,
                    topIssues,
                  };
                })
                .sort((a, b) => {
                  const idxA = FIXED_DEPT_ORDER.indexOf(a.id);
                  const idxB = FIXED_DEPT_ORDER.indexOf(b.id);
                  return (idxA === -1 ? 99 : idxA) - (idxB === -1 ? 99 : idxB);
                });

              // Active selected department or default to first department
              const selectedDept = activeDepts.find((d) => d.id === selectedDeptId) || activeDepts[0];

              return (
                <div className="space-y-6">
                  {/* Department Switcher Pills */}
                  <div className="flex flex-wrap gap-2">
                    {activeDepts.map((dept) => {
                      const Icon = dept.icon;
                      const isSelected = selectedDept.id === dept.id;
                      return (
                        <button
                          key={dept.id}
                          onClick={() => setSelectedDeptId(dept.id)}
                          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                            isSelected
                              ? 'bg-[#c2410c] text-white shadow-xs'
                              : 'bg-white border border-stone-200 text-stone-700 hover:bg-stone-50'
                          }`}
                        >
                          <Icon className="w-3.5 h-3.5" />
                          <span>{dept.name}</span>
                          <span
                            className={`text-[10px] px-1.5 py-0.2 rounded-full font-mono ${
                              isSelected ? 'bg-white/20 text-white' : 'bg-stone-100 text-stone-600'
                            }`}
                          >
                            {dept.inflow.toLocaleString()}
                          </span>
                        </button>
                      );
                    })}
                  </div>

                  {/* Department Profile */}
                  <div className="bg-white rounded-2xl border border-stone-200 p-6 shadow-xs space-y-6">
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-stone-100">
                      <div>
                        <div className="flex items-center gap-2">
                          <Building2 className="w-5 h-5 text-[#c2410c]" />
                          <h3 className="text-base font-bold text-stone-900">{selectedDept.fullName}</h3>
                        </div>
                        <p className="text-xs text-stone-500 font-medium mt-0.5">Nodal Head: {selectedDept.head}</p>
                      </div>
                      <div className="bg-stone-50 px-3 py-1.5 rounded-xl border border-stone-200 text-center">
                        <span className="text-[10px] text-stone-500 block font-semibold">Active Teams</span>
                        <span className="text-xs font-bold text-stone-800">{selectedDept.teams}</span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                      <div className="p-3.5 rounded-xl bg-stone-50 border border-stone-200/80">
                        <span className="text-[11px] text-stone-500 font-semibold block">Weekly Inflow</span>
                        <span className="text-lg font-bold font-mono text-stone-900">{selectedDept.inflow.toLocaleString()}</span>
                      </div>
                      <div className="p-3.5 rounded-xl bg-stone-50 border border-stone-200/80">
                        <span className="text-[11px] text-stone-500 font-semibold block">Resolved</span>
                        <span className="text-lg font-bold font-mono text-emerald-600">
                          {selectedDept.resolved.toLocaleString()} ({selectedDept.rate})
                        </span>
                      </div>
                      <div className="p-3.5 rounded-xl bg-stone-50 border border-stone-200/80">
                        <span className="text-[11px] text-stone-500 font-semibold block">Average SLA Time</span>
                        <span className="text-lg font-bold font-mono text-[#c2410c]">{selectedDept.slaAvg}</span>
                      </div>
                      <div className="p-3.5 rounded-xl bg-stone-50 border border-stone-200/80">
                        <span className="text-[11px] text-stone-500 font-semibold block">Citizen Rating</span>
                        <span className="text-lg font-bold font-mono text-amber-600">{selectedDept.rating} ★</span>
                      </div>
                    </div>

                    <div className="space-y-3">
                      <h4 className="text-xs font-bold text-stone-900 uppercase tracking-wider">Top Grievance Categories</h4>
                      <div className="space-y-2.5">
                        {selectedDept.topIssues.map((sub) => (
                          <div key={sub.issue} className="space-y-1">
                            <div className="flex items-center justify-between text-xs font-semibold">
                              <span className="text-stone-700">{sub.issue}</span>
                              <div className="flex items-center gap-2 font-mono">
                                <span className="text-stone-900 font-bold">{sub.count.toLocaleString()}</span>
                                <span className="text-stone-400 text-[11px]">({sub.pct})</span>
                              </div>
                            </div>
                            <div className="w-full bg-stone-100 h-2 rounded-full overflow-hidden">
                              <div className="bg-[#c2410c] h-full rounded-full" style={{ width: sub.pct }} />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })()}
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 6: CITIZEN FEEDBACK */}
        {/* ===================================================================== */}
        {sidebarTab === 'feedback' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs">
              <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
                <div className="md:col-span-4 text-center md:border-r border-stone-200 md:pr-6">
                  <span className="text-4xl font-extrabold text-stone-900 font-mono">{displayCitizenScore}</span>
                  <span className="text-sm font-bold text-stone-400"> / 5.0</span>
                  <div className="flex justify-center gap-1 text-amber-400 my-1.5">
                    {[1, 2, 3, 4].map((s) => (
                      <Star key={s} className="w-4 h-4 fill-amber-400" />
                    ))}
                    <Star className="w-4 h-4 text-stone-300" />
                  </div>
                  <p className="text-xs font-bold text-emerald-600">{displayPositivePercent}% Positive Feedback Rate</p>
                </div>

                <div className="md:col-span-8 space-y-2">
                  <h4 className="text-xs font-bold text-stone-700 uppercase tracking-wider mb-2">
                    Filter by Star Rating
                  </h4>
                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={() => setFeedbackStarFilter('all')}
                      className={`px-3 py-1 rounded-lg text-xs font-bold border transition-all ${
                        feedbackStarFilter === 'all'
                          ? 'bg-[#c2410c] text-white border-[#c2410c]'
                          : 'bg-stone-50 text-stone-700 border-stone-200 hover:bg-stone-100'
                      }`}
                    >
                      All Ratings
                    </button>
                    {[5, 4, 3, 2, 1].map((stars) => (
                      <button
                        key={stars}
                        onClick={() => setFeedbackStarFilter(stars)}
                        className={`px-3 py-1 rounded-lg text-xs font-bold border transition-all flex items-center gap-1 ${
                          feedbackStarFilter === stars
                            ? 'bg-[#c2410c] text-white border-[#c2410c]'
                            : 'bg-stone-50 text-stone-700 border-stone-200 hover:bg-stone-100'
                        }`}
                      >
                        <span>{stars} ★</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* Verified Testimonials */}
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Verified Citizen Reviews</h3>
                  <p className="text-xs text-stone-500">Post-resolution feedback received via Kartavya App</p>
                </div>
                <div className="relative">
                  <Search className="w-3.5 h-3.5 text-stone-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={feedbackSearch}
                    onChange={(e) => setFeedbackSearch(e.target.value)}
                    placeholder="Search comments..."
                    className="bg-stone-50 border border-stone-200 rounded-lg pl-8 pr-3 py-1 text-xs text-stone-800 placeholder-stone-400 focus:outline-none focus:border-[#c2410c]"
                  />
                </div>
              </div>

              <div className="space-y-3">
                {[
                  {
                    name: 'Rameshwar Mahato',
                    ward: 'Ward 29, Kanke Road, Ranchi',
                    category: 'Roadways',
                    rating: 5,
                    time: '14 Aug 2025',
                    comment: 'Pothole was repaired within 24 hours of reporting! Smooth bitumen leveling restored.',
                  },
                  {
                    name: 'Sunita Devi',
                    ward: 'Harmu Housing Colony, Ranchi',
                    category: 'Electricity',
                    rating: 4,
                    time: '13 Aug 2025',
                    comment: 'Street light fixed and sagging wires tightened properly. Good response from lineman team.',
                  },
                  {
                    name: 'Dr. Vivek Sinha',
                    ward: 'Block C, Harmu, Ranchi',
                    category: 'Sewage',
                    rating: 5,
                    time: '12 Aug 2025',
                    comment: 'Sewage suction jetting was done efficiently. Cleared the manhole blockage completely.',
                  },
                  {
                    name: 'Amit Kumar Verma',
                    ward: 'Lalpur Chowk, Ranchi',
                    category: 'Waste Management',
                    rating: 4,
                    time: '11 Aug 2025',
                    comment: 'Compactor vehicle arrived by afternoon and cleared the entire overflow dump.',
                  },
                ]
                  .filter((item) => {
                    if (feedbackStarFilter !== 'all' && item.rating !== feedbackStarFilter) return false;
                    if (feedbackSearch.trim()) {
                      const q = feedbackSearch.toLowerCase();
                      return item.comment.toLowerCase().includes(q) || item.ward.toLowerCase().includes(q) || item.name.toLowerCase().includes(q);
                    }
                    return true;
                  })
                  .map((item, idx) => (
                    <div key={idx} className="p-4 rounded-xl border border-stone-200/80 bg-stone-50/50 space-y-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-stone-900 text-xs">{item.name}</span>
                          <span className="text-[10px] text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded font-semibold border border-emerald-200">
                            Citizen Verified
                          </span>
                        </div>
                        <div className="flex items-center gap-1 text-amber-400">
                          {Array.from({ length: item.rating }).map((_, i) => (
                            <Star key={i} className="w-3 h-3 fill-amber-400" />
                          ))}
                        </div>
                      </div>
                      <p className="text-xs text-stone-700 font-medium">"{item.comment}"</p>
                      <div className="flex items-center justify-between text-[11px] text-stone-400 pt-1 border-t border-stone-200/40">
                        <span>{item.ward} • <strong className="text-stone-600">{item.category}</strong></span>
                        <span className="font-mono">{item.time}</span>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        )}

        {/* ===================================================================== */}
        {/* TAB 7: COMPARATIVE VIEW */}
        {/* ===================================================================== */}
        {sidebarTab === 'comparative' && (
          <div className="space-y-6 animate-in fade-in duration-200">
            <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs space-y-5">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-stone-100">
                <div>
                  <h3 className="text-sm font-bold text-stone-900">Inter-District Benchmark Comparison</h3>
                  <p className="text-xs text-stone-500">Side-by-side performance metrics comparison</p>
                </div>
                <div className="flex items-center gap-2">
                  <select
                    value={compDistrictA}
                    onChange={(e) => setCompDistrictA(e.target.value)}
                    className="bg-stone-50 border border-stone-200 rounded-xl px-3 py-1.5 text-xs font-bold text-[#c2410c] focus:outline-none cursor-pointer"
                  >
                    {districtList.map((d) => (
                      <option key={d.id} value={d.id}>
                        District A: {d.name}
                      </option>
                    ))}
                  </select>
                  <span className="text-xs font-bold text-stone-400">VS</span>
                  <select
                    value={compDistrictB}
                    onChange={(e) => setCompDistrictB(e.target.value)}
                    className="bg-stone-50 border border-stone-200 rounded-xl px-3 py-1.5 text-xs font-bold text-[#0284c7] focus:outline-none cursor-pointer"
                  >
                    {districtList.map((d) => (
                      <option key={d.id} value={d.id}>
                        District B: {d.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {(() => {
                const distA = districtList.find((d) => d.id === compDistrictA) || districtList[0];
                const distB = districtList.find((d) => d.id === compDistrictB) || districtList[1];

                const compMetrics = [
                  { label: 'Weekly Inflow Volume', valA: `${distA.reportedThisWeek} tickets`, valB: `${distB.reportedThisWeek} tickets` },
                  { label: 'Resolution Rate (%)', valA: `${distA.resolutionRate}%`, valB: `${distB.resolutionRate}%` },
                  { label: 'Pending Open Issues', valA: `${distA.openIssuesCount}`, valB: `${distB.openIssuesCount}` },
                  { label: 'Critical Breaches', valA: `${distA.criticalIssuesCount}`, valB: `${distB.criticalIssuesCount}` },
                  { label: 'Top Sector', valA: distA.topDepartmentIssue, valB: distB.topDepartmentIssue },
                  { label: 'Nodal Officer', valA: distA.nodalOfficer.name, valB: distB.nodalOfficer.name },
                ];

                return (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 rounded-xl border border-[#fed7aa] bg-[#fff7ed] text-center">
                        <span className="text-[10px] font-bold uppercase text-[#c2410c] tracking-wider block">District A</span>
                        <h4 className="text-base font-extrabold text-stone-900">{distA.name}</h4>
                        <span className="text-xs text-stone-500 font-medium font-mono">{distA.resolutionRate}% SLA Rate</span>
                      </div>
                      <div className="p-4 rounded-xl border border-sky-200 bg-sky-50 text-center">
                        <span className="text-[10px] font-bold uppercase text-sky-700 tracking-wider block">District B</span>
                        <h4 className="text-base font-extrabold text-stone-900">{distB.name}</h4>
                        <span className="text-xs text-stone-500 font-medium font-mono">{distB.resolutionRate}% SLA Rate</span>
                      </div>
                    </div>

                    <div className="divide-y divide-stone-100 border border-stone-200 rounded-xl overflow-hidden bg-stone-50/50">
                      {compMetrics.map((m) => (
                        <div key={m.label} className="grid grid-cols-12 p-3 text-xs items-center hover:bg-stone-50 transition-colors">
                          <div className="col-span-5 font-mono font-bold text-stone-800 text-left pl-2">
                            {m.valA}
                          </div>
                          <div className="col-span-2 text-center text-stone-400 font-semibold text-[11px]">
                            {m.label}
                          </div>
                          <div className="col-span-5 font-mono font-bold text-stone-800 text-right pr-2">
                            {m.valB}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })()}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
