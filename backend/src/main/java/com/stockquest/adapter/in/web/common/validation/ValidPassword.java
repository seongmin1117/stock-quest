package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 강력한 비밀번호 유효성 검증 어노테이션
 * - 최소 8자, 최대 50자
 * - 대문자, 소문자, 숫자, 특수문자 중 최소 3가지 포함
 * - 연속된 문자 3개 이상 금지
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
    String message() default "비밀번호는 8-50자이며, 대소문자/숫자/특수문자 중 3가지 이상을 포함해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}