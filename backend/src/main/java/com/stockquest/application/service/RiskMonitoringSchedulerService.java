package com.stockquest.application.service;

import com.stockquest.adapter.in.websocket.RiskMonitoringWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 리스크 모니터링 스케줄러 서비스
 * WebSocket 컨트롤러의 스케줄링 로직을 분리하여 AOP 프록시 문제 해결
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskMonitoringSchedulerService {
    
    private final RiskMonitoringWebSocketController riskMonitoringWebSocketController;
    
    /**
     * 주기적으로 리스크 상태 업데이트 브로드캐스트 (5분마다)
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void scheduleRiskStatusUpdate() {
        try {
            riskMonitoringWebSocketController.broadcastRiskStatusUpdate();
            log.debug("리스크 상태 업데이트 스케줄링 완료");
        } catch (Exception e) {
            log.error("리스크 상태 업데이트 스케줄링 오류", e);
        }
    }
}