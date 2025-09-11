package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetAllocationResponse {
    private String assetClass;
    private String sector;
    private double weight;
    private double value;
    private int positionCount;
    private double averageWeight;
}