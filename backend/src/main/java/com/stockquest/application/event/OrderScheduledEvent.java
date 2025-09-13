package com.stockquest.application.event;

import com.stockquest.domain.execution.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 주문 실행 스케줄링 이벤트
 */
@Getter
public class OrderScheduledEvent extends ApplicationEvent {

    private final Order order;

    public OrderScheduledEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}