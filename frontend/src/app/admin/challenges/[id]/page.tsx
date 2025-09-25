'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  LinearProgress,
  Alert,
  Skeleton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Tabs,
  Tab
} from '@mui/material';
import {
  Edit,
  Delete,
  Archive,
  PlayArrow,
  Stop,
  Star,
  StarBorder,
  ArrowBack,
  People,
  Schedule,
  AttachMoney,
  TrendingUp,
  Assessment,
  Settings,
  Visibility,
  EmojiEvents
} from '@mui/icons-material';
import { useParams, useRouter } from 'next/navigation';
import { adminChallengeApi, Challenge, ChallengeDifficulty, ChallengeType, ChallengeStatus } from '@/shared/api/admin-challenge-client';
import { useAuth } from '@/shared/lib/auth/auth-store';

interface Participant {
  id: number;
  username: string;
  email: string;
  joinedAt: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED';
  currentBalance: number;
  profitLoss: number;
  rank: number;
}

const difficultyColors = {
  [ChallengeDifficulty.BEGINNER]: 'success',
  [ChallengeDifficulty.INTERMEDIATE]: 'warning',
  [ChallengeDifficulty.ADVANCED]: 'error',
  [ChallengeDifficulty.EXPERT]: 'error'
} as const;

const statusColors = {
  [ChallengeStatus.DRAFT]: 'default',
  [ChallengeStatus.SCHEDULED]: 'warning',
  [ChallengeStatus.ACTIVE]: 'primary',
  [ChallengeStatus.COMPLETED]: 'success',
  [ChallengeStatus.ARCHIVED]: 'secondary',
  [ChallengeStatus.CANCELLED]: 'error',
  [ChallengeStatus.PAUSED]: 'warning'
} as const;

