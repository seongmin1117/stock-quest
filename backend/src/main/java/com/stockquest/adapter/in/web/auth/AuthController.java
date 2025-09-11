package com.stockquest.adapter.in.web.auth;

import com.stockquest.adapter.in.web.auth.dto.LoginRequest;
import com.stockquest.adapter.in.web.auth.dto.SignupRequest;
import com.stockquest.adapter.in.web.common.dto.AuthResponse;
import com.stockquest.adapter.in.web.common.ErrorResponse;
import com.stockquest.application.auth.port.in.GetCurrentUserUseCase;
import com.stockquest.application.auth.port.in.LoginUseCase;
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

/**
 * 인증 관련 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
public class AuthController {
    
    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    
    public AuthController(SignupUseCase signupUseCase, LoginUseCase loginUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
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
        
        var response = new AuthResponse(
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
        description = "이메일과 비밀번호로 로그인합니다",
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
        
        var response = new AuthResponse(
            result.accessToken(),
            result.userId(),
            result.email(),
            result.nickname()
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
        
        var response = new AuthResponse(
            null,  // /me 엔드포인트에서는 토큰 재발급 안함
            result.userId(),
            result.email(),
            result.nickname()
        );
        
        return ResponseEntity.ok(response);
    }
}