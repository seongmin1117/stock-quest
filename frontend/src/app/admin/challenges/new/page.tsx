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
  Alert,
  InputAdornment,
  FormControlLabel,
  Switch,
  Autocomplete
} from '@mui/material';
import {
  Save,
  Cancel,
  ArrowBack
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';

// 초기 폼 데이터
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

const initialFormData: ChallengeFormData = {
  title: '',
  description: '',
  difficulty: 'BEGINNER',
  challengeType: 'PRACTICE',
  status: 'DRAFT',
  initialBalance: 100000,
  durationDays: 30,
  estimatedDurationMinutes: 60,
  maxParticipants: undefined,
  tags: [],
  isFeatured: false,
  learningObjectives: '',
  marketScenario: '',
  availableInstruments: ['STOCKS', 'ETF', 'CASH']
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

export default function NewChallengePage() {
  const router = useRouter();

  const [formData, setFormData] = useState<ChallengeFormData>(initialFormData);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // 새로운 태그 입력 상태
  const [newTag, setNewTag] = useState('');

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
      if (formData.availableInstruments.length === 0) {
        setError('최소 하나의 투자 상품을 선택해주세요.');
        return;
      }

      // TODO: 실제 API 호출
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSuccess('챌린지가 성공적으로 생성되었습니다.');

      // 2초 후 챌린지 목록으로 이동
      setTimeout(() => {
        router.push('/admin/challenges');
      }, 2000);

    } catch (err) {
      setError('챌린지 생성 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/challenges');
  };

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/challenges')}
          sx={{ mb: 2 }}
        >
          챌린지 목록으로
        </Button>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          새 챌린지 생성
        </Typography>
        <Typography variant="body1" color="text.secondary">
          새로운 투자 챌린지를 만들어 사용자들에게 제공하세요
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
                  placeholder="예: 2020년 코로나 시장 급락"
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
                  placeholder="챌린지의 목적과 학습할 내용을 상세히 설명해주세요"
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
                  placeholder="• 목표 1&#10;• 목표 2&#10;• 목표 3"
                />

                <TextField
                  fullWidth
                  label="시장 시나리오"
                  value={formData.marketScenario}
                  onChange={(e) => handleInputChange('marketScenario', e.target.value)}
                  margin="normal"
                  multiline
                  rows={3}
                  placeholder="이 챌린지에서 시뮬레이션할 시장 상황을 설명해주세요"
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
                {formData.tags.length === 0 && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    태그를 추가하여 챌린지를 쉽게 찾을 수 있도록 하세요
                  </Typography>
                )}
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
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  챌린지에서 사용할 수 있는 투자 상품을 선택하세요
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
                  생성
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
                    {saving ? '생성 중...' : '챌린지 생성'}
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

                {/* 미리보기 */}
                <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                  생성된 챌린지는 상태에 따라 사용자에게 공개됩니다.
                  초안 상태로 생성하여 검토 후 활성화하는 것을 권장합니다.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}