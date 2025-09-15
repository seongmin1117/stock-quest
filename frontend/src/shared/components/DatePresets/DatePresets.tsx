'use client';

import React from 'react';
import { Box, Chip, Typography } from '@mui/material';

interface DatePreset {
  label: string;
  years: number;
  testId: string;
}

interface DatePresetsProps {
  onSelect: (startDate: string, endDate: string) => void;
  selectedPreset?: string;
}

const DATE_PRESETS: DatePreset[] = [
  { label: '1년', years: 1, testId: 'date-preset-1year' },
  { label: '3년', years: 3, testId: 'date-preset-3years' },
  { label: '5년', years: 5, testId: 'date-preset-5years' },
  { label: '10년', years: 10, testId: 'date-preset-10years' },
];

/**
 * 날짜 범위 프리셋 컴포넌트
 */
export default function DatePresets({ onSelect, selectedPreset }: DatePresetsProps) {
  const handlePresetClick = (preset: DatePreset) => {
    const today = new Date();
    const startDate = new Date(today.getFullYear() - preset.years, today.getMonth(), today.getDate());

    // ISO 날짜 형식으로 변환 (YYYY-MM-DD)
    const formatDate = (date: Date) => {
      return date.toISOString().split('T')[0];
    };

    onSelect(formatDate(startDate), formatDate(today));
  };

  return (
    <Box mb={2}>
      <Typography variant="caption" color="text.secondary" gutterBottom display="block">
        빠른 선택
      </Typography>
      <Box display="flex" gap={1} flexWrap="wrap">
        {DATE_PRESETS.map((preset) => (
          <Chip
            key={preset.testId}
            label={preset.label}
            size="small"
            variant={selectedPreset === preset.testId ? "filled" : "outlined"}
            onClick={() => handlePresetClick(preset)}
            data-testid={preset.testId}
            sx={{
              borderColor: selectedPreset === preset.testId ? 'primary.main' : 'divider',
              '&:hover': {
                borderColor: 'primary.main',
                backgroundColor: selectedPreset === preset.testId ? 'primary.main' : 'action.hover',
              }
            }}
          />
        ))}
      </Box>
    </Box>
  );
}