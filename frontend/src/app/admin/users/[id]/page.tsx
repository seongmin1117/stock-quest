'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Avatar,
  Chip,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tab,
  Tabs,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import {
  ArrowBack,
  Edit,
  Block,
  Delete,
  Email,
  CalendarToday,
  AccessTime,
  TrendingUp,
  TrendingDown,
  Assessment,
  Security,
  Warning,
  CheckCircle,
  Person,
  AdminPanelSettings,
  VerifiedUser,
  EmojiEvents,
  AccountBalance,
  Timeline
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';

interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'USER';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  lastLoginAt?: string;
  totalChallenges: number;
  completedChallenges: number;
  averageReturn: number;
  riskScore: number;
  totalAssets: number;
  winRate: number;
  maxDrawdown: number;
  avatar?: string;
}

interface ChallengeSession {
  id: number;
  challengeName: string;
  status: 'ACTIVE' | 'COMPLETED' | 'FAILED';
  startedAt: string;
  completedAt?: string;
  initialBalance: number;
  currentBalance: number;
  returnRate: number;
  rank?: number;
  totalParticipants?: number;
}

interface ActivityLog {
  id: number;
  type: 'LOGIN' | 'LOGOUT' | 'CHALLENGE_START' | 'CHALLENGE_COMPLETE' | 'ORDER_PLACED';
  description: string;
  timestamp: string;
  metadata?: Record<string, any>;
}

const mockUser: User = {
  id: 1,
  username: 'john_investor',
  email: 'john@example.com',
  fullName: '김투자',
  role: 'USER',
  status: 'ACTIVE',
  createdAt: '2024-02-01T10:15:00Z',
  lastLoginAt: '2024-09-13T16:45:00Z',
  totalChallenges: 15,
  completedChallenges: 12,
  averageReturn: 8.5,
  riskScore: 3.2,
  totalAssets: 1250000,
  winRate: 75.5,
  maxDrawdown: -12.3
};

const mockChallengeSessions: ChallengeSession[] = [
  {
    id: 1,
    challengeName: '2020년 코로나 시장 급락 대응',
    status: 'COMPLETED',
    startedAt: '2024-09-01T09:00:00Z',
    completedAt: '2024-09-10T18:00:00Z',
    initialBalance: 100000,
    currentBalance: 118500,
    returnRate: 18.5,
    rank: 3,
    totalParticipants: 45
  },
  {
    id: 2,
    challengeName: '2017-2021 장기 상승장 전략',
    status: 'ACTIVE',
    startedAt: '2024-09-12T10:30:00Z',
    initialBalance: 100000,
    currentBalance: 103200,
    returnRate: 3.2
  },
  {
    id: 3,
    challengeName: '고변동성 시장 대응 전략',
    status: 'FAILED',
    startedAt: '2024-08-15T14:00:00Z',
    completedAt: '2024-08-25T16:30:00Z',
    initialBalance: 100000,
    currentBalance: 87500,
    returnRate: -12.5,
    rank: 32,
    totalParticipants: 38
  }
];

const mockActivityLogs: ActivityLog[] = [
  {
    id: 1,
    type: 'LOGIN',
    description: '사용자가 로그인했습니다',
    timestamp: '2024-09-14T08:30:00Z'
  },
  {
    id: 2,
    type: 'ORDER_PLACED',
    description: 'AAPL 주식 100주 매수 주문',
    timestamp: '2024-09-13T14:22:00Z',
    metadata: { symbol: 'AAPL', quantity: 100, type: 'BUY' }
  },
  {
    id: 3,
    type: 'CHALLENGE_COMPLETE',
    description: '2020년 코로나 시장 급락 대응 챌린지 완료',
    timestamp: '2024-09-10T18:00:00Z'
  }
];

