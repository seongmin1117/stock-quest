-- V26: 블로그 관련 테이블과 컬럼에 한국어 코멘트 수정
-- 작성일: 2025-09-22
-- 목적: articles, article_tags, tags 테이블의 UTF-8 인코딩 문제 해결 및 올바른 한국어 코멘트 적용

-- ========================================
-- 1. 블로그 글 테이블 (articles) 코멘트 수정
-- ========================================

-- 테이블 코멘트 수정
ALTER TABLE articles COMMENT = '블로그 글 테이블';

-- 컬럼 코멘트 수정
ALTER TABLE articles
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '글 고유 식별자',
  MODIFY COLUMN title VARCHAR(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '글 제목',
  MODIFY COLUMN slug VARCHAR(250) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SEO 친화적 URL slug',
  MODIFY COLUMN summary VARCHAR(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '글 요약',
  MODIFY COLUMN content LONGTEXT COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '글 본문 (Markdown)',
  MODIFY COLUMN author_id BIGINT NOT NULL COMMENT '작성자 ID',
  MODIFY COLUMN category_id BIGINT NOT NULL COMMENT '카테고리 ID',
  MODIFY COLUMN status ENUM('DRAFT','PUBLISHED','ARCHIVED','DELETED') COLLATE utf8mb4_unicode_ci DEFAULT 'DRAFT' COMMENT '발행 상태',
  MODIFY COLUMN featured TINYINT(1) DEFAULT '0' COMMENT '추천 글 여부',
  MODIFY COLUMN view_count BIGINT DEFAULT '0' COMMENT '조회수',
  MODIFY COLUMN like_count BIGINT DEFAULT '0' COMMENT '좋아요 수',
  MODIFY COLUMN comment_count BIGINT DEFAULT '0' COMMENT '댓글 수',
  MODIFY COLUMN reading_time_minutes INT DEFAULT NULL COMMENT '예상 읽기 시간 (분)',
  MODIFY COLUMN difficulty ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT') COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '난이도',
  MODIFY COLUMN seo_title VARCHAR(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO 제목',
  MODIFY COLUMN meta_description VARCHAR(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '메타 설명',
  MODIFY COLUMN seo_keywords TEXT COLLATE utf8mb4_unicode_ci COMMENT 'SEO 키워드 (쉼표 구분)',
  MODIFY COLUMN canonical_url VARCHAR(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '정규 URL',
  MODIFY COLUMN og_title VARCHAR(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Open Graph 제목',
  MODIFY COLUMN og_description VARCHAR(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Open Graph 설명',
  MODIFY COLUMN og_image_url VARCHAR(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Open Graph 이미지 URL',
  MODIFY COLUMN twitter_card_type VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT 'summary_large_image' COMMENT 'Twitter Card 타입',
  MODIFY COLUMN twitter_title VARCHAR(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Twitter 제목',
  MODIFY COLUMN twitter_description VARCHAR(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Twitter 설명',
  MODIFY COLUMN twitter_image_url VARCHAR(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Twitter 이미지 URL',
  MODIFY COLUMN indexable TINYINT(1) DEFAULT '1' COMMENT '검색 엔진 인덱싱 허용 여부',
  MODIFY COLUMN followable TINYINT(1) DEFAULT '1' COMMENT '팔로우 링크 허용 여부',
  MODIFY COLUMN schema_type VARCHAR(50) COLLATE utf8mb4_unicode_ci DEFAULT 'Article' COMMENT '구조화된 데이터 스키마 타입',
  MODIFY COLUMN published_at TIMESTAMP NULL DEFAULT NULL COMMENT '발행일',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';

-- ========================================
-- 2. 글-태그 연결 테이블 (article_tags) 코멘트 수정
-- ========================================

-- 테이블 코멘트 수정
ALTER TABLE article_tags COMMENT = '글-태그 연결 테이블';

-- 컬럼 코멘트 수정
ALTER TABLE article_tags
  MODIFY COLUMN article_id BIGINT NOT NULL COMMENT '글 ID',
  MODIFY COLUMN tag_id BIGINT NOT NULL COMMENT '태그 ID',
  MODIFY COLUMN tag_order INT NOT NULL COMMENT '태그 순서 (1부터 시작)',
  MODIFY COLUMN relevance_score DECIMAL(3,2) DEFAULT '1.00' COMMENT '태그 관련성 점수 (0.00 ~ 1.00)',
  MODIFY COLUMN auto_generated TINYINT(1) DEFAULT '0' COMMENT '자동 생성된 태그인지 여부',
  MODIFY COLUMN created_by BIGINT DEFAULT NULL COMMENT '태그를 추가한 사용자 ID',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시';

-- ========================================
-- 3. 블로그 태그 테이블 (tags) 코멘트 수정
-- ========================================

-- 테이블 코멘트 수정
ALTER TABLE tags COMMENT = '블로그 태그 테이블';

-- 컬럼 코멘트 수정
ALTER TABLE tags
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '태그 고유 식별자',
  MODIFY COLUMN name VARCHAR(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '태그 이름',
  MODIFY COLUMN slug VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SEO 친화적 URL slug',
  MODIFY COLUMN description VARCHAR(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '태그 설명',
  MODIFY COLUMN color_code VARCHAR(7) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '태그 색상 (#RRGGBB)',
  MODIFY COLUMN type ENUM('STOCK','BOND','FUND','CRYPTO','REAL_ESTATE','ECONOMICS','STRATEGY','ANALYSIS','NEWS','TUTORIAL','BEGINNER','ADVANCED','GENERAL') COLLATE utf8mb4_unicode_ci DEFAULT 'GENERAL' COMMENT '태그 타입',
  MODIFY COLUMN active TINYINT(1) DEFAULT '1' COMMENT '활성화 여부',
  MODIFY COLUMN usage_count BIGINT DEFAULT '0' COMMENT '이 태그를 사용하는 글 수 (캐시)',
  MODIFY COLUMN popular TINYINT(1) DEFAULT '0' COMMENT '인기 태그 여부',
  MODIFY COLUMN suggested TINYINT(1) DEFAULT '0' COMMENT '추천 태그 여부',
  MODIFY COLUMN weight DECIMAL(3,1) DEFAULT '1.0' COMMENT '태그 가중치 (0.0 ~ 10.0)',
  MODIFY COLUMN created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  MODIFY COLUMN updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 일시';