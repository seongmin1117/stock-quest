'use client';

import { ReactNode } from 'react';
import BlogHeader from './BlogHeader';
import BlogSidebar from './BlogSidebar';
import BlogFooter from './BlogFooter';

interface BlogLayoutProps {
  children: ReactNode;
  showSidebar?: boolean;
  sidebarContent?: ReactNode;
  className?: string;
}

export default function BlogLayout({
  children,
  showSidebar = true,
  sidebarContent,
  className = ''
}: BlogLayoutProps) {
  return (
    <div className={`min-h-screen bg-gray-50 ${className}`}>
      {/* Blog Header */}
      <BlogHeader />

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <div className={`grid gap-8 ${showSidebar ? 'lg:grid-cols-3' : 'lg:grid-cols-1'}`}>
          {/* Content Area */}
          <div className={showSidebar ? 'lg:col-span-2' : 'lg:col-span-1'}>
            <div className="bg-white rounded-lg shadow-sm">
              {children}
            </div>
          </div>

          {/* Sidebar - 광고 및 위젯 영역 */}
          {showSidebar && (
            <aside className="lg:col-span-1">
              {sidebarContent || <BlogSidebar />}
            </aside>
          )}
        </div>
      </main>

      {/* Blog Footer */}
      <BlogFooter />
    </div>
  );
}