package com.stockquest.domain.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 도메인 엔터티 (임시 구현)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private BigDecimal totalValue;
    private List<Position> positions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 포트폴리오의 심볼 목록 반환
     */
    public List<String> getSymbols() {
        if (positions == null) {
            return List.of();
        }
        return positions.stream()
                .map(Position::getSymbol)
                .toList();
    }
    
    /**
     * 총 포지션 수 반환
     */
    public int getPositionCount() {
        return positions != null ? positions.size() : 0;
    }
    
    /**
     * 특정 심볼의 포지션 반환
     */
    public Position getPosition(String symbol) {
        if (positions == null) {
            return null;
        }
        return positions.stream()
                .filter(pos -> symbol.equals(pos.getSymbol()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 포트폴리오 가중치 계산
     */
    public Map<String, BigDecimal> getWeights() {
        if (positions == null || totalValue == null || totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return Map.of();
        }
        
        return positions.stream()
                .collect(java.util.stream.Collectors.toMap(
                    Position::getSymbol,
                    pos -> pos.getValue().divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                ));
    }
    
    /**
     * 포트폴리오의 포지션 목록 반환 (holdings 메서드 별칭)
     */
    public List<Position> getHoldings() {
        return positions != null ? positions : List.of();
    }
}