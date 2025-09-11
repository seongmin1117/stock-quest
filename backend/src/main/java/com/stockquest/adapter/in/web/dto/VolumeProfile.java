package com.stockquest.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class VolumeProfile {
    private Map<Double, Long> priceVolumeMap;
    private double volumeWeightedAveragePrice;
    private double pointOfControl;
    private double valueAreaHigh;
    private double valueAreaLow;
    private List<VolumeNode> volumeNodes;
    
    @Data
    @Builder
    public static class VolumeNode {
        private double price;
        private long volume;
        private double percentage;
    }
}