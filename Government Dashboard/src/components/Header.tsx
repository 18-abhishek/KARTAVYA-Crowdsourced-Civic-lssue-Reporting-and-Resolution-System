import React, { useState } from 'react';
import { TabType, UserProfile } from '../types';
import { LayoutGrid, Calendar, ListOrdered, Bell, ChevronDown, Shield, LogOut, CheckCircle2, User, Globe } from 'lucide-react';
import logoImg from '../assets/logo.jpeg';

interface HeaderProps {
  activeTab: TabType;
  setActiveTab: (tab: TabType) => void;
  currentUser: UserProfile;
  unreadNotificationsCount: number;
  onOpenNewGrievance?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  activeTab,
  setActiveTab,
  currentUser,
  unreadNotificationsCount,
}) => {
  const [profileOpen, setProfileOpen] = useState(false);
  const [language, setLanguage] = useState<'EN' | 'HI'>('EN');

  return (
    <header className="fixed top-0 left-0 right-0 z-50 glass-header">
      <div className="max-w-[1750px] w-full mx-auto px-4 sm:px-6 lg:px-8 h-18 flex items-center justify-between gap-4">
        {/* Left Zone: Brand and State emblem */}
        <div className="flex items-center gap-3.5 shrink-0">
          <div className="w-13 h-13 sm:w-14 sm:h-14 rounded-2xl overflow-hidden glass-pill flex items-center justify-center shrink-0 p-1 border border-stone-200/50">
            <img
              src={logoImg}
              alt="Government of Jharkhand Emblem"
              className="w-full h-full object-contain hover:scale-105 transition-transform"
            />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base sm:text-lg font-bold tracking-tight text-stone-900 leading-none">
                Jharkhand Civic Admin
              </h1>
            </div>
            <p className="text-xs text-stone-500 font-medium mt-0.5">
              State Overview
            </p>
          </div>
        </div>

        {/* Center Zone: Nav Links */}
        <nav className="bg-white/95 backdrop-blur-md p-1.5 rounded-3xl flex items-center gap-2.5 sm:gap-4 md:gap-5 border border-stone-200/50">
          {/* Dashboard Tab */}
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`inline-flex items-center gap-2 px-4.5 py-2 rounded-xl text-sm font-semibold transition-all whitespace-nowrap ${
              activeTab === 'dashboard'
                ? 'bg-gradient-to-r from-[#ea580c] via-[#f97316] to-[#fb923c] text-white font-bold scale-[1.02]'
                : 'text-stone-700 hover:text-stone-900 hover:bg-white/70 backdrop-blur-sm'
            }`}
          >
            <LayoutGrid className="w-4 h-4 shrink-0" />
            <span>Dashboard</span>
          </button>

          {/* Weekly Report Tab */}
          <button
            onClick={() => setActiveTab('weekly-report')}
            className={`inline-flex items-center gap-2 px-4.5 py-2 rounded-xl text-sm font-semibold transition-all whitespace-nowrap ${
              activeTab === 'weekly-report'
                ? 'bg-gradient-to-r from-[#ea580c] via-[#f97316] to-[#fb923c] text-white font-bold scale-[1.02]'
                : 'text-stone-700 hover:text-stone-900 hover:bg-white/70 backdrop-blur-sm'
            }`}
          >
            <Calendar className="w-4 h-4 shrink-0" />
            <span>Weekly Report</span>
          </button>

          {/* Details Tab */}
          <button
            onClick={() => setActiveTab('details')}
            className={`inline-flex items-center gap-2 px-4.5 py-2 rounded-xl text-sm font-semibold transition-all whitespace-nowrap ${
              activeTab === 'details'
                ? 'bg-gradient-to-r from-[#ea580c] via-[#f97316] to-[#fb923c] text-white font-bold scale-[1.02]'
                : 'text-stone-700 hover:text-stone-900 hover:bg-white/70 backdrop-blur-sm'
            }`}
          >
            <ListOrdered className="w-4 h-4 shrink-0" />
            <span>Details</span>
          </button>

          {/* Notifications Tab */}
          <button
            onClick={() => setActiveTab('notifications')}
            className={`relative inline-flex items-center gap-2 px-4.5 py-2 rounded-xl text-sm font-semibold transition-all whitespace-nowrap ${
              activeTab === 'notifications'
                ? 'bg-gradient-to-r from-[#ea580c] via-[#f97316] to-[#fb923c] text-white font-bold scale-[1.02]'
                : 'text-stone-700 hover:text-stone-900 hover:bg-white/70 backdrop-blur-sm'
            }`}
          >
            <Bell className="w-4 h-4 shrink-0" />
            <span>Notifications</span>
            {unreadNotificationsCount > 0 && (
              <span
                className={`text-[11px] font-bold px-1.5 py-0.2 rounded-full leading-none shrink-0 ${
                  activeTab === 'notifications'
                    ? 'bg-white text-[#ea580c]'
                    : 'bg-[#dc2626] text-white'
                }`}
              >
                {unreadNotificationsCount}
              </span>
            )}
          </button>
        </nav>

        {/* Right Zone: Primary Actions & User Profile */}
        <div className="flex items-center gap-3 shrink-0">
          {/* Language Toggle */}
          <button
            onClick={() => setLanguage(language === 'EN' ? 'HI' : 'EN')}
            className="hidden md:inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-xl glass-pill text-stone-700 hover:bg-white/90 shadow-xs"
            title="Toggle Language (English / Hindi)"
          >
            <Globe className="w-3.5 h-3.5 text-stone-500" />
            <span>{language === 'EN' ? 'हिन्दी' : 'English'}</span>
          </button>

          {/* User Profile dropdown */}
          <div className="relative">
            <button
              onClick={() => setProfileOpen(!profileOpen)}
              className="flex items-center gap-2.5 p-1.5 rounded-2xl glass-pill hover:bg-white/90 transition-all shadow-xs"
            >
              <img
                src={currentUser.avatarUrl}
                alt={currentUser.name}
                className="w-9 h-9 rounded-full object-cover ring-2 ring-white"
              />
              <div className="text-left hidden lg:block pr-1">
                <div className="text-xs font-bold text-stone-900 leading-tight">
                  {currentUser.name}
                </div>
                <div className="text-[11px] text-stone-500 font-medium">
                  {currentUser.role}
                </div>
              </div>
              <ChevronDown className="w-4 h-4 text-stone-400" />
            </button>

            {/* Profile Dropdown Menu */}
            {profileOpen && (
              <div className="absolute right-0 mt-2 w-64 glass-modal rounded-2xl p-2 z-50 animate-in fade-in zoom-in-95 duration-150">
                <div className="px-4 py-2.5 border-b border-stone-200/50">
                  <p className="text-xs font-bold text-stone-900">{currentUser.name}</p>
                  <p className="text-xs text-stone-500">{currentUser.role} &bull; {currentUser.cadre}</p>
                  <span className="inline-flex items-center gap-1 mt-1.5 text-[10px] font-semibold text-emerald-700 bg-emerald-50/80 backdrop-blur-xs px-2 py-0.5 rounded-full border border-emerald-200/50">
                    <CheckCircle2 className="w-3 h-3" /> State Command Authorized
                  </span>
                </div>

                <div className="py-1 text-xs text-stone-700">
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 hover:bg-white/60 rounded-xl transition-colors flex items-center gap-2.5"
                  >
                    <Shield className="w-3.5 h-3.5 text-stone-400" />
                    <span>IAS Executive Dashboard</span>
                  </button>
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 hover:bg-white/60 rounded-xl transition-colors flex items-center gap-2.5"
                  >
                    <User className="w-3.5 h-3.5 text-stone-400" />
                    <span>District Nodal Directory</span>
                  </button>
                </div>

                <div className="pt-1 border-t border-stone-200/50 text-xs">
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 text-red-600 hover:bg-red-50/80 rounded-xl transition-colors flex items-center gap-2.5 font-medium"
                  >
                    <LogOut className="w-3.5 h-3.5 text-red-500" />
                    <span>Switch Session / Sign Out</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
