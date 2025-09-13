package com.stockquest.adapter.in.web.common.validation;

import com.stockquest.adapter.in.web.common.security.SecurityEnhancement;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 주식 심볼 검증기
 * 보안 강화 및 형식 검증
 */
@Slf4j
public class SymbolValidator implements ConstraintValidator<ValidSymbol, String> {

    @Override
    public boolean isValid(String symbol, ConstraintValidatorContext context) {
        if (symbol == null) {
            return false;
        }
        
        String trimmedSymbol = symbol.trim();
        
        // 빈 문자열 체크
        if (trimmedSymbol.isEmpty()) {
            return false;
        }
        
        // 보안 검증 (SQL 인젝션, XSS 등)
        SecurityEnhancement.ValidationResult validationResult = 
            SecurityEnhancement.validateInput(trimmedSymbol, "symbol");
        
        if (!validationResult.isValid()) {
            log.warn("심볼 보안 검증 실패: {} - {}", trimmedSymbol, validationResult.getErrorMessage());
            return false;
        }
        
        // 주식 심볼 형식 검증
        boolean isValidSymbol = SecurityEnhancement.isValidSymbol(trimmedSymbol);
        
        if (!isValidSymbol) {
            log.debug("유효하지 않은 심볼 형식: {}", trimmedSymbol);
        }
        
        return isValidSymbol;
    }
}