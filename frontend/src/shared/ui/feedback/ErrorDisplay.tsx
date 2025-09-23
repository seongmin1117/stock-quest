'use client';

import React, { useState, useEffect } from 'react';
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Snackbar,
  Paper,
  Typography,
  IconButton,
  Collapse,
  LinearProgress,
  useTheme,
  alpha,
  Fade
} from '@mui/material';
import {
  Error,
  Warning,
  Info,
  CheckCircle,
  Refresh,
  Close,
  ExpandMore,
  ExpandLess,
  NetworkCheck,
  WifiOff
} from '@mui/icons-material';

type ErrorSeverity = 'error' | 'warning' | 'info' | 'success';

interface ErrorInfo {
  message: string;
  details?: string;
  severity: ErrorSeverity;
  code?: string;
  timestamp?: Date;
  action?: {
    label: string;
    handler: () => void | Promise<void>;
  };
  retry?: {
    enabled: boolean;
    maxAttempts?: number;
    currentAttempt?: number;
    handler?: () => void | Promise<void>;
  };
}

interface ErrorDisplayProps {
  error: ErrorInfo;
  onDismiss?: () => void;
  onRetry?: () => void | Promise<void>;
  autoHide?: boolean;
  autoHideDuration?: number;
  showTimestamp?: boolean;
  showDetails?: boolean;
  elevation?: number;
}

/**
 * Contextual error display with retry mechanisms and smart recovery
 * 재시도 메커니즘과 스마트 복구가 있는 맥락적 오류 표시
 */
