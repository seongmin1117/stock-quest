package com.stockquest.application.auth;

import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 권한 검증 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {
    
    private final UserRepository userRepository;
    
    /**
     * 관리자 권한 확인
     */
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        
        return userRepository.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }
    
    /**
     * 리소스 소유자 또는 관리자 권한 확인
     */
    public boolean canAccessResource(Long userId, Long resourceOwnerId) {
        if (userId == null) {
            return false;
        }
        
        // 본인 리소스인 경우
        if (userId.equals(resourceOwnerId)) {
            return true;
        }
        
        // 관리자인 경우
        return isAdmin(userId);
    }
    
    /**
     * 챌린지 수정 권한 확인
     */
    public boolean canModifyChallenge(Long userId, Long challengeCreatorId) {
        return canAccessResource(userId, challengeCreatorId);
    }
}