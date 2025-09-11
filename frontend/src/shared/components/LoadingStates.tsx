'use client';

import React from 'react';
import {
  Box,
  CircularProgress,
  Skeleton,
  Typography,
  Card,
  CardContent,
  LinearProgress,
  Fade,
  Backdrop,
} from '@mui/material';
import { SxProps, Theme } from '@mui/material/styles';

interface LoadingStateProps {
  loading?: boolean;
  children?: React.ReactNode;
  sx?: SxProps<Theme>;
}

interface SkeletonLoadingProps {
  type: 'card' | 'table' | 'list' | 'chart' | 'text' | 'avatar';
  count?: number;
  height?: number | string;
  sx?: SxProps<Theme>;
}

/**
 * 원형 로딩 인디케이터
 */
export const CircularLoading: React.FC<LoadingStateProps & { 
  size?: number;
  message?: string;
  color?: 'primary' | 'secondary' | 'inherit';
}> = ({ 
  loading = true, 
  children, 
  size = 40, 
  message,
  color = 'primary',
  sx 
}) => {
  if (!loading && children) {
    return <>{children}</>;
  }

  return (
    <Box 
      display="flex" 
      flexDirection="column" 
      alignItems="center" 
      justifyContent="center"
      gap={2}
      p={4}
      sx={sx}
    >
      <CircularProgress size={size} color={color} />
      {message && (
        <Typography variant="body2" color="text.secondary" textAlign="center">
          {message}
        </Typography>
      )}
    </Box>
  );
};

/**
 * 전체 화면 백드롭 로딩
 */
export const FullScreenLoading: React.FC<{
  open: boolean;
  message?: string;
}> = ({ open, message = "로딩 중..." }) => (
  <Backdrop
    sx={{ 
      color: '#fff', 
      zIndex: (theme) => theme.zIndex.drawer + 1,
      flexDirection: 'column',
      gap: 2,
    }}
    open={open}
  >
    <CircularProgress color="inherit" size={60} />
    <Typography variant="h6" color="inherit">
      {message}
    </Typography>
  </Backdrop>
);

/**
 * 인라인 로딩 바
 */
export const InlineLoading: React.FC<LoadingStateProps & {
  message?: string;
  variant?: 'determinate' | 'indeterminate' | 'buffer' | 'query';
  value?: number;
}> = ({ 
  loading = true, 
  children, 
  message,
  variant = 'indeterminate',
  value,
  sx 
}) => {
  if (!loading && children) {
    return <>{children}</>;
  }

  return (
    <Box sx={{ width: '100%', ...sx }}>
      <LinearProgress 
        variant={variant} 
        value={value}
        sx={{ height: 6, borderRadius: 3, mb: message ? 1 : 0 }}
      />
      {message && (
        <Typography variant="caption" color="text.secondary">
          {message}
        </Typography>
      )}
    </Box>
  );
};

/**
 * 페이드 인/아웃 로딩
 */
export const FadeLoading: React.FC<LoadingStateProps & {
  message?: string;
  timeout?: number;
}> = ({ 
  loading = true, 
  children, 
  message = "데이터를 불러오는 중...",
  timeout = 300,
  sx 
}) => (
  <Fade in={loading} timeout={timeout}>
    <Box sx={sx}>
      {loading ? (
        <Box display="flex" alignItems="center" gap={2} p={2}>
          <CircularProgress size={24} />
          <Typography variant="body2" color="text.secondary">
            {message}
          </Typography>
        </Box>
      ) : (
        children
      )}
    </Box>
  </Fade>
);

/**
 * 스켈레톤 로딩
 */
