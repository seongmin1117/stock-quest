package com.stockquest.domain.user.port;

import com.stockquest.domain.user.User;

import java.util.Optional;

/**
 * 사용자 저장소 포트 (출력 포트)
 */
public interface UserRepository {
    
    /**
     * 사용자 저장
     */
    User save(User user);
    
    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(Long id);
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임 중복 확인
     */
    boolean existsByNickname(String nickname);
}