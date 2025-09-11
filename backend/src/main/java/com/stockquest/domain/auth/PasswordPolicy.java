package com.stockquest.domain.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 비밀번호 정책 도메인 서비스
 * 강력한 비밀번호 규칙 적용 및 검증
 */
public class PasswordPolicy {
    
    // 비밀번호 규칙 상수
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    
    // 금지된 패턴들
    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
        Pattern.compile("123456", Pattern.CASE_INSENSITIVE),
        Pattern.compile("password", Pattern.CASE_INSENSITIVE),
        Pattern.compile("qwerty", Pattern.CASE_INSENSITIVE),
        Pattern.compile("admin", Pattern.CASE_INSENSITIVE),
        Pattern.compile("stockquest", Pattern.CASE_INSENSITIVE)
    );
    
    /**
     * 비밀번호 유효성 검증
     */
    public PasswordValidationResult validate(String password) {
        List<String> violations = new ArrayList<>();
        
        if (password == null) {
            violations.add("비밀번호를 입력해주세요.");
            return new PasswordValidationResult(false, violations);
        }
        
        // 길이 검증
        if (password.length() < MIN_LENGTH) {
            violations.add("비밀번호는 최소 " + MIN_LENGTH + "자 이상이어야 합니다.");
        }
        
        if (password.length() > MAX_LENGTH) {
            violations.add("비밀번호는 최대 " + MAX_LENGTH + "자 이하여야 합니다.");
        }
        
        // 복잡도 검증
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("소문자를 최소 1개 이상 포함해야 합니다.");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("대문자를 최소 1개 이상 포함해야 합니다.");
        }
        
        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add("숫자를 최소 1개 이상 포함해야 합니다.");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("특수문자를 최소 1개 이상 포함해야 합니다.");
        }
        
        // 금지된 패턴 검증
        for (Pattern forbiddenPattern : FORBIDDEN_PATTERNS) {
            if (forbiddenPattern.matcher(password).find()) {
                violations.add("일반적으로 사용되는 비밀번호는 사용할 수 없습니다.");
                break;
            }
        }
        
        // 연속된 문자 검증
        if (hasConsecutiveCharacters(password)) {
            violations.add("연속된 문자나 숫자를 3개 이상 사용할 수 없습니다.");
        }
        
        // 반복된 문자 검증
        if (hasRepeatedCharacters(password)) {
            violations.add("같은 문자를 3번 이상 연속으로 사용할 수 없습니다.");
        }
        
        return new PasswordValidationResult(violations.isEmpty(), violations);
    }
    
    /**
     * 비밀번호 강도 평가
     */
    public PasswordStrength evaluateStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // 길이 점수
        if (password.length() >= 12) score += 2;
        else if (password.length() >= 10) score += 1;
        
        // 복잡도 점수
        if (LOWERCASE_PATTERN.matcher(password).find()) score += 1;
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 1;
        if (DIGIT_PATTERN.matcher(password).find()) score += 1;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 1;
        
        // 다양성 점수
        if (hasVariousCharacterTypes(password)) score += 1;
        
        if (score >= 7) return PasswordStrength.STRONG;
        if (score >= 5) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
    
    /**
     * 연속된 문자 검사
     */
    private boolean hasConsecutiveCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if ((c1 + 1 == c2 && c2 + 1 == c3) || (c1 - 1 == c2 && c2 - 1 == c3)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 반복된 문자 검사
     */
    private boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i + 1) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 다양한 문자 타입 포함 검사
     */
    private boolean hasVariousCharacterTypes(String password) {
        int typeCount = 0;
        if (LOWERCASE_PATTERN.matcher(password).find()) typeCount++;
        if (UPPERCASE_PATTERN.matcher(password).find()) typeCount++;
        if (DIGIT_PATTERN.matcher(password).find()) typeCount++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) typeCount++;
        return typeCount >= 3;
    }
    
    /**
     * 비밀번호 유효성 검증 결과
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> violations;
        
        public PasswordValidationResult(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = violations != null ? violations : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getViolations() {
            return violations;
        }
        
        public String getViolationMessage() {
            return String.join(" ", violations);
        }
    }
    
    /**
     * 비밀번호 강도
     */
    public enum PasswordStrength {
        WEAK("약함", 1),
        MEDIUM("보통", 2),
        STRONG("강함", 3);
        
        private final String description;
        private final int level;
        
        PasswordStrength(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return level;
        }
    }
}