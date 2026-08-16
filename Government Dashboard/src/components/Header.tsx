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
    <header className="sticky top-0 z-40 bg-white border-b border-stone-200/90 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Left Zone: Brand and State emblem */}
        <div className="flex items-center gap-3 shrink-0">
          <div className="w-10 h-10 rounded-lg overflow-hidden bg-white border border-stone-200 flex items-center justify-center shadow-xs shrink-0 p-0.5">
            <img
              src={logoImg}
              alt="Government of Jharkhand Emblem"
              className="w-full h-full object-contain"
            />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold tracking-tight text-stone-900 leading-none">
                Jharkhand Civic Admin
              </h1>
            </div>
            <p className="text-xs text-stone-500 font-medium mt-0.5">
              State Overview
            </p>
          </div>
        </div>

        {/* Center Zone: Nav Links */}
        <nav className="flex items-center gap-1.5 sm:gap-2">
          {/* Dashboard Tab */}
          <button
            onClick={() => setActiveTab('dashboard')}
            className={`inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap ${
              activeTab === 'dashboard'
                ? 'bg-[#ea580c] text-white shadow-xs'
                : 'text-stone-600 hover:text-stone-900 hover:bg-stone-100'
            }`}
          >
            <LayoutGrid className="w-4 h-4 shrink-0" />
            <span>Dashboard</span>
          </button>

          {/* Weekly Report Tab */}
          <button
            onClick={() => setActiveTab('weekly-report')}
            className={`inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap ${
              activeTab === 'weekly-report'
                ? 'bg-[#ea580c] text-white shadow-xs'
                : 'text-stone-600 hover:text-stone-900 hover:bg-stone-100'
            }`}
          >
            <Calendar className="w-4 h-4 shrink-0" />
            <span>Weekly Report</span>
          </button>

          {/* Details Tab */}
          <button
            onClick={() => setActiveTab('details')}
            className={`inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap ${
              activeTab === 'details'
                ? 'bg-[#ea580c] text-white shadow-xs'
                : 'text-stone-600 hover:text-stone-900 hover:bg-stone-100'
            }`}
          >
            <ListOrdered className="w-4 h-4 shrink-0" />
            <span>Details</span>
          </button>

          {/* Notifications Tab */}
          <button
            onClick={() => setActiveTab('notifications')}
            className={`relative inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap ${
              activeTab === 'notifications'
                ? 'bg-[#ea580c] text-white shadow-xs'
                : 'text-stone-600 hover:text-stone-900 hover:bg-stone-100'
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
            className="hidden md:inline-flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1.5 rounded-md border border-stone-200 text-stone-600 hover:bg-stone-50"
            title="Toggle Language (English / Hindi)"
          >
            <Globe className="w-3.5 h-3.5" />
            <span>{language === 'EN' ? 'हिन्दी' : 'English'}</span>
          </button>

          {/* User Profile dropdown */}
          <div className="relative">
            <button
              onClick={() => setProfileOpen(!profileOpen)}
              className="flex items-center gap-2.5 p-1 rounded-lg hover:bg-stone-100 transition-colors"
            >
              <img
                src={currentUser.avatarUrl}
                alt={currentUser.name}
                className="w-9 h-9 rounded-full object-cover ring-1 ring-stone-300"
              />
              <div className="text-left hidden lg:block">
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
              <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-xl border border-stone-200 py-2 z-50 animate-in fade-in slide-in-from-top-2">
                <div className="px-4 py-2 border-b border-stone-100">
                  <p className="text-xs font-bold text-stone-900">{currentUser.name}</p>
                  <p className="text-xs text-stone-500">{currentUser.role} &bull; {currentUser.cadre}</p>
                  <span className="inline-flex items-center gap-1 mt-1 text-[10px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                    <CheckCircle2 className="w-3 h-3" /> State Command Authorized
                  </span>
                </div>

                <div className="py-1 text-xs text-stone-700">
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 hover:bg-stone-50 flex items-center gap-2.5"
                  >
                    <Shield className="w-3.5 h-3.5 text-stone-400" />
                    <span>IAS Executive Dashboard</span>
                  </button>
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 hover:bg-stone-50 flex items-center gap-2.5"
                  >
                    <User className="w-3.5 h-3.5 text-stone-400" />
                    <span>District Nodal Directory</span>
                  </button>
                </div>

                <div className="pt-1 border-t border-stone-100 text-xs">
                  <button
                    onClick={() => setProfileOpen(false)}
                    className="w-full text-left px-4 py-2 text-red-600 hover:bg-red-50 flex items-center gap-2.5"
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
