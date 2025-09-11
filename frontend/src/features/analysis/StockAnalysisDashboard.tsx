'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Progress } from '@/components/ui/progress';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, RadarChart, PolarGrid, PolarAngleAxis, 
  PolarRadiusAxis, Radar
} from 'recharts';
import { 
  TrendingUp, TrendingDown, AlertTriangle, Search, 
  Star, Shield, Target, Brain, Activity, DollarSign 
} from 'lucide-react';

// Types for Stock Analysis
interface StockAnalysisResult {
  symbol: string;
  analysisTimestamp: string;
  currentPrice: number;
  priceChangePercent: number;
  volume24h: number;
  marketCap: number;
  strategyAnalysis: StrategyAnalysisResult;
  technicalAnalysis: TechnicalAnalysisResult;
  fundamentalAnalysis: FundamentalAnalysisResult;
  overallRating: number;
  riskAssessment: RiskAssessmentResult;
  investmentRecommendation: string;
  keyInsights: string[];
  priceTargets: PriceTargetResult;
}

interface StrategyAnalysisResult {
  recommendedAction: 'BUY' | 'SELL' | 'HOLD';
  confidence: number;
  primaryStrategy: string;
  strategyScores: Record<string, number>;
  aiInsights: string;
}

interface TechnicalAnalysisResult {
  rsi: number;
  rsiSignal: string;
  macd: number;
  macdSignal: number;
  trendDirection: string;
  bollingerPosition: string;
  volatilityLevel: string;
  technicalScore: number;
  supportLevel: number;
  resistanceLevel: number;
  technicalSummary: string;
}

interface FundamentalAnalysisResult {
  peRatio: number;
  pbRatio: number;
  roe: number;
  debtToEquityRatio: number;
  revenueGrowth: number;
  fundamentalScore: number;
  valueRating: string;
  financialHealthRating: string;
  growthPotential: string;
  fundamentalSummary: string;
}

interface RiskAssessmentResult {
  overallRiskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  volatilityRisk: number;
  liquidityRisk: number;
  marketDepthRisk: number;
  riskFactors: string[];
  riskMitigationSuggestions: string[];
}

interface PriceTargetResult {
  shortTermTarget: number;
  mediumTermTarget: number;
  longTermTarget: number;
  stopLoss: number;
}

