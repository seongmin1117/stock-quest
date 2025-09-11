package com.stockquest.application.admin.challenge;

import com.stockquest.domain.challenge.*;
import com.stockquest.domain.challenge.port.ChallengeTemplateRepository;
import com.stockquest.domain.challenge.port.ChallengeCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * 챌린지 템플릿 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChallengeTemplateService {
    
    private final ChallengeTemplateRepository templateRepository;
    private final ChallengeCategoryRepository categoryRepository;
    
    /**
     * 기본 챌린지 템플릿들을 생성
     */
    public void createDefaultTemplates() {
        log.info("Creating default challenge templates");
        
        // 카테고리별로 기본 템플릿 생성
        createMarketCrashTemplates();
        createBullMarketTemplates();
        createSectorRotationTemplates();
        createVolatilityTemplates();
        createESGTemplates();
        createInternationalTemplates();
        createRiskManagementTemplates();
        
        log.info("Default challenge templates created successfully");
    }
    
    private void createMarketCrashTemplates() {
        Long categoryId = getCategoryIdByName("Market Crash Scenarios");
        if (categoryId == null) return;
        
        // 2008 금융위기 템플릿
        ChallengeTemplate financialCrisis = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("2008 금융위기 생존하기")
                .description("2008년 글로벌 금융위기 상황에서 포트폴리오를 방어하고 회복하는 전략을 학습하세요.")
                .difficulty(ChallengeDifficulty.ADVANCED)
                .templateType(ChallengeType.MARKET_CRASH)
                .config(createFinancialCrisisConfig())
                .tags(Arrays.asList("금융위기", "리스크관리", "방어투자", "경기침체"))
                .successCriteria(createCrashSuccessCriteria())
                .marketScenario(createFinancialCrisisScenario())
                .learningObjectives("극심한 시장 급락 상황에서의 리스크 관리와 손실 최소화 전략 습득")
                .estimatedDurationMinutes(45)
                .initialBalance(new BigDecimal("100000"))
                .speedFactor(20)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(financialCrisis);
        
        // COVID-19 시장 급락 템플릿
        ChallengeTemplate covidCrash = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("COVID-19 시장 급락과 회복")
                .description("2020년 코로나19로 인한 시장 급락과 빠른 회복 과정을 경험하세요.")
                .difficulty(ChallengeDifficulty.INTERMEDIATE)
                .templateType(ChallengeType.MARKET_CRASH)
                .config(createCovidCrashConfig())
                .tags(Arrays.asList("코로나19", "팬데믹", "V자회복", "기술주"))
                .successCriteria(createCrashSuccessCriteria())
                .marketScenario(createCovidScenario())
                .learningObjectives("예측 불가능한 외부 충격에 대한 대응 전략과 빠른 회복기 투자 기회 포착")
                .estimatedDurationMinutes(30)
                .initialBalance(new BigDecimal("100000"))
                .speedFactor(30)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(covidCrash);
    }
    
    private void createBullMarketTemplates() {
        Long categoryId = getCategoryIdByName("Bull Market Strategies");
        if (categoryId == null) return;
        
        // 1990년대 기술주 상승장 템플릿
        ChallengeTemplate techBoom = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("90년대 기술주 열풍")
                .description("1990년대 인터넷 혁명과 함께한 기술주 상승장을 경험하세요.")
                .difficulty(ChallengeDifficulty.INTERMEDIATE)
                .templateType(ChallengeType.BULL_MARKET)
                .config(createTechBoomConfig())
                .tags(Arrays.asList("기술주", "인터넷혁명", "성장투자", "90년대"))
                .successCriteria(createBullMarketSuccessCriteria())
                .marketScenario(createTechBoomScenario())
                .learningObjectives("성장주 투자의 기본 원칙과 버블 위험 인식 능력 개발")
                .estimatedDurationMinutes(40)
                .initialBalance(new BigDecimal("50000"))
                .speedFactor(15)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(techBoom);
    }
    
    private void createSectorRotationTemplates() {
        Long categoryId = getCategoryIdByName("Sector Rotation");
        if (categoryId == null) return;
        
        // 경기순환 섹터 로테이션 템플릿
        ChallengeTemplate sectorRotation = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("경기순환 섹터 로테이션")
                .description("경기 사이클에 따른 섹터별 투자 기회를 포착하는 전략을 배우세요.")
                .difficulty(ChallengeDifficulty.ADVANCED)
                .templateType(ChallengeType.SECTOR_ROTATION)
                .config(createSectorRotationConfig())
                .tags(Arrays.asList("섹터로테이션", "경기순환", "업종분석", "자산배분"))
                .successCriteria(createSectorRotationSuccessCriteria())
                .marketScenario(createSectorRotationScenario())
                .learningObjectives("경기 사이클 분석을 통한 섹터별 투자 타이밍 포착 능력 개발")
                .estimatedDurationMinutes(50)
                .initialBalance(new BigDecimal("150000"))
                .speedFactor(12)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(sectorRotation);
    }
    
    private void createVolatilityTemplates() {
        Long categoryId = getCategoryIdByName("High Volatility Trading");
        if (categoryId == null) return;
        
        // 고변동성 시장 템플릿
        ChallengeTemplate volatilityTrading = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("극한 변동성 시장 대응")
                .description("일일 등락률이 5% 이상인 극한 변동성 시장에서의 거래 전략을 마스터하세요.")
                .difficulty(ChallengeDifficulty.EXPERT)
                .templateType(ChallengeType.VOLATILITY)
                .config(createVolatilityConfig())
                .tags(Arrays.asList("고변동성", "단기매매", "리스크관리", "심리전"))
                .successCriteria(createVolatilitySuccessCriteria())
                .marketScenario(createVolatilityScenario())
                .learningObjectives("극한 변동성 상황에서의 감정 관리와 단기 거래 기법 습득")
                .estimatedDurationMinutes(25)
                .initialBalance(new BigDecimal("80000"))
                .speedFactor(40)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(volatilityTrading);
    }
    
    private void createESGTemplates() {
        Long categoryId = getCategoryIdByName("ESG Investing");
        if (categoryId == null) return;
        
        // ESG 투자 템플릿
        ChallengeTemplate esgInvesting = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("지속가능한 ESG 투자")
                .description("환경, 사회, 지배구조를 고려한 지속가능한 투자 전략을 학습하세요.")
                .difficulty(ChallengeDifficulty.INTERMEDIATE)
                .templateType(ChallengeType.ESG)
                .config(createESGConfig())
                .tags(Arrays.asList("ESG", "지속가능투자", "친환경", "사회책임"))
                .successCriteria(createESGSuccessCriteria())
                .marketScenario(createESGScenario())
                .learningObjectives("ESG 기준을 적용한 투자 분석 능력과 장기 가치 투자 철학 개발")
                .estimatedDurationMinutes(35)
                .initialBalance(new BigDecimal("120000"))
                .speedFactor(18)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(esgInvesting);
    }
    
    private void createInternationalTemplates() {
        Long categoryId = getCategoryIdByName("International Markets");
        if (categoryId == null) return;
        
        // 신흥시장 투자 템플릿
        ChallengeTemplate emergingMarkets = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("신흥시장 투자의 기회와 위험")
                .description("높은 성장 잠재력과 리스크를 동시에 가진 신흥시장 투자를 경험하세요.")
                .difficulty(ChallengeDifficulty.ADVANCED)
                .templateType(ChallengeType.INTERNATIONAL)
                .config(createEmergingMarketsConfig())
                .tags(Arrays.asList("신흥시장", "글로벌투자", "환율리스크", "성장기회"))
                .successCriteria(createInternationalSuccessCriteria())
                .marketScenario(createEmergingMarketsScenario())
                .learningObjectives("글로벌 투자의 다각화 효과와 신흥시장 특유의 리스크 관리 방법 습득")
                .estimatedDurationMinutes(40)
                .initialBalance(new BigDecimal("100000"))
                .speedFactor(25)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(emergingMarkets);
    }
    
    private void createRiskManagementTemplates() {
        Long categoryId = getCategoryIdByName("Risk Management");
        if (categoryId == null) return;
        
        // 리스크 관리 템플릿
        ChallengeTemplate riskManagement = ChallengeTemplate.builder()
                .categoryId(categoryId)
                .name("디펜시브 포트폴리오 구축")
                .description("불확실한 시장에서 자본을 보존하고 안정적인 수익을 추구하는 전략을 배우세요.")
                .difficulty(ChallengeDifficulty.BEGINNER)
                .templateType(ChallengeType.RISK_MANAGEMENT)
                .config(createRiskManagementConfig())
                .tags(Arrays.asList("리스크관리", "자본보존", "안정투자", "분산투자"))
                .successCriteria(createRiskManagementSuccessCriteria())
                .marketScenario(createRiskManagementScenario())
                .learningObjectives("리스크 관리의 기본 원칙과 안정적인 포트폴리오 구성 방법 습득")
                .estimatedDurationMinutes(30)
                .initialBalance(new BigDecimal("200000"))
                .speedFactor(10)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        templateRepository.save(riskManagement);
    }
    
    private Long getCategoryIdByName(String name) {
        return categoryRepository.findByName(name)
                .map(ChallengeCategory::getId)
                .orElse(null);
    }
    
    // 설정 생성 메서드들
    private Map<String, Object> createFinancialCrisisConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "SPY", "hidden", "대형주 ETF", "actual", "S&P 500 ETF", "type", "STOCK"),
                Map.of("key", "B", "ticker", "XLF", "hidden", "금융 ETF", "actual", "Financial Select SPDR", "type", "STOCK"),
                Map.of("key", "C", "ticker", "GLD", "hidden", "금 ETF", "actual", "SPDR Gold Trust", "type", "STOCK"),
                Map.of("key", "D", "ticker", "TLT", "hidden", "장기채권 ETF", "actual", "20+ Year Treasury Bond", "type", "BOND"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.001); // 0.1%
        return config;
    }
    
    private Map<String, Object> createCovidCrashConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "QQQ", "hidden", "기술주 ETF", "actual", "Nasdaq 100 ETF", "type", "STOCK"),
                Map.of("key", "B", "ticker", "ZOOM", "hidden", "화상회의 주식", "actual", "Zoom Technologies", "type", "STOCK"),
                Map.of("key", "C", "ticker", "AMZN", "hidden", "전자상거래 주식", "actual", "Amazon", "type", "STOCK"),
                Map.of("key", "D", "ticker", "CCL", "hidden", "여행업 주식", "actual", "Carnival Corp", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.0025); // 0.25%
        return config;
    }
    
    private Map<String, Object> createTechBoomConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "MSFT", "hidden", "소프트웨어 회사", "actual", "Microsoft", "type", "STOCK"),
                Map.of("key", "B", "ticker", "INTC", "hidden", "반도체 회사", "actual", "Intel", "type", "STOCK"),
                Map.of("key", "C", "ticker", "CSCO", "hidden", "네트워크 장비", "actual", "Cisco Systems", "type", "STOCK"),
                Map.of("key", "D", "ticker", "AMZN", "hidden", "온라인 서점", "actual", "Amazon", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.002); // 0.2%
        return config;
    }
    
    private Map<String, Object> createSectorRotationConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "XLI", "hidden", "산업재 ETF", "actual", "Industrial Select SPDR", "type", "STOCK"),
                Map.of("key", "B", "ticker", "XLF", "hidden", "금융 ETF", "actual", "Financial Select SPDR", "type", "STOCK"),
                Map.of("key", "C", "ticker", "XLE", "hidden", "에너지 ETF", "actual", "Energy Select SPDR", "type", "STOCK"),
                Map.of("key", "D", "ticker", "XLK", "hidden", "기술 ETF", "actual", "Technology Select SPDR", "type", "STOCK"),
                Map.of("key", "E", "ticker", "XLP", "hidden", "필수소비재 ETF", "actual", "Consumer Staples SPDR", "type", "STOCK"),
                Map.of("key", "F", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.0015); // 0.15%
        return config;
    }
    
    private Map<String, Object> createVolatilityConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "TSLA", "hidden", "전기차 회사", "actual", "Tesla", "type", "STOCK"),
                Map.of("key", "B", "ticker", "GME", "hidden", "게임 소매업", "actual", "GameStop", "type", "STOCK"),
                Map.of("key", "C", "ticker", "MEME", "hidden", "밈 주식", "actual", "Meme ETF", "type", "STOCK"),
                Map.of("key", "D", "ticker", "VIX", "hidden", "변동성 지수", "actual", "VIX ETF", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", true);
        config.put("transactionCost", 0.005); // 0.5%
        return config;
    }
    
    private Map<String, Object> createESGConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "ESG", "hidden", "ESG ETF", "actual", "iShares MSCI KLD 400 Social ETF", "type", "STOCK"),
                Map.of("key", "B", "ticker", "ICLN", "hidden", "청정에너지 ETF", "actual", "iShares Clean Energy ETF", "type", "STOCK"),
                Map.of("key", "C", "ticker", "TSLA", "hidden", "전기차 회사", "actual", "Tesla", "type", "STOCK"),
                Map.of("key", "D", "ticker", "NEE", "hidden", "재생에너지 회사", "actual", "NextEra Energy", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.002); // 0.2%
        config.put("esgScoring", true);
        return config;
    }
    
    private Map<String, Object> createEmergingMarketsConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "EEM", "hidden", "신흥시장 ETF", "actual", "iShares MSCI Emerging Markets", "type", "STOCK"),
                Map.of("key", "B", "ticker", "VWO", "hidden", "신흥시장 ETF2", "actual", "Vanguard Emerging Markets", "type", "STOCK"),
                Map.of("key", "C", "ticker", "FXI", "hidden", "중국 ETF", "actual", "iShares China Large-Cap", "type", "STOCK"),
                Map.of("key", "D", "ticker", "EWZ", "hidden", "브라질 ETF", "actual", "iShares MSCI Brazil", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.003); // 0.3%
        config.put("currencyRisk", true);
        return config;
    }
    
    private Map<String, Object> createRiskManagementConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("instruments", Arrays.asList(
                Map.of("key", "A", "ticker", "SPY", "hidden", "대형주 ETF", "actual", "S&P 500 ETF", "type", "STOCK"),
                Map.of("key", "B", "ticker", "BND", "hidden", "채권 ETF", "actual", "Vanguard Total Bond Market", "type", "BOND"),
                Map.of("key", "C", "ticker", "VNQ", "hidden", "부동산 ETF", "actual", "Vanguard Real Estate", "type", "STOCK"),
                Map.of("key", "D", "ticker", "GLD", "hidden", "금 ETF", "actual", "SPDR Gold Trust", "type", "STOCK"),
                Map.of("key", "E", "ticker", "CASH", "hidden", "현금", "actual", "예금", "type", "DEPOSIT")
        ));
        config.put("allowShortSelling", false);
        config.put("transactionCost", 0.001); // 0.1%
        config.put("riskManagementMode", true);
        return config;
    }
    
    // 성공 기준 생성 메서드들
    private Map<String, Object> createCrashSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.0);  // 손실 방지가 목표
        criteria.put("maxDrawdown", 0.2);   // 최대 20% 손실
        criteria.put("bonusObjectives", Arrays.asList(
                "시장 대비 초과 수익 달성",
                "변동성 최소화",
                "빠른 회복력 보여주기"
        ));
        return criteria;
    }
    
    private Map<String, Object> createBullMarketSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.3);  // 30% 수익 목표
        criteria.put("maxDrawdown", 0.15);  // 최대 15% 손실
        criteria.put("bonusObjectives", Arrays.asList(
                "시장 수익률의 1.5배 달성",
                "성장주 포트폴리오 구성",
                "적절한 익절 타이밍"
        ));
        return criteria;
    }
    
    private Map<String, Object> createSectorRotationSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.25); // 25% 수익 목표
        criteria.put("maxDrawdown", 0.1);   // 최대 10% 손실
        criteria.put("sectorDiversification", true);
        criteria.put("bonusObjectives", Arrays.asList(
                "최소 4개 섹터에 분산 투자",
                "경기 사이클에 맞는 섹터 선택",
                "적절한 리밸런싱"
        ));
        return criteria;
    }
    
    private Map<String, Object> createVolatilitySuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.2);  // 20% 수익 목표 (고위험 대비)
        criteria.put("maxDrawdown", 0.25);  // 최대 25% 손실 허용
        criteria.put("volatilityManagement", true);
        criteria.put("bonusObjectives", Arrays.asList(
                "변동성 활용한 수익 창출",
                "감정적 거래 자제",
                "리스크 관리 철저"
        ));
        return criteria;
    }
    
    private Map<String, Object> createESGSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.15); // 15% 수익 목표
        criteria.put("maxDrawdown", 0.12);  // 최대 12% 손실
        criteria.put("esgCompliance", true);
        criteria.put("bonusObjectives", Arrays.asList(
                "ESG 기준 만족하는 투자",
                "지속가능성 고려",
                "장기 가치 투자"
        ));
        return criteria;
    }
    
    private Map<String, Object> createInternationalSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.18); // 18% 수익 목표
        criteria.put("maxDrawdown", 0.2);   // 최대 20% 손실
        criteria.put("geographicDiversification", true);
        criteria.put("bonusObjectives", Arrays.asList(
                "환율 리스크 관리",
                "지역별 분산 투자",
                "신흥시장 기회 포착"
        ));
        return criteria;
    }
    
    private Map<String, Object> createRiskManagementSuccessCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("targetReturn", 0.08); // 8% 안정적 수익 목표
        criteria.put("maxDrawdown", 0.05);  // 최대 5% 손실
        criteria.put("stabilityFocus", true);
        criteria.put("bonusObjectives", Arrays.asList(
                "변동성 최소화",
                "자산 배분 최적화",
                "꾸준한 수익 창출"
        ));
        return criteria;
    }
    
    // 시장 시나리오 생성 메서드들 (간단한 예시)
    private Map<String, Object> createFinancialCrisisScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "2007-2009 금융위기");
        scenario.put("volatility", 0.8);
        scenario.put("trend", "BEAR");
        scenario.put("keyEvents", Arrays.asList("리먼 브라더스 파산", "베어스턴스 구제금융", "TARP 구제금융"));
        return scenario;
    }
    
    private Map<String, Object> createCovidScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "2020 코로나19 팬데믹");
        scenario.put("volatility", 0.9);
        scenario.put("trend", "V_SHAPED_RECOVERY");
        scenario.put("keyEvents", Arrays.asList("팬데믹 선언", "봉쇄 조치", "백신 개발", "경기부양책"));
        return scenario;
    }
    
    private Map<String, Object> createTechBoomScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "1990년대 기술주 붐");
        scenario.put("volatility", 0.6);
        scenario.put("trend", "BULL");
        scenario.put("keyEvents", Arrays.asList("인터넷 상용화", "닷컴 기업 상장", "Y2K 준비", "기술혁신"));
        return scenario;
    }
    
    private Map<String, Object> createSectorRotationScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "경기순환 전체 사이클");
        scenario.put("volatility", 0.4);
        scenario.put("trend", "CYCLICAL");
        scenario.put("keyEvents", Arrays.asList("경기확장", "정점", "수축", "저점"));
        return scenario;
    }
    
    private Map<String, Object> createVolatilityScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "고변동성 시장");
        scenario.put("volatility", 1.0);
        scenario.put("trend", "VOLATILE");
        scenario.put("keyEvents", Arrays.asList("밈주식 열풍", "소셜미디어 영향", "개인투자자 급증"));
        return scenario;
    }
    
    private Map<String, Object> createESGScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "ESG 투자 붐");
        scenario.put("volatility", 0.3);
        scenario.put("trend", "SUSTAINABLE_GROWTH");
        scenario.put("keyEvents", Arrays.asList("파리기후협약", "ESG 공시 의무화", "탄소중립 선언"));
        return scenario;
    }
    
    private Map<String, Object> createEmergingMarketsScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "신흥시장 성장기");
        scenario.put("volatility", 0.7);
        scenario.put("trend", "EMERGING_GROWTH");
        scenario.put("keyEvents", Arrays.asList("경제개방", "외국인투자 유입", "통화위기 극복"));
        return scenario;
    }
    
    private Map<String, Object> createRiskManagementScenario() {
        Map<String, Object> scenario = new HashMap<>();
        scenario.put("period", "불확실성 시대");
        scenario.put("volatility", 0.5);
        scenario.put("trend", "SIDEWAYS");
        scenario.put("keyEvents", Arrays.asList("지정학적 긴장", "인플레이션 우려", "금리 변동성"));
        return scenario;
    }
}