-- MIGRATION TEMPLATE FOR STOCK QUEST PROJECT
-- 파일명: V[NUMBER]__[Description].sql (예: V25__Create_new_feature_table.sql)
--
-- ⚠️  중요: 이 템플릿을 복사하여 새로운 마이그레이션을 생성하세요
-- ⚠️  항상 utf8mb4 charset을 사용하여 한국어 텍스트 지원을 보장하세요
--
-- 작성자: [작성자명]
-- 작성일: [YYYY-MM-DD]
-- 목적: [마이그레이션의 목적과 배경 설명]

-- =============================================================================
-- 1. CREATE TABLE 구문 템플릿 (⚠️ charset 필수!)
-- =============================================================================

-- CREATE TABLE example_table (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(255) NOT NULL COMMENT '이름',
--     description TEXT COMMENT '설명',
--     status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '상태',
--
--     -- 표준 timestamp 컬럼
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--
--     -- 인덱스
--     INDEX idx_example_name (name),
--     INDEX idx_example_status (status),
--     INDEX idx_example_created (created_at)
--
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='예시 테이블';

-- =============================================================================
-- 2. ALTER TABLE 구문 템플릿
-- =============================================================================

-- 컬럼 추가
-- ALTER TABLE existing_table
--     ADD COLUMN new_column VARCHAR(255) NULL COMMENT '새 컬럼',
--     ADD INDEX idx_existing_new_column (new_column);

-- 컬럼 수정
-- ALTER TABLE existing_table
--     MODIFY COLUMN existing_column VARCHAR(500) NOT NULL COMMENT '수정된 컬럼';

-- 외래키 추가
-- ALTER TABLE child_table
--     ADD CONSTRAINT fk_child_parent
--     FOREIGN KEY (parent_id) REFERENCES parent_table(id) ON DELETE CASCADE;

-- =============================================================================
-- 3. 데이터 삽입 템플릿 (한국어 지원)
-- =============================================================================

-- INSERT INTO example_table (name, description, status) VALUES
-- ('한국어 이름', '한국어 설명입니다. 특수문자도 지원됩니다: ~!@#$%^&*()', 'ACTIVE'),
-- ('English Name', 'English description with special chars: ~!@#$%^&*()', 'ACTIVE');

-- =============================================================================
-- 4. 인덱스 생성 템플릿
-- =============================================================================

-- 단일 컬럼 인덱스
-- CREATE INDEX idx_table_column ON table_name(column_name);

-- 복합 인덱스 (자주 함께 검색되는 컬럼들)
-- CREATE INDEX idx_table_multi ON table_name(column1, column2, column3);

-- 유니크 인덱스
-- CREATE UNIQUE INDEX idx_table_unique ON table_name(unique_column);

-- 조건부 인덱스 (WHERE 절 포함)
-- CREATE INDEX idx_table_conditional ON table_name(column) WHERE status = 'ACTIVE';

-- =============================================================================
-- 5. 뷰 생성 템플릿
-- =============================================================================

-- CREATE VIEW view_name AS
-- SELECT
--     t1.id,
--     t1.name,
--     t1.status,
--     t2.related_data
-- FROM table1 t1
-- INNER JOIN table2 t2 ON t1.id = t2.table1_id
-- WHERE t1.status = 'ACTIVE';

-- =============================================================================
-- 6. 트리거 생성 템플릿 (필요한 경우만)
-- =============================================================================

-- DELIMITER //
-- CREATE TRIGGER tr_table_after_insert
-- AFTER INSERT ON target_table
-- FOR EACH ROW
-- BEGIN
--     -- 트리거 로직
--     UPDATE related_table
--     SET count = count + 1
--     WHERE id = NEW.related_id;
-- END//
-- DELIMITER ;

-- =============================================================================
-- 7. 체크리스트 및 검증
-- =============================================================================

-- ✅ 체크리스트:
-- [ ] CREATE TABLE에 ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 추가
-- [ ] 모든 VARCHAR/TEXT 컬럼에 적절한 COMMENT 추가
-- [ ] 필요한 인덱스 모두 생성 (성능 고려)
-- [ ] 외래키 제약조건 올바르게 설정
-- [ ] 한국어 텍스트 테스트 데이터로 검증
-- [ ] 롤백 시나리오 고려

-- 검증 쿼리 (마이그레이션 적용 후 실행)
-- SHOW CREATE TABLE your_table_name;
-- SELECT * FROM your_table_name WHERE name LIKE '%한국어%';

-- =============================================================================
-- 8. 롤백 가이드
-- =============================================================================

-- 이 마이그레이션을 롤백해야 하는 경우:
-- 1. DROP TABLE statements (CREATE TABLE의 역순)
-- 2. ALTER TABLE statements (ADD의 경우 DROP, MODIFY의 경우 원래 상태로)
-- 3. DROP INDEX statements
-- 4. DELETE statements (INSERT의 경우)

-- 롤백 예시:
-- DROP TABLE IF EXISTS example_table;
-- ALTER TABLE existing_table DROP COLUMN new_column;
-- DROP INDEX IF EXISTS idx_table_column ON table_name;

-- =============================================================================
-- 완료 메시지
-- =============================================================================

-- 마이그레이션 완료 확인 메시지
SELECT 'V[NUMBER]: [마이그레이션 설명] 완료' AS migration_status;

-- =============================================================================
-- 추가 참고사항
-- =============================================================================

-- 📝 마이그레이션 작성 시 주의사항:
-- 1. 파일명은 순차적으로 번호를 매기고 설명을 포함하세요
-- 2. 모든 CREATE TABLE에는 charset 지정이 필수입니다
-- 3. 한국어 텍스트가 포함된 경우 반드시 테스트하세요
-- 4. 큰 데이터 변경의 경우 배치 처리를 고려하세요
-- 5. 프로덕션 적용 전에 개발/스테이징 환경에서 충분히 테스트하세요
-- 6. 롤백 계획을 미리 수립하세요

-- 🔗 관련 문서:
-- - /backend/CLAUDE.md (백엔드 가이드)
-- - /CLAUDE.md (프로젝트 전체 가이드)
-- - MySQL 8.0 utf8mb4 공식 문서