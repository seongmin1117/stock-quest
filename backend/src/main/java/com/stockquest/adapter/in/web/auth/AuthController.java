package com.stockquest.adapter.in.web.auth;

import com.stockquest.adapter.in.web.auth.dto.LoginRequest;
import com.stockquest.adapter.in.web.auth.dto.SignupRequest;
import com.stockquest.adapter.in.web.auth.dto.RefreshTokenRequest;
import com.stockquest.adapter.in.web.auth.dto.LogoutRequest;
import com.stockquest.adapter.in.web.common.dto.AuthResponse;
import com.stockquest.adapter.in.web.common.ErrorResponse;
import com.stockquest.application.auth.port.in.GetCurrentUserUseCase;
import com.stockquest.application.auth.port.in.LoginUseCase;
import com.stockquest.application.auth.port.in.RefreshTokenUseCase;
import com.stockquest.application.auth.port.in.LogoutUseCase;
import com.stockquest.application.auth.port.in.SignupUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 인증 관련 REST 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
public class AuthController {
    
    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    
    public AuthController(SignupUseCase signupUseCase, LoginUseCase loginUseCase, GetCurrentUserUseCase getCurrentUserUseCase,
                         RefreshTokenUseCase refreshTokenUseCase, LogoutUseCase logoutUseCase) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }
    
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자 계정을 생성합니다",
        responses = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", 
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        var command = new SignupUseCase.SignupCommand(
            request.email(),
            request.password(),
            request.nickname()
        );
        
        var result = signupUseCase.signup(command);
        
        var response = AuthResponse.basic(
            null,  // 회원가입 시에는 토큰 없음
            result.userId(),
            result.email(),
            result.nickname()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하며, 선택적으로 리다이렉트 URL을 지원합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = new LoginUseCase.LoginCommand(
            request.email(),
            request.password()
        );
        
        var result = loginUseCase.login(command);
        
        // TokenPair 정보로 완전한 토큰 응답 생성
        var response = AuthResponse.withTokenPair(
            result.accessToken(),
            result.refreshToken(),
            result.accessTokenExpiresAt(),
            result.refreshTokenExpiresAt(),
            result.userId(),
            result.email(),
            result.nickname(),
            request.redirectUrl()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    @Operation(
        summary = "현재 사용자 조회",
        description = "JWT 토큰 기반으로 현재 인증된 사용자 정보를 조회합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        var result = getCurrentUserUseCase.getCurrentUser(userId);
        
        var response = AuthResponse.basic(
            null,  // /me 엔드포인트에서는 토큰 재발급 안함
            result.userId(),
            result.email(),
            result.nickname()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 만료 또는 유효하지 않음",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        var command = new RefreshTokenUseCase.RefreshTokenCommand(request.refreshToken());
        
        var result = refreshTokenUseCase.refreshToken(command);
        
        var response = AuthResponse.withTokenPair(
            result.accessToken(),
            result.refreshToken(),
            result.accessTokenExpiresAt(),
            result.refreshTokenExpiresAt(),
            result.userId(),
            result.email(),
            result.nickname(),
            null
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 종료하고 토큰을 무효화합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<?> logout(
        @RequestBody(required = false) LogoutRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                    .message("인증이 필요합니다")
                    .error("AUTHENTICATION_REQUIRED")
                    .status(401)
                    .path("/api/auth/logout")
                    .build());
        }
        
        Long userId = Long.parseLong(userDetails.getUsername());
        
        // 로그아웃 명령 생성
        var command = new LogoutUseCase.LogoutCommand(
            userId,
            request != null ? request.refreshToken() : null,
            request != null ? request.logoutFromAllDevices() : false
        );
        
        var result = logoutUseCase.logout(command);
        
        return ResponseEntity.ok(Map.of(
            "success", result.success(),
            "message", result.message()
        ));
    }
    
    @GetMapping("/validate")
    @Operation(
        summary = "토큰 검증",
        description = "현재 토큰의 유효성을 검증하고 사용자 정보를 반환합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "토큰 유효"),
            @ApiResponse(responseCode = "401", description = "토큰 무효",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    public ResponseEntity<AuthResponse> validateToken(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userDetails.getUsername());
        var result = getCurrentUserUseCase.getCurrentUser(userId);
        
        var response = AuthResponse.basic(
            null, // 토큰 재발급은 /refresh 엔드포인트에서
            result.userId(),
            result.email(),
            result.nickname()
        );
        
        return ResponseEntity.ok(response);
    }
}