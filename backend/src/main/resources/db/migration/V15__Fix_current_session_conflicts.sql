-- 현재 세션 충돌 문제 해결
-- 기존 활성 세션들을 완료 상태로 변경하여 새로운 세션 시작을 허용

-- 1. 현재 모든 ACTIVE 세션을 COMPLETED 상태로 변경
UPDATE challenge_session 
SET 
    status = 'COMPLETED',
    completed_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'ACTIVE';

-- 2. 변경 결과 확인을 위한 정보 조회 (SELECT는 마이그레이션에서 직접 실행되지 않으므로 주석 처리)
-- SELECT 
--     challenge_id,
--     user_id, 
--     status,
--     started_at,
--     completed_at
-- FROM challenge_session 
-- ORDER BY challenge_id, user_id, created_at;

-- 참고사항:
-- - 이 마이그레이션은 기존 활성 세션들을 강제로 완료 처리합니다
-- - 실제 운영 환경에서는 사용자에게 사전 공지 후 실행해야 합니다
-- - 필요시 백업을 먼저 수행하세요