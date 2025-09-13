package com.stockquest.application.auth;

import com.stockquest.adapter.out.security.JwtTokenProvider;
import com.stockquest.application.auth.port.in.RefreshTokenUseCase;
import com.stockquest.domain.auth.RefreshToken;
import com.stockquest.domain.auth.TokenPair;
import com.stockquest.domain.auth.port.RefreshTokenRepository;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리프레시 토큰 서비스
 * 토큰 갱신 및 관리 담당
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public RefreshTokenResult refreshToken(RefreshTokenCommand command) {
        // 리프레시 토큰 조회 및 검증
        RefreshToken refreshToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        
        if (!refreshToken.isValid()) {
            log.warn("Invalid refresh token used: userId={}, expired={}", 
                    refreshToken.getUserId(), refreshToken.isExpired());
            throw new IllegalArgumentException("만료되거나 폐기된 리프레시 토큰입니다.");
        }
        
        // 사용자 정보 조회
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 새로운 토큰 쌍 생성
        TokenPair newTokenPair = jwtTokenProvider.createTokenPair(user.getId(), user.getEmail());
        
        // 기존 리프레시 토큰 사용 기록 업데이트 및 폐기
        refreshToken.updateLastUsed();
        refreshToken.revoke();
        
        // 새 리프레시 토큰 저장
        RefreshToken newRefreshToken = RefreshToken.create(
                user.getId(), 
                newTokenPair.getRefreshToken(), 
                7 // 7일 유효
        );
        refreshTokenRepository.save(newRefreshToken);
        
        log.info("Token refreshed successfully for userId: {}", user.getId());
        
        return new RefreshTokenResult(
                newTokenPair.getAccessToken(),
                newTokenPair.getRefreshToken(),
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                newTokenPair.getAccessTokenExpiresAt(),
                newTokenPair.getRefreshTokenExpiresAt(),
                "토큰이 성공적으로 갱신되었습니다."
        );
    }
    
    /**
     * 새로운 리프레시 토큰 생성 및 저장
     * (LoginService에서 사용)
     */
    public RefreshToken createAndSaveRefreshToken(Long userId, String refreshTokenValue) {
        RefreshToken refreshToken = RefreshToken.create(userId, refreshTokenValue, 7);
        return refreshTokenRepository.save(refreshToken);
    }
}