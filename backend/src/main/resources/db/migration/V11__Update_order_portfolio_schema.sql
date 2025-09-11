-- 주문 및 포트폴리오 테이블 스키마 업데이트

-- order_history 테이블에 created_at, updated_at 추가
ALTER TABLE order_history 
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- portfolio_position 테이블에 타임스탬프 컬럼 추가
ALTER TABLE portfolio_position 
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;