'use client';

import React, { useState, useEffect } from 'react';
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
  Alert,
  Skeleton,
  Paper,
  Divider,
  FormControlLabel,
  Switch,
  InputAdornment,
  Autocomplete
} from '@mui/material';
import {
  Save,
  Cancel,
  ArrowBack,
  Add,
  Delete
} from '@mui/icons-material';
import { useParams, useRouter } from 'next/navigation';
import { adminChallengeApi, Challenge, ChallengeDifficulty, ChallengeType, ChallengeStatus, UpdateChallengeRequest } from '@/shared/api/admin-challenge-client';
import { useAuth } from '@/shared/lib/auth/auth-store';

// 타입 정의
interface ChallengeFormData {
  title: string;
  description: string;
  difficulty: ChallengeDifficulty;
  challengeType: ChallengeType;
  initialBalance: number;
  durationDays: number;
  estimatedTimeMinutes: number;
  maxParticipants?: number;
  tags: string[];
  learningObjectives: string;
  marketScenarioDescription: string;
  availableInstruments: string[];
  riskLevel: number;
  categoryId: number;
}


const availableTagOptions = [
  'market-crash', 'volatility', 'pandemic', 'risk-management', 'growth-stocks',
  'value-investing', 'dividend-stocks', 'tech-stocks', 'energy-sector',
  'healthcare', 'finance', 'inflation', 'interest-rates', 'bonds',
  'etf', 'cryptocurrency', 'forex', 'commodities', 'real-estate'
];

const instrumentOptions = [
  { value: 'STOCKS', label: '주식' },
  { value: 'ETF', label: 'ETF' },
  { value: 'BONDS', label: '채권' },
  { value: 'CASH', label: '현금' },
  { value: 'OPTIONS', label: '옵션' },
  { value: 'FUTURES', label: '선물' },
  { value: 'FOREX', label: '외환' },
  { value: 'CRYPTO', label: '암호화폐' }
];

