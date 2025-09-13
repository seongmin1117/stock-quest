import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import AuthGuard from '@/shared/lib/auth/AuthGuard';
import { Providers } from './providers';
import { Navbar } from '@/shared/components/Navigation';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'StockQuest - 모의 투자 챌린지',
  description: '과거 시장 데이터로 배우는 투자 학습 플랫폼',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <head>
        {/* 웹폰트 preload로 성능 최적화 */}
        <link 
          rel="preconnect" 
          href="https://fonts.googleapis.com" 
        />
        <link 
          rel="preconnect" 
          href="https://fonts.gstatic.com" 
          crossOrigin="anonymous"
        />
        <link
          rel="preload"
          href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
          as="style"
        />
        <noscript>
          <link
            rel="stylesheet"
            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap"
          />
        </noscript>
        {/* 메타 태그 개선 */}
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
        <meta name="theme-color" content="#6366f1" />
        <meta name="description" content="AI가 과거 시장 데이터를 빠르게 재생하여 투자 경험을 쌓고, 전 세계 트레이더들과 경쟁하며 투자 실력을 향상시키는 플랫폼" />
        <meta property="og:title" content="StockQuest - AI 트레이딩 시뮬레이터" />
        <meta property="og:description" content="과거 데이터로 미래 투자 실력을 키우는 AI 트레이딩 플랫폼" />
        <meta property="og:type" content="website" />
      </head>
      <body className={inter.className}>
        <Providers>
          <AuthGuard>
            <Navbar />
            {children}
          </AuthGuard>
        </Providers>
      </body>
    </html>
  );
}

