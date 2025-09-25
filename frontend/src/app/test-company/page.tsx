'use client';

import React, { useState } from 'react';
import { Box, Container, Typography, Paper, Grid } from '@mui/material';
import CompanyAutocomplete from '@/shared/components/CompanyAutocomplete/CompanyAutocomplete';

export default function TestCompanyPage() {
  const [selectedCompany, setSelectedCompany] = useState<string>('');

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        CompanyAutocomplete 테스트 페이지
      </Typography>

      <Grid container spacing={4}>
        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              회사 검색 테스트
            </Typography>

            <Box sx={{ mt: 2 }}>
              <CompanyAutocomplete
                value={selectedCompany}
                onChange={(value) => {
                  console.log('🔄 Company selected:', value);
                  setSelectedCompany(value || '');
                }}
                placeholder="회사를 검색하세요... (예: 삼성, 카카오)"
                label="회사 선택"
              />
            </Box>

            <Box sx={{ mt: 3 }}>
              <Typography variant="body1">
                <strong>선택된 회사:</strong> {selectedCompany || '없음'}
              </Typography>
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper elevation={2} sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              테스트 가이드
            </Typography>
            <Typography variant="body2" component="div">
              <ul>
                <li>검색창에 한글로 회사명을 입력해보세요 (예: &ldquo;삼성&rdquo;)</li>
                <li>자동완성 드롭다운이 나타나는지 확인하세요</li>
                <li>회사를 선택했을 때 선택된 값이 표시되는지 확인하세요</li>
                <li>브라우저 개발자 도구 콘솔에서 API 요청/응답을 확인하세요</li>
              </ul>
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}