'use client';

import React, { useState, useRef } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  IconButton,
  Stepper,
  Step,
  StepLabel,
  Card,
  CardContent,
  Chip,
  Dialog,
  Slide,
  AppBar,
  Toolbar,
  Alert,
  LinearProgress,
  Fab,
  SwipeableDrawer,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  useTheme,
} from '@mui/material';
import {
  Close,
  ArrowBack,
  ArrowForward,
  PlayArrow,
  TrendingUp,
  Assessment,
  Share,
  Download,
  SwipeUp,
  Timeline,
  MonetizationOn,
  Speed,
  ExpandMore,
  ExpandLess,
} from '@mui/icons-material';
import { TransitionProps } from '@mui/material/transitions';
import { motion, AnimatePresence } from 'framer-motion';
import { Company } from '@/shared/api/company-client';
import type { DCASimulationResponse, InvestmentFrequency } from '@/shared/api/types/dca-types';
import type { DCASimulationRequest } from '@/features/dca/api/dca-api';
import { useSimulate } from '@/features/dca/api/dca-api';

// Mobile-specific components
import MobileStepForm from './components/MobileStepForm';
import MobileResultsCards from './components/MobileResultsCards';
import MobileChart from './components/MobileChart';
import MobileRiskMetrics from './components/MobileRiskMetrics';

const Transition = React.forwardRef(function Transition(
  props: TransitionProps & {
    children: React.ReactElement;
  },
  ref: React.Ref<unknown>,
) {
  return <Slide direction="up" ref={ref} {...props} />;
});

interface MobileDCASimulationProps {
  onClose: () => void;
  isOpen: boolean;
}

const steps = [
  { label: '회사 선택', icon: <Assessment /> },
  { label: '투자 설정', icon: <MonetizationOn /> },
  { label: '기간 설정', icon: <Timeline /> },
  { label: '확인', icon: <Speed /> },
];

