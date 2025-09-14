'use client';

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Alert,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  LinearProgress,
  Tooltip,
  IconButton,
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  ExpandMore,
  Refresh,
  Analytics,
  Timeline,
  AccountBalance,
  ShowChart,
  Info,
} from '@mui/icons-material';
import {
  usePostApiV1MlPortfolioOptimizationPortfolioIdOptimize,
  usePostApiV1MlPortfolioOptimizationPortfolioIdEfficientFrontier,
  useGetApiV1MlPortfolioOptimizationPortfolioIdRebalancingSuggestions,
  useGetApiV1MlPortfolioOptimizationPortfolioIdHistory,
  usePostApiV1MlPortfolioOptimizationPortfolioIdBacktest,
} from '@/shared/api/generated/포트폴리오-최적화/포트폴리오-최적화';
import {
  OptimizationRequestObjective,
  OptimizationRequestOptimizationType,
  type OptimizationRequest,
  type EfficientFrontierRequest,
  type BacktestRequest,
  type BacktestRequestObjective,
  type BacktestRequestOptimizationType,
  type PortfolioOptimizationResponse,
  type EfficientFrontierResponse,
  type RebalancingSuggestionsResponse,
  type OptimizationHistoryResponse,
  type BacktestResponse,
} from '@/shared/api/generated/model';

interface PortfolioOptimizationPanelProps {
  portfolioId: number;
}

interface OptimizationFormData {
  objective: OptimizationRequestObjective;
  optimizationType: OptimizationRequestOptimizationType;
  targetVolatility: string;
  minWeight: string;
  maxWeight: string;
}

interface BacktestFormData {
  objective: BacktestRequestObjective;
  optimizationType: BacktestRequestOptimizationType;
  startDate: string;
  endDate: string;
  initialCapital: string;
  rebalancingFrequency: string;
}

const objectiveLabels = {
  [OptimizationRequestObjective.MAXIMIZE_RETURN]: '수익률 최대화',
  [OptimizationRequestObjective.MINIMIZE_RISK]: '리스크 최소화',
  [OptimizationRequestObjective.MAXIMIZE_SHARPE_RATIO]: '샤프 비율 최대화',
  [OptimizationRequestObjective.TARGET_VOLATILITY]: '목표 변동성',
};

const optimizationTypeLabels = {
  [OptimizationRequestOptimizationType.MODERN_PORTFOLIO_THEORY]: '현대 포트폴리오 이론',
  [OptimizationRequestOptimizationType.BLACK_LITTERMAN]: '블랙-리터만 모델',
  [OptimizationRequestOptimizationType.RISK_PARITY]: '리스크 패리티',
  [OptimizationRequestOptimizationType.EQUAL_WEIGHT]: '동일 가중',
};

