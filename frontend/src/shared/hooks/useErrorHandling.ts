'use client';

import { useState, useCallback, useRef, useEffect } from 'react';

interface ErrorState {
  error: Error | null;
  isError: boolean;
  errorMessage: string;
  retryCount: number;
  lastErrorTime: Date | null;
}

interface RetryConfig {
  maxRetries?: number;
  retryDelay?: number;
  exponentialBackoff?: boolean;
  maxDelay?: number;
  retryCondition?: (error: Error) => boolean;
}

interface UseErrorHandlingOptions {
  onError?: (error: Error) => void;
  autoRetry?: boolean;
  retryConfig?: RetryConfig;
  clearErrorAfter?: number; // milliseconds
}

interface UseErrorHandlingReturn {
  // Error state
  error: Error | null;
  isError: boolean;
  errorMessage: string;
  retryCount: number;
  
  // Actions
  setError: (error: Error | string | null) => void;
  clearError: () => void;
  retry: () => void;
  
  // Utilities
  withErrorHandling: <T extends any[], R>(
    fn: (...args: T) => Promise<R>
  ) => (...args: T) => Promise<R | void>;
  
  // Network-specific utilities
  isNetworkError: (error: Error) => boolean;
  isTimeoutError: (error: Error) => boolean;
  isServerError: (error: Error) => boolean;
}

/**
 * 포괄적인 에러 처리 및 재시도 로직을 제공하는 커스텀 훅
 */
export function useErrorHandling(options: UseErrorHandlingOptions = {}): UseErrorHandlingReturn {
  const {
    onError,
    autoRetry = false,
    retryConfig = {},
    clearErrorAfter,
  } = options;

  const {
    maxRetries = 3,
    retryDelay = 1000,
    exponentialBackoff = true,
    maxDelay = 30000,
    retryCondition = () => true,
  } = retryConfig;

  const [errorState, setErrorState] = useState<ErrorState>({
    error: null,
    isError: false,
    errorMessage: '',
    retryCount: 0,
    lastErrorTime: null,
  });

  const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const clearErrorTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastRetryFunctionRef = useRef<(() => void) | null>(null);

  // 에러 자동 클리어
  useEffect(() => {
    if (clearErrorAfter && errorState.isError) {
      clearErrorTimeoutRef.current = setTimeout(() => {
        clearError();
      }, clearErrorAfter);
    }

    return () => {
      if (clearErrorTimeoutRef.current) {
        clearTimeout(clearErrorTimeoutRef.current);
      }
    };
  }, [errorState.isError, clearErrorAfter]);

  const setError = useCallback((error: Error | string | null) => {
    // Clear existing timeouts
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    if (clearErrorTimeoutRef.current) {
      clearTimeout(clearErrorTimeoutRef.current);
      clearErrorTimeoutRef.current = null;
    }

    if (error === null) {
      setErrorState({
        error: null,
        isError: false,
        errorMessage: '',
        retryCount: 0,
        lastErrorTime: null,
      });
      return;
    }

    const errorObj = typeof error === 'string' ? new Error(error) : error;
    const newState: ErrorState = {
      error: errorObj,
      isError: true,
      errorMessage: errorObj.message,
      retryCount: errorState.retryCount,
      lastErrorTime: new Date(),
    };

    setErrorState(newState);

    // Call external error handler
    if (onError) {
      onError(errorObj);
    }

    // Auto-retry logic
    if (autoRetry && newState.retryCount < maxRetries && retryCondition(errorObj)) {
      const delay = exponentialBackoff
        ? Math.min(retryDelay * Math.pow(2, newState.retryCount), maxDelay)
        : retryDelay;

      retryTimeoutRef.current = setTimeout(() => {
        retry();
      }, delay);
    }
  }, [errorState.retryCount, onError, autoRetry, maxRetries, retryCondition, retryDelay, exponentialBackoff, maxDelay]);

  const clearError = useCallback(() => {
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    if (clearErrorTimeoutRef.current) {
      clearTimeout(clearErrorTimeoutRef.current);
      clearErrorTimeoutRef.current = null;
    }

    setErrorState({
      error: null,
      isError: false,
      errorMessage: '',
      retryCount: 0,
      lastErrorTime: null,
    });
  }, []);

  const retry = useCallback(() => {
    setErrorState(prev => ({
      ...prev,
      retryCount: prev.retryCount + 1,
    }));

    // Execute the last retry function if available
    if (lastRetryFunctionRef.current) {
      lastRetryFunctionRef.current();
    }
  }, []);

  const withErrorHandling = useCallback(
    <T extends any[], R>(fn: (...args: T) => Promise<R>) => {
      return async (...args: T): Promise<R | void> => {
        try {
          clearError();
          const result = await fn(...args);
          return result;
        } catch (error) {
          const errorObj = error instanceof Error ? error : new Error(String(error));
          
          // Store the retry function
          lastRetryFunctionRef.current = () => fn(...args);
          
          setError(errorObj);
          
          // Don't throw the error if auto-retry is enabled
          if (autoRetry && retryCondition(errorObj)) {
            return;
          }
          
          throw error;
        }
      };
    },
    [setError, clearError, autoRetry, retryCondition]
  );

  // Network error detection utilities
  const isNetworkError = useCallback((error: Error): boolean => {
    const message = error.message.toLowerCase();
    return message.includes('network') || 
           message.includes('fetch') || 
           message.includes('connection') ||
           message.includes('offline') ||
           error.name === 'NetworkError';
  }, []);

  const isTimeoutError = useCallback((error: Error): boolean => {
    const message = error.message.toLowerCase();
    return message.includes('timeout') || 
           message.includes('aborted') ||
           error.name === 'TimeoutError' ||
           error.name === 'AbortError';
  }, []);

  const isServerError = useCallback((error: Error): boolean => {
    const message = error.message.toLowerCase();
    return message.includes('500') || 
           message.includes('502') || 
           message.includes('503') || 
           message.includes('504') ||
           message.includes('server error');
  }, []);

  // Cleanup timeouts on unmount
  useEffect(() => {
    return () => {
      if (retryTimeoutRef.current) {
        clearTimeout(retryTimeoutRef.current);
      }
      if (clearErrorTimeoutRef.current) {
        clearTimeout(clearErrorTimeoutRef.current);
      }
    };
  }, []);

  return {
    error: errorState.error,
    isError: errorState.isError,
    errorMessage: errorState.errorMessage,
    retryCount: errorState.retryCount,
    setError,
    clearError,
    retry,
    withErrorHandling,
    isNetworkError,
    isTimeoutError,
    isServerError,
  };
}

/**
 * React Query와 함께 사용하기 위한 에러 처리 훅
 */
export function useQueryErrorHandling(options: UseErrorHandlingOptions = {}) {
  const errorHandling = useErrorHandling(options);

  const onError = useCallback((error: Error) => {
    errorHandling.setError(error);
  }, [errorHandling]);

  const onSuccess = useCallback(() => {
    errorHandling.clearError();
  }, [errorHandling]);

  return {
    ...errorHandling,
    queryOptions: {
      onError,
      onSuccess,
    },
  };
}