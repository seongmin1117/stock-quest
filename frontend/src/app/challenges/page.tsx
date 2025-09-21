'use client';

import React from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Grid,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useRouter } from 'next/navigation';
import { TrendingUp, Speed, DateRange } from '@mui/icons-material';
import challengeApi, {
  ChallengeStatus,
  ChallengeDifficulty,
  ChallengeType
} from "@/shared/api/challenge-client";
import type { Challenge } from "@/shared/api/generated/model";

/**
 * 챌린지 목록 페이지
 * 사용 가능한 모든 챌린지를 표시하고 참여 버튼 제공
 */
export default function ChallengesPage() {
  const router = useRouter();
  const [challenges, setChallenges] = React.useState<Challenge[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    loadChallenges();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadChallenges = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await challengeApi.getChallenges({
        page: 0,
        size: 20,
      });
      setChallenges(response.challenges ?? []);
    } catch (err: any) {
      const msg =
          err?.response?.data?.message ||
          err?.message ||
          '챌린지 목록을 불러오는데 실패했습니다';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleStartChallenge = async (challengeId: number) => {
    try {
      setError(null);
      const response = await challengeApi.startChallenge(challengeId);
      router.push(`/challenges/${challengeId}/session/${response.sessionId}`);
    } catch (err: any) {
      const msg =
          err?.response?.data?.message ||
          err?.message ||
          '챌린지 시작에 실패했습니다';
      setError(msg);
    }
  };

  const getStatusColor = (status: Challenge['status']) => {
    switch (status) {
      case ChallengeStatus.ACTIVE:
        return 'success';
      case ChallengeStatus.COMPLETED:
        return 'default';
      case ChallengeStatus.DRAFT:
        return 'warning';
      case ChallengeStatus.SCHEDULED:
        return 'warning';
      case ChallengeStatus.ARCHIVED:
        return 'default';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: Challenge['status']) => {
    switch (status) {
      case ChallengeStatus.ACTIVE:
        return '진행중';
      case ChallengeStatus.COMPLETED:
        return '완료';
      case ChallengeStatus.DRAFT:
        return '준비중';
      case ChallengeStatus.SCHEDULED:
        return '예정됨';
      case ChallengeStatus.ARCHIVED:
        return '보관됨';
      default:
        return status;
    }
  };

  if (loading) {
    return (
        <Container maxWidth="lg">
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
            <CircularProgress />
          </Box>
        </Container>
    );
  }

  return (
      <Container maxWidth="lg">
        <Box sx={{ py: 4 }}>
          <Typography variant="h3" component="h1" gutterBottom>
            투자 챌린지
          </Typography>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
            과거 시장 데이터로 투자 실력을 테스트해보세요.
            각 챌린지는 실제 역사적 시장 상황을 재현합니다.
          </Typography>

          {error && (
              <Alert severity="error" sx={{ mb: 3 }}>
                {error}
              </Alert>
          )}

          <Grid container spacing={3}>
            {challenges.map((challenge) => (
                <Grid item xs={12} md={6} lg={4} key={challenge.id}>
                  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    <CardContent sx={{ flexGrow: 1 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                        <Typography variant="h6" component="h2" sx={{ flexGrow: 1 }}>
                          {challenge.title}
                        </Typography>
                        <Chip
                            label={getStatusText(challenge.status)}
                            color={getStatusColor(challenge.status)}
                            size="small"
                        />
                      </Box>

                      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                        {challenge.description}
                      </Typography>

                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        {challenge.startDate && challenge.endDate && (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <DateRange fontSize="small" color="action" />
                            <Typography variant="body2" color="text.secondary">
                              {new Date(challenge.startDate).toLocaleDateString()} ~ {new Date(challenge.endDate).toLocaleDateString()}
                            </Typography>
                          </Box>
                        )}

                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Speed fontSize="small" color="action" />
                          <Typography variant="body2" color="text.secondary">
                            기간: {challenge.durationDays}일
                          </Typography>
                        </Box>

                        {challenge.currentParticipants !== undefined && (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Typography variant="body2" color="text.secondary">
                              👥 참가자: {challenge.currentParticipants}명
                              {challenge.maxParticipants && ` / ${challenge.maxParticipants}명`}
                            </Typography>
                          </Box>
                        )}
                      </Box>
                    </CardContent>

                    <CardActions sx={{ p: 2, pt: 0 }}>
                      <Button
                          fullWidth
                          variant="contained"
                          startIcon={<TrendingUp />}
                          onClick={() => challenge.id && handleStartChallenge(challenge.id)}
                          disabled={challenge.status !== ChallengeStatus.ACTIVE}
                      >
                        {challenge.status === ChallengeStatus.ACTIVE ? '챌린지 시작' : '참여 불가'}
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
            ))}
          </Grid>

          {challenges.length === 0 && !loading && (
              <Box textAlign="center" sx={{ py: 8 }}>
                <Typography variant="h6" color="text.secondary">
                  사용 가능한 챌린지가 없습니다
                </Typography>
              </Box>
          )}
        </Box>
      </Container>
  );
}
