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
import { adminChallengeApi, Challenge, ChallengeDifficulty, ChallengeType, ChallengeStatus, ChallengeSearchParams } from '@/shared/api/admin-challenge-client';
import { useAuth } from '@/shared/lib/auth/auth-store';

const difficultyColors = {
  [ChallengeDifficulty.BEGINNER]: 'success',
  [ChallengeDifficulty.INTERMEDIATE]: 'warning',
  [ChallengeDifficulty.ADVANCED]: 'error',
  [ChallengeDifficulty.EXPERT]: 'error'
} as const;

const statusColors = {
  [ChallengeStatus.DRAFT]: 'default',
  [ChallengeStatus.ACTIVE]: 'primary',
  [ChallengeStatus.COMPLETED]: 'success',
  [ChallengeStatus.ARCHIVED]: 'secondary',
  [ChallengeStatus.PAUSED]: 'warning'
} as const;

const typeColors = {
  [ChallengeType.STOCK_PICKING]: 'primary',
  [ChallengeType.PORTFOLIO_MANAGEMENT]: 'secondary',
  [ChallengeType.RISK_MANAGEMENT]: 'warning',
  [ChallengeType.OPTIONS_TRADING]: 'error',
  [ChallengeType.SECTOR_ANALYSIS]: 'info',
  [ChallengeType.TECHNICAL_ANALYSIS]: 'success'
} as const;

