'use client';

import Link from 'next/link';

export default function BlogFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-900 text-white">
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* 브랜드 정보 */}
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">SQ</span>
              </div>
              <div>
                <h3 className="text-lg font-bold">StockQuest</h3>
                <p className="text-gray-400 text-sm">투자 교육 플랫폼</p>
              </div>
            </div>
            <p className="text-gray-300 text-sm">
              안전한 투자 시뮬레이션과 전문적인 교육 콘텐츠로
              투자 실력을 키워나가세요.
            </p>
            <div className="flex space-x-4">
              <a href="#" className="text-gray-400 hover:text-white transition-colors">
                <span className="sr-only">Facebook</span>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M20 10C20 4.477 15.523 0 10 0S0 4.477 0 10c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V10h2.54V7.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V10h2.773l-.443 2.89h-2.33v6.988C16.343 19.128 20 14.991 20 10z" clipRule="evenodd" />
                </svg>
              </a>
              <a href="#" className="text-gray-400 hover:text-white transition-colors">
                <span className="sr-only">YouTube</span>
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" clipRule="evenodd" />
                  <path fillRule="evenodd" d="M12.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-2.293-2.293a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </a>
            </div>
          </div>

          {/* 블로그 카테고리 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">투자 교육</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link href="/blog/categories/investment-basics" className="text-gray-300 hover:text-white transition-colors">
                  투자 기초
                </Link>
              </li>
              <li>
                <Link href="/blog/categories/stock-investment" className="text-gray-300 hover:text-white transition-colors">
                  주식 투자
                </Link>
              </li>
              <li>
                <Link href="/blog/categories/bond-investment" className="text-gray-300 hover:text-white transition-colors">
                  채권 투자
                </Link>
              </li>
              <li>
                <Link href="/blog/categories/fund-etf" className="text-gray-300 hover:text-white transition-colors">
                  펀드/ETF
                </Link>
              </li>
              <li>
                <Link href="/blog/categories/cryptocurrency" className="text-gray-300 hover:text-white transition-colors">
                  암호화폐
                </Link>
              </li>
            </ul>
          </div>

          {/* 투자 도구 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">투자 도구</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link href="/education/calculators/compound" className="text-gray-300 hover:text-white transition-colors">
                  복리 계산기
                </Link>
              </li>
              <li>
                <Link href="/education/calculators/dca" className="text-gray-300 hover:text-white transition-colors">
                  DCA 계산기
                </Link>
              </li>
              <li>
                <Link href="/education/calculators/risk" className="text-gray-300 hover:text-white transition-colors">
                  위험도 계산기
                </Link>
              </li>
              <li>
                <Link href="/challenges" className="text-gray-300 hover:text-white transition-colors">
                  투자 시뮬레이션
                </Link>
              </li>
              <li>
                <Link href="/education/guides" className="text-gray-300 hover:text-white transition-colors">
                  투자 가이드
                </Link>
              </li>
            </ul>
          </div>

          {/* 플랫폼 정보 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">StockQuest</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link href="/about" className="text-gray-300 hover:text-white transition-colors">
                  서비스 소개
                </Link>
              </li>
              <li>
                <Link href="/pricing" className="text-gray-300 hover:text-white transition-colors">
                  요금제
                </Link>
              </li>
              <li>
                <Link href="/contact" className="text-gray-300 hover:text-white transition-colors">
                  문의하기
                </Link>
              </li>
              <li>
                <Link href="/help" className="text-gray-300 hover:text-white transition-colors">
                  도움말
                </Link>
              </li>
              <li>
                <Link href="/api" className="text-gray-300 hover:text-white transition-colors">
                  API 문서
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* 하단 정보 */}
        <div className="border-t border-gray-800 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-sm text-gray-400">
              <p>© {currentYear} StockQuest. All rights reserved.</p>
              <p className="mt-1">
                투자에는 원금 손실 위험이 있습니다. 투자 결정은 신중하게 하시기 바랍니다.
              </p>
            </div>
            <div className="flex space-x-6 text-sm">
              <Link href="/privacy" className="text-gray-400 hover:text-white transition-colors">
                개인정보처리방침
              </Link>
              <Link href="/terms" className="text-gray-400 hover:text-white transition-colors">
                이용약관
              </Link>
              <Link href="/disclaimer" className="text-gray-400 hover:text-white transition-colors">
                면책사항
              </Link>
            </div>
          </div>
        </div>

        {/* 금융투자업 관련 면책사항 */}
        <div className="mt-6 p-4 bg-gray-800 rounded-lg">
          <p className="text-xs text-gray-400 leading-relaxed">
            <strong className="text-gray-300">투자 유의사항:</strong>
            본 서비스는 교육 목적의 투자 시뮬레이션 플랫폼입니다.
            실제 투자 상품 거래는 제공하지 않으며, 모든 콘텐츠는 정보 제공 목적으로만 사용되어야 합니다.
            투자 결정에 대한 모든 책임은 투자자 본인에게 있습니다.
            과거 성과는 미래 수익을 보장하지 않습니다.
          </p>
        </div>
      </div>
    </footer>
  );
}