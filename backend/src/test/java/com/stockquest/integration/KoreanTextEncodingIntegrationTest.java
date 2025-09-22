package com.stockquest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockquest.adapter.out.persistence.challenge.ChallengeJpaRepository;
import com.stockquest.adapter.out.persistence.company.CompanyJpaRepository;
import com.stockquest.adapter.out.persistence.user.UserJpaRepository;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.company.Company;
import com.stockquest.domain.user.User;
import com.stockquest.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 한국어 텍스트 인코딩 통합 테스트
 *
 * 테스트 범위:
 * 1. 데이터베이스 저장/조회 (JPA 엔티티)
 * 2. REST API 요청/응답 (Controller)
 * 3. JSON 직렬화/역직렬화
 * 4. 특수문자 및 이모지 처리
 * 5. SQL 직접 쿼리 검증
 */
@DisplayName("한국어 텍스트 인코딩 통합 테스트")
@AutoConfigureWebMvc
@Transactional
public class KoreanTextEncodingIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ChallengeJpaRepository challengeRepository;

    @Autowired
    private CompanyJpaRepository companyRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    // 테스트용 한국어 텍스트 데이터
    private static final String KOREAN_TEXT = "한국어 텍스트 테스트";
    private static final String KOREAN_DESCRIPTION = "이것은 한국어 설명입니다. 특수문자도 포함됩니다: !@#$%^&*()";
    private static final String KOREAN_WITH_EMOJI = "한국어 텍스트 🚀 이모지 포함 ✅ 테스트";
    private static final String KOREAN_COMPANY_NAME = "삼성전자";
    private static final String MIXED_LANG_TEXT = "Mixed 한국어 English 텍스트 123";
    private static final String COMPLEX_KOREAN_TEXT = "가나다라마바사아자차카타파하 ABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890 !@#$%^&*()_+-=[]{}|;:,.<>?";

    @Test
    @DisplayName("1. JPA 엔티티 한국어 저장/조회 테스트")
    void testJpaEntityKoreanTextStorage() {
        // Given: 한국어 텍스트를 포함한 사용자 생성
        User user = User.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .nickname(KOREAN_TEXT)
                .role(UserRole.USER)
                .build();

        // When: 데이터베이스에 저장
        User savedUser = userRepository.save(user);

        // Then: 저장된 데이터 검증
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo(KOREAN_TEXT);

        // 다시 조회하여 검증
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getNickname()).isEqualTo(KOREAN_TEXT);
    }

    @Test
    @DisplayName("2. 챌린지 엔티티 복잡한 한국어 텍스트 테스트")
    void testChallengeEntityComplexKoreanText() {
        // Given: 복잡한 한국어 텍스트를 포함한 챌린지 생성
        Challenge challenge = Challenge.builder()
                .title(KOREAN_WITH_EMOJI)
                .description(COMPLEX_KOREAN_TEXT)
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(30))
                .speedFactor(10)
                .build();

        // When: 저장
        Challenge savedChallenge = challengeRepository.save(challenge);

        // Then: 검증
        assertThat(savedChallenge.getTitle()).isEqualTo(KOREAN_WITH_EMOJI);
        assertThat(savedChallenge.getDescription()).isEqualTo(COMPLEX_KOREAN_TEXT);

        // 재조회 검증
        Challenge foundChallenge = challengeRepository.findById(savedChallenge.getId()).orElse(null);
        assertThat(foundChallenge).isNotNull();
        assertThat(foundChallenge.getTitle()).isEqualTo(KOREAN_WITH_EMOJI);
        assertThat(foundChallenge.getDescription()).isEqualTo(COMPLEX_KOREAN_TEXT);
    }

    @Test
    @DisplayName("3. 회사 엔티티 한국어 회사명 테스트")
    void testCompanyEntityKoreanName() {
        // Given: 한국어 회사명을 가진 회사 생성
        Company company = Company.builder()
                .ticker("005930")
                .nameKr(KOREAN_COMPANY_NAME)
                .nameEn("Samsung Electronics")
                .sector("기술")
                .marketCap(1000000000L)
                .build();

        // When: 저장
        Company savedCompany = companyRepository.save(company);

        // Then: 검증
        assertThat(savedCompany.getNameKr()).isEqualTo(KOREAN_COMPANY_NAME);
        assertThat(savedCompany.getSector()).isEqualTo("기술");

        // 재조회 검증
        Company foundCompany = companyRepository.findById(savedCompany.getId()).orElse(null);
        assertThat(foundCompany).isNotNull();
        assertThat(foundCompany.getNameKr()).isEqualTo(KOREAN_COMPANY_NAME);
        assertThat(foundCompany.getSector()).isEqualTo("기술");
    }

    @Test
    @DisplayName("4. REST API 한국어 응답 테스트")
    void testRestApiKoreanResponse() throws Exception {
        // Given: 한국어 데이터를 가진 챌린지 저장
        Challenge challenge = Challenge.builder()
                .title(KOREAN_TEXT)
                .description(KOREAN_DESCRIPTION)
                .periodStart(LocalDate.now())
                .periodEnd(LocalDate.now().plusDays(30))
                .speedFactor(10)
                .build();
        Challenge savedChallenge = challengeRepository.save(challenge);

        // When & Then: API 호출하여 한국어 응답 검증
        mockMvc.perform(get("/api/challenges/" + savedChallenge.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(KOREAN_TEXT))
                .andExpect(jsonPath("$.description").value(KOREAN_DESCRIPTION));
    }

    @Test
    @DisplayName("5. JSON 직렬화/역직렬화 한국어 테스트")
    void testJsonSerializationKoreanText() throws Exception {
        // Given: 한국어 텍스트를 포함한 Map 생성
        Map<String, Object> testData = Map.of(
                "title", KOREAN_TEXT,
                "description", KOREAN_WITH_EMOJI,
                "complexText", COMPLEX_KOREAN_TEXT,
                "mixedText", MIXED_LANG_TEXT
        );

        // When: JSON으로 직렬화
        String json = objectMapper.writeValueAsString(testData);

        // Then: JSON에 한국어가 올바르게 포함되는지 검증
        assertThat(json).contains(KOREAN_TEXT);
        assertThat(json).contains(KOREAN_WITH_EMOJI);

        // 역직렬화 검증
        @SuppressWarnings("unchecked")
        Map<String, Object> deserializedData = objectMapper.readValue(json, Map.class);
        assertThat(deserializedData.get("title")).isEqualTo(KOREAN_TEXT);
        assertThat(deserializedData.get("description")).isEqualTo(KOREAN_WITH_EMOJI);
        assertThat(deserializedData.get("complexText")).isEqualTo(COMPLEX_KOREAN_TEXT);
        assertThat(deserializedData.get("mixedText")).isEqualTo(MIXED_LANG_TEXT);
    }

    @Test
    @DisplayName("6. SQL 직접 쿼리 한국어 텍스트 검증")
    void testDirectSqlKoreanText() throws Exception {
        // Given: 한국어 데이터 저장
        User user = User.builder()
                .username("sqltest")
                .password("password123")
                .email("sqltest@example.com")
                .nickname(KOREAN_WITH_EMOJI)
                .role(UserRole.USER)
                .build();
        User savedUser = userRepository.save(user);

        // When: 직접 SQL로 조회
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT nickname FROM users WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, savedUser.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    // Then: 결과 검증
                    assertThat(rs.next()).isTrue();
                    String nickname = rs.getString("nickname");
                    assertThat(nickname).isEqualTo(KOREAN_WITH_EMOJI);
                }
            }
        }
    }

    @Test
    @DisplayName("7. 데이터베이스 charset 설정 검증")
    void testDatabaseCharsetConfiguration() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // 데이터베이스 charset 확인
            String sql = "SHOW VARIABLES LIKE 'character_set_%'";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                boolean foundUtf8mb4 = false;
                while (rs.next()) {
                    String variableName = rs.getString("Variable_name");
                    String value = rs.getString("Value");

                    if (variableName.equals("character_set_database") ||
                        variableName.equals("character_set_server")) {
                        assertThat(value).isEqualTo("utf8mb4");
                        foundUtf8mb4 = true;
                    }
                }
                assertThat(foundUtf8mb4).isTrue();
            }

            // Collation 확인
            sql = "SHOW VARIABLES LIKE 'collation_%'";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                boolean foundUtf8mb4Collation = false;
                while (rs.next()) {
                    String variableName = rs.getString("Variable_name");
                    String value = rs.getString("Value");

                    if (variableName.equals("collation_database") ||
                        variableName.equals("collation_server")) {
                        assertThat(value).isEqualTo("utf8mb4_unicode_ci");
                        foundUtf8mb4Collation = true;
                    }
                }
                assertThat(foundUtf8mb4Collation).isTrue();
            }
        }
    }

    @Test
    @DisplayName("8. 테이블별 charset 검증")
    void testTableCharsetConfiguration() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // 주요 테이블들의 charset 확인
            List<String> tablesToCheck = Arrays.asList("users", "challenge", "company");

            for (String tableName : tablesToCheck) {
                String sql = "SHOW CREATE TABLE " + tableName;
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    if (rs.next()) {
                        String createTableSql = rs.getString(2);
                        // CREATE TABLE 문에 utf8mb4가 포함되어 있는지 확인
                        assertThat(createTableSql.toLowerCase())
                                .contains("utf8mb4")
                                .withFailMessage("Table %s does not have utf8mb4 charset", tableName);
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("9. 다양한 한국어 문자 조합 테스트")
    void testVariousKoreanCharacterCombinations() {
        // Given: 다양한 한국어 문자 조합
        List<String> koreanTexts = Arrays.asList(
                "가나다라", // 기본 한글
                "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ", // 자음
                "ㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣ", // 모음
                "ㄲㄸㅃㅆㅉ", // 된소리
                "삼성전자 카카오 네이버", // 실제 회사명
                "주식투자 포트폴리오 관리", // 도메인 용어
                "가격: 50,000원", // 숫자 포함
                "수익률: +15.5%", // 퍼센트 포함
                "📈 상승 📉 하락 💰 수익", // 이모지 포함
                "한국어123EnglishMixed텍스트" // 혼합 텍스트
        );

        // When & Then: 각 텍스트에 대해 저장/조회 테스트
        for (int i = 0; i < koreanTexts.size(); i++) {
            String text = koreanTexts.get(i);

            User user = User.builder()
                    .username("user" + i)
                    .password("password123")
                    .email("user" + i + "@example.com")
                    .nickname(text)
                    .role(UserRole.USER)
                    .build();

            User savedUser = userRepository.save(user);
            assertThat(savedUser.getNickname()).isEqualTo(text);

            // 재조회 확인
            User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getNickname()).isEqualTo(text);
        }
    }

    @Test
    @DisplayName("10. HTTP 응답 Content-Type 및 인코딩 검증")
    void testHttpResponseContentTypeAndEncoding() throws Exception {
        // Given: 한국어 데이터를 가진 회사 저장
        Company company = Company.builder()
                .ticker("005930")
                .nameKr(KOREAN_COMPANY_NAME)
                .nameEn("Samsung Electronics")
                .sector("반도체")
                .marketCap(1000000000L)
                .build();
        Company savedCompany = companyRepository.save(company);

        // When & Then: API 응답의 Content-Type과 인코딩 검증
        String url = "http://localhost:" + port + "/api/companies/" + savedCompany.getTicker();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Content-Type 헤더 검증
        String contentType = response.getHeaders().getContentType().toString();
        assertThat(contentType).contains("application/json");
        assertThat(contentType).containsAnyOf("charset=UTF-8", "charset=utf-8");

        // 응답 본문에 한국어가 올바르게 포함되는지 검증
        String responseBody = response.getBody();
        assertThat(responseBody).contains(KOREAN_COMPANY_NAME);
        assertThat(responseBody).contains("반도체");
    }
}