export default function ChallengesPage() {
  const { user } = useAuth();
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
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

  // 데이터 로딩 함수
  const loadChallenges = async () => {
    try {
      setLoading(true);
      setError(null);

      const searchParams: ChallengeSearchParams = {
        page,
        size: rowsPerPage,
        ...(searchQuery && { title: searchQuery }),
        ...(statusFilter !== 'all' && { status: statusFilter as ChallengeStatus }),
        ...(difficultyFilter !== 'all' && { difficulty: difficultyFilter as ChallengeDifficulty }),
        ...(typeFilter !== 'all' && { challengeType: typeFilter as ChallengeType }),
        ...(featuredFilter === 'featured' && { featured: true }),
        ...(featuredFilter === 'not-featured' && { featured: false }),
        sortBy: 'createdAt',
        sortDirection: 'desc'
      };

      const response = await adminChallengeApi.getChallenges(searchParams);
      setChallenges(response.content);
      setTotalElements(response.totalElements);
    } catch (err) {
      setError('챌린지 목록을 불러오는데 실패했습니다.');
      console.error('Error loading challenges:', err);
    } finally {
      setLoading(false);
    }
  };

  // 초기 로딩 및 필터 변경 시 데이터 로딩
  useEffect(() => {
    loadChallenges();
  }, [page, rowsPerPage, searchQuery, statusFilter, difficultyFilter, typeFilter, featuredFilter]);

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

  const confirmDelete = async () => {
    if (challengeToDelete && user) {
      try {
        await adminChallengeApi.archiveChallenge(challengeToDelete.id, user.id);
        setDeleteDialogOpen(false);
        setChallengeToDelete(null);
        loadChallenges(); // 목록 새로고침
      } catch (err) {
        setError('챌린지 삭제에 실패했습니다.');
      }
    }
  };

  const toggleFeatured = async (challenge: Challenge) => {
    if (!user) return;

    try {
      await adminChallengeApi.setFeaturedChallenge(challenge.id, !challenge.featured, user.id);
      handleMenuClose();
      loadChallenges(); // 목록 새로고침
    } catch (err) {
      setError('피처드 상태 변경에 실패했습니다.');
    }
  };

  const changeStatus = async (challenge: Challenge, newStatus: ChallengeStatus) => {
    if (!user) return;

    try {
      await adminChallengeApi.changeStatus(challenge.id, newStatus, user.id);
      handleMenuClose();
      loadChallenges(); // 목록 새로고침
    } catch (err) {
      setError('상태 변경에 실패했습니다.');
    }
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
{challenges.length}개의 챌린지 (전체 {totalElements}개)
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
                  <MenuItem value={ChallengeStatus.DRAFT}>초안</MenuItem>
                  <MenuItem value={ChallengeStatus.ACTIVE}>진행중</MenuItem>
                  <MenuItem value={ChallengeStatus.PAUSED}>일시정지</MenuItem>
                  <MenuItem value={ChallengeStatus.COMPLETED}>완료</MenuItem>
                  <MenuItem value={ChallengeStatus.ARCHIVED}>보관</MenuItem>
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
                  <MenuItem value={ChallengeDifficulty.BEGINNER}>초급</MenuItem>
                  <MenuItem value={ChallengeDifficulty.INTERMEDIATE}>중급</MenuItem>
                  <MenuItem value={ChallengeDifficulty.ADVANCED}>고급</MenuItem>
                  <MenuItem value={ChallengeDifficulty.EXPERT}>전문가</MenuItem>
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
                  <MenuItem value={ChallengeType.STOCK_PICKING}>주식 선택</MenuItem>
                  <MenuItem value={ChallengeType.PORTFOLIO_MANAGEMENT}>포트폴리오 관리</MenuItem>
                  <MenuItem value={ChallengeType.RISK_MANAGEMENT}>리스크 관리</MenuItem>
                  <MenuItem value={ChallengeType.OPTIONS_TRADING}>옵션 거래</MenuItem>
                  <MenuItem value={ChallengeType.SECTOR_ANALYSIS}>섹터 분석</MenuItem>
                  <MenuItem value={ChallengeType.TECHNICAL_ANALYSIS}>기술적 분석</MenuItem>
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
        ) : challenges.length > 0 ? (
          challenges.map((challenge) => (
            <Grid item xs={12} sm={6} md={4} key={challenge.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                    <Typography variant="h6" fontWeight="bold" sx={{ flexGrow: 1, mr: 1 }}>
                      {challenge.title}
                      {challenge.featured && (
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
                      label={challenge.status === ChallengeStatus.DRAFT ? '초안' :
                            challenge.status === ChallengeStatus.ACTIVE ? '진행중' :
                            challenge.status === ChallengeStatus.PAUSED ? '일시정지' :
                            challenge.status === ChallengeStatus.COMPLETED ? '완료' : '보관'}
                      color={statusColors[challenge.status]}
                      size="small"
                    />
                    <Chip
                      label={challenge.difficulty === ChallengeDifficulty.BEGINNER ? '초급' :
                            challenge.difficulty === ChallengeDifficulty.INTERMEDIATE ? '중급' :
                            challenge.difficulty === ChallengeDifficulty.ADVANCED ? '고급' : '전문가'}
                      color={difficultyColors[challenge.difficulty]}
                      size="small"
                    />
                    <Chip
                      label={challenge.challengeType === ChallengeType.STOCK_PICKING ? '주식 선택' :
                            challenge.challengeType === ChallengeType.PORTFOLIO_MANAGEMENT ? '포트폴리오' :
                            challenge.challengeType === ChallengeType.RISK_MANAGEMENT ? '리스크 관리' :
                            challenge.challengeType === ChallengeType.OPTIONS_TRADING ? '옵션 거래' :
                            challenge.challengeType === ChallengeType.SECTOR_ANALYSIS ? '섹터 분석' : '기술적 분석'}
                      color={typeColors[challenge.challengeType]}
                      size="small"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    참여자: {challenge.currentParticipants}
                    {challenge.maxParticipants && `/${challenge.maxParticipants}`}명
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    기간: {challenge.durationDays}일 · 예상시간: {challenge.estimatedTimeMinutes || 0}분
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
      {totalElements > 0 && (
        <Box mt={3} display="flex" justifyContent="center">
          <TablePagination
            component="div"
            count={totalElements}
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
              {selectedChallenge.featured ? (
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
            {selectedChallenge.status === ChallengeStatus.DRAFT && (
              <MenuItem onClick={() => changeStatus(selectedChallenge, ChallengeStatus.ACTIVE)}>
                <PlayArrow sx={{ mr: 1 }} />
                활성화
              </MenuItem>
            )}
            {selectedChallenge.status === ChallengeStatus.ACTIVE && (
              <MenuItem onClick={() => changeStatus(selectedChallenge, ChallengeStatus.COMPLETED)}>
                <Stop sx={{ mr: 1 }} />
                완료처리
              </MenuItem>
            )}
            <MenuItem onClick={() => changeStatus(selectedChallenge, ChallengeStatus.ARCHIVED)}>
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