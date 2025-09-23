'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Card,
  CardContent,
  Chip,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  ToggleButton,
  ToggleButtonGroup,
  Alert,
  Button,
  Paper,
  Avatar,
  Divider,
  useTheme,
} from '@mui/material';
import {
  Search,
  MonetizationOn,
  CalendarToday,
  TrendingUp,
  CheckCircle,
  Business,
  Schedule,
  Assessment,
  Info,
} from '@mui/icons-material';
import { Company } from '@/shared/api/company-client';
import { InvestmentFrequency } from '@/shared/api/types/dca-types';
import CompanyAutocomplete from '@/shared/components/CompanyAutocomplete/CompanyAutocomplete';

interface MobileStepFormProps {
  step: 'company' | 'investment' | 'period' | 'confirmation';
  // Company step
  value?: string;
  onChange?: (value: string) => void;
  selectedCompany?: Company | null;
  onCompanyChange?: (company: Company | null) => void;
  // Investment step
  frequency?: InvestmentFrequency;
  onFrequencyChange?: (frequency: InvestmentFrequency) => void;
  // Period step
  startDate?: string;
  endDate?: string;
  onStartDateChange?: (date: string) => void;
  onEndDateChange?: (date: string) => void;
  startDateError?: string;
  endDateError?: string;
  // Confirmation step
  symbol?: string;
  monthlyInvestmentAmount?: string;
  error?: string;
}

const amountPresets = [
  { label: '10만원', value: '100000' },
  { label: '30만원', value: '300000' },
  { label: '50만원', value: '500000' },
  { label: '100만원', value: '1000000' },
];

const datePresets = [
  { label: '1년', startOffset: -12, endOffset: 0 },
  { label: '3년', startOffset: -36, endOffset: 0 },
  { label: '5년', startOffset: -60, endOffset: 0 },
  { label: '10년', startOffset: -120, endOffset: 0 },
];

