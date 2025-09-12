package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 안전한 텍스트 입력 검증 어노테이션
 * - XSS, SQL 인젝션, 경로 순회 공격 방지
 * - HTML 태그 및 스크립트 차단
 * - 최대 길이 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTextValidator.class)
@Documented
public @interface SafeText {
    String message() default "입력에 허용되지 않은 문자나 패턴이 포함되어 있습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 최대 길이 (기본값: 500)
     */
    int maxLength() default 500;
    
    /**
     * HTML 태그 허용 여부 (기본값: false)
     */
    boolean allowHtml() default false;
}