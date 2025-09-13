package com.stockquest.adapter.in.web.common.validation;

import com.stockquest.adapter.in.web.common.security.SecurityEnhancement;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 안전한 텍스트 검증기
 * 보안 위협 요소 차단 및 길이 제한
 */
@Slf4j
public class SafeTextValidator implements ConstraintValidator<SafeText, String> {
    
    private int maxLength;
    private boolean allowHtml;
    
    @Override
    public void initialize(SafeText annotation) {
        this.maxLength = annotation.maxLength();
        this.allowHtml = annotation.allowHtml();
    }

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context) {
        if (text == null) {
            return true; // null 값은 @NotNull 등 다른 어노테이션에서 처리
        }
        
        // 길이 제한 체크
        if (text.length() > maxLength) {
            log.debug("텍스트 길이 초과: actual={}, max={}", text.length(), maxLength);
            return false;
        }
        
        // 보안 검증
        SecurityEnhancement.ValidationResult validationResult = 
            SecurityEnhancement.validateInput(text, "text");
        
        if (!validationResult.isValid()) {
            log.warn("텍스트 보안 검증 실패: {} - {}", 
                text.substring(0, Math.min(50, text.length())), 
                validationResult.getErrorMessage());
            return false;
        }
        
        // HTML 허용 여부에 따른 추가 검증
        if (!allowHtml) {
            // HTML 태그가 포함되어 있는지 추가 검증
            if (text.contains("<") || text.contains(">") || 
                text.contains("&lt;") || text.contains("&gt;")) {
                log.debug("HTML 태그 포함된 텍스트 차단: {}", 
                    text.substring(0, Math.min(30, text.length())));
                return false;
            }
        }
        
        return true;
    }
}