// Mock Stock Analysis Service
class StockAnalysisService {
  static async analyzeStock(symbol: string): Promise<StockAnalysisResult> {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // Mock data generation based on symbol
    const basePrice = 100 + (symbol.charCodeAt(0) - 65) * 10;
    const changePercent = (Math.random() - 0.5) * 10;
    
    return {
      symbol,
      analysisTimestamp: new Date().toISOString(),
      currentPrice: basePrice,
      priceChangePercent: changePercent,
      volume24h: Math.floor(Math.random() * 1000000),
      marketCap: basePrice * 1000000,
      strategyAnalysis: {
        recommendedAction: changePercent > 2 ? 'BUY' : changePercent < -2 ? 'SELL' : 'HOLD',
        confidence: 0.7 + Math.random() * 0.3,
        primaryStrategy: 'MOMENTUM',
        strategyScores: {
          'MOMENTUM': Math.floor(Math.random() * 10) + 1,
          'VALUE': Math.floor(Math.random() * 10) + 1,
          'GROWTH': Math.floor(Math.random() * 10) + 1,
          'MEAN_REVERSION': Math.floor(Math.random() * 10) + 1,
          'QUANTITATIVE': Math.floor(Math.random() * 10) + 1,
        },
        aiInsights: `${symbol}에 대한 AI 분석 결과, ${changePercent > 0 ? '상승' : '하락'} 추세가 감지되었습니다.`
      },
      technicalAnalysis: {
        rsi: 30 + Math.random() * 40,
        rsiSignal: 'NEUTRAL',
        macd: Math.random() * 2 - 1,
        macdSignal: Math.random() * 2 - 1,
        trendDirection: changePercent > 0 ? 'BULLISH' : 'BEARISH',
        bollingerPosition: 'MIDDLE',
        volatilityLevel: 'MEDIUM',
        technicalScore: Math.floor(Math.random() * 10) + 1,
        supportLevel: basePrice * 0.95,
        resistanceLevel: basePrice * 1.05,
        technicalSummary: `RSI: 중립, 추세: ${changePercent > 0 ? '강세' : '약세'}, 변동성: 보통`
      },
      fundamentalAnalysis: {
        peRatio: 15 + Math.random() * 20,
        pbRatio: 1 + Math.random() * 3,
        roe: 5 + Math.random() * 25,
        debtToEquityRatio: Math.random() * 0.6,
        revenueGrowth: -10 + Math.random() * 30,
        fundamentalScore: Math.floor(Math.random() * 10) + 1,
        valueRating: 'FAIR_VALUE',
        financialHealthRating: 'MODERATE',
        growthPotential: 'MODERATE',
        fundamentalSummary: 'P/E: 적정, ROE: 양호, 매출성장률: 보통'
      },
      overallRating: Math.floor(Math.random() * 10) + 1,
      riskAssessment: {
        overallRiskScore: Math.random(),
        riskLevel: 'MEDIUM' as const,
        volatilityRisk: Math.random(),
        liquidityRisk: Math.random(),
        marketDepthRisk: Math.random(),
        riskFactors: ['시장 변동성', '유동성 리스크'],
        riskMitigationSuggestions: ['분산투자', '손절매 설정']
      },
      investmentRecommendation: changePercent > 2 ? '매수 추천' : '보유 권장',
      keyInsights: [
        `${symbol} 기술적 분석: ${changePercent > 0 ? '긍정적' : '부정적'} 신호`,
        `기본적 분석: 적정 가치 평가`,
        `AI 전략: ${changePercent > 2 ? '매수' : '관망'} 신호`
      ],
      priceTargets: {
        shortTermTarget: basePrice * 1.05,
        mediumTermTarget: basePrice * 1.1,
        longTermTarget: basePrice * 1.2,
        stopLoss: basePrice * 0.9
      }
    };
  }
}

