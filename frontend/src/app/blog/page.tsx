import { Metadata } from 'next';
import BlogLayout from '@/widgets/blog-layout/BlogLayout';
import FeaturedArticles from '@/widgets/content-showcase/FeaturedArticles';
import RecentArticles from '@/widgets/content-showcase/RecentArticles';
import CategoryNavigation from '@/widgets/navigation/CategoryNavigation';

export const metadata: Metadata = {
  title: 'StockQuest 블로그 | 투자 교육 & 투자 도구',
  description: '투자 초보자부터 전문가까지, 체계적인 투자 교육 콘텐츠와 실용적인 투자 계산기 도구를 제공합니다. 주식, 채권, 펀드, ETF 투자 가이드와 무료 시뮬레이션으로 안전하게 투자 실력을 키워보세요.',
  keywords: '투자 교육, 투자 블로그, 주식 투자, 채권 투자, 펀드 투자, ETF, 투자 계산기, DCA 계산기, 복리 계산기, 투자 시뮬레이션',
  openGraph: {
    title: 'StockQuest 블로그 | 투자 교육 & 투자 도구',
    description: '체계적인 투자 교육과 실용적인 도구로 투자 실력을 키워보세요.',
    url: 'https://stockquest.co.kr/blog',
    siteName: 'StockQuest',
    locale: 'ko_KR',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'StockQuest 블로그 | 투자 교육 & 투자 도구',
    description: '체계적인 투자 교육과 실용적인 도구로 투자 실력을 키워보세요.',
  },
  alternates: {
    canonical: 'https://stockquest.co.kr/blog',
  },
};

export default function BlogHomePage() {
  return (
    <BlogLayout>
      <div className="p-6 space-y-8">
        {/* 히어로 섹션 */}
        <section className="text-center py-12 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg">
          <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            투자 교육 & 투자 도구
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
            투자 초보자부터 전문가까지, 체계적인 교육 콘텐츠와 실용적인 계산기 도구로
            <br />
            <span className="text-blue-600 font-semibold">안전하게 투자 실력을 키워보세요</span>
          </p>
          <div className="flex flex-col sm:flex-row justify-center gap-4">
            <a
              href="/blog/articles"
              className="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
            >
              투자 교육 글 보기
            </a>
            <a
              href="/education/calculators"
              className="bg-white text-blue-600 border-2 border-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
            >
              무료 계산기 사용하기
            </a>
          </div>
        </section>

        {/* 카테고리 네비게이션 */}
        <section>
          <CategoryNavigation />
        </section>

        {/* 추천 글 섹션 */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900 flex items-center">
              🌟 추천 글
            </h2>
            <a
              href="/blog/articles?featured=true"
              className="text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              더 보기 →
            </a>
          </div>
          <FeaturedArticles />
        </section>

        {/* 최신 글 섹션 */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900 flex items-center">
              📝 최신 글
            </h2>
            <a
              href="/blog/articles"
              className="text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              더 보기 →
            </a>
          </div>
          <RecentArticles />
        </section>

        {/* 투자 도구 홍보 섹션 */}
        <section className="bg-green-50 border border-green-200 rounded-lg p-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-green-900 mb-4">
              💰 무료 투자 계산기 도구
            </h2>
            <p className="text-green-700 mb-6 max-w-2xl mx-auto">
              복리 효과, DCA 투자, 위험도 분석 등 다양한 계산기로 투자 계획을 수립하고 검증해보세요.
              모든 도구는 완전 무료로 제공됩니다.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-w-4xl mx-auto">
              <a
                href="/education/calculators/compound"
                className="bg-white border border-green-300 rounded-lg p-6 hover:shadow-md transition-shadow group"
              >
                <div className="text-green-600 text-2xl mb-2">📈</div>
                <h3 className="font-semibold text-green-900 mb-2">복리 계산기</h3>
                <p className="text-green-700 text-sm">복리 효과로 자산이 얼마나 증가하는지 계산해보세요.</p>
              </a>
              <a
                href="/education/calculators/dca"
                className="bg-white border border-green-300 rounded-lg p-6 hover:shadow-md transition-shadow group"
              >
                <div className="text-green-600 text-2xl mb-2">📊</div>
                <h3 className="font-semibold text-green-900 mb-2">DCA 계산기</h3>
                <p className="text-green-700 text-sm">정기 적립 투자의 성과를 시뮬레이션해보세요.</p>
              </a>
              <a
                href="/education/calculators/risk"
                className="bg-white border border-green-300 rounded-lg p-6 hover:shadow-md transition-shadow group"
              >
                <div className="text-green-600 text-2xl mb-2">⚖️</div>
                <h3 className="font-semibold text-green-900 mb-2">위험도 계산기</h3>
                <p className="text-green-700 text-sm">포트폴리오의 리스크를 분석하고 최적화하세요.</p>
              </a>
            </div>
          </div>
        </section>

        {/* 투자 시뮬레이션 홍보 섹션 */}
        <section className="bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg p-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold mb-4">
              🎮 실전 투자 시뮬레이션 체험
            </h2>
            <p className="mb-6 max-w-2xl mx-auto opacity-90">
              실제 돈 없이 안전하게 투자를 연습해보세요.
              실시간 시장 데이터로 진짜 같은 투자 경험을 제공합니다.
            </p>
            <div className="flex flex-col sm:flex-row justify-center gap-4">
              <a
                href="/challenges"
                className="bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
              >
                무료 체험하기
              </a>
              <a
                href="/leaderboard"
                className="border-2 border-white text-white px-8 py-3 rounded-lg font-semibold hover:bg-white hover:text-blue-600 transition-colors"
              >
                리더보드 보기
              </a>
            </div>
          </div>
        </section>

        {/* SEO를 위한 추가 정보 */}
        <section className="text-center py-8 border-t border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            왜 StockQuest 블로그인가요?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-4xl mx-auto text-sm text-gray-600">
            <div>
              <div className="text-blue-600 text-xl mb-2">📚</div>
              <h3 className="font-semibold text-gray-900 mb-1">체계적인 교육</h3>
              <p>기초부터 고급까지 단계별 투자 교육 콘텐츠</p>
            </div>
            <div>
              <div className="text-blue-600 text-xl mb-2">🛠️</div>
              <h3 className="font-semibold text-gray-900 mb-1">실용적인 도구</h3>
              <p>실제 투자에 바로 활용할 수 있는 계산기와 분석 도구</p>
            </div>
            <div>
              <div className="text-blue-600 text-xl mb-2">🔒</div>
              <h3 className="font-semibold text-gray-900 mb-1">안전한 연습</h3>
              <p>실제 손실 없이 투자 실력을 키울 수 있는 시뮬레이션</p>
            </div>
            <div>
              <div className="text-blue-600 text-xl mb-2">💯</div>
              <h3 className="font-semibold text-gray-900 mb-1">완전 무료</h3>
              <p>모든 교육 콘텐츠와 도구를 무료로 이용 가능</p>
            </div>
          </div>
        </section>
      </div>
    </BlogLayout>
  );
}