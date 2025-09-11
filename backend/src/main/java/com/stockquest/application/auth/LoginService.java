package com.stockquest.application.auth;

import com.stockquest.adapter.out.security.JwtTokenProvider;
import com.stockquest.application.auth.port.in.LoginUseCase;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
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
    
    @Override
    public LoginResult login(LoginCommand command) {
        // 사용자 조회
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다: " + command.email()));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        
        return new LoginResult(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                jwtTokenProvider.getExpiration(accessToken),
                "로그인 성공"
        );
    }
}