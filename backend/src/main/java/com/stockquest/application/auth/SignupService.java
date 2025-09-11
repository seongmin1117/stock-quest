package com.stockquest.application.auth;

import com.stockquest.application.auth.port.in.SignupUseCase;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public SignupResult signup(SignupCommand command) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + command.email());
        }
        
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(command.nickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다: " + command.nickname());
        }
        
        // 비밀번호 해싱
        String passwordHash = passwordEncoder.encode(command.password());
        
        // 사용자 생성
        User user = new User(command.email(), passwordHash, command.nickname());
        User savedUser = userRepository.save(user);
        
        return new SignupResult(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt(),
                "회원가입이 완료되었습니다."
        );
    }
}