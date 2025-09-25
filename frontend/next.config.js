const withPWA = require('next-pwa')({
  dest: 'public',
  register: true,
  skipWaiting: true,
  runtimeCaching: [
    {
      urlPattern: /^https?.*$/,
      handler: 'NetworkFirst',
      options: {
        cacheName: 'offlineCache',
        expiration: {
          maxEntries: 200,
          maxAgeSeconds: 86400, // 24 hours
        },
      },
    },
  ],
  buildExcludes: [/middleware-manifest\.json$/],
  disable: process.env.NODE_ENV === 'development',
});

/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    // Feature-Sliced Design을 위한 설정
    typedRoutes: false,
  },

  // 환경 변수 설정
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
    NEXT_PUBLIC_MOCK_API: process.env.NEXT_PUBLIC_MOCK_API || 'true',
  },

  // 개발 환경 최적화
  reactStrictMode: true,

  // TypeScript 빌드 오류 무시 (관대한 설정)
  typescript: {
    ignoreBuildErrors: true,
  },

  // ESLint 빌드 오류 무시 (관대한 설정)
  eslint: {
    ignoreDuringBuilds: true,
  },

  // 이미지 최적화 설정
  images: {
    domains: ['localhost'],
  },

  // PWA 지원을 위한 헤더 설정
  async headers() {
    return [
      {
        source: '/manifest.json',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=31536000, immutable',
          },
        ],
      },
    ];
  },

  // 웹팩 설정 (FSD 구조 지원)
  webpack: (config, { dev, isServer }) => {
    // FSD 구조의 절대 경로 임포트 지원
    config.resolve.alias = {
      ...config.resolve.alias,
      '~': __dirname,
      '@': __dirname + '/src',
    };

    return config;
  },
};

module.exports = withPWA(nextConfig);