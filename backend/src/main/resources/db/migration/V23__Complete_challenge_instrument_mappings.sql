-- V23__Complete_challenge_instrument_mappings.sql
-- 챌린지 4-13번의 누락된 상품 매핑 완전 보강
-- 각 챌린지 테마에 맞는 적절한 상품 구성

-- 4. 코로나 폭락장 생존하기 - 코로나로 타격받은 업종 + 방어주
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(4, 'A', 'AAPL', '기술주 A', 'Apple Inc.', 'STOCK'),
(4, 'B', 'MSFT', '기술주 B', 'Microsoft Corporation', 'STOCK'),
(4, 'C', 'JNJ', '의료주 A', 'Johnson & Johnson', 'STOCK'),
(4, 'D', 'PG', '방어주 A', 'Procter & Gamble Co.', 'STOCK'),
(4, 'E', 'KO', '음료주 A', 'The Coca-Cola Company', 'STOCK');

-- 5. 테슬라 신화 도전 - 전기차/청정에너지 중심
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(5, 'A', 'TSLA', '전기차 A', 'Tesla Inc.', 'STOCK'),
(5, 'B', 'AAPL', '기술주 A', 'Apple Inc.', 'STOCK'),
(5, 'C', 'MSFT', '기술주 B', 'Microsoft Corporation', 'STOCK'),
(5, 'D', 'GOOGL', '기술주 C', 'Alphabet Inc.', 'STOCK'),
(5, 'E', 'AMZN', '전자상거래 A', 'Amazon.com Inc.', 'STOCK');

-- 6. 금리인상기 버티기 - 금리 민감주 vs 방어주
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(6, 'A', 'JNJ', '헬스케어 A', 'Johnson & Johnson', 'STOCK'),
(6, 'B', 'PG', '생필품 A', 'Procter & Gamble Co.', 'STOCK'),
(6, 'C', 'KO', '음료주 A', 'The Coca-Cola Company', 'STOCK'),
(6, 'D', 'AAPL', '기술주 A', 'Apple Inc.', 'STOCK'),
(6, 'E', 'MSFT', '기술주 B', 'Microsoft Corporation', 'STOCK');

-- 7. 반도체 사이클 마스터 - 한국 반도체 + 글로벌 반도체
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(7, 'A', '005930', '반도체 A', '삼성전자', 'STOCK'),
(7, 'B', '000660', '반도체 B', 'SK하이닉스', 'STOCK'),
(7, 'C', 'AAPL', '기술주 A', 'Apple Inc.', 'STOCK'),
(7, 'D', 'MSFT', '기술주 B', 'Microsoft Corporation', 'STOCK'),
(7, 'E', 'GOOGL', '기술주 C', 'Alphabet Inc.', 'STOCK');

-- 8. AI 혁명 올라타기 - AI/빅테크 중심
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(8, 'A', 'AAPL', 'AI 기업 A', 'Apple Inc.', 'STOCK'),
(8, 'B', 'MSFT', 'AI 기업 B', 'Microsoft Corporation', 'STOCK'),
(8, 'C', 'GOOGL', 'AI 기업 C', 'Alphabet Inc.', 'STOCK'),
(8, 'D', 'AMZN', '클라우드 A', 'Amazon.com Inc.', 'STOCK'),
(8, 'E', 'TSLA', '혁신기업 A', 'Tesla Inc.', 'STOCK');

-- 9. 배당주 수집가 - 배당 중심 포트폴리오
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(9, 'A', 'JNJ', '배당주 A', 'Johnson & Johnson', 'STOCK'),
(9, 'B', 'PG', '배당주 B', 'Procter & Gamble Co.', 'STOCK'),
(9, 'C', 'KO', '배당주 C', 'The Coca-Cola Company', 'STOCK'),
(9, 'D', 'AAPL', '배당주 D', 'Apple Inc.', 'STOCK'),
(9, 'E', '005930', '배당주 E', '삼성전자', 'STOCK');

-- 10. 주린이 첫걸음 - 초보자용 안전한 대형주
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(10, 'A', 'AAPL', '대형주 A', 'Apple Inc.', 'STOCK'),
(10, 'B', 'MSFT', '대형주 B', 'Microsoft Corporation', 'STOCK'),
(10, 'C', 'JNJ', '안전주 A', 'Johnson & Johnson', 'STOCK'),
(10, 'D', 'PG', '안전주 B', 'Procter & Gamble Co.', 'STOCK'),
(10, 'E', '005930', '안전주 C', '삼성전자', 'STOCK');

-- 11. 가치투자 구루되기 - 가치투자 후보주
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(11, 'A', 'JNJ', '가치주 A', 'Johnson & Johnson', 'STOCK'),
(11, 'B', 'PG', '가치주 B', 'Procter & Gamble Co.', 'STOCK'),
(11, 'C', 'KO', '가치주 C', 'The Coca-Cola Company', 'STOCK'),
(11, 'D', '005930', '가치주 D', '삼성전자', 'STOCK'),
(11, 'E', '000660', '가치주 E', 'SK하이닉스', 'STOCK');

-- 12. 모멘텀 서퍼 - 성장주/모멘텀 중심
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(12, 'A', 'TSLA', '성장주 A', 'Tesla Inc.', 'STOCK'),
(12, 'B', 'AAPL', '성장주 B', 'Apple Inc.', 'STOCK'),
(12, 'C', 'GOOGL', '성장주 C', 'Alphabet Inc.', 'STOCK'),
(12, 'D', 'AMZN', '성장주 D', 'Amazon.com Inc.', 'STOCK'),
(12, 'E', '035720', '성장주 E', '카카오', 'STOCK');

-- 13. 글로벌 다이버시파이어 - 글로벌 분산투자
INSERT IGNORE INTO challenge_instrument (challenge_id, instrument_key, actual_ticker, hidden_name, actual_name, type) VALUES
(13, 'A', 'AAPL', '미국주 A', 'Apple Inc.', 'STOCK'),
(13, 'B', 'MSFT', '미국주 B', 'Microsoft Corporation', 'STOCK'),
(13, 'C', '005930', '한국주 A', '삼성전자', 'STOCK'),
(13, 'D', '000660', '한국주 B', 'SK하이닉스', 'STOCK'),
(13, 'E', '035720', '한국주 C', '카카오', 'STOCK');