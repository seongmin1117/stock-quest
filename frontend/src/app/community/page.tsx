'use client';

import React from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
  Chip,
} from '@mui/material';
import { People } from '@mui/icons-material';
import { CommunityFeed } from '@/widgets/community';
import { useQuery } from '@tanstack/react-query';
import apiClient from '@/shared/api/api-client';

interface Challenge {
  id: number;
  title: string;
  status: string;
}

/**
 * 커뮤니티 메인 페이지
 * 챌린지별 게시글과 댓글 기능 제공
 */
export default function CommunityPage() {
  const [selectedChallenge, setSelectedChallenge] = React.useState<number | ''>('');

  // 챌린지 목록 조회
  const { data: challenges = [], isLoading: challengesLoading } = useQuery<Challenge[]>({
    queryKey: ['challenges'],
    queryFn: async () => {
      const response = await apiClient.get<{ challenges: Challenge[] }>('/api/challenges');
      return response?.challenges ?? [];
    },
    staleTime: 300000, // 5분
  });

  React.useEffect(() => {
    if (challenges.length > 0 && !selectedChallenge) {
      setSelectedChallenge(challenges[0].id);
    }
  }, [challenges, selectedChallenge]);

  if (challengesLoading) {
    return (
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#0A0E18', pt: 4 }}>
      <Container maxWidth="md">
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
              <People sx={{ fontSize: 40, color: '#2196F3' }} />
              커뮤니티
            </Typography>
            
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#78828A',
                maxWidth: 600,
                lineHeight: 1.6,
              }}
            >
              다른 투자자들과 투자 전략을 공유하고 함께 성장하세요.
              챌린지별로 경험을 나누고 조언을 구할 수 있습니다.
            </Typography>
          </Box>

          {/* 챌린지 선택 */}
          {challenges.length > 0 && (
            <Card sx={{ 
              mb: 4, 
              backgroundColor: '#1A1F2E',
              border: '1px solid #2A3441',
            }}>
              <CardContent>
                <FormControl fullWidth>
                  <InputLabel sx={{ color: '#78828A' }}>챌린지 선택</InputLabel>
                  <Select
                    value={selectedChallenge}
                    onChange={(e) => setSelectedChallenge(e.target.value === '' ? '' : Number(e.target.value))}
                    sx={{
                      color: '#FFFFFF',
                      '& .MuiOutlinedInput-notchedOutline': {
                        borderColor: '#2A3441',
                      },
                      '&:hover .MuiOutlinedInput-notchedOutline': {
                        borderColor: '#2196F3',
                      },
                      '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
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
              </CardContent>
            </Card>
          )}

          {/* 커뮤니티 피드 위젯 */}
          {selectedChallenge && (
            <CommunityFeed challengeId={selectedChallenge as number} />
          )}
        </Box>
      </Container>
    </Box>
  );
}