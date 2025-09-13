'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Avatar,
  Chip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Paper,
} from '@mui/material';
import { EmojiEvents, TrendingUp, TrendingDown } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import apiClient from '@/shared/api/api-client';

interface LeaderboardEntry {
  rank: number;
  userId: number;
  nickname: string;
  finalBalance: number;
  totalReturn: number;
  returnRate: number;
  completedAt: string;
}

interface Challenge {
  id: number;
  title: string;
  status: string;
}

interface GlobalLeaderboardProps {
  defaultChallengeId?: number;
}

/**
 * 글로벌 리더보드 위젯
 * 챌린지별 리더보드를 표시하는 재사용 가능한 컴포넌트
 */
export function GlobalLeaderboard({ defaultChallengeId }: GlobalLeaderboardProps) {
  const [selectedChallenge, setSelectedChallenge] = React.useState<number | ''>('');

  // 챌린지 목록 조회
  const { data: challenges = [] } = useQuery<Challenge[]>({
    queryKey: ['challenges'],
    queryFn: async () => {
      const response = await apiClient.get<{ challenges: Challenge[] }>('/api/challenges');
      return response?.challenges ?? [];
    },
    staleTime: 300000, // 5분
  });

  // 리더보드 데이터 조회
  const { 
    data: leaderboard = [], 
    isLoading,
    error 
  } = useQuery<LeaderboardEntry[]>({
    queryKey: ['leaderboard', selectedChallenge],
    queryFn: async () => {
      if (!selectedChallenge) return [];
      const data = await apiClient.get<LeaderboardEntry[]>(
        `/api/challenges/${selectedChallenge}/leaderboard?limit=10`
      );
      return data ?? [];
    },
    enabled: !!selectedChallenge,
    refetchInterval: 30000,
  });

  React.useEffect(() => {
    if (defaultChallengeId) {
      setSelectedChallenge(defaultChallengeId);
    } else if (challenges.length > 0 && !selectedChallenge) {
      // 첫 번째 활성 챌린지를 기본 선택
      const activeChallenge = challenges.find(c => c.status === 'ACTIVE');
      setSelectedChallenge(activeChallenge?.id ?? challenges[0].id);
    }
  }, [challenges, defaultChallengeId, selectedChallenge]);

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1:
        return <EmojiEvents sx={{ color: '#FFD700', fontSize: 20 }} />;
      case 2:
        return <EmojiEvents sx={{ color: '#C0C0C0', fontSize: 20 }} />;
      case 3:
        return <EmojiEvents sx={{ color: '#CD7F32', fontSize: 20 }} />;
      default:
        return (
          <Box
            sx={{
              width: 20,
              height: 20,
              borderRadius: '50%',
              backgroundColor: '#2A3441',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 10,
              fontWeight: 'bold',
              color: '#FFFFFF',
            }}
          >
            {rank}
          </Box>
        );
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
    }).format(amount);
  };

  const formatPercentage = (rate: number) => {
    const isPositive = rate > 0;
    return {
      value: `${isPositive ? '+' : ''}${rate.toFixed(1)}%`,
      color: isPositive ? '#4CAF50' : rate < 0 ? '#F44336' : '#78828A',
      icon: isPositive ? <TrendingUp sx={{ fontSize: 14 }} /> : 
            rate < 0 ? <TrendingDown sx={{ fontSize: 14 }} /> : null,
    };
  };

  if (isLoading) {
    return (
      <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
        <CardContent>
          <Typography variant="h6" sx={{ color: '#FFFFFF', mb: 2 }}>
            리더보드
          </Typography>
          <Box display="flex" justifyContent="center" py={4}>
            <Typography sx={{ color: '#78828A' }}>로딩 중...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <EmojiEvents sx={{ color: '#FFD700' }} />
          <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
            리더보드
          </Typography>
        </Box>

        {/* 챌린지 선택 */}
        {challenges.length > 0 && (
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel sx={{ color: '#78828A' }}>챌린지</InputLabel>
            <Select
              value={selectedChallenge}
              onChange={(e) => setSelectedChallenge(e.target.value === '' ? '' : Number(e.target.value))}
              size="small"
              sx={{
                color: '#FFFFFF',
                '& .MuiOutlinedInput-notchedOutline': {
                  borderColor: '#2A3441',
                },
                '&:hover .MuiOutlinedInput-notchedOutline': {
                  borderColor: '#2196F3',
                },
              }}
            >
              {challenges.map((challenge) => (
                <MenuItem key={challenge.id} value={challenge.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {challenge.title}
                    <Chip
                      label={challenge.status === 'ACTIVE' ? '진행중' : '완료'}
                      color={challenge.status === 'ACTIVE' ? 'success' : 'default'}
                      size="small"
                    />
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        {/* 리더보드 테이블 */}
        {leaderboard.length > 0 ? (
          <TableContainer component={Paper} sx={{ backgroundColor: 'transparent' }}>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ backgroundColor: '#0A0E18' }}>
                  <TableCell sx={{ color: '#78828A', borderBottom: '1px solid #2A3441' }}>
                    순위
                  </TableCell>
                  <TableCell sx={{ color: '#78828A', borderBottom: '1px solid #2A3441' }}>
                    닉네임
                  </TableCell>
                  <TableCell align="right" sx={{ color: '#78828A', borderBottom: '1px solid #2A3441' }}>
                    수익률
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {leaderboard.slice(0, 5).map((entry) => {
                  const percentage = formatPercentage(entry.returnRate);
                  return (
                    <TableRow
                      key={entry.userId}
                      sx={{ borderBottom: '1px solid #2A3441' }}
                    >
                      <TableCell sx={{ color: '#FFFFFF' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {getRankIcon(entry.rank)}
                          <Typography variant="body2">
                            #{entry.rank}
                          </Typography>
                        </Box>
                      </TableCell>
                      
                      <TableCell sx={{ color: '#FFFFFF' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Avatar
                            sx={{
                              width: 24,
                              height: 24,
                              backgroundColor: entry.rank <= 3 ? '#2196F3' : '#2A3441',
                              fontSize: '0.75rem',
                              fontWeight: 'bold',
                            }}
                          >
                            {entry.nickname.charAt(0).toUpperCase()}
                          </Avatar>
                          <Typography variant="body2">
                            {entry.nickname}
                          </Typography>
                        </Box>
                      </TableCell>
                      
                      <TableCell align="right">
                        <Box sx={{ 
                          display: 'flex', 
                          alignItems: 'center', 
                          justifyContent: 'flex-end',
                          gap: 0.5,
                          color: percentage.color,
                        }}>
                          {percentage.icon}
                          <Typography 
                            variant="body2" 
                            sx={{ 
                              fontFamily: 'monospace', 
                              fontWeight: 'bold',
                              color: percentage.color,
                            }}
                          >
                            {percentage.value}
                          </Typography>
                        </Box>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        ) : (
          <Box textAlign="center" sx={{ py: 4 }}>
            <Typography variant="body2" sx={{ color: '#78828A' }}>
              아직 완료된 챌린지가 없습니다
            </Typography>
          </Box>
        )}

        <Typography variant="caption" sx={{ color: '#78828A', display: 'block', textAlign: 'center', mt: 1 }}>
          30초마다 자동 업데이트
        </Typography>
      </CardContent>
    </Card>
  );
}