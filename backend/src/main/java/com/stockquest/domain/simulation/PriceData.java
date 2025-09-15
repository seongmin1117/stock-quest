package com.stockquest.domain.simulation;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주가 데이터 도메인 객체
 * 특정 시점의 주식 가격 정보를 나타냄
 */
@Getter
public class PriceData {

    private final LocalDateTime date;
    private final BigDecimal price;

    public PriceData(LocalDateTime date, BigDecimal price) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }

        this.date = date;
        this.price = price;
    }
}