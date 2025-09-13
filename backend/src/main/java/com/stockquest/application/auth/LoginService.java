package com.stockquest.application.auth;

import com.stockquest.adapter.out.security.JwtTokenProvider;
import com.stockquest.application.auth.port.in.LoginUseCase;
import com.stockquest.domain.auth.RefreshToken;
import com.stockquest.domain.auth.TokenPair;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    
    @Override
    public LoginResult login(LoginCommand command) {
        // 사용자 조회
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new BadCredentialsException("등록되지 않은 이메일입니다: " + command.email()));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }
        
        // JWT 토큰 쌍 생성
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user.getId(), user.getEmail());
        
        // 리프레시 토큰 저장
        refreshTokenService.createAndSaveRefreshToken(user.getId(), tokenPair.getRefreshToken());
        
        return new LoginResult(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                tokenPair.getAccessTokenExpiresAt(),
                tokenPair.getRefreshTokenExpiresAt(),
                "로그인 성공"
        );
    }
}