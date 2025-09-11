'use client';

import React from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Grid,
  Divider,
} from '@mui/material';
import { useParams, useRouter } from 'next/navigation';
import { TrendingUp, TrendingDown, Assessment } from '@mui/icons-material';

/**
 * 챌린지 결과 페이지
 * 챌린지 완료 후 수익률, 포트폴리오 내역, 실제 종목명 공개
 */
export default function ChallengeResultsPage() {
  const params = useParams();
  const router = useRouter();
  const challengeId = Number(params.challengeId);
  const sessionId = Number(params.sessionId);

  // TODO: API에서 실제 결과 데이터 가져오기
  const [results, setResults] = React.useState(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const fetchResults = async () => {
      try {
        // API 호출 로직 추가 예정
        setLoading(false);
      } catch (error) {
        console.error('결과 조회 오류:', error);
        setLoading(false);
      }
    };

    fetchResults();
  }, [challengeId, sessionId]);

  const handleGoBack = () => {
    router.push('/challenges');
  };

  if (loading) {
    return (
      <Container maxWidth="md">
        <Box sx={{ py: 4, textAlign: 'center' }}>
          <Typography>결과를 불러오는 중...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 4 }}>
        <Paper sx={{ p: 4, mb: 3 }}>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Assessment sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
            <Typography variant="h4" component="h1" gutterBottom>
              챌린지 완료!
            </Typography>
            <Typography variant="body1" color="text.secondary">
              모의투자 결과와 실제 종목을 확인해보세요
            </Typography>
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* 수익률 요약 */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  최종 수익률
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <TrendingUp sx={{ color: 'success.main', mr: 1 }} />
                  <Typography variant="h4" color="success.main">
                    +15.2%
                  </Typography>
                </Box>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  최종 자산
                </Typography>
                <Typography variant="h4">
                  ₩1,152,000
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  순손익
                </Typography>
                <Typography variant="h4" color="success.main">
                  +₩152,000
                </Typography>
              </Box>
            </Grid>
          </Grid>

          <Divider sx={{ my: 3 }} />

          {/* 종목 공개 섹션 */}
          <Box sx={{ mb: 4 }}>
            <Typography variant="h5" gutterBottom>
              🎭 종목 공개
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              챌린지에서 거래했던 종목들의 실제 이름을 확인하세요
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Company A → Apple Inc. (AAPL)
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    보유수량: 10주 | 수익률: +18.5%
                  </Typography>
                </Paper>
              </Grid>
              <Grid item xs={12} md={6}>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Company B → Microsoft Corp. (MSFT)
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    보유수량: 5주 | 수익률: +12.3%
                  </Typography>
                </Paper>
              </Grid>
            </Grid>
          </Box>

          {/* 액션 버튼 */}
          <Box sx={{ textAlign: 'center', mt: 4 }}>
            <Button
              variant="contained"
              size="large"
              onClick={handleGoBack}
              sx={{ minWidth: 200 }}
            >
              다른 챌린지 참여하기
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}