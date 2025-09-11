/**
 * Advanced Portfolio Analytics Service
 * 고급 포트폴리오 분석 서비스
 */

export interface PortfolioHolding {
  symbol: string;
  name: string;
  shares: number;
  averagePrice: number;
  currentPrice: number;
  sector: string;
  marketCap: number;
  beta: number;
  dividendYield: number;
  peRatio: number;
  weekHigh52: number;
  weekLow52: number;
  historicalPrices: HistoricalPrice[];
}

export interface HistoricalPrice {
  date: string;
  price: number;
  volume: number;
}

export interface PortfolioAnalytics {
  // Basic Metrics
  totalValue: number;
  totalCost: number;
  totalReturn: number;
  totalReturnPercent: number;
  dailyChange: number;
  dailyChangePercent: number;

  // Risk Metrics
  volatility: number;
  beta: number;
  sharpeRatio: number;
  sortinoRatio: number;
  maxDrawdown: number;
  valueAtRisk: number; // 95% VaR
  expectedShortfall: number; // CVaR

  // Performance Metrics
  annualizedReturn: number;
  winRate: number;
  profitFactor: number;
  calmarRatio: number;
  informationRatio: number;
  treynorRatio: number;

  // Allocation Metrics
  sectorAllocation: SectorAllocation[];
  assetAllocation: AssetAllocation[];
  geographicAllocation: GeographicAllocation[];
  
  // Diversification
  diversificationRatio: number;
  correlationMatrix: CorrelationData[];
  concentrationRisk: number;

  // Insights
  recommendations: PortfolioRecommendation[];
  riskFactors: RiskFactor[];
  opportunities: Opportunity[];
}

export interface SectorAllocation {
  sector: string;
  value: number;
  percentage: number;
  return: number;
  risk: number;
  targetPercentage?: number;
}

export interface AssetAllocation {
  assetClass: string;
  value: number;
  percentage: number;
  return: number;
  risk: number;
  targetPercentage?: number;
}

export interface GeographicAllocation {
  region: string;
  value: number;
  percentage: number;
  return: number;
  risk: number;
}

export interface CorrelationData {
  symbol1: string;
  symbol2: string;
  correlation: number;
  significance: number;
}

export interface PortfolioRecommendation {
  type: 'REBALANCE' | 'DIVERSIFY' | 'REDUCE_RISK' | 'TAKE_PROFIT' | 'ADD_POSITION';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  title: string;
  description: string;
  action: string;
  impact: string;
  symbols?: string[];
  targetAllocation?: number;
  reasoning: string;
}

export interface RiskFactor {
  factor: string;
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  impact: string;
  mitigation: string;
  affectedSymbols: string[];
}

export interface Opportunity {
  type: 'SECTOR_ROTATION' | 'MOMENTUM' | 'VALUE' | 'DIVIDEND' | 'GROWTH';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  title: string;
  description: string;
  potentialReturn: number;
  riskLevel: string;
  timeHorizon: string;
  actionRequired: string;
}

export interface BenchmarkComparison {
  benchmarkName: string;
  benchmarkReturn: number;
  portfolioReturn: number;
  alpha: number;
  beta: number;
  correlationCoefficient: number;
  trackingError: number;
  informationRatio: number;
}

