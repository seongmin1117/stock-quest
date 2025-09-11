package com.stockquest.adapter.in.web.auth.dto;

import com.stockquest.adapter.in.web.common.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 */
@Schema(description = "회원가입 요청")
public record SignupRequest(
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,
    
    @Schema(description = "비밀번호 (최소 8자, 대소문자/숫자/특수문자 중 3가지 이상)", example = "Password123!")
    @NotBlank(message = "비밀번호는 필수입니다")
    @ValidPassword
    String password,
    
    @Schema(description = "사용자 닉네임 (한글, 영문, 숫자만 허용)", example = "투자왕")
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다")
    String nickname
) {}