export default function MobileStepForm({
  step,
  value = '',
  onChange,
  selectedCompany,
  onCompanyChange,
  frequency = 'MONTHLY',
  onFrequencyChange,
  startDate = '',
  endDate = '',
  onStartDateChange,
  onEndDateChange,
  startDateError,
  endDateError,
  symbol = '',
  monthlyInvestmentAmount = '',
  error,
}: MobileStepFormProps) {
  const theme = useTheme();
  const [selectedAmountPreset, setSelectedAmountPreset] = useState('');
  const [selectedDatePreset, setSelectedDatePreset] = useState('');

  const handleAmountPresetSelect = (presetValue: string) => {
    setSelectedAmountPreset(presetValue);
    onChange?.(presetValue);
  };

  const handleDatePresetSelect = (preset: typeof datePresets[0]) => {
    const today = new Date();
    const startDate = new Date(today);
    startDate.setMonth(today.getMonth() + preset.startOffset);
    const endDate = new Date(today);
    endDate.setMonth(today.getMonth() + preset.endOffset);

    onStartDateChange?.(startDate.toISOString().split('T')[0]);
    onEndDateChange?.(endDate.toISOString().split('T')[0]);
    setSelectedDatePreset(preset.label);
  };

  const formatCurrency = (amount: string) => {
    if (!amount) return '';
    return Number(amount).toLocaleString() + '원';
  };

  switch (step) {
    case 'company':
      return (
        <Box>
          <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
            <Business sx={{ mr: 2, verticalAlign: 'middle' }} />
            투자할 회사를 선택하세요
          </Typography>

          <CompanyAutocomplete
            value={value}
            onChange={(selectedSymbol: string, company?: Company) => {
              onChange?.(selectedSymbol);
              onCompanyChange?.(company || null);
            }}
            label="회사 검색"
            placeholder="삼성전자, Samsung, 005930"
            error={!!error}
            helperText={error}
            fullWidth
            size="medium"
          />

          {selectedCompany && (
            <Card sx={{ mt: 3, border: `2px solid ${theme.palette.primary.main}` }}>
              <CardContent>
                <Box display="flex" alignItems="center" gap={2}>
                  <Avatar
                    sx={{
                      width: 60,
                      height: 60,
                      bgcolor: theme.palette.primary.main,
                      fontSize: '1.5rem',
                      fontWeight: 'bold',
                    }}
                  >
                    {selectedCompany.nameKr.charAt(0)}
                  </Avatar>
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      {selectedCompany.nameKr}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {selectedCompany.symbol} • {selectedCompany.exchange}
                    </Typography>
                    <Chip
                      label="선택됨"
                      color="primary"
                      size="small"
                      icon={<CheckCircle />}
                      sx={{ mt: 1 }}
                    />
                  </Box>
                </Box>
              </CardContent>
            </Card>
          )}

          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              회사명, 종목코드, 또는 영문명으로 검색하실 수 있습니다.
            </Typography>
          </Alert>
        </Box>
      );

    case 'investment':
      return (
        <Box>
          <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
            <MonetizationOn sx={{ mr: 2, verticalAlign: 'middle' }} />
            투자 금액을 설정하세요
          </Typography>

          {/* Amount Presets */}
          <Typography variant="h6" gutterBottom sx={{ mt: 3, mb: 2 }}>
            빠른 선택
          </Typography>
          <Grid container spacing={2} sx={{ mb: 3 }}>
            {amountPresets.map((preset) => (
              <Grid item xs={6} key={preset.value}>
                <Button
                  variant={selectedAmountPreset === preset.value ? 'contained' : 'outlined'}
                  fullWidth
                  size="large"
                  onClick={() => handleAmountPresetSelect(preset.value)}
                  sx={{
                    py: 2,
                    fontSize: '1.1rem',
                    fontWeight: 'bold',
                  }}
                >
                  {preset.label}
                </Button>
              </Grid>
            ))}
          </Grid>

          {/* Custom Amount */}
          <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
            직접 입력
          </Typography>
          <TextField
            label="월 투자 금액"
            value={value}
            onChange={(e) => {
              onChange?.(e.target.value);
              setSelectedAmountPreset('');
            }}
            fullWidth
            size="medium"
            type="number"
            error={!!error}
            helperText={error || `입력 금액: ${formatCurrency(value)}`}
            InputProps={{
              endAdornment: '원'
            }}
          />

          {/* Frequency Selection */}
          <Typography variant="h6" gutterBottom sx={{ mt: 4, mb: 2 }}>
            투자 주기
          </Typography>
          <ToggleButtonGroup
            value={frequency}
            exclusive
            onChange={(e, newFrequency) => newFrequency && onFrequencyChange?.(newFrequency)}
            fullWidth
            size="large"
          >
            <ToggleButton value="WEEKLY" sx={{ py: 2, fontSize: '1.1rem' }}>
              <Schedule sx={{ mr: 1 }} />
              주간
            </ToggleButton>
            <ToggleButton value="MONTHLY" sx={{ py: 2, fontSize: '1.1rem' }}>
              <CalendarToday sx={{ mr: 1 }} />
              월간
            </ToggleButton>
          </ToggleButtonGroup>

          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              DCA는 정기적으로 동일한 금액을 투자하는 전략입니다.
              {frequency === 'MONTHLY' ? '매월' : '매주'} {formatCurrency(value)}을 투자합니다.
            </Typography>
          </Alert>
        </Box>
      );

    case 'period':
      return (
        <Box>
          <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
            <CalendarToday sx={{ mr: 2, verticalAlign: 'middle' }} />
            투자 기간을 설정하세요
          </Typography>

          {/* Date Presets */}
          <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
            빠른 선택
          </Typography>
          <Grid container spacing={2} sx={{ mb: 3 }}>
            {datePresets.map((preset) => (
              <Grid item xs={6} key={preset.label}>
                <Button
                  variant={selectedDatePreset === preset.label ? 'contained' : 'outlined'}
                  fullWidth
                  size="large"
                  onClick={() => handleDatePresetSelect(preset)}
                  sx={{
                    py: 2,
                    fontSize: '1.1rem',
                    fontWeight: 'bold',
                  }}
                >
                  {preset.label}
                </Button>
              </Grid>
            ))}
          </Grid>

          {/* Custom Date Range */}
          <Typography variant="h6" gutterBottom sx={{ mt: 4, mb: 2 }}>
            직접 선택
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="시작 날짜"
              type="date"
              value={startDate}
              onChange={(e) => {
                onStartDateChange?.(e.target.value);
                setSelectedDatePreset('');
              }}
              fullWidth
              InputLabelProps={{ shrink: true }}
              error={!!startDateError}
              helperText={startDateError}
              sx={{
                '& .MuiOutlinedInput-root': {
                  minHeight: 56,
                  fontSize: '1.1rem',
                }
              }}
            />
            <TextField
              label="종료 날짜"
              type="date"
              value={endDate}
              onChange={(e) => {
                onEndDateChange?.(e.target.value);
                setSelectedDatePreset('');
              }}
              fullWidth
              InputLabelProps={{ shrink: true }}
              error={!!endDateError}
              helperText={endDateError}
              sx={{
                '& .MuiOutlinedInput-root': {
                  minHeight: 56,
                  fontSize: '1.1rem',
                }
              }}
            />
          </Box>

          {startDate && endDate && (
            <Paper sx={{ p: 2, mt: 3, bgcolor: theme.palette.primary.light, color: 'white' }}>
              <Typography variant="body1" fontWeight="bold">
                투자 기간: {Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24 * 30))}개월
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                {startDate} ~ {endDate}
              </Typography>
            </Paper>
          )}

          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              과거 데이터를 기반으로 시뮬레이션합니다.
              최소 6개월 이상의 기간을 권장합니다.
            </Typography>
          </Alert>
        </Box>
      );

    case 'confirmation':
      return (
        <Box>
          <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
            <Assessment sx={{ mr: 2, verticalAlign: 'middle' }} />
            설정 확인
          </Typography>

          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                투자 대상
              </Typography>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                <Avatar
                  sx={{
                    width: 50,
                    height: 50,
                    bgcolor: theme.palette.primary.main,
                    fontSize: '1.2rem',
                    fontWeight: 'bold',
                  }}
                >
                  {selectedCompany?.nameKr.charAt(0) || symbol.charAt(0)}
                </Avatar>
                <Box>
                  <Typography variant="h6" fontWeight="bold">
                    {selectedCompany?.nameKr || symbol}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {symbol} • {selectedCompany?.exchange || 'KRX'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                투자 설정
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Box textAlign="center" p={2} bgcolor="background.default" borderRadius={1}>
                    <Typography variant="h5" fontWeight="bold" color="primary">
                      {formatCurrency(monthlyInvestmentAmount)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {frequency === 'MONTHLY' ? '월간' : '주간'} 투자
                    </Typography>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Box textAlign="center" p={2} bgcolor="background.default" borderRadius={1}>
                    <Typography variant="h5" fontWeight="bold" color="success.main">
                      {Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24 * 30))}개월
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      투자 기간
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom color="primary">
                투자 기간
              </Typography>
              <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>시작:</strong> {startDate}
              </Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>
                <strong>종료:</strong> {endDate}
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Typography variant="body1" color="text.secondary">
                  예상 총 투자금액
                </Typography>
                <Typography variant="h6" fontWeight="bold" color="warning.main">
                  {(() => {
                    const months = Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / (1000 * 60 * 60 * 24 * 30));
                    const multiplier = frequency === 'MONTHLY' ? months : months * 4;
                    return formatCurrency((Number(monthlyInvestmentAmount) * multiplier).toString());
                  })()}
                </Typography>
              </Box>
            </CardContent>
          </Card>

          <Alert severity="warning" sx={{ mb: 3 }}>
            <Typography variant="body2">
              <strong>주의사항:</strong> 이 시뮬레이션은 과거 데이터를 기반으로 하며,
              실제 투자 결과와 다를 수 있습니다. 투자 결정 시 신중히 고려하세요.
            </Typography>
          </Alert>

          <Paper sx={{ p: 3, bgcolor: theme.palette.success.light, color: 'white' }}>
            <Box display="flex" alignItems="center" gap={2}>
              <TrendingUp sx={{ fontSize: 40 }} />
              <Box>
                <Typography variant="h6" fontWeight="bold">
                  시뮬레이션 준비 완료
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.9 }}>
                  아래 버튼을 눌러 DCA 투자 시뮬레이션을 시작하세요
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Box>
      );

    default:
      return null;
  }
}