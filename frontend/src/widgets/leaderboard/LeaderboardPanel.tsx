'use client';

import React from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Avatar,
  Chip,
  LinearProgress,
} from '@mui/material';
import { EmojiEvents, TrendingUp, TrendingDown } from '@mui/icons-material';

interface LeaderboardEntry {
  id: number;
  challengeId: number;
  sessionId: number;
  userId: number;
  pnl: number;
  returnPercentage: number;
  rankPosition: number;
  calculatedAt: string;
}

interface LeaderboardPanelProps {
  challengeId: number;
}

/**
 * 리더보드 패널 컴포넌트
 * 챌린지 참가자들의 실시간 순위 표시
 */
export function LeaderboardPanel({ challengeId }: LeaderboardPanelProps) {
  const [leaderboard, setLeaderboard] = React.useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    loadLeaderboard();
    
    // 실시간 업데이트 (10초마다)
    const interval = setInterval(loadLeaderboard, 10000);
    return () => clearInterval(interval);
  }, [challengeId]);

  const loadLeaderboard = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/challenges/${challengeId}/leaderboard?limit=10`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setLeaderboard(data);
      }
    } catch (err) {
      console.error('리더보드 로드 오류:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return `₩${amount.toLocaleString()}`;
  };

  const formatPercentage = (percentage: number) => {
    const sign = percentage >= 0 ? '+' : '';
    return `${sign}${percentage.toFixed(2)}%`;
  };

  const getRankIcon = (rank: number) => {
    if (rank === 1) return <EmojiEvents sx={{ color: '#ffd700' }} />;
    if (rank === 2) return <EmojiEvents sx={{ color: '#c0c0c0' }} />;
    if (rank === 3) return <EmojiEvents sx={{ color: '#cd7f32' }} />;
    return null;
  };

  if (loading) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          리더보드
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        리더보드 🏆
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        실시간 수익률 순위 (상위 10명)
      </Typography>

      {leaderboard.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 3 }}>
          아직 참가자가 없습니다
        </Typography>
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>순위</TableCell>
                <TableCell>닉네임</TableCell>
                <TableCell align="right">수익률</TableCell>
                <TableCell align="right">손익</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {leaderboard.map((entry) => (
                <TableRow key={entry.userId}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {getRankIcon(entry.rankPosition)}
                      <Typography variant="body2" fontWeight="medium">
                        {entry.rankPosition}위
                      </Typography>
                    </Box>
                  </TableCell>
                  
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem' }}>
                        {entry.userId.toString().charAt(0)}
                      </Avatar>
                      <Typography variant="body2">
                        사용자 {entry.userId}
                      </Typography>
                    </Box>
                  </TableCell>
                  
                  <TableCell align="right">
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 0.5 }}>
                      {entry.returnPercentage >= 0 ? (
                        <TrendingUp fontSize="small" color="success" />
                      ) : (
                        <TrendingDown fontSize="small" color="error" />
                      )}
                      <Chip
                        label={formatPercentage(entry.returnPercentage)}
                        color={entry.returnPercentage >= 0 ? 'success' : 'error'}
                        size="small"
                        variant="outlined"
                      />
                    </Box>
                  </TableCell>
                  
                  <TableCell align="right">
                    <Typography 
                      variant="body2"
                      color={entry.pnl >= 0 ? 'success.main' : 'error.main'}
                      fontWeight="medium"
                    >
                      {formatCurrency(entry.pnl)}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
        10초마다 자동 업데이트
      </Typography>
    </Box>
  );
}