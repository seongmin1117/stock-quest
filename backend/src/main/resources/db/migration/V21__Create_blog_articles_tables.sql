-- V11: 투자 교육 블로그 글 관련 테이블 생성
-- articles와 article_tags 테이블을 생성합니다. (categories와 tags 테이블은 이미 존재)

-- 1. 블로그 글 테이블
CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '글 제목',
    slug VARCHAR(250) NOT NULL UNIQUE COMMENT 'SEO 친화적 URL slug',
    summary VARCHAR(500) NOT NULL COMMENT '글 요약',
    content LONGTEXT NOT NULL COMMENT '글 본문 (Markdown)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    category_id BIGINT NOT NULL COMMENT '카테고리 ID',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'DELETED') DEFAULT 'DRAFT' COMMENT '발행 상태',
    featured BOOLEAN DEFAULT FALSE COMMENT '추천 글 여부',
    view_count BIGINT DEFAULT 0 COMMENT '조회수',
    like_count BIGINT DEFAULT 0 COMMENT '좋아요 수',
    comment_count BIGINT DEFAULT 0 COMMENT '댓글 수',
    reading_time_minutes INT COMMENT '예상 읽기 시간 (분)',
    difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') COMMENT '난이도',

    -- SEO 메타데이터
    seo_title VARCHAR(60) COMMENT 'SEO 제목',
    meta_description VARCHAR(160) COMMENT '메타 설명',
    seo_keywords TEXT COMMENT 'SEO 키워드 (쉼표 구분)',
    canonical_url VARCHAR(500) COMMENT '정규 URL',
    og_title VARCHAR(60) COMMENT 'Open Graph 제목',
    og_description VARCHAR(160) COMMENT 'Open Graph 설명',
    og_image_url VARCHAR(500) COMMENT 'Open Graph 이미지 URL',
    twitter_card_type VARCHAR(20) DEFAULT 'summary_large_image' COMMENT 'Twitter Card 타입',
    twitter_title VARCHAR(60) COMMENT 'Twitter 제목',
    twitter_description VARCHAR(160) COMMENT 'Twitter 설명',
    twitter_image_url VARCHAR(500) COMMENT 'Twitter 이미지 URL',
    indexable BOOLEAN DEFAULT TRUE COMMENT '검색 엔진 인덱싱 허용 여부',
    followable BOOLEAN DEFAULT TRUE COMMENT '팔로우 링크 허용 여부',
    schema_type VARCHAR(50) DEFAULT 'Article' COMMENT '구조화된 데이터 스키마 타입',

    published_at TIMESTAMP NULL COMMENT '발행일',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 외래키 제약조건
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,

    -- 인덱스
    INDEX idx_articles_slug (slug),
    INDEX idx_articles_author (author_id),
    INDEX idx_articles_category (category_id),
    INDEX idx_articles_status (status),
    INDEX idx_articles_featured (featured),
    INDEX idx_articles_published (published_at DESC),
    INDEX idx_articles_view_count (view_count DESC),
    INDEX idx_articles_like_count (like_count DESC),
    INDEX idx_articles_difficulty (difficulty),
    INDEX idx_articles_status_published (status, published_at DESC),
    INDEX idx_articles_category_published (category_id, published_at DESC),
    INDEX idx_articles_title_search (title),
    FULLTEXT INDEX idx_articles_content_search (title, summary, content),

    -- 제약조건
    CONSTRAINT chk_articles_view_count CHECK (view_count >= 0),
    CONSTRAINT chk_articles_like_count CHECK (like_count >= 0),
    CONSTRAINT chk_articles_comment_count CHECK (comment_count >= 0),
    CONSTRAINT chk_articles_reading_time CHECK (reading_time_minutes IS NULL OR reading_time_minutes > 0),
    CONSTRAINT chk_articles_published_when_status CHECK (
        (status = 'PUBLISHED' AND published_at IS NOT NULL) OR
        (status != 'PUBLISHED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT = '블로그 글 테이블';

-- 2. 글-태그 연결 테이블
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL COMMENT '글 ID',
    tag_id BIGINT NOT NULL COMMENT '태그 ID',
    tag_order INT NOT NULL COMMENT '태그 순서 (1부터 시작)',
    relevance_score DECIMAL(3,2) DEFAULT 1.00 COMMENT '태그 관련성 점수 (0.00 ~ 1.00)',
    auto_generated BOOLEAN DEFAULT FALSE COMMENT '자동 생성된 태그인지 여부',
    created_by BIGINT NULL COMMENT '태그를 추가한 사용자 ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 복합 기본키
    PRIMARY KEY (article_id, tag_id),

    -- 외래키 제약조건
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,

    -- 인덱스
    INDEX idx_article_tags_tag (tag_id),
    INDEX idx_article_tags_order (article_id, tag_order),
    INDEX idx_article_tags_relevance (relevance_score DESC),
    INDEX idx_article_tags_auto (auto_generated),
    INDEX idx_article_tags_created_by (created_by),
    INDEX idx_article_tags_created_at (created_at),

    -- 제약조건
    CONSTRAINT chk_article_tags_order CHECK (tag_order >= 1),
    CONSTRAINT chk_article_tags_relevance CHECK (relevance_score >= 0.00 AND relevance_score <= 1.00),
    CONSTRAINT uq_article_tags_order UNIQUE (article_id, tag_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT = '글-태그 연결 테이블';

-- 3. 뷰 생성: 발행된 글 목록 (자주 사용되는 쿼리 최적화)
CREATE VIEW published_articles AS
SELECT
    a.*,
    c.name AS category_name,
    c.slug AS category_slug,
    c.color_code AS category_color,
    c.icon AS category_icon,
    u.nickname AS author_nickname
FROM articles a
INNER JOIN categories c ON a.category_id = c.id
INNER JOIN users u ON a.author_id = u.id
WHERE a.status = 'PUBLISHED'
AND a.published_at IS NOT NULL
AND c.active = TRUE;

-- 4. 트리거 생성: 글 발행 시 카테고리 글 수 업데이트
DELIMITER //
CREATE TRIGGER tr_articles_after_insert
AFTER INSERT ON articles
FOR EACH ROW
BEGIN
    IF NEW.status = 'PUBLISHED' THEN
        UPDATE categories
        SET article_count = article_count + 1
        WHERE id = NEW.category_id;
    END IF;
END//

CREATE TRIGGER tr_articles_after_update
AFTER UPDATE ON articles
FOR EACH ROW
BEGIN
    -- 발행 상태 변경 시 카테고리 글 수 업데이트
    IF OLD.status != NEW.status THEN
        IF OLD.status = 'PUBLISHED' AND NEW.status != 'PUBLISHED' THEN
            UPDATE categories
            SET article_count = article_count - 1
            WHERE id = OLD.category_id;
        ELSEIF OLD.status != 'PUBLISHED' AND NEW.status = 'PUBLISHED' THEN
            UPDATE categories
            SET article_count = article_count + 1
            WHERE id = NEW.category_id;
        END IF;
    END IF;

    -- 카테고리 변경 시 글 수 업데이트
    IF OLD.category_id != NEW.category_id AND NEW.status = 'PUBLISHED' THEN
        UPDATE categories
        SET article_count = article_count - 1
        WHERE id = OLD.category_id;

        UPDATE categories
        SET article_count = article_count + 1
        WHERE id = NEW.category_id;
    END IF;
END//

CREATE TRIGGER tr_articles_after_delete
AFTER DELETE ON articles
FOR EACH ROW
BEGIN
    IF OLD.status = 'PUBLISHED' THEN
        UPDATE categories
        SET article_count = article_count - 1
        WHERE id = OLD.category_id;
    END IF;
END//

-- 5. 트리거 생성: 태그 사용 시 사용 횟수 업데이트
CREATE TRIGGER tr_article_tags_after_insert
AFTER INSERT ON article_tags
FOR EACH ROW
BEGIN
    UPDATE tags
    SET usage_count = usage_count + 1,
        popular = CASE WHEN usage_count + 1 >= 10 THEN TRUE ELSE popular END
    WHERE id = NEW.tag_id;
END//

CREATE TRIGGER tr_article_tags_after_delete
AFTER DELETE ON article_tags
FOR EACH ROW
BEGIN
    UPDATE tags
    SET usage_count = GREATEST(0, usage_count - 1),
        popular = CASE WHEN usage_count - 1 < 10 THEN FALSE ELSE popular END
    WHERE id = OLD.tag_id;
END//

DELIMITER ;

-- 완료 메시지
SELECT 'V11: 블로그 글 관련 테이블 생성 완료' AS message;