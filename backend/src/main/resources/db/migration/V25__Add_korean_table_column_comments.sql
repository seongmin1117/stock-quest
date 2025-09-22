-- V25: 테이블과 컬럼에 한국어 코멘트 추가
-- 작성일: 2025-09-22
-- 목적: 데이터베이스 스키마 문서화 개선 - 영어 코멘트를 한국어로 변경

-- ========================================
-- 1. 챌린지 관련 테이블 코멘트 추가
-- ========================================

-- 챌린지 메인 테이블
ALTER TABLE challenge COMMENT = '투자 챌린지 정보';
ALTER TABLE challenge
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '챌린지 고유 식별자',
  MODIFY COLUMN title VARCHAR(200) NOT NULL COMMENT '챌린지 제목',
  MODIFY COLUMN description TEXT COMMENT '챌린지 상세 설명',
  MODIFY COLUMN difficulty ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') NOT NULL DEFAULT 'BEGINNER' COMMENT '난이도 (초급/중급/고급/전문가)',
  MODIFY COLUMN initial_balance DECIMAL(15,2) NOT NULL DEFAULT '1000000.00' COMMENT '초기 투자 자금 (원)',
  MODIFY COLUMN duration_days INT NOT NULL DEFAULT '30' COMMENT '챌린지 진행 기간 (일)',
  MODIFY COLUMN start_date DATETIME NOT NULL DEFAULT '2024-01-01 00:00:00' COMMENT '챌린지 시작 일시',
  MODIFY COLUMN end_date DATETIME NOT NULL DEFAULT '2024-01-31 23:59:59' COMMENT '챌린지 종료 일시',
  MODIFY COLUMN period_start DATE NOT NULL COMMENT '백테스팅 시작 날짜',
  MODIFY COLUMN period_end DATE NOT NULL COMMENT '백테스팅 종료 날짜',
  MODIFY COLUMN speed_factor INT NOT NULL DEFAULT '10' COMMENT '시뮬레이션 속도 배율 (1-100배)',
  MODIFY COLUMN status ENUM('DRAFT','ACTIVE','COMPLETED') NOT NULL DEFAULT 'DRAFT' COMMENT '챌린지 상태 (초안/활성/완료)',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 챌린지 세션 테이블