export const SkeletonLoading: React.FC<SkeletonLoadingProps> = ({
  type,
  count = 1,
  height = 40,
  sx,
}) => {
  const renderSkeleton = () => {
    switch (type) {
      case 'card':
        return (
          <Card sx={{ ...sx }}>
            <CardContent>
              <Skeleton variant="text" width="60%" height={24} sx={{ mb: 1 }} />
              <Skeleton variant="text" width="80%" height={20} sx={{ mb: 2 }} />
              <Skeleton variant="rectangular" width="100%" height={120} />
            </CardContent>
          </Card>
        );

      case 'table':
        return (
          <Box sx={sx}>
            {/* Table header */}
            <Box display="flex" gap={2} mb={1}>
              {[...Array(4)].map((_, i) => (
                <Skeleton key={i} variant="text" width="20%" height={32} />
              ))}
            </Box>
            {/* Table rows */}
            {[...Array(count)].map((_, i) => (
              <Box key={i} display="flex" gap={2} mb={1}>
                {[...Array(4)].map((_, j) => (
                  <Skeleton key={j} variant="text" width="20%" height={24} />
                ))}
              </Box>
            ))}
          </Box>
        );

      case 'list':
        return (
          <Box sx={sx}>
            {[...Array(count)].map((_, i) => (
              <Box key={i} display="flex" alignItems="center" gap={2} mb={2}>
                <Skeleton variant="circular" width={40} height={40} />
                <Box flex={1}>
                  <Skeleton variant="text" width="60%" height={20} />
                  <Skeleton variant="text" width="40%" height={16} />
                </Box>
              </Box>
            ))}
          </Box>
        );

      case 'chart':
        return (
          <Box sx={sx}>
            <Skeleton variant="text" width="40%" height={32} sx={{ mb: 2 }} />
            <Skeleton 
              variant="rectangular" 
              width="100%" 
              height={typeof height === 'number' ? height : 200} 
            />
          </Box>
        );

      case 'text':
        return (
          <Box sx={sx}>
            {[...Array(count)].map((_, i) => (
              <Skeleton 
                key={i} 
                variant="text" 
                height={height}
                width={`${Math.random() * 40 + 60}%`}
                sx={{ mb: 0.5 }}
              />
            ))}
          </Box>
        );

      case 'avatar':
        return (
          <Box display="flex" alignItems="center" gap={2} sx={sx}>
            <Skeleton variant="circular" width={48} height={48} />
            <Box>
              <Skeleton variant="text" width={120} height={24} />
              <Skeleton variant="text" width={80} height={20} />
            </Box>
          </Box>
        );

      default:
        return (
          <Skeleton 
            variant="rectangular" 
            width="100%" 
            height={height}
            sx={sx}
          />
        );
    }
  };

  return <>{renderSkeleton()}</>;
};

/**
 * 조건부 로딩 래퍼
 */
export const ConditionalLoading: React.FC<{
  loading: boolean;
  fallback?: React.ReactNode;
  children: React.ReactNode;
}> = ({ loading, fallback, children }) => {
  if (loading) {
    return fallback ? <>{fallback}</> : <CircularLoading />;
  }
  
  return <>{children}</>;
};

/**
 * 페이지 레벨 로딩
 */
export const PageLoading: React.FC<{
  message?: string;
}> = ({ message = "페이지를 불러오는 중..." }) => (
  <Box
    display="flex"
    flexDirection="column"
    alignItems="center"
    justifyContent="center"
    minHeight="50vh"
    gap={3}
  >
    <CircularProgress size={60} />
    <Typography variant="h6" color="text.secondary">
      {message}
    </Typography>
  </Box>
);

/**
 * 데이터 테이블 로딩
 */
export const TableLoading: React.FC<{
  rows?: number;
  columns?: number;
}> = ({ rows = 5, columns = 4 }) => (
  <Box>
    {/* Header */}
    <Box display="flex" gap={2} mb={2} p={1}>
      {[...Array(columns)].map((_, i) => (
        <Skeleton key={i} variant="text" width={`${100 / columns}%`} height={32} />
      ))}
    </Box>
    
    {/* Rows */}
    {[...Array(rows)].map((_, i) => (
      <Box key={i} display="flex" gap={2} mb={1} p={1}>
        {[...Array(columns)].map((_, j) => (
          <Skeleton key={j} variant="text" width={`${100 / columns}%`} height={24} />
        ))}
      </Box>
    ))}
  </Box>
);

/**
 * 차트 로딩
 */
export const ChartLoading: React.FC<{
  height?: number;
  title?: boolean;
}> = ({ height = 300, title = true }) => (
  <Box>
    {title && (
      <Skeleton variant="text" width="40%" height={32} sx={{ mb: 2 }} />
    )}
    <Skeleton variant="rectangular" width="100%" height={height} />
  </Box>
);

/**
 * 카드 그리드 로딩
 */
export const CardGridLoading: React.FC<{
  count?: number;
  columns?: number;
}> = ({ count = 6, columns = 3 }) => (
  <Box display="grid" gridTemplateColumns={`repeat(${columns}, 1fr)`} gap={2}>
    {[...Array(count)].map((_, i) => (
      <Card key={i}>
        <CardContent>
          <Skeleton variant="text" width="60%" height={24} sx={{ mb: 1 }} />
          <Skeleton variant="text" width="80%" height={20} sx={{ mb: 2 }} />
          <Skeleton variant="rectangular" width="100%" height={80} />
        </CardContent>
      </Card>
    ))}
  </Box>
);