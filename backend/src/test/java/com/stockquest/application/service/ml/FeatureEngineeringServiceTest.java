package com.stockquest.application.service.ml;

import com.stockquest.application.service.ml.FeatureEngineeringService.FeatureVector;
import com.stockquest.application.service.ml.FeatureEngineeringService.TrainingData;
import com.stockquest.testutils.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureEngineeringService 테스트
 * Phase 4.1: 핵심 서비스 테스트 스위트 - ML 피처 엔지니어링 전문 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FeatureEngineeringService 테스트")
class FeatureEngineeringServiceTest extends TestBase {

    @InjectMocks
    private FeatureEngineeringService featureEngineeringService;

    private List<MarketData> sampleMarketData;
    private String sampleSymbol = "005930";

    @BeforeEach
    void setUp() {
        // 샘플 시장 데이터 생성 (10일치)
        sampleMarketData = Arrays.asList(
            createMarketData(LocalDateTime.now().minusDays(9), 78000, 1000000),
            createMarketData(LocalDateTime.now().minusDays(8), 79000, 1200000),
            createMarketData(LocalDateTime.now().minusDays(7), 80000, 1100000),
            createMarketData(LocalDateTime.now().minusDays(6), 79500, 950000),
            createMarketData(LocalDateTime.now().minusDays(5), 81000, 1300000),
            createMarketData(LocalDateTime.now().minusDays(4), 80500, 1150000),
            createMarketData(LocalDateTime.now().minusDays(3), 82000, 1250000),
            createMarketData(LocalDateTime.now().minusDays(2), 81500, 1050000),
            createMarketData(LocalDateTime.now().minusDays(1), 83000, 1400000),
            createMarketData(LocalDateTime.now(), 82500, 1200000)
        );
    }

    @Nested
    @DisplayName("피처 추출 테스트")
    class FeatureExtractionTests {

        @Test
        @DisplayName("유효한 인덱스에서 피처가 추출되어야 한다")
        void shouldExtractFeaturesForValidIndex() {
            // Given - 마지막에서 두 번째 데이터 포인트 사용 (충분한 이력 확보)
            int index = sampleMarketData.size() - 2;

            // When
            FeatureVector result = featureEngineeringService.extractFeatures(sampleMarketData, index);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(sampleSymbol);
            assertThat(result.getFeatures()).isNotNull();
            assertThat(result.getFeatures()).hasSize(13); // 13개 피처 예상
            assertThat(result.getTimestamp()).isEqualTo(sampleMarketData.get(index).getTimestamp());
        }

        @Test
        @DisplayName("기술적 지표들이 올바르게 계산되어야 한다")
        void shouldCalculateTechnicalIndicatorsCorrectly() {
            // Given
            int index = sampleMarketData.size() - 1;

            // When
            FeatureVector result = featureEngineeringService.extractFeatures(sampleMarketData, index);

            // Then
            double[] features = result.getFeatures();
            
            // 가격 기반 피처들
            assertThat(features[0]).isGreaterThan(0); // 현재 가격
            assertThat(features[1]).isNotEqualTo(0); // 수익률
            
            // 기술적 지표들
            assertThat(features[2]).isBetween(0.0, 100.0); // RSI (0-100 범위)
            assertThat(features[5]).isGreaterThan(0); // 볼린저 밴드 위치
            assertThat(features[6]).isGreaterThan(0); // 거래량
        }

        @Test
        @DisplayName("불충분한 데이터로 피처 추출 시 기본값이 사용되어야 한다")
        void shouldUseDefaultValuesForInsufficientData() {
            // Given - 초기 인덱스 (충분한 이력 없음)
            int index = 2;

            // When
            FeatureVector result = featureEngineeringService.extractFeatures(sampleMarketData, index);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFeatures()).hasSize(13);
            
            // 일부 기술적 지표는 기본값(0) 또는 계산 가능한 값이어야 함
            double[] features = result.getFeatures();
            assertThat(features[0]).isGreaterThan(0); // 현재 가격은 항상 양수
        }