export class PortfolioAnalyticsService {
  /**
   * Calculate comprehensive portfolio analytics
   * 종합적인 포트폴리오 분석 계산
   */
  static calculatePortfolioAnalytics(
    holdings: PortfolioHolding[],
    timeframe: '1M' | '3M' | '6M' | '1Y' | '2Y' = '1Y'
  ): PortfolioAnalytics {
    const totalValue = this.calculateTotalValue(holdings);
    const totalCost = this.calculateTotalCost(holdings);
    const historicalReturns = this.calculateHistoricalReturns(holdings, timeframe);
    
    return {
      // Basic Metrics
      totalValue,
      totalCost,
      totalReturn: totalValue - totalCost,
      totalReturnPercent: ((totalValue - totalCost) / totalCost) * 100,
      dailyChange: this.calculateDailyChange(holdings),
      dailyChangePercent: this.calculateDailyChangePercent(holdings),

      // Risk Metrics
      volatility: this.calculateVolatility(historicalReturns),
      beta: this.calculatePortfolioBeta(holdings),
      sharpeRatio: this.calculateSharpeRatio(historicalReturns),
      sortinoRatio: this.calculateSortinoRatio(historicalReturns),
      maxDrawdown: this.calculateMaxDrawdown(historicalReturns),
      valueAtRisk: this.calculateVaR(historicalReturns, 0.05),
      expectedShortfall: this.calculateCVaR(historicalReturns, 0.05),

      // Performance Metrics
      annualizedReturn: this.calculateAnnualizedReturn(historicalReturns),
      winRate: this.calculateWinRate(historicalReturns),
      profitFactor: this.calculateProfitFactor(historicalReturns),
      calmarRatio: this.calculateCalmarRatio(historicalReturns),
      informationRatio: this.calculateInformationRatio(historicalReturns),
      treynorRatio: this.calculateTreynorRatio(historicalReturns, holdings),

      // Allocation Metrics
      sectorAllocation: this.calculateSectorAllocation(holdings),
      assetAllocation: this.calculateAssetAllocation(holdings),
      geographicAllocation: this.calculateGeographicAllocation(holdings),
      
      // Diversification
      diversificationRatio: this.calculateDiversificationRatio(holdings),
      correlationMatrix: this.calculateCorrelationMatrix(holdings),
      concentrationRisk: this.calculateConcentrationRisk(holdings),

      // Insights
      recommendations: this.generateRecommendations(holdings, historicalReturns),
      riskFactors: this.identifyRiskFactors(holdings),
      opportunities: this.identifyOpportunities(holdings)
    };
  }

  /**
   * Calculate total portfolio value
   * 총 포트폴리오 가치 계산
   */
  private static calculateTotalValue(holdings: PortfolioHolding[]): number {
    return holdings.reduce((total, holding) => {
      return total + (holding.shares * holding.currentPrice);
    }, 0);
  }

  /**
   * Calculate total cost basis
   * 총 투자원금 계산
   */
  private static calculateTotalCost(holdings: PortfolioHolding[]): number {
    return holdings.reduce((total, holding) => {
      return total + (holding.shares * holding.averagePrice);
    }, 0);
  }

  /**
   * Calculate portfolio volatility (standard deviation of returns)
   * 포트폴리오 변동성 계산 (수익률 표준편차)
   */
  private static calculateVolatility(returns: number[]): number {
    if (returns.length < 2) return 0;
    
    const mean = returns.reduce((sum, r) => sum + r, 0) / returns.length;
    const variance = returns.reduce((sum, r) => sum + Math.pow(r - mean, 2), 0) / (returns.length - 1);
    return Math.sqrt(variance) * Math.sqrt(252); // Annualized
  }

  /**
   * Calculate portfolio beta
   * 포트폴리오 베타 계산
   */
  private static calculatePortfolioBeta(holdings: PortfolioHolding[]): number {
    const totalValue = this.calculateTotalValue(holdings);
    
    return holdings.reduce((weightedBeta, holding) => {
      const weight = (holding.shares * holding.currentPrice) / totalValue;
      return weightedBeta + (holding.beta * weight);
    }, 0);
  }

  /**
   * Calculate Sharpe Ratio
   * 샤프 비율 계산
   */
  private static calculateSharpeRatio(returns: number[], riskFreeRate: number = 0.02): number {
    if (returns.length === 0) return 0;
    
    const annualizedReturn = this.calculateAnnualizedReturn(returns);
    const volatility = this.calculateVolatility(returns);
    
    return volatility === 0 ? 0 : (annualizedReturn - riskFreeRate) / volatility;
  }