export default function ChallengeEditPage() {
  const params = useParams();
  const router = useRouter();
  const { user } = useAuth();
  const challengeId = parseInt(params.id as string);

  const [originalChallenge, setOriginalChallenge] = useState<Challenge | null>(null);
  const [formData, setFormData] = useState<ChallengeFormData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // 새로운 태그 입력 상태
  const [newTag, setNewTag] = useState('');

  useEffect(() => {
    const fetchChallenge = async () => {
      try {
        setLoading(true);
        setError(null);

        const challenge = await adminChallengeApi.getChallengeById(challengeId);
        setOriginalChallenge(challenge);

        // Challenge 데이터를 ChallengeFormData로 변환
        setFormData({
          title: challenge.title,
          description: challenge.description,
          difficulty: challenge.difficulty,
          challengeType: challenge.challengeType,
          initialBalance: challenge.initialBalance,
          durationDays: challenge.durationDays || 0,
          estimatedTimeMinutes: challenge.estimatedTimeMinutes || 0,
          maxParticipants: challenge.maxParticipants,
          tags: challenge.tags || [],
          learningObjectives: challenge.learningObjectives || '',
          marketScenarioDescription: challenge.marketScenarioDescription || '',
          availableInstruments: challenge.availableInstruments || [],
          riskLevel: challenge.riskLevel || 0,
          categoryId: challenge.categoryId || 0
        });
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

  const handleInputChange = (field: keyof ChallengeFormData, value: any) => {
    setFormData(prev => prev ? ({
      ...prev,
      [field]: value
    }) : null);
  };

  const handleTagAdd = (tag: string) => {
    if (tag && formData && !formData.tags.includes(tag)) {
      setFormData(prev => prev ? ({
        ...prev,
        tags: [...prev.tags, tag]
      }) : null);
    }
    setNewTag('');
  };

  const handleTagRemove = (tagToRemove: string) => {
    setFormData(prev => prev ? ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }) : null);
  };

  const handleInstrumentChange = (instruments: string[]) => {
    setFormData(prev => prev ? ({
      ...prev,
      availableInstruments: instruments
    }) : null);
  };

  const handleSave = async () => {
    if (!formData || !user) return;

    try {
      setSaving(true);
      setError(null);

      // 유효성 검사
      if (!formData.title.trim()) {
        setError('챌린지 제목을 입력해주세요.');
        return;
      }
      if (!formData.description.trim()) {
        setError('챌린지 설명을 입력해주세요.');
        return;
      }
      if (formData.initialBalance <= 0) {
        setError('초기 자금은 0보다 커야 합니다.');
        return;
      }
      if (formData.durationDays <= 0) {
        setError('진행 기간은 0보다 커야 합니다.');
        return;
      }

      // 실제 API 호출
      const updateData: UpdateChallengeRequest = {
        title: formData.title,
        description: formData.description,
        categoryId: formData.categoryId,
        difficulty: formData.difficulty,
        challengeType: formData.challengeType,
        initialBalance: formData.initialBalance,
        durationDays: formData.durationDays,
        maxParticipants: formData.maxParticipants,
        availableInstruments: formData.availableInstruments,
        learningObjectives: formData.learningObjectives,
        marketScenarioDescription: formData.marketScenarioDescription,
        riskLevel: formData.riskLevel,
        estimatedTimeMinutes: formData.estimatedTimeMinutes,
        tags: formData.tags,
        createdBy: user.id
      };

      const updatedChallenge = await adminChallengeApi.updateChallenge(challengeId, updateData);

      setSuccess(`챌린지 "${updatedChallenge.title}"가 성공적으로 수정되었습니다.`);

      // 2초 후 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/admin/challenges/${challengeId}`);
      }, 2000);

    } catch (err) {
      setError('챌린지 수정에 실패했습니다.');
      console.error('Error updating challenge:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    router.push(`/admin/challenges/${challengeId}`);
  };

  if (loading) {
    return (
      <Box>
        <Skeleton variant="text" width={200} height={40} sx={{ mb: 2 }} />
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Skeleton variant="text" width="100%" height={40} sx={{ mb: 2 }} />
                <Skeleton variant="rectangular" width="100%" height={120} sx={{ mb: 2 }} />
                <Skeleton variant="text" width="50%" height={40} />
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>
    );
  }

  if (error && !formData) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
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

  if (!formData) {
    return null;
  }

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push(`/admin/challenges/${challengeId}`)}
          sx={{ mb: 2 }}
        >
          챌린지 상세로
        </Button>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          챌린지 수정
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {formData.title}
        </Typography>
      </Box>

      {/* 알림 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}

      <form onSubmit={(e) => { e.preventDefault(); handleSave(); }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {/* 기본 정보 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  기본 정보
                </Typography>

                <TextField
                  fullWidth
                  label="챌린지 제목"
                  value={formData.title}
                  onChange={(e) => handleInputChange('title', e.target.value)}
                  margin="normal"
                  required
                />

                <TextField
                  fullWidth
                  label="챌린지 설명"
                  value={formData.description}
                  onChange={(e) => handleInputChange('description', e.target.value)}
                  margin="normal"
                  multiline
                  rows={4}
                  required
                />

                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={4}>
                    <FormControl fullWidth>
                      <InputLabel>난이도</InputLabel>
                      <Select
                        value={formData.difficulty}
                        onChange={(e) => handleInputChange('difficulty', e.target.value)}
                        label="난이도"
                      >
                        <MenuItem value={ChallengeDifficulty.BEGINNER}>초급</MenuItem>
                        <MenuItem value={ChallengeDifficulty.INTERMEDIATE}>중급</MenuItem>
                        <MenuItem value={ChallengeDifficulty.ADVANCED}>고급</MenuItem>
                        <MenuItem value={ChallengeDifficulty.EXPERT}>전문가</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <FormControl fullWidth>
                      <InputLabel>유형</InputLabel>
                      <Select
                        value={formData.challengeType}
                        onChange={(e) => handleInputChange('challengeType', e.target.value)}
                        label="유형"
                      >
                        <MenuItem value={ChallengeType.MARKET_CRASH}>마켓 크래시</MenuItem>
                        <MenuItem value={ChallengeType.BULL_MARKET}>상승장</MenuItem>
                        <MenuItem value={ChallengeType.SECTOR_ROTATION}>섹터 로테이션</MenuItem>
                        <MenuItem value={ChallengeType.VOLATILITY}>변동성 거래</MenuItem>
                        <MenuItem value={ChallengeType.ESG}>ESG 투자</MenuItem>
                        <MenuItem value={ChallengeType.INTERNATIONAL}>해외 시장</MenuItem>
                        <MenuItem value={ChallengeType.OPTIONS}>옵션 거래</MenuItem>
                        <MenuItem value={ChallengeType.RISK_MANAGEMENT}>리스크 관리</MenuItem>
                        <MenuItem value={ChallengeType.TOURNAMENT}>토너먼트</MenuItem>
                        <MenuItem value={ChallengeType.EDUCATIONAL}>교육용</MenuItem>
                        <MenuItem value={ChallengeType.COMMUNITY}>커뮤니티</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="리스크 레벨"
                      type="number"
                      value={formData.riskLevel}
                      onChange={(e) => handleInputChange('riskLevel', parseInt(e.target.value) || 1)}
                      inputProps={{ min: 1, max: 10 }}
                      helperText="1(낮음) ~ 10(높음)"
                      margin="normal"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 설정 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  챌린지 설정
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="초기 자금"
                      type="number"
                      value={formData.initialBalance}
                      onChange={(e) => handleInputChange('initialBalance', parseInt(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">원</InputAdornment>,
                      }}
                      margin="normal"
                      required
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="진행 기간"
                      type="number"
                      value={formData.durationDays}
                      onChange={(e) => handleInputChange('durationDays', parseInt(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">일</InputAdornment>,
                      }}
                      margin="normal"
                      required
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="예상 소요 시간"
                      type="number"
                      value={formData.estimatedTimeMinutes}
                      onChange={(e) => handleInputChange('estimatedTimeMinutes', parseInt(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">분</InputAdornment>,
                      }}
                      margin="normal"
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="최대 참여자 수"
                      type="number"
                      value={formData.maxParticipants || ''}
                      onChange={(e) => handleInputChange('maxParticipants', parseInt(e.target.value) || undefined)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">명</InputAdornment>,
                      }}
                      margin="normal"
                      helperText="비어두면 무제한"
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="카테고리 ID"
                      type="number"
                      value={formData.categoryId}
                      onChange={(e) => handleInputChange('categoryId', parseInt(e.target.value) || 1)}
                      margin="normal"
                      helperText="카테고리 ID (임시)"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 학습 내용 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  학습 내용
                </Typography>

                <TextField
                  fullWidth
                  label="학습 목표"
                  value={formData.learningObjectives}
                  onChange={(e) => handleInputChange('learningObjectives', e.target.value)}
                  margin="normal"
                  multiline
                  rows={4}
                  helperText="각 목표를 • 로 시작하여 입력하세요"
                />

                <TextField
                  fullWidth
                  label="시장 시나리오"
                  value={formData.marketScenarioDescription}
                  onChange={(e) => handleInputChange('marketScenarioDescription', e.target.value)}
                  margin="normal"
                  multiline
                  rows={3}
                />
              </CardContent>
            </Card>

            {/* 태그 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  태그 관리
                </Typography>

                <Box mb={2}>
                  <Autocomplete
                    freeSolo
                    options={availableTagOptions}
                    value={newTag}
                    onChange={(event, newValue) => {
                      if (newValue) {
                        handleTagAdd(newValue);
                      }
                    }}
                    onInputChange={(event, newInputValue) => {
                      setNewTag(newInputValue);
                    }}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="태그 추가"
                        helperText="엔터를 누르거나 드롭다운에서 선택하세요"
                        onKeyPress={(e) => {
                          if (e.key === 'Enter') {
                            e.preventDefault();
                            handleTagAdd(newTag);
                          }
                        }}
                      />
                    )}
                  />
                </Box>

                <Box display="flex" flexWrap="wrap" gap={1}>
                  {formData.tags.map((tag) => (
                    <Chip
                      key={tag}
                      label={tag}
                      onDelete={() => handleTagRemove(tag)}
                      variant="outlined"
                    />
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 사용 가능한 투자 상품 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  투자 상품 설정
                </Typography>

                <Box display="flex" flexDirection="column" gap={1}>
                  {instrumentOptions.map((option) => (
                    <FormControlLabel
                      key={option.value}
                      control={
                        <Switch
                          checked={formData.availableInstruments.includes(option.value)}
                          onChange={(e) => {
                            if (e.target.checked) {
                              handleInstrumentChange([...formData.availableInstruments, option.value]);
                            } else {
                              handleInstrumentChange(formData.availableInstruments.filter(i => i !== option.value));
                            }
                          }}
                        />
                      }
                      label={option.label}
                    />
                  ))}
                </Box>
              </CardContent>
            </Card>

            {/* 액션 버튼 */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  저장
                </Typography>

                <Box display="flex" flexDirection="column" gap={2}>
                  <Button
                    variant="contained"
                    startIcon={<Save />}
                    onClick={handleSave}
                    disabled={saving}
                    fullWidth
                    size="large"
                  >
                    {saving ? '저장 중...' : '변경사항 저장'}
                  </Button>

                  <Button
                    variant="outlined"
                    startIcon={<Cancel />}
                    onClick={handleCancel}
                    disabled={saving}
                    fullWidth
                  >
                    취소
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}