export function StockAnalysisDashboard() {
  const [searchSymbol, setSearchSymbol] = useState('');
  const [currentAnalysis, setCurrentAnalysis] = useState<StockAnalysisResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedTab, setSelectedTab] = useState('overview');

  const handleAnalyzeStock = async () => {
    if (!searchSymbol.trim()) return;
    
    setIsLoading(true);
    try {
      const result = await StockAnalysisService.analyzeStock(searchSymbol.toUpperCase());
      setCurrentAnalysis(result);
    } catch (error) {
      console.error('Analysis failed:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const strategyRadarData = useMemo(() => {
    if (!currentAnalysis) return [];
    
    return Object.entries(currentAnalysis.strategyAnalysis.strategyScores).map(([strategy, score]) => ({
      strategy: strategy.replace('_', ' '),
      score,
      fullMark: 10
    }));
  }, [currentAnalysis]);

  const riskData = useMemo(() => {
    if (!currentAnalysis) return [];
    
    const risk = currentAnalysis.riskAssessment;
    return [
      { name: '변동성', value: risk.volatilityRisk * 100 },
      { name: '유동성', value: risk.liquidityRisk * 100 },
      { name: '시장심도', value: risk.marketDepthRisk * 100 }
    ];
  }, [currentAnalysis]);

  const getRatingColor = (rating: number) => {
    if (rating >= 8) return 'text-green-600';
    if (rating >= 6) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getRiskColor = (level: string) => {
    switch (level) {
      case 'LOW': return 'bg-green-500';
      case 'MEDIUM': return 'bg-yellow-500';
      case 'HIGH': return 'bg-red-500';
      default: return 'bg-gray-500';
    }
  };

  const getActionColor = (action: string) => {
    switch (action) {
      case 'BUY': return 'bg-green-500';
      case 'SELL': return 'bg-red-500';
      case 'HOLD': return 'bg-blue-500';
      default: return 'bg-gray-500';
    }
  };

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Brain className="h-8 w-8 text-blue-600" />
            AI 주식 종합 분석
          </h1>
          <p className="text-muted-foreground mt-1">
            실시간 데이터, AI 전략, 기술적/기본적 분석을 통합한 종합 주식 분석
          </p>
        </div>
      </div>

      {/* Search Section */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Search className="h-5 w-5" />
            주식 분석 검색
          </CardTitle>
          <CardDescription>
            분석하고 싶은 주식의 심볼을 입력하세요 (예: AAPL, TSLA, MSFT)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4">
            <Input
              placeholder="주식 심볼 입력 (예: AAPL)"
              value={searchSymbol}
              onChange={(e) => setSearchSymbol(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleAnalyzeStock()}
              className="flex-1"
            />
            <Button 
              onClick={handleAnalyzeStock}
              disabled={isLoading || !searchSymbol.trim()}
            >
              {isLoading ? '분석 중...' : '분석 시작'}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Loading State */}
      {isLoading && (
        <Card>
          <CardContent className="py-8">
            <div className="flex items-center justify-center space-y-4">
              <div className="text-center">
                <Activity className="h-12 w-12 animate-spin text-blue-600 mx-auto mb-4" />
                <h3 className="text-lg font-semibold">AI 분석 진행 중...</h3>
                <p className="text-muted-foreground">
                  실시간 데이터 수집 및 AI 전략 분석을 수행하고 있습니다.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Analysis Results */}
      {currentAnalysis && !isLoading && (
        <div className="space-y-6">
          {/* Overview Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">현재가</p>
                    <p className="text-2xl font-bold">
                      ${currentAnalysis.currentPrice.toFixed(2)}
                    </p>
                    <p className={`text-sm flex items-center gap-1 ${
                      currentAnalysis.priceChangePercent >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {currentAnalysis.priceChangePercent >= 0 ? 
                        <TrendingUp className="h-4 w-4" /> : 
                        <TrendingDown className="h-4 w-4" />
                      }
                      {currentAnalysis.priceChangePercent.toFixed(2)}%
                    </p>
                  </div>
                  <DollarSign className="h-8 w-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">종합 평가</p>
                    <p className={`text-2xl font-bold ${getRatingColor(currentAnalysis.overallRating)}`}>
                      {currentAnalysis.overallRating}/10
                    </p>
                    <div className="flex items-center gap-1 mt-1">
                      {[...Array(5)].map((_, i) => (
                        <Star 
                          key={i} 
                          className={`h-3 w-3 ${
                            i < currentAnalysis.overallRating / 2 ? 'text-yellow-400 fill-current' : 'text-gray-300'
                          }`} 
                        />
                      ))}
                    </div>
                  </div>
                  <Target className="h-8 w-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">추천 액션</p>
                    <Badge className={`${getActionColor(currentAnalysis.strategyAnalysis.recommendedAction)} text-white`}>
                      {currentAnalysis.strategyAnalysis.recommendedAction}
                    </Badge>
                    <p className="text-sm text-muted-foreground mt-1">
                      신뢰도: {(currentAnalysis.strategyAnalysis.confidence * 100).toFixed(0)}%
                    </p>
                  </div>
                  <Brain className="h-8 w-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-muted-foreground">리스크 레벨</p>
                    <Badge className={`${getRiskColor(currentAnalysis.riskAssessment.riskLevel)} text-white`}>
                      {currentAnalysis.riskAssessment.riskLevel}
                    </Badge>
                    <p className="text-sm text-muted-foreground mt-1">
                      리스크 점수: {(currentAnalysis.riskAssessment.overallRiskScore * 100).toFixed(0)}%
                    </p>
                  </div>
                  <Shield className="h-8 w-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Detailed Analysis Tabs */}
          <Tabs value={selectedTab} onValueChange={setSelectedTab}>
            <TabsList className="grid w-full grid-cols-5">
              <TabsTrigger value="overview">종합</TabsTrigger>
              <TabsTrigger value="strategy">AI 전략</TabsTrigger>
              <TabsTrigger value="technical">기술적 분석</TabsTrigger>
              <TabsTrigger value="fundamental">기본적 분석</TabsTrigger>
              <TabsTrigger value="risk">리스크</TabsTrigger>
            </TabsList>

            <TabsContent value="overview" className="space-y-4">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>핵심 인사이트</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {currentAnalysis.keyInsights.map((insight, index) => (
                        <div key={index} className="flex items-start gap-2">
                          <div className="w-2 h-2 rounded-full bg-blue-600 mt-2 flex-shrink-0" />
                          <p className="text-sm">{insight}</p>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>가격 목표</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">단기 목표 (1-3개월)</span>
                        <span className="font-semibold">${currentAnalysis.priceTargets.shortTermTarget.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">중기 목표 (3-6개월)</span>
                        <span className="font-semibold">${currentAnalysis.priceTargets.mediumTermTarget.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">장기 목표 (6-12개월)</span>
                        <span className="font-semibold">${currentAnalysis.priceTargets.longTermTarget.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between items-center pt-2 border-t">
                        <span className="text-sm text-red-600">손절매 기준</span>
                        <span className="font-semibold text-red-600">${currentAnalysis.priceTargets.stopLoss.toFixed(2)}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>투자 추천</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="bg-blue-50 p-4 rounded-lg">
                    <h4 className="font-semibold text-blue-900 mb-2">
                      {currentAnalysis.investmentRecommendation}
                    </h4>
                    <p className="text-blue-800 text-sm">
                      {currentAnalysis.strategyAnalysis.aiInsights}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="strategy" className="space-y-4">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>AI 전략 분석 레이더</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width="100%" height={300}>
                      <RadarChart data={strategyRadarData}>
                        <PolarGrid />
                        <PolarAngleAxis dataKey="strategy" />
                        <PolarRadiusAxis angle={18} domain={[0, 10]} />
                        <Radar
                          name="Strategy Score"
                          dataKey="score"
                          stroke="#3b82f6"
                          fill="#3b82f6"
                          fillOpacity={0.3}
                        />
                      </RadarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>전략별 점수</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      {Object.entries(currentAnalysis.strategyAnalysis.strategyScores).map(([strategy, score]) => (
                        <div key={strategy} className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm font-medium">{strategy.replace('_', ' ')}</span>
                            <span className="text-sm text-muted-foreground">{score}/10</span>
                          </div>
                          <Progress value={score * 10} className="h-2" />
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>AI 인사이트</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <p className="text-sm">
                      <strong>주요 전략:</strong> {currentAnalysis.strategyAnalysis.primaryStrategy}
                    </p>
                    <p className="text-sm mt-2">
                      <strong>AI 분석:</strong> {currentAnalysis.strategyAnalysis.aiInsights}
                    </p>
                    <p className="text-sm mt-2">
                      <strong>신뢰도:</strong> {(currentAnalysis.strategyAnalysis.confidence * 100).toFixed(1)}%
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="technical" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">RSI 지표</h4>
                    <div className="text-2xl font-bold mb-1">
                      {currentAnalysis.technicalAnalysis.rsi.toFixed(1)}
                    </div>
                    <Badge variant="outline">
                      {currentAnalysis.technicalAnalysis.rsiSignal}
                    </Badge>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">MACD</h4>
                    <div className="text-2xl font-bold mb-1">
                      {currentAnalysis.technicalAnalysis.macd.toFixed(3)}
                    </div>
                    <p className="text-sm text-muted-foreground">
                      신호: {currentAnalysis.technicalAnalysis.macdSignal.toFixed(3)}
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">추세 방향</h4>
                    <Badge className={currentAnalysis.technicalAnalysis.trendDirection === 'BULLISH' ? 'bg-green-500' : 'bg-red-500'}>
                      {currentAnalysis.technicalAnalysis.trendDirection}
                    </Badge>
                    <p className="text-sm text-muted-foreground mt-1">
                      변동성: {currentAnalysis.technicalAnalysis.volatilityLevel}
                    </p>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>지지/저항 수준</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-muted-foreground">저항선</span>
                      <span className="font-semibold text-red-600">
                        ${currentAnalysis.technicalAnalysis.resistanceLevel.toFixed(2)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-muted-foreground">현재가</span>
                      <span className="font-semibold">
                        ${currentAnalysis.currentPrice.toFixed(2)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-muted-foreground">지지선</span>
                      <span className="font-semibold text-green-600">
                        ${currentAnalysis.technicalAnalysis.supportLevel.toFixed(2)}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>기술적 분석 요약</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <p className="text-sm">
                      <strong>기술적 점수:</strong> {currentAnalysis.technicalAnalysis.technicalScore}/10
                    </p>
                    <p className="text-sm mt-2">
                      {currentAnalysis.technicalAnalysis.technicalSummary}
                    </p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="fundamental" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">P/E 비율</h4>
                    <div className="text-2xl font-bold">
                      {currentAnalysis.fundamentalAnalysis.peRatio.toFixed(1)}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">P/B 비율</h4>
                    <div className="text-2xl font-bold">
                      {currentAnalysis.fundamentalAnalysis.pbRatio.toFixed(1)}
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">ROE</h4>
                    <div className="text-2xl font-bold">
                      {currentAnalysis.fundamentalAnalysis.roe.toFixed(1)}%
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">부채비율</h4>
                    <div className="text-2xl font-bold">
                      {(currentAnalysis.fundamentalAnalysis.debtToEquityRatio * 100).toFixed(1)}%
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">매출 성장률</h4>
                    <div className={`text-2xl font-bold ${
                      currentAnalysis.fundamentalAnalysis.revenueGrowth >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {currentAnalysis.fundamentalAnalysis.revenueGrowth.toFixed(1)}%
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">기본적 점수</h4>
                    <div className="text-2xl font-bold">
                      {currentAnalysis.fundamentalAnalysis.fundamentalScore}/10
                    </div>
                  </CardContent>
                </Card>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">가치 평가</h4>
                    <Badge variant="outline">
                      {currentAnalysis.fundamentalAnalysis.valueRating.replace('_', ' ')}
                    </Badge>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">재무 건전성</h4>
                    <Badge variant="outline">
                      {currentAnalysis.fundamentalAnalysis.financialHealthRating}
                    </Badge>
                  </CardContent>
                </Card>

                <Card>
                  <CardContent className="p-4">
                    <h4 className="font-semibold mb-2">성장 잠재력</h4>
                    <Badge variant="outline">
                      {currentAnalysis.fundamentalAnalysis.growthPotential}
                    </Badge>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            <TabsContent value="risk" className="space-y-4">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle>리스크 구성</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width="100%" height={200}>
                      <BarChart data={riskData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="value" fill="#f59e0b" />
                      </BarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle>리스크 평가</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">종합 리스크</span>
                        <Badge className={`${getRiskColor(currentAnalysis.riskAssessment.riskLevel)} text-white`}>
                          {currentAnalysis.riskAssessment.riskLevel}
                        </Badge>
                      </div>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-sm">변동성 리스크</span>
                          <span className="text-sm">{(currentAnalysis.riskAssessment.volatilityRisk * 100).toFixed(0)}%</span>
                        </div>
                        <Progress value={currentAnalysis.riskAssessment.volatilityRisk * 100} />
                      </div>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-sm">유동성 리스크</span>
                          <span className="text-sm">{(currentAnalysis.riskAssessment.liquidityRisk * 100).toFixed(0)}%</span>
                        </div>
                        <Progress value={currentAnalysis.riskAssessment.liquidityRisk * 100} />
                      </div>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-sm">시장심도 리스크</span>
                          <span className="text-sm">{(currentAnalysis.riskAssessment.marketDepthRisk * 100).toFixed(0)}%</span>
                        </div>
                        <Progress value={currentAnalysis.riskAssessment.marketDepthRisk * 100} />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <AlertTriangle className="h-5 w-5 text-yellow-500" />
                    리스크 완화 방안
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {currentAnalysis.riskAssessment.riskMitigationSuggestions.map((suggestion, index) => (
                      <div key={index} className="flex items-start gap-2">
                        <div className="w-2 h-2 rounded-full bg-yellow-500 mt-2 flex-shrink-0" />
                        <p className="text-sm">{suggestion}</p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      )}
    </div>
  );
}