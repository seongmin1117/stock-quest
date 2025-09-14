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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Skeleton,
  IconButton,
  Menu,
  Tabs,
  Tab,
  Divider
} from '@mui/material';
import {
  Add,
  Search,
  Edit,
  Delete,
  FileCopy,
  Visibility,
  MoreVert,
  TrendingDown,
  TrendingUp,
  Autorenew,
  ShowChart,
  EmojiNature,
  Public,
  Security
} from '@mui/icons-material';

// 템플릿 타입 정의
interface ChallengeTemplate {
  id: number;
  name: string;
  description: string;
  category: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  templateType: string;
  initialBalance: number;
  estimatedDurationMinutes: number;
  speedFactor: number;
  tags: string[];
  isActive: boolean;
  usageCount: number;
  createdAt: string;
  updatedAt: string;
  learningObjectives: string;
  successCriteria: any;
}

// 카테고리 정의
const templateCategories = [
  { id: 'market-crash', name: '시장 급락 시나리오', icon: <TrendingDown />, color: '#f44336' },
  { id: 'bull-market', name: '상승장 전략', icon: <TrendingUp />, color: '#4caf50' },
  { id: 'sector-rotation', name: '섹터 순환', icon: <Autorenew />, color: '#ff9800' },
  { id: 'volatility', name: '변동성 관리', icon: <ShowChart />, color: '#9c27b0' },
  { id: 'esg', name: 'ESG 투자', icon: <EmojiNature />, color: '#2e7d32' },
  { id: 'international', name: '국제 투자', icon: <Public />, color: '#1976d2' },
  { id: 'risk-management', name: '리스크 관리', icon: <Security />, color: '#795548' }
];

// 임시 데이터
const mockTemplates: ChallengeTemplate[] = [
  {
    id: 1,
    name: '2008 금융위기 생존하기',
    description: '2008년 글로벌 금융위기 상황에서 포트폴리오를 방어하고 회복하는 전략을 학습하세요.',
    category: 'market-crash',
    difficulty: 'ADVANCED',
    templateType: 'MARKET_CRASH',
    initialBalance: 100000,
    estimatedDurationMinutes: 45,
    speedFactor: 20,
    tags: ['금융위기', '리스크관리', '방어투자', '경기침체'],
    isActive: true,
    usageCount: 23,
    createdAt: '2024-01-01T09:00:00',
    updatedAt: '2024-01-15T14:30:00',
    learningObjectives: '극심한 시장 급락 상황에서의 리스크 관리와 손실 최소화 전략 습득',
    successCriteria: { minReturn: -20, maxDrawdown: 30 }
  },
  {
    id: 2,
    name: 'COVID-19 시장 급락과 회복',
    description: '2020년 코로나19로 인한 시장 급락과 빠른 회복 과정을 경험하세요.',
    category: 'market-crash',
    difficulty: 'INTERMEDIATE',
    templateType: 'MARKET_CRASH',
    initialBalance: 100000,
    estimatedDurationMinutes: 30,
    speedFactor: 30,
    tags: ['코로나19', '팬데믹', 'V자회복', '기술주'],
    isActive: true,
    usageCount: 45,
    createdAt: '2024-01-05T10:00:00',
    updatedAt: '2024-01-20T16:00:00',
    learningObjectives: '예측 불가능한 외부 충격에 대한 대응 전략과 빠른 회복기 투자 기회 포착',
    successCriteria: { minReturn: -15, recoveryTime: 60 }
  },
  {
    id: 3,
    name: '1990년대 기술주 상승장',
    description: '1990년대 후반 기술주 상승장에서 성장주 투자 전략을 학습하세요.',
    category: 'bull-market',
    difficulty: 'INTERMEDIATE',
    templateType: 'BULL_MARKET',
    initialBalance: 75000,
    estimatedDurationMinutes: 40,
    speedFactor: 25,
    tags: ['기술주', '성장투자', '닷컴버블', '상승장'],
    isActive: true,
    usageCount: 31,
    createdAt: '2024-01-03T11:00:00',
    updatedAt: '2024-01-18T12:00:00',
    learningObjectives: '상승장에서의 성장주 선별과 적절한 이익실현 타이밍 학습',
    successCriteria: { minReturn: 25, sharpeRatio: 1.5 }
  },
  {
    id: 4,
    name: '섹터 순환 전략',
    description: '경기 사이클에 따른 섹터 순환 투자 전략을 학습하세요.',
    category: 'sector-rotation',
    difficulty: 'ADVANCED',
    templateType: 'SECTOR_ROTATION',
    initialBalance: 80000,
    estimatedDurationMinutes: 50,
    speedFactor: 15,
    tags: ['섹터순환', '경기사이클', '순환주', '방어주'],
    isActive: true,
    usageCount: 18,
    createdAt: '2024-01-07T13:00:00',
    updatedAt: '2024-01-22T15:30:00',
    learningObjectives: '경기 사이클 단계별 최적 섹터 배분과 포트폴리오 리밸런싱',
    successCriteria: { minReturn: 15, volatility: 18 }
  },
  {
    id: 5,
    name: 'ESG 지속가능 투자',
    description: 'ESG 요소를 고려한 지속가능한 투자 전략을 학습하세요.',
    category: 'esg',
    difficulty: 'BEGINNER',
    templateType: 'ESG_INVESTING',
    initialBalance: 50000,
    estimatedDurationMinutes: 35,
    speedFactor: 20,
    tags: ['ESG', '지속가능', '친환경', '사회적책임'],
    isActive: true,
    usageCount: 27,
    createdAt: '2024-01-10T09:30:00',
    updatedAt: '2024-01-25T11:00:00',
    learningObjectives: 'ESG 평가 지표 이해와 장기적 지속가능한 투자 포트폴리오 구성',
    successCriteria: { minESGScore: 7.5, minReturn: 10 }
  }
];

