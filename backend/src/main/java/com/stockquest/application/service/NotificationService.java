package com.stockquest.application.service;

import com.stockquest.domain.risk.RiskAlert;
import com.stockquest.domain.risk.RiskAlert.AlertConfiguration.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 알림 전송 서비스
 * Phase 8.3: Advanced Risk Management - 멀티채널 알림 전송 시스템
 */
@Slf4j
@Service
public class NotificationService {
    
    // 전송 실패 재시도 카운터
    private final Map<String, Integer> retryCounters = new ConcurrentHashMap<>();
    private final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * 알림 전송 (모든 설정된 채널)
     */
    public CompletableFuture<Void> sendNotification(RiskAlert alert, NotificationChannel channel) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending {} notification for alert: {} [{}]", 
                    channel, alert.getAlertId(), alert.getSeverity());
                
                switch (channel) {
                    case EMAIL -> sendEmailNotification(alert);
                    case SMS -> sendSmsNotification(alert);
                    case SLACK -> sendSlackNotification(alert);
                    case TEAMS -> sendTeamsNotification(alert);
                    case WEBHOOK -> sendWebhookNotification(alert);
                    case IN_APP -> sendInAppNotification(alert);
                    case MOBILE_PUSH -> sendMobilePushNotification(alert);
                }
                
                log.debug("Successfully sent {} notification for alert: {}", channel, alert.getAlertId());
                
            } catch (Exception e) {
                log.error("Failed to send {} notification for alert: {}", channel, alert.getAlertId(), e);
                handleNotificationFailure(alert, channel, e);
            }
        });
    }
    
    /**
     * 에스컬레이션 알림 전송
     */
    public CompletableFuture<Void> sendEscalationNotification(RiskAlert alert, String recipient) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.warn("Sending escalation notification for alert: {} to recipient: {}", 
                    alert.getAlertId(), recipient);
                
                // 에스컬레이션은 주로 이메일과 SMS를 통해 전송
                sendEscalationEmail(alert, recipient);
                sendEscalationSms(alert, recipient);
                
                log.info("Escalation notification sent successfully for alert: {}", alert.getAlertId());
                
            } catch (Exception e) {
                log.error("Failed to send escalation notification for alert: {}", alert.getAlertId(), e);
                // 에스컬레이션 실패는 더 상위로 보고
                reportEscalationFailure(alert, recipient, e);
            }
        });
    }
    
    /**
     * 대량 알림 전송 (여러 알림을 배치로 처리)
     */
    public CompletableFuture<Void> sendBatchNotifications(java.util.List<RiskAlert> alerts) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending batch notifications for {} alerts", alerts.size());
                
                alerts.parallelStream().forEach(alert -> {
                    if (alert.getConfiguration() != null && 
                        alert.getConfiguration().getNotificationChannels() != null) {
                        
                        alert.getConfiguration().getNotificationChannels().forEach(channel -> {
                            try {
                                sendNotification(alert, channel).get();
                            } catch (Exception e) {
                                log.warn("Failed to send batch notification", e);
                            }
                        });
                    }
                });
                
                log.info("Batch notifications completed for {} alerts", alerts.size());
                
            } catch (Exception e) {
                log.error("Failed to send batch notifications", e);
            }
        });
    }
    
    /**
     * 알림 전송 상태 확인
     */
    public NotificationStatus getNotificationStatus(String alertId) {
        // 실제 구현에서는 데이터베이스나 캐시에서 상태 조회
        return NotificationStatus.builder()
            .alertId(alertId)
            .status("SENT")
            .timestamp(LocalDateTime.now())
            .channels(java.util.List.of("EMAIL", "IN_APP"))
            .failedChannels(java.util.List.of())
            .retryCount(0)
            .build();
    }
    
    // ========================= 채널별 전송 메서드들 =========================
    
    private void sendEmailNotification(RiskAlert alert) {
        log.debug("Sending email notification for alert: {}", alert.getAlertId());
        
        // 이메일 내용 구성
        String subject = String.format("[%s] %s", alert.getSeverity().getDescription(), alert.getTitle());
        String body = generateEmailBody(alert);
        
        // 실제 구현에서는 이메일 서비스 (SendGrid, AWS SES 등) 사용
        simulateEmailSend(alert.getPortfolioId(), subject, body);
        
        // 전송 로그 기록
        log.info("Email sent for alert: {} - Subject: {}", alert.getAlertId(), subject);
    }
    
    private void sendSmsNotification(RiskAlert alert) {
        log.debug("Sending SMS notification for alert: {}", alert.getAlertId());
        
        // SMS는 짧은 메시지로 제한
        String message = String.format("[%s] %s - %s", 
            alert.getSeverity().getDescription(), 
            alert.getTitle(), 
            alert.getMessage());
        
        // 실제 구현에서는 SMS 서비스 (Twilio, AWS SNS 등) 사용
        simulateSmsSend(alert.getPortfolioId(), message);
        
        log.info("SMS sent for alert: {} - Length: {} chars", alert.getAlertId(), message.length());
    }
    
    private void sendSlackNotification(RiskAlert alert) {
        log.debug("Sending Slack notification for alert: {}", alert.getAlertId());
        
        // Slack 메시지 포맷
        SlackMessage slackMessage = SlackMessage.builder()
            .channel("#risk-alerts")
            .username("Risk Alert Bot")
            .text(generateSlackText(alert))
            .attachments(java.util.List.of(createSlackAttachment(alert)))
            .build();
        
        // 실제 구현에서는 Slack Webhook 또는 API 사용
        simulateSlackSend(slackMessage);
        
        log.info("Slack message sent for alert: {}", alert.getAlertId());
    }
    
    private void sendTeamsNotification(RiskAlert alert) {
        log.debug("Sending Teams notification for alert: {}", alert.getAlertId());
        
        // Microsoft Teams 카드 형태 메시지
        TeamsMessage teamsMessage = TeamsMessage.builder()
            .title(alert.getTitle())
            .subtitle(String.format("심각도: %s", alert.getSeverity().getDescription()))
            .text(alert.getDescription())
            .themeColor(alert.getSeverity().getColorCode())
            .build();
        
        // 실제 구현에서는 Teams Webhook 사용
        simulateTeamsSend(teamsMessage);
        
        log.info("Teams message sent for alert: {}", alert.getAlertId());
    }
    
    private void sendWebhookNotification(RiskAlert alert) {
        log.debug("Sending webhook notification for alert: {}", alert.getAlertId());
        
        // JSON 형태의 웹훅 페이로드
        WebhookPayload payload = WebhookPayload.builder()
            .alertId(alert.getAlertId())
            .portfolioId(alert.getPortfolioId())
            .alertType(alert.getAlertType().name())
            .severity(alert.getSeverity().name())
            .message(alert.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        // 실제 구현에서는 HTTP POST 요청 전송
        simulateWebhookSend(payload);
        
        log.info("Webhook sent for alert: {}", alert.getAlertId());
    }
    
    private void sendInAppNotification(RiskAlert alert) {
        log.debug("Sending in-app notification for alert: {}", alert.getAlertId());
        
        // 인앱 알림 (WebSocket 등을 통해 실시간 전송)
        InAppNotification notification = InAppNotification.builder()
            .alertId(alert.getAlertId())
            .title(alert.getTitle())
            .message(alert.getMessage())
            .severity(alert.getSeverity().name())
            .timestamp(LocalDateTime.now())
            .actionable(alert.getRecommendedActions() != null && !alert.getRecommendedActions().isEmpty())
            .build();
        
        // 실제 구현에서는 WebSocket이나 Server-Sent Events 사용
        simulateInAppSend(alert.getPortfolioId(), notification);
        
        log.info("In-app notification sent for alert: {}", alert.getAlertId());
    }
    
    private void sendMobilePushNotification(RiskAlert alert) {
        log.debug("Sending mobile push notification for alert: {}", alert.getAlertId());
        
        // 모바일 푸시 알림
        PushNotification pushNotification = PushNotification.builder()
            .title(alert.getTitle())
            .body(alert.getMessage())
            .badge(1)
            .sound("default")
            .priority("high")
            .data(Map.of("alertId", alert.getAlertId(), "portfolioId", alert.getPortfolioId()))
            .build();
        
        // 실제 구현에서는 Firebase Cloud Messaging 등 사용
        simulatePushSend(alert.getPortfolioId(), pushNotification);
        
        log.info("Mobile push notification sent for alert: {}", alert.getAlertId());
    }
    
    // ========================= 에스컬레이션 메서드들 =========================
    
    private void sendEscalationEmail(RiskAlert alert, String recipient) {
        String subject = String.format("🚨 ESCALATED: %s", alert.getTitle());
        String body = generateEscalationEmailBody(alert);
        
        simulateEmailSend(recipient, subject, body);
        log.info("Escalation email sent to: {}", recipient);
    }
    
    private void sendEscalationSms(RiskAlert alert, String recipient) {
        String message = String.format("🚨 ESCALATED ALERT: %s - Immediate attention required. Alert ID: %s", 
            alert.getTitle(), alert.getAlertId());
        
        simulateSmsSend(recipient, message);
        log.info("Escalation SMS sent to: {}", recipient);
    }
    
    // ========================= 실패 처리 메서드들 =========================
    
    private void handleNotificationFailure(RiskAlert alert, NotificationChannel channel, Exception e) {
        String key = alert.getAlertId() + ":" + channel.name();
        int attempts = retryCounters.getOrDefault(key, 0) + 1;
        retryCounters.put(key, attempts);
        
        if (attempts < MAX_RETRY_ATTEMPTS) {
            log.warn("Notification failed, scheduling retry {} for alert: {}", attempts, alert.getAlertId());
            scheduleRetry(alert, channel, attempts);
        } else {
            log.error("Maximum retry attempts reached for alert: {} channel: {}", alert.getAlertId(), channel);
            reportNotificationFailure(alert, channel, e);
        }
    }
    
    private void scheduleRetry(RiskAlert alert, NotificationChannel channel, int attemptNumber) {
        // 실제 구현에서는 스케줄러나 메시지 큐를 사용하여 재시도
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(attemptNumber * 5000L); // 재시도 간격 증가
                sendNotification(alert, channel).get();
            } catch (Exception e) {
                log.warn("Retry {} failed for alert: {}", attemptNumber, alert.getAlertId(), e);
            }
        });
    }
    
    private void reportNotificationFailure(RiskAlert alert, NotificationChannel channel, Exception e) {
        log.error("Permanent notification failure for alert: {} channel: {}", alert.getAlertId(), channel, e);
        // 실제 구현에서는 모니터링 시스템에 실패 보고
    }
    
    private void reportEscalationFailure(RiskAlert alert, String recipient, Exception e) {
        log.error("Escalation failure for alert: {} recipient: {}", alert.getAlertId(), recipient, e);
        // 실제 구현에서는 최고 관리자나 시스템 관리자에게 보고
    }
    
    // ========================= 메시지 생성 메서드들 =========================
    
    private String generateEmailBody(RiskAlert alert) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("알림 ID: %s\n", alert.getAlertId()));
        body.append(String.format("포트폴리오: %s\n", alert.getPortfolioId()));
        body.append(String.format("알림 유형: %s\n", alert.getAlertType().getDescription()));
        body.append(String.format("심각도: %s\n", alert.getSeverity().getDescription()));
        body.append(String.format("발생 시간: %s\n\n", alert.getCreatedAt()));
        body.append(String.format("내용: %s\n\n", alert.getDescription()));
        
        if (alert.getRecommendedActions() != null && !alert.getRecommendedActions().isEmpty()) {
            body.append("권장 조치사항:\n");
            alert.getRecommendedActions().forEach(action -> 
                body.append(String.format("- %s: %s\n", action.getTitle(), action.getDescription())));
        }
        
        return body.toString();
    }
    
    private String generateEscalationEmailBody(RiskAlert alert) {
        return String.format(
            "다음 알림이 에스컬레이션되었습니다.\n\n" +
            "알림 정보:\n%s\n\n" +
            "즉시 확인하고 필요한 조치를 취해주시기 바랍니다.",
            generateEmailBody(alert)
        );
    }
    
    private String generateSlackText(RiskAlert alert) {
        return String.format("*%s* 🚨\n포트폴리오: %s\n심각도: %s\n%s", 
            alert.getTitle(), 
            alert.getPortfolioId(), 
            alert.getSeverity().getDescription(), 
            alert.getMessage());
    }
    
    private SlackAttachment createSlackAttachment(RiskAlert alert) {
        return SlackAttachment.builder()
            .color(alert.getSeverity().getColorCode())
            .title("알림 세부사항")
            .text(alert.getDescription())
            .footer("Risk Management System")
            .timestamp(alert.getCreatedAt())
            .build();
    }
    
    // ========================= 시뮬레이션 메서드들 =========================
    
    private void simulateEmailSend(String recipient, String subject, String body) {
        // 실제 이메일 전송 시뮬레이션
        log.debug("📧 EMAIL SENT - To: {}, Subject: {}", recipient, subject);
    }
    
    private void simulateSmsSend(String recipient, String message) {
        // 실제 SMS 전송 시뮬레이션
        log.debug("📱 SMS SENT - To: {}, Message: {}", recipient, message.substring(0, Math.min(50, message.length())));
    }
    
    private void simulateSlackSend(SlackMessage message) {
        log.debug("💬 SLACK SENT - Channel: {}, Text: {}", message.channel, message.text);
    }
    
    private void simulateTeamsSend(TeamsMessage message) {
        log.debug("💼 TEAMS SENT - Title: {}, Theme: {}", message.title, message.themeColor);
    }
    
    private void simulateWebhookSend(WebhookPayload payload) {
        log.debug("🔗 WEBHOOK SENT - Alert: {}, Type: {}", payload.alertId, payload.alertType);
    }
    
    private void simulateInAppSend(String userId, InAppNotification notification) {
        log.debug("📱 IN-APP SENT - User: {}, Title: {}", userId, notification.title);
    }
    
    private void simulatePushSend(String userId, PushNotification notification) {
        log.debug("🔔 PUSH SENT - User: {}, Title: {}", userId, notification.title);
    }
    
    // ========================= 데이터 클래스들 =========================
    
    @lombok.Data
    @lombok.Builder
    public static class NotificationStatus {
        private String alertId;
        private String status;
        private LocalDateTime timestamp;
        private java.util.List<String> channels;
        private java.util.List<String> failedChannels;
        private int retryCount;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class SlackMessage {
        private String channel;
        private String username;
        private String text;
        private java.util.List<SlackAttachment> attachments;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class SlackAttachment {
        private String color;
        private String title;
        private String text;
        private String footer;
        private LocalDateTime timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class TeamsMessage {
        private String title;
        private String subtitle;
        private String text;
        private String themeColor;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class WebhookPayload {
        private String alertId;
        private String portfolioId;
        private String alertType;
        private String severity;
        private String message;
        private LocalDateTime timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class InAppNotification {
        private String alertId;
        private String title;
        private String message;
        private String severity;
        private LocalDateTime timestamp;
        private boolean actionable;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class PushNotification {
        private String title;
        private String body;
        private int badge;
        private String sound;
        private String priority;
        private Map<String, String> data;
    }
}