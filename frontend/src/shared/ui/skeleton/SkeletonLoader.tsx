'use client';

import React from 'react';
import { Box, Skeleton, Card, CardContent } from '@mui/material';
import { useTheme } from '@mui/material/styles';

interface SkeletonLoaderProps {
  variant?: 'text' | 'rectangular' | 'circular';
  width?: number | string;
  height?: number | string;
  animation?: 'pulse' | 'wave' | false;
  sx?: object;
}

/**
 * Enhanced skeleton loader with improved animation and professional styling
 * 개선된 스켈레톤 로더 - 전문적인 애니메이션과 스타일링
 */
export const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({
  variant = 'rectangular',
  width = '100%',
  height = 20,
  animation = 'wave',
  sx = {}
}) => {
  const theme = useTheme();

  return (
    <Skeleton
      variant={variant}
      width={width}
      height={height}
      animation={animation}
      sx={{
        backgroundColor: 'rgba(255, 255, 255, 0.05)',
        '&::after': {
          background: `linear-gradient(
            90deg,
            transparent,
            rgba(255, 255, 255, 0.1),
            transparent
          )`,
        },
        borderRadius: variant === 'rectangular' ? 1 : undefined,
        ...sx
      }}
    />
  );
};

/**
 * Portfolio skeleton component for mobile portfolio loading
 * 모바일 포트폴리오 로딩용 스켈레톤 컴포넌트
 */
export const PortfolioSkeleton: React.FC = () => {
  const theme = useTheme();

  return (
    <Box sx={{ p: 3 }}>
      {/* Header skeleton */}
      <Box sx={{ mb: 3 }}>
        <SkeletonLoader width="60%" height={32} sx={{ mb: 1 }} />
        <SkeletonLoader width="40%" height={24} />
      </Box>

      {/* Portfolio value skeleton */}
      <Card sx={{ mb: 3, backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
        <CardContent>
          <Box sx={{ textAlign: 'center', mb: 2 }}>
            <SkeletonLoader width="70%" height={48} sx={{ mx: 'auto', mb: 1 }} />
            <SkeletonLoader width="50%" height={28} sx={{ mx: 'auto', mb: 1 }} />
            <SkeletonLoader width="30%" height={20} sx={{ mx: 'auto' }} />
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, textAlign: 'center' }}>
            <Box>
              <SkeletonLoader width="80%" height={28} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="60%" height={16} sx={{ mx: 'auto' }} />
            </Box>
            <Box>
              <SkeletonLoader width="80%" height={28} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="60%" height={16} sx={{ mx: 'auto' }} />
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Holdings skeleton */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {[1, 2, 3, 4, 5].map((index) => (
          <Card key={index} sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ flex: 1 }}>
                  <SkeletonLoader width="40%" height={24} sx={{ mb: 1 }} />
                  <SkeletonLoader width="60%" height={16} sx={{ mb: 1 }} />
                  <SkeletonLoader width="50%" height={14} />
                </Box>
                <SkeletonLoader variant="rectangular" width={60} height={30} />
              </Box>

              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 2 }}>
                <Box>
                  <SkeletonLoader width="50%" height={14} sx={{ mb: 1 }} />
                  <SkeletonLoader width="80%" height={20} />
                </Box>
                <Box>
                  <SkeletonLoader width="50%" height={14} sx={{ mb: 1 }} />
                  <SkeletonLoader width="80%" height={20} />
                </Box>
              </Box>

              <Box sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                pt: 2,
                borderTop: '1px solid #2A3441'
              }}>
                <Box>
                  <SkeletonLoader width="60%" height={14} sx={{ mb: 1 }} />
                  <SkeletonLoader width="80%" height={18} />
                </Box>
                <Box sx={{ textAlign: 'right' }}>
                  <SkeletonLoader width="40%" height={14} sx={{ mb: 1 }} />
                  <SkeletonLoader width="60%" height={18} />
                </Box>
              </Box>

              <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                <SkeletonLoader width="50%" height={36} />
                <SkeletonLoader width="50%" height={36} />
              </Box>
            </CardContent>
          </Card>
        ))}
      </Box>
    </Box>
  );
};

/**
 * Challenge list skeleton component
 * 챌린지 목록 스켈레톤 컴포넌트
 */
