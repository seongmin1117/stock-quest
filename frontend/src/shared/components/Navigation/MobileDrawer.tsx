'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

interface MobileDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * Mobile-optimized navigation drawer with touch-friendly design
 * 모바일 친화적인 navigation drawer 구현
 */
export const MobileDrawer: React.FC<MobileDrawerProps> = ({ isOpen, onClose }) => {
  const pathname = usePathname();
  
  const navigationItems = [
    { href: '/dashboard', label: '대시보드', icon: '📊' },
    { href: '/challenges', label: '챌린지', icon: '🎯' },
    { href: '/leaderboard', label: '리더보드', icon: '🏆' },
    { href: '/portfolio', label: '포트폴리오', icon: '💼' },
    { href: '/community', label: '커뮤니티', icon: '👥' },
    { href: '/profile', label: '프로필', icon: '👤' },
  ];

  return (
    <>
      {/* Backdrop overlay */}
      <div
        className={`fixed inset-0 bg-black transition-opacity duration-300 z-40 ${
          isOpen ? 'opacity-50' : 'opacity-0 pointer-events-none'
        }`}
        onClick={onClose}
      />

      {/* Drawer content */}
      <div
        className={`fixed top-0 left-0 h-full w-80 max-w-[85vw] bg-gradient-to-b from-slate-900 to-slate-800 shadow-2xl transform transition-transform duration-300 ease-in-out z-50 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-slate-700">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">SQ</span>
            </div>
            <h2 className="text-xl font-bold text-white">StockQuest</h2>
          </div>
          
          {/* Close button with proper touch target */}
          <button
            onClick={onClose}
            className="w-10 h-10 flex items-center justify-center rounded-full bg-slate-700 hover:bg-slate-600 transition-colors"
            aria-label="메뉴 닫기"
          >
            <span className="text-white text-lg">×</span>
          </button>
        </div>

        {/* Navigation items */}
        <nav className="flex-1 py-4">
          <ul className="space-y-1 px-3">
            {navigationItems.map((item) => {
              const isActive = pathname === item.href;
              
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    onClick={onClose}
                    className={`flex items-center space-x-4 px-4 py-3 rounded-lg transition-all duration-200 text-base font-medium ${
                      isActive
                        ? 'bg-blue-500 text-white shadow-lg'
                        : 'text-gray-300 hover:bg-slate-700 hover:text-white'
                    }`}
                  >
                    <span className="text-xl" role="img" aria-hidden="true">
                      {item.icon}
                    </span>
                    <span>{item.label}</span>
                    
                    {/* Active indicator */}
                    {isActive && (
                      <div className="ml-auto w-2 h-2 bg-white rounded-full" />
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Footer with user info */}
        <div className="p-4 border-t border-slate-700">
          <div className="flex items-center space-x-3 p-3 rounded-lg bg-slate-800">
            <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full flex items-center justify-center">
              <span className="text-white text-xs font-bold">U</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">
                사용자
              </p>
              <p className="text-xs text-gray-400">
                Premium Member
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

/**
 * Mobile hamburger menu button
 * 모바일 햄버거 메뉴 버튼
 */
export const MobileMenuButton: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  return (
    <button
      onClick={onClick}
      className="lg:hidden w-10 h-10 flex items-center justify-center rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
      aria-label="메뉴 열기"
    >
      <div className="w-5 h-5 flex flex-col justify-center space-y-1">
        <div className="w-full h-0.5 bg-white rounded"></div>
        <div className="w-full h-0.5 bg-white rounded"></div>
        <div className="w-full h-0.5 bg-white rounded"></div>
      </div>
    </button>
  );
};