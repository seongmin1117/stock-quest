package com.stockquest.application.session;

import com.stockquest.application.session.port.in.CloseChallengeUseCase;
import com.stockquest.domain.challenge.ChallengeInstrument;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.ChallengeSession.SessionStatus;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 챌린지 종료 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CloseChallengeService implements CloseChallengeUseCase {
    
    private final ChallengeSessionRepository sessionRepository;
    private final PortfolioRepository portfolioRepository;
    private final ChallengeRepository challengeRepository;
    
    @Override
    public CloseChallengeResult close(CloseChallengeCommand command) {
        log.info("챌린지 세션 종료 시작: 세션={}", command.sessionId());
        
        // 1. 세션 유효성 확인
        var session = sessionRepository.findById(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + command.sessionId()));
            
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 세션만 종료할 수 있습니다");
        }
        
        // 2. 챌린지 정보 조회
        var challenge = challengeRepository.findById(session.getChallengeId())
            .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + session.getChallengeId()));
        
        // 3. 현재 포트폴리오 포지션 조회
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(command.sessionId());
        
        // 4. 포트폴리오 총 평가금액 계산 (Mock 시장가 사용)
        BigDecimal totalPortfolioValue = calculateTotalPortfolioValue(positions);
        
        // 5. 최종 자산가치 및 수익률 계산
        BigDecimal finalBalance = session.getCurrentBalance().add(totalPortfolioValue);
        BigDecimal totalPnL = session.calculateTotalPnL(totalPortfolioValue);
        BigDecimal returnPercentage = session.calculateReturnPercentage(totalPortfolioValue);
        
        // 6. 세션 종료
        session.end(); // 상태를 ENDED로 변경하고 completedAt 설정
        sessionRepository.save(session);
        
        // 7. 랭킹 계산 (현재는 임시로 1위 반환, 실제로는 다른 참가자와 비교)
        Integer rank = calculateRank(session.getChallengeId(), returnPercentage);
        
        // 8. 상품명 공개 정보 생성
        List<RevealedInstrument> revealedInstruments = createRevealedInstruments(challenge, positions);
        
        log.info("챌린지 세션 종료 완료: 세션={}, 최종잔고={}, 수익률={}%, 순위={}", 
            command.sessionId(), finalBalance, returnPercentage, rank);
        
        return new CloseChallengeResult(
            command.sessionId(),
            finalBalance,
            totalPnL,
            returnPercentage,
            rank,
            revealedInstruments
        );
    }
    
    /**
     * 포트폴리오 총 평가금액 계산
     */
    private BigDecimal calculateTotalPortfolioValue(List<PortfolioPosition> positions) {
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (PortfolioPosition position : positions) {
            if (position.hasPosition()) {
                BigDecimal currentPrice = getMockCurrentPrice(position.getInstrumentKey());
                BigDecimal positionValue = position.calculateCurrentValue(currentPrice);
                totalValue = totalValue.add(positionValue);
            }
        }
        
        return totalValue;
    }
    
    /**
     * Mock 현재가 조회
     */
    private BigDecimal getMockCurrentPrice(String instrumentKey) {
        // 실제로는 MarketDataRepository에서 최신 가격 조회
        return switch (instrumentKey) {
            case "A" -> new BigDecimal("155.50");  // Apple 현재가
            case "B" -> new BigDecimal("355.20");  // Microsoft 현재가  
            case "C" -> new BigDecimal("2850.00"); // Google 현재가
            default -> new BigDecimal("105.00");
        };
    }
    
    /**
     * 랭킹 계산 - 현재는 Mock 구현
     */
    private Integer calculateRank(Long challengeId, BigDecimal returnPercentage) {
        // 실제로는 같은 챌린지의 모든 완료된 세션과 비교하여 순위 계산
        // 현재는 간단히 수익률에 따라 임시 순위 반환
        if (returnPercentage.compareTo(BigDecimal.TEN) > 0) {
            return 1; // 10% 이상 수익
        } else if (returnPercentage.compareTo(BigDecimal.ZERO) > 0) {
            return 2; // 플러스 수익
        } else {
            return 3; // 마이너스 수익
        }
    }
    
    /**
     * 상품명 공개 정보 생성
     */
    private List<RevealedInstrument> createRevealedInstruments(
            com.stockquest.domain.challenge.Challenge challenge, 
            List<PortfolioPosition> positions) {
        
        // 챌린지의 모든 상품 정보를 Map으로 변환
        Map<String, ChallengeInstrument> instrumentMap = challenge.getInstruments()
            .stream()
            .collect(Collectors.toMap(
                ChallengeInstrument::getInstrumentKey,
                instrument -> instrument
            ));
        
        // Mock 데이터로 상품명 매핑 (실제로는 DB에서 조회)
        Map<String, String[]> mockTickerMapping = createMockTickerMapping();
        
        return challenge.getInstruments().stream()
            .map(instrument -> {
                String[] tickerInfo = mockTickerMapping.get(instrument.getInstrumentKey());
                return new RevealedInstrument(
                    instrument.getInstrumentKey(),
                    tickerInfo[0], // ticker
                    tickerInfo[1]  // name
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Mock 티커 매핑 데이터
     */
    private Map<String, String[]> createMockTickerMapping() {
        Map<String, String[]> mapping = new HashMap<>();
        mapping.put("A", new String[]{"AAPL", "Apple Inc."});
        mapping.put("B", new String[]{"MSFT", "Microsoft Corporation"});
        mapping.put("C", new String[]{"GOOGL", "Alphabet Inc."});
        return mapping;
    }
}