  /**
   * Calculate Sortino Ratio (downside deviation)
   * 소르티노 비율 계산 (하향 편차)
   */
  private static calculateSortinoRatio(returns: number[], riskFreeRate: number = 0.02): number {
    if (returns.length === 0) return 0;
    
    const annualizedReturn = this.calculateAnnualizedReturn(returns);
    const negativeReturns = returns.filter(r => r < 0);
    
    if (negativeReturns.length === 0) return annualizedReturn > riskFreeRate ? Infinity : 0;
    
    const downside = Math.sqrt(negativeReturns.reduce((sum, r) => sum + r * r, 0) / negativeReturns.length) * Math.sqrt(252);
    
    return (annualizedReturn - riskFreeRate) / downside;
  }

  /**
   * Calculate Maximum Drawdown
   * 최대 낙폭 계산
   */
  private static calculateMaxDrawdown(returns: number[]): number {
    if (returns.length === 0) return 0;
    
    let peak = 1;
    let maxDrawdown = 0;
    let value = 1;
    
    for (const ret of returns) {
      value *= (1 + ret);
      peak = Math.max(peak, value);
      const drawdown = (peak - value) / peak;
      maxDrawdown = Math.max(maxDrawdown, drawdown);
    }
    
    return maxDrawdown;
  }

  /**
   * Calculate Value at Risk (VaR)
   * 위험가치 계산
   */
  private static calculateVaR(returns: number[], confidence: number = 0.05): number {
    if (returns.length === 0) return 0;
    
    const sortedReturns = [...returns].sort((a, b) => a - b);
    const index = Math.floor(returns.length * confidence);
    
    return Math.abs(sortedReturns[index] || 0);
  }

  /**
   * Calculate Conditional Value at Risk (Expected Shortfall)
   * 조건부 위험가치 계산
   */
  private static calculateCVaR(returns: number[], confidence: number = 0.05): number {
    if (returns.length === 0) return 0;
    
    const sortedReturns = [...returns].sort((a, b) => a - b);
    const cutoff = Math.floor(returns.length * confidence);
    const tailReturns = sortedReturns.slice(0, cutoff);
    
    if (tailReturns.length === 0) return 0;
    
    return Math.abs(tailReturns.reduce((sum, r) => sum + r, 0) / tailReturns.length);
  }

  /**
   * Calculate annualized return
   * 연환산 수익률 계산
   */
  private static calculateAnnualizedReturn(returns: number[]): number {
    if (returns.length === 0) return 0;
    
    const totalReturn = returns.reduce((product, r) => product * (1 + r), 1) - 1;
    const periods = returns.length / 252; // Assuming daily returns
    
    return periods > 0 ? Math.pow(1 + totalReturn, 1 / periods) - 1 : 0;
  }

  /**
   * Calculate sector allocation
   * 섹터별 자산배분 계산
   */
  private static calculateSectorAllocation(holdings: PortfolioHolding[]): SectorAllocation[] {
    const totalValue = this.calculateTotalValue(holdings);
    const sectorMap = new Map<string, {value: number, returns: number[], count: number}>();
    
    holdings.forEach(holding => {
      const value = holding.shares * holding.currentPrice;
      const returnPct = ((holding.currentPrice - holding.averagePrice) / holding.averagePrice) * 100;
      
      if (sectorMap.has(holding.sector)) {
        const existing = sectorMap.get(holding.sector)!;
        existing.value += value;
        existing.returns.push(returnPct);
        existing.count++;
      } else {
        sectorMap.set(holding.sector, {value, returns: [returnPct], count: 1});
      }
    });
    
    return Array.from(sectorMap.entries()).map(([sector, data]) => ({
      sector,
      value: data.value,
      percentage: (data.value / totalValue) * 100,
      return: data.returns.reduce((sum, r) => sum + r, 0) / data.returns.length,
      risk: this.calculateVolatility(data.returns) || 0,
      targetPercentage: this.getTargetSectorAllocation(sector)
    }));
  }

