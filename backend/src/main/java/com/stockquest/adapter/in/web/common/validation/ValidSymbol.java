package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 주식 심볼 유효성 검증 어노테이션
 * - 1-10자의 영대문자만 허용
 * - 공백, 특수문자, 숫자 불허
 * - SQL 인젝션 및 XSS 패턴 차단
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SymbolValidator.class)
@Documented
public @interface ValidSymbol {
    String message() default "주식 심볼은 1-10자의 영대문자만 허용됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}