const difficultyColors = {
  BEGINNER: 'success',
  INTERMEDIATE: 'warning',
  ADVANCED: 'error'
} as const;

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<ChallengeTemplate[]>(mockTemplates);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 필터링 및 검색 상태
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [difficultyFilter, setDifficultyFilter] = useState<string>('all');
  const [activeFilter, setActiveFilter] = useState<string>('all');
  const [tabValue, setTabValue] = useState(0);

  // 액션 메뉴 상태
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<ChallengeTemplate | null>(null);

  // 삭제 다이얼로그
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [templateToDelete, setTemplateToDelete] = useState<ChallengeTemplate | null>(null);

  // 필터링된 템플릿 계산
  const filteredTemplates = templates.filter(template => {
    const matchesSearch = template.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         template.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         template.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesCategory = selectedCategory === 'all' || template.category === selectedCategory;
    const matchesDifficulty = difficultyFilter === 'all' || template.difficulty === difficultyFilter;
    const matchesActive = activeFilter === 'all' ||
                         (activeFilter === 'active' && template.isActive) ||
                         (activeFilter === 'inactive' && !template.isActive);

    return matchesSearch && matchesCategory && matchesDifficulty && matchesActive;
  });

  // 카테고리별 템플릿 그룹화
  const templatesByCategory = templateCategories.map(category => ({
    ...category,
    templates: filteredTemplates.filter(t => t.category === category.id),
    count: templates.filter(t => t.category === category.id).length
  }));

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    if (newValue === 0) {
      setSelectedCategory('all');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, template: ChallengeTemplate) => {
    setAnchorEl(event.currentTarget);
    setSelectedTemplate(template);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedTemplate(null);
  };

  const handleClone = (template: ChallengeTemplate) => {
    // TODO: API 호출로 템플릿 복제
    const clonedTemplate = {
      ...template,
      id: templates.length + 1,
      name: `${template.name} (복사본)`,
      usageCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    setTemplates([...templates, clonedTemplate]);
    handleMenuClose();
  };

  const handleToggleActive = (template: ChallengeTemplate) => {
    // TODO: API 호출로 활성화 상태 변경
    setTemplates(templates.map(t =>
      t.id === template.id ? { ...t, isActive: !t.isActive } : t
    ));
    handleMenuClose();
  };

  const handleDelete = (template: ChallengeTemplate) => {
    setTemplateToDelete(template);
    setDeleteDialogOpen(true);
    handleMenuClose();
  };

  const confirmDelete = () => {
    if (templateToDelete) {
      // TODO: API 호출로 템플릿 삭제
      setTemplates(templates.filter(t => t.id !== templateToDelete.id));
      setDeleteDialogOpen(false);
      setTemplateToDelete(null);
    }
  };

  const resetFilters = () => {
    setSearchQuery('');
    setSelectedCategory('all');
    setDifficultyFilter('all');
    setActiveFilter('all');
  };

  const getCategoryInfo = (categoryId: string) => {
    return templateCategories.find(c => c.id === categoryId);
  };

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4} display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
            템플릿 관리
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {filteredTemplates.length}개의 템플릿 (전체 {templates.length}개)
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          size="large"
          href="/admin/templates/new"
        >
          새 템플릿 생성
        </Button>
      </Box>

      {/* 탭 네비게이션 */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="전체 보기" />
          <Tab label="카테고리별 보기" />
        </Tabs>
      </Box>

      {/* 검색 및 필터 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                variant="outlined"
                placeholder="템플릿 이름, 설명, 태그 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ color: 'text.secondary', mr: 1 }} />
                }}
              />
            </Grid>

            <Grid item xs={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>카테고리</InputLabel>
                <Select
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  label="카테고리"
                >
                  <MenuItem value="all">전체</MenuItem>
                  {templateCategories.map((category) => (
                    <MenuItem key={category.id} value={category.id}>
                      {category.name}
                    </MenuItem>
                  ))}
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
                <InputLabel>상태</InputLabel>
                <Select
                  value={activeFilter}
                  onChange={(e) => setActiveFilter(e.target.value)}
                  label="상태"
                >
                  <MenuItem value="all">전체</MenuItem>
                  <MenuItem value="active">활성</MenuItem>
                  <MenuItem value="inactive">비활성</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6} md={2}>
              <Button
                variant="outlined"
                onClick={resetFilters}
                fullWidth
              >
                필터 초기화
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* 전체 보기 탭 */}
      <TabPanel value={tabValue} index={0}>
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        <Grid container spacing={2}>
          {loading ? (
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
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))
          ) : filteredTemplates.length > 0 ? (
            filteredTemplates.map((template) => {
              const categoryInfo = getCategoryInfo(template.category);
              return (
                <Grid item xs={12} sm={6} md={4} key={template.id}>
                  <Card sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    border: template.isActive ? '1px solid #1976d2' : '1px solid #e0e0e0'
                  }}>
                    <CardContent sx={{ flexGrow: 1 }}>
                      <Box display="flex" justifyContent="space-between" alignItems="start" mb={1}>
                        <Typography variant="h6" fontWeight="bold" sx={{ flexGrow: 1, mr: 1 }}>
                          {template.name}
                        </Typography>
                        <IconButton
                          size="small"
                          onClick={(e) => handleMenuOpen(e, template)}
                        >
                          <MoreVert />
                        </IconButton>
                      </Box>

                      <Box display="flex" alignItems="center" gap={1} mb={2}>
                        {categoryInfo?.icon}
                        <Typography variant="body2" color="text.secondary">
                          {categoryInfo?.name}
                        </Typography>
                      </Box>

                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {template.description}
                      </Typography>

                      <Box display="flex" flexWrap="wrap" gap={1} mb={2}>
                        <Chip
                          label={template.difficulty === 'BEGINNER' ? '초급' :
                                template.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
                          color={difficultyColors[template.difficulty]}
                          size="small"
                        />
                        <Chip
                          label={template.isActive ? '활성' : '비활성'}
                          color={template.isActive ? 'primary' : 'default'}
                          size="small"
                        />
                        <Chip
                          label={`사용: ${template.usageCount}회`}
                          variant="outlined"
                          size="small"
                        />
                      </Box>

                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        초기 자금: {template.initialBalance.toLocaleString()}원
                      </Typography>

                      <Typography variant="body2" color="text.secondary">
                        예상 소요: {template.estimatedDurationMinutes}분
                      </Typography>

                      <Box mt={2} display="flex" flexWrap="wrap" gap={0.5}>
                        {template.tags.slice(0, 3).map((tag) => (
                          <Chip key={tag} label={tag} size="small" variant="outlined" />
                        ))}
                        {template.tags.length > 3 && (
                          <Chip label={`+${template.tags.length - 3}`} size="small" />
                        )}
                      </Box>
                    </CardContent>

                    <CardActions>
                      <Button
                        size="small"
                        startIcon={<Visibility />}
                        href={`/admin/templates/${template.id}`}
                      >
                        상세보기
                      </Button>
                      <Button
                        size="small"
                        startIcon={<Edit />}
                        href={`/admin/templates/${template.id}/edit`}
                      >
                        수정
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              );
            })
          ) : (
            <Grid item xs={12}>
              <Card>
                <CardContent sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    조건에 맞는 템플릿이 없습니다
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
      </TabPanel>

      {/* 카테고리별 보기 탭 */}
      <TabPanel value={tabValue} index={1}>
        {templatesByCategory.map((category) => (
          <Box key={category.id} mb={4}>
            <Box display="flex" alignItems="center" mb={2}>
              {React.cloneElement(category.icon, { sx: { mr: 1, color: category.color } })}
              <Typography variant="h5" sx={{ fontWeight: 'bold', color: category.color }}>
                {category.name}
              </Typography>
              <Chip
                label={`${category.templates.length}개`}
                sx={{ ml: 2 }}
                variant="outlined"
              />
            </Box>

            {category.templates.length > 0 ? (
              <Grid container spacing={2}>
                {category.templates.map((template) => (
                  <Grid item xs={12} sm={6} md={4} key={template.id}>
                    <Card sx={{
                      height: '100%',
                      border: `2px solid ${category.color}20`,
                      '&:hover': { borderColor: category.color }
                    }}>
                      <CardContent>
                        <Typography variant="h6" fontWeight="bold" gutterBottom>
                          {template.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          {template.description}
                        </Typography>
                        <Box display="flex" gap={1} mb={2}>
                          <Chip
                            label={template.difficulty === 'BEGINNER' ? '초급' :
                                  template.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
                            color={difficultyColors[template.difficulty]}
                            size="small"
                          />
                          <Chip
                            label={`사용: ${template.usageCount}회`}
                            size="small"
                            variant="outlined"
                          />
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                          {template.estimatedDurationMinutes}분 · {template.initialBalance.toLocaleString()}원
                        </Typography>
                      </CardContent>
                      <CardActions>
                        <Button size="small" href={`/admin/templates/${template.id}`}>
                          상세보기
                        </Button>
                        <Button size="small" href={`/admin/templates/${template.id}/edit`}>
                          수정
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Card>
                <CardContent sx={{ textAlign: 'center', py: 3 }}>
                  <Typography color="text.secondary">
                    이 카테고리에는 템플릿이 없습니다
                  </Typography>
                </CardContent>
              </Card>
            )}
            <Divider sx={{ mt: 3 }} />
          </Box>
        ))}
      </TabPanel>

      {/* 액션 메뉴 */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        {selectedTemplate && (
          <>
            <MenuItem onClick={() => window.open(`/admin/templates/${selectedTemplate.id}`, '_blank')}>
              <Visibility sx={{ mr: 1 }} />
              상세보기
            </MenuItem>
            <MenuItem onClick={() => window.open(`/admin/templates/${selectedTemplate.id}/edit`, '_blank')}>
              <Edit sx={{ mr: 1 }} />
              수정하기
            </MenuItem>
            <MenuItem onClick={() => handleClone(selectedTemplate)}>
              <FileCopy sx={{ mr: 1 }} />
              복제하기
            </MenuItem>
            <MenuItem onClick={() => handleToggleActive(selectedTemplate)}>
              {selectedTemplate.isActive ? '비활성화' : '활성화'}
            </MenuItem>
            <MenuItem onClick={() => handleDelete(selectedTemplate)} sx={{ color: 'error.main' }}>
              <Delete sx={{ mr: 1 }} />
              삭제하기
            </MenuItem>
          </>
        )}
      </Menu>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>템플릿 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            "{templateToDelete?.name}" 템플릿을 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 템플릿으로 생성된 챌린지는 영향받지 않지만, 템플릿 자체는 복구할 수 없습니다.
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