export const ErrorDisplay: React.FC<ErrorDisplayProps> = ({
  error,
  onDismiss,
  onRetry,
  autoHide = false,
  autoHideDuration = 6000,
  showTimestamp = true,
  showDetails = true,
  elevation = 2
}) => {
  const theme = useTheme();
  const [isExpanded, setIsExpanded] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const [isRetrying, setIsRetrying] = useState(false);

  const maxRetries = error.retry?.maxAttempts || 3;
  const canRetry = error.retry?.enabled && retryCount < maxRetries;

  const getIcon = () => {
    switch (error.severity) {
      case 'error':
        return <Error />;
      case 'warning':
        return <Warning />;
      case 'info':
        return <Info />;
      case 'success':
        return <CheckCircle />;
      default:
        return <Error />;
    }
  };

  const getErrorContext = () => {
    if (error.code?.includes('NETWORK') || error.message.includes('network')) {
      return {
        icon: <WifiOff />,
        title: '네트워크 연결 오류',
        suggestion: '인터넷 연결을 확인하고 다시 시도해주세요.'
      };
    }

    if (error.code?.includes('AUTH') || error.message.includes('권한')) {
      return {
        icon: <Error />,
        title: '인증 오류',
        suggestion: '로그인 상태를 확인하고 다시 시도해주세요.'
      };
    }

    if (error.code?.includes('SERVER') || error.message.includes('서버')) {
      return {
        icon: <Error />,
        title: '서버 오류',
        suggestion: '잠시 후 다시 시도해주세요. 문제가 지속되면 고객센터에 문의해주세요.'
      };
    }

    return {
      icon: getIcon(),
      title: error.severity === 'error' ? '오류 발생' : '알림',
      suggestion: ''
    };
  };

  const context = getErrorContext();

  const handleRetry = async () => {
    if (!canRetry || isRetrying) return;

    setIsRetrying(true);
    setRetryCount(prev => prev + 1);

    try {
      if (error.retry?.handler) {
        await error.retry.handler();
      } else if (onRetry) {
        await onRetry();
      }
    } catch (retryError) {
      console.error('Retry failed:', retryError);
    } finally {
      setIsRetrying(false);
    }
  };

  const formatTimestamp = (date: Date) => {
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  return (
    <Fade in timeout={300}>
      <Paper
        elevation={elevation}
        sx={{
          backgroundColor: alpha(theme.palette[error.severity].main, 0.1),
          border: `1px solid ${alpha(theme.palette[error.severity].main, 0.3)}`,
          borderRadius: 2,
          overflow: 'hidden',
          mb: 2
        }}
      >
        <Alert
          severity={error.severity}
          icon={context.icon}
          sx={{
            backgroundColor: 'transparent',
            border: 'none',
            '& .MuiAlert-icon': {
              alignItems: 'flex-start',
              pt: 0.5
            }
          }}
          action={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {error.details && showDetails && (
                <IconButton
                  size="small"
                  onClick={() => setIsExpanded(!isExpanded)}
                  sx={{ color: 'inherit' }}
                >
                  {isExpanded ? <ExpandLess /> : <ExpandMore />}
                </IconButton>
              )}
              {onDismiss && (
                <IconButton
                  size="small"
                  onClick={onDismiss}
                  sx={{ color: 'inherit' }}
                >
                  <Close />
                </IconButton>
              )}
            </Box>
          }
        >
          <AlertTitle sx={{ fontWeight: 600 }}>
            {context.title}
            {error.code && (
              <Typography
                component="span"
                variant="caption"
                sx={{
                  ml: 1,
                  px: 1,
                  py: 0.25,
                  backgroundColor: alpha(theme.palette[error.severity].main, 0.2),
                  borderRadius: 1,
                  fontFamily: 'monospace'
                }}
              >
                {error.code}
              </Typography>
            )}
          </AlertTitle>

          <Typography variant="body2" sx={{ mb: context.suggestion ? 1 : 0 }}>
            {error.message}
          </Typography>

          {context.suggestion && (
            <Typography variant="body2" sx={{ fontStyle: 'italic', opacity: 0.8 }}>
              {context.suggestion}
            </Typography>
          )}

          {showTimestamp && error.timestamp && (
            <Typography variant="caption" sx={{ display: 'block', mt: 1, opacity: 0.7 }}>
              {formatTimestamp(error.timestamp)}
            </Typography>
          )}

          {/* Action buttons */}
          <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
            {canRetry && (
              <Button
                size="small"
                variant="outlined"
                startIcon={isRetrying ? undefined : <Refresh />}
                onClick={handleRetry}
                disabled={isRetrying}
                sx={{
                  borderColor: theme.palette[error.severity].main,
                  color: theme.palette[error.severity].main,
                  '&:hover': {
                    borderColor: theme.palette[error.severity].dark,
                    backgroundColor: alpha(theme.palette[error.severity].main, 0.08)
                  }
                }}
              >
                {isRetrying ? (
                  <>
                    <Box
                      component="span"
                      sx={{
                        width: 16,
                        height: 16,
                        mr: 1,
                        border: `2px solid ${theme.palette[error.severity].main}`,
                        borderTopColor: 'transparent',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        '@keyframes spin': {
                          '0%': { transform: 'rotate(0deg)' },
                          '100%': { transform: 'rotate(360deg)' }
                        }
                      }}
                    />
                    재시도 중...
                  </>
                ) : (
                  `재시도 (${maxRetries - retryCount}/${maxRetries})`
                )}
              </Button>
            )}

            {error.action && (
              <Button
                size="small"
                variant="contained"
                onClick={error.action.handler}
                sx={{
                  backgroundColor: theme.palette[error.severity].main,
                  '&:hover': {
                    backgroundColor: theme.palette[error.severity].dark
                  }
                }}
              >
                {error.action.label}
              </Button>
            )}
          </Box>

          {/* Retry progress */}
          {isRetrying && (
            <Box sx={{ mt: 2 }}>
              <LinearProgress
                sx={{
                  backgroundColor: alpha(theme.palette[error.severity].main, 0.2),
                  '& .MuiLinearProgress-bar': {
                    backgroundColor: theme.palette[error.severity].main
                  }
                }}
              />
            </Box>
          )}
        </Alert>

        {/* Expandable details */}
        <Collapse in={isExpanded}>
          <Box
            sx={{
              p: 2,
              backgroundColor: alpha(theme.palette[error.severity].main, 0.05),
              borderTop: `1px solid ${alpha(theme.palette[error.severity].main, 0.2)}`
            }}
          >
            <Typography variant="body2" sx={{ fontFamily: 'monospace', whiteSpace: 'pre-wrap' }}>
              {error.details}
            </Typography>
          </Box>
        </Collapse>
      </Paper>
    </Fade>
  );
};

/**
 * Toast notification for temporary errors
 * 임시 오류용 토스트 알림
 */
export const ErrorToast: React.FC<{
  error: ErrorInfo | null;
  onClose: () => void;
  autoHideDuration?: number;
}> = ({
  error,
  onClose,
  autoHideDuration = 6000
}) => {
  return (
    <Snackbar
      open={!!error}
      autoHideDuration={autoHideDuration}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      {error ? (
        <Alert
          onClose={onClose}
          severity={error.severity}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {error.message}
        </Alert>
      ) : undefined}
    </Snackbar>
  );
};

/**
 * Network status error component
 * 네트워크 상태 오류 컴포넌트
 */
export const NetworkError: React.FC<{
  isOnline: boolean;
  onRetry?: () => void;
}> = ({ isOnline, onRetry }) => {
  const [showOfflineMessage, setShowOfflineMessage] = useState(false);

  useEffect(() => {
    if (!isOnline) {
      setShowOfflineMessage(true);
    } else {
      const timer = setTimeout(() => setShowOfflineMessage(false), 1000);
      return () => clearTimeout(timer);
    }
  }, [isOnline]);

  if (!showOfflineMessage) return null;

  return (
    <Box
      sx={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 9999,
        backgroundColor: isOnline ? '#4CAF50' : '#F44336',
        color: 'white',
        padding: 1,
        textAlign: 'center',
        transition: 'all 0.3s ease-in-out'
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
        {isOnline ? <NetworkCheck /> : <WifiOff />}
        <Typography variant="body2">
          {isOnline ? '네트워크 연결이 복구되었습니다' : '네트워크 연결이 끊어졌습니다'}
        </Typography>
        {!isOnline && onRetry && (
          <Button
            size="small"
            variant="outlined"
            onClick={onRetry}
            sx={{
              color: 'white',
              borderColor: 'white',
              '&:hover': {
                backgroundColor: 'rgba(255, 255, 255, 0.1)'
              }
            }}
          >
            재시도
          </Button>
        )}
      </Box>
    </Box>
  );
};

/**
 * API Error boundary component
 * API 오류 경계 컴포넌트
 */
export const ApiErrorBoundary: React.FC<{
  error: any;
  onRetry?: () => void;
  children?: React.ReactNode;
}> = ({ error, onRetry, children }) => {
  if (!error) return <>{children}</>;

  const getApiErrorInfo = (error: any): ErrorInfo => {
    if (error?.response?.status === 401) {
      return {
        message: '인증이 만료되었습니다. 다시 로그인해주세요.',
        severity: 'warning',
        code: 'AUTH_401',
        timestamp: new Date(),
        action: {
          label: '로그인',
          handler: () => { window.location.href = '/auth/login'; }
        }
      };
    }

    if (error?.response?.status === 403) {
      return {
        message: '이 작업을 수행할 권한이 없습니다.',
        severity: 'error',
        code: 'AUTH_403',
        timestamp: new Date()
      };
    }

    if (error?.response?.status >= 500) {
      return {
        message: '서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.',
        severity: 'error',
        code: `SERVER_${error.response.status}`,
        timestamp: new Date(),
        retry: {
          enabled: true,
          maxAttempts: 3,
          handler: onRetry
        }
      };
    }

    if (error?.code === 'NETWORK_ERROR' || !navigator.onLine) {
      return {
        message: '네트워크 연결을 확인하고 다시 시도해주세요.',
        severity: 'warning',
        code: 'NETWORK_ERROR',
        timestamp: new Date(),
        retry: {
          enabled: true,
          maxAttempts: 5,
          handler: onRetry
        }
      };
    }

    return {
      message: error?.message || '알 수 없는 오류가 발생했습니다.',
      details: error?.stack || JSON.stringify(error, null, 2),
      severity: 'error',
      code: error?.code || 'UNKNOWN_ERROR',
      timestamp: new Date(),
      retry: {
        enabled: !!onRetry,
        maxAttempts: 2,
        handler: onRetry
      }
    };
  };

  const errorInfo = getApiErrorInfo(error);

  return (
    <ErrorDisplay
      error={errorInfo}
      onRetry={onRetry}
      showDetails={true}
      showTimestamp={true}
    />
  );
};