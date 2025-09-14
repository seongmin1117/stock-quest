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
  InputAdornment,
  FormControlLabel,
  Switch,
  Autocomplete,
  Divider
} from '@mui/material';
import {
  Save,
  Cancel,
  ArrowBack
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';

interface TemplateFormData {
  name: string;
  description: string;
  category: 'market-crash' | 'bull-market' | 'sector-rotation' | 'volatility' | 'esg' | 'international' | 'risk-management';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  estimatedDurationMinutes: number;
  isActive: boolean;
  tags: string[];
  marketScenario: string;
  learningObjectives: string;
  successCriteria: {
    minReturn: number;
    maxDrawdown: number;
    minWinRate: number;
    maxRisk: number;
  };
  challengeDefaults: {
    initialBalance: number;
    durationDays: number;
    maxParticipants?: number;
    availableInstruments: string[];
  };
}

const initialFormData: TemplateFormData = {
  name: '',
  description: '',
  category: 'market-crash',
  difficulty: 'BEGINNER',
  estimatedDurationMinutes: 60,
  isActive: true,
  tags: [],
  marketScenario: '',
  learningObjectives: '',
  successCriteria: {
    minReturn: 5,
    maxDrawdown: -15,
    minWinRate: 60,
    maxRisk: 20
  },
  challengeDefaults: {
    initialBalance: 100000,
    durationDays: 30,
    maxParticipants: undefined,
    availableInstruments: ['STOCKS', 'ETF', 'CASH']
  }
};

const categoryOptions = [
  { value: 'market-crash', label: '시장 급락 시나리오' },
  { value: 'bull-market', label: '상승장 전략' },
  { value: 'sector-rotation', label: '섹터 로테이션' },
  { value: 'volatility', label: '변동성 관리' },
  { value: 'esg', label: 'ESG 투자' },
  { value: 'international', label: '글로벌 투자' },
  { value: 'risk-management', label: '리스크 관리' }
];

const availableTagOptions = [
  'market-crash', 'volatility', 'pandemic', 'risk-management', 'growth-stocks',
  'value-investing', 'dividend-stocks', 'tech-stocks', 'energy-sector',
  'healthcare', 'finance', 'inflation', 'interest-rates', 'bonds',
  'etf', 'cryptocurrency', 'forex', 'commodities', 'real-estate',
  'esg', 'sustainable', 'emerging-markets', 'developed-markets'
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

export default function EditTemplatePage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [formData, setFormData] = useState<TemplateFormData>(initialFormData);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [newTag, setNewTag] = useState('');

  // Load existing template data
  useEffect(() => {
    const loadTemplate = async () => {
      try {
        // TODO: 실제 API 호출로 템플릿 데이터 로드
        // Mock data for demonstration
        await new Promise(resolve => setTimeout(resolve, 500));

        const mockTemplate: TemplateFormData = {
          name: '2020년 코로나 시장 급락 대응',
          description: '코로나19로 인한 급격한 시장 하락 상황에서의 투자 전략을 학습하는 템플릿입니다.',
          category: 'market-crash',
          difficulty: 'INTERMEDIATE',
          estimatedDurationMinutes: 90,
          isActive: true,
          tags: ['market-crash', 'pandemic', 'volatility', 'risk-management'],
          marketScenario: '2020년 2월부터 3월까지의 급격한 시장 하락 상황을 재현합니다. S&P 500이 약 35% 하락하며, VIX 지수가 80 이상으로 치솟는 극도의 변동성 상황입니다.',
          learningObjectives: '• 극도의 시장 변동성 상황에서의 심리적 대응 방법 학습\n• 리스크 관리 전략과 포지션 사이징 실습\n• 시장 급락 시 투자 기회 발견 능력 개발\n• 다양한 방어적 투자 전략 비교 분석',
          successCriteria: {
            minReturn: -10,
            maxDrawdown: -25,
            minWinRate: 55,
            maxRisk: 25
          },
          challengeDefaults: {
            initialBalance: 100000,
            durationDays: 30,
            maxParticipants: 100,
            availableInstruments: ['STOCKS', 'ETF', 'BONDS', 'CASH']
          }
        };

        setFormData(mockTemplate);
      } catch (err) {
        setError('템플릿 데이터를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadTemplate();
  }, [params.id]);

  const handleInputChange = (field: keyof TemplateFormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSuccessCriteriaChange = (field: keyof TemplateFormData['successCriteria'], value: number) => {
    setFormData(prev => ({
      ...prev,
      successCriteria: {
        ...prev.successCriteria,
        [field]: value
      }
    }));
  };

  const handleChallengeDefaultChange = (field: keyof TemplateFormData['challengeDefaults'], value: any) => {
    setFormData(prev => ({
      ...prev,
      challengeDefaults: {
        ...prev.challengeDefaults,
        [field]: value
      }
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
    handleChallengeDefaultChange('availableInstruments', instruments);
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);

      // 유효성 검사
      if (!formData.name.trim()) {
        setError('템플릿 이름을 입력해주세요.');
        return;
      }
      if (!formData.description.trim()) {
        setError('템플릿 설명을 입력해주세요.');
        return;
      }
      if (!formData.marketScenario.trim()) {
        setError('시장 시나리오를 입력해주세요.');
        return;
      }
      if (formData.challengeDefaults.availableInstruments.length === 0) {
        setError('최소 하나의 투자 상품을 선택해주세요.');
        return;
      }

      // TODO: 실제 API 호출
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSuccess('템플릿이 성공적으로 수정되었습니다.');

      // 2초 후 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/admin/templates/${params.id}`);
      }, 2000);

    } catch (err) {
      setError('템플릿 수정 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    router.push(`/admin/templates/${params.id}`);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>템플릿 데이터를 불러오는 중...</Typography>
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
          onClick={() => router.push(`/admin/templates/${params.id}`)}
          sx={{ mb: 2 }}
        >
          템플릿 상세로
        </Button>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          템플릿 수정
        </Typography>
        <Typography variant="body1" color="text.secondary">
          템플릿의 설정과 내용을 수정하세요
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
                  label="템플릿 이름"
                  value={formData.name}
                  onChange={(e) => handleInputChange('name', e.target.value)}
                  margin="normal"
                  required
                  placeholder="예: 2020년 코로나 시장 급락 대응"
                />

                <TextField
                  fullWidth
                  label="템플릿 설명"
                  value={formData.description}
                  onChange={(e) => handleInputChange('description', e.target.value)}
                  margin="normal"
                  multiline
                  rows={3}
                  required
                  placeholder="템플릿의 목적과 학습할 내용을 상세히 설명해주세요"
                />

                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={4}>
                    <FormControl fullWidth>
                      <InputLabel>카테고리</InputLabel>
                      <Select
                        value={formData.category}
                        onChange={(e) => handleInputChange('category', e.target.value)}
                        label="카테고리"
                      >
                        {categoryOptions.map((option) => (
                          <MenuItem key={option.value} value={option.value}>
                            {option.label}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>

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
                </Grid>

                <FormControlLabel
                  control={
                    <Switch
                      checked={formData.isActive}
                      onChange={(e) => handleInputChange('isActive', e.target.checked)}
                    />
                  }
                  label="활성 상태"
                  sx={{ mt: 2 }}
                />
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
                  label="시장 시나리오"
                  value={formData.marketScenario}
                  onChange={(e) => handleInputChange('marketScenario', e.target.value)}
                  margin="normal"
                  multiline
                  rows={3}
                  required
                  placeholder="이 템플릿에서 시뮬레이션할 시장 상황을 상세히 설명해주세요"
                />

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
              </CardContent>
            </Card>

            {/* 성공 기준 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  성공 기준
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  이 템플릿으로 생성된 챌린지에서 사용자가 달성해야 할 목표 수치를 설정하세요
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={6} sm={3}>
                    <TextField
                      fullWidth
                      label="최소 수익률"
                      type="number"
                      value={formData.successCriteria.minReturn}
                      onChange={(e) => handleSuccessCriteriaChange('minReturn', parseFloat(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">%</InputAdornment>,
                      }}
                      margin="normal"
                    />
                  </Grid>

                  <Grid item xs={6} sm={3}>
                    <TextField
                      fullWidth
                      label="최대 손실률"
                      type="number"
                      value={formData.successCriteria.maxDrawdown}
                      onChange={(e) => handleSuccessCriteriaChange('maxDrawdown', parseFloat(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">%</InputAdornment>,
                      }}
                      margin="normal"
                    />
                  </Grid>

                  <Grid item xs={6} sm={3}>
                    <TextField
                      fullWidth
                      label="최소 승률"
                      type="number"
                      value={formData.successCriteria.minWinRate}
                      onChange={(e) => handleSuccessCriteriaChange('minWinRate', parseFloat(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">%</InputAdornment>,
                      }}
                      margin="normal"
                    />
                  </Grid>

                  <Grid item xs={6} sm={3}>
                    <TextField
                      fullWidth
                      label="최대 리스크"
                      type="number"
                      value={formData.successCriteria.maxRisk}
                      onChange={(e) => handleSuccessCriteriaChange('maxRisk', parseFloat(e.target.value) || 0)}
                      InputProps={{
                        endAdornment: <InputAdornment position="end">%</InputAdornment>,
                      }}
                      margin="normal"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 태그 관리 */}
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
                    태그를 추가하여 템플릿을 쉽게 찾을 수 있도록 하세요
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 기본 챌린지 설정 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  챌린지 기본 설정
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  이 템플릿으로 챌린지 생성 시 적용될 기본값을 설정하세요
                </Typography>

                <TextField
                  fullWidth
                  label="초기 자금"
                  type="number"
                  value={formData.challengeDefaults.initialBalance}
                  onChange={(e) => handleChallengeDefaultChange('initialBalance', parseInt(e.target.value) || 0)}
                  InputProps={{
                    endAdornment: <InputAdornment position="end">원</InputAdornment>,
                  }}
                  margin="normal"
                />

                <TextField
                  fullWidth
                  label="진행 기간"
                  type="number"
                  value={formData.challengeDefaults.durationDays}
                  onChange={(e) => handleChallengeDefaultChange('durationDays', parseInt(e.target.value) || 0)}
                  InputProps={{
                    endAdornment: <InputAdornment position="end">일</InputAdornment>,
                  }}
                  margin="normal"
                />

                <TextField
                  fullWidth
                  label="최대 참여자 수"
                  type="number"
                  value={formData.challengeDefaults.maxParticipants || ''}
                  onChange={(e) => handleChallengeDefaultChange('maxParticipants', parseInt(e.target.value) || undefined)}
                  InputProps={{
                    endAdornment: <InputAdornment position="end">명</InputAdornment>,
                  }}
                  margin="normal"
                  helperText="비어두면 무제한"
                />

                <Divider sx={{ my: 2 }} />

                <Typography variant="subtitle1" gutterBottom>
                  사용 가능한 투자 상품
                </Typography>
                <Box display="flex" flexDirection="column" gap={1}>
                  {instrumentOptions.map((option) => (
                    <FormControlLabel
                      key={option.value}
                      control={
                        <Switch
                          checked={formData.challengeDefaults.availableInstruments.includes(option.value)}
                          onChange={(e) => {
                            if (e.target.checked) {
                              handleInstrumentChange([...formData.challengeDefaults.availableInstruments, option.value]);
                            } else {
                              handleInstrumentChange(formData.challengeDefaults.availableInstruments.filter(i => i !== option.value));
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
                  수정 완료
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

                <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                  수정된 템플릿은 새로 생성되는 챌린지에만 적용됩니다.
                  기존 챌린지에는 영향을 주지 않습니다.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}