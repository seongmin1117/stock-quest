package com.stockquest.adapter.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * 로그인 요청 DTO - 리다이렉트 URL 지원 추가
 */
@Schema(description = "로그인 요청")
public record LoginRequest(
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,
    
    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수입니다")
    String password,
    
    @Schema(description = "로그인 성공 후 리다이렉트할 URL (선택사항)", 
            example = "/dashboard", nullable = true)
    String redirectUrl
) {}