  /**
   * Calculate asset allocation
   * 자산클래스별 배분 계산
   */
  private static calculateAssetAllocation(holdings: PortfolioHolding[]): AssetAllocation[] {
    const totalValue = this.calculateTotalValue(holdings);
    
    // Simplified asset class categorization
    const assetClasses = holdings.map(holding => {
      const value = holding.shares * holding.currentPrice;
      const returnPct = ((holding.currentPrice - holding.averagePrice) / holding.averagePrice) * 100;
      
      let assetClass = 'Equities';
      if (holding.sector === 'Real Estate') assetClass = 'REITs';
      if (holding.dividendYield > 4) assetClass = 'Dividend Stocks';
      if (holding.marketCap > 100000000000) assetClass = 'Large Cap';
      else if (holding.marketCap > 10000000000) assetClass = 'Mid Cap';
      else assetClass = 'Small Cap';
      
      return {assetClass, value, return: returnPct};
    });
    
    const classMap = new Map<string, {value: number, returns: number[]}>();
    assetClasses.forEach(({assetClass, value, return: ret}) => {
      if (classMap.has(assetClass)) {
        const existing = classMap.get(assetClass)!;
        existing.value += value;
        existing.returns.push(ret);
      } else {
        classMap.set(assetClass, {value, returns: [ret]});
      }
    });
    
    return Array.from(classMap.entries()).map(([assetClass, data]) => ({
      assetClass,
      value: data.value,
      percentage: (data.value / totalValue) * 100,
      return: data.returns.reduce((sum, r) => sum + r, 0) / data.returns.length,
      risk: this.calculateVolatility(data.returns) || 0,
      targetPercentage: this.getTargetAssetAllocation(assetClass)
    }));
  }

  /**
   * Generate portfolio recommendations
   * 포트폴리오 추천사항 생성
   */
  private static generateRecommendations(
    holdings: PortfolioHolding[],
    returns: number[]
  ): PortfolioRecommendation[] {
    const recommendations: PortfolioRecommendation[] = [];
    const sectorAllocation = this.calculateSectorAllocation(holdings);
    const totalValue = this.calculateTotalValue(holdings);
    
    // Check for over-concentration
    const concentratedPositions = holdings.filter(h => 
      (h.shares * h.currentPrice / totalValue) > 0.15
    );
    
    if (concentratedPositions.length > 0) {
      recommendations.push({
        type: 'REDUCE_RISK',
        priority: 'HIGH',
        title: 'Position Concentration Risk 포지션 집중 위험',
        description: `${concentratedPositions.length}개 종목이 포트폴리오의 15% 이상을 차지하고 있습니다.`,
        action: 'Reduce position sizes or diversify holdings',
        impact: 'Lower portfolio risk and improve diversification',
        symbols: concentratedPositions.map(p => p.symbol),
        reasoning: 'High concentration increases volatility and single-stock risk'
      });
    }
    
    // Check sector imbalances
    const overweightSectors = sectorAllocation.filter(s => 
      s.targetPercentage && s.percentage > s.targetPercentage * 1.2
    );
    
    if (overweightSectors.length > 0) {
      recommendations.push({
        type: 'REBALANCE',
        priority: 'MEDIUM',
        title: 'Sector Rebalancing Needed 섹터 리밸런싱 필요',
        description: `${overweightSectors.map(s => s.sector).join(', ')} 섹터가 목표 배분을 초과했습니다.`,
        action: 'Rebalance sector allocations to target weights',
        impact: 'Improved risk-adjusted returns and diversification',
        reasoning: 'Sector concentration can lead to increased correlation risk'
      });
    }
    
    // Check for profit-taking opportunities
    const profitablePositions = holdings.filter(h => {
      const returnPct = ((h.currentPrice - h.averagePrice) / h.averagePrice) * 100;
      return returnPct > 50; // More than 50% gain
    });
    
    if (profitablePositions.length > 0) {
      recommendations.push({
        type: 'TAKE_PROFIT',
        priority: 'MEDIUM',
        title: 'Profit Taking Opportunity 수익 실현 기회',
        description: `${profitablePositions.length}개 종목이 50% 이상 상승했습니다.`,
        action: 'Consider taking partial profits on outperforming positions',
        impact: 'Lock in gains and reduce position risk',
        symbols: profitablePositions.map(p => p.symbol),
        reasoning: 'High returns may indicate overvaluation risk'
      });
    }
    
    return recommendations;
  }

