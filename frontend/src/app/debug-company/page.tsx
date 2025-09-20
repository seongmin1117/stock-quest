'use client';

import React, { useState, useEffect } from 'react';
import { Box, Container, Typography, Button, CircularProgress, Alert } from '@mui/material';
import { companyClient } from '@/shared/api/company-client';
import { useAuthStore } from '@/shared/lib/auth/auth-store';

export default function DebugCompanyPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any>(null);
  const [error, setError] = useState<string>('');

  // Authentication debugging
  const { getAccessToken, isTokenExpired } = useAuthStore();
  const { isAuthenticated, user } = useAuthStore();

  const testCategories = async () => {
    console.log('🔍 Testing categories API...');
    console.log('🔐 Auth status:', {
      isAuthenticated,
      hasUser: !!user,
      userEmail: user?.email,
      hasToken: !!getAccessToken(),
      isTokenExpired: isTokenExpired()
    });
    setLoading(true);
    setError('');
    try {
      const result = await companyClient.getCategories();
      console.log('✅ Categories result:', result);
      setData({ type: 'categories', data: result });
    } catch (err: any) {
      console.error('❌ Categories error:', err);
      console.error('❌ Categories error details:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
        headers: err.response?.headers
      });
      setError(`Categories error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const testPopular = async () => {
    console.log('🔍 Testing popular companies API...');
    console.log('🔐 Auth status:', {
      isAuthenticated,
      hasUser: !!user,
      hasToken: !!getAccessToken(),
      isTokenExpired: isTokenExpired()
    });
    setLoading(true);
    setError('');
    try {
      const result = await companyClient.getPopular(5);
      console.log('✅ Popular companies result:', result);
      setData({ type: 'popular', data: result });
    } catch (err: any) {
      console.error('❌ Popular companies error:', err);
      console.error('❌ Popular companies error details:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data
      });
      setError(`Popular companies error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const testSearch = async () => {
    console.log('🔍 Testing search API...');
    console.log('🔐 Auth status:', {
      isAuthenticated,
      hasUser: !!user,
      hasToken: !!getAccessToken(),
      isTokenExpired: isTokenExpired()
    });
    setLoading(true);
    setError('');
    try {
      const result = await companyClient.search({ q: '삼성', limit: 5 });
      console.log('✅ Search result:', result);
      setData({ type: 'search', data: result });
    } catch (err: any) {
      console.error('❌ Search error:', err);
      console.error('❌ Search error details:', {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data
      });
      setError(`Search error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const testDirectFetch = async () => {
    console.log('🔍 Testing direct fetch (bypassing API client)...');
    setLoading(true);
    setError('');
    try {
      const response = await fetch('http://localhost:8080/api/v1/companies/categories');
      console.log('✅ Direct fetch response status:', response.status);
      console.log('✅ Direct fetch response headers:', Array.from(response.headers.entries()));

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const result = await response.json();
      console.log('✅ Direct fetch result:', result);
      console.log('✅ Direct fetch result type:', typeof result, 'isArray:', Array.isArray(result));
      setData({ type: 'direct-fetch', data: result });
    } catch (err: any) {
      console.error('❌ Direct fetch error:', err);
      setError(`Direct fetch error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        API 디버깅 페이지
      </Typography>

      {/* Authentication Status Display */}
      <Box sx={{ mb: 4, p: 2, border: 1, borderColor: 'divider', borderRadius: 1 }}>
        <Typography variant="h6" gutterBottom>
          인증 상태
        </Typography>
        <Typography variant="body2" component="div">
          <strong>인증됨:</strong> {isAuthenticated ? '✅ YES' : '❌ NO'}<br />
          <strong>사용자:</strong> {user?.email || '없음'}<br />
          <strong>토큰 있음:</strong> {getAccessToken() ? '✅ YES' : '❌ NO'}<br />
          <strong>토큰 만료:</strong> {isTokenExpired() ? '❌ 만료됨' : '✅ 유효함'}
        </Typography>
      </Box>

      <Box sx={{ mb: 4, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <Button variant="contained" onClick={testCategories} disabled={loading}>
          카테고리 테스트
        </Button>
        <Button variant="contained" onClick={testPopular} disabled={loading}>
          인기 종목 테스트
        </Button>
        <Button variant="contained" onClick={testSearch} disabled={loading}>
          검색 테스트
        </Button>
        <Button variant="outlined" color="secondary" onClick={testDirectFetch} disabled={loading}>
          직접 Fetch 테스트
        </Button>
      </Box>

      {loading && (
        <Box display="flex" justifyContent="center" my={2}>
          <CircularProgress />
        </Box>
      )}

      {error && (
        <Typography color="error" sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}

      {data && (
        <Box>
          <Typography variant="h5" gutterBottom>
            {data.type} 결과:
          </Typography>
          <Box
            component="pre"
            sx={{
              backgroundColor: '#f5f5f5',
              p: 2,
              borderRadius: 1,
              overflow: 'auto',
              fontSize: '0.875rem'
            }}
          >
            {JSON.stringify(data.data, null, 2)}
          </Box>
        </Box>
      )}
    </Container>
  );
}