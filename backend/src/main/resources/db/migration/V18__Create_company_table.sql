-- Create companies table for Korean market stocks
CREATE TABLE company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL UNIQUE,
    name_kr VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    sector VARCHAR(50),
    market_cap BIGINT,
    market_cap_display VARCHAR(20),
    logo_path VARCHAR(200),
    description_kr TEXT,
    description_en TEXT,
    exchange VARCHAR(10) DEFAULT 'KRX',
    currency VARCHAR(3) DEFAULT 'KRW',
    is_active BOOLEAN DEFAULT TRUE,
    popularity_score INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_company_symbol (symbol),
    INDEX idx_company_sector (sector),
    INDEX idx_company_popularity (popularity_score DESC),
    INDEX idx_company_active (is_active),
    INDEX idx_company_search_kr (name_kr),
    INDEX idx_company_search_en (name_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create company categories table
CREATE TABLE company_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id VARCHAR(20) NOT NULL UNIQUE,
    name_kr VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description_kr TEXT,
    description_en TEXT,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_category_sort (sort_order),
    INDEX idx_category_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create company-category mapping table
CREATE TABLE company_category_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    category_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES company_category(category_id) ON DELETE CASCADE,
    UNIQUE KEY uk_company_category (company_id, category_id),
    INDEX idx_mapping_company (company_id),
    INDEX idx_mapping_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert company categories
INSERT INTO company_category (category_id, name_kr, name_en, description_kr, description_en, sort_order) VALUES
('tech', '기술', 'Technology', 'IT 서비스, 소프트웨어, 인터넷', 'IT services, software, internet', 1),
('semiconductor', '반도체', 'Semiconductor', '반도체, 메모리, 시스템반도체', 'Semiconductors, memory, system semiconductors', 2),
('automotive', '자동차', 'Automotive', '자동차, 자동차부품', 'Automobiles, auto parts', 3),
('finance', '금융', 'Finance', '은행, 증권, 보험', 'Banking, securities, insurance', 4),
('chemical', '화학', 'Chemical', '화학, 정유, 배터리', 'Chemicals, refining, batteries', 5),
('steel', '철강', 'Steel', '철강, 금속', 'Steel, metals', 6),
('retail', '유통', 'Retail', '유통, 쇼핑몰, 전자상거래', 'Retail, shopping malls, e-commerce', 7),
('energy', '에너지', 'Energy', '전력, 가스, 신재생에너지', 'Power, gas, renewable energy', 8),
('bio', '바이오', 'Bio', '제약, 바이오', 'Pharmaceuticals, biotechnology', 9),
('entertainment', '엔터테인먼트', 'Entertainment', '게임, 미디어, 엔터테인먼트', 'Gaming, media, entertainment', 10);

-- Insert top Korean companies with comprehensive data
INSERT INTO company (symbol, name_kr, name_en, sector, market_cap, market_cap_display, logo_path, description_kr, description_en, popularity_score) VALUES
-- Tier 1: 대형주 (Large Cap)
('005930', '삼성전자', 'Samsung Electronics', '반도체', 360000000000000, '360조원', '/logos/samsung.png', '글로벌 반도체, 스마트폰 제조업체', 'Global semiconductor and smartphone manufacturer', 100),
('000660', 'SK하이닉스', 'SK Hynix', '반도체', 70000000000000, '70조원', '/logos/sk-hynix.png', '메모리 반도체 전문기업', 'Memory semiconductor specialist', 95),
('373220', 'LG에너지솔루션', 'LG Energy Solution', '배터리', 65000000000000, '65조원', '/logos/lg-energy.png', '배터리 및 에너지 저장장치 제조업체', 'Battery and energy storage system manufacturer', 90),

-- Tier 2: IT 서비스
('035720', '카카오', 'Kakao', 'IT서비스', 25000000000000, '25조원', '/logos/kakao.png', '모바일 플랫폼 및 인터넷 서비스', 'Mobile platform and internet services', 88),
('035420', '네이버', 'Naver', 'IT서비스', 35000000000000, '35조원', '/logos/naver.png', '검색엔진 및 IT 플랫폼 서비스', 'Search engine and IT platform services', 85),

-- Tier 3: 자동차
('005380', '현대차', 'Hyundai Motor', '자동차', 40000000000000, '40조원', '/logos/hyundai.png', '글로벌 자동차 제조업체', 'Global automobile manufacturer', 82),
('000270', '기아', 'Kia', '자동차', 30000000000000, '30조원', '/logos/kia.png', '자동차 제조 및 판매', 'Automobile manufacturing and sales', 78),
('012330', '현대모비스', 'Hyundai Mobis', '자동차부품', 25000000000000, '25조원', '/logos/hyundai-mobis.png', '자동차 부품 전문업체', 'Automotive parts specialist', 75),

-- Tier 4: 화학/에너지
('051910', 'LG화학', 'LG Chem', '화학', 45000000000000, '45조원', '/logos/lg-chem.png', '종합화학 및 배터리소재', 'Comprehensive chemicals and battery materials', 80),
('096770', 'SK이노베이션', 'SK Innovation', '정유', 20000000000000, '20조원', '/logos/sk-innovation.png', '정유 및 화학사업', 'Refining and chemical business', 72),

-- Tier 5: 금융
('105560', 'KB금융지주', 'KB Financial Group', '금융지주', 15000000000000, '15조원', '/logos/kb.png', '종합금융지주회사', 'Comprehensive financial holding company', 70),
('055550', '신한지주', 'Shinhan Financial Group', '금융지주', 12000000000000, '12조원', '/logos/shinhan.png', '금융지주회사', 'Financial holding company', 68),

-- Tier 6: 철강/소재
('005490', '포스코홀딩스', 'POSCO Holdings', '철강', 18000000000000, '18조원', '/logos/posco.png', '철강 제조 및 소재', 'Steel manufacturing and materials', 65),

-- Tier 7: 바이오/제약
('068270', '셀트리온', 'Celltrion', '바이오', 22000000000000, '22조원', '/logos/celltrion.png', '바이오의약품 개발 및 제조', 'Biopharmaceutical development and manufacturing', 75),
('207940', '삼성바이오로직스', 'Samsung Biologics', '바이오', 35000000000000, '35조원', '/logos/samsung-bio.png', '바이오의약품 위탁개발생산', 'Biopharmaceutical contract development and manufacturing', 73),

-- Tier 8: 게임/엔터테인먼트
('036570', '엔씨소프트', 'NCSOFT', '게임', 8000000000000, '8조원', '/logos/ncsoft.png', '온라인 게임 개발 및 서비스', 'Online game development and services', 60),
('251270', '넷마블', 'Netmarble', '게임', 6000000000000, '6조원', '/logos/netmarble.png', '모바일 게임 개발', 'Mobile game development', 58),

-- Tier 9: 유통/이커머스
('282330', 'BGF리테일', 'BGF Retail', '유통', 3000000000000, '3조원', '/logos/bgf.png', '편의점 프랜차이즈', 'Convenience store franchise', 55),
('009830', '한화솔루션', 'Hanwha Solutions', '화학', 8000000000000, '8조원', '/logos/hanwha.png', '태양광 및 화학소재', 'Solar and chemical materials', 62),

-- Tier 10: 항공/운송
('003490', '대한항공', 'Korean Air', '항공운송', 4000000000000, '4조원', '/logos/korean-air.png', '국제항공운송업', 'International air transport', 52);

-- Create company-category mappings
INSERT INTO company_category_mapping (company_id, category_id) VALUES
-- Samsung Electronics - tech & semiconductor
((SELECT id FROM company WHERE symbol = '005930'), 'tech'),
((SELECT id FROM company WHERE symbol = '005930'), 'semiconductor'),

-- SK Hynix - semiconductor
((SELECT id FROM company WHERE symbol = '000660'), 'semiconductor'),

-- LG Energy Solution - chemical (battery)
((SELECT id FROM company WHERE symbol = '373220'), 'chemical'),

-- Kakao - tech
((SELECT id FROM company WHERE symbol = '035720'), 'tech'),

-- Naver - tech
((SELECT id FROM company WHERE symbol = '035420'), 'tech'),

-- Hyundai Motor - automotive
((SELECT id FROM company WHERE symbol = '005380'), 'automotive'),

-- Kia - automotive
((SELECT id FROM company WHERE symbol = '000270'), 'automotive'),

-- Hyundai Mobis - automotive
((SELECT id FROM company WHERE symbol = '012330'), 'automotive'),

-- LG Chem - chemical
((SELECT id FROM company WHERE symbol = '051910'), 'chemical'),

-- SK Innovation - chemical & energy
((SELECT id FROM company WHERE symbol = '096770'), 'chemical'),
((SELECT id FROM company WHERE symbol = '096770'), 'energy'),

-- KB Financial - finance
((SELECT id FROM company WHERE symbol = '105560'), 'finance'),

-- Shinhan Financial - finance
((SELECT id FROM company WHERE symbol = '055550'), 'finance'),

-- POSCO Holdings - steel
((SELECT id FROM company WHERE symbol = '005490'), 'steel'),

-- Celltrion - bio
((SELECT id FROM company WHERE symbol = '068270'), 'bio'),

-- Samsung Biologics - bio
((SELECT id FROM company WHERE symbol = '207940'), 'bio'),

-- NCSOFT - entertainment
((SELECT id FROM company WHERE symbol = '036570'), 'entertainment'),

-- Netmarble - entertainment
((SELECT id FROM company WHERE symbol = '251270'), 'entertainment'),

-- BGF Retail - retail
((SELECT id FROM company WHERE symbol = '282330'), 'retail'),

-- Hanwha Solutions - chemical & energy
((SELECT id FROM company WHERE symbol = '009830'), 'chemical'),
((SELECT id FROM company WHERE symbol = '009830'), 'energy'),

-- Korean Air - transport (no category defined, would need to add)
((SELECT id FROM company WHERE symbol = '003490'), 'energy'); -- Using energy as placeholder