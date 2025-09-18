-- V17: 사용자 권한 관리를 위한 role 컬럼 추가

-- users 테이블에 role 컬럼 추가
ALTER TABLE users 
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' 
COMMENT '사용자 역할 (USER, ADMIN)';

-- 기존 사용자들은 모두 USER 역할로 설정
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- role 컬럼에 인덱스 추가 (관리자 검색 최적화)
CREATE INDEX idx_user_role ON users(role);

-- 권한별 사용자 수 확인용 뷰 생성
CREATE VIEW v_user_role_stats AS
SELECT 
    role,
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM users), 2) as percentage
FROM users 
GROUP BY role
ORDER BY user_count DESC;

-- 관리자 계정 생성 (개발용 - 프로덕션에서는 별도 생성 필요)
-- 비밀번호: admin123 (BCrypt 해시)
INSERT IGNORE INTO users (email, password_hash, nickname, role, created_at, updated_at)
VALUES (
    'admin@stockquest.com',
    '$2a$10$N.zmdr9k7uOIW8B8gLKih.0.0.aK4QNjp9//.mKnN8m9VhCxj3s9W',
    '시스템관리자',
    'ADMIN',
    NOW(),
    NOW()
);

-- 권한 관련 제약조건 추가
ALTER TABLE users
ADD CONSTRAINT chk_user_role
CHECK (role IN ('USER', 'ADMIN'));

-- 관리자 활동 로그 테이블 생성
CREATE TABLE admin_activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL COMMENT '수행 작업 유형',
    target_type VARCHAR(50) COMMENT '대상 엔티티 타입',
    target_id BIGINT COMMENT '대상 엔티티 ID',
    description TEXT COMMENT '작업 설명',
    ip_address VARCHAR(45) COMMENT '접속 IP',
    user_agent VARCHAR(500) COMMENT '사용자 에이전트',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (admin_user_id) REFERENCES users(id),
    INDEX idx_admin_activity_admin_id (admin_user_id),
    INDEX idx_admin_activity_created_at (created_at),
    INDEX idx_admin_activity_action_type (action_type)
) ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci 
COMMENT='관리자 활동 로그';