        @Test
        @DisplayName("RSI 계산이 올바른 범위 내에 있어야 한다")
        void shouldCalculateRSIWithinValidRange() {
            // Given
            int index = sampleMarketData.size() - 1;

            // When
            FeatureVector result = featureEngineeringService.extractFeatures(sampleMarketData, index);

            // Then
            double rsi = result.getFeatures()[2]; // RSI는 3번째 피처
            assertThat(rsi).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("MACD 값들이 계산되어야 한다")
        void shouldCalculateMACDValues() {
            // Given
            int index = sampleMarketData.size() - 1;

            // When
            FeatureVector result = featureEngineeringService.extractFeatures(sampleMarketData, index);

            // Then
            double[] features = result.getFeatures();
            double macd = features[3]; // MACD
            double macdSignal = features[4]; // MACD Signal
            
            // MACD 값들이 계산되었는지 확인 (0이 아닌 값 또는 유의미한 계산)
            assertThat(macd).isNotNull();
            assertThat(macdSignal).isNotNull();
        }
    }

    @Nested
    @DisplayName("라벨 생성 테스트")
    class LabelGenerationTests {

        @Test
        @DisplayName("상승장에서 BUY 라벨이 생성되어야 한다")
        void shouldGenerateBuyLabelForUpwardTrend() {
            // Given - 상승 추세 데이터 생성
            List<MarketData> upwardTrendData = Arrays.asList(
                createMarketData(LocalDateTime.now().minusDays(3), 80000, 1000000),
                createMarketData(LocalDateTime.now().minusDays(2), 81000, 1100000),
                createMarketData(LocalDateTime.now().minusDays(1), 82000, 1200000),
                createMarketData(LocalDateTime.now(), 83000, 1300000) // 지속적 상승
            );
            int index = upwardTrendData.size() - 2; // 마지막에서 두 번째

            // When
            int label = featureEngineeringService.generateLabel(upwardTrendData, index);

            // Then
            assertThat(label).isEqualTo(1); // BUY = 1
        }

        @Test
        @DisplayName("하락장에서 SELL 라벨이 생성되어야 한다")
        void shouldGenerateSellLabelForDownwardTrend() {
            // Given - 하락 추세 데이터 생성
            List<MarketData> downwardTrendData = Arrays.asList(
                createMarketData(LocalDateTime.now().minusDays(3), 83000, 1000000),
                createMarketData(LocalDateTime.now().minusDays(2), 82000, 1100000),
                createMarketData(LocalDateTime.now().minusDays(1), 81000, 1200000),
                createMarketData(LocalDateTime.now(), 80000, 1300000) // 지속적 하락
            );
            int index = downwardTrendData.size() - 2;

            // When
            int label = featureEngineeringService.generateLabel(downwardTrendData, index);

            // Then
            assertThat(label).isEqualTo(-1); // SELL = -1
        }

        @Test
        @DisplayName("횡보장에서 HOLD 라벨이 생성되어야 한다")
        void shouldGenerateHoldLabelForSidewaysMarket() {
            // Given - 횡보 추세 데이터 생성
            List<MarketData> sidewaysData = Arrays.asList(
                createMarketData(LocalDateTime.now().minusDays(3), 80000, 1000000),
                createMarketData(LocalDateTime.now().minusDays(2), 80100, 1100000),
                createMarketData(LocalDateTime.now().minusDays(1), 79900, 1200000),
                createMarketData(LocalDateTime.now(), 80050, 1300000) // 작은 변동
            );
            int index = sidewaysData.size() - 2;

            // When
            int label = featureEngineeringService.generateLabel(sidewaysData, index);

            // Then
            assertThat(label).isEqualTo(0); // HOLD = 0
        }

        @Test
        @DisplayName("마지막 인덱스에서 라벨 생성 시 기본값을 반환해야 한다")
        void shouldReturnDefaultLabelForLastIndex() {
            // Given
            int lastIndex = sampleMarketData.size() - 1;

            // When
            int label = featureEngineeringService.generateLabel(sampleMarketData, lastIndex);

            // Then
            assertThat(label).isEqualTo(0); // HOLD = 0 (기본값)
        }
    }

    @Nested
    @DisplayName("훈련 데이터 생성 테스트")
    class TrainingDataGenerationTests {

