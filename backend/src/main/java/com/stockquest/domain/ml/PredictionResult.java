package com.stockquest.domain.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ML 모델 예측 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {
    
    /**
     * 예측 값 (-1.0 ~ 1.0)
     * -1.0: 강한 매도 신호
     *  0.0: 중립
     *  1.0: 강한 매수 신호
     */
    private double prediction;
    
    /**
     * 예측 신뢰도 (0.0 ~ 1.0)
     */
    private double confidence;
    
    /**
     * 시그널 강도 (0.0 ~ 1.0)
     */
    private double strength;
    
    /**
     * 예측 이유/근거
     */
    private String reason;
    
    /**
     * 추가 메타데이터
     */
    private String metadata;
    
    /**
     * 강한 매수 신호인지 확인
     */
    public boolean isStrongBuy() {
        return prediction > 0.6 && confidence > 0.7;
    }
    
    /**
     * 매수 신호인지 확인
     */
    public boolean isBuy() {
        return prediction > 0.3 && confidence > 0.5;
    }
    
    /**
     * 중립인지 확인
     */
    public boolean isNeutral() {
        return Math.abs(prediction) <= 0.3;
    }
    
    /**
     * 매도 신호인지 확인
     */
    public boolean isSell() {
        return prediction < -0.3 && confidence > 0.5;
    }
    
    /**
     * 강한 매도 신호인지 확인
     */
    public boolean isStrongSell() {
        return prediction < -0.6 && confidence > 0.7;
    }
}