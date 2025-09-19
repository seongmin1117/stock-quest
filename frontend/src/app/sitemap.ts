import { MetadataRoute } from 'next'

// Base URL for the site
const baseUrl = process.env.NEXT_PUBLIC_BASE_URL || 'https://localhost:3000'

// API base URL for backend calls
const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

interface Article {
  slug: string
  publishedAt?: string
  updatedAt?: string
}

interface Category {
  slug: string
  updatedAt?: string
}

async function fetchArticles(): Promise<Article[]> {
  try {
    const response = await fetch(`${apiUrl}/api/v1/content/articles?status=PUBLISHED&limit=1000`, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) {
      console.error('Failed to fetch articles for sitemap')
      return []
    }

    const data = await response.json()
    return data.articles || []
  } catch (error) {
    console.error('Error fetching articles for sitemap:', error)
    return []
  }
}

async function fetchCategories(): Promise<Category[]> {
  try {
    const response = await fetch(`${apiUrl}/api/v1/content/categories`, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) {
      console.error('Failed to fetch categories for sitemap')
      return []
    }

    const data = await response.json()
    return data || []
  } catch (error) {
    console.error('Error fetching categories for sitemap:', error)
    return []
  }
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  // Static routes with their priorities and change frequencies
  const staticRoutes: MetadataRoute.Sitemap = [
    {
      url: baseUrl,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1,
    },
    {
      url: `${baseUrl}/blog`,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 0.9,
    },
    {
      url: `${baseUrl}/challenges`,
      lastModified: new Date(),
      changeFrequency: 'weekly',
      priority: 0.8,
    },
    {
      url: `${baseUrl}/leaderboard`,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 0.7,
    },
    {
      url: `${baseUrl}/dca-simulation`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.8,
    },
    {
      url: `${baseUrl}/community`,
      lastModified: new Date(),
      changeFrequency: 'weekly',
      priority: 0.6,
    },
    {
      url: `${baseUrl}/auth/login`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.3,
    },
    {
      url: `${baseUrl}/auth/register`,
      lastModified: new Date(),
      changeFrequency: 'monthly',
      priority: 0.3,
    },
  ]

  // Fetch dynamic content
  const [articles, categories] = await Promise.all([
    fetchArticles(),
    fetchCategories()
  ])

  // Generate article URLs
  const articleRoutes: MetadataRoute.Sitemap = articles.map((article) => ({
    url: `${baseUrl}/blog/articles/${article.slug}`,
    lastModified: article.updatedAt ? new Date(article.updatedAt) : article.publishedAt ? new Date(article.publishedAt) : new Date(),
    changeFrequency: 'weekly',
    priority: 0.7,
  }))

  // Generate category URLs
  const categoryRoutes: MetadataRoute.Sitemap = categories.map((category) => ({
    url: `${baseUrl}/blog/categories/${category.slug}`,
    lastModified: category.updatedAt ? new Date(category.updatedAt) : new Date(),
    changeFrequency: 'weekly',
    priority: 0.6,
  }))

  // Combine all routes
  return [...staticRoutes, ...articleRoutes, ...categoryRoutes]
}