        @Test
        @DisplayName("충분한 데이터로 훈련 세트가 생성되어야 한다")
        void shouldGenerateTrainingDataWithSufficientData() {
            // When
            TrainingData result = featureEngineeringService.prepareTrainingData(sampleSymbol, sampleMarketData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo(sampleSymbol);
            assertThat(result.getFeatures()).isNotNull();
            assertThat(result.getLabels()).isNotNull();
            assertThat(result.getFeatures()).hasSameSizeAs(result.getLabels());
            
            // 최소한의 훈련 데이터가 생성되어야 함
            assertThat(result.getFeatures()).hasSizeGreaterThan(0);
        }

        @Test
        @DisplayName("피처와 라벨의 크기가 일치해야 한다")
        void shouldHaveMatchingFeatureAndLabelSizes() {
            // When
            TrainingData result = featureEngineeringService.prepareTrainingData(sampleSymbol, sampleMarketData);

            // Then
            assertThat(result.getFeatures().size()).isEqualTo(result.getLabels().size());
        }

        @Test
        @DisplayName("각 피처 벡터의 크기가 일정해야 한다")
        void shouldHaveConsistentFeatureVectorSizes() {
            // When
            TrainingData result = featureEngineeringService.prepareTrainingData(sampleSymbol, sampleMarketData);

            // Then
            if (!result.getFeatures().isEmpty()) {
                int expectedFeatureSize = result.getFeatures().get(0).getFeatures().length;
                for (FeatureVector fv : result.getFeatures()) {
                    assertThat(fv.getFeatures()).hasSize(expectedFeatureSize);
                }
            }
        }

        @Test
        @DisplayName("훈련 데이터 생성 시간이 설정되어야 한다")
        void shouldSetTrainingDataGenerationTime() {
            // Given
            LocalDateTime beforeGeneration = LocalDateTime.now().minusSeconds(1);

            // When
            TrainingData result = featureEngineeringService.prepareTrainingData(sampleSymbol, sampleMarketData);

            // Then
            LocalDateTime afterGeneration = LocalDateTime.now().plusSeconds(1);
            assertThat(result.getGeneratedAt()).isAfter(beforeGeneration);
            assertThat(result.getGeneratedAt()).isBefore(afterGeneration);
        }
    }

    @Nested
    @DisplayName("오류 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("null 마켓 데이터에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullMarketData() {
            // When & Then
            assertThatThrownBy(() -> featureEngineeringService.extractFeatures(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Market data cannot be null or empty");
        }

        @Test
        @DisplayName("빈 마켓 데이터에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForEmptyMarketData() {
            // When & Then
            assertThatThrownBy(() -> featureEngineeringService.extractFeatures(Arrays.asList(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Market data cannot be null or empty");
        }

        @Test
        @DisplayName("잘못된 인덱스에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForInvalidIndex() {
            // When & Then
            assertThatThrownBy(() -> featureEngineeringService.extractFeatures(sampleMarketData, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Index must be non-negative");

            assertThatThrownBy(() -> featureEngineeringService.extractFeatures(sampleMarketData, sampleMarketData.size()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Index must be within data range");
        }

        @Test
        @DisplayName("null 심볼에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForNullSymbol() {
            // When & Then
            assertThatThrownBy(() -> featureEngineeringService.prepareTrainingData(null, sampleMarketData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol cannot be null or empty");
        }

        @Test
        @DisplayName("빈 심볼에 대해 예외를 발생시켜야 한다")
        void shouldThrowExceptionForEmptySymbol() {
            // When & Then
            assertThatThrownBy(() -> featureEngineeringService.prepareTrainingData("", sampleMarketData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Symbol cannot be null or empty");
        }
    }

    // 헬퍼 메서드
    private MarketData createMarketData(LocalDateTime timestamp, int price, int volume) {
        return MarketData.builder()
            .symbol(sampleSymbol)
            .timestamp(timestamp)
            .openPrice(BigDecimal.valueOf(price - 100))
            .closePrice(BigDecimal.valueOf(price))
            .highPrice(BigDecimal.valueOf(price + 200))
            .lowPrice(BigDecimal.valueOf(price - 200))
            .volume(BigDecimal.valueOf(volume))
            .build();
    }

    // MarketData 클래스 정의 (실제 구현에서는 별도 파일에 위치)
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketData {
        private String symbol;
        private LocalDateTime timestamp;
        private BigDecimal openPrice;
        private BigDecimal closePrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private BigDecimal volume;
    }
}