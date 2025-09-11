'use client';

import React from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
} from '@mui/material';
import { EmojiEvents } from '@mui/icons-material';
import { GlobalLeaderboard } from '@/widgets/leaderboard';

/**
 * 글로벌 리더보드 페이지
 * 모든 챌린지의 리더보드를 챌린지별로 조회 가능
 */
export default function LeaderboardPage() {
  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#0A0E18', pt: 4 }}>
      <Container maxWidth="lg">
        <Box sx={{ py: 4 }}>
          {/* 헤더 */}
          <Box sx={{ mb: 4 }}>
            <Typography 
              variant="h3" 
              component="h1" 
              gutterBottom
              sx={{ 
                color: '#FFFFFF',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: 2,
              }}
            >
              <EmojiEvents sx={{ fontSize: 40, color: '#FFD700' }} />
              리더보드
            </Typography>
            
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#78828A',
                maxWidth: 600,
                lineHeight: 1.6,
              }}
            >
              전 세계 트레이더들과 투자 실력을 겨루고 순위를 확인하세요.
              챌린지별로 최고의 수익률을 달성한 투자자들을 만나보세요.
            </Typography>
          </Box>

          {/* 리더보드 위젯 */}
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <GlobalLeaderboard />
            </Grid>
          </Grid>
        </Box>
      </Container>
    </Box>
  );
}