ALTER TABLE challenge_session COMMENT = '사용자별 챌린지 참여 세션';
ALTER TABLE challenge_session
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '세션 고유 식별자',
  MODIFY COLUMN challenge_id BIGINT NOT NULL COMMENT '챌린지 ID (FK)',
  MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '사용자 ID (FK)',
  MODIFY COLUMN initial_balance DECIMAL(15,2) NOT NULL COMMENT '초기 투자 자금',
  MODIFY COLUMN current_balance DECIMAL(15,2) NOT NULL COMMENT '현재 보유 현금',
  MODIFY COLUMN status ENUM('READY','ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'READY' COMMENT '세션 상태 (준비/진행중/완료/취소)',
  MODIFY COLUMN started_at TIMESTAMP NULL DEFAULT NULL COMMENT '시작 일시',
  MODIFY COLUMN completed_at TIMESTAMP NULL DEFAULT NULL COMMENT '완료 일시',
  MODIFY COLUMN return_rate DECIMAL(10,6) DEFAULT NULL COMMENT '수익률 (%)',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 챌린지 카테고리 테이블
ALTER TABLE challenge_category COMMENT = '챌린지 카테고리 분류';

-- 챌린지 투자 상품 테이블
ALTER TABLE challenge_instrument COMMENT = '챌린지에서 거래 가능한 투자 상품';

-- 챌린지 일정 테이블
ALTER TABLE challenge_schedule COMMENT = '챌린지 일정 관리';

-- 챌린지 템플릿 테이블
ALTER TABLE challenge_template COMMENT = '챌린지 템플릿 설정';

-- ========================================
-- 2. 거래 및 포트폴리오 관련 테이블 코멘트
-- ========================================

-- 주문 내역 테이블
ALTER TABLE order_history COMMENT = '주문 거래 내역';
ALTER TABLE order_history
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문 고유 식별자',
  MODIFY COLUMN session_id BIGINT NOT NULL COMMENT '챌린지 세션 ID (FK)',
  MODIFY COLUMN instrument_key VARCHAR(10) NOT NULL COMMENT '투자 상품 키 (종목 코드)',
  MODIFY COLUMN side ENUM('BUY','SELL') NOT NULL COMMENT '매매 구분 (매수/매도)',
  MODIFY COLUMN quantity DECIMAL(15,4) NOT NULL COMMENT '주문 수량',
  MODIFY COLUMN order_type ENUM('MARKET','LIMIT') NOT NULL COMMENT '주문 유형 (시장가/지정가)',
  MODIFY COLUMN limit_price DECIMAL(12,4) DEFAULT NULL COMMENT '지정가 (지정가 주문시)',
  MODIFY COLUMN executed_price DECIMAL(12,4) DEFAULT NULL COMMENT '체결 가격',
  MODIFY COLUMN slippage_rate DECIMAL(5,2) DEFAULT '0.00' COMMENT '슬리피지 비율 (%)',
  MODIFY COLUMN status ENUM('PENDING','EXECUTED','CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '주문 상태 (대기/체결/취소)',
  MODIFY COLUMN ordered_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주문 일시',
  MODIFY COLUMN executed_at TIMESTAMP NULL DEFAULT NULL COMMENT '체결 일시',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 포트폴리오 포지션 테이블
ALTER TABLE portfolio_position COMMENT = '포트폴리오 보유 종목';
ALTER TABLE portfolio_position
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '포지션 고유 식별자',
  MODIFY COLUMN session_id BIGINT NOT NULL COMMENT '챌린지 세션 ID (FK)',
  MODIFY COLUMN instrument_key VARCHAR(10) NOT NULL COMMENT '투자 상품 키 (종목 코드)',
  MODIFY COLUMN quantity DECIMAL(15,4) NOT NULL DEFAULT '0.0000' COMMENT '보유 수량',
  MODIFY COLUMN average_price DECIMAL(12,4) NOT NULL DEFAULT '0.0000' COMMENT '평균 매수 가격',
  MODIFY COLUMN total_cost DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '총 투자 금액',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 가격 캔들 데이터 테이블
ALTER TABLE price_candle COMMENT = '주가 캔들 차트 데이터';

-- 리더보드 테이블
ALTER TABLE leaderboard COMMENT = '챌린지 순위표';

-- ========================================
-- 3. 기업 및 시장 데이터 테이블 코멘트
-- ========================================

-- 기업 정보 테이블
ALTER TABLE company COMMENT = '상장 기업 정보';
ALTER TABLE company
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '기업 고유 식별자',
  MODIFY COLUMN symbol VARCHAR(10) NOT NULL COMMENT '종목 코드',
  MODIFY COLUMN name_kr VARCHAR(100) NOT NULL COMMENT '기업명 (한글)',
  MODIFY COLUMN name_en VARCHAR(100) NOT NULL COMMENT '기업명 (영문)',
  MODIFY COLUMN sector VARCHAR(50) DEFAULT NULL COMMENT '업종',
  MODIFY COLUMN market_cap BIGINT DEFAULT NULL COMMENT '시가총액 (원)',
  MODIFY COLUMN market_cap_display VARCHAR(20) DEFAULT NULL COMMENT '시가총액 표시용 (예: 10조원)',
  MODIFY COLUMN logo_path VARCHAR(200) DEFAULT NULL COMMENT '로고 이미지 경로',
  MODIFY COLUMN description_kr TEXT COMMENT '기업 설명 (한글)',
  MODIFY COLUMN description_en TEXT COMMENT '기업 설명 (영문)',
  MODIFY COLUMN exchange VARCHAR(10) DEFAULT 'KRX' COMMENT '거래소 (KRX/NASDAQ/NYSE)',
  MODIFY COLUMN currency VARCHAR(3) DEFAULT 'KRW' COMMENT '거래 통화 (KRW/USD)',
  MODIFY COLUMN is_active TINYINT(1) DEFAULT '1' COMMENT '활성 상태 (1: 활성, 0: 비활성)',
  MODIFY COLUMN popularity_score INT DEFAULT '0' COMMENT '인기도 점수',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 기업 카테고리 테이블
ALTER TABLE company_category COMMENT = '기업 업종 분류';

-- 기업-카테고리 매핑 테이블
ALTER TABLE company_category_mapping COMMENT = '기업-업종 매핑 관계';

-- ========================================
-- 4. 사용자 및 커뮤니티 테이블 코멘트
-- ========================================

-- 사용자 테이블
ALTER TABLE users COMMENT = '사용자 계정 정보';
ALTER TABLE users
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 식별자',
  MODIFY COLUMN email VARCHAR(255) NOT NULL COMMENT '이메일 주소 (로그인 ID)',
  MODIFY COLUMN password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
  MODIFY COLUMN nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
  MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '사용자 권한 (USER, ADMIN)',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- 커뮤니티 게시글 테이블
ALTER TABLE community_post COMMENT = '커뮤니티 게시글';

-- 커뮤니티 댓글 테이블
ALTER TABLE community_comment COMMENT = '커뮤니티 댓글';

-- 리프레시 토큰 테이블
ALTER TABLE refresh_tokens COMMENT = 'JWT 리프레시 토큰';

-- ========================================
-- 5. 적금 관련 테이블 코멘트
-- ========================================

-- 적금 상품 테이블
ALTER TABLE savings_product COMMENT = '적금 상품 정보';

-- 적금 가입 현황 테이블
ALTER TABLE savings_position COMMENT = '적금 가입 현황';