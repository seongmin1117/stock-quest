'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Avatar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Menu,
  ListItemIcon,
  ListItemText,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Switch,
  FormControlLabel
} from '@mui/material';
import {
  Add,
  Search,
  FilterList,
  MoreVert,
  Edit,
  Block,
  Delete,
  PersonAdd,
  AdminPanelSettings,
  Person,
  TrendingUp,
  TrendingDown,
  Group,
  Security,
  VerifiedUser,
  Warning
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
  avatar?: string;
}

const mockUsers: User[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@stockquest.com',
    fullName: '시스템 관리자',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: '2024-01-15T09:00:00Z',
    lastLoginAt: '2024-09-14T08:30:00Z',
    totalChallenges: 0,
    completedChallenges: 0,
    averageReturn: 0,
    riskScore: 0
  },
  {
    id: 2,
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
    riskScore: 3.2
  },
  {
    id: 3,
    username: 'sarah_trader',
    email: 'sarah@example.com',
    fullName: '박트레이더',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: '2024-01-28T14:22:00Z',
    lastLoginAt: '2024-09-14T09:15:00Z',
    totalChallenges: 23,
    completedChallenges: 20,
    averageReturn: 12.3,
    riskScore: 4.1
  },
  {
    id: 4,
    username: 'mike_beginner',
    email: 'mike@example.com',
    fullName: '이초보',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: '2024-03-10T11:30:00Z',
    lastLoginAt: '2024-09-12T13:20:00Z',
    totalChallenges: 5,
    completedChallenges: 3,
    averageReturn: -2.1,
    riskScore: 2.8
  },
  {
    id: 5,
    username: 'inactive_user',
    email: 'inactive@example.com',
    fullName: '비활성사용자',
    role: 'USER',
    status: 'INACTIVE',
    createdAt: '2024-02-15T16:45:00Z',
    lastLoginAt: '2024-08-01T10:00:00Z',
    totalChallenges: 8,
    completedChallenges: 4,
    averageReturn: 3.2,
    riskScore: 1.9
  },
  {
    id: 6,
    username: 'suspended_user',
    email: 'suspended@example.com',
    fullName: '정지사용자',
    role: 'USER',
    status: 'SUSPENDED',
    createdAt: '2024-01-20T12:10:00Z',
    lastLoginAt: '2024-07-15T14:30:00Z',
    totalChallenges: 12,
    completedChallenges: 6,
    averageReturn: -5.8,
    riskScore: 6.2
  }
];

