import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import {
  Article,
  blogApi,
  blogUtils,
} from '@/shared/api/blog-client';
import BlogLayout from '@/widgets/blog-layout/BlogLayout';
import {
  ClockIcon,
  UserIcon,
  CalendarIcon,
  EyeIcon,
  HeartIcon,
  TagIcon,
  HomeIcon,
  ChevronRightIcon
} from '@heroicons/react/24/outline';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface ArticlePageProps {
  params: { slug: string };
}

// Generate dynamic metadata for SEO
export async function generateMetadata({ params }: ArticlePageProps): Promise<Metadata> {
  try {
    const article = await blogApi.getArticleBySlug(params.slug);

    const title = blogUtils.generateSEOTitle(article);
    const description = blogUtils.formatMetaDescription(article);
    const url = `https://stockquest.co.kr/blog/articles/${article.slug}`;
    const imageUrl = article.ogImageUrl || 'https://stockquest.co.kr/images/blog-default-og.jpg';

    return {
      title,
      description,
      keywords: article.seoKeywords || article.tagNames.join(', '),
      authors: [{ name: article.authorNickname }],
      openGraph: {
        title: article.ogTitle || title,
        description: article.ogDescription || description,
        url,
        siteName: 'StockQuest',
        locale: 'ko_KR',
        type: 'article',
        images: [
          {
            url: imageUrl,
            width: 1200,
            height: 630,
            alt: article.title,
          },
        ],
        publishedTime: article.publishedAt,
        modifiedTime: article.updatedAt,
        authors: [article.authorNickname],
        tags: article.tagNames,
      },
      twitter: {
        card: 'summary_large_image',
        title: article.twitterTitle || title,
        description: article.twitterDescription || description,
        images: [article.twitterImageUrl || imageUrl],
      },
      alternates: {
        canonical: article.canonicalUrl || url,
      },
      robots: {
        index: article.indexable,
        follow: article.followable,
        googleBot: {
          index: article.indexable,
          follow: article.followable,
        },
      },
      other: {
        'article:author': article.authorNickname,
        'article:tag': article.tagNames.join(','),
        'article:section': article.categoryName || 'Investment',
        'article:published_time': article.publishedAt || '',
        'article:modified_time': article.updatedAt,
        'reading-time': article.readingTimeMinutes.toString(),
        'difficulty': article.difficultyDisplay,
      },
    };
  } catch (error) {
    // Return default metadata if article not found
    return {
      title: '게시글을 찾을 수 없습니다 | StockQuest',
      description: '요청하신 게시글이 존재하지 않거나 삭제되었습니다.',
    };
  }
}

// Server Component - fetches data at build time/request time
export default async function ArticlePage({ params }: ArticlePageProps) {
  try {
    const article = await blogApi.getArticleBySlug(params.slug);

    // Component implementation with article data
    return <ArticlePageContent article={article} />;
  } catch (error) {
    // Show 404 for not found articles
    notFound();
  }
}

