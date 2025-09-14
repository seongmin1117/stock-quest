'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  TextField,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  TablePagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Skeleton,
  IconButton,
  Menu
} from '@mui/material';
import {
  Add,
  FilterList,
  Search,
  Edit,
  Delete,
  PlayArrow,
  Stop,
  Archive,
  Star,
  StarBorder,
  MoreVert,
  Visibility
} from '@mui/icons-material';

// 임시 타입 정의 (향후 API 타입으로 대체)
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
}

// 임시 데이터
const mockChallenges: Challenge[] = [
  {
    id: 1,
    title: '2020년 코로나 시장 급락',
    description: '코로나19 팬데믹 초기 시장 상황에서의 투자 전략을 학습합니다.',
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
    tags: ['market-crash', 'volatility', 'pandemic'],
    isFeatured: true
  },
  {
    id: 2,
    title: '2021년 밈주식 광풍',
    description: 'GameStop, AMC 등 밈주식 현상을 통한 시장 동향 이해',
    difficulty: 'ADVANCED',
    challengeType: 'COMPETITION',
    status: 'ACTIVE',
    initialBalance: 50000,
    durationDays: 14,
    estimatedDurationMinutes: 60,
    startDate: '2024-01-01T00:00:00',
    endDate: '2024-01-15T23:59:59',
    currentParticipants: 32,
    maxParticipants: 50,
    tags: ['meme-stocks', 'social-trading', 'volatility'],
    isFeatured: false
  },
  {
    id: 3,
    title: '2022년 인플레이션 우려',
    description: '금리 인상기 포트폴리오 관리 전략을 학습합니다.',
    difficulty: 'BEGINNER',
    challengeType: 'GUIDED',
    status: 'COMPLETED',
    initialBalance: 75000,
    durationDays: 21,
    estimatedDurationMinutes: 30,
    startDate: '2023-12-01T00:00:00',
    endDate: '2023-12-22T23:59:59',
    currentParticipants: 28,
    maxParticipants: 40,
    tags: ['inflation', 'interest-rates', 'bonds'],
    isFeatured: false
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

export default function ChallengesPage() {
  const [challenges, setChallenges] = useState<Challenge[]>(mockChallenges);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 검색 및 필터링 상태
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [difficultyFilter, setDifficultyFilter] = useState<string>('all');
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [featuredFilter, setFeaturedFilter] = useState<string>('all');

  // 페이지네이션 상태
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(12);

  // 액션 메뉴 상태
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedChallenge, setSelectedChallenge] = useState<Challenge | null>(null);

  // 삭제 확인 다이얼로그
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [challengeToDelete, setChallengeToDelete] = useState<Challenge | null>(null);

  // 필터링된 챌린지 계산
  const filteredChallenges = challenges.filter(challenge => {
    const matchesSearch = challenge.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         challenge.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         challenge.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesStatus = statusFilter === 'all' || challenge.status === statusFilter;
    const matchesDifficulty = difficultyFilter === 'all' || challenge.difficulty === difficultyFilter;
    const matchesType = typeFilter === 'all' || challenge.challengeType === typeFilter;
    const matchesFeatured = featuredFilter === 'all' ||
                           (featuredFilter === 'featured' && challenge.isFeatured) ||
                           (featuredFilter === 'not-featured' && !challenge.isFeatured);

    return matchesSearch && matchesStatus && matchesDifficulty && matchesType && matchesFeatured;
  });

  // 페이지네이션 적용
  const paginatedChallenges = filteredChallenges.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const handlePageChange = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleRowsPerPageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, challenge: Challenge) => {
    setAnchorEl(event.currentTarget);
    setSelectedChallenge(challenge);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedChallenge(null);
  };

  const handleDelete = (challenge: Challenge) => {
    setChallengeToDelete(challenge);
    setDeleteDialogOpen(true);
    handleMenuClose();
  };

  const confirmDelete = () => {
    if (challengeToDelete) {
      // TODO: API 호출로 챌린지 삭제
      setChallenges(challenges.filter(c => c.id !== challengeToDelete.id));
      setDeleteDialogOpen(false);
      setChallengeToDelete(null);
    }
  };

  const toggleFeatured = (challenge: Challenge) => {
    // TODO: API 호출로 피처드 상태 변경
    setChallenges(challenges.map(c =>
      c.id === challenge.id
        ? { ...c, isFeatured: !c.isFeatured }
        : c
    ));
    handleMenuClose();
  };

  const changeStatus = (challenge: Challenge, newStatus: Challenge['status']) => {
    // TODO: API 호출로 상태 변경
    setChallenges(challenges.map(c =>
      c.id === challenge.id
        ? { ...c, status: newStatus }
        : c
    ));
    handleMenuClose();
  };

  // 필터 초기화
  const resetFilters = () => {
    setSearchQuery('');
    setStatusFilter('all');
    setDifficultyFilter('all');
    setTypeFilter('all');
    setFeaturedFilter('all');
    setPage(0);
  };

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
            챌린지 관리
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {filteredChallenges.length}개의 챌린지 (전체 {challenges.length}개)
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          size="large"
          href="/admin/challenges/new"
        >
          새 챌린지 생성
        </Button>
      </Box>

      {/* 검색 및 필터 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            {/* 검색 */}
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                variant="outlined"
                placeholder="챌린지 제목, 설명, 태그 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ color: 'text.secondary', mr: 1 }} />
                }}
              />
            </Grid>

            {/* 필터들 */}
            <Grid item xs={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>상태</InputLabel>
                <Select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  label="상태"
                >
                  <MenuItem value="all">전체</MenuItem>
                  <MenuItem value="DRAFT">초안</MenuItem>
                  <MenuItem value="ACTIVE">진행중</MenuItem>
                  <MenuItem value="COMPLETED">완료</MenuItem>
                  <MenuItem value="ARCHIVED">보관</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>난이도</InputLabel>
                <Select
                  value={difficultyFilter}
                  onChange={(e) => setDifficultyFilter(e.target.value)}
                  label="난이도"
                >
                  <MenuItem value="all">전체</MenuItem>
                  <MenuItem value="BEGINNER">초급</MenuItem>
                  <MenuItem value="INTERMEDIATE">중급</MenuItem>
                  <MenuItem value="ADVANCED">고급</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>유형</InputLabel>
                <Select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                  label="유형"
                >
                  <MenuItem value="all">전체</MenuItem>
                  <MenuItem value="PRACTICE">연습</MenuItem>
                  <MenuItem value="COMPETITION">경쟁</MenuItem>
                  <MenuItem value="GUIDED">가이드</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6} md={2}>
              <Button
                variant="outlined"
                startIcon={<FilterList />}
                onClick={resetFilters}
                fullWidth
              >
                필터 초기화
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* 에러 표시 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* 챌린지 목록 */}
      <Grid container spacing={2}>
        {loading ? (
          // 로딩 스켈레톤
          Array.from({ length: 6 }, (_, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card>
                <CardContent>
                  <Skeleton variant="text" width="80%" height={32} />
                  <Skeleton variant="text" width="100%" height={20} />
                  <Skeleton variant="text" width="100%" height={20} />
                  <Box mt={2} display="flex" gap={1}>
                    <Skeleton variant="rounded" width={60} height={24} />
                    <Skeleton variant="rounded" width={60} height={24} />
                    <Skeleton variant="rounded" width={60} height={24} />
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))
        ) : paginatedChallenges.length > 0 ? (
          paginatedChallenges.map((challenge) => (
            <Grid item xs={12} sm={6} md={4} key={challenge.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                    <Typography variant="h6" fontWeight="bold" sx={{ flexGrow: 1, mr: 1 }}>
                      {challenge.title}
                      {challenge.isFeatured && (
                        <Star sx={{ color: 'gold', ml: 1, fontSize: 20 }} />
                      )}
                    </Typography>
                    <IconButton
                      size="small"
                      onClick={(e) => handleMenuOpen(e, challenge)}
                    >
                      <MoreVert />
                    </IconButton>
                  </Box>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    {challenge.description}
                  </Typography>

                  <Box display="flex" flexWrap="wrap" gap={1} mb={2}>
                    <Chip
                      label={challenge.status === 'DRAFT' ? '초안' :
                            challenge.status === 'ACTIVE' ? '진행중' :
                            challenge.status === 'COMPLETED' ? '완료' : '보관'}
                      color={statusColors[challenge.status]}
                      size="small"
                    />
                    <Chip
                      label={challenge.difficulty === 'BEGINNER' ? '초급' :
                            challenge.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
                      color={difficultyColors[challenge.difficulty]}
                      size="small"
                    />
                    <Chip
                      label={challenge.challengeType === 'PRACTICE' ? '연습' :
                            challenge.challengeType === 'COMPETITION' ? '경쟁' : '가이드'}
                      color={typeColors[challenge.challengeType]}
                      size="small"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    참여자: {challenge.currentParticipants}
                    {challenge.maxParticipants && `/${challenge.maxParticipants}`}명
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    기간: {challenge.durationDays}일 · 예상시간: {challenge.estimatedDurationMinutes}분
                  </Typography>

                  <Typography variant="body2" color="text.secondary">
                    초기 자금: {challenge.initialBalance.toLocaleString()}원
                  </Typography>
                </CardContent>

                <CardActions>
                  <Button
                    size="small"
                    startIcon={<Visibility />}
                    href={`/admin/challenges/${challenge.id}`}
                  >
                    상세보기
                  </Button>
                  <Button
                    size="small"
                    startIcon={<Edit />}
                    href={`/admin/challenges/${challenge.id}/edit`}
                  >
                    수정
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))
        ) : (
          <Grid item xs={12}>
            <Card>
              <CardContent sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  조건에 맞는 챌린지가 없습니다
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  검색어나 필터를 조정해보세요
                </Typography>
                <Button variant="outlined" onClick={resetFilters}>
                  필터 초기화
                </Button>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>

      {/* 페이지네이션 */}
      {filteredChallenges.length > 0 && (
        <Box mt={3} display="flex" justifyContent="center">
          <TablePagination
            component="div"
            count={filteredChallenges.length}
            page={page}
            onPageChange={handlePageChange}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleRowsPerPageChange}
            rowsPerPageOptions={[6, 12, 24, 48]}
            labelRowsPerPage="페이지당 항목 수:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}-${to} of ${count !== -1 ? count : `more than ${to}`}`
            }
          />
        </Box>
      )}

      {/* 액션 메뉴 */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        {selectedChallenge && (
          <>
            <MenuItem onClick={() => window.open(`/admin/challenges/${selectedChallenge.id}`, '_blank')}>
              <Visibility sx={{ mr: 1 }} />
              상세보기
            </MenuItem>
            <MenuItem onClick={() => window.open(`/admin/challenges/${selectedChallenge.id}/edit`, '_blank')}>
              <Edit sx={{ mr: 1 }} />
              수정하기
            </MenuItem>
            <MenuItem onClick={() => toggleFeatured(selectedChallenge)}>
              {selectedChallenge.isFeatured ? (
                <>
                  <StarBorder sx={{ mr: 1 }} />
                  피처드 해제
                </>
              ) : (
                <>
                  <Star sx={{ mr: 1 }} />
                  피처드 설정
                </>
              )}
            </MenuItem>
            {selectedChallenge.status === 'DRAFT' && (
              <MenuItem onClick={() => changeStatus(selectedChallenge, 'ACTIVE')}>
                <PlayArrow sx={{ mr: 1 }} />
                활성화
              </MenuItem>
            )}
            {selectedChallenge.status === 'ACTIVE' && (
              <MenuItem onClick={() => changeStatus(selectedChallenge, 'COMPLETED')}>
                <Stop sx={{ mr: 1 }} />
                완료처리
              </MenuItem>
            )}
            <MenuItem onClick={() => changeStatus(selectedChallenge, 'ARCHIVED')}>
              <Archive sx={{ mr: 1 }} />
              보관하기
            </MenuItem>
            <MenuItem onClick={() => handleDelete(selectedChallenge)} sx={{ color: 'error.main' }}>
              <Delete sx={{ mr: 1 }} />
              삭제하기
            </MenuItem>
          </>
        )}
      </Menu>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>챌린지 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            "{challengeToDelete?.title}" 챌린지를 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            취소
          </Button>
          <Button onClick={confirmDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}