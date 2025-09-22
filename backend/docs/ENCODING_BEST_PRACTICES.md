# UTF-8 Encoding Best Practices for Stock Quest

**한국어 텍스트 지원을 위한 종합 인코딩 가이드**

## 📋 목차

1. [개요](#개요)
2. [필수 설정 체크리스트](#필수-설정-체크리스트)
3. [데이터베이스 설정](#데이터베이스-설정)
4. [애플리케이션 설정](#애플리케이션-설정)
5. [마이그레이션 작성 가이드](#마이그레이션-작성-가이드)
6. [개발 환경 설정](#개발-환경-설정)
7. [테스트 가이드](#테스트-가이드)
8. [문제 해결](#문제-해결)
9. [모니터링](#모니터링)

---

## 개요

Stock Quest 프로젝트에서는 **한국어 텍스트 완벽 지원**을 위해 시스템 전반에 걸쳐 **UTF-8 (utf8mb4)** 인코딩을 사용합니다.

### 핵심 원칙
- ✅ **일관성**: 모든 계층에서 동일한 인코딩 사용
- ✅ **명시성**: 모든 설정을 명시적으로 지정
- ✅ **검증**: 모든 변경사항은 한국어 텍스트로 테스트
- ✅ **모니터링**: 인코딩 설정 지속적 감시

---

## 필수 설정 체크리스트

### ✅ Database Level
- [ ] MySQL 서버 charset: `utf8mb4`
- [ ] MySQL 서버 collation: `utf8mb4_unicode_ci`
- [ ] 데이터베이스 charset: `utf8mb4`
- [ ] 모든 테이블 charset: `utf8mb4`

### ✅ Connection Level
- [ ] JDBC URL에 `useUnicode=true&characterEncoding=UTF-8` 포함
- [ ] HikariCP `connection-init-sql` 설정
- [ ] Connection pool charset 초기화

### ✅ Application Level
- [ ] Spring Boot default charset: UTF-8
- [ ] HTTP 응답 Content-Type: `application/json;charset=UTF-8`
- [ ] 모든 환경(dev, test, prod)에서 동일 설정

### ✅ Migration Level
- [ ] 모든 CREATE TABLE에 charset 명시
- [ ] 마이그레이션 템플릿 사용
- [ ] 변경 후 한국어 텍스트 테스트

---

## 데이터베이스 설정

### MySQL Server 설정

**Docker Compose (docker-compose.yml)**
```yaml
services:
  mysql:
    image: mysql:8.0
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    environment:
      MYSQL_DATABASE: stockquest
      MYSQL_USER: stockquest
      MYSQL_PASSWORD: stockquest123
      MYSQL_ROOT_PASSWORD: root123
    ports:
      - "3306:3306"
```

**MySQL Configuration (my.cnf)**
```ini
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
init-connect = 'SET NAMES utf8mb4'

[mysql]
default-character-set = utf8mb4

[client]
default-character-set = utf8mb4
```

### Database & Table 생성

**데이터베이스 생성**
```sql
CREATE DATABASE stockquest
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

**테이블 생성 (필수 템플릿)**
```sql
CREATE TABLE example_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    korean_name VARCHAR(255) NOT NULL COMMENT '한국어 이름',
    description TEXT COMMENT '설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 기존 테이블 변경

**Database Charset 변경**
```sql
ALTER DATABASE stockquest
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

**Table Charset 변경**
```sql
ALTER TABLE table_name
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

**Column Charset 변경**
```sql
ALTER TABLE table_name
MODIFY COLUMN column_name VARCHAR(255)
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

## 애플리케이션 설정

### Spring Boot Configuration

**application.yml (모든 환경 공통)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"

  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

  jpa:
    properties:
      hibernate:
        connection:
          CharSet: utf8mb4
          characterEncoding: utf8
          useUnicode: true
```

**Production 환경 (application-prod.yml)**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:mysql://localhost:3306/stockquest?serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&useSSL=true}
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
      validation-timeout: 5000
      connection-test-query: "SELECT 1"
```

### HTTP Response 설정

**RestController 설정**
```java
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class ExampleController {

    @GetMapping("/korean-test")
    public ResponseEntity<Map<String, String>> getKoreanText() {
        Map<String, String> response = Map.of(
            "message", "한국어 텍스트 테스트",
            "company", "삼성전자"
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
```

---

## 마이그레이션 작성 가이드

### 템플릿 사용 (필수)

**위치**: `/src/main/resources/db/migration/TEMPLATE__Migration_Template.sql`

**새 마이그레이션 작성 단계**:
1. 템플릿 파일 복사
2. 파일명을 `V[NUMBER]__[Description].sql`로 변경
3. 템플릿 내용 수정
4. 한국어 텍스트로 테스트

### CREATE TABLE 예시

```sql
-- ✅ 올바른 예시
CREATE TABLE korean_test_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    korean_name VARCHAR(255) NOT NULL COMMENT '한국어 이름',
    korean_description TEXT COMMENT '한국어 설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_korean_name (korean_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='한국어 테스트 테이블';

-- ❌ 잘못된 예시 - charset 누락
CREATE TABLE bad_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
); -- charset 설정 없음!
```

### 데이터 삽입 예시

```sql
-- 한국어 텍스트 삽입 테스트
INSERT INTO korean_test_table (korean_name, korean_description) VALUES
('삼성전자', '대한민국의 대표 전자기업입니다.'),
('카카오', 'IT 서비스 기업입니다. 🚀 이모지도 포함!'),
('LG전자', '생활가전 및 전자제품 제조업체입니다.');

-- 삽입 후 검증
SELECT korean_name, korean_description
FROM korean_test_table
WHERE korean_name LIKE '%전자%';
```

### 마이그레이션 체크리스트

**작성 전**:
- [ ] 템플릿 파일 복사 사용
- [ ] 파일명 규칙 준수 (V숫자__설명.sql)
- [ ] 목적과 배경 주석 작성

**CREATE TABLE 시**:
- [ ] `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` 포함
- [ ] 모든 VARCHAR/TEXT 컬럼에 COMMENT 추가
- [ ] 적절한 INDEX 생성

**데이터 삽입 시**:
- [ ] 한국어 텍스트 포함하여 테스트
- [ ] 특수문자, 이모지 테스트
- [ ] 삽입 후 SELECT로 검증

**완료 후**:
- [ ] 로컬에서 마이그레이션 적용 테스트
- [ ] 한국어 데이터 정상 표시 확인
- [ ] `SHOW CREATE TABLE table_name;`으로 charset 확인

---

## 개발 환경 설정

### IDE 설정

**IntelliJ IDEA**
```
File → Settings → Editor → File Encodings
- Global Encoding: UTF-8
- Project Encoding: UTF-8
- Default encoding for properties files: UTF-8
- Transparent native-to-ascii conversion: ✅
```

**VS Code**
```json
{
    "files.encoding": "utf8",
    "files.autoGuessEncoding": false
}
```

### Git 설정

```bash
# 전역 설정
git config --global core.quotepath false
git config --global core.autocrlf false

# 프로젝트별 .gitattributes
echo "* text=auto eol=lf" > .gitattributes
echo "*.sql text eol=lf" >> .gitattributes
echo "*.java text eol=lf" >> .gitattributes
```

### Docker 개발 환경

**Dockerfile에서 locale 설정**
```dockerfile
FROM openjdk:21-jre-slim

# UTF-8 locale 설정
RUN apt-get update && apt-get install -y locales && \
    sed -i 's/^# *\(ko_KR.UTF-8\)/\1/' /etc/locale.gen && \
    locale-gen && \
    apt-get clean

ENV LANG=ko_KR.UTF-8
ENV LANGUAGE=ko_KR:ko
ENV LC_ALL=ko_KR.UTF-8

COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "/app/app.jar"]
```

---

## 테스트 가이드

### 단위 테스트

**JPA Entity 테스트**
```java
@Test
@DisplayName("한국어 텍스트 JPA 저장/조회 테스트")
void testKoreanTextWithJpa() {
    // Given
    String koreanText = "한국어 테스트 텍스트 🚀";
    User user = User.builder()
            .username("testuser")
            .nickname(koreanText)
            .build();

    // When
    User savedUser = userRepository.save(user);

    // Then
    assertThat(savedUser.getNickname()).isEqualTo(koreanText);

    // 재조회 검증
    User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
    assertThat(foundUser.getNickname()).isEqualTo(koreanText);
}
```

### 통합 테스트

**기존 테스트 활용**: `KoreanTextEncodingIntegrationTest.java`

**추가 테스트 케이스**:
```java
@Test
void testApiKoreanTextResponse() throws Exception {
    mockMvc.perform(get("/api/companies/005930")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(StandardCharsets.UTF_8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.nameKr").value("삼성전자"));
}
```

### E2E 테스트

**Playwright 예시**
```javascript
// frontend/tests/e2e/korean-text.spec.js
test('한국어 텍스트 표시 확인', async ({ page }) => {
  await page.goto('/challenges');

  // 한국어 챌린지 제목 확인
  const challengeTitle = await page.locator('[data-testid="challenge-title"]').first();
  await expect(challengeTitle).toContainText('한국어');

  // 한국어 회사명 확인
  await page.goto('/companies/005930');
  const companyName = await page.locator('[data-testid="company-name"]');
  await expect(companyName).toContainText('삼성전자');
});
```

### 테스트 데이터

**다양한 한국어 패턴**
```java
public class KoreanTestData {
    public static final List<String> KOREAN_TEST_CASES = Arrays.asList(
        "기본 한글",                    // 기본 한글
        "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ",      // 자음
        "ㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣ",          // 모음
        "삼성전자 카카오 네이버",           // 실제 회사명
        "가격: 50,000원",             // 숫자 포함
        "수익률: +15.5%",             // 특수문자
        "📈 상승 📉 하락 💰 수익",       // 이모지
        "Mixed한국어English",          // 혼합 텍스트
        "!@#$%^&*()_+-=[]{}|;:,.<>?" // 특수문자 전체
    );
}
```

---

## 문제 해결

### 일반적인 문제들

#### 1. 한국어가 물음표(?)로 표시

**증상**: 데이터베이스나 API 응답에서 한국어가 `?` 또는 `???`로 표시

**원인**: charset 설정 불일치

**해결책**:
```sql
-- 1. 데이터베이스 charset 확인
SHOW VARIABLES LIKE 'character_set_%';
SHOW VARIABLES LIKE 'collation_%';

-- 2. 테이블 charset 확인
SHOW CREATE TABLE table_name;

-- 3. 필요시 변경
ALTER TABLE table_name CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 연결 문자열 문제

**증상**: 애플리케이션 시작 시 charset 관련 오류

**원인**: JDBC URL에 인코딩 파라미터 누락

**해결책**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

#### 3. HTTP 응답 인코딩 문제

**증상**: REST API 응답에서 한국어가 깨져서 전송

**해결책**:
```java
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class ApiController {
    // 컨트롤러 메서드들
}
```

#### 4. 마이그레이션 후 데이터 손실

**증상**: 마이그레이션 실행 후 기존 한국어 데이터가 손상

**예방책**:
- 운영 데이터 백업 후 마이그레이션 실행
- 스테이징 환경에서 먼저 테스트
- 롤백 계획 수립

**복구 방법**:
```sql
-- 백업에서 데이터 복구
-- (사전에 백업이 있어야 함)
```

### 진단 명령어

**데이터베이스 상태 확인**
```sql
-- 서버 charset 설정
SHOW VARIABLES LIKE 'character_set_%';
SHOW VARIABLES LIKE 'collation_%';

-- 특정 데이터베이스 정보
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'stockquest';

-- 테이블별 charset 정보
SELECT TABLE_NAME, TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'stockquest';

-- 컬럼별 charset 정보
SELECT TABLE_NAME, COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'stockquest'
AND CHARACTER_SET_NAME IS NOT NULL;
```

**한국어 텍스트 테스트**
```sql
-- 임시 테이블로 테스트
CREATE TEMPORARY TABLE charset_test (
    id INT AUTO_INCREMENT PRIMARY KEY,
    korean_text VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO charset_test (korean_text) VALUES ('한국어 테스트 🚀');
SELECT korean_text FROM charset_test;
```

**애플리케이션 레벨 테스트**
```bash
# API 응답 확인
curl -s -H "Accept: application/json" http://localhost:8080/api/companies/005930 | jq .

# 인코딩 헤더 확인
curl -I http://localhost:8080/api/companies/005930
```

---

## 모니터링

### Actuator Health Check

**엔드포인트**: `/actuator/health/databaseCharsetMonitor`

**상태 확인**:
```bash
curl -s http://localhost:8080/actuator/health/databaseCharsetMonitor | jq .
```

### 관리자 API

**상태 조회**:
```bash
curl -H "Authorization: Bearer <admin-token>" \
     http://localhost:8080/api/admin/charset-monitoring/status
```

**수동 검증 실행**:
```bash
curl -X POST \
     -H "Authorization: Bearer <admin-token>" \
     http://localhost:8080/api/admin/charset-monitoring/check
```

### 로그 모니터링

**주요 로그 패턴**:
```bash
# 정상 상태
grep "✅.*Database charset.*successful" application.log

# 문제 감지
grep "❌.*charset.*issue" application.log
grep "🚨 CHARSET_MONITORING_ALERT" application.log

# 한국어 텍스트 테스트 실패
grep "Korean.*text.*test.*failed" application.log
```

### 스케줄드 모니터링

**자동 실행 스케줄**:
- **매시 정각**: 기본 charset 검증
- **매일 오전 9시**: 상세 상태 리포트
- **애플리케이션 시작시**: 초기 검증

**로그 레벨 설정**:
```yaml
logging:
  level:
    com.stockquest.config.monitoring: INFO
```

---

## 체크리스트 요약

### 🚀 새 프로젝트 시작 시

- [ ] MySQL Docker 설정에 charset 파라미터 추가
- [ ] application.yml에 데이터소스 URL 및 HikariCP 설정
- [ ] 마이그레이션 템플릿 파일 준비
- [ ] 한국어 텍스트 통합 테스트 작성
- [ ] charset 모니터링 활성화

### 🛠️ 개발 중

- [ ] 새 마이그레이션 작성 시 템플릿 사용
- [ ] 모든 CREATE TABLE에 charset 명시
- [ ] 한국어 데이터로 기능 테스트
- [ ] API 응답에서 한국어 정상 표시 확인

### 🚢 배포 전

- [ ] 스테이징 환경에서 한국어 텍스트 E2E 테스트
- [ ] charset 모니터링 상태 확인
- [ ] 데이터베이스 백업 생성
- [ ] 롤백 계획 수립

### 🔍 운영 중

- [ ] 매일 charset 모니터링 로그 확인
- [ ] 월 1회 전체 테이블 charset 감사
- [ ] 새로운 한국어 데이터 추가 시 검증
- [ ] 사용자 신고 시 즉시 charset 상태 확인

---

## 참고 자료

### 공식 문서
- [MySQL 8.0 Character Set Support](https://dev.mysql.com/doc/refman/8.0/en/charset.html)
- [Spring Boot Internationalization](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.internationalization)

### 프로젝트 내 참고 파일
- `src/main/resources/db/migration/TEMPLATE__Migration_Template.sql`
- `src/test/java/com/stockquest/integration/KoreanTextEncodingIntegrationTest.java`
- `src/main/java/com/stockquest/config/monitoring/DatabaseCharsetMonitor.java`

### 관련 API 엔드포인트
- 건강 상태: `/actuator/health/databaseCharsetMonitor`
- 관리자 상태: `/api/admin/charset-monitoring/status`
- 문제 해결 가이드: `/api/admin/charset-monitoring/troubleshooting-guide`

---

**마지막 업데이트**: 2025-09-22
**작성자**: Claude Code for Stock Quest Project