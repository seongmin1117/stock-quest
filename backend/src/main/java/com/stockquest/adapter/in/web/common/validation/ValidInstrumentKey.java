package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 유효한 상품 키 검증 어노테이션
 * 실제 거래소에서 사용되는 심볼 형태 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstrumentKeyValidator.class)
@Documented
public @interface ValidInstrumentKey {
    String message() default "유효하지 않은 상품 키입니다. 영문 대문자와 숫자만 사용 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}