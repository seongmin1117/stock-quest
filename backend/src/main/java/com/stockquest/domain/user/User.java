package com.stockquest.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 엔티티
 * 플랫폼에 가입한 투자 학습자를 나타냄
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 도메인 생성자
    public User(String email, String passwordHash, String nickname) {
        validateEmail(email);
        validateNickname(nickname);
        
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = Role.USER; // 기본적으로 일반 사용자로 설정
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다");
        }
    }
    
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다");
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2-20자 사이여야 합니다");
        }
    }
    
    public void updateNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updatePassword(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("패스워드 해시는 필수입니다");
        }
        this.passwordHash = passwordHash;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 관리자 권한 여부 확인
     */
    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }
    
    /**
     * 사용자 권한 변경 (관리자만 가능)
     */
    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("권한은 필수입니다");
        }
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }
}