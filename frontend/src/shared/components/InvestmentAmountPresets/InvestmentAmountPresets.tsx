'use client';

import React from 'react';
import { Box, Chip, Typography } from '@mui/material';

interface AmountPreset {
  label: string;
  amount: number;
  testId: string;
}

interface InvestmentAmountPresetsProps {
  onSelect: (amount: string) => void;
  selectedPreset?: string;
}

const AMOUNT_PRESETS: AmountPreset[] = [
  { label: '월 10만원', amount: 100000, testId: 'amount-preset-100k' },
  { label: '월 30만원', amount: 300000, testId: 'amount-preset-300k' },
  { label: '월 50만원', amount: 500000, testId: 'amount-preset-500k' },
  { label: '월 100만원', amount: 1000000, testId: 'amount-preset-1m' },
];

/**
 * 투자 금액 프리셋 컴포넌트
 */
export default function InvestmentAmountPresets({ onSelect, selectedPreset }: InvestmentAmountPresetsProps) {
  const handlePresetClick = (preset: AmountPreset) => {
    onSelect(preset.amount.toString());
  };

  return (
    <Box mb={2}>
      <Typography variant="caption" color="text.secondary" gutterBottom display="block">
        추천 투자 금액
      </Typography>
      <Box display="flex" gap={1} flexWrap="wrap">
        {AMOUNT_PRESETS.map((preset) => (
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