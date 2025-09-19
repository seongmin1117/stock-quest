package com.stockquest.adapter.in.web.dashboard;

import com.stockquest.application.dashboard.DashboardService;
import com.stockquest.application.dashboard.dto.DashboardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 대시보드 웹 어댑터
 * 사용자 대시보드 API 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardWebAdapter {

    private final DashboardService dashboardService;

    /**
     * 사용자 대시보드 데이터 조회
     * GET /api/dashboard
     */
    @GetMapping
    public ResponseEntity<DashboardData> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("대시보드 데이터 요청 수신");

        try {
            // 현재 사용자 ID 추출
            Long userId = Long.parseLong(userDetails.getUsername());
            log.info("대시보드 데이터 조회: userId={}", userId);

            // 대시보드 데이터 조회
            DashboardData dashboardData = dashboardService.getDashboardData(userId);

            log.info("대시보드 데이터 응답 완료: userId={}", userId);
            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            log.error("대시보드 데이터 조회 중 오류 발생", e);
            throw e;
        }
    }
}