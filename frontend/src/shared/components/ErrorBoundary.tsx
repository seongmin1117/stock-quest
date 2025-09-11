'use client';

import React from 'react';
import {
  Box,
  Alert,
  AlertTitle,
  Button,
  Typography,
  Card,
  CardContent,
  Collapse,
} from '@mui/material';
import {
  ErrorOutline as ErrorIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandIcon,
  ExpandLess as CollapseIcon,
  BugReport as BugIcon,
} from '@mui/icons-material';

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: React.ErrorInfo | null;
  showDetails: boolean;
}

interface ErrorBoundaryProps {
  children: React.ReactNode;
  fallback?: React.ComponentType<{ error: Error; retry: () => void }>;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

/**
 * 에러 경계 컴포넌트
 * React 컴포넌트 트리에서 발생하는 JavaScript 오류를 포착하고 폴백 UI를 표시
 */
export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    this.setState({
      error,
      errorInfo,
    });

    // 에러 로깅
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // 외부 에러 핸들러 호출
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // 운영 환경에서는 에러 추적 서비스에 보고
    if (process.env.NODE_ENV === 'production') {
      // 예: Sentry, LogRocket 등의 에러 추적 서비스
      // reportError(error, errorInfo);
    }
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    });
  };

  toggleDetails = () => {
    this.setState(prev => ({
      showDetails: !prev.showDetails,
    }));
  };

  render() {
    if (this.state.hasError) {
      // 커스텀 폴백 컴포넌트가 있으면 사용
      if (this.props.fallback) {
        const FallbackComponent = this.props.fallback;
        return <FallbackComponent error={this.state.error!} retry={this.handleRetry} />;
      }

      // 기본 에러 UI
      return (
        <Card elevation={3} sx={{ maxWidth: 800, mx: 'auto', my: 4 }}>
          <CardContent sx={{ p: 4 }}>
            <Alert 
              severity="error" 
              icon={<ErrorIcon fontSize="large" />}
              sx={{ mb: 3 }}
            >
              <AlertTitle sx={{ fontSize: '1.25rem', fontWeight: 'bold' }}>
                앗! 문제가 발생했습니다
              </AlertTitle>
              <Typography variant="body1" sx={{ mt: 1 }}>
                예상치 못한 오류로 인해 이 컴포넌트를 표시할 수 없습니다.
              </Typography>
            </Alert>

            {/* 에러 메시지 */}
            {this.state.error && (
              <Box mb={3}>
                <Typography variant="h6" color="error" gutterBottom>
                  오류 정보
                </Typography>
                <Typography 
                  variant="body2" 
                  sx={{ 
                    fontFamily: 'monospace',
                    backgroundColor: 'grey.100',
                    p: 2,
                    borderRadius: 1,
                    wordBreak: 'break-word',
                  }}
                >
                  {this.state.error.message}
                </Typography>
              </Box>
            )}

            {/* 액션 버튼들 */}
            <Box display="flex" gap={2} mb={3}>
              <Button
                variant="contained"
                color="primary"
                startIcon={<RefreshIcon />}
                onClick={this.handleRetry}
                size="large"
              >
                다시 시도
              </Button>
              
              <Button
                variant="outlined"
                onClick={() => window.location.reload()}
                size="large"
              >
                페이지 새로고침
              </Button>

              {process.env.NODE_ENV === 'development' && (
                <Button
                  variant="text"
                  startIcon={this.state.showDetails ? <CollapseIcon /> : <ExpandIcon />}
                  onClick={this.toggleDetails}
                  size="large"
                >
                  {this.state.showDetails ? '상세 정보 숨기기' : '상세 정보 보기'}
                </Button>
              )}
            </Box>

            {/* 개발 환경에서만 상세 에러 정보 표시 */}
            {process.env.NODE_ENV === 'development' && (
              <Collapse in={this.state.showDetails}>
                <Alert severity="info" icon={<BugIcon />} sx={{ mt: 2 }}>
                  <AlertTitle>개발자 정보</AlertTitle>
                  <Typography variant="body2" component="div">
                    이 정보는 개발 환경에서만 표시됩니다.
                  </Typography>
                  
                  {this.state.error && (
                    <Box mt={2}>
                      <Typography variant="subtitle2" gutterBottom>
                        에러 스택:
                      </Typography>
                      <Typography 
                        variant="caption" 
                        component="pre"
                        sx={{ 
                          fontFamily: 'monospace',
                          fontSize: '0.75rem',
                          backgroundColor: 'rgba(0, 0, 0, 0.04)',
                          p: 1,
                          borderRadius: 1,
                          overflow: 'auto',
                          maxHeight: 200,
                          whiteSpace: 'pre-wrap',
                        }}
                      >
                        {this.state.error.stack}
                      </Typography>
                    </Box>
                  )}

                  {this.state.errorInfo && (
                    <Box mt={2}>
                      <Typography variant="subtitle2" gutterBottom>
                        컴포넌트 스택:
                      </Typography>
                      <Typography 
                        variant="caption"
                        component="pre"
                        sx={{ 
                          fontFamily: 'monospace',
                          fontSize: '0.75rem',
                          backgroundColor: 'rgba(0, 0, 0, 0.04)',
                          p: 1,
                          borderRadius: 1,
                          overflow: 'auto',
                          maxHeight: 200,
                          whiteSpace: 'pre-wrap',
                        }}
                      >
                        {this.state.errorInfo.componentStack}
                      </Typography>
                    </Box>
                  )}
                </Alert>
              </Collapse>
            )}

            {/* 도움말 정보 */}
            <Typography variant="body2" color="text.secondary" sx={{ mt: 3 }}>
              💡 이 문제가 계속 발생한다면:
            </Typography>
            <Box component="ul" sx={{ mt: 1, pl: 2 }}>
              <Typography component="li" variant="body2" color="text.secondary">
                브라우저를 새로고침해 보세요
              </Typography>
              <Typography component="li" variant="body2" color="text.secondary">
                브라우저 캐시를 지워보세요
              </Typography>
              <Typography component="li" variant="body2" color="text.secondary">
                네트워크 연결을 확인해보세요
              </Typography>
              <Typography component="li" variant="body2" color="text.secondary">
                잠시 후 다시 시도해보세요
              </Typography>
            </Box>
          </CardContent>
        </Card>
      );
    }

    return this.props.children;
  }
}

/**
 * Hook을 사용한 간단한 에러 바운더리 래퍼
 */
export function withErrorBoundary<T extends object>(
  Component: React.ComponentType<T>,
  errorBoundaryProps?: Omit<ErrorBoundaryProps, 'children'>
) {
  const WrappedComponent = (props: T) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  
  return WrappedComponent;
}

/**
 * 에러 경계용 커스텀 폴백 컴포넌트 예시
 */
export const SimpleErrorFallback: React.FC<{ error: Error; retry: () => void }> = ({ 
  error, 
  retry 
}) => (
  <Alert 
    severity="error" 
    action={
      <Button color="inherit" size="small" onClick={retry}>
        다시 시도
      </Button>
    }
  >
    <AlertTitle>오류 발생</AlertTitle>
    {error.message}
  </Alert>
);