// Client Component for interactive functionality
function ArticlePageContent({ article }: { article: Article }) {

  return (
    <BlogLayout>
      <div className="max-w-4xl mx-auto p-6">
        {/* Breadcrumb Navigation */}
        <nav className="flex items-center space-x-2 text-sm text-gray-500 mb-6">
          <Link href="/" className="hover:text-blue-600 flex items-center">
            <HomeIcon className="w-4 h-4" />
          </Link>
          <ChevronRightIcon className="w-4 h-4" />
          <Link href="/blog" className="hover:text-blue-600">
            블로그
          </Link>
          <ChevronRightIcon className="w-4 h-4" />
          <Link href="/blog/articles" className="hover:text-blue-600">
            글
          </Link>
          {article.categoryName && (
            <>
              <ChevronRightIcon className="w-4 h-4" />
              <span className="text-blue-600">{article.categoryName}</span>
            </>
          )}
        </nav>

        {/* Article Header */}
        <header className="mb-8 pb-8 border-b border-gray-200">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4 leading-tight">
            {article.title}
          </h1>

          <p className="text-lg text-gray-600 mb-6 leading-relaxed">
            {article.summary}
          </p>

          {/* Article Meta Information */}
          <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500 mb-6">
            <div className="flex items-center">
              <UserIcon className="w-4 h-4 mr-1" />
              <span>{article.authorNickname}</span>
            </div>

            {article.publishedAt && (
              <div className="flex items-center">
                <CalendarIcon className="w-4 h-4 mr-1" />
                <span>{new Date(article.publishedAt).toLocaleDateString('ko-KR')}</span>
              </div>
            )}

            <div className="flex items-center">
              <ClockIcon className="w-4 h-4 mr-1" />
              <span>{article.readingTimeDisplay}</span>
            </div>

            <div className="flex items-center">
              <EyeIcon className="w-4 h-4 mr-1" />
              <span>{article.viewCount.toLocaleString()} 조회</span>
            </div>

            <div className="flex items-center">
              <HeartIcon className="w-4 h-4 mr-1" />
              <span>{article.likeCount.toLocaleString()} 좋아요</span>
            </div>
          </div>

          {/* Difficulty Badge */}
          <div className="flex items-center gap-4 mb-6">
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${blogUtils.getDifficultyColor(article.difficulty)}`}>
              {article.difficultyDisplay}
            </span>

            {article.featured && (
              <span className="px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm font-medium">
                ⭐ 추천글
              </span>
            )}
          </div>

          {/* Tags */}
          {article.tags.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {article.tags.map((tag) => (
                <Link
                  key={tag.id}
                  href={blogUtils.getTagUrl(tag.slug)}
                  className={`inline-flex items-center px-2 py-1 rounded-md text-xs font-medium hover:opacity-80 transition-opacity ${blogUtils.getTagTypeColor(tag.type)}`}
                >
                  <TagIcon className="w-3 h-3 mr-1" />
                  {tag.name}
                </Link>
              ))}
            </div>
          )}
        </header>

        {/* Article Content */}
        <main className="prose prose-lg max-w-none">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            components={{
              // Custom components for better styling
              h1: ({children}) => <h1 className="text-3xl font-bold text-gray-900 mt-8 mb-4">{children}</h1>,
              h2: ({children}) => <h2 className="text-2xl font-bold text-gray-900 mt-6 mb-3">{children}</h2>,
              h3: ({children}) => <h3 className="text-xl font-bold text-gray-900 mt-4 mb-2">{children}</h3>,
              p: ({children}) => <p className="text-gray-700 leading-relaxed mb-4">{children}</p>,
              ul: ({children}) => <ul className="list-disc pl-6 mb-4 space-y-1">{children}</ul>,
              ol: ({children}) => <ol className="list-decimal pl-6 mb-4 space-y-1">{children}</ol>,
              li: ({children}) => <li className="text-gray-700">{children}</li>,
              blockquote: ({children}) => (
                <blockquote className="border-l-4 border-blue-500 pl-4 py-2 my-4 bg-blue-50 italic text-gray-700">
                  {children}
                </blockquote>
              ),
              code: ({children}) => (
                <code className="bg-gray-100 px-2 py-1 rounded text-sm font-mono text-gray-800">
                  {children}
                </code>
              ),
              pre: ({children}) => (
                <pre className="bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto mb-4">
                  {children}
                </pre>
              ),
              a: ({href, children}) => (
                <a
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:text-blue-700 underline"
                >
                  {children}
                </a>
              ),
            }}
          >
            {article.content}
          </ReactMarkdown>
        </main>

        {/* Article Footer */}
        <footer className="mt-12 pt-8 border-t border-gray-200">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <p className="text-sm text-gray-500">
                작성자: {article.authorNickname}
              </p>
              {article.publishedAt && (
                <p className="text-sm text-gray-500">
                  발행일: {new Date(article.publishedAt).toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                  })}
                </p>
              )}
            </div>

            <div className="flex gap-3">
              <Link
                href="/blog/articles"
                className="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-200 transition-colors"
              >
                목록으로
              </Link>
              <Link
                href="/blog"
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                블로그 홈
              </Link>
            </div>
          </div>
        </footer>
      </div>
    </BlogLayout>
  );
}