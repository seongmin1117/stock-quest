import { MetadataRoute } from 'next'

export default function robots(): MetadataRoute.Robots {
  const baseUrl = process.env.NEXT_PUBLIC_BASE_URL || 'https://localhost:3000'

  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: [
        '/admin/',
        '/auth/',
        '/dashboard/',
        '/profile/',
        '/api/',
        '/debug-company/',
        '/test-company/',
        '/_next/',
        '/private/'
      ],
    },
    sitemap: `${baseUrl}/sitemap.xml`,
  }
}