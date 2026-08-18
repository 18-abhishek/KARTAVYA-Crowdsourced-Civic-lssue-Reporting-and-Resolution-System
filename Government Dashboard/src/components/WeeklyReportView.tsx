import React, { useState, useRef, useEffect } from 'react';
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
  reportedIsDown: boolean;
  resolvedChange: string;
  resolvedIsUp: boolean;
  rateChange: string;
  rateIsUp: boolean;
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
  reportedIsDown: boolean;
  resolvedChange: string;
  resolvedIsUp: boolean;
  rateChange: string;
  rateIsUp: boolean;
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
    rateIsUp: true,
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
      { name: 'Roadways / Potholes', count: 782, percentage: 34, color: '#78716c' },
      { name: 'Waste Management', count: 690, percentage: 30, color: '#c2410c' },
      { name: 'Sewage & Drainage', count: 483, percentage: 21, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 345, percentage: 15, color: '#d97706' },
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
    reportedChange: '↓ 4.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 12.0%',
    resolvedIsUp: true,
    rateChange: '↑ 6.5%',
    rateIsUp: true,
    dataInsight: 'DMC improved streetlight breakdown turnaround by 18% across Bank More and Saraidhela commercial hubs.',
    avgSlaHours: 27.5,
    citizenScore: 4.1,
    positiveRatingPercent: 81,
    wards: [
      { name: 'Ward 14 (Bank More)', reported: 440, resolved: 380, rate: '86.3%' },
      { name: 'Ward 8 (Hirapur)', reported: 410, resolved: 350, rate: '85.3%' },
      { name: 'Ward 21 (Saraidhela)', reported: 390, resolved: 330, rate: '84.6%' },
      { name: 'Ward 27 (Jharia)', reported: 370, resolved: 310, rate: '83.7%' },
      { name: 'Ward 32 (Katras)', reported: 250, resolved: 210, rate: '84.0%' },
      { name: 'Ward 19 (Govindpur)', reported: 140, resolved: 120, rate: '85.7%' },
    ],
    deptStats: [
      { name: 'Electricity & Lighting', count: 620, percentage: 31, color: '#d97706' },
      { name: 'Waste Management', count: 580, percentage: 29, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 500, percentage: 25, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 300, percentage: 15, color: '#0284c7' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 29.0 },
      { day: 'Tue', avgHours: 33.2 },
      { day: 'Wed', avgHours: 28.1 },
      { day: 'Thu', avgHours: 25.0 },
      { day: 'Fri', avgHours: 31.4 },
      { day: 'Sat', avgHours: 24.5 },
      { day: 'Sun', avgHours: 21.3 },
    ],
  },
  Jamshedpur: {
    cityName: 'Jamshedpur',
    corporationName: 'Jamshedpur Notified Area Committee (JNAC)',
    totalReported: 1800,
    totalResolved: 1500,
    resolutionRate: 83.3,
    reportedChange: '↓ 5.0%',
    reportedIsDown: true,
    resolvedChange: '↑ 9.5%',
    resolvedIsUp: true,
    rateChange: '↑ 4.8%',
    rateIsUp: true,
    dataInsight: 'Waste collection frequency in Sakchi and Bistupur commercial corridors reached 98% daily consistency.',
    avgSlaHours: 26.2,
    citizenScore: 4.3,
    positiveRatingPercent: 85,
    wards: [
      { name: 'Ward 6 (Bistupur)', reported: 410, resolved: 360, rate: '87.8%' },
      { name: 'Ward 11 (Sakchi)', reported: 390, resolved: 330, rate: '84.6%' },
      { name: 'Ward 15 (Kadma)', reported: 360, resolved: 300, rate: '83.3%' },
      { name: 'Ward 22 (Sonari)', reported: 340, resolved: 280, rate: '82.3%' },
      { name: 'Ward 9 (Telco)', reported: 210, resolved: 170, rate: '80.9%' },
      { name: 'Ward 18 (Mango)', reported: 90, resolved: 60, rate: '66.6%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 648, percentage: 36, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 468, percentage: 26, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 414, percentage: 23, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 270, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 27.5 },
      { day: 'Tue', avgHours: 31.0 },
      { day: 'Wed', avgHours: 26.8 },
      { day: 'Thu', avgHours: 23.5 },
      { day: 'Fri', avgHours: 29.0 },
      { day: 'Sat', avgHours: 23.0 },
      { day: 'Sun', avgHours: 22.5 },
    ],
  },
  Bokaro: {
    cityName: 'Bokaro',
    corporationName: 'Chas Municipal Corporation & Bokaro Township',
    totalReported: 1500,
    totalResolved: 1400,
    resolutionRate: 93.3,
    reportedChange: '↓ 7.1%',
    reportedIsDown: true,
    resolvedChange: '↑ 15.6%',
    resolvedIsUp: true,
    rateChange: '↑ 8.2%',
    rateIsUp: true,
    dataInsight: 'Bokaro achieved the highest SLA compliance in the state with an average resolution turnaround under 21 hours.',
    avgSlaHours: 21.0,
    citizenScore: 4.5,
    positiveRatingPercent: 91,
    wards: [
      { name: 'Ward 10 (Sec 4)', reported: 360, resolved: 340, rate: '94.4%' },
      { name: 'Ward 15 (Chas)', reported: 330, resolved: 310, rate: '93.9%' },
      { name: 'Ward 7 (Sec 1)', reported: 300, resolved: 280, rate: '93.3%' },
      { name: 'Ward 21 (Sec 9)', reported: 280, resolved: 260, rate: '92.8%' },
      { name: 'Ward 3 (Sec 12)', reported: 150, resolved: 140, rate: '93.3%' },
      { name: 'Ward 18 (Kundi)', reported: 80, resolved: 70, rate: '87.5%' },
    ],
    deptStats: [
      { name: 'Sewage & Drainage', count: 450, percentage: 30, color: '#0284c7' },
      { name: 'Roadways / Potholes', count: 420, percentage: 28, color: '#78716c' },
      { name: 'Waste Management', count: 390, percentage: 26, color: '#c2410c' },
      { name: 'Electricity & Lighting', count: 240, percentage: 16, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 22.0 },
      { day: 'Tue', avgHours: 25.5 },
      { day: 'Wed', avgHours: 21.8 },
      { day: 'Thu', avgHours: 18.0 },
      { day: 'Fri', avgHours: 24.2 },
      { day: 'Sat', avgHours: 19.5 },
      { day: 'Sun', avgHours: 16.0 },
    ],
  },
  Deoghar: {
    cityName: 'Deoghar',
    corporationName: 'Deoghar Municipal Corporation (DMC)',
    totalReported: 900,
    totalResolved: 800,
    resolutionRate: 88.8,
    reportedChange: '↓ 3.8%',
    reportedIsDown: true,
    resolvedChange: '↑ 11.2%',
    resolvedIsUp: true,
    rateChange: '↑ 6.0%',
    rateIsUp: true,
    dataInsight: 'Deoghar Municipal Corporation completed 100% of temple corridor solid waste clearance within 2 hours.',
    avgSlaHours: 24.5,
    citizenScore: 4.3,
    positiveRatingPercent: 86,
    wards: [
      { name: 'Ward 5 (Tower Chowk)', reported: 240, resolved: 220, rate: '91.6%' },
      { name: 'Ward 12 (Mandir Rd)', reported: 220, resolved: 200, rate: '90.9%' },
      { name: 'Ward 8 (Castairs Town)', reported: 180, resolved: 160, rate: '88.8%' },
      { name: 'Ward 19 (Jasidih)', reported: 140, resolved: 120, rate: '85.7%' },
      { name: 'Ward 3 (Rohini)', reported: 80, resolved: 70, rate: '87.5%' },
      { name: 'Ward 16 (Kunda)', reported: 40, resolved: 30, rate: '75.0%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 378, percentage: 42, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 234, percentage: 26, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 180, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 108, percentage: 12, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 25.0 },
      { day: 'Tue', avgHours: 29.5 },
      { day: 'Wed', avgHours: 25.2 },
      { day: 'Thu', avgHours: 21.0 },
      { day: 'Fri', avgHours: 28.0 },
      { day: 'Sat', avgHours: 22.5 },
      { day: 'Sun', avgHours: 20.3 },
    ],
  },
  Hazaribagh: {
    cityName: 'Hazaribagh',
    corporationName: 'Hazaribagh Municipal Corporation (HMC)',
    totalReported: 620,
    totalResolved: 540,
    resolutionRate: 87.1,
    reportedChange: '↓ 5.2%',
    reportedIsDown: true,
    resolvedChange: '↑ 8.8%',
    resolvedIsUp: true,
    rateChange: '↑ 4.5%',
    rateIsUp: true,
    dataInsight: 'HMC potable water supply grievance redressal reduced pipeline repair turnaround to 18 hours.',
    avgSlaHours: 25.8,
    citizenScore: 4.2,
    positiveRatingPercent: 83,
    wards: [
      { name: 'Ward 4 (Bada Bazar)', reported: 160, resolved: 140, rate: '87.5%' },
      { name: 'Ward 9 (Matwari)', reported: 140, resolved: 125, rate: '89.2%' },
      { name: 'Ward 14 (Korrah)', reported: 130, resolved: 115, rate: '88.4%' },
      { name: 'Ward 18 (Lake Road)', reported: 110, resolved: 95, rate: '86.3%' },
      { name: 'Ward 7 (Dipugarha)', reported: 50, resolved: 40, rate: '80.0%' },
      { name: 'Ward 21 (Hurhuru)', reported: 30, resolved: 25, rate: '83.3%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 217, percentage: 35, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 186, percentage: 30, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 130, percentage: 21, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 87, percentage: 14, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 26.5 },
      { day: 'Tue', avgHours: 30.0 },
      { day: 'Wed', avgHours: 27.2 },
      { day: 'Thu', avgHours: 22.8 },
      { day: 'Fri', avgHours: 29.4 },
      { day: 'Sat', avgHours: 23.5 },
      { day: 'Sun', avgHours: 21.0 },
    ],
  },
  Giridih: {
    cityName: 'Giridih',
    corporationName: 'Giridih Municipal Council',
    totalReported: 510,
    totalResolved: 440,
    resolutionRate: 86.2,
    reportedChange: '↓ 3.0%',
    reportedIsDown: true,
    resolvedChange: '↑ 7.5%',
    resolvedIsUp: true,
    rateChange: '↑ 3.8%',
    rateIsUp: true,
    dataInsight: 'Pothole patch-up teams covered 14 kilometers of internal town roads in Makatpur and Pachamba.',
    avgSlaHours: 27.0,
    citizenScore: 4.1,
    positiveRatingPercent: 80,
    wards: [
      { name: 'Ward 3 (Bada Chowk)', reported: 130, resolved: 115, rate: '88.4%' },
      { name: 'Ward 8 (Makatpur)', reported: 120, resolved: 105, rate: '87.5%' },
      { name: 'Ward 12 (Pachamba)', reported: 110, resolved: 95, rate: '86.3%' },
      { name: 'Ward 16 (Beniadih)', reported: 90, resolved: 75, rate: '83.3%' },
      { name: 'Ward 20 (Sirsiya)', reported: 40, resolved: 35, rate: '87.5%' },
      { name: 'Ward 6 (Bhandaridih)', reported: 20, resolved: 15, rate: '75.0%' },
    ],
    deptStats: [
      { name: 'Roadways / Potholes', count: 178, percentage: 35, color: '#78716c' },
      { name: 'Waste Management', count: 153, percentage: 30, color: '#c2410c' },
      { name: 'Electricity & Lighting', count: 102, percentage: 20, color: '#d97706' },
      { name: 'Sewage & Drainage', count: 77, percentage: 15, color: '#0284c7' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 28.0 },
      { day: 'Tue', avgHours: 32.5 },
      { day: 'Wed', avgHours: 28.0 },
      { day: 'Thu', avgHours: 24.5 },
      { day: 'Fri', avgHours: 30.0 },
      { day: 'Sat', avgHours: 24.8 },
      { day: 'Sun', avgHours: 21.2 },
    ],
  },
  Ramgarh: {
    cityName: 'Ramgarh',
    corporationName: 'Ramgarh Cantonment Board & Nagar Parishad',
    totalReported: 380,
    totalResolved: 340,
    resolutionRate: 89.4,
    reportedChange: '↓ 6.1%',
    reportedIsDown: true,
    resolvedChange: '↑ 10.4%',
    resolvedIsUp: true,
    rateChange: '↑ 5.2%',
    rateIsUp: true,
    dataInsight: 'Cantt board emergency streetlight maintenance team achieved zero overdue pending tickets this cycle.',
    avgSlaHours: 23.4,
    citizenScore: 4.3,
    positiveRatingPercent: 87,
    wards: [
      { name: 'Ward 2 (Main Road)', reported: 100, resolved: 90, rate: '90.0%' },
      { name: 'Ward 5 (Subhash Chowk)', reported: 90, resolved: 82, rate: '91.1%' },
      { name: 'Ward 8 (Thana Chowk)', reported: 80, resolved: 72, rate: '90.0%' },
      { name: 'Ward 11 (Barkakana)', reported: 65, resolved: 56, rate: '86.1%' },
      { name: 'Ward 14 (Chitarpur)', reported: 30, resolved: 26, rate: '86.6%' },
      { name: 'Ward 7 (Kothar)', reported: 15, resolved: 14, rate: '93.3%' },
    ],
    deptStats: [
      { name: 'Roadways / Potholes', count: 133, percentage: 35, color: '#78716c' },
      { name: 'Waste Management', count: 114, percentage: 30, color: '#c2410c' },
      { name: 'Sewage & Drainage', count: 76, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 57, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 24.0 },
      { day: 'Tue', avgHours: 27.5 },
      { day: 'Wed', avgHours: 24.2 },
      { day: 'Thu', avgHours: 20.0 },
      { day: 'Fri', avgHours: 26.5 },
      { day: 'Sat', avgHours: 21.0 },
      { day: 'Sun', avgHours: 20.4 },
    ],
  },
  Dumka: {
    cityName: 'Dumka',
    corporationName: 'Dumka Municipal Council',
    totalReported: 290,
    totalResolved: 260,
    resolutionRate: 89.6,
    reportedChange: '↓ 4.0%',
    reportedIsDown: true,
    resolvedChange: '↑ 8.5%',
    resolvedIsUp: true,
    rateChange: '↑ 4.2%',
    rateIsUp: true,
    dataInsight: 'Door-to-door waste collection coverage expanded to 100% of Dudhani and Rasikpur households.',
    avgSlaHours: 24.0,
    citizenScore: 4.2,
    positiveRatingPercent: 85,
    wards: [
      { name: 'Ward 1 (Tinbazar)', reported: 80, resolved: 72, rate: '90.0%' },
      { name: 'Ward 4 (Rasikpur)', reported: 70, resolved: 64, rate: '91.4%' },
      { name: 'Ward 7 (Dudhani)', reported: 60, resolved: 54, rate: '90.0%' },
      { name: 'Ward 10 (Bandhabpara)', reported: 50, resolved: 44, rate: '88.0%' },
      { name: 'Ward 13 (Kurwa)', reported: 20, resolved: 18, rate: '90.0%' },
      { name: 'Ward 6 (Dangalpada)', reported: 10, resolved: 8, rate: '80.0%' },
    ],
    deptStats: [
      { name: 'Waste Management', count: 101, percentage: 35, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 87, percentage: 30, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 58, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 44, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 25.0 },
      { day: 'Tue', avgHours: 28.0 },
      { day: 'Wed', avgHours: 24.5 },
      { day: 'Thu', avgHours: 21.2 },
      { day: 'Fri', avgHours: 27.0 },
      { day: 'Sat', avgHours: 22.0 },
      { day: 'Sun', avgHours: 20.3 },
    ],
  },
  Medininagar: {
    cityName: 'Medininagar',
    corporationName: 'Medininagar Municipal Corporation (MMC)',
    totalReported: 260,
    totalResolved: 220,
    resolutionRate: 84.6,
    reportedChange: '↓ 2.5%',
    reportedIsDown: true,
    resolvedChange: '↑ 6.0%',
    resolvedIsUp: true,
    rateChange: '↑ 3.2%',
    rateIsUp: true,
    dataInsight: 'Borewell pump and pipeline repairs accelerated with mobile mechanic units deployed across MMC.',
    avgSlaHours: 28.2,
    citizenScore: 4.0,
    positiveRatingPercent: 79,
    wards: [
      { name: 'Ward 2 (Shahpur)', reported: 70, resolved: 60, rate: '85.7%' },
      { name: 'Ward 5 (Chowk Bazar)', reported: 65, resolved: 56, rate: '86.1%' },
      { name: 'Ward 8 (Kizirpur)', reported: 55, resolved: 46, rate: '83.6%' },
      { name: 'Ward 12 (Station Rd)', reported: 45, resolved: 38, rate: '84.4%' },
      { name: 'Ward 16 (Sudarshan)', reported: 15, resolved: 12, rate: '80.0%' },
      { name: 'Ward 10 (Redma)', reported: 10, resolved: 8, rate: '80.0%' },
    ],
    deptStats: [
      { name: 'Roadways / Potholes', count: 91, percentage: 35, color: '#78716c' },
      { name: 'Waste Management', count: 78, percentage: 30, color: '#c2410c' },
      { name: 'Sewage & Drainage', count: 52, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 39, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 29.5 },
      { day: 'Tue', avgHours: 33.0 },
      { day: 'Wed', avgHours: 29.0 },
      { day: 'Thu', avgHours: 25.5 },
      { day: 'Fri', avgHours: 31.8 },
      { day: 'Sat', avgHours: 26.0 },
      { day: 'Sun', avgHours: 22.6 },
    ],
  },
};

const AVAILABLE_CITIES = Object.keys(CITY_METRICS_DATABASE);

const HISTORICAL_WEEKLY_REPORTS: WeeklyReportOption[] = [
  {
    id: 'w33',
    label: 'Aug 08 – Aug 15, 2025',
    weekNumber: 'Week 33 (Current)',
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
    rateIsUp: true,
    dataInsight: 'Resolution rate improved by 15.3% compared to last week across urban municipal corporations.',
    avgSlaHours: 28.4,
    citizenScore: 4.2,
    positiveRatingPercent: 84,
    deptStats: [
      { name: 'Waste Management', count: 2983, percentage: 38, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 2120, percentage: 27, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1570, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1177, percentage: 15, color: '#d97706' },
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
    label: 'Aug 01 – Aug 07, 2025',
    weekNumber: 'Week 32',
    month: 'August 2025',
    totalReported: 8370,
    totalResolved: 6540,
    resolutionRate: 78.1,
    reportedChange: '↑ 5.4%',
    reportedIsDown: false,
    resolvedChange: '↑ 8.2%',
    resolvedIsUp: true,
    rateChange: '↑ 2.4%',
    rateIsUp: true,
    dataInsight: 'Road maintenance complaints peaked due to heavy monsoon showers in Dhanbad and Ranchi.',
    avgSlaHours: 32.1,
    citizenScore: 4.1,
    positiveRatingPercent: 81,
    deptStats: [
      { name: 'Waste Management', count: 2840, percentage: 34, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 2680, percentage: 32, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1670, percentage: 20, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1180, percentage: 14, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 34.2 },
      { day: 'Tue', avgHours: 38.0 },
      { day: 'Wed', avgHours: 33.5 },
      { day: 'Thu', avgHours: 29.1 },
      { day: 'Fri', avgHours: 36.4 },
      { day: 'Sat', avgHours: 28.0 },
      { day: 'Sun', avgHours: 25.5 },
    ],
  },
  {
    id: 'w31',
    label: 'Jul 25 – Jul 31, 2025',
    weekNumber: 'Week 31',
    month: 'July 2025',
    totalReported: 7940,
    totalResolved: 6010,
    resolutionRate: 75.7,
    reportedChange: '↓ 6.0%',
    reportedIsDown: true,
    resolvedChange: '↓ 3.2%',
    resolvedIsUp: false,
    rateChange: '↑ 2.2%',
    rateIsUp: true,
    dataInsight: 'Electricity & street lighting restoration achieved 94% SLA compliance statewide.',
    avgSlaHours: 34.8,
    citizenScore: 4.0,
    positiveRatingPercent: 79,
    deptStats: [
      { name: 'Waste Management', count: 3100, percentage: 39, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 2140, percentage: 27, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1430, percentage: 18, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1270, percentage: 16, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 36.5 },
      { day: 'Tue', avgHours: 39.2 },
      { day: 'Wed', avgHours: 34.0 },
      { day: 'Thu', avgHours: 31.8 },
      { day: 'Fri', avgHours: 37.1 },
      { day: 'Sat', avgHours: 30.5 },
      { day: 'Sun', avgHours: 27.2 },
    ],
  },
  {
    id: 'w30',
    label: 'Jul 18 – Jul 24, 2025',
    weekNumber: 'Week 30',
    month: 'July 2025',
    totalReported: 8450,
    totalResolved: 6210,
    resolutionRate: 73.5,
    reportedChange: '↑ 10.0%',
    reportedIsDown: false,
    resolvedChange: '↑ 9.3%',
    resolvedIsUp: true,
    rateChange: '↓ 0.5%',
    rateIsUp: false,
    dataInsight: 'Water logging and drain blockage triage teams deployed in high-density flood prone wards.',
    avgSlaHours: 36.8,
    citizenScore: 3.9,
    positiveRatingPercent: 77,
    deptStats: [
      { name: 'Waste Management', count: 2870, percentage: 34, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 2535, percentage: 30, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1945, percentage: 23, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1100, percentage: 13, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 38.0 },
      { day: 'Tue', avgHours: 42.1 },
      { day: 'Wed', avgHours: 36.8 },
      { day: 'Thu', avgHours: 33.4 },
      { day: 'Fri', avgHours: 39.5 },
      { day: 'Sat', avgHours: 32.1 },
      { day: 'Sun', avgHours: 29.0 },
    ],
  },
  {
    id: 'w29',
    label: 'Jul 11 – Jul 17, 2025',
    weekNumber: 'Week 29',
    month: 'July 2025',
    totalReported: 7680,
    totalResolved: 5680,
    resolutionRate: 74.0,
    reportedChange: '↑ 2.1%',
    reportedIsDown: false,
    resolvedChange: '↑ 4.8%',
    resolvedIsUp: true,
    rateChange: '↑ 1.9%',
    rateIsUp: true,
    dataInsight: 'Special sanitation drives launched in Deoghar ahead of annual Shravani Mela festival.',
    avgSlaHours: 38.2,
    citizenScore: 3.9,
    positiveRatingPercent: 78,
    deptStats: [
      { name: 'Waste Management', count: 3225, percentage: 42, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 1996, percentage: 26, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1382, percentage: 18, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1077, percentage: 14, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 40.2 },
      { day: 'Tue', avgHours: 43.5 },
      { day: 'Wed', avgHours: 37.9 },
      { day: 'Thu', avgHours: 35.0 },
      { day: 'Fri', avgHours: 41.2 },
      { day: 'Sat', avgHours: 34.6 },
      { day: 'Sun', avgHours: 30.1 },
    ],
  },
  {
    id: 'w28',
    label: 'Jul 04 – Jul 10, 2025',
    weekNumber: 'Week 28',
    month: 'July 2025',
    totalReported: 7520,
    totalResolved: 5420,
    resolutionRate: 72.1,
    reportedChange: '↑ 2.9%',
    reportedIsDown: false,
    resolvedChange: '↑ 4.4%',
    resolvedIsUp: true,
    rateChange: '↑ 1.1%',
    rateIsUp: true,
    dataInsight: 'Automated civic grievance dispatch engine rolled out to 12 additional municipal corporations.',
    avgSlaHours: 39.5,
    citizenScore: 3.8,
    positiveRatingPercent: 76,
    deptStats: [
      { name: 'Waste Management', count: 2932, percentage: 39, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 2030, percentage: 27, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1428, percentage: 19, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1130, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 42.0 },
      { day: 'Tue', avgHours: 45.2 },
      { day: 'Wed', avgHours: 39.1 },
      { day: 'Thu', avgHours: 36.5 },
      { day: 'Fri', avgHours: 42.8 },
      { day: 'Sat', avgHours: 35.7 },
      { day: 'Sun', avgHours: 31.4 },
    ],
  },
  {
    id: 'w27',
    label: 'Jun 27 – Jul 03, 2025',
    weekNumber: 'Week 27',
    month: 'June 2025',
    totalReported: 7310,
    totalResolved: 5190,
    resolutionRate: 71.0,
    reportedChange: '↓ 1.5%',
    reportedIsDown: true,
    resolvedChange: '↑ 3.8%',
    resolvedIsUp: true,
    rateChange: '↑ 3.8%',
    rateIsUp: true,
    dataInsight: 'Baseline performance audit completed for Q2 across all 24 administrative districts.',
    avgSlaHours: 41.0,
    citizenScore: 3.8,
    positiveRatingPercent: 75,
    deptStats: [
      { name: 'Waste Management', count: 2850, percentage: 39, color: '#c2410c' },
      { name: 'Roadways / Potholes', count: 1973, percentage: 27, color: '#78716c' },
      { name: 'Sewage & Drainage', count: 1388, percentage: 19, color: '#0284c7' },
      { name: 'Electricity & Lighting', count: 1099, percentage: 15, color: '#d97706' },
    ],
    slaPoints: [
      { day: 'Mon', avgHours: 43.5 },
      { day: 'Tue', avgHours: 46.8 },
      { day: 'Wed', avgHours: 41.2 },
      { day: 'Thu', avgHours: 38.0 },
      { day: 'Fri', avgHours: 44.5 },
      { day: 'Sat', avgHours: 37.0 },
      { day: 'Sun', avgHours: 32.8 },
    ],
  },
];

export const WeeklyReportView: React.FC<WeeklyReportViewProps> = ({ onExportPdf }) => {
  const [sidebarTab, setSidebarTab] = useState<'overview' | 'inflow' | 'resolution' | 'sla' | 'departments' | 'feedback' | 'comparative'>('overview');
  const [viewScope, setViewScope] = useState<'state' | 'city'>('state');
  const [selectedCity, setSelectedCity] = useState<string>('Ranchi');
  const [selectedTopDistrictsCount, setSelectedTopDistrictsCount] = useState<string>('Top 5 Districts');
  const [selectedWeekId, setSelectedWeekId] = useState<string>('w33');
  const [isDateDropdownOpen, setIsDateDropdownOpen] = useState(false);
  const [isCityDropdownOpen, setIsCityDropdownOpen] = useState(false);
  const [hoveredBarIndex, setHoveredBarIndex] = useState<number | null>(null);

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
  const displayDeptStats = isCityMode ? activeCityData.deptStats : currentReport.deptStats;
  const displaySlaPoints = isCityMode ? activeCityData.slaPoints : currentReport.slaPoints;
  const displayAvgSlaHours = isCityMode ? activeCityData.avgSlaHours : currentReport.avgSlaHours;
  const displayCitizenScore = isCityMode ? activeCityData.citizenScore : currentReport.citizenScore;
  const displayPositivePercent = isCityMode ? activeCityData.positiveRatingPercent : currentReport.positiveRatingPercent;

  // Build All 24 Districts dataset proportional to the active week
  const weekRatio = currentReport.totalReported / 7850;
  const all24DistrictsData = districtList.map((d) => {
    const rawReported = d.reportedThisWeek || Math.round(d.totalIssues * 0.6);
    const rawResolved = d.resolved || Math.round(d.totalIssues * 0.5);
    const adjustedReported = Math.max(20, Math.round(rawReported * weekRatio));
    const adjustedResolved = Math.max(15, Math.round(rawResolved * weekRatio));
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
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start animate-in fade-in duration-300">
      {/* Left Secondary Sidebar (col-span-3 on lg) */}
      <div className="lg:col-span-3 space-y-6">
        <div className="bg-white rounded-2xl border border-stone-200 p-3 shadow-xs space-y-1">
          {sidebarItems.map((item) => {
            const Icon = item.icon;
            const isActive = sidebarTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setSidebarTab(item.id as any)}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-colors text-left ${
                  isActive
                    ? 'bg-[#c2410c] text-white shadow-2xs'
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
        <div className="bg-[#fffbf6] rounded-2xl border border-[#fed7aa]/60 p-4 shadow-xs">
          <div className="flex items-center gap-2 text-[#ea580c] font-bold text-xs mb-1.5">
            <Lightbulb className="w-4 h-4 text-[#ea580c]" />
            <span>{isCityMode ? `${selectedCity} City Insights` : 'State Data Insights'}</span>
          </div>
          <p className="text-xs text-stone-600 leading-relaxed font-medium">
            {displayDataInsight}
          </p>
        </div>
      </div>

      {/* Main Analytics Content (col-span-9 on lg) */}
      <div className="lg:col-span-9 space-y-6">
        {/* Top Controls Bar */}
        <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h2 className="text-base sm:text-lg font-bold text-stone-900 tracking-tight">
                {isCityMode ? `${selectedCity} Performance & Analytics` : 'Weekly Performance & Analytics Report'}
              </h2>
              {isCityMode ? (
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-extrabold bg-[#fff5ee] text-[#c2410c] border border-[#fed7aa]">
                  <Building2 className="w-3 h-3" />
                  <span>{activeCityData.corporationName}</span>
                </span>
              ) : currentReport.isLatest ? (
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-emerald-100 text-emerald-800 border border-emerald-200">
                  Live
                </span>
              ) : (
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-amber-100 text-amber-800 border border-amber-200">
                  Archived
                </span>
              )}
            </div>
            <p className="text-xs text-stone-500 font-medium mt-0.5">
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
                className={`inline-flex items-center gap-2 px-3.5 py-1.5 border rounded-xl text-xs font-semibold transition-all shadow-2xs ${
                  isDateDropdownOpen
                    ? 'bg-stone-100 border-[#c2410c] text-stone-900 ring-2 ring-[#c2410c]/15'
                    : 'bg-white hover:bg-stone-50 border-stone-300 text-stone-800'
                }`}
                title="Select report week"
              >
                <Calendar className="w-3.5 h-3.5 text-[#c2410c]" />
                <span className="font-semibold">{currentReport.label}</span>
                <ChevronDown
                  className={`w-3.5 h-3.5 text-stone-500 transition-transform duration-200 ${
                    isDateDropdownOpen ? 'rotate-180 text-[#c2410c]' : ''
                  }`}
                />
              </button>

              {/* Dropdown Menu Popup */}
              {isDateDropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-72 sm:w-80 bg-white rounded-2xl border border-stone-200 shadow-2xl z-50 p-2 animate-in fade-in zoom-in-95 origin-top-right">
                  <div className="px-3 py-2 border-b border-stone-100 mb-1 flex items-center justify-between">
                    <div className="flex items-center gap-1.5">
                      <History className="w-3.5 h-3.5 text-[#c2410c]" />
                      <h4 className="text-xs font-bold text-stone-900">Historical Weekly Audits</h4>
                    </div>
                    <span className="text-[10px] font-bold px-2 py-0.5 bg-stone-100 text-stone-600 rounded-full">
                      {HISTORICAL_WEEKLY_REPORTS.length} Weeks
                    </span>
                  </div>

                  <div className="max-h-64 overflow-y-auto space-y-1 py-1 pr-1 custom-scrollbar">
                    {HISTORICAL_WEEKLY_REPORTS.map((week) => {
                      const isSelected = week.id === selectedWeekId;
                      return (
                        <button
                          key={week.id}
                          type="button"
                          onClick={() => {
                            setSelectedWeekId(week.id);
                            setIsDateDropdownOpen(false);
                          }}
                          className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-left transition-all ${
                            isSelected
                              ? 'bg-[#fff5ee] text-[#c2410c] font-bold border border-[#fed7aa]'
                              : 'hover:bg-stone-50 text-stone-700 font-medium border border-transparent'
                          }`}
                        >
                          <div className="space-y-0.5">
                            <div className="flex items-center gap-1.5">
                              <span className="text-xs">{week.weekNumber}</span>
                              {week.isLatest && (
                                <span className="text-[9px] font-extrabold px-1.5 py-0.2 rounded bg-emerald-100 text-emerald-800">
                                  Current
                                </span>
                              )}
                            </div>
                            <p className="text-[11px] text-stone-500 font-normal flex items-center gap-1">
                              <Clock className="w-3 h-3 text-stone-400" />
                              <span>{week.label}</span>
                            </p>
                          </div>

                          <div className="text-right">
                            <span
                              className={`text-[11px] font-mono font-bold px-2 py-0.5 rounded-full ${
                                isSelected
                                  ? 'bg-[#c2410c] text-white'
                                  : 'bg-stone-100 text-stone-700'
                              }`}
                            >
                              {week.resolutionRate}%
                            </span>
                          </div>
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
                onClick={() => setViewScope('city')}
                className={`px-3 py-1 rounded-lg transition-all ${
                  viewScope === 'city'
                    ? 'bg-[#c2410c] text-white shadow-2xs font-bold'
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

        {/* Charts Grid: 2x2 Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 1. Comparative Bar Chart (District-wise in State View VS Ward-wise in City View) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
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

              {/* Chart Legend & Horizontal Scroll Indicator */}
              <div className="flex items-center justify-between text-xs font-medium text-stone-600 mt-2 mb-4">
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-xs bg-[#c2410c]" />
                    <span>Reported Issues</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-3 h-3 rounded-xs bg-[#78716c]" />
                    <span>Resolved Issues</span>
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
                          rx="2.5"
                          fill={isHovered ? '#ea580c' : '#c2410c'}
                          className="transition-all duration-150"
                        />

                        {/* Resolved Bar */}
                        <rect
                          x={startX + gapBetweenBars}
                          y={190 - resolvedHeight}
                          width={barWidth}
                          height={resolvedHeight}
                          rx="2.5"
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
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
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
                    {/* Dynamic Circle segments */}
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
                            className="cursor-pointer hover:opacity-85 transition-opacity"
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
                <strong className="text-stone-900">{displayDeptStats[0].name}</strong> is the primary grievance driver in {isCityMode ? selectedCity : currentReport.weekNumber} ({displayDeptStats[0].percentage}% of local volume).
              </p>
            </div>
          </div>

          {/* 3. SLA Performance (Average Resolution Time) (Line Chart) */}
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
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
                  <span className="w-3 h-0.5 bg-[#c2410c]" />
                  <span>{isCityMode ? `${selectedCity} Avg (hrs)` : 'State Avg (hrs)'}</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="w-3 h-0.5 bg-[#dc2626] border-b border-dashed border-red-500" />
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
          <div className="bg-white rounded-2xl border border-stone-200 p-5 shadow-xs flex flex-col justify-between">
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
    </div>
  );
};