export const ChallengeSkeleton: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <SkeletonLoader width="50%" height={32} sx={{ mb: 2 }} />
        <SkeletonLoader width="70%" height={20} />
      </Box>

      {/* Challenge cards */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        {[1, 2, 3, 4].map((index) => (
          <Card key={index} sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 2 }}>
                <Box sx={{ flex: 1 }}>
                  <SkeletonLoader width="70%" height={24} sx={{ mb: 1 }} />
                  <SkeletonLoader width="90%" height={16} sx={{ mb: 1 }} />
                  <SkeletonLoader width="80%" height={16} />
                </Box>
                <SkeletonLoader variant="rectangular" width={80} height={24} />
              </Box>

              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 2, mb: 3 }}>
                <Box sx={{ textAlign: 'center' }}>
                  <SkeletonLoader width="60%" height={20} sx={{ mx: 'auto', mb: 1 }} />
                  <SkeletonLoader width="40%" height={14} sx={{ mx: 'auto' }} />
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <SkeletonLoader width="60%" height={20} sx={{ mx: 'auto', mb: 1 }} />
                  <SkeletonLoader width="40%" height={14} sx={{ mx: 'auto' }} />
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <SkeletonLoader width="60%" height={20} sx={{ mx: 'auto', mb: 1 }} />
                  <SkeletonLoader width="40%" height={14} sx={{ mx: 'auto' }} />
                </Box>
              </Box>

              <SkeletonLoader width="100%" height={40} />
            </CardContent>
          </Card>
        ))}
      </Box>
    </Box>
  );
};

/**
 * Leaderboard skeleton component
 * 리더보드 스켈레톤 컴포넌트
 */
export const LeaderboardSkeleton: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <SkeletonLoader width="60%" height={32} sx={{ mb: 2 }} />
        <SkeletonLoader width="80%" height={20} />
      </Box>

      {/* Top 3 podium */}
      <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 2, mb: 4 }}>
        {[1, 2, 3].map((index) => (
          <Card key={index} sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <SkeletonLoader variant="circular" width={40} height={40} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="80%" height={18} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="60%" height={16} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="70%" height={20} sx={{ mx: 'auto' }} />
            </CardContent>
          </Card>
        ))}
      </Box>

      {/* Leaderboard list */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {[4, 5, 6, 7, 8, 9, 10].map((index) => (
          <Box
            key={index}
            sx={{
              display: 'flex',
              alignItems: 'center',
              p: 2,
              backgroundColor: '#1A1F2E',
              borderRadius: 1,
              border: '1px solid #2A3441'
            }}
          >
            <SkeletonLoader width={24} height={20} sx={{ mr: 2 }} />
            <SkeletonLoader variant="circular" width={32} height={32} sx={{ mr: 2 }} />
            <Box sx={{ flex: 1 }}>
              <SkeletonLoader width="60%" height={18} sx={{ mb: 1 }} />
              <SkeletonLoader width="40%" height={14} />
            </Box>
            <Box sx={{ textAlign: 'right' }}>
              <SkeletonLoader width={80} height={18} sx={{ mb: 1 }} />
              <SkeletonLoader width={60} height={14} />
            </Box>
          </Box>
        ))}
      </Box>
    </Box>
  );
};

/**
 * Chart skeleton component for loading charts
 * 차트 로딩용 스켈레톤 컴포넌트
 */
export const ChartSkeleton: React.FC<{ height?: number }> = ({ height = 280 }) => {
  return (
    <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
      <CardContent>
        {/* Chart header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <SkeletonLoader width="30%" height={24} />
          <Box sx={{ textAlign: 'right' }}>
            <SkeletonLoader width={100} height={24} sx={{ mb: 1 }} />
            <SkeletonLoader width={80} height={16} />
          </Box>
        </Box>

        {/* Time range selector */}
        <Box sx={{ display: 'flex', gap: 1, mb: 3 }}>
          {[1, 2, 3, 4, 5].map((index) => (
            <SkeletonLoader key={index} width={40} height={32} />
          ))}
        </Box>

        {/* Chart area */}
        <SkeletonLoader width="100%" height={height} sx={{ mb: 2 }} />

        {/* Chart stats */}
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 2, textAlign: 'center' }}>
          {[1, 2, 3].map((index) => (
            <Box key={index}>
              <SkeletonLoader width="50%" height={14} sx={{ mx: 'auto', mb: 1 }} />
              <SkeletonLoader width="70%" height={18} sx={{ mx: 'auto' }} />
            </Box>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};

/**
 * Trading interface skeleton
 * 트레이딩 인터페이스 스켈레톤
 */
export const TradingSkeleton: React.FC = () => {
  return (
    <Box sx={{ p: 3 }}>
      {/* Stock info */}
      <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441', mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <SkeletonLoader width="40%" height={28} />
            <Box sx={{ textAlign: 'right' }}>
              <SkeletonLoader width={120} height={32} sx={{ mb: 1 }} />
              <SkeletonLoader width={100} height={20} />
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Chart */}
      <ChartSkeleton height={250} />

      {/* Trading form */}
      <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441', mt: 3 }}>
        <CardContent>
          <SkeletonLoader width="30%" height={24} sx={{ mb: 3 }} />

          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 3 }}>
            <SkeletonLoader width="100%" height={56} />
            <SkeletonLoader width="100%" height={56} />
          </Box>

          <SkeletonLoader width="100%" height={56} sx={{ mb: 3 }} />

          <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
            <SkeletonLoader width="100%" height={48} />
            <SkeletonLoader width="100%" height={48} />
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};