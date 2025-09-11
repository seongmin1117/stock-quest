package com.stockquest.application.auth;

import com.stockquest.application.auth.port.in.GetCurrentUserUseCase;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 현재 사용자 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCurrentUserService implements GetCurrentUserUseCase {
    
    private final UserRepository userRepository;
    
    @Override
    public CurrentUserResult getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        return new CurrentUserResult(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}