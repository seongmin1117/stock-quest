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

// 임시 타입 정의
interface Challenge {
  id: number;
  title: string;
  description: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  challengeType: 'PRACTICE' | 'COMPETITION' | 'GUIDED';
  status: 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  initialBalance: number;
  durationDays: number;
  estimatedDurationMinutes: number;
  startDate: string;
  endDate: string;
  currentParticipants: number;
  maxParticipants?: number;
  tags: string[];
  isFeatured: boolean;
  learningObjectives?: string;
  marketScenario?: string;
  successCriteria?: any;
  availableInstruments?: string[];
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

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

// 임시 데이터
const mockChallenge: Challenge = {
  id: 1,
  title: '2020년 코로나 시장 급락',
  description: '코로나19 팬데믹 초기 시장 상황에서의 투자 전략을 학습합니다. 2020년 2월부터 4월까지의 급격한 시장 변동성 속에서 포트폴리오를 관리하고 리스크를 제어하는 방법을 배웁니다.',
  difficulty: 'INTERMEDIATE',
  challengeType: 'PRACTICE',
  status: 'ACTIVE',
  initialBalance: 100000,
  durationDays: 30,
  estimatedDurationMinutes: 45,
  startDate: '2024-01-15T00:00:00',
  endDate: '2024-02-15T23:59:59',
  currentParticipants: 45,
  maxParticipants: 100,
  tags: ['market-crash', 'volatility', 'pandemic', 'risk-management'],
  isFeatured: true,
  learningObjectives: '• 시장 급락 상황에서의 심리적 대응\n• 포트폴리오 리밸런싱 전략\n• 방어주 선택 기준\n• 손실 제한 기법',
  marketScenario: '2020년 2월 19일부터 3월 23일까지 S&P 500이 34% 급락한 기간을 시뮬레이션합니다.',
  availableInstruments: ['STOCKS', 'ETF', 'BONDS', 'CASH'],
  createdBy: 'admin@stockquest.com',
  createdAt: '2024-01-01T09:00:00',
  updatedAt: '2024-01-10T14:30:00'
};

const mockParticipants: Participant[] = [
  {
    id: 1,
    username: 'trader_kim',
    email: 'kim@example.com',
    joinedAt: '2024-01-15T10:30:00',
    status: 'ACTIVE',
    currentBalance: 98500,
    profitLoss: -1500,
    rank: 1
  },
  {
    id: 2,
    username: 'investor_lee',
    email: 'lee@example.com',
    joinedAt: '2024-01-15T11:15:00',
    status: 'ACTIVE',
    currentBalance: 95000,
    profitLoss: -5000,
    rank: 2
  },
  {
    id: 3,
    username: 'portfolio_master',
    email: 'master@example.com',
    joinedAt: '2024-01-16T09:00:00',
    status: 'COMPLETED',
    currentBalance: 105000,
    profitLoss: 5000,
    rank: 1
  }
];

const difficultyColors = {
  BEGINNER: 'success',
  INTERMEDIATE: 'warning',
  ADVANCED: 'error'
} as const;

const statusColors = {
  DRAFT: 'default',
  ACTIVE: 'primary',
  COMPLETED: 'success',
  ARCHIVED: 'secondary'
} as const;

const typeColors = {
  PRACTICE: 'info',
  COMPETITION: 'warning',
  GUIDED: 'success'
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
  const challengeId = params.id as string;

  const [challenge, setChallenge] = useState<Challenge | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  // 액션 다이얼로그 상태
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    // TODO: 실제 API 호출로 대체
    const fetchChallenge = async () => {
      try {
        setLoading(true);
        // 임시 데이터 로딩 시뮬레이션
        await new Promise(resolve => setTimeout(resolve, 500));

        if (challengeId === '1') {
          setChallenge(mockChallenge);
          setParticipants(mockParticipants);
        } else {
          setError('챌린지를 찾을 수 없습니다.');
        }
      } catch (err) {
        setError('챌린지 정보를 불러오는 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchChallenge();
  }, [challengeId]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleStatusChange = (newStatus: Challenge['status']) => {
    if (challenge) {
      // TODO: API 호출
      setChallenge({ ...challenge, status: newStatus });
    }
  };

  const toggleFeatured = () => {
    if (challenge) {
      // TODO: API 호출
      setChallenge({ ...challenge, isFeatured: !challenge.isFeatured });
    }
  };

  const handleDelete = () => {
    // TODO: API 호출로 삭제
    setDeleteDialogOpen(false);
    router.push('/admin/challenges');
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
            {challenge.isFeatured && (
              <Star sx={{ color: 'gold', ml: 1, fontSize: 30 }} />
            )}
          </Typography>
          <Box display="flex" gap={1} mb={2}>
            <Chip
              label={challenge.status === 'DRAFT' ? '초안' :
                    challenge.status === 'ACTIVE' ? '진행중' :
                    challenge.status === 'COMPLETED' ? '완료' : '보관'}
              color={statusColors[challenge.status]}
            />
            <Chip
              label={challenge.difficulty === 'BEGINNER' ? '초급' :
                    challenge.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
              color={difficultyColors[challenge.difficulty]}
            />
            <Chip
              label={challenge.challengeType === 'PRACTICE' ? '연습' :
                    challenge.challengeType === 'COMPETITION' ? '경쟁' : '가이드'}
              color={typeColors[challenge.challengeType]}
            />
          </Box>
        </Box>

        {/* 액션 버튼들 */}
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={challenge.isFeatured ? <StarBorder /> : <Star />}
            onClick={toggleFeatured}
          >
            {challenge.isFeatured ? '피처드 해제' : '피처드 설정'}
          </Button>
          <Button
            variant="outlined"
            startIcon={<Edit />}
            href={`/admin/challenges/${challengeId}/edit`}
          >
            수정
          </Button>
          {challenge.status === 'DRAFT' && (
            <Button
              variant="contained"
              startIcon={<PlayArrow />}
              onClick={() => handleStatusChange('ACTIVE')}
            >
              활성화
            </Button>
          )}
          {challenge.status === 'ACTIVE' && (
            <Button
              variant="outlined"
              startIcon={<Stop />}
              onClick={() => handleStatusChange('COMPLETED')}
            >
              완료처리
            </Button>
          )}
          <Button
            variant="outlined"
            startIcon={<Archive />}
            onClick={() => handleStatusChange('ARCHIVED')}
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
            {challenge.marketScenario && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    시장 시나리오
                  </Typography>
                  <Typography variant="body1">
                    {challenge.marketScenario}
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
                  {challenge.tags.map((tag) => (
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
                      secondary={`${challenge.durationDays}일 (예상: ${challenge.estimatedDurationMinutes}분)`}
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
                      value={(challenge.currentParticipants / challenge.maxParticipants) * 100}
                      sx={{ mb: 2 }}
                    />
                    <Typography variant="body2" color="text.secondary">
                      {Math.round((challenge.currentParticipants / challenge.maxParticipants) * 100)}% 달성
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
            "{challenge.title}" 챌린지를 정말 삭제하시겠습니까?
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