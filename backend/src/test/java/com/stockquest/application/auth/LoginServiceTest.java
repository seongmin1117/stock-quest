package com.stockquest.application.auth;

import com.stockquest.application.common.port.PasswordEncoder;
import com.stockquest.application.common.port.JwtTokenProvider;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import com.stockquest.testutils.TestBase;
import com.stockquest.testutils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

/**
 * LoginService 단위 테스트
 * Given-When-Then 패턴 적용
 */
@DisplayName("LoginService 테스트")
class LoginServiceTest extends TestBase {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private LoginService loginService;
    
    @Nested
    @DisplayName("로그인 성공 시나리오")
    class SuccessfulLogin {
        
        @Test
        @DisplayName("유효한 이메일과 비밀번호로 로그인 성공")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // Given
            String email = "test@example.com";
            String password = "validPassword123!";
            String hashedPassword = "$2a$12$hashed.password";
            String expectedToken = "jwt.token.here";
            
            User user = TestDataFactory.createUser(email, "Test User");
            user.setPasswordHash(hashedPassword);
            
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, hashedPassword)).willReturn(true);
            given(jwtTokenProvider.createAccessToken(user.getId(), email)).willReturn(expectedToken);
            
            // When
            String actualToken = loginService.login(email, password);
            
            // Then
            assertThat(actualToken).isEqualTo(expectedToken);
            
            // Verify interactions
            then(userRepository).should().findByEmail(email);
            then(passwordEncoder).should().matches(password, hashedPassword);
            then(jwtTokenProvider).should().createAccessToken(user.getId(), email);
        }
        
        @Test
        @DisplayName("대소문자를 구분하지 않는 이메일로 로그인 성공")
        void shouldLoginSuccessfullyWithCaseInsensitiveEmail() {
            // Given
            String inputEmail = "Test@Example.COM";
            String storedEmail = "test@example.com";
            String password = "validPassword123!";
            
            User user = TestDataFactory.createUser(storedEmail, "Test User");
            
            given(userRepository.findByEmail(inputEmail.toLowerCase())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn("token");
            
            // When & Then
            assertThatNoException().isThrownBy(() -> 
                loginService.login(inputEmail, password)
            );
        }
    }
    
    @Nested
    @DisplayName("로그인 실패 시나리오")
    class FailedLogin {
        
        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void shouldFailWithNonExistentEmail() {
            // Given
            String email = "nonexistent@example.com";
            String password = "anyPassword";
            
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid credentials");
            
            // Verify password encoder is not called
            then(passwordEncoder).should(never()).matches(anyString(), anyString());
            then(jwtTokenProvider).should(never()).createAccessToken(any(), any());
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void shouldFailWithInvalidPassword() {
            // Given
            String email = "test@example.com";
            String password = "wrongPassword";
            String hashedPassword = "$2a$12$hashed.password";
            
            User user = TestDataFactory.createUser(email, "Test User");
            user.setPasswordHash(hashedPassword);
            
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, hashedPassword)).willReturn(false);
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid credentials");
            
            // Verify JWT token is not created
            then(jwtTokenProvider).should(never()).createAccessToken(any(), any());
        }
        
        @Test
        @DisplayName("null 이메일로 로그인 실패")
        void shouldFailWithNullEmail() {
            // Given
            String email = null;
            String password = "anyPassword";
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email and password are required");
        }
        
        @Test
        @DisplayName("null 비밀번호로 로그인 실패")
        void shouldFailWithNullPassword() {
            // Given
            String email = "test@example.com";
            String password = null;
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email and password are required");
        }
        
        @Test
        @DisplayName("빈 문자열 이메일로 로그인 실패")
        void shouldFailWithEmptyEmail() {
            // Given
            String email = "";
            String password = "anyPassword";
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email and password are required");
        }
    }
    
    @Nested
    @DisplayName("보안 관련 테스트")
    class SecurityTests {
        
        @Test
        @DisplayName("SQL Injection 시도 방어")
        void shouldPreventSqlInjection() {
            // Given
            String maliciousEmail = "test@example.com'; DROP TABLE users; --";
            String password = "password";
            
            given(userRepository.findByEmail(maliciousEmail)).willReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(maliciousEmail, password))
                    .isInstanceOf(IllegalArgumentException.class);
            
            // Verify repository is called safely
            then(userRepository).should().findByEmail(maliciousEmail);
        }
        
        @Test
        @DisplayName("매우 긴 비밀번호 처리")
        void shouldHandleVeryLongPassword() {
            // Given
            String email = "test@example.com";
            String veryLongPassword = "a".repeat(1000);
            
            User user = TestDataFactory.createUser(email, "Test User");
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(veryLongPassword, user.getPasswordHash())).willReturn(false);
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, veryLongPassword))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    @Nested
    @DisplayName("예외 상황 처리")
    class ExceptionHandling {
        
        @Test
        @DisplayName("데이터베이스 연결 오류 처리")
        void shouldHandleDatabaseConnectionError() {
            // Given
            String email = "test@example.com";
            String password = "password";
            
            given(userRepository.findByEmail(email))
                    .willThrow(new RuntimeException("Database connection failed"));
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection failed");
        }
        
        @Test
        @DisplayName("JWT 토큰 생성 실패 처리")
        void shouldHandleJwtTokenGenerationError() {
            // Given
            String email = "test@example.com";
            String password = "password";
            
            User user = TestDataFactory.createUser(email, "Test User");
            
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(any(), any()))
                    .willThrow(new RuntimeException("JWT generation failed"));
            
            // When & Then
            assertThatThrownBy(() -> loginService.login(email, password))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("JWT generation failed");
        }
    }
}