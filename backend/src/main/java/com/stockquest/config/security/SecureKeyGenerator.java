package com.stockquest.config.security;

import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 보안 키 생성 유틸리티
 * JWT 서명키 및 기타 암호화 키 생성을 담당
 */
@Component
public class SecureKeyGenerator {
    
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final int KEY_SIZE = 256; // 256비트 (32바이트)
    
    /**
     * 256비트 HMAC-SHA256 키 생성
     * JWT 서명용 강력한 암호화 키
     */
    public String generateSecureJwtKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_SHA_256);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secure JWT key", e);
        }
    }
    
    /**
     * 지정된 길이의 랜덤 문자열 생성
     * Salt, 토큰 생성 등에 활용
     */
    public String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * 바이트 배열을 Base64 인코딩된 문자열로 변환
     */
    public String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    
    /**
     * Base64 문자열을 바이트 배열로 디코딩
     */
    public byte[] decodeFromBase64(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }
}