export default function UsersPage() {
  const router = useRouter();
  const [users] = useState<User[]>(mockUsers);
  const [filteredUsers, setFilteredUsers] = useState<User[]>(mockUsers);
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<'ALL' | 'ADMIN' | 'USER'>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'>('ALL');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [bulkSelectMode, setBulkSelectMode] = useState(false);
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    message: string;
    action?: () => void;
  }>({ open: false, title: '', message: '' });

  // Filter users based on search query, role, and status
  React.useEffect(() => {
    let filtered = users.filter(user => {
      const matchesSearch =
        user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
        user.fullName.toLowerCase().includes(searchQuery.toLowerCase());

      const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
      const matchesStatus = statusFilter === 'ALL' || user.status === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });

    setFilteredUsers(filtered);
  }, [users, searchQuery, roleFilter, statusFilter]);

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>, userId: number) => {
    setAnchorEl(event.currentTarget);
    setSelectedUserId(userId);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedUserId(null);
  };

  const handleEditUser = (userId: number) => {
    handleMenuClose();
    router.push(`/admin/users/${userId}/edit`);
  };

  const handleViewUser = (userId: number) => {
    router.push(`/admin/users/${userId}`);
  };

  const handleSuspendUser = (userId: number) => {
    const user = users.find(u => u.id === userId);
    setConfirmDialog({
      open: true,
      title: '사용자 정지',
      message: `${user?.fullName}(${user?.username}) 사용자를 정지하시겠습니까?`,
      action: () => {
        // TODO: 실제 API 호출
        console.log('Suspending user:', userId);
        setConfirmDialog({ open: false, title: '', message: '' });
      }
    });
    handleMenuClose();
  };

  const handleDeleteUser = (userId: number) => {
    const user = users.find(u => u.id === userId);
    setConfirmDialog({
      open: true,
      title: '사용자 삭제',
      message: `${user?.fullName}(${user?.username}) 사용자를 영구 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`,
      action: () => {
        // TODO: 실제 API 호출
        console.log('Deleting user:', userId);
        setConfirmDialog({ open: false, title: '', message: '' });
      }
    });
    handleMenuClose();
  };

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
        size="small"
        variant="filled"
      />
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
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

  // Calculate statistics
  const totalUsers = users.length;
  const activeUsers = users.filter(u => u.status === 'ACTIVE').length;
  const adminUsers = users.filter(u => u.role === 'ADMIN').length;
  const suspendedUsers = users.filter(u => u.status === 'SUSPENDED').length;

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          사용자 관리
        </Typography>
        <Typography variant="body1" color="text.secondary">
          시스템 사용자를 관리하고 역할 및 권한을 설정하세요
        </Typography>
      </Box>

      {/* 통계 카드 */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="primary.main">
                    {totalUsers}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 사용자
                  </Typography>
                </Box>
                <Group color="primary" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="success.main">
                    {activeUsers}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    활성 사용자
                  </Typography>
                </Box>
                <VerifiedUser color="success" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="error.main">
                    {adminUsers}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    관리자
                  </Typography>
                </Box>
                <Security color="error" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box>
                  <Typography variant="h4" color="warning.main">
                    {suspendedUsers}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    정지된 사용자
                  </Typography>
                </Box>
                <Warning color="warning" sx={{ fontSize: 40, opacity: 0.7 }} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* 검색 및 필터 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={3} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                placeholder="사용자 이름, 이메일로 검색"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />
                }}
              />
            </Grid>

            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>역할</InputLabel>
                <Select
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value as typeof roleFilter)}
                  label="역할"
                >
                  <MenuItem value="ALL">모든 역할</MenuItem>
                  <MenuItem value="ADMIN">관리자</MenuItem>
                  <MenuItem value="USER">사용자</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>상태</InputLabel>
                <Select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as typeof statusFilter)}
                  label="상태"
                >
                  <MenuItem value="ALL">모든 상태</MenuItem>
                  <MenuItem value="ACTIVE">활성</MenuItem>
                  <MenuItem value="INACTIVE">비활성</MenuItem>
                  <MenuItem value="SUSPENDED">정지</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={2}>
              <Box display="flex" gap={1}>
                <Button
                  variant="contained"
                  startIcon={<PersonAdd />}
                  onClick={() => router.push('/admin/users/new')}
                  fullWidth
                >
                  사용자 추가
                </Button>
              </Box>
            </Grid>
          </Grid>

          <Box mt={2} display="flex" alignItems="center" justifyContent="space-between">
            <FormControlLabel
              control={
                <Switch
                  checked={bulkSelectMode}
                  onChange={(e) => setBulkSelectMode(e.target.checked)}
                />
              }
              label="일괄 선택 모드"
            />
            <Typography variant="body2" color="text.secondary">
              {filteredUsers.length}명의 사용자가 검색되었습니다
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* 사용자 테이블 */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>사용자 정보</TableCell>
                <TableCell>역할</TableCell>
                <TableCell>상태</TableCell>
                <TableCell>가입일</TableCell>
                <TableCell>마지막 접속</TableCell>
                <TableCell>챌린지 통계</TableCell>
                <TableCell>수익률</TableCell>
                <TableCell>리스크</TableCell>
                <TableCell align="center">작업</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredUsers.map((user) => (
                <TableRow
                  key={user.id}
                  hover
                  sx={{ cursor: 'pointer' }}
                  onClick={() => handleViewUser(user.id)}
                >
                  <TableCell>
                    <Box display="flex" alignItems="center" gap={2}>
                      <Avatar
                        src={user.avatar}
                        sx={{
                          bgcolor: user.role === 'ADMIN' ? 'error.main' : 'primary.main',
                          width: 40,
                          height: 40
                        }}
                      >
                        {user.fullName[0]}
                      </Avatar>
                      <Box>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {user.fullName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          @{user.username}
                        </Typography>
                        <br />
                        <Typography variant="caption" color="text.secondary">
                          {user.email}
                        </Typography>
                      </Box>
                    </Box>
                  </TableCell>

                  <TableCell>
                    {getRoleChip(user.role)}
                  </TableCell>

                  <TableCell>
                    {getStatusChip(user.status)}
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {formatDate(user.createdAt)}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {formatDateTime(user.lastLoginAt)}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <Box>
                      <Typography variant="body2">
                        전체: {user.totalChallenges}개
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        완료: {user.completedChallenges}개
                      </Typography>
                    </Box>
                  </TableCell>

                  <TableCell>
                    <Box display="flex" alignItems="center" gap={1}>
                      {user.averageReturn > 0 ? (
                        <TrendingUp color="success" fontSize="small" />
                      ) : user.averageReturn < 0 ? (
                        <TrendingDown color="error" fontSize="small" />
                      ) : (
                        <Typography variant="body2">-</Typography>
                      )}
                      <Typography
                        variant="body2"
                        color={
                          user.averageReturn > 0 ? 'success.main' :
                          user.averageReturn < 0 ? 'error.main' :
                          'text.primary'
                        }
                      >
                        {user.averageReturn > 0 ? '+' : ''}{user.averageReturn}%
                      </Typography>
                    </Box>
                  </TableCell>

                  <TableCell>
                    <Typography
                      variant="body2"
                      color={
                        user.riskScore > 4 ? 'error.main' :
                        user.riskScore > 2 ? 'warning.main' :
                        'success.main'
                      }
                    >
                      {user.riskScore.toFixed(1)}
                    </Typography>
                  </TableCell>

                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleMenuClick(e, user.id);
                      }}
                    >
                      <MoreVert />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {filteredUsers.length === 0 && (
          <Box display="flex" justifyContent="center" alignItems="center" p={8}>
            <Box textAlign="center">
              <Group sx={{ fontSize: 64, color: 'text.disabled', mb: 2 }} />
              <Typography variant="h6" color="text.secondary" gutterBottom>
                검색 결과가 없습니다
              </Typography>
              <Typography variant="body2" color="text.secondary">
                검색 조건을 변경해보세요
              </Typography>
            </Box>
          </Box>
        )}
      </Card>

      {/* 액션 메뉴 */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => selectedUserId && handleEditUser(selectedUserId)}>
          <ListItemIcon>
            <Edit fontSize="small" />
          </ListItemIcon>
          <ListItemText>수정</ListItemText>
        </MenuItem>

        <Divider />

        <MenuItem
          onClick={() => selectedUserId && handleSuspendUser(selectedUserId)}
          sx={{ color: 'warning.main' }}
        >
          <ListItemIcon>
            <Block fontSize="small" color="warning" />
          </ListItemIcon>
          <ListItemText>정지</ListItemText>
        </MenuItem>

        <MenuItem
          onClick={() => selectedUserId && handleDeleteUser(selectedUserId)}
          sx={{ color: 'error.main' }}
        >
          <ListItemIcon>
            <Delete fontSize="small" color="error" />
          </ListItemIcon>
          <ListItemText>삭제</ListItemText>
        </MenuItem>
      </Menu>

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