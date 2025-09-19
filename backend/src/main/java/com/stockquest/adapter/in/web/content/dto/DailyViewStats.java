package com.stockquest.adapter.in.web.content.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyViewStats(
    LocalDate date,
    Long views
) {
}