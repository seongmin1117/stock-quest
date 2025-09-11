package com.stockquest.adapter.in.web.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 비밀번호 강도 검증기
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern CONSECUTIVE = Pattern.compile("(.)\\1\\1"); // 같은 문자 3번 연속

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // 길이 체크
        if (password.length() < 8 || password.length() > 50) {
            return false;
        }

        // 연속된 같은 문자 체크
        if (CONSECUTIVE.matcher(password).find()) {
            return false;
        }

        // 문자 종류별 체크
        int criteriaCount = 0;
        
        if (UPPERCASE.matcher(password).find()) {
            criteriaCount++;
        }
        if (LOWERCASE.matcher(password).find()) {
            criteriaCount++;
        }
        if (DIGIT.matcher(password).find()) {
            criteriaCount++;
        }
        if (SPECIAL.matcher(password).find()) {
            criteriaCount++;
        }

        // 최소 3가지 종류의 문자 포함 여부
        return criteriaCount >= 3;
    }
}