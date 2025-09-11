package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MACDResult {
    private double macdLine;
    private double signalLine;
    private double histogram;
    private boolean bullishSignal;
    private boolean bearishSignal;
}