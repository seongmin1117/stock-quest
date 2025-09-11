package com.stockquest.application.session;

import com.stockquest.application.session.dto.GetSessionDetailQuery;
import com.stockquest.application.session.dto.GetSessionDetailResult;
import com.stockquest.application.session.port.in.GetSessionDetailUseCase;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;
import com.stockquest.domain.order.Order;
import com.stockquest.domain.order.port.OrderRepository;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 세션 상세 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSessionDetailService implements GetSessionDetailUseCase {
    
    private final ChallengeSessionRepository sessionRepository;
    private final ChallengeRepository challengeRepository;
    private final PortfolioRepository portfolioRepository;
    private final OrderRepository orderRepository;
    
    @Override
    public GetSessionDetailResult getSessionDetail(GetSessionDetailQuery query) {
        // 세션 조회 및 권한 확인
        ChallengeSession session = sessionRepository.findById(query.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + query.sessionId()));
        
        if (!session.getUserId().equals(query.userId())) {
            throw new IllegalArgumentException("세션에 대한 접근 권한이 없습니다.");
        }
        
        // 챌린지 정보 조회
        Challenge challenge = challengeRepository.findById(session.getChallengeId())
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다: " + session.getChallengeId()));
        
        // 포트폴리오 조회
        List<PortfolioPosition> portfolio = portfolioRepository.findBySessionId(query.sessionId());
        
        // 주문 내역 조회
        List<Order> orders = orderRepository.findBySessionId(query.sessionId());
        
        return new GetSessionDetailResult(
                session,
                challenge.getTitle(),
                portfolio,
                orders
        );
    }
}