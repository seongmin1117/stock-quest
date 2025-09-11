package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 상품 키 검증기
 * 주식 심볼 형태의 문자열 검증 (예: AAPL, MSFT, GOOGL)
 */
public class InstrumentKeyValidator implements ConstraintValidator<ValidInstrumentKey, String> {

    // 영문 대문자와 숫자만 허용, 1-10자
    private static final Pattern INSTRUMENT_KEY_PATTERN = Pattern.compile("^[A-Z0-9]{1,10}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        return INSTRUMENT_KEY_PATTERN.matcher(value.trim()).matches();
    }
}