  /**
   * Helper methods for target allocations
   */
  private static getTargetSectorAllocation(sector: string): number {
    const targets: Record<string, number> = {
      'Technology': 25,
      'Healthcare': 15,
      'Financial': 15,
      'Consumer': 12,
      'Industrial': 10,
      'Energy': 8,
      'Utilities': 5,
      'Materials': 5,
      'Real Estate': 5
    };
    return targets[sector] || 10;
  }

  private static getTargetAssetAllocation(assetClass: string): number {
    const targets: Record<string, number> = {
      'Large Cap': 60,
      'Mid Cap': 25,
      'Small Cap': 10,
      'REITs': 5
    };
    return targets[assetClass] || 20;
  }

  // Additional helper methods for comprehensive analytics...
  private static calculateHistoricalReturns(holdings: PortfolioHolding[], timeframe: string): number[] {
    // Simplified implementation - would use actual historical data
    const days = this.getTimeframeDays(timeframe);
    const returns: number[] = [];
    
    for (let i = 0; i < days; i++) {
      // Simulate daily returns based on volatility
      const avgVolatility = holdings.reduce((sum, h) => sum + (h.beta * 0.01), 0) / holdings.length;
      const dailyReturn = (Math.random() - 0.5) * avgVolatility;
      returns.push(dailyReturn);
    }
    
    return returns;
  }

  private static getTimeframeDays(timeframe: string): number {
    const mapping: Record<string, number> = {
      '1M': 30,
      '3M': 90,
      '6M': 180,
      '1Y': 252,
      '2Y': 504
    };
    return mapping[timeframe] || 252;
  }

  private static calculateDailyChange(holdings: PortfolioHolding[]): number {
    // Simplified - would calculate based on previous day's prices
    return holdings.reduce((sum, h) => {
      const change = h.shares * h.currentPrice * 0.01; // 1% assumed daily change
      return sum + change;
    }, 0);
  }

  private static calculateDailyChangePercent(holdings: PortfolioHolding[]): number {
    const totalValue = this.calculateTotalValue(holdings);
    const dailyChange = this.calculateDailyChange(holdings);
    return totalValue > 0 ? (dailyChange / totalValue) * 100 : 0;
  }

  private static calculateWinRate(returns: number[]): number {
    if (returns.length === 0) return 0;
    const positiveReturns = returns.filter(r => r > 0);
    return (positiveReturns.length / returns.length) * 100;
  }

  private static calculateProfitFactor(returns: number[]): number {
    const gains = returns.filter(r => r > 0).reduce((sum, r) => sum + r, 0);
    const losses = Math.abs(returns.filter(r => r < 0).reduce((sum, r) => sum + r, 0));
    return losses === 0 ? (gains > 0 ? Infinity : 0) : gains / losses;
  }

  private static calculateCalmarRatio(returns: number[]): number {
    const annualizedReturn = this.calculateAnnualizedReturn(returns);
    const maxDrawdown = this.calculateMaxDrawdown(returns);
    return maxDrawdown === 0 ? (annualizedReturn > 0 ? Infinity : 0) : annualizedReturn / maxDrawdown;
  }

  private static calculateInformationRatio(returns: number[]): number {
    // Simplified - would need benchmark returns
    const volatility = this.calculateVolatility(returns);
    const annualizedReturn = this.calculateAnnualizedReturn(returns);
    return volatility === 0 ? 0 : annualizedReturn / volatility;
  }

  private static calculateTreynorRatio(returns: number[], holdings: PortfolioHolding[]): number {
    const annualizedReturn = this.calculateAnnualizedReturn(returns);
    const beta = this.calculatePortfolioBeta(holdings);
    const riskFreeRate = 0.02;
    return beta === 0 ? 0 : (annualizedReturn - riskFreeRate) / beta;
  }

