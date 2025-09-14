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

// 임시 타입 정의
interface ChallengeFormData {
  title: string;
  description: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  challengeType: 'PRACTICE' | 'COMPETITION' | 'GUIDED';
  status: 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  initialBalance: number;
  durationDays: number;
  estimatedDurationMinutes: number;
  maxParticipants?: number;
  tags: string[];
  isFeatured: boolean;
  learningObjectives: string;
  marketScenario: string;
  availableInstruments: string[];
}

// 초기 폼 데이터
const mockFormData: ChallengeFormData = {
  title: '2020년 코로나 시장 급락',
  description: '코로나19 팬데믹 초기 시장 상황에서의 투자 전략을 학습합니다. 2020년 2월부터 4월까지의 급격한 시장 변동성 속에서 포트폴리오를 관리하고 리스크를 제어하는 방법을 배웁니다.',
  difficulty: 'INTERMEDIATE',
  challengeType: 'PRACTICE',
  status: 'ACTIVE',
  initialBalance: 100000,
  durationDays: 30,
  estimatedDurationMinutes: 45,
  maxParticipants: 100,
  tags: ['market-crash', 'volatility', 'pandemic', 'risk-management'],
  isFeatured: true,
  learningObjectives: '• 시장 급락 상황에서의 심리적 대응\n• 포트폴리오 리밸런싱 전략\n• 방어주 선택 기준\n• 손실 제한 기법',
  marketScenario: '2020년 2월 19일부터 3월 23일까지 S&P 500이 34% 급락한 기간을 시뮬레이션합니다.',
  availableInstruments: ['STOCKS', 'ETF', 'BONDS', 'CASH']
};

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
  const challengeId = params.id as string;

  const [formData, setFormData] = useState<ChallengeFormData>(mockFormData);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // 새로운 태그 입력 상태
  const [newTag, setNewTag] = useState('');

  useEffect(() => {
    // TODO: 실제 API 호출로 대체
    const fetchChallenge = async () => {
      try {
        setLoading(true);
        await new Promise(resolve => setTimeout(resolve, 500));

        if (challengeId === '1') {
          setFormData(mockFormData);
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

  const handleInputChange = (field: keyof ChallengeFormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleTagAdd = (tag: string) => {
    if (tag && !formData.tags.includes(tag)) {
      setFormData(prev => ({
        ...prev,
        tags: [...prev.tags, tag]
      }));
    }
    setNewTag('');
  };

  const handleTagRemove = (tagToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }));
  };

  const handleInstrumentChange = (instruments: string[]) => {
    setFormData(prev => ({
      ...prev,
      availableInstruments: instruments
    }));
  };

  const handleSave = async () => {
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

      // TODO: 실제 API 호출
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSuccess('챌린지가 성공적으로 수정되었습니다.');

      // 3초 후 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/admin/challenges/${challengeId}`);
      }, 2000);

    } catch (err) {
      setError('챌린지 수정 중 오류가 발생했습니다.');
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

  if (error && !formData.title) {
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
                        <MenuItem value="BEGINNER">초급</MenuItem>
                        <MenuItem value="INTERMEDIATE">중급</MenuItem>
                        <MenuItem value="ADVANCED">고급</MenuItem>
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
                        <MenuItem value="PRACTICE">연습</MenuItem>
                        <MenuItem value="COMPETITION">경쟁</MenuItem>
                        <MenuItem value="GUIDED">가이드</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <FormControl fullWidth>
                      <InputLabel>상태</InputLabel>
                      <Select
                        value={formData.status}
                        onChange={(e) => handleInputChange('status', e.target.value)}
                        label="상태"
                      >
                        <MenuItem value="DRAFT">초안</MenuItem>
                        <MenuItem value="ACTIVE">진행중</MenuItem>
                        <MenuItem value="COMPLETED">완료</MenuItem>
                        <MenuItem value="ARCHIVED">보관</MenuItem>
                      </Select>
                    </FormControl>
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
                      value={formData.estimatedDurationMinutes}
                      onChange={(e) => handleInputChange('estimatedDurationMinutes', parseInt(e.target.value) || 0)}
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
                    <Box sx={{ mt: 2 }}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={formData.isFeatured}
                            onChange={(e) => handleInputChange('isFeatured', e.target.checked)}
                          />
                        }
                        label="피처드 챌린지로 설정"
                      />
                    </Box>
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
                  value={formData.marketScenario}
                  onChange={(e) => handleInputChange('marketScenario', e.target.value)}
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