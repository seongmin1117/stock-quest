-- V24__Update_challenge_dates_to_2025.sql
-- 챌린지 날짜를 2024년에서 2025년으로 업데이트

-- 시작일과 종료일이 2024년으로 되어 있는 챌린지들을 2025년으로 업데이트
UPDATE challenge
SET start_date = REPLACE(start_date, '2024-', '2025-'),
    end_date = REPLACE(end_date, '2024-', '2025-')
WHERE start_date LIKE '2024-%' OR end_date LIKE '2024-%';

-- 업데이트된 레코드 확인을 위한 주석
-- 2025년 9월 22일 현재 날짜에 맞춰 챌린지 날짜를 업데이트함