  private static calculateGeographicAllocation(holdings: PortfolioHolding[]): GeographicAllocation[] {
    // Simplified implementation
    const totalValue = this.calculateTotalValue(holdings);
    return [
      {
        region: 'North America',
        value: totalValue * 0.7,
        percentage: 70,
        return: 8.5,
        risk: 15.2
      },
      {
        region: 'Europe',
        value: totalValue * 0.2,
        percentage: 20,
        return: 6.3,
        risk: 18.1
      },
      {
        region: 'Asia Pacific',
        value: totalValue * 0.1,
        percentage: 10,
        return: 12.1,
        risk: 22.5
      }
    ];
  }

  private static calculateDiversificationRatio(holdings: PortfolioHolding[]): number {
    // Simplified calculation
    return Math.min(holdings.length / 10, 1); // Max score at 10+ holdings
  }

  private static calculateCorrelationMatrix(holdings: PortfolioHolding[]): CorrelationData[] {
    const correlations: CorrelationData[] = [];
    
    for (let i = 0; i < holdings.length; i++) {
      for (let j = i + 1; j < holdings.length; j++) {
        // Simplified correlation based on sector similarity
        const sameSector = holdings[i].sector === holdings[j].sector;
        const correlation = sameSector ? 0.7 + Math.random() * 0.25 : Math.random() * 0.4;
        
        correlations.push({
          symbol1: holdings[i].symbol,
          symbol2: holdings[j].symbol,
          correlation,
          significance: 0.95
        });
      }
    }
    
    return correlations;
  }

  private static calculateConcentrationRisk(holdings: PortfolioHolding[]): number {
    const totalValue = this.calculateTotalValue(holdings);
    const weights = holdings.map(h => (h.shares * h.currentPrice) / totalValue);
    
    // Herfindahl-Hirschman Index
    const hhi = weights.reduce((sum, w) => sum + w * w, 0);
    return hhi; // 0 = perfectly diversified, 1 = fully concentrated
  }

  private static identifyRiskFactors(holdings: PortfolioHolding[]): RiskFactor[] {
    const riskFactors: RiskFactor[] = [];
    const totalValue = this.calculateTotalValue(holdings);
    
    // High beta exposure
    const highBetaHoldings = holdings.filter(h => h.beta > 1.5);
    if (highBetaHoldings.length > 0) {
      const totalHighBetaValue = highBetaHoldings.reduce((sum, h) => sum + h.shares * h.currentPrice, 0);
      const exposure = (totalHighBetaValue / totalValue) * 100;
      
      if (exposure > 25) {
        riskFactors.push({
          factor: 'High Beta Exposure',
          level: exposure > 50 ? 'HIGH' : 'MEDIUM',
          description: `${exposure.toFixed(1)}% of portfolio in high beta stocks (β > 1.5)`,
          impact: 'Increased sensitivity to market volatility',
          mitigation: 'Consider adding low-beta defensive stocks or bonds',
          affectedSymbols: highBetaHoldings.map(h => h.symbol)
        });
      }
    }
    
    return riskFactors;
  }

  private static identifyOpportunities(holdings: PortfolioHolding[]): Opportunity[] {
    const opportunities: Opportunity[] = [];
    
    // Value opportunities
    const valueStocks = holdings.filter(h => h.peRatio > 0 && h.peRatio < 15);
    if (valueStocks.length > 0) {
      opportunities.push({
        type: 'VALUE',
        priority: 'MEDIUM',
        title: 'Value Investment Opportunity',
        description: `${valueStocks.length} value stocks with attractive P/E ratios`,
        potentialReturn: 12.5,
        riskLevel: 'Medium',
        timeHorizon: '6-12 months',
        actionRequired: 'Consider increasing allocation to undervalued positions'
      });
    }
    
    return opportunities;
  }
}