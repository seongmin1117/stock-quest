'use client';

import React from 'react';
import {
  Container,
  Grid,
  Typography,
  Box,
  Button,
  Alert,
} from '@mui/material';
import { useParams, useRouter } from 'next/navigation';
import { TradingPanel } from '@/features/place-order';
import { PortfolioPanel } from '@/widgets/portfolio';
import { LeaderboardPanel } from '@/widgets/leaderboard';
import { MarketDataPanel } from '@/widgets/market-data';
import { apiClient } from '@/shared/api/api-client';

/**
 * 챌린지 세션 메인 페이지
 * 거래 인터페이스, 포트폴리오, 리더보드 등을 통합 표시
 */
export default function ChallengeSessionPage() {
  const params = useParams();
  const router = useRouter();
  const challengeId = Number(params.challengeId);
  const sessionId = Number(params.sessionId);
  
  const [sessionEnded, setSessionEnded] = React.useState(false);

  const handleEndChallenge = async () => {
    try {
      const result = await apiClient({
        method: 'POST',
        url: `/api/sessions/${sessionId}/close`,
      });

      setSessionEnded(true);
      
      // 결과 페이지로 이동
      setTimeout(() => {
        router.push(`/challenges/${challengeId}/results/${sessionId}`);
      }, 2000); // 2초 후 이동
    } catch (err) {
      console.error('챌린지 종료 오류:', err);
      alert('챌린지 종료에 실패했습니다. 다시 시도해 주세요.');
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', py: { xs: 2, md: 4 } }}>
      <Container maxWidth="xl">
        {/* 헤더 */}
        <Box 
          className="glass"
          sx={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            mb: 4,
            p: 3,
            borderRadius: 3,
            flexDirection: { xs: 'column', sm: 'row' },
            gap: { xs: 2, sm: 0 }
          }}
        >
          <Box>
            <Typography 
              variant="h4" 
              component="h1"
              sx={{
                fontWeight: 800,
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                mb: 1
              }}
            >
              🚀 투자 챌린지 진행중
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Challenge #{challengeId} • Session #{sessionId}
            </Typography>
          </Box>
          
          <Button
            variant="contained"
            onClick={handleEndChallenge}
            disabled={sessionEnded}
            className="btn-hover"
            sx={{
              px: 4,
              py: 1.5,
              borderRadius: 3,
              background: sessionEnded 
                ? 'rgba(244, 63, 94, 0.3)' 
                : 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)',
              '&:hover:not(:disabled)': {
                background: 'linear-gradient(135deg, #dc2626 0%, #b91c1c 100%)',
              }
            }}
          >
            {sessionEnded ? '종료됨' : '🏁 챌린지 종료하기'}
          </Button>
        </Box>

        {sessionEnded && (
          <Alert 
            severity="info" 
            sx={{ 
              mb: 3,
              borderRadius: 2,
              background: 'rgba(6, 182, 212, 0.1)',
              border: '1px solid rgba(6, 182, 212, 0.2)',
            }}
          >
            챌린지가 종료되었습니다. 결과 페이지로 이동합니다...
          </Alert>
        )}

        {/* 메인 컨텐츠 그리드 */}
        <Grid container spacing={3}>
          {/* 좌측: 거래 패널 */}
          <Grid item xs={12} lg={8}>
            <Grid container spacing={3}>
              {/* 시장 데이터 차트 */}
              <Grid item xs={12}>
                <Box className="glass" sx={{ p: 3, borderRadius: 3 }}>
                  <MarketDataPanel challengeId={challengeId} />
                </Box>
              </Grid>
              
              {/* 거래 패널 */}
              <Grid item xs={12}>
                <TradingPanel sessionId={sessionId} />
              </Grid>
            </Grid>
          </Grid>

          {/* 우측: 포트폴리오 & 리더보드 */}
          <Grid item xs={12} lg={4}>
            <Grid container spacing={3}>
              {/* 포트폴리오 */}
              <Grid item xs={12}>
                <PortfolioPanel sessionId={sessionId} />
              </Grid>
              
              {/* 리더보드 */}
              <Grid item xs={12}>
                <Box className="glass" sx={{ p: 3, borderRadius: 3 }}>
                  <LeaderboardPanel challengeId={challengeId} />
                </Box>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}