# 📋 StockQuest 프로젝트 컨텍스트

## 🎯 프로젝트 미션

**"안전한 환경에서 실제와 같은 투자 경험을 제공하여 금융 리터러시를 향상시킨다"**

### 핵심 가치
1. **교육**: 투자 원리와 리스크 관리 학습
2. **안전성**: 실제 자금 없이 경험 습득
3. **현실성**: 실제 시장 데이터 기반 시뮬레이션
4. **커뮤니티**: 경험 공유와 협력 학습

## 🏗️ 시스템 아키텍처 철학

### Backend: Hexagonal Architecture
```
External World ← Adapter ← Application ← Domain → Application → Adapter → External World
```

**핵심 원칙**:
- Domain: 순수 비즈니스 로직 (프레임워크 독립)
- Application: 유스케이스 구현 (트랜잭션 경계)
- Adapter: 외부 시스템 연동 (DB, API, Web)

### Frontend: Feature-Sliced Design
```
app/ (Pages & Routing)
├── widgets/ (Complex UI Blocks)
├── features/ (User Features)
├── entities/ (Business Entities)
└── shared/ (Reusable Resources)
```

**핵심 원칙**:
- 계층 간 단방향 의존성
- 기능별 모듈화
- 재사용성 극대화

## 📊 도메인 모델

### Core Entities

#### 1. User (사용자)
- **역할**: 플랫폼 참가자
- **속성**: email, nickname, encrypted_password
- **관계**: Challenge Session과 1:N

#### 2. Challenge (챌린지)
- **역할**: 투자 게임의 단위
- **속성**: title, description, start_date, end_date, speed_factor
- **특징**: 과거 데이터를 압축 재생 (10-100배속)

#### 3. Challenge Session (챌린지 세션)
- **역할**: 사용자의 챌린지 참여 인스턴스
- **속성**: user_id, challenge_id, seed_balance, current_balance
- **상태**: ACTIVE, COMPLETED, CLOSED

#### 4. Order (주문)
- **역할**: 거래 요청
- **타입**: MARKET, LIMIT
- **방향**: BUY, SELL
- **상태**: PENDING, FILLED, CANCELLED

#### 5. Portfolio Position (포트폴리오 포지션)  
- **역할**: 보유 자산 현황
- **속성**: instrument_key, quantity, average_price, total_cost

#### 6. Price Candle (가격 캔들)
- **역할**: 시장 데이터
- **속성**: symbol, timestamp, open, high, low, close, volume

## 🎮 사용자 여정 (User Journey)

### 1. 온보딩
1. 회원가입 → 이메일 인증
2. 프로필 설정 → 닉네임, 투자 경험
3. 튜토리얼 → 기본 거래 방법 학습

### 2. 챌린지 참여
1. 챌린지 목록 조회 → 필터링 (기간, 난이도)
2. 챌린지 상세 정보 → 참여 조건, 상품 종류
3. 챌린지 시작 → 시드머니 100만원 지급

### 3. 거래 실행
1. 시장 분석 → 차트, 지표 (회사명 숨김)
2. 주문 접수 → 수량, 타입, 가격 설정
3. 체결 확인 → 슬리피지 반영된 실제 체결가

### 4. 포트폴리오 관리
1. 실시간 모니터링 → 손익, 수익률
2. 리밸런싱 → 포지션 조정
3. 리스크 관리 → 손절, 익절

### 5. 챌린지 종료
1. 종료 결정 → 수동 또는 자동 종료
2. 결과 분석 → 최종 수익률, 거래 내역
3. 티커 공개 → 실제 회사명 공개
4. 리더보드 → 순위 확인

### 6. 커뮤니티 참여
1. 전략 공유 → 성공/실패 사례
2. 토론 참여 → 의견 교환
3. 멘토링 → 경험자와 초보자 연결

## 🔄 비즈니스 플로우

### 챌린지 생성 플로우
```
Admin → Challenge 생성 → Instrument 설정 → 기간 설정 → 활성화
```

