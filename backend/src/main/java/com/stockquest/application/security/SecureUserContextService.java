package com.stockquest.application.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 보안 강화된 사용자 컨텍스트 서비스
 *
 * 안전한 사용자 ID 추출 및 검증을 담당
 * 취약한 String → Long 변환을 방지하고 보안 감사 로깅을 제공
 */
@Slf4j
@Service
public class SecureUserContextService {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^\\d{1,19}$"); // Long.MAX_VALUE 고려
    private static final String SECURITY_LOG_PREFIX = "[SECURITY] ";

    /**
     * 현재 인증된 사용자의 ID를 안전하게 추출
     *
     * @param userDetails Spring Security 사용자 상세 정보
     * @return 검증된 사용자 ID
     * @throws SecurityException 유효하지 않은 사용자 인증 형식일 경우
     * @throws IllegalStateException 사용자가 인증되지 않은 경우
     */
    public Long getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            log.error(SECURITY_LOG_PREFIX + "UserDetails is null - potential authentication bypass attempt");
            throw new IllegalStateException("User authentication is required");
        }

        return extractAndValidateUserId(userDetails.getUsername());
    }

    /**
     * Spring Security Context에서 현재 사용자 ID를 안전하게 추출
     *
     * @return 검증된 사용자 ID
     * @throws SecurityException 유효하지 않은 인증 상태일 경우
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error(SECURITY_LOG_PREFIX + "No valid authentication found in security context");
            throw new SecurityException("User authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            log.error(SECURITY_LOG_PREFIX + "Invalid principal type: {}", principal.getClass().getSimpleName());
            throw new SecurityException("Invalid authentication principal type");
        }

        return extractAndValidateUserId(userDetails.getUsername());
    }

    /**
     * 사용자명에서 사용자 ID를 안전하게 추출하고 검증
     *
     * @param username 사용자명 (사용자 ID 문자열로 예상)
     * @return 검증된 사용자 ID
     * @throws SecurityException 유효하지 않은 형식일 경우
     */
    private Long extractAndValidateUserId(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.error(SECURITY_LOG_PREFIX + "Empty username provided");
            throw new SecurityException("Invalid user authentication format: empty username");
        }

        String trimmedUsername = username.trim();

        // 정규식으로 형식 검증 (숫자만, 1-19자리)
        if (!USER_ID_PATTERN.matcher(trimmedUsername).matches()) {
            log.error(SECURITY_LOG_PREFIX + "Invalid username format: {} (expected numeric)",
                     sanitizeForLogging(trimmedUsername));
            throw new SecurityException("Invalid user authentication format");
        }

        try {
            Long userId = Long.parseLong(trimmedUsername);

            // 추가 비즈니스 검증
            if (userId <= 0) {
                log.error(SECURITY_LOG_PREFIX + "Invalid user ID value: {} (must be positive)", userId);
                throw new SecurityException("Invalid user ID value");
            }

            log.debug("Successfully extracted user ID: {}", userId);
            return userId;

        } catch (NumberFormatException e) {
            log.error(SECURITY_LOG_PREFIX + "Failed to parse user ID from username: {}",
                     sanitizeForLogging(trimmedUsername), e);
            throw new SecurityException("Invalid user authentication format", e);
        }
    }

    /**
     * 현재 사용자가 지정된 사용자 ID와 일치하는지 검증
     * 권한 확인을 위한 보안 메서드
     *
     * @param targetUserId 검증할 대상 사용자 ID
     * @return 현재 사용자가 대상 사용자와 일치하면 true
     */
    public boolean isCurrentUser(Long targetUserId) {
        try {
            Long currentUserId = getCurrentUserId();
            boolean isMatch = currentUserId.equals(targetUserId);

            if (!isMatch) {
                log.warn(SECURITY_LOG_PREFIX + "User {} attempted to access resources for user {}",
                        currentUserId, targetUserId);
            }

            return isMatch;
        } catch (Exception e) {
            log.error(SECURITY_LOG_PREFIX + "Failed to verify user identity", e);
            return false;
        }
    }

    /**
     * 관리자 권한 확인
     *
     * @return 현재 사용자가 관리자 권한을 가지고 있으면 true
     */
    public boolean hasAdminRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                return false;
            }

            boolean hasAdminRole = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

            if (hasAdminRole) {
                log.debug(SECURITY_LOG_PREFIX + "Admin access granted for user: {}", getCurrentUserId());
            }

            return hasAdminRole;
        } catch (Exception e) {
            log.error(SECURITY_LOG_PREFIX + "Failed to check admin role", e);
            return false;
        }
    }

    /**
     * 로깅을 위해 사용자 입력을 안전하게 정리
     * 로그 인젝션 공격 방지
     */
    private String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        // 개행 문자와 제어 문자 제거하여 로그 인젝션 방지
        return input.replaceAll("[\\r\\n\\t]", "_").substring(0, Math.min(input.length(), 50));
    }
}