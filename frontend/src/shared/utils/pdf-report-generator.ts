import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import type { DCASimulationResponse } from '@/shared/api/types/dca-types';
import type { Company } from '@/shared/api/company-client';

interface PDFReportOptions {
  simulationResult: DCASimulationResponse;
  selectedCompany?: Company | null;
  chartElement?: HTMLElement | null;
  includeChart?: boolean;
  includeDetailedTable?: boolean;
}

interface RiskMetrics {
  volatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  calmarRatio: number;
  sortino: number;
}

/**
 * Advanced PDF Report Generator for DCA Simulation
 * Generates comprehensive investment analysis reports
 */
export class PDFReportGenerator {
  private pdf: jsPDF;
  private pageWidth: number;
  private pageHeight: number;
  private margin: number = 20;
  private currentY: number = 20;

  constructor() {
    this.pdf = new jsPDF('p', 'mm', 'a4');
    this.pageWidth = this.pdf.internal.pageSize.getWidth();
    this.pageHeight = this.pdf.internal.pageSize.getHeight();
  }

  /**
   * Generate comprehensive DCA simulation report
   */
  async generateReport(options: PDFReportOptions): Promise<void> {
    const { simulationResult, selectedCompany, chartElement, includeChart = true, includeDetailedTable = true } = options;

    // Reset position
    this.currentY = this.margin;

    // Generate report sections
    this.addHeader(simulationResult, selectedCompany);
    this.addExecutiveSummary(simulationResult);
    this.addKeyMetrics(simulationResult);
    this.addRiskAnalysis(simulationResult);

    if (includeChart && chartElement) {
      await this.addChart(chartElement);
    }

    this.addPerformanceAnalysis(simulationResult);
    this.addBenchmarkComparison(simulationResult);

    if (includeDetailedTable) {
      this.addInvestmentTable(simulationResult);
    }

    this.addDisclaimer();
    this.addFooter();
  }

  /**
   * Save the generated PDF
   */
  save(filename?: string): void {
    const defaultFilename = `DCA-Analysis-${new Date().toISOString().split('T')[0]}.pdf`;
    this.pdf.save(filename || defaultFilename);
  }

  /**
   * Add report header with company information
   */
  private addHeader(result: DCASimulationResponse, company?: Company | null): void {
    // Title
    this.pdf.setFontSize(20);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('DCA 투자 시뮬레이션 보고서', this.margin, this.currentY);
    this.currentY += 10;

    // Company information
    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'normal');
    const companyInfo = company
      ? `${company.nameKr} (${company.nameEn}) - ${result.symbol}`
      : result.symbol;
    this.pdf.text(companyInfo, this.margin, this.currentY);
    this.currentY += 8;

    // Report date
    this.pdf.setFontSize(10);
    this.pdf.setTextColor(100);
    this.pdf.text(`보고서 생성일: ${new Date().toLocaleDateString('ko-KR')}`, this.margin, this.currentY);
    this.currentY += 15;