const typeColors = {
  [ChallengeType.MARKET_CRASH]: 'error',
  [ChallengeType.BULL_MARKET]: 'success',
  [ChallengeType.SECTOR_ROTATION]: 'primary',
  [ChallengeType.VOLATILITY]: 'warning',
  [ChallengeType.ESG]: 'info',
  [ChallengeType.INTERNATIONAL]: 'secondary',
  [ChallengeType.OPTIONS]: 'error',
  [ChallengeType.RISK_MANAGEMENT]: 'warning',
  [ChallengeType.TOURNAMENT]: 'primary',
  [ChallengeType.EDUCATIONAL]: 'info',
  [ChallengeType.COMMUNITY]: 'secondary',
  [ChallengeType.STOCK_PICKING]: 'primary',
  [ChallengeType.PORTFOLIO_MANAGEMENT]: 'secondary',
  [ChallengeType.OPTIONS_TRADING]: 'error',
  [ChallengeType.SECTOR_ANALYSIS]: 'info',
  [ChallengeType.TECHNICAL_ANALYSIS]: 'success'
} as const;

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      {...other}
    >
      {value === index && (
        <Box sx={{ py: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

export default function ChallengeDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const challengeId = parseInt(params.id as string);

  const [challenge, setChallenge] = useState<Challenge | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  // 액션 다이얼로그 상태
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    const fetchChallenge = async () => {
      try {
        setLoading(true);
        setError(null);

        const challenge = await adminChallengeApi.getChallengeById(challengeId);
        setChallenge(challenge);

        // TODO: 참여자 목록 API 추가 시 연동
        setParticipants([]);
      } catch (err) {
        setError('챌린지 정보를 불러오는데 실패했습니다.');
        console.error('Error fetching challenge:', err);
      } finally {
        setLoading(false);
      }
    };

    if (challengeId) {
      fetchChallenge();
    }
  }, [challengeId]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleStatusChange = async (newStatus: ChallengeStatus) => {
    if (!challenge || !user) return;

    try {
      const updatedChallenge = await adminChallengeApi.changeStatus(challengeId, newStatus, user.id);
      setChallenge(updatedChallenge);
    } catch (err) {
      console.error('Error changing status:', err);
      setError('상태 변경에 실패했습니다.');
    }
  };

  const toggleFeatured = async () => {
    if (!challenge || !user) return;

    try {
      const updatedChallenge = await adminChallengeApi.setFeaturedChallenge(challengeId, !challenge.featured, user.id);
      setChallenge(updatedChallenge);
    } catch (err) {
      console.error('Error toggling featured:', err);
      setError('피처드 설정 변경에 실패했습니다.');
    }
  };

  const handleDelete = async () => {
    if (!user) return;

    try {
      // TODO: 삭제 API 호출 (현재 admin API에 삭제가 없음)
      setDeleteDialogOpen(false);
      router.push('/admin/challenges');
    } catch (err) {
      console.error('Error deleting challenge:', err);
      setError('챌린지 삭제에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <Box>
        <Skeleton variant="text" width={200} height={40} sx={{ mb: 2 }} />
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Skeleton variant="text" width="80%" height={32} />
                <Skeleton variant="text" width="100%" height={20} sx={{ my: 1 }} />
                <Skeleton variant="text" width="100%" height={20} />
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Skeleton variant="text" width="60%" height={24} />
                <Skeleton variant="rectangular" width="100%" height={120} sx={{ my: 2 }} />
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>
    );
  }

  if (error || !challenge) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error || '챌린지를 찾을 수 없습니다.'}
        </Alert>
        <Button
          variant="outlined"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/challenges')}
        >
          챌린지 목록으로
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="start">
        <Box>
          <Button
            variant="text"
            startIcon={<ArrowBack />}
            onClick={() => router.push('/admin/challenges')}
            sx={{ mb: 2 }}
          >
            챌린지 목록으로
          </Button>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
            {challenge.title}
            {challenge.featured && (
              <Star sx={{ color: 'gold', ml: 1, fontSize: 30 }} />
            )}
          </Typography>
          <Box display="flex" gap={1} mb={2}>
            <Chip
              label={challenge.status === ChallengeStatus.DRAFT ? '초안' :
                    challenge.status === ChallengeStatus.SCHEDULED ? '예약됨' :
                    challenge.status === ChallengeStatus.ACTIVE ? '진행중' :
                    challenge.status === ChallengeStatus.COMPLETED ? '완료' :
                    challenge.status === ChallengeStatus.CANCELLED ? '취소됨' : '보관'}
              color={statusColors[challenge.status]}
            />
            <Chip
              label={challenge.difficulty === ChallengeDifficulty.BEGINNER ? '초급' :
                    challenge.difficulty === ChallengeDifficulty.INTERMEDIATE ? '중급' :
                    challenge.difficulty === ChallengeDifficulty.ADVANCED ? '고급' : '전문가'}
              color={difficultyColors[challenge.difficulty]}
            />
            <Chip
              label={challenge.challengeType === ChallengeType.MARKET_CRASH ? '마켓 크래시' :
                    challenge.challengeType === ChallengeType.BULL_MARKET ? '상승장' :
                    challenge.challengeType === ChallengeType.SECTOR_ROTATION ? '섹터 로테이션' :
                    challenge.challengeType === ChallengeType.VOLATILITY ? '변동성 거래' :
                    challenge.challengeType === ChallengeType.ESG ? 'ESG 투자' :
                    challenge.challengeType === ChallengeType.INTERNATIONAL ? '해외 시장' :
                    challenge.challengeType === ChallengeType.OPTIONS ? '옵션 거래' :
                    challenge.challengeType === ChallengeType.RISK_MANAGEMENT ? '리스크 관리' :
                    challenge.challengeType === ChallengeType.TOURNAMENT ? '토너먼트' :
                    challenge.challengeType === ChallengeType.EDUCATIONAL ? '교육용' : '커뮤니티'}
              color={typeColors[challenge.challengeType]}
            />
          </Box>
        </Box>

        {/* 액션 버튼들 */}
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={challenge.featured ? <StarBorder /> : <Star />}
            onClick={toggleFeatured}
          >
            {challenge.featured ? '피처드 해제' : '피처드 설정'}
          </Button>
          <Button
            variant="outlined"
            startIcon={<Edit />}
            href={`/admin/challenges/${challengeId}/edit`}
          >
            수정
          </Button>
          {challenge.status === ChallengeStatus.DRAFT && (
            <Button
              variant="contained"
              startIcon={<PlayArrow />}
              onClick={() => handleStatusChange(ChallengeStatus.ACTIVE)}
            >
              활성화
            </Button>
          )}
          {challenge.status === ChallengeStatus.ACTIVE && (
            <Button
              variant="outlined"
              startIcon={<Stop />}
              onClick={() => handleStatusChange(ChallengeStatus.COMPLETED)}
            >
              완료처리
            </Button>
          )}
          <Button
            variant="outlined"
            startIcon={<Archive />}
            onClick={() => handleStatusChange(ChallengeStatus.ARCHIVED)}
          >
            보관
          </Button>
          <Button
            variant="outlined"
            color="error"
            startIcon={<Delete />}
            onClick={() => setDeleteDialogOpen(true)}
          >
            삭제
          </Button>
        </Box>
      </Box>

      {/* 탭 네비게이션 */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="기본 정보" />
          <Tab label="참여자 현황" />
          <Tab label="설정 및 관리" />
        </Tabs>
      </Box>

      {/* 기본 정보 탭 */}
      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {/* 설명 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  챌린지 설명
                </Typography>
                <Typography variant="body1" sx={{ lineHeight: 1.6 }}>
                  {challenge.description}
                </Typography>
              </CardContent>
            </Card>

            {/* 학습 목표 */}
            {challenge.learningObjectives && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    학습 목표
                  </Typography>
                  <Typography variant="body1" sx={{ whiteSpace: 'pre-line' }}>
                    {challenge.learningObjectives}
                  </Typography>
                </CardContent>
              </Card>
            )}

            {/* 시장 시나리오 */}
            {challenge.marketScenarioDescription && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    시장 시나리오
                  </Typography>
                  <Typography variant="body1">
                    {challenge.marketScenarioDescription}
                  </Typography>
                </CardContent>
              </Card>
            )}

            {/* 태그 */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  태그
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {(challenge.tags || []).map((tag) => (
                    <Chip key={tag} label={tag} variant="outlined" size="small" />
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 기본 통계 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  기본 정보
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemIcon>
                      <AttachMoney />
                    </ListItemIcon>
                    <ListItemText
                      primary="초기 자금"
                      secondary={`${challenge.initialBalance.toLocaleString()}원`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <Schedule />
                    </ListItemIcon>
                    <ListItemText
                      primary="진행 기간"
                      secondary={`${challenge.durationDays}일 (예상: ${challenge.estimatedTimeMinutes || 0}분)`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <People />
                    </ListItemIcon>
                    <ListItemText
                      primary="참여자"
                      secondary={`${challenge.currentParticipants}${challenge.maxParticipants ? `/${challenge.maxParticipants}` : ''}명`}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>

            {/* 참여율 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  참여 현황
                </Typography>
                {challenge.maxParticipants && (
                  <>
                    <LinearProgress
                      variant="determinate"
                      value={((challenge.currentParticipants || 0) / challenge.maxParticipants) * 100}
                      sx={{ mb: 2 }}
                    />
                    <Typography variant="body2" color="text.secondary">
                      {Math.round(((challenge.currentParticipants || 0) / challenge.maxParticipants) * 100)}% 달성
                    </Typography>
                  </>
                )}
              </CardContent>
            </Card>

            {/* 사용 가능한 투자 상품 */}
            {challenge.availableInstruments && (
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    투자 상품
                  </Typography>
                  <Box display="flex" flexWrap="wrap" gap={1}>
                    {challenge.availableInstruments.map((instrument) => (
                      <Chip
                        key={instrument}
                        label={
                          instrument === 'STOCKS' ? '주식' :
                          instrument === 'ETF' ? 'ETF' :
                          instrument === 'BONDS' ? '채권' :
                          instrument === 'CASH' ? '현금' : instrument
                        }
                        color="primary"
                        variant="outlined"
                        size="small"
                      />
                    ))}
                  </Box>
                </CardContent>
              </Card>
            )}
          </Grid>
        </Grid>
      </TabPanel>

      {/* 참여자 현황 탭 */}
      <TabPanel value={tabValue} index={1}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              참여자 목록 ({participants.length}명)
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>순위</TableCell>
                    <TableCell>사용자</TableCell>
                    <TableCell>참여일</TableCell>
                    <TableCell>상태</TableCell>
                    <TableCell align="right">현재 잔고</TableCell>
                    <TableCell align="right">손익</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {participants.map((participant) => (
                    <TableRow key={participant.id}>
                      <TableCell>{participant.rank}</TableCell>
                      <TableCell>
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {participant.username}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {participant.email}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        {new Date(participant.joinedAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={
                            participant.status === 'ACTIVE' ? '참여중' :
                            participant.status === 'COMPLETED' ? '완료' : '포기'
                          }
                          color={
                            participant.status === 'ACTIVE' ? 'primary' :
                            participant.status === 'COMPLETED' ? 'success' : 'default'
                          }
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        {participant.currentBalance.toLocaleString()}원
                      </TableCell>
                      <TableCell align="right">
                        <Typography
                          color={participant.profitLoss >= 0 ? 'success.main' : 'error.main'}
                          fontWeight="medium"
                        >
                          {participant.profitLoss >= 0 ? '+' : ''}{participant.profitLoss.toLocaleString()}원
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      </TabPanel>

      {/* 설정 및 관리 탭 */}
      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  생성 정보
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemText
                      primary="생성자"
                      secondary={challenge.createdBy}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="생성일"
                      secondary={new Date(challenge.createdAt!).toLocaleString()}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="최종 수정일"
                      secondary={new Date(challenge.updatedAt!).toLocaleString()}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  관리 작업
                </Typography>
                <Box display="flex" flexDirection="column" gap={2}>
                  <Button
                    variant="outlined"
                    startIcon={<Assessment />}
                    fullWidth
                  >
                    성과 분석 보기
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<People />}
                    fullWidth
                  >
                    참여자 관리
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<Settings />}
                    fullWidth
                  >
                    상세 설정
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>챌린지 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            &ldquo;{challenge.title}&rdquo; 챌린지를 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            참여 중인 사용자가 있으면 삭제할 수 없습니다. 이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            취소
          </Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}