'use client';

import React, { useState, useRef } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Alert,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Divider,
} from '@mui/material';
import {
  TrendingUp,
  ShowChart,
  Assessment,
  Download,
  PictureAsPdf,
  Timeline,
  MonetizationOn,
  Calculate,
  Speed,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { dcaClient } from '@/shared/api/dca-client';
import { Company } from '@/shared/api/company-client';
import CompanyAutocomplete from '@/shared/components/CompanyAutocomplete/CompanyAutocomplete';
import DatePresets from '@/shared/components/DatePresets/DatePresets';
import InvestmentAmountPresets from '@/shared/components/InvestmentAmountPresets/InvestmentAmountPresets';
import InvestmentStrategyTemplates, {
  type InvestmentStrategy
} from '@/shared/components/InvestmentStrategyTemplates/InvestmentStrategyTemplates';
import type {
  DCASimulationRequest,
  DCASimulationResponse,
  DCAMonthlyRecord,
  InvestmentFrequency
} from '@/shared/api/types/dca-types';

const DCASimulationPage = () => {
  // 폼 상태
  const [symbol, setSymbol] = useState('');
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [monthlyInvestmentAmount, setMonthlyInvestmentAmount] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [frequency, setFrequency] = useState<InvestmentFrequency>('MONTHLY');

  // UI 상태 추가
  const [selectedDatePreset, setSelectedDatePreset] = useState<string>('');
  const [selectedAmountPreset, setSelectedAmountPreset] = useState<string>('');
  const [selectedStrategy, setSelectedStrategy] = useState<string>('');

  // 시뮬레이션 상태
  const [isLoading, setIsLoading] = useState(false);
  const [simulationResult, setSimulationResult] = useState<DCASimulationResponse | null>(null);
  const [error, setError] = useState<string>('');

  // 폼 검증 상태
  const [validationErrors, setValidationErrors] = useState<{[key: string]: string}>({});

  const chartRef = useRef(null);

  // 핸들러 함수들
  const handleCompanySelect = (selectedSymbol: string, company?: Company) => {
    setSymbol(selectedSymbol);
    setSelectedCompany(company || null);
    // 검증 에러 클리어
    if (selectedSymbol) {
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors.symbol;
        return newErrors;
      });
    }
  };

  const handleDatePresetSelect = (startDateStr: string, endDateStr: string) => {
    setStartDate(startDateStr);
    setEndDate(endDateStr);
    setSelectedDatePreset(''); // 선택 후 하이라이트 해제
    // 검증 에러 클리어
    setValidationErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors.startDate;
      delete newErrors.endDate;
      delete newErrors.dateRange;
      return newErrors;
    });
  };

  const handleAmountPresetSelect = (amount: string) => {
    setMonthlyInvestmentAmount(amount);
    setSelectedAmountPreset(''); // 선택 후 하이라이트 해제
    // 검증 에러 클리어
    setValidationErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors.amount;
      return newErrors;
    });
  };

  const handleStrategySelect = (strategy: InvestmentStrategy) => {
    // 전략에 따라 모든 폼 값 설정
    setMonthlyInvestmentAmount(strategy.monthlyAmount.toString());
    setFrequency(strategy.frequency);

    // 날짜 설정
    const [startDateStr, endDateStr] = strategy.duration.split(',');
    setStartDate(startDateStr);
    setEndDate(endDateStr);

    setSelectedStrategy(strategy.id);

    // 모든 검증 에러 클리어
    setValidationErrors({});
  };

  // 폼 검증 함수
  const validateForm = (): boolean => {
    const errors: {[key: string]: string} = {};

    if (!symbol.trim()) {
      errors.symbol = '종목 코드는 필수입니다';
    }

    if (!monthlyInvestmentAmount || parseFloat(monthlyInvestmentAmount) <= 0) {
      errors.amount = '투자 금액은 필수입니다';
    }

    if (!startDate) {
      errors.startDate = '시작일은 필수입니다';
    }

    if (!endDate) {
      errors.endDate = '종료일은 필수입니다';
    }

    if (startDate && endDate && new Date(startDate) >= new Date(endDate)) {
      errors.dateRange = '종료일은 시작일보다 늦어야 합니다';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // 시뮬레이션 실행
  const handleSimulate = async () => {
    setError('');
    setSimulationResult(null);

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      const request: DCASimulationRequest = {
        symbol: symbol.toUpperCase(),
        monthlyInvestmentAmount: parseFloat(monthlyInvestmentAmount),
        startDate: new Date(startDate).toISOString(),
        endDate: new Date(endDate).toISOString(),
        frequency,
      };

      const result = await dcaClient.simulate(request);
      setSimulationResult(result);
    } catch (err: any) {
      setError(err.message || '시뮬레이션 실행 중 오류가 발생했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  // CSV 다운로드
  const handleDownloadCSV = () => {
    if (!simulationResult) return;

    const csvData = [
      ['투자일', '투자금액', '주식가격', '매수주식수', '포트폴리오가치'],
      ...simulationResult.investmentRecords.map(record => [
        new Date(record.investmentDate).toLocaleDateString(),
        record.investmentAmount,
        record.stockPrice,
        record.sharesPurchased,
        record.portfolioValue
      ])
    ];

    const csvContent = csvData.map(row => row.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `dca-simulation-${simulationResult.symbol}-${Date.now()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // PDF 리포트 다운로드 (완전 구현)
  const handleDownloadPDF = async () => {
    if (!simulationResult) return;

    try {
      // 동적 import로 PDF 생성 모듈 로드
      const { PDFReportGenerator } = await import('@/shared/utils/pdf-report-generator');

      const generator = new PDFReportGenerator();
      await generator.generateReport({
        simulationResult,
        selectedCompany,
        chartElement: chartRef.current,
        includeChart: true,
        includeDetailedTable: true,
      });

      const filename = `DCA-Report-${simulationResult.symbol}-${new Date().toISOString().split('T')[0]}.pdf`;
      generator.save(filename);
    } catch (error) {
      console.error('PDF 생성 중 오류:', error);
      alert('PDF 리포트 생성 중 오류가 발생했습니다.');
    }
  };

  // 차트 데이터 변환
  const getChartData = () => {
    if (!simulationResult) return [];

    return simulationResult.investmentRecords.map(record => ({
      date: new Date(record.investmentDate).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'numeric'
      }),
      portfolioValue: record.portfolioValue,
      cumulativeInvestment: simulationResult.investmentRecords
        .slice(0, simulationResult.investmentRecords.indexOf(record) + 1)
        .reduce((sum, r) => sum + r.investmentAmount, 0),
      benchmark1: Math.round(record.portfolioValue * (simulationResult.sp500ReturnAmount / simulationResult.finalPortfolioValue)),
      benchmark2: Math.round(record.portfolioValue * (simulationResult.nasdaqReturnAmount / simulationResult.finalPortfolioValue)),
    }));
  };

  // 숫자 포맷팅
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const formatPercent = (value: number) => {
    return `${value.toFixed(2)}%`;
  };

  // Advanced risk metrics calculation
  const calculateRiskMetrics = (result: DCASimulationResponse) => {
    if (!result.investmentRecords.length) return null;

    // Calculate monthly returns
    const returns: number[] = [];
    for (let i = 1; i < result.investmentRecords.length; i++) {
      const prevValue = result.investmentRecords[i - 1].portfolioValue;
      const currValue = result.investmentRecords[i].portfolioValue;
      const investmentAmount = result.investmentRecords[i].investmentAmount;

      const adjustedPrevValue = prevValue + investmentAmount;
      const monthlyReturn = (currValue - adjustedPrevValue) / adjustedPrevValue;
      returns.push(monthlyReturn);
    }

    if (returns.length === 0) return null;

    const avgReturn = returns.reduce((sum, r) => sum + r, 0) / returns.length;

    // Volatility (annualized standard deviation)
    const variance = returns.reduce((sum, r) => sum + Math.pow(r - avgReturn, 2), 0) / returns.length;
    const volatility = Math.sqrt(variance) * Math.sqrt(12) * 100;

    // Sharpe Ratio (assuming 2% risk-free rate)
    const riskFreeRate = 0.02 / 12;
    const excessReturn = avgReturn - riskFreeRate;
    const sharpeRatio = variance > 0 ? (excessReturn / Math.sqrt(variance)) * Math.sqrt(12) : 0;

    // Maximum Drawdown
    let peak = result.investmentRecords[0].portfolioValue;
    let maxDrawdown = 0;

    result.investmentRecords.forEach(record => {
      if (record.portfolioValue > peak) {
        peak = record.portfolioValue;
      } else {
        const drawdown = (peak - record.portfolioValue) / peak * 100;
        maxDrawdown = Math.max(maxDrawdown, drawdown);
      }
    });

    return {
      volatility,
      sharpeRatio,
      maxDrawdown,
    };
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 페이지 헤더 */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            DCA 시뮬레이션
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Dollar Cost Averaging 투자 전략 시뮬레이션
          </Typography>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* 입력 폼 */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              시뮬레이션 설정
            </Typography>

            {/* 투자 전략 템플릿 */}
            <InvestmentStrategyTemplates
              onSelect={handleStrategySelect}
              selectedStrategyId={selectedStrategy}
            />

            <Divider sx={{ my: 3 }} />

            {/* 회사 선택 */}
            <CompanyAutocomplete
              value={symbol}
              onChange={handleCompanySelect}
              label="회사 검색"
              placeholder="삼성전자, Samsung, 005930"
              error={!!validationErrors.symbol}
              helperText={validationErrors.symbol}
              fullWidth
              size="small"
            />

            {/* 투자 금액 프리셋 */}
            <InvestmentAmountPresets
              onSelect={handleAmountPresetSelect}
              selectedPreset={selectedAmountPreset}
            />

            <Box mb={2}>
              <TextField
                label="월 투자 금액"
                value={monthlyInvestmentAmount}
                onChange={(e) => setMonthlyInvestmentAmount(e.target.value)}
                fullWidth
                size="small"
                type="number"
                error={!!validationErrors.amount}
                helperText={validationErrors.amount}
                InputProps={{
                  endAdornment: '원'
                }}
              />
            </Box>

            {/* 날짜 프리셋 */}
            <DatePresets
              onSelect={handleDatePresetSelect}
              selectedPreset={selectedDatePreset}
            />

            <Box mb={2}>
              <TextField
                label="시작일"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                fullWidth
                size="small"
                type="date"
                InputLabelProps={{
                  shrink: true,
                }}
                error={!!validationErrors.startDate}
                helperText={validationErrors.startDate}
              />
            </Box>

            <Box mb={2}>
              <TextField
                label="종료일"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                fullWidth
                size="small"
                type="date"
                InputLabelProps={{
                  shrink: true,
                }}
                error={!!validationErrors.endDate || !!validationErrors.dateRange}
                helperText={validationErrors.endDate || validationErrors.dateRange}
              />
            </Box>

            <Box mb={3}>
              <FormControl fullWidth size="small">
                <InputLabel>투자 주기</InputLabel>
                <Select
                  value={frequency}
                  label="투자 주기"
                  onChange={(e) => setFrequency(e.target.value as InvestmentFrequency)}
                >
                  <MenuItem value="DAILY">일별</MenuItem>
                  <MenuItem value="WEEKLY">주별</MenuItem>
                  <MenuItem value="MONTHLY">월별</MenuItem>
                </Select>
              </FormControl>
            </Box>

            <Button
              variant="contained"
              fullWidth
              onClick={handleSimulate}
              disabled={isLoading}
              startIcon={<Calculate />}
              sx={{ mb: 2 }}
            >
              {isLoading ? '시뮬레이션 진행 중...' : '시뮬레이션 실행'}
            </Button>

            {isLoading && (
              <Box mb={2}>
                <LinearProgress />
                <Typography variant="caption" color="text.secondary" align="center" display="block" mt={1}>
                  시뮬레이션 진행 중...
                </Typography>
              </Box>
            )}

            {error && (
              <Alert severity="error" sx={{ mt: 2 }} role="alert">
                {error}
              </Alert>
            )}
          </Paper>
        </Grid>

        {/* 결과 표시 */}
        <Grid item xs={12} lg={8}>
          {simulationResult && (
            <Box>
              {/* 시뮬레이션 결과 헤더 */}
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  시뮬레이션 결과
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h6" color="primary">
                        ₩{formatCurrency(simulationResult.totalInvestmentAmount)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        총 투자금액
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h6" color="success.main">
                        ₩{formatCurrency(simulationResult.finalPortfolioValue)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        최종 가치
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h6" color="info.main">
                        {formatPercent(simulationResult.totalReturnPercentage)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        총 수익률
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6} sm={3}>
                    <Box textAlign="center">
                      <Typography variant="h6" color="warning.main">
                        {formatPercent(simulationResult.annualizedReturn)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        연평균 수익률
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Box textAlign="center">
                      <Typography variant="body2" color="text.secondary">
                        {selectedCompany?.exchange === 'KRX' ? 'KOSPI 대비' : 'S&P 500 대비'}
                      </Typography>
                      <Typography
                        variant="h6"
                        color={simulationResult.outperformanceVsSP500 >= 0 ? "success.main" : "error.main"}
                      >
                        {simulationResult.outperformanceVsSP500 >= 0 ? "+" : ""}{formatPercent(simulationResult.outperformanceVsSP500)}
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box textAlign="center">
                      <Typography variant="body2" color="text.secondary">
                        {selectedCompany?.exchange === 'KRX' ? 'KOSDAQ 대비' : 'NASDAQ 대비'}
                      </Typography>
                      <Typography
                        variant="h6"
                        color={simulationResult.outperformanceVsNASDAQ >= 0 ? "success.main" : "error.main"}
                      >
                        {simulationResult.outperformanceVsNASDAQ >= 0 ? "+" : ""}{formatPercent(simulationResult.outperformanceVsNASDAQ)}
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>

                <Box display="flex" justifyContent="center" gap={2} mt={3}>
                  <Button
                    variant="outlined"
                    startIcon={<Download />}
                    onClick={handleDownloadCSV}
                  >
                    CSV 다운로드
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<PictureAsPdf />}
                    onClick={handleDownloadPDF}
                  >
                    PDF 리포트
                  </Button>
                </Box>
              </Paper>

              {/* 고급 위험 지표 */}
              {(() => {
                const riskMetrics = calculateRiskMetrics(simulationResult);
                return riskMetrics && (
                  <Paper sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h6" gutterBottom>
                      ⚠️ 위험 분석
                    </Typography>

                    <Grid container spacing={3}>
                      <Grid item xs={12} sm={4}>
                        <Card variant="outlined">
                          <CardContent sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" color={
                              riskMetrics.volatility > 25 ? 'error.main' :
                              riskMetrics.volatility > 15 ? 'warning.main' : 'success.main'
                            }>
                              {formatPercent(riskMetrics.volatility)}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              변동성 (연간)
                            </Typography>
                            <Typography variant="caption" display="block" mt={1}>
                              {riskMetrics.volatility > 25 ? '높음' :
                               riskMetrics.volatility > 15 ? '보통' : '낮음'}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>

                      <Grid item xs={12} sm={4}>
                        <Card variant="outlined">
                          <CardContent sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" color={
                              riskMetrics.sharpeRatio > 1 ? 'success.main' :
                              riskMetrics.sharpeRatio > 0 ? 'warning.main' : 'error.main'
                            }>
                              {riskMetrics.sharpeRatio.toFixed(2)}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              샤프 비율
                            </Typography>
                            <Typography variant="caption" display="block" mt={1}>
                              {riskMetrics.sharpeRatio > 1 ? '우수' :
                               riskMetrics.sharpeRatio > 0 ? '양호' : '부족'}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>

                      <Grid item xs={12} sm={4}>
                        <Card variant="outlined">
                          <CardContent sx={{ textAlign: 'center' }}>
                            <Typography variant="h6" color={
                              riskMetrics.maxDrawdown > 30 ? 'error.main' :
                              riskMetrics.maxDrawdown > 15 ? 'warning.main' : 'success.main'
                            }>
                              -{formatPercent(riskMetrics.maxDrawdown)}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              최대 낙폭
                            </Typography>
                            <Typography variant="caption" display="block" mt={1}>
                              {riskMetrics.maxDrawdown > 30 ? '위험' :
                               riskMetrics.maxDrawdown > 15 ? '주의' : '안전'}
                            </Typography>
                          </CardContent>
                        </Card>
                      </Grid>
                    </Grid>

                    <Box mt={2}>
                      <Alert severity="info" sx={{ fontSize: '0.85em' }}>
                        <Typography variant="caption">
                          <strong>위험 지표 설명:</strong><br/>
                          • <strong>변동성</strong>: 투자 수익률의 변동 정도 (낮을수록 안정적)<br/>
                          • <strong>샤프 비율</strong>: 위험 대비 수익률 (1 이상이면 우수)<br/>
                          • <strong>최대 낙폭</strong>: 투자 기간 중 최대 손실 구간
                        </Typography>
                      </Alert>
                    </Box>
                  </Paper>
                );
              })()}

              {/* 차트 */}
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                  투자 성과 추이
                </Typography>
                <Box data-testid="dca-chart">
                  <ResponsiveContainer width="100%" height={400} ref={chartRef}>
                    <LineChart data={getChartData()}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <Tooltip
                        formatter={(value: any, name: string) => [
                          `₩${formatCurrency(value)}`,
                          name === 'portfolioValue' ? '포트폴리오 가치' :
                          name === 'cumulativeInvestment' ? '누적 투자금액' :
                          name === 'benchmark1' ? (selectedCompany?.exchange === 'KRX' ? 'KOSPI' : 'S&P 500') :
                          name === 'benchmark2' ? (selectedCompany?.exchange === 'KRX' ? 'KOSDAQ' : 'NASDAQ') : name
                        ]}
                        labelFormatter={(label) => `${label}`}
                      />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="portfolioValue"
                        stroke="#1976d2"
                        strokeWidth={3}
                        name="포트폴리오 가치"
                      />
                      <Line
                        type="monotone"
                        dataKey="cumulativeInvestment"
                        stroke="#757575"
                        strokeWidth={2}
                        strokeDasharray="5 5"
                        name="누적 투자금액"
                      />
                      <Line
                        type="monotone"
                        dataKey="benchmark1"
                        stroke="#ff9800"
                        strokeWidth={2}
                        name={selectedCompany?.exchange === 'KRX' ? 'KOSPI' : 'S&P 500'}
                      />
                      <Line
                        type="monotone"
                        dataKey="benchmark2"
                        stroke="#4caf50"
                        strokeWidth={2}
                        name={selectedCompany?.exchange === 'KRX' ? 'KOSDAQ' : 'NASDAQ'}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </Box>
              </Paper>

              {/* 투자 기록 테이블 */}
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  투자 기록 상세
                </Typography>
                <TableContainer sx={{ maxHeight: 400 }}>
                  <Table stickyHeader>
                    <TableHead>
                      <TableRow>
                        <TableCell>투자일</TableCell>
                        <TableCell align="right">투자금액</TableCell>
                        <TableCell align="right">주식가격</TableCell>
                        <TableCell align="right">매수주식수</TableCell>
                        <TableCell align="right">포트폴리오가치</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {simulationResult.investmentRecords.map((record, index) => (
                        <TableRow key={index} hover>
                          <TableCell>
                            {new Date(record.investmentDate).toLocaleDateString()}
                          </TableCell>
                          <TableCell align="right">
                            ₩{formatCurrency(record.investmentAmount)}
                          </TableCell>
                          <TableCell align="right">
                            ${record.stockPrice.toFixed(2)}
                          </TableCell>
                          <TableCell align="right">
                            {record.sharesPurchased.toFixed(4)}
                          </TableCell>
                          <TableCell align="right">
                            ₩{formatCurrency(record.portfolioValue)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Box>
          )}

          {!simulationResult && !isLoading && (
            <Paper sx={{ p: 6, textAlign: 'center' }}>
              <ShowChart sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
              <Typography variant="h6" color="text.secondary" gutterBottom>
                시뮬레이션을 실행해보세요
              </Typography>
              <Typography variant="body2" color="text.secondary">
                좌측 설정 패널에서 투자 조건을 입력하고 시뮬레이션을 실행하면 결과를 확인할 수 있습니다.
              </Typography>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};

export default DCASimulationPage;