    // Add separator line
    this.pdf.setDrawColor(200);
    this.pdf.line(this.margin, this.currentY, this.pageWidth - this.margin, this.currentY);
    this.currentY += 10;
  }

  /**
   * Add executive summary section
   */
  private addExecutiveSummary(result: DCASimulationResponse): void {
    this.pdf.setTextColor(0);
    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('📊 투자 성과 요약', this.margin, this.currentY);
    this.currentY += 8;

    this.pdf.setFontSize(10);
    this.pdf.setFont('helvetica', 'normal');

    const summaryText = [
      `총 투자금액: ₩${this.formatNumber(result.totalInvestmentAmount)}`,
      `최종 포트폴리오 가치: ₩${this.formatNumber(result.finalPortfolioValue)}`,
      `총 수익률: ${result.totalReturnPercentage.toFixed(2)}%`,
      `연평균 수익률: ${result.annualizedReturn.toFixed(2)}%`,
      `투자 기간: ${result.investmentRecords.length}개월`,
    ];

    summaryText.forEach(text => {
      this.pdf.text(text, this.margin + 5, this.currentY);
      this.currentY += 5;
    });

    this.currentY += 5;
  }

  /**
   * Add key performance metrics
   */
  private addKeyMetrics(result: DCASimulationResponse): void {
    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('🎯 핵심 성과 지표', this.margin, this.currentY);
    this.currentY += 10;

    // Create metrics table
    const metrics = [
      ['지표', '값', '설명'],
      ['총 수익', `₩${this.formatNumber(result.finalPortfolioValue - result.totalInvestmentAmount)}`, '최종 손익'],
      ['수익률', `${result.totalReturnPercentage.toFixed(2)}%`, '총 투자 대비 수익률'],
      ['연평균 수익률', `${result.annualizedReturn.toFixed(2)}%`, '연간 복리 수익률'],
      ['최대 포트폴리오 가치', `₩${this.formatNumber(result.maxPortfolioValue || result.finalPortfolioValue)}`, '투자 기간 중 최고치'],
    ];

    this.addTable(metrics);
    this.currentY += 10;
  }

  /**
   * Add risk analysis section with advanced metrics
   */
  private addRiskAnalysis(result: DCASimulationResponse): void {
    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('⚠️ 위험 분석', this.margin, this.currentY);
    this.currentY += 10;

    // Calculate risk metrics
    const riskMetrics = this.calculateRiskMetrics(result);

    const riskData = [
      ['위험 지표', '값', '해석'],
      ['변동성 (연간)', `${riskMetrics.volatility.toFixed(2)}%`, riskMetrics.volatility > 20 ? '높음' : riskMetrics.volatility > 10 ? '보통' : '낮음'],
      ['샤프 비율', riskMetrics.sharpeRatio.toFixed(2), riskMetrics.sharpeRatio > 1 ? '우수' : riskMetrics.sharpeRatio > 0 ? '양호' : '부족'],
      ['최대 낙폭', `${riskMetrics.maxDrawdown.toFixed(2)}%`, riskMetrics.maxDrawdown > 20 ? '위험' : '양호'],
      ['칼마 비율', riskMetrics.calmarRatio.toFixed(2), riskMetrics.calmarRatio > 0.5 ? '우수' : '보통'],
    ];

    this.addTable(riskData);
    this.currentY += 10;
  }

  /**
   * Add chart to PDF if provided
   */
  private async addChart(chartElement: HTMLElement): Promise<void> {
    try {
      this.checkPageSpace(100); // Check if we need new page for chart

      this.pdf.setFontSize(14);
      this.pdf.setFont('helvetica', 'bold');
      this.pdf.text('📈 투자 성과 차트', this.margin, this.currentY);
      this.currentY += 10;

      const canvas = await html2canvas(chartElement, {
        backgroundColor: 'white',
        scale: 2,
        logging: false,
      });

      const imgData = canvas.toDataURL('image/png');
      const imgWidth = this.pageWidth - (this.margin * 2);
      const imgHeight = (canvas.height * imgWidth) / canvas.width;

      this.pdf.addImage(imgData, 'PNG', this.margin, this.currentY, imgWidth, imgHeight);
      this.currentY += imgHeight + 10;
    } catch (error) {
      console.error('차트 추가 중 오류:', error);
      this.pdf.setFontSize(10);
      this.pdf.text('차트를 불러올 수 없습니다.', this.margin, this.currentY);
      this.currentY += 10;
    }
  }

  /**
   * Add performance analysis section
   */
  private addPerformanceAnalysis(result: DCASimulationResponse): void {
    this.checkPageSpace(50);

    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('📈 성과 분석', this.margin, this.currentY);
    this.currentY += 10;

    this.pdf.setFontSize(10);
    this.pdf.setFont('helvetica', 'normal');

    const analysis = this.generatePerformanceAnalysis(result);
    const lines = this.pdf.splitTextToSize(analysis, this.pageWidth - (this.margin * 2));

    lines.forEach((line: string) => {
      this.checkPageSpace(5);
      this.pdf.text(line, this.margin, this.currentY);
      this.currentY += 5;
    });

    this.currentY += 5;
  }

  /**
   * Add benchmark comparison section
   */
  private addBenchmarkComparison(result: DCASimulationResponse): void {
    this.checkPageSpace(30);

    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('🔄 벤치마크 대비 성과', this.margin, this.currentY);
    this.currentY += 10;

    const benchmarkData = [
      ['벤치마크', '수익률', '초과수익', '평가'],
      ['S&P 500', `${((result.sp500ReturnAmount - result.totalInvestmentAmount) / result.totalInvestmentAmount * 100).toFixed(2)}%`,
       `${result.outperformanceVsSP500.toFixed(2)}%`, result.outperformanceVsSP500 > 0 ? '우수' : '부족'],
      ['NASDAQ', `${((result.nasdaqReturnAmount - result.totalInvestmentAmount) / result.totalInvestmentAmount * 100).toFixed(2)}%`,
       `${result.outperformanceVsNASDAQ.toFixed(2)}%`, result.outperformanceVsNASDAQ > 0 ? '우수' : '부족'],
    ];

    this.addTable(benchmarkData);
    this.currentY += 10;
  }

  /**
   * Add detailed investment records table
   */
  private addInvestmentTable(result: DCASimulationResponse): void {
    this.checkPageSpace(40);

    this.pdf.setFontSize(14);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('📋 투자 기록 상세', this.margin, this.currentY);
    this.currentY += 10;

    // Show only first 10 records to avoid too long table
    const records = result.investmentRecords.slice(0, 10);
    const tableData = [
      ['날짜', '투자금액', '주가', '매수주식', '포트폴리오'],
      ...records.map(record => [
        new Date(record.investmentDate).toLocaleDateString('ko-KR'),
        `₩${this.formatNumber(record.investmentAmount)}`,
        `$${record.stockPrice.toFixed(2)}`,
        record.sharesPurchased.toFixed(3),
        `₩${this.formatNumber(record.portfolioValue)}`,
      ])
    ];

    if (result.investmentRecords.length > 10) {
      tableData.push(['...', '...', '...', '...', '...']);
    }

    this.addTable(tableData);
    this.currentY += 10;
  }

  /**
   * Add legal disclaimer
   */
  private addDisclaimer(): void {
    this.checkPageSpace(30);

    this.pdf.setFontSize(12);
    this.pdf.setFont('helvetica', 'bold');
    this.pdf.text('⚠️ 투자 유의사항', this.margin, this.currentY);
    this.currentY += 8;

    this.pdf.setFontSize(8);
    this.pdf.setFont('helvetica', 'normal');
    this.pdf.setTextColor(100);

    const disclaimer = `본 보고서는 과거 데이터를 기반으로 한 시뮬레이션 결과이며, 미래 수익을 보장하지 않습니다.
실제 투자 시에는 세금, 거래비용, 슬리피지 등이 발생할 수 있어 시뮬레이션 결과와 차이가 날 수 있습니다.
투자 결정은 신중히 하시기 바라며, 필요시 전문가의 조언을 구하시기 바랍니다.`;

    const lines = this.pdf.splitTextToSize(disclaimer, this.pageWidth - (this.margin * 2));
    lines.forEach((line: string) => {
      this.pdf.text(line, this.margin, this.currentY);
      this.currentY += 4;
    });
  }

  /**
   * Add footer with page numbers and generation info
   */
  private addFooter(): void {
    const pageCount = this.pdf.getNumberOfPages();

    for (let i = 1; i <= pageCount; i++) {
      this.pdf.setPage(i);
      this.pdf.setFontSize(8);
      this.pdf.setTextColor(100);
      this.pdf.text(
        `StockQuest DCA Analysis Report - Page ${i}/${pageCount}`,
        this.margin,
        this.pageHeight - 10
      );
    }
  }

  /**
   * Helper: Add table to PDF
   */
  private addTable(data: string[][]): void {
    const rowHeight = 8;
    const colWidths = [60, 50, 60]; // Adjust based on content

    data.forEach((row, rowIndex) => {
      this.checkPageSpace(rowHeight);

      let x = this.margin;
      row.forEach((cell, colIndex) => {
        const width = colWidths[colIndex] || 40;

        // Header styling
        if (rowIndex === 0) {
          this.pdf.setFillColor(240, 240, 240);
          this.pdf.rect(x, this.currentY - 5, width, rowHeight, 'F');
          this.pdf.setFont('helvetica', 'bold');
        } else {
          this.pdf.setFont('helvetica', 'normal');
        }

        this.pdf.setFontSize(9);
        this.pdf.text(cell, x + 2, this.currentY);
        x += width;
      });

      this.currentY += rowHeight;
    });
  }

  /**
   * Helper: Check if we need a new page
   */
  private checkPageSpace(requiredSpace: number): void {
    if (this.currentY + requiredSpace > this.pageHeight - 30) {
      this.pdf.addPage();
      this.currentY = this.margin;
    }
  }

  /**
   * Helper: Format numbers with thousands separators
   */
  private formatNumber(value: number): string {
    return new Intl.NumberFormat('ko-KR').format(value);
  }

  /**
   * Calculate advanced risk metrics
   */
  private calculateRiskMetrics(result: DCASimulationResponse): RiskMetrics {
    const returns = this.calculateMonthlyReturns(result);
    const avgReturn = returns.reduce((sum, r) => sum + r, 0) / returns.length;

    // Volatility (standard deviation of returns)
    const variance = returns.reduce((sum, r) => sum + Math.pow(r - avgReturn, 2), 0) / returns.length;
    const volatility = Math.sqrt(variance) * Math.sqrt(12) * 100; // Annualized

    // Sharpe Ratio (assuming 2% risk-free rate)
    const riskFreeRate = 0.02 / 12; // Monthly risk-free rate
    const excessReturns = returns.map(r => r - riskFreeRate);
    const avgExcessReturn = excessReturns.reduce((sum, r) => sum + r, 0) / excessReturns.length;
    const sharpeRatio = avgExcessReturn / (Math.sqrt(variance));

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

    // Calmar Ratio
    const calmarRatio = maxDrawdown > 0 ? (result.annualizedReturn / maxDrawdown) : 0;

    // Sortino Ratio (simplified)
    const downSideReturns = returns.filter(r => r < avgReturn);
    const downSideVariance = downSideReturns.length > 0
      ? downSideReturns.reduce((sum, r) => sum + Math.pow(r - avgReturn, 2), 0) / downSideReturns.length
      : variance;
    const sortino = avgExcessReturn / Math.sqrt(downSideVariance);

    return {
      volatility,
      sharpeRatio: sharpeRatio * Math.sqrt(12), // Annualized
      maxDrawdown,
      calmarRatio,
      sortino: sortino * Math.sqrt(12), // Annualized
    };
  }

  /**
   * Calculate monthly returns from investment records
   */
  private calculateMonthlyReturns(result: DCASimulationResponse): number[] {
    const returns: number[] = [];

    for (let i = 1; i < result.investmentRecords.length; i++) {
      const prevValue = result.investmentRecords[i - 1].portfolioValue;
      const currValue = result.investmentRecords[i].portfolioValue;
      const investmentAmount = result.investmentRecords[i].investmentAmount;

      // Calculate return excluding new investment
      const adjustedPrevValue = prevValue + investmentAmount;
      const monthlyReturn = (currValue - adjustedPrevValue) / adjustedPrevValue;
      returns.push(monthlyReturn);
    }

    return returns;
  }

  /**
   * Generate performance analysis text
   */
  private generatePerformanceAnalysis(result: DCASimulationResponse): string {
    const totalReturn = result.totalReturnPercentage;
    const annualizedReturn = result.annualizedReturn;

    let analysis = `이 투자 전략은 총 ${result.totalInvestmentAmount.toLocaleString()}원을 투자하여 `;
    analysis += `${result.finalPortfolioValue.toLocaleString()}원의 최종 가치를 달성했습니다. `;

    if (totalReturn > 0) {
      analysis += `총 ${totalReturn.toFixed(2)}%의 양의 수익률을 기록하여 성공적인 투자 결과를 보였습니다. `;
    } else {
      analysis += `총 ${Math.abs(totalReturn).toFixed(2)}%의 손실을 기록했습니다. `;
    }

    analysis += `연평균 수익률은 ${annualizedReturn.toFixed(2)}%로, `;

    if (annualizedReturn > 10) {
      analysis += `매우 우수한 성과를 보였습니다.`;
    } else if (annualizedReturn > 5) {
      analysis += `양호한 성과를 보였습니다.`;
    } else if (annualizedReturn > 0) {
      analysis += `보통 수준의 성과를 보였습니다.`;
    } else {
      analysis += `시장 상황이 어려웠음을 보여줍니다.`;
    }

    return analysis;
  }
}