export default function MobileDCASimulation({ onClose, isOpen }: MobileDCASimulationProps) {
  const theme = useTheme();

  // DCA Simulation API hook
  const dcaSimulation = useSimulate();

  // Form state
  const [activeStep, setActiveStep] = useState(0);
  const [symbol, setSymbol] = useState('');
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [monthlyInvestmentAmount, setMonthlyInvestmentAmount] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [frequency, setFrequency] = useState<InvestmentFrequency>('MONTHLY');

  // UI state
  const [isLoading, setIsLoading] = useState(false);
  const [simulationResult, setSimulationResult] = useState<DCASimulationResponse | null>(null);
  const [error, setError] = useState<string>('');
  const [validationErrors, setValidationErrors] = useState<{[key: string]: string}>({});
  const [showResults, setShowResults] = useState(false);
  const [selectedResultSection, setSelectedResultSection] = useState<string>('summary');

  // Bottom sheet state
  const [bottomSheetOpen, setBottomSheetOpen] = useState(false);
  const [expandedSections, setExpandedSections] = useState<Set<string>>(new Set(['summary']));

  const handleNext = () => {
    if (activeStep < steps.length - 1) {
      setActiveStep(activeStep + 1);
    } else {
      handleRunSimulation();
    }
  };

  const handleBack = () => {
    if (activeStep > 0) {
      setActiveStep(activeStep - 1);
    }
  };

  const handleRunSimulation = async () => {
    setIsLoading(true);
    setError('');

    try {
      // Validate form
      const errors: {[key: string]: string} = {};

      if (!symbol) errors.symbol = '회사를 선택해주세요';
      if (!monthlyInvestmentAmount || Number(monthlyInvestmentAmount) <= 0) {
        errors.amount = '올바른 투자 금액을 입력해주세요';
      }
      if (!startDate) errors.startDate = '시작 날짜를 선택해주세요';
      if (!endDate) errors.endDate = '종료 날짜를 선택해주세요';

      if (Object.keys(errors).length > 0) {
        setValidationErrors(errors);
        setIsLoading(false);
        return;
      }

      const request: DCASimulationRequest = {
        symbol,
        monthlyInvestmentAmount: Number(monthlyInvestmentAmount),
        startDate,
        endDate,
        frequency
      };

      // Call real DCA API
      const apiResult = await dcaSimulation.mutateAsync({ data: request });
      const result = apiResult as unknown as DCASimulationResponse;

      setSimulationResult(result);
      setShowResults(true);
      setBottomSheetOpen(true);

    } catch (err: any) {
      console.error('DCA 시뮬레이션 오류:', err);
      const errorMessage = err?.message || err?.response?.data?.message || '시뮬레이션 실행 중 오류가 발생했습니다.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleSection = (sectionId: string) => {
    setExpandedSections(prev => {
      const newSections = new Set(prev);
      if (newSections.has(sectionId)) {
        newSections.delete(sectionId);
      } else {
        newSections.add(sectionId);
      }
      return newSections;
    });
  };

  const getStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <MobileStepForm
            step="company"
            value={symbol}
            onChange={setSymbol}
            selectedCompany={selectedCompany}
            onCompanyChange={setSelectedCompany}
            error={validationErrors.symbol}
          />
        );
      case 1:
        return (
          <MobileStepForm
            step="investment"
            value={monthlyInvestmentAmount}
            onChange={setMonthlyInvestmentAmount}
            frequency={frequency}
            onFrequencyChange={setFrequency}
            error={validationErrors.amount}
          />
        );
      case 2:
        return (
          <MobileStepForm
            step="period"
            startDate={startDate}
            endDate={endDate}
            onStartDateChange={setStartDate}
            onEndDateChange={setEndDate}
            startDateError={validationErrors.startDate}
            endDateError={validationErrors.endDate}
          />
        );
      case 3:
        return (
          <MobileStepForm
            step="confirmation"
            symbol={symbol}
            selectedCompany={selectedCompany}
            monthlyInvestmentAmount={monthlyInvestmentAmount}
            frequency={frequency}
            startDate={startDate}
            endDate={endDate}
          />
        );
      default:
        return null;
    }
  };

  return (
    <>
      {/* Main Dialog */}
      <Dialog
        fullScreen
        open={isOpen}
        onClose={onClose}
        TransitionComponent={Transition}
        sx={{
          '& .MuiDialog-paper': {
            background: theme.palette.background.default,
          }
        }}
      >
        {/* App Bar */}
        <AppBar
          position="sticky"
          elevation={0}
          sx={{
            background: `linear-gradient(135deg, ${theme.palette.primary.main}, ${theme.palette.primary.dark})`,
          }}
        >
          <Toolbar>
            <IconButton edge="start" color="inherit" onClick={onClose}>
              <Close />
            </IconButton>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1, ml: 2 }}>
              DCA 시뮬레이션
            </Typography>
            {simulationResult && (
              <IconButton color="inherit" onClick={() => setBottomSheetOpen(true)}>
                <Assessment />
              </IconButton>
            )}
          </Toolbar>

          {isLoading && <LinearProgress color="secondary" />}
        </AppBar>

        {/* Content */}
        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          {!showResults ? (
            <>
              {/* Stepper */}
              <Box sx={{ p: 2, pb: 1 }}>
                <Stepper activeStep={activeStep} alternativeLabel>
                  {steps.map((step, index) => (
                    <Step key={step.label}>
                      <StepLabel icon={step.icon}>
                        <Typography variant="caption">{step.label}</Typography>
                      </StepLabel>
                    </Step>
                  ))}
                </Stepper>
              </Box>

              {/* Step Content */}
              <Box sx={{ flex: 1, p: 2, pb: 100 }}>
                <AnimatePresence mode="wait">
                  <motion.div
                    key={activeStep}
                    initial={{ opacity: 0, x: 50 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -50 }}
                    transition={{ duration: 0.3 }}
                  >
                    {getStepContent(activeStep)}
                  </motion.div>
                </AnimatePresence>

                {error && (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {error}
                  </Alert>
                )}
              </Box>

              {/* Navigation Buttons */}
              <Box
                sx={{
                  position: 'fixed',
                  bottom: 0,
                  left: 0,
                  right: 0,
                  p: 2,
                  background: theme.palette.background.paper,
                  borderTop: `1px solid ${theme.palette.divider}`,
                  display: 'flex',
                  gap: 2,
                }}
              >
                <Button
                  variant="outlined"
                  onClick={handleBack}
                  disabled={activeStep === 0 || isLoading}
                  startIcon={<ArrowBack />}
                  sx={{ flex: 1 }}
                >
                  이전
                </Button>
                <Button
                  variant="contained"
                  onClick={handleNext}
                  disabled={isLoading}
                  endIcon={activeStep === steps.length - 1 ? <PlayArrow /> : <ArrowForward />}
                  sx={{ flex: 2 }}
                >
                  {activeStep === steps.length - 1 ? '시뮬레이션 실행' : '다음'}
                </Button>
              </Box>
            </>
          ) : (
            /* Results View */
            <Box sx={{ flex: 1, position: 'relative' }}>
              {/* Quick Results Summary */}
              <Box sx={{ p: 2 }}>
                <Card sx={{ mb: 2, background: theme.palette.primary.main, color: 'white' }}>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      {selectedCompany?.nameKr || symbol} DCA 결과
                    </Typography>
                    <Box display="flex" justifyContent="space-between" alignItems="center">
                      <Box>
                        <Typography variant="h4">
                          {simulationResult?.totalReturnPercentage.toFixed(1)}%
                        </Typography>
                        <Typography variant="body2" sx={{ opacity: 0.9 }}>
                          총 수익률
                        </Typography>
                      </Box>
                      <TrendingUp sx={{ fontSize: 48, opacity: 0.7 }} />
                    </Box>
                  </CardContent>
                </Card>

                <Typography variant="body2" color="text.secondary" align="center">
                  <SwipeUp sx={{ mr: 1, verticalAlign: 'middle' }} />
                  위로 스와이프하여 상세 결과 보기
                </Typography>
              </Box>

              {/* Floating Action Button */}
              <Fab
                color="primary"
                onClick={() => setBottomSheetOpen(true)}
                sx={{
                  position: 'fixed',
                  bottom: 20,
                  right: 20,
                  zIndex: 1000,
                }}
              >
                <Assessment />
              </Fab>
            </Box>
          )}
        </Box>

        {/* Bottom Sheet for Results */}
        <SwipeableDrawer
          anchor="bottom"
          open={bottomSheetOpen}
          onClose={() => setBottomSheetOpen(false)}
          onOpen={() => setBottomSheetOpen(true)}
          disableSwipeToOpen
          PaperProps={{
            sx: {
              height: '80vh',
              borderTopLeftRadius: 16,
              borderTopRightRadius: 16,
              background: theme.palette.background.default,
            }
          }}
        >
          {/* Bottom Sheet Handle */}
          <Box
            sx={{
              width: 40,
              height: 4,
              borderRadius: 2,
              backgroundColor: theme.palette.divider,
              mx: 'auto',
              mt: 1,
              mb: 2,
            }}
          />

          {/* Bottom Sheet Content */}
          <Box sx={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
            {/* Section Tabs */}
            <Box sx={{ px: 2, mb: 2 }}>
              <List component="nav" sx={{ display: 'flex', flexDirection: 'row', p: 0 }}>
                {[
                  { id: 'summary', label: '요약', icon: <Assessment /> },
                  { id: 'chart', label: '차트', icon: <Timeline /> },
                  { id: 'risk', label: '위험분석', icon: <Speed /> },
                ].map((section) => (
                  <ListItem
                    key={section.id}
                    button
                    selected={selectedResultSection === section.id}
                    onClick={() => setSelectedResultSection(section.id)}
                    sx={{
                      flex: 1,
                      flexDirection: 'column',
                      py: 1,
                      borderRadius: 1,
                      mx: 0.5,
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 'auto', mb: 0.5 }}>
                      {section.icon}
                    </ListItemIcon>
                    <ListItemText
                      primary={section.label}
                      primaryTypographyProps={{ variant: 'caption', align: 'center' }}
                    />
                  </ListItem>
                ))}
              </List>
            </Box>

            {/* Section Content */}
            <Box sx={{ flex: 1, overflow: 'auto', px: 2 }}>
              <AnimatePresence mode="wait">
                <motion.div
                  key={selectedResultSection}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -20 }}
                  transition={{ duration: 0.2 }}
                  style={{ height: '100%' }}
                >
                  {selectedResultSection === 'summary' && simulationResult && (
                    <MobileResultsCards
                      result={simulationResult}
                      selectedCompany={selectedCompany}
                      expandedSections={expandedSections}
                      onToggleSection={toggleSection}
                      startDate={startDate}
                      endDate={endDate}
                      frequency={frequency}
                      monthlyInvestmentAmount={Number(monthlyInvestmentAmount)}
                    />
                  )}
                  {selectedResultSection === 'chart' && simulationResult && (
                    <MobileChart
                      result={simulationResult}
                      selectedCompany={selectedCompany}
                      startDate={startDate}
                      endDate={endDate}
                      frequency={frequency}
                      monthlyInvestmentAmount={Number(monthlyInvestmentAmount)}
                    />
                  )}
                  {selectedResultSection === 'risk' && simulationResult && (
                    <MobileRiskMetrics
                      result={simulationResult}
                      expandedSections={expandedSections}
                      onToggleSection={toggleSection}
                    />
                  )}
                </motion.div>
              </AnimatePresence>
            </Box>

            {/* Bottom Sheet Actions */}
            <Box sx={{ p: 2, borderTop: `1px solid ${theme.palette.divider}` }}>
              <Box display="flex" gap={1}>
                <Button
                  variant="outlined"
                  startIcon={<Share />}
                  size="small"
                  sx={{ flex: 1 }}
                >
                  공유
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<Download />}
                  size="small"
                  sx={{ flex: 1 }}
                >
                  저장
                </Button>
                <Button
                  variant="contained"
                  onClick={() => {
                    setShowResults(false);
                    setBottomSheetOpen(false);
                    setActiveStep(0);
                    setSimulationResult(null);
                  }}
                  size="small"
                  sx={{ flex: 1 }}
                >
                  새 시뮬레이션
                </Button>
              </Box>
            </Box>
          </Box>
        </SwipeableDrawer>
      </Dialog>
    </>
  );
}