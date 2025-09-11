package com.stockquest.application.auth;

import com.stockquest.application.auth.port.in.GetCurrentUserUseCase;
import com.stockquest.application.auth.port.in.LoginUseCase;
import com.stockquest.application.auth.port.in.SignupUseCase;
import com.stockquest.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 인증 플로우 통합 테스트
 * 회원가입 → 로그인 → 현재 사용자 조회 플로우 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class AuthFlowIntegrationTest {
    
    @Autowired
    private SignupUseCase signupUseCase;
    
    @Autowired
    private LoginUseCase loginUseCase;
    
    @Autowired
    private GetCurrentUserUseCase getCurrentUserUseCase;
    
    @Test
    @DisplayName("회원가입 → 로그인 → 현재 사용자 조회 플로우가 정상 동작해야 한다")
    void authFlowShouldWorkCorrectly() {
        // Given: 회원가입 정보
        String email = "test@example.com";
        String password = "password123";
        String nickname = "테스트사용자";
        
        // When: 회원가입 실행
        var signupCommand = new SignupUseCase.SignupCommand(email, password, nickname);
        var signupResult = signupUseCase.signup(signupCommand);
        
        // Then: 회원가입 성공 확인
        assertThat(signupResult.userId()).isNotNull();
        assertThat(signupResult.email()).isEqualTo(email);
        assertThat(signupResult.nickname()).isEqualTo(nickname);
        assertThat(signupResult.message()).contains("회원가입이 완료되었습니다");
        
        // When: 로그인 실행
        var loginCommand = new LoginUseCase.LoginCommand(email, password);
        var loginResult = loginUseCase.login(loginCommand);
        
        // Then: 로그인 성공 확인
        assertThat(loginResult.accessToken()).isNotNull();
        assertThat(loginResult.userId()).isEqualTo(signupResult.userId());
        assertThat(loginResult.email()).isEqualTo(email);
        assertThat(loginResult.nickname()).isEqualTo(nickname);
        assertThat(loginResult.expiresAt()).isAfter(java.time.LocalDateTime.now());
        assertThat(loginResult.message()).contains("로그인 성공");
        
        // When: 현재 사용자 조회 실행
        var currentUserResult = getCurrentUserUseCase.getCurrentUser(loginResult.userId());
        
        // Then: 현재 사용자 정보 확인
        assertThat(currentUserResult.userId()).isEqualTo(signupResult.userId());
        assertThat(currentUserResult.email()).isEqualTo(email);
        assertThat(currentUserResult.nickname()).isEqualTo(nickname);
    }
    
    @Test
    @DisplayName("중복된 이메일로 회원가입 시 예외가 발생해야 한다")
    void signupWithDuplicateEmailShouldThrowException() {
        // Given: 첫 번째 사용자 회원가입
        String email = "duplicate@example.com";
        var firstCommand = new SignupUseCase.SignupCommand(email, "password123", "첫번째사용자");
        signupUseCase.signup(firstCommand);
        
        // When & Then: 동일한 이메일로 두 번째 회원가입 시도 시 예외 발생
        var secondCommand = new SignupUseCase.SignupCommand(email, "password456", "두번째사용자");
        assertThatThrownBy(() -> signupUseCase.signup(secondCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 가입된 이메일입니다");
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생해야 한다")
    void loginWithWrongPasswordShouldThrowException() {
        // Given: 사용자 회원가입
        String email = "wrongpass@example.com";
        String correctPassword = "correct123";
        var signupCommand = new SignupUseCase.SignupCommand(email, correctPassword, "사용자");
        signupUseCase.signup(signupCommand);
        
        // When & Then: 잘못된 비밀번호로 로그인 시도 시 예외 발생
        var loginCommand = new LoginUseCase.LoginCommand(email, "wrong123");
        assertThatThrownBy(() -> loginUseCase.login(loginCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 올바르지 않습니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생해야 한다")
    void loginWithNonExistentEmailShouldThrowException() {
        // When & Then: 존재하지 않는 이메일로 로그인 시도 시 예외 발생
        var loginCommand = new LoginUseCase.LoginCommand("nonexistent@example.com", "password123");
        assertThatThrownBy(() -> loginUseCase.login(loginCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("등록되지 않은 이메일입니다");
    }
}