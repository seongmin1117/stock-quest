/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    // Feature-Sliced Design을 위한 설정
    typedRoutes: true,
  },
  
  // 환경 변수 설정
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
    NEXT_PUBLIC_MOCK_API: process.env.NEXT_PUBLIC_MOCK_API || 'true',
  },
  
  // 개발 환경 최적화
  reactStrictMode: true,
  
  // 이미지 최적화 설정
  images: {
    domains: ['localhost'],
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

module.exports = nextConfig;