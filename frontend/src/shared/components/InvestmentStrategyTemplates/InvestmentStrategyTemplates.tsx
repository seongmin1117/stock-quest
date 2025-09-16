'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  CardActionArea,
  Grid,
  Chip,
  Tooltip,
} from '@mui/material';
import {
  TrendingUp,
  Speed,
  Shield,
  MonetizationOn,
  Timeline,
  Star,
} from '@mui/icons-material';

export interface InvestmentStrategy {
  id: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  monthlyAmount: number;
  duration: string; // e.g., "2020-01-01,2024-01-01"
  risk: 'low' | 'medium' | 'high';
  expectedReturn: string;
  tags: string[];
  tooltip: string;
}

interface InvestmentStrategyTemplatesProps {
  onSelect: (strategy: InvestmentStrategy) => void;
  selectedStrategyId?: string;
}

const strategies: InvestmentStrategy[] = [
  {
    id: 'conservative-monthly',
    name: '안정형 월간 전략',
    description: '매월 30만원씩 꾸준한 장기 투자',
    icon: <Shield color="success" />,
    frequency: 'MONTHLY',
    monthlyAmount: 300000,
    duration: '2020-01-01,2024-01-01',
    risk: 'low',
    expectedReturn: '5-8%',
    tags: ['안정성', '장기투자', '초보자'],
    tooltip: '위험을 최소화하고 꾸준한 성장을 추구하는 보수적 전략'
  },
  {
    id: 'balanced-weekly',
    name: '균형형 주간 전략',
    description: '매주 7만원씩 시장 변동성 완화',
    icon: <Timeline color="primary" />,
    frequency: 'WEEKLY',
    monthlyAmount: 280000, // 7만원 × 4주
    duration: '2021-01-01,2024-01-01',
    risk: 'medium',
    expectedReturn: '7-12%',
    tags: ['균형성', '변동성완화', '중급자'],
    tooltip: '시장 변동성을 효과적으로 분산하는 균형잡힌 전략'
  },
  {
    id: 'aggressive-monthly',
    name: '공격형 대규모 전략',
    description: '매월 100만원씩 적극적 투자',
    icon: <TrendingUp color="warning" />,
    frequency: 'MONTHLY',
    monthlyAmount: 1000000,
    duration: '2019-01-01,2024-01-01',
    risk: 'high',
    expectedReturn: '10-18%',
    tags: ['고수익', '대규모', '경험자'],
    tooltip: '높은 수익을 추구하는 적극적 대규모 투자 전략'
  },
  {
    id: 'quick-daily',
    name: '단기 집중 전략',
    description: '매일 2만원씩 단기 집중 투자',
    icon: <Speed color="error" />,
    frequency: 'DAILY',
    monthlyAmount: 600000, // 2만원 × 30일
    duration: '2023-01-01,2024-01-01',
    risk: 'high',
    expectedReturn: '8-15%',
    tags: ['단기집중', '고빈도', '변동성활용'],
    tooltip: '짧은 기간에 집중적으로 투자하는 고빈도 전략'
  },
  {
    id: 'premium-monthly',
    name: '프리미엄 장기 전략',
    description: '매월 50만원씩 5년 장기 투자',
    icon: <Star color="secondary" />,
    frequency: 'MONTHLY',
    monthlyAmount: 500000,
    duration: '2019-01-01,2024-01-01',
    risk: 'medium',
    expectedReturn: '8-14%',
    tags: ['프리미엄', '장기성장', '복리효과'],
    tooltip: '장기 복리 효과를 극대화하는 프리미엄 전략'
  },
  {
    id: 'starter-monthly',
    name: '시작하기 전략',
    description: '매월 10만원씩 투자 시작',
    icon: <MonetizationOn color="info" />,
    frequency: 'MONTHLY',
    monthlyAmount: 100000,
    duration: '2022-01-01,2024-01-01',
    risk: 'low',
    expectedReturn: '4-8%',
    tags: ['초보자', '소액투자', '학습용'],
    tooltip: '투자를 처음 시작하는 분들을 위한 소액 학습 전략'
  }
];

/**
 * Investment Strategy Templates Component
 * Pre-defined DCA investment strategies for quick setup
 */
export default function InvestmentStrategyTemplates({
  onSelect,
  selectedStrategyId
}: InvestmentStrategyTemplatesProps) {

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case 'low': return 'success';
      case 'medium': return 'warning';
      case 'high': return 'error';
      default: return 'default';
    }
  };

  const getRiskLabel = (risk: string) => {
    switch (risk) {
      case 'low': return '낮음';
      case 'medium': return '보통';
      case 'high': return '높음';
      default: return risk;
    }
  };

  return (
    <Box mb={3}>
      <Typography variant="h6" gutterBottom>
        💡 투자 전략 템플릿
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={2}>
        미리 설정된 전략을 선택하여 빠르게 시뮬레이션을 시작하세요
      </Typography>

      <Grid container spacing={2}>
        {strategies.map((strategy) => (
          <Grid item xs={12} sm={6} md={4} key={strategy.id}>
            <Tooltip title={strategy.tooltip} placement="top">
              <Card
                variant="outlined"
                sx={{
                  height: '100%',
                  border: selectedStrategyId === strategy.id ? 2 : 1,
                  borderColor: selectedStrategyId === strategy.id ? 'primary.main' : 'divider',
                  '&:hover': {
                    borderColor: 'primary.main',
                    boxShadow: 1
                  }
                }}
              >
                <CardActionArea
                  onClick={() => onSelect(strategy)}
                  sx={{ height: '100%', p: 0 }}
                >
                  <CardContent>
                    {/* Strategy Header */}
                    <Box display="flex" alignItems="center" mb={1}>
                      {strategy.icon}
                      <Typography variant="subtitle1" fontWeight="bold" ml={1}>
                        {strategy.name}
                      </Typography>
                    </Box>

                    {/* Description */}
                    <Typography variant="body2" color="text.secondary" mb={2}>
                      {strategy.description}
                    </Typography>

                    {/* Key Metrics */}
                    <Box mb={2}>
                      <Typography variant="caption" color="text.secondary">
                        투자금액: <strong>₩{strategy.monthlyAmount.toLocaleString()}/월</strong>
                      </Typography>
                      <br />
                      <Typography variant="caption" color="text.secondary">
                        주기: <strong>{
                          strategy.frequency === 'DAILY' ? '일간' :
                          strategy.frequency === 'WEEKLY' ? '주간' : '월간'
                        }</strong>
                      </Typography>
                      <br />
                      <Typography variant="caption" color="text.secondary">
                        예상 수익률: <strong>{strategy.expectedReturn}</strong>
                      </Typography>
                    </Box>

                    {/* Risk Level */}
                    <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
                      <Chip
                        label={`위험도: ${getRiskLabel(strategy.risk)}`}
                        size="small"
                        color={getRiskColor(strategy.risk) as any}
                        variant="outlined"
                      />
                    </Box>

                    {/* Tags */}
                    <Box>
                      {strategy.tags.slice(0, 2).map((tag, index) => (
                        <Chip
                          key={index}
                          label={tag}
                          size="small"
                          variant="outlined"
                          sx={{ mr: 0.5, mb: 0.5, fontSize: '0.7rem' }}
                        />
                      ))}
                      {strategy.tags.length > 2 && (
                        <Chip
                          label={`+${strategy.tags.length - 2}`}
                          size="small"
                          variant="outlined"
                          sx={{ fontSize: '0.7rem' }}
                        />
                      )}
                    </Box>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Tooltip>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}