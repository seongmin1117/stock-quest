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
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  LinearProgress
} from '@mui/material';
import {
  ArrowBack,
  Edit,
  Delete,
  FileCopy,
  Add,
  Star,
  Schedule,
  AttachMoney,
  Speed,
  TrendingUp,
  Assessment,
  Visibility,
  Launch
} from '@mui/icons-material';
import { useParams, useRouter } from 'next/navigation';

// 타입 정의
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
  marketScenario: string;
  config: any;
}

interface ChallengeFromTemplate {
  id: number;
  title: string;
  status: 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
  participants: number;
  createdAt: string;
  createdBy: string;
}

// 임시 데이터
const mockTemplate: ChallengeTemplate = {
  id: 1,
  name: '2008 금융위기 생존하기',
  description: '2008년 글로벌 금융위기 상황에서 포트폴리오를 방어하고 회복하는 전략을 학습하세요. 리먼 브라더스 파산으로 시작된 금융 시스템의 붕괴 상황에서 투자자들이 어떻게 대응해야 하는지 실제 역사적 데이터를 바탕으로 학습할 수 있습니다.',
  category: 'market-crash',
  difficulty: 'ADVANCED',
  templateType: 'MARKET_CRASH',
  initialBalance: 100000,
  estimatedDurationMinutes: 45,
  speedFactor: 20,
  tags: ['금융위기', '리스크관리', '방어투자', '경기침체', '서브프라임', '리먼브라더스'],
  isActive: true,
  usageCount: 23,
  createdAt: '2024-01-01T09:00:00',
  updatedAt: '2024-01-15T14:30:00',
  learningObjectives: `• 극심한 시장 급락 상황에서의 심리적 대응 방법 학습
• 포트폴리오 리스크 관리와 손실 최소화 전략
• 방어주와 안전 자산으로의 포지션 전환 타이밍
• 시장 회복기 진입 시점 판단과 재투자 전략
• 레버리지 리스크와 유동성 관리의 중요성`,
  successCriteria: {
    minReturn: -20,
    maxDrawdown: 30,
    riskAdjustedReturn: 0.5,
    recoveryTime: 180
  },
  marketScenario: `2007년 8월부터 2009년 3월까지의 실제 금융위기 기간을 시뮬레이션합니다.
• 2007년 8월: 서브프라임 모기지 위기 시작
• 2008년 3월: 베어스턴스 구제금융
• 2008년 9월: 리먼 브라더스 파산, AIG 국유화
• 2008년 10월: TARP 프로그램 발표
• 2009년 3월: 시장 바닥권 형성 및 회복 시작

이 기간 동안 S&P 500은 약 57% 하락했으며, 금융, 부동산 섹터는 더욱 큰 타격을 받았습니다.`,
  config: {
    availableInstruments: ['STOCKS', 'BONDS', 'CASH', 'GOLD'],
    tradingFees: 0.1,
    allowShortSelling: true,
    marginRequirement: 50,
    maxLeverage: 2
  }
};

const mockChallengesFromTemplate: ChallengeFromTemplate[] = [
  {
    id: 101,
    title: '2008 금융위기 챌린지 - 1월',
    status: 'ACTIVE',
    participants: 45,
    createdAt: '2024-01-15T09:00:00',
    createdBy: 'admin@stockquest.com'
  },
  {
    id: 102,
    title: '2008 금융위기 챌린지 - 고급반',
    status: 'COMPLETED',
    participants: 32,
    createdAt: '2024-01-05T14:00:00',
    createdBy: 'admin@stockquest.com'
  },
  {
    id: 103,
    title: '금융위기 시뮬레이션 테스트',
    status: 'DRAFT',
    participants: 0,
    createdAt: '2024-01-20T11:00:00',
    createdBy: 'manager@stockquest.com'
  }
];

const categoryNames: Record<string, string> = {
  'market-crash': '시장 급락 시나리오',
  'bull-market': '상승장 전략',
  'sector-rotation': '섹터 순환',
  'volatility': '변동성 관리',
  'esg': 'ESG 투자',
  'international': '국제 투자',
  'risk-management': '리스크 관리'
};

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

export default function TemplateDetailPage() {
  const params = useParams();
  const router = useRouter();
  const templateId = params.id as string;

  const [template, setTemplate] = useState<ChallengeTemplate | null>(null);
  const [challenges, setChallenges] = useState<ChallengeFromTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  // 다이얼로그 상태
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [createChallengeDialogOpen, setCreateChallengeDialogOpen] = useState(false);

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        setLoading(true);
        await new Promise(resolve => setTimeout(resolve, 500));

        if (templateId === '1') {
          setTemplate(mockTemplate);
          setChallenges(mockChallengesFromTemplate);
        } else {
          setError('템플릿을 찾을 수 없습니다.');
        }
      } catch (err) {
        setError('템플릿 정보를 불러오는 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchTemplate();
  }, [templateId]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleClone = () => {
    // TODO: API 호출로 템플릿 복제
    router.push('/admin/templates/new?clone=' + templateId);
  };

  const handleDelete = () => {
    // TODO: API 호출로 템플릿 삭제
    setDeleteDialogOpen(false);
    router.push('/admin/templates');
  };

  const handleCreateChallenge = () => {
    // TODO: API 호출로 챌린지 생성
    setCreateChallengeDialogOpen(false);
    router.push('/admin/challenges/new?template=' + templateId);
  };

  const toggleActive = () => {
    if (template) {
      setTemplate({ ...template, isActive: !template.isActive });
      // TODO: API 호출
    }
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
        </Grid>
      </Box>
    );
  }

  if (error || !template) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error || '템플릿을 찾을 수 없습니다.'}
        </Alert>
        <Button
          variant="outlined"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/templates')}
        >
          템플릿 목록으로
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
            onClick={() => router.push('/admin/templates')}
            sx={{ mb: 2 }}
          >
            템플릿 목록으로
          </Button>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
            {template.name}
          </Typography>
          <Box display="flex" gap={1} mb={2}>
            <Chip
              label={categoryNames[template.category] || template.category}
              color="primary"
              variant="outlined"
            />
            <Chip
              label={template.difficulty === 'BEGINNER' ? '초급' :
                    template.difficulty === 'INTERMEDIATE' ? '중급' : '고급'}
              color={difficultyColors[template.difficulty]}
            />
            <Chip
              label={template.isActive ? '활성' : '비활성'}
              color={template.isActive ? 'success' : 'default'}
            />
            <Chip
              label={`사용: ${template.usageCount}회`}
              variant="outlined"
            />
          </Box>
        </Box>

        {/* 액션 버튼들 */}
        <Box display="flex" gap={1}>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setCreateChallengeDialogOpen(true)}
          >
            챌린지 생성
          </Button>
          <Button
            variant="outlined"
            startIcon={<FileCopy />}
            onClick={handleClone}
          >
            복제
          </Button>
          <Button
            variant="outlined"
            startIcon={<Edit />}
            href={`/admin/templates/${templateId}/edit`}
          >
            수정
          </Button>
          <Button
            variant="outlined"
            onClick={toggleActive}
          >
            {template.isActive ? '비활성화' : '활성화'}
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
          <Tab label="템플릿 정보" />
          <Tab label="생성된 챌린지" />
          <Tab label="설정 및 관리" />
        </Tabs>
      </Box>

      {/* 템플릿 정보 탭 */}
      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {/* 설명 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  템플릿 설명
                </Typography>
                <Typography variant="body1" sx={{ lineHeight: 1.6 }}>
                  {template.description}
                </Typography>
              </CardContent>
            </Card>

            {/* 학습 목표 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  학습 목표
                </Typography>
                <Typography variant="body1" sx={{ whiteSpace: 'pre-line' }}>
                  {template.learningObjectives}
                </Typography>
              </CardContent>
            </Card>

            {/* 시장 시나리오 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  시장 시나리오
                </Typography>
                <Typography variant="body1" sx={{ whiteSpace: 'pre-line' }}>
                  {template.marketScenario}
                </Typography>
              </CardContent>
            </Card>

            {/* 성공 기준 */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  성공 기준
                </Typography>
                <Grid container spacing={2}>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="error.main">
                        {template.successCriteria.minReturn}%
                      </Typography>
                      <Typography variant="caption">최소 수익률</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="warning.main">
                        {template.successCriteria.maxDrawdown}%
                      </Typography>
                      <Typography variant="caption">최대 손실률</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="info.main">
                        {template.successCriteria.riskAdjustedReturn}
                      </Typography>
                      <Typography variant="caption">위험조정수익률</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="success.main">
                        {template.successCriteria.recoveryTime}일
                      </Typography>
                      <Typography variant="caption">회복 기간</Typography>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 기본 정보 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  기본 설정
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemIcon>
                      <AttachMoney />
                    </ListItemIcon>
                    <ListItemText
                      primary="초기 자금"
                      secondary={`${template.initialBalance.toLocaleString()}원`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <Schedule />
                    </ListItemIcon>
                    <ListItemText
                      primary="예상 소요 시간"
                      secondary={`${template.estimatedDurationMinutes}분`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <Speed />
                    </ListItemIcon>
                    <ListItemText
                      primary="시간 압축률"
                      secondary={`${template.speedFactor}배속`}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>

            {/* 사용 통계 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  사용 현황
                </Typography>
                <Box mb={2}>
                  <Typography variant="body2" color="text.secondary">
                    총 사용 횟수
                  </Typography>
                  <Typography variant="h4" color="primary.main">
                    {template.usageCount}회
                  </Typography>
                </Box>
                <LinearProgress
                  variant="determinate"
                  value={Math.min(template.usageCount * 2, 100)}
                  sx={{ mb: 1 }}
                />
                <Typography variant="caption" color="text.secondary">
                  인기도: {Math.min(template.usageCount * 2, 100)}%
                </Typography>
              </CardContent>
            </Card>

            {/* 태그 */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  태그
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {template.tags.map((tag) => (
                    <Chip key={tag} label={tag} variant="outlined" size="small" />
                  ))}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* 생성된 챌린지 탭 */}
      <TabPanel value={tabValue} index={1}>
        <Card>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Typography variant="h6">
                이 템플릿으로 생성된 챌린지 ({challenges.length}개)
              </Typography>
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={() => setCreateChallengeDialogOpen(true)}
              >
                새 챌린지 생성
              </Button>
            </Box>

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>챌린지 제목</TableCell>
                    <TableCell>상태</TableCell>
                    <TableCell align="right">참여자</TableCell>
                    <TableCell>생성일</TableCell>
                    <TableCell>생성자</TableCell>
                    <TableCell align="center">액션</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {challenges.map((challenge) => (
                    <TableRow key={challenge.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {challenge.title}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={
                            challenge.status === 'DRAFT' ? '초안' :
                            challenge.status === 'ACTIVE' ? '진행중' :
                            challenge.status === 'COMPLETED' ? '완료' : '보관'
                          }
                          color={statusColors[challenge.status]}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        {challenge.participants}명
                      </TableCell>
                      <TableCell>
                        {new Date(challenge.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {challenge.createdBy}
                        </Typography>
                      </TableCell>
                      <TableCell align="center">
                        <Button
                          size="small"
                          startIcon={<Launch />}
                          href={`/admin/challenges/${challenge.id}`}
                        >
                          보기
                        </Button>
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
                      primary="생성일"
                      secondary={new Date(template.createdAt).toLocaleString()}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="최종 수정일"
                      secondary={new Date(template.updatedAt).toLocaleString()}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="템플릿 유형"
                      secondary={template.templateType}
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
                    사용 통계 분석
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<Star />}
                    fullWidth
                  >
                    피처드 템플릿 설정
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<FileCopy />}
                    onClick={handleClone}
                    fullWidth
                  >
                    템플릿 복제
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </TabPanel>

      {/* 삭제 확인 다이얼로그 */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>템플릿 삭제 확인</DialogTitle>
        <DialogContent>
          <Typography>
            "{template.name}" 템플릿을 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 템플릿으로 생성된 챌린지는 영향받지 않지만, 템플릿 자체는 복구할 수 없습니다.
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

      {/* 챌린지 생성 다이얼로그 */}
      <Dialog open={createChallengeDialogOpen} onClose={() => setCreateChallengeDialogOpen(false)}>
        <DialogTitle>새 챌린지 생성</DialogTitle>
        <DialogContent>
          <Typography>
            "{template.name}" 템플릿을 사용하여 새로운 챌린지를 생성하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            템플릿의 모든 설정이 적용된 챌린지가 생성됩니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateChallengeDialogOpen(false)}>
            취소
          </Button>
          <Button onClick={handleCreateChallenge} variant="contained">
            생성
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}