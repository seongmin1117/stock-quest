'use client';

import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  TextField,
  Box,
  Typography,
  Checkbox,
  FormGroup,
  Divider,
  Alert,
  LinearProgress,
  Chip,
} from '@mui/material';
import {
  PictureAsPdf,
  TableChart,
  Description,
  DataUsage,
} from '@mui/icons-material';

interface ExportDialogProps {
  open: boolean;
  onClose: () => void;
  title: string;
  dataType: 'analytics' | 'users' | 'challenges' | 'system' | 'returns';
  onExport: (options: ExportOptions) => Promise<void>;
}

export interface ExportOptions {
  format: 'pdf' | 'excel' | 'csv' | 'json';
  dateRange: 'week' | 'month' | 'quarter' | 'year' | 'custom';
  customStartDate?: string;
  customEndDate?: string;
  includeCharts: boolean;
  includeRawData: boolean;
  includeSummary: boolean;
  selectedSections: string[];
  fileName: string;
}

const ExportDialog: React.FC<ExportDialogProps> = ({
  open,
  onClose,
  title,
  dataType,
  onExport
}) => {
  const [exportOptions, setExportOptions] = useState<ExportOptions>({
    format: 'pdf',
    dateRange: 'month',
    customStartDate: '',
    customEndDate: '',
    includeCharts: true,
    includeRawData: false,
    includeSummary: true,
    selectedSections: [],
    fileName: `${dataType}_report_${new Date().toISOString().split('T')[0]}`
  });

  const [isExporting, setIsExporting] = useState(false);
  const [exportProgress, setExportProgress] = useState(0);

  // 데이터 타입별 섹션 옵션
  const getSectionOptions = () => {
    switch (dataType) {
      case 'analytics':
        return [
          '사용자 통계',
          '트래픽 분석',
          '성과 지표',
          '트렌드 분석'
        ];
      case 'users':
        return [
          '사용자 목록',
          '활동 통계',
          '등급별 분석',
          '가입 트렌드'
        ];
      case 'challenges':
        return [
          '챌린지 성과',
          '참여율 분석',
          '난이도별 통계',
          '카테고리 분석'
        ];
      case 'system':
        return [
          '시스템 메트릭',
          '서비스 상태',
          '에러 로그',
          '리소스 사용량'
        ];
      case 'returns':
        return [
          '수익률 분석',
          '리스크 지표',
          '벤치마크 비교',
          '섹터별 성과'
        ];
      default:
        return [];
    }
  };

  const handleFormatChange = (format: string) => {
    setExportOptions(prev => ({
      ...prev,
      format: format as ExportOptions['format']
    }));
  };

  const handleDateRangeChange = (range: string) => {
    setExportOptions(prev => ({
      ...prev,
      dateRange: range as ExportOptions['dateRange']
    }));
  };

  const handleSectionToggle = (section: string) => {
    setExportOptions(prev => ({
      ...prev,
      selectedSections: prev.selectedSections.includes(section)
        ? prev.selectedSections.filter(s => s !== section)
        : [...prev.selectedSections, section]
    }));
  };

  const handleSelectAllSections = () => {
    const allSections = getSectionOptions();
    setExportOptions(prev => ({
      ...prev,
      selectedSections: prev.selectedSections.length === allSections.length ? [] : allSections
    }));
  };

  const handleExport = async () => {
    setIsExporting(true);
    setExportProgress(0);

    try {
      // 진행률 시뮬레이션
      const progressInterval = setInterval(() => {
        setExportProgress(prev => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return prev;
          }
          return prev + 10;
        });
      }, 200);

      await onExport(exportOptions);

      setExportProgress(100);
      setTimeout(() => {
        setIsExporting(false);
        setExportProgress(0);
        onClose();
      }, 500);
    } catch (error) {
      console.error('Export failed:', error);
      setIsExporting(false);
      setExportProgress(0);
    }
  };

  const getFormatIcon = (format: string) => {
    switch (format) {
      case 'pdf': return <PictureAsPdf />;
      case 'excel': return <TableChart />;
      case 'csv': return <DataUsage />;
      case 'json': return <Description />;
      default: return <Description />;
    }
  };

  const getFormatDescription = (format: string) => {
    switch (format) {
      case 'pdf': return '차트와 그래프가 포함된 완전한 리포트';
      case 'excel': return '데이터 분석과 피벗 테이블을 위한 스프레드시트';
      case 'csv': return '다른 도구에서 사용할 수 있는 원시 데이터';
      case 'json': return '개발자를 위한 구조화된 데이터 형식';
      default: return '';
    }
  };

  const isFormValid = () => {
    if (!exportOptions.fileName.trim()) return false;
    if (exportOptions.dateRange === 'custom') {
      if (!exportOptions.customStartDate || !exportOptions.customEndDate) return false;
    }
    return true;
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: { minHeight: '600px' }
      }}
    >
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          {getFormatIcon(exportOptions.format)}
          <Typography variant="h6">
            {title} 내보내기
          </Typography>
        </Box>
      </DialogTitle>

      <DialogContent dividers>
        {/* 내보내기 형식 선택 */}
        <Box mb={3}>
          <FormLabel component="legend" sx={{ mb: 2, fontWeight: 'bold' }}>
            파일 형식
          </FormLabel>
          <RadioGroup
            value={exportOptions.format}
            onChange={(e) => handleFormatChange(e.target.value)}
            row
          >
            {['pdf', 'excel', 'csv', 'json'].map((format) => (
              <FormControlLabel
                key={format}
                value={format}
                control={<Radio />}
                label={
                  <Box display="flex" alignItems="center" gap={1}>
                    {getFormatIcon(format)}
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {format.toUpperCase()}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {getFormatDescription(format)}
                      </Typography>
                    </Box>
                  </Box>
                }
                sx={{ mb: 1, alignItems: 'flex-start' }}
              />
            ))}
          </RadioGroup>
        </Box>

        <Divider sx={{ my: 3 }} />

        {/* 기간 선택 */}
        <Box mb={3}>
          <FormLabel component="legend" sx={{ mb: 2, fontWeight: 'bold' }}>
            데이터 기간
          </FormLabel>
          <RadioGroup
            value={exportOptions.dateRange}
            onChange={(e) => handleDateRangeChange(e.target.value)}
          >
            <FormControlLabel value="week" control={<Radio />} label="최근 1주" />
            <FormControlLabel value="month" control={<Radio />} label="최근 1개월" />
            <FormControlLabel value="quarter" control={<Radio />} label="최근 3개월" />
            <FormControlLabel value="year" control={<Radio />} label="최근 1년" />
            <FormControlLabel value="custom" control={<Radio />} label="사용자 지정" />
          </RadioGroup>

          {exportOptions.dateRange === 'custom' && (
            <Box display="flex" gap={2} mt={2}>
              <TextField
                label="시작일"
                type="date"
                value={exportOptions.customStartDate}
                onChange={(e) => setExportOptions(prev => ({
                  ...prev,
                  customStartDate: e.target.value
                }))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
              <TextField
                label="종료일"
                type="date"
                value={exportOptions.customEndDate}
                onChange={(e) => setExportOptions(prev => ({
                  ...prev,
                  customEndDate: e.target.value
                }))}
                InputLabelProps={{ shrink: true }}
                fullWidth
              />
            </Box>
          )}
        </Box>

        <Divider sx={{ my: 3 }} />

        {/* 포함할 섹션 선택 */}
        <Box mb={3}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <FormLabel component="legend" sx={{ fontWeight: 'bold' }}>
              포함할 섹션
            </FormLabel>
            <Button
              size="small"
              onClick={handleSelectAllSections}
            >
              {exportOptions.selectedSections.length === getSectionOptions().length ? '전체 해제' : '전체 선택'}
            </Button>
          </Box>
          <FormGroup>
            {getSectionOptions().map((section) => (
              <FormControlLabel
                key={section}
                control={
                  <Checkbox
                    checked={exportOptions.selectedSections.includes(section)}
                    onChange={() => handleSectionToggle(section)}
                  />
                }
                label={section}
              />
            ))}
          </FormGroup>
        </Box>

        <Divider sx={{ my: 3 }} />

        {/* 추가 옵션 */}
        <Box mb={3}>
          <FormLabel component="legend" sx={{ mb: 2, fontWeight: 'bold' }}>
            추가 옵션
          </FormLabel>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={exportOptions.includeSummary}
                  onChange={(e) => setExportOptions(prev => ({
                    ...prev,
                    includeSummary: e.target.checked
                  }))}
                />
              }
              label="요약 정보 포함"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={exportOptions.includeCharts}
                  onChange={(e) => setExportOptions(prev => ({
                    ...prev,
                    includeCharts: e.target.checked
                  }))}
                  disabled={exportOptions.format === 'csv' || exportOptions.format === 'json'}
                />
              }
              label="차트 및 그래프 포함"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={exportOptions.includeRawData}
                  onChange={(e) => setExportOptions(prev => ({
                    ...prev,
                    includeRawData: e.target.checked
                  }))}
                />
              }
              label="원시 데이터 포함"
            />
          </FormGroup>
        </Box>

        <Divider sx={{ my: 3 }} />

        {/* 파일명 설정 */}
        <Box mb={3}>
          <TextField
            label="파일명"
            value={exportOptions.fileName}
            onChange={(e) => setExportOptions(prev => ({
              ...prev,
              fileName: e.target.value
            }))}
            fullWidth
            helperText={`확장자는 자동으로 추가됩니다. (.${exportOptions.format})`}
          />
        </Box>

        {/* 진행률 표시 */}
        {isExporting && (
          <Box mb={3}>
            <Alert severity="info" sx={{ mb: 2 }}>
              리포트를 생성하고 있습니다. 잠시만 기다려주세요...
            </Alert>
            <LinearProgress
              variant="determinate"
              value={exportProgress}
              sx={{ height: 8, borderRadius: 1 }}
            />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {exportProgress}% 완료
            </Typography>
          </Box>
        )}

        {/* 예상 파일 크기 */}
        <Alert severity="info" sx={{ mb: 2 }}>
          <Typography variant="body2">
            <strong>예상 파일 크기:</strong>{' '}
            {exportOptions.format === 'pdf' ? '2-5MB' :
             exportOptions.format === 'excel' ? '500KB-2MB' :
             exportOptions.format === 'csv' ? '100KB-500KB' : '50KB-200KB'}
          </Typography>
          <Box display="flex" gap={1} mt={1}>
            {exportOptions.selectedSections.map(section => (
              <Chip key={section} label={section} size="small" />
            ))}
          </Box>
        </Alert>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={isExporting}>
          취소
        </Button>
        <Button
          onClick={handleExport}
          variant="contained"
          disabled={!isFormValid() || isExporting}
          startIcon={getFormatIcon(exportOptions.format)}
        >
          {isExporting ? '내보내는 중...' : `${exportOptions.format.toUpperCase()}로 내보내기`}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ExportDialog;