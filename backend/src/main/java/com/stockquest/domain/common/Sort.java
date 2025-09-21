package com.stockquest.domain.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 헥사고날 아키텍처를 위한 프레임워크 독립적인 정렬 조건
 * Spring Data의 Sort를 대체하는 도메인 객체
 */
public class Sort {

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = Collections.unmodifiableList(orders);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public boolean isSorted() {
        return !orders.isEmpty();
    }

    public boolean isUnsorted() {
        return orders.isEmpty();
    }

    public static Sort unsorted() {
        return new Sort(Collections.emptyList());
    }

    public static Sort by(String... properties) {
        return by(Direction.ASC, properties);
    }

    public static Sort by(Direction direction, String... properties) {
        List<Order> orders = new ArrayList<>();
        for (String property : properties) {
            orders.add(new Order(direction, property));
        }
        return new Sort(orders);
    }

    public static Sort by(Order... orders) {
        return new Sort(Arrays.asList(orders));
    }

    public Sort and(Sort sort) {
        List<Order> newOrders = new ArrayList<>(this.orders);
        newOrders.addAll(sort.orders);
        return new Sort(newOrders);
    }

    /**
     * 정렬 방향
     */
    public enum Direction {
        ASC, DESC
    }

    /**
     * 정렬 조건
     */
    public static class Order {
        private final Direction direction;
        private final String property;

        public Order(Direction direction, String property) {
            if (direction == null) {
                throw new IllegalArgumentException("정렬 방향은 필수입니다");
            }
            if (property == null || property.trim().isEmpty()) {
                throw new IllegalArgumentException("정렬 필드는 필수입니다");
            }

            this.direction = direction;
            this.property = property.trim();
        }

        public Direction getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }

        public boolean isAscending() {
            return Direction.ASC.equals(direction);
        }

        public boolean isDescending() {
            return Direction.DESC.equals(direction);
        }

        public static Order asc(String property) {
            return new Order(Direction.ASC, property);
        }

        public static Order desc(String property) {
            return new Order(Direction.DESC, property);
        }
    }
}