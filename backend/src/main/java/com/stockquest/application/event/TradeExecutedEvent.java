package com.stockquest.application.event;

import com.stockquest.domain.execution.Trade;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 거래 체결 이벤트
 */
@Getter
public class TradeExecutedEvent extends ApplicationEvent {

    private final String orderId;
    private final Trade trade;

    public TradeExecutedEvent(Object source, String orderId, Trade trade) {
        super(source);
        this.orderId = orderId;
        this.trade = trade;
    }
}