### 거래 실행 플로우
```
User → 주문 접수 → 잔고 확인 → 슬리피지 적용 → 체결 → 포지션 업데이트 → 리더보드 갱신
```

### 시뮬레이션 플로우
```
Historical Data → Time Compression → Price Update → Portfolio Valuation → Leaderboard Update
```

## 💰 경제 시스템

### 시드머니 시스템
- 초기 자금: 1,000,000원 (고정)
- 추가 입금 불가 (공정성 보장)
- 마이너스 잔고 허용 안함

### 거래 수수료
- 현재: 수수료 없음 (교육 목적)
- 향후: 실제 증권사 수준 수수료 도입 검토

### 슬리피지 시뮬레이션
- 시장가 주문: 1-2% 슬리피지 적용
- 지정가 주문: 지정가 터치 시 즉시 체결
- 대량 거래: 추가 슬리피지 적용

### 리더보드 계산
- 기준: 총 수익률 (%)
- 갱신: 실시간 (Redis 캐시)
- 순위: 동점자 처리 (가입일 순)

## 📈 데이터 소스

### 주식 데이터
- **Primary**: Yahoo Finance API
- **Backup**: Alpha Vantage, IEX Cloud
- **Update**: Daily EOD 데이터
- **History**: 최근 5년간 일봉 데이터

### 안전자산 데이터
- 예금: 3.0-3.5% (연이율)
- 국고채: 4.0-4.5% (3년 만기)
- 회사채: 5.0-6.0% (신용등급별)

## 🎯 성공 지표 (KPI)

### 사용자 참여도
- DAU (Daily Active Users): 목표 1,000명
- MAU (Monthly Active Users): 목표 10,000명
- 평균 세션 시간: 목표 15분+
- 챌린지 완주율: 목표 60%+

### 교육 효과
- 투자 지식 향상도: 설문 조사
- 리스크 인식 개선: Before/After 비교
- 실제 투자 연계율: 추적 조사

### 기술 지표
- 시스템 가용성: 99.9%
- API 응답 시간: 200ms 이하
- 데이터 정확성: 99.99%

## 🔮 로드맵

### Phase 1: MVP (v1.0) - 완료
- [x] 기본 인증 시스템
- [x] 챌린지 시스템
- [x] 거래 시스템
- [x] 포트폴리오 추적
- [x] 리더보드

### Phase 2: 실시간 (v1.1) - 진행중
- [ ] WebSocket 실시간 업데이트
- [ ] 실시간 알림 시스템
- [ ] 채팅 시스템
- [ ] 모바일 대응

### Phase 3: AI 기능 (v1.2)
- [ ] AI 투자 조언
- [ ] 패턴 분석
- [ ] 리스크 평가
- [ ] 개인화 추천

### Phase 4: 소셜 (v1.3)
- [ ] 소셜 로그인
- [ ] 팔로우 시스템
- [ ] 투자 클럽
- [ ] 경쟁전

### Phase 5: 확장 (v2.0)
- [ ] 해외 주식
- [ ] 파생상품
- [ ] 포트폴리오 백테스팅
- [ ] B2B 교육 솔루션

## 🎨 브랜딩

### 디자인 철학
- **신뢰성**: 전문적이고 안정적인 인상
- **접근성**: 초보자도 쉽게 사용 가능
- **게임화**: 재미있고 engaging한 경험

### 컬러 팔레트
- Primary: Blue (#1976D2) - 신뢰, 전문성
- Secondary: Green (#388E3C) - 수익, 성장  
- Accent: Orange (#F57C00) - 주의, 액션
- Gray: (#616161) - 텍스트, 중성

### 톤 앤 매너
- **친근하지만 전문적**
- **교육적이지만 재미있게**
- **안전하지만 도전적**

---

**🔄 업데이트 주기**: 매월 검토 및 업데이트  
**📅 마지막 업데이트**: 2024-09-11  
**👤 작성자**: Claude AI Assistant