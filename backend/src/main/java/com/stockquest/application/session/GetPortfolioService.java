package com.stockquest.application.session;

import com.stockquest.application.session.dto.GetPortfolioQuery;
import com.stockquest.application.session.dto.GetPortfolioResult;
import com.stockquest.application.session.port.in.GetPortfolioUseCase;
import com.stockquest.domain.portfolio.PortfolioPosition;
import com.stockquest.domain.portfolio.port.PortfolioRepository;
import com.stockquest.domain.session.ChallengeSession;
import com.stockquest.domain.session.port.ChallengeSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 포트폴리오 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPortfolioService implements GetPortfolioUseCase {

    private final ChallengeSessionRepository sessionRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    public GetPortfolioResult getPortfolio(GetPortfolioQuery query) {
        // 세션 조회 및 권한 확인
        ChallengeSession session = sessionRepository.findById(query.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + query.sessionId()));

        if (!session.getUserId().equals(query.userId())) {
            throw new IllegalArgumentException("세션에 대한 접근 권한이 없습니다.");
        }

        // 포트폴리오 포지션 조회
        List<PortfolioPosition> positions = portfolioRepository.findBySessionId(query.sessionId());

        // TODO: 실제로는 시장 가격을 가져와서 계산해야 하지만, 일단 기본값으로 처리
        // 총 가치 계산 (현재는 totalCost를 사용, 실제로는 현재 시장가 * 수량으로 계산해야 함)
        BigDecimal totalValue = positions.stream()
                .map(position -> position.getTotalCost()) // 임시로 매입 비용을 사용
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 현재 현금 잔고 (세션에서 가져옴)
        BigDecimal cashBalance = session.getCurrentBalance();

        // 총 포트폴리오 가치 = 포지션 가치 + 현금 잔고
        BigDecimal totalPortfolioValue = totalValue.add(cashBalance);

        return new GetPortfolioResult(
                session.getId(),
                totalPortfolioValue,
                cashBalance,
                positions
        );
    }
}