export default function UserDetailPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [challengeSessions] = useState<ChallengeSession[]>(mockChallengeSessions);
  const [activityLogs] = useState<ActivityLog[]>(mockActivityLogs);
  const [loading, setLoading] = useState(true);
  const [currentTab, setCurrentTab] = useState(0);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    message: string;
    action?: () => void;
  }>({ open: false, title: '', message: '' });

  useEffect(() => {
    const loadUser = async () => {
      try {
        // TODO: 실제 API 호출
        await new Promise(resolve => setTimeout(resolve, 500));
        setUser(mockUser);
      } catch (error) {
        console.error('Failed to load user:', error);
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, [params.id]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>사용자 정보를 불러오는 중...</Typography>
      </Box>
    );
  }

  if (!user) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>사용자를 찾을 수 없습니다.</Typography>
      </Box>
    );
  }

  const getRoleChip = (role: User['role']) => {
    const config = {
      ADMIN: { label: '관리자', color: 'error' as const, icon: <AdminPanelSettings /> },
      USER: { label: '사용자', color: 'primary' as const, icon: <Person /> }
    };

    return (
      <Chip
        icon={config[role].icon}
        label={config[role].label}
        color={config[role].color}
        size="small"
        variant="outlined"
      />
    );
  };

  const getStatusChip = (status: User['status']) => {
    const config = {
      ACTIVE: { label: '활성', color: 'success' as const, icon: <VerifiedUser /> },
      INACTIVE: { label: '비활성', color: 'default' as const, icon: <Person /> },
      SUSPENDED: { label: '정지', color: 'error' as const, icon: <Warning /> }
    };

    return (
      <Chip
        icon={config[status].icon}
        label={config[status].label}
        color={config[status].color}
        size="medium"
        variant="filled"
      />
    );
  };

  const getChallengeStatusChip = (status: ChallengeSession['status']) => {
    const config = {
      ACTIVE: { label: '진행중', color: 'primary' as const, icon: <Timeline /> },
      COMPLETED: { label: '완료', color: 'success' as const, icon: <CheckCircle /> },
      FAILED: { label: '실패', color: 'error' as const, icon: <Warning /> }
    };

    return (
      <Chip
        icon={config[status].icon}
        label={config[status].label}
        color={config[status].color}
        size="small"
        variant="filled"
      />
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatDateTime = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
  };

  const handleSuspendUser = () => {
    setConfirmDialog({
      open: true,
      title: '사용자 정지',
      message: `${user.fullName}(${user.username}) 사용자를 정지하시겠습니까?`,
      action: () => {
        // TODO: 실제 API 호출
        console.log('Suspending user:', user.id);
        setConfirmDialog({ open: false, title: '', message: '' });
      }
    });
  };

  const handleDeleteUser = () => {
    setConfirmDialog({
      open: true,
      title: '사용자 삭제',
      message: `${user.fullName}(${user.username}) 사용자를 영구 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`,
      action: () => {
        // TODO: 실제 API 호출
        console.log('Deleting user:', user.id);
        setConfirmDialog({ open: false, title: '', message: '' });
        router.push('/admin/users');
      }
    });
  };

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/users')}
          sx={{ mb: 2 }}
        >
          사용자 목록으로
        </Button>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
              사용자 상세 정보
            </Typography>
            <Typography variant="body1" color="text.secondary">
              {user.fullName}님의 계정 정보와 활동 내역을 확인하세요
            </Typography>
          </Box>
          <Box display="flex" gap={1}>
            <Button
              variant="outlined"
              startIcon={<Edit />}
              onClick={() => router.push(`/admin/users/${user.id}/edit`)}
            >
              수정
            </Button>
            <Button
              variant="outlined"
              color="warning"
              startIcon={<Block />}
              onClick={handleSuspendUser}
            >
              정지
            </Button>
            <Button
              variant="outlined"
              color="error"
              startIcon={<Delete />}
              onClick={handleDeleteUser}
            >
              삭제
            </Button>
          </Box>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* 사용자 기본 정보 */}
        <Grid item xs={12} md={4}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box display="flex" flexDirection="column" alignItems="center" textAlign="center">
                <Avatar
                  src={user.avatar}
                  sx={{
                    width: 120,
                    height: 120,
                    fontSize: 48,
                    bgcolor: user.role === 'ADMIN' ? 'error.main' : 'primary.main',
                    mb: 2
                  }}
                >
                  {user.fullName[0]}
                </Avatar>

                <Typography variant="h5" gutterBottom>
                  {user.fullName}
                </Typography>

                <Typography variant="body1" color="text.secondary" gutterBottom>
                  @{user.username}
                </Typography>

                <Box display="flex" gap={1} mb={3}>
                  {getRoleChip(user.role)}
                  {getStatusChip(user.status)}
                </Box>

                <Divider sx={{ width: '100%', my: 2 }} />

                <List sx={{ width: '100%' }}>
                  <ListItem>
                    <ListItemIcon>
                      <Email color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="이메일"
                      secondary={user.email}
                      secondaryTypographyProps={{ sx: { wordBreak: 'break-word' } }}
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <CalendarToday color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="가입일"
                      secondary={formatDate(user.createdAt)}
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <AccessTime color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="마지막 접속"
                      secondary={formatDateTime(user.lastLoginAt)}
                    />
                  </ListItem>
                </List>
              </Box>
            </CardContent>
          </Card>

          {/* 투자 통계 */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                투자 통계
              </Typography>

              <Box mb={2}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  평균 수익률
                </Typography>
                <Box display="flex" alignItems="center" gap={1}>
                  {user.averageReturn > 0 ? (
                    <TrendingUp color="success" />
                  ) : (
                    <TrendingDown color="error" />
                  )}
                  <Typography
                    variant="h6"
                    color={user.averageReturn > 0 ? 'success.main' : 'error.main'}
                  >
                    {user.averageReturn > 0 ? '+' : ''}{user.averageReturn}%
                  </Typography>
                </Box>
              </Box>

              <Box mb={2}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  승률
                </Typography>
                <Typography variant="h6">
                  {user.winRate}%
                </Typography>
              </Box>

              <Box mb={2}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  최대 손실률
                </Typography>
                <Typography variant="h6" color="error.main">
                  {user.maxDrawdown}%
                </Typography>
              </Box>

              <Box mb={2}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  리스크 점수
                </Typography>
                <Typography
                  variant="h6"
                  color={
                    user.riskScore > 4 ? 'error.main' :
                    user.riskScore > 2 ? 'warning.main' :
                    'success.main'
                  }
                >
                  {user.riskScore.toFixed(1)}
                </Typography>
              </Box>

              <Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  총 자산
                </Typography>
                <Typography variant="h6" color="primary.main">
                  {formatCurrency(user.totalAssets)}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* 상세 정보 탭 */}
        <Grid item xs={12} md={8}>
          <Card>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
              <Tabs
                value={currentTab}
                onChange={(_, newValue) => setCurrentTab(newValue)}
                aria-label="사용자 상세 정보 탭"
              >
                <Tab label="챌린지 내역" icon={<EmojiEvents />} iconPosition="start" />
                <Tab label="활동 로그" icon={<Assessment />} iconPosition="start" />
                <Tab label="계정 설정" icon={<Security />} iconPosition="start" />
              </Tabs>
            </Box>

            <CardContent>
              {/* 챌린지 내역 탭 */}
              {currentTab === 0 && (
                <Box>
                  <Box mb={3}>
                    <Typography variant="h6" gutterBottom>
                      참여한 챌린지 ({user.totalChallenges}개)
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      완료: {user.completedChallenges}개 / 진행중: {user.totalChallenges - user.completedChallenges}개
                    </Typography>
                  </Box>

                  <TableContainer component={Paper} variant="outlined">
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>챌린지명</TableCell>
                          <TableCell>상태</TableCell>
                          <TableCell>시작일</TableCell>
                          <TableCell>완료일</TableCell>
                          <TableCell align="right">수익률</TableCell>
                          <TableCell align="center">순위</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {challengeSessions.map((session) => (
                          <TableRow key={session.id} hover>
                            <TableCell>
                              <Typography variant="subtitle2">
                                {session.challengeName}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {formatCurrency(session.initialBalance)} → {formatCurrency(session.currentBalance)}
                              </Typography>
                            </TableCell>

                            <TableCell>
                              {getChallengeStatusChip(session.status)}
                            </TableCell>

                            <TableCell>
                              {formatDate(session.startedAt)}
                            </TableCell>

                            <TableCell>
                              {session.completedAt ? formatDate(session.completedAt) : '-'}
                            </TableCell>

                            <TableCell align="right">
                              <Box display="flex" alignItems="center" justifyContent="flex-end" gap={0.5}>
                                {session.returnRate > 0 ? (
                                  <TrendingUp color="success" fontSize="small" />
                                ) : session.returnRate < 0 ? (
                                  <TrendingDown color="error" fontSize="small" />
                                ) : null}
                                <Typography
                                  variant="body2"
                                  color={
                                    session.returnRate > 0 ? 'success.main' :
                                    session.returnRate < 0 ? 'error.main' :
                                    'text.primary'
                                  }
                                >
                                  {session.returnRate > 0 ? '+' : ''}{session.returnRate}%
                                </Typography>
                              </Box>
                            </TableCell>

                            <TableCell align="center">
                              {session.rank && session.totalParticipants ? (
                                <Typography variant="body2">
                                  {session.rank}/{session.totalParticipants}
                                </Typography>
                              ) : (
                                '-'
                              )}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Box>
              )}

              {/* 활동 로그 탭 */}
              {currentTab === 1 && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    최근 활동 내역
                  </Typography>

                  <List>
                    {activityLogs.map((log, index) => (
                      <React.Fragment key={log.id}>
                        <ListItem alignItems="flex-start">
                          <ListItemIcon>
                            {log.type === 'LOGIN' && <Security color="success" />}
                            {log.type === 'LOGOUT' && <Security color="default" />}
                            {log.type === 'CHALLENGE_START' && <EmojiEvents color="primary" />}
                            {log.type === 'CHALLENGE_COMPLETE' && <CheckCircle color="success" />}
                            {log.type === 'ORDER_PLACED' && <AccountBalance color="primary" />}
                          </ListItemIcon>
                          <ListItemText
                            primary={log.description}
                            secondary={formatDateTime(log.timestamp)}
                          />
                        </ListItem>
                        {index < activityLogs.length - 1 && <Divider variant="inset" component="li" />}
                      </React.Fragment>
                    ))}
                  </List>
                </Box>
              )}

              {/* 계정 설정 탭 */}
              {currentTab === 2 && (
                <Box>
                  <Typography variant="h6" gutterBottom>
                    계정 설정 및 권한
                  </Typography>

                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="subtitle2" gutterBottom>
                        사용자 역할
                      </Typography>
                      {getRoleChip(user.role)}
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <Typography variant="subtitle2" gutterBottom>
                        계정 상태
                      </Typography>
                      {getStatusChip(user.status)}
                    </Grid>

                    <Grid item xs={12}>
                      <Typography variant="subtitle2" gutterBottom>
                        계정 생성일
                      </Typography>
                      <Typography variant="body2">
                        {formatDateTime(user.createdAt)}
                      </Typography>
                    </Grid>

                    <Grid item xs={12}>
                      <Typography variant="subtitle2" gutterBottom>
                        계정 ID
                      </Typography>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {user.id}
                      </Typography>
                    </Grid>
                  </Grid>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 확인 다이얼로그 */}
      <Dialog
        open={confirmDialog.open}
        onClose={() => setConfirmDialog({ open: false, title: '', message: '' })}
      >
        <DialogTitle>{confirmDialog.title}</DialogTitle>
        <DialogContent>
          <Typography>{confirmDialog.message}</Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setConfirmDialog({ open: false, title: '', message: '' })}
          >
            취소
          </Button>
          <Button
            onClick={confirmDialog.action}
            color="error"
            variant="contained"
          >
            확인
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}