export function PortfolioOptimizationPanel({ portfolioId }: PortfolioOptimizationPanelProps) {
  const [optimizationForm, setOptimizationForm] = React.useState<OptimizationFormData>({
    objective: OptimizationRequestObjective.MAXIMIZE_SHARPE_RATIO,
    optimizationType: OptimizationRequestOptimizationType.MODERN_PORTFOLIO_THEORY,
    targetVolatility: '15.0',
    minWeight: '0.0',
    maxWeight: '30.0',
  });

  const [backtestForm, setBacktestForm] = React.useState<BacktestFormData>({
    objective: 'MAXIMIZE_SHARPE_RATIO' as BacktestRequestObjective,
    optimizationType: 'MODERN_PORTFOLIO_THEORY' as BacktestRequestOptimizationType,
    startDate: '2023-01-01',
    endDate: '2024-01-01',
    initialCapital: '1000000',
    rebalancingFrequency: 'MONTHLY',
  });

  const [efficientFrontierRequest, setEfficientFrontierRequest] = React.useState<EfficientFrontierRequest>({
    points: 100,
    minWeight: '0.0',
    maxWeight: '100.0',
    riskLevels: ['0.05', '0.10', '0.15', '0.20', '0.25'],
  });

  const [activeTab, setActiveTab] = React.useState('optimization');
  const [optimizationResult, setOptimizationResult] = React.useState<PortfolioOptimizationResponse | null>(null);
  const [efficientFrontierResult, setEfficientFrontierResult] = React.useState<EfficientFrontierResponse | null>(null);
  const [backtestResult, setBacktestResult] = React.useState<BacktestResponse | null>(null);

  // Fetch rebalancing suggestions
  const { data: rebalancingSuggestions, isLoading: rebalancingLoading, refetch: refetchRebalancing } =
    useGetApiV1MlPortfolioOptimizationPortfolioIdRebalancingSuggestions(portfolioId, {
      query: {
        enabled: !isNaN(portfolioId) && portfolioId > 0,
        refetchInterval: 300000, // Refresh every 5 minutes
      }
    });

  // Fetch optimization history
  const { data: optimizationHistory, isLoading: historyLoading, refetch: refetchHistory } =
    useGetApiV1MlPortfolioOptimizationPortfolioIdHistory(portfolioId, {}, {
      query: {
        enabled: !isNaN(portfolioId) && portfolioId > 0,
      }
    });

  // Portfolio optimization mutation
  const { mutate: runOptimization, isPending: optimizationPending } =
    usePostApiV1MlPortfolioOptimizationPortfolioIdOptimize({
      mutation: {
        onSuccess: (data) => {
          setOptimizationResult(data);
          refetchRebalancing();
          refetchHistory();
        },
        onError: (error) => {
          console.error('Portfolio optimization failed:', error);
        },
      },
    });

  // Efficient frontier mutation
  const { mutate: calculateEfficientFrontier, isPending: frontierPending } =
    usePostApiV1MlPortfolioOptimizationPortfolioIdEfficientFrontier({
      mutation: {
        onSuccess: (data) => {
          setEfficientFrontierResult(data);
        },
        onError: (error) => {
          console.error('Efficient frontier calculation failed:', error);
        },
      },
    });

  // Backtest mutation
  const { mutate: runBacktest, isPending: backtestPending } =
    usePostApiV1MlPortfolioOptimizationPortfolioIdBacktest({
      mutation: {
        onSuccess: (data) => {
          setBacktestResult(data);
        },
        onError: (error) => {
          console.error('Backtest failed:', error);
        },
      },
    });

  const handleOptimizationSubmit = () => {
    const optimizationData: OptimizationRequest = {
      objective: optimizationForm.objective,
      optimizationType: optimizationForm.optimizationType,
      targetVolatility: optimizationForm.targetVolatility,
      minWeight: optimizationForm.minWeight,
      maxWeight: optimizationForm.maxWeight,
    };

    runOptimization({
      portfolioId,
      data: optimizationData,
    });
  };

  const handleEfficientFrontierSubmit = () => {
    calculateEfficientFrontier({
      portfolioId,
      data: efficientFrontierRequest,
    });
  };

  const handleBacktestSubmit = () => {
    const backtestData: BacktestRequest = {
      objective: backtestForm.objective,
      optimizationType: backtestForm.optimizationType,
      startDate: backtestForm.startDate,
      endDate: backtestForm.endDate,
    };

    runBacktest({
      portfolioId,
      data: backtestData,
    });
  };

  const formatCurrency = (value: string | number | undefined) => {
    if (value === undefined || value === null) return '₩0';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    return `₩${numValue.toLocaleString()}`;
  };

  const formatPercentage = (value: string | number | undefined) => {
    if (value === undefined || value === null) return '0.00%';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    return `${numValue.toFixed(2)}%`;
  };

  if (!portfolioId || isNaN(portfolioId)) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        유효한 포트폴리오 ID가 필요합니다.
      </Alert>
    );
  }

  return (
    <Box sx={{ width: '100%' }}>
      <Typography variant="h5" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
        <AccountBalance />
        포트폴리오 최적화
        <Chip
          label={`Portfolio ${portfolioId}`}
          variant="outlined"
          size="small"
        />
      </Typography>

      <Grid container spacing={3}>
        {/* Optimization Configuration */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Analytics />
                포트폴리오 최적화 실행
              </Typography>

              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <FormControl fullWidth>
                    <InputLabel>최적화 목표</InputLabel>
                    <Select
                      value={optimizationForm.objective}
                      label="최적화 목표"
                      onChange={(e) => setOptimizationForm(prev => ({
                        ...prev,
                        objective: e.target.value as OptimizationRequestObjective
                      }))}
                    >
                      {Object.entries(objectiveLabels).map(([value, label]) => (
                        <MenuItem key={value} value={value}>
                          {label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12}>
                  <FormControl fullWidth>
                    <InputLabel>최적화 유형</InputLabel>
                    <Select
                      value={optimizationForm.optimizationType}
                      label="최적화 유형"
                      onChange={(e) => setOptimizationForm(prev => ({
                        ...prev,
                        optimizationType: e.target.value as OptimizationRequestOptimizationType
                      }))}
                    >
                      {Object.entries(optimizationTypeLabels).map(([value, label]) => (
                        <MenuItem key={value} value={value}>
                          {label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>

                {optimizationForm.objective === OptimizationRequestObjective.TARGET_VOLATILITY && (
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="목표 변동성 (%)"
                      type="number"
                      value={optimizationForm.targetVolatility}
                      onChange={(e) => setOptimizationForm(prev => ({
                        ...prev,
                        targetVolatility: e.target.value
                      }))}
                      inputProps={{ min: 0, max: 100, step: 0.1 }}
                    />
                  </Grid>
                )}

                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="최소 종목 비중 (%)"
                    type="number"
                    value={optimizationForm.minWeight}
                    onChange={(e) => setOptimizationForm(prev => ({
                      ...prev,
                      minWeight: e.target.value
                    }))}
                    inputProps={{ min: 0, max: 100, step: 0.1 }}
                  />
                </Grid>

                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="최대 종목 비중 (%)"
                    type="number"
                    value={optimizationForm.maxWeight}
                    onChange={(e) => setOptimizationForm(prev => ({
                      ...prev,
                      maxWeight: e.target.value
                    }))}
                    inputProps={{ min: 0, max: 100, step: 0.1 }}
                  />
                </Grid>

                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={handleOptimizationSubmit}
                    disabled={optimizationPending}
                    startIcon={optimizationPending ? <CircularProgress size={20} /> : <Analytics />}
                  >
                    {optimizationPending ? '최적화 실행 중...' : '포트폴리오 최적화 실행'}
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Rebalancing Suggestions */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Timeline />
                리밸런싱 제안
                <IconButton size="small" onClick={() => refetchRebalancing()}>
                  <Refresh />
                </IconButton>
              </Typography>

              {rebalancingLoading ? (
                <CircularProgress />
              ) : rebalancingSuggestions?.suggestions?.length ? (
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>종목</TableCell>
                        <TableCell>액션</TableCell>
                        <TableCell>수량</TableCell>
                        <TableCell>우선순위</TableCell>
                        <TableCell>금액</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rebalancingSuggestions.suggestions.map((suggestion, index) => (
                        <TableRow key={index}>
                          <TableCell>{suggestion.symbol}</TableCell>
                          <TableCell>
                            <Chip
                              label={suggestion.actionType}
                              color={suggestion.actionType === 'BUY' ? 'success' :
                                     suggestion.actionType === 'SELL' ? 'error' : 'default'}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>{suggestion.quantity || 'N/A'}</TableCell>
                          <TableCell>{suggestion.priority || 'N/A'}</TableCell>
                          <TableCell>{formatCurrency(suggestion.amount)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  현재 리밸런싱 제안이 없습니다.
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Optimization Results */}
        {optimizationResult && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <TrendingUp />
                  최적화 결과
                  <Chip
                    label={`신뢰도: ${optimizationResult.confidence}%`}
                    variant="outlined"
                    size="small"
                  />
                </Typography>

                <Grid container spacing={3}>
                  {/* Performance Metrics */}
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" gutterBottom>예상 성과</Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2">
                        예상 수익률: {formatPercentage(optimizationResult.expectedPerformance?.expectedReturn)}
                      </Typography>
                      <Typography variant="body2">
                        예상 변동성: {formatPercentage(optimizationResult.expectedPerformance?.expectedVolatility)}
                      </Typography>
                      <Typography variant="body2">
                        예상 샤프 비율: {optimizationResult.expectedPerformance?.expectedSharpeRatio || 'N/A'}
                      </Typography>
                    </Box>
                  </Grid>

                  {/* Risk Metrics */}
                  <Grid item xs={12} md={6}>
                    <Typography variant="subtitle2" gutterBottom>리스크 지표</Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2">
                        VaR (95%): {formatCurrency(optimizationResult.riskMetrics?.var95)}
                      </Typography>
                      <Typography variant="body2">
                        CVaR (95%): {formatCurrency(optimizationResult.riskMetrics?.cvar95)}
                      </Typography>
                      <Typography variant="body2">
                        최대 손실: {formatCurrency(optimizationResult.riskMetrics?.var95)}
                      </Typography>
                    </Box>
                  </Grid>

                  {/* Current vs Recommended Allocations */}
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" gutterBottom>자산 배분 비교</Typography>
                    <TableContainer component={Paper} variant="outlined">
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>종목</TableCell>
                            <TableCell>현재 비중</TableCell>
                            <TableCell>권장 비중</TableCell>
                            <TableCell>차이</TableCell>
                            <TableCell>권장 금액</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {optimizationResult.recommendedAllocations?.map((allocation, index) => {
                            const currentAllocation = optimizationResult.currentAllocations?.find(
                              current => current.symbol === allocation.symbol
                            );
                            const currentWeight = currentAllocation?.currentWeight ? parseFloat(currentAllocation.currentWeight) : 0;
                            const recommendedWeight = parseFloat(allocation.recommendedWeight || '0');
                            const difference = recommendedWeight - currentWeight;

                            return (
                              <TableRow key={allocation.symbol || index}>
                                <TableCell>{allocation.symbol}</TableCell>
                                <TableCell>{formatPercentage(currentWeight)}</TableCell>
                                <TableCell>{formatPercentage(recommendedWeight)}</TableCell>
                                <TableCell>
                                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                    {difference > 0 ? (
                                      <TrendingUp color="success" fontSize="small" />
                                    ) : difference < 0 ? (
                                      <TrendingDown color="error" fontSize="small" />
                                    ) : null}
                                    {formatPercentage(Math.abs(difference))}
                                  </Box>
                                </TableCell>
                                <TableCell>{formatCurrency(allocation.expectedReturn)}</TableCell>
                              </TableRow>
                            );
                          })}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Efficient Frontier */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <ShowChart />
                효율적 프론티어
              </Typography>

              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="포트폴리오 개수"
                    type="number"
                    value={efficientFrontierRequest.points}
                    onChange={(e) => setEfficientFrontierRequest(prev => ({
                      ...prev,
                      points: parseInt(e.target.value)
                    }))}
                    inputProps={{ min: 10, max: 1000 }}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    fullWidth
                    label="최소 비중 (%)"
                    type="number"
                    value={efficientFrontierRequest.minWeight}
                    onChange={(e) => setEfficientFrontierRequest(prev => ({
                      ...prev,
                      minWeight: e.target.value
                    }))}
                    inputProps={{ min: -1, max: 1, step: 0.01 }}
                  />
                </Grid>
                <Grid item xs={3}>
                  <TextField
                    fullWidth
                    label="최대 비중 (%)"
                    type="number"
                    value={efficientFrontierRequest.maxWeight}
                    onChange={(e) => setEfficientFrontierRequest(prev => ({
                      ...prev,
                      maxWeight: e.target.value
                    }))}
                    inputProps={{ min: -1, max: 1, step: 0.01 }}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={handleEfficientFrontierSubmit}
                    disabled={frontierPending}
                    startIcon={frontierPending ? <CircularProgress size={20} /> : <ShowChart />}
                  >
                    {frontierPending ? '계산 중...' : '효율적 프론티어 계산'}
                  </Button>
                </Grid>
              </Grid>

              {efficientFrontierResult && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    {efficientFrontierResult.frontierPoints?.length}개의 효율적 포트폴리오가 계산되었습니다.
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    총 {efficientFrontierResult.frontierPoints?.length || 0}개의 효율적 포트폴리오가 계산되었습니다.
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Backtesting */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Timeline />
                백테스팅
              </Typography>

              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <FormControl fullWidth>
                    <InputLabel>최적화 목표</InputLabel>
                    <Select
                      value={backtestForm.objective}
                      label="최적화 목표"
                      onChange={(e) => setBacktestForm(prev => ({
                        ...prev,
                        objective: e.target.value as BacktestRequestObjective
                      }))}
                    >
                      <MenuItem value="MAXIMIZE_RETURN">수익률 최대화</MenuItem>
                      <MenuItem value="MINIMIZE_RISK">리스크 최소화</MenuItem>
                      <MenuItem value="MAXIMIZE_SHARPE_RATIO">샤프 비율 최대화</MenuItem>
                      <MenuItem value="TARGET_VOLATILITY">목표 변동성</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={6}>
                  <FormControl fullWidth>
                    <InputLabel>최적화 유형</InputLabel>
                    <Select
                      value={backtestForm.optimizationType}
                      label="최적화 유형"
                      onChange={(e) => setBacktestForm(prev => ({
                        ...prev,
                        optimizationType: e.target.value as BacktestRequestOptimizationType
                      }))}
                    >
                      <MenuItem value="MODERN_PORTFOLIO_THEORY">현대 포트폴리오 이론</MenuItem>
                      <MenuItem value="BLACK_LITTERMAN">블랙-리터만 모델</MenuItem>
                      <MenuItem value="RISK_PARITY">리스크 패리티</MenuItem>
                      <MenuItem value="EQUAL_WEIGHT">동일 가중</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="시작 날짜"
                    type="date"
                    value={backtestForm.startDate}
                    onChange={(e) => setBacktestForm(prev => ({
                      ...prev,
                      startDate: e.target.value
                    }))}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>

                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="종료 날짜"
                    type="date"
                    value={backtestForm.endDate}
                    onChange={(e) => setBacktestForm(prev => ({
                      ...prev,
                      endDate: e.target.value
                    }))}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>

                <Grid item xs={6}>
                  <TextField
                    fullWidth
                    label="초기 자본금"
                    type="number"
                    value={backtestForm.initialCapital}
                    onChange={(e) => setBacktestForm(prev => ({
                      ...prev,
                      initialCapital: e.target.value
                    }))}
                  />
                </Grid>

                <Grid item xs={6}>
                  <FormControl fullWidth>
                    <InputLabel>리밸런싱 주기</InputLabel>
                    <Select
                      value={backtestForm.rebalancingFrequency}
                      label="리밸런싱 주기"
                      onChange={(e) => setBacktestForm(prev => ({
                        ...prev,
                        rebalancingFrequency: e.target.value
                      }))}
                    >
                      <MenuItem value="DAILY">일별</MenuItem>
                      <MenuItem value="WEEKLY">주별</MenuItem>
                      <MenuItem value="MONTHLY">월별</MenuItem>
                      <MenuItem value="QUARTERLY">분기별</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={handleBacktestSubmit}
                    disabled={backtestPending}
                    startIcon={backtestPending ? <CircularProgress size={20} /> : <Timeline />}
                  >
                    {backtestPending ? '백테스트 실행 중...' : '백테스트 실행'}
                  </Button>
                </Grid>
              </Grid>

              {backtestResult && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2">
                    총 수익률: {formatPercentage(backtestResult.totalReturn)}
                  </Typography>
                  <Typography variant="body2">
                    연율화 수익률: {formatPercentage(backtestResult.totalReturn)}
                  </Typography>
                  <Typography variant="body2">
                    최대 손실률: {formatPercentage(backtestResult.maxDrawdown)}
                  </Typography>
                  <Typography variant="body2">
                    샤프 비율: {backtestResult.sharpeRatio || 'N/A'}
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Optimization History */}
        {optimizationHistory && optimizationHistory.optimizations && optimizationHistory.optimizations.length > 0 && (
          <Grid item xs={12}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Info />
                  최적화 이력 ({optimizationHistory.optimizations?.length || 0})
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <TableContainer component={Paper} variant="outlined">
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>날짜</TableCell>
                        <TableCell>최적화 유형</TableCell>
                        <TableCell>목표</TableCell>
                        <TableCell>결과 수익률</TableCell>
                        <TableCell>결과 리스크</TableCell>
                        <TableCell>샤프 비율</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {optimizationHistory.optimizations?.map((optimization, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {optimization.generatedAt ? new Date(optimization.generatedAt).toLocaleDateString('ko-KR') : 'N/A'}
                          </TableCell>
                          <TableCell>{optimization.optimizationType}</TableCell>
                          <TableCell>{optimization.objective}</TableCell>
                          <TableCell>{formatPercentage(optimization.expectedPerformance?.expectedReturn)}</TableCell>
                          <TableCell>{formatPercentage(optimization.expectedPerformance?.expectedVolatility)}</TableCell>
                          <TableCell>{optimization.expectedPerformance?.expectedSharpeRatio || 'N/A'}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </AccordionDetails>
            </Accordion>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}