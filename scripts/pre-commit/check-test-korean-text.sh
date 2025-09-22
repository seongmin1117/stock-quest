#!/bin/bash

# Pre-commit hook for validating Korean text handling in test files
# This script ensures test files properly handle Korean text encoding and validation

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
TEST_PATTERN='backend/src/test/.*\.java$'
KOREAN_CHAR_PATTERN='[가-힣]'

# Statistics
total_files=0
checked_files=0
issues_found=0
korean_test_files=0

echo -e "${BLUE}🧪 Test Korean Text Validation${NC}"
echo "=============================="

# Function to check Korean text in test methods
check_korean_test_methods() {
    local file="$1"
    local method_issues=0

    # Find test methods with Korean text
    local test_methods_with_korean
    test_methods_with_korean=$(grep -n "@Test\|@ParameterizedTest\|@TestFactory" "$file" | while read -r test_line; do
        local test_line_num
        test_line_num=$(echo "$test_line" | cut -d: -f1)

        # Look for the next method declaration
        local method_start
        method_start=$(tail -n +"$test_line_num" "$file" | grep -n "void\|public.*(" | head -1 | cut -d: -f1)
        method_start=$((test_line_num + method_start - 1))

        # Get method name
        local method_name
        method_name=$(sed -n "${method_start}p" "$file" | grep -o "void [a-zA-Z_][a-zA-Z0-9_]*" | cut -d' ' -f2 || echo "unknown")

        # Check next 20 lines for Korean text
        local method_content
        method_content=$(tail -n +$method_start "$file" | head -20)

        if echo "$method_content" | grep -q "$KOREAN_CHAR_PATTERN"; then
            echo "$method_start:$method_name"
        fi
    done)

    if [[ -n "$test_methods_with_korean" ]]; then
        ((korean_test_files++))
        echo -e "${BLUE}🔍 Korean text found in test methods:${NC}"

        while IFS=: read -r line_num method_name; do
            echo -e "   📝 Method: ${YELLOW}$method_name${NC} (line $line_num)"

            # Check if method follows Korean text testing best practices
            local method_block
            method_block=$(tail -n +"$line_num" "$file" | sed '/^[[:space:]]*}[[:space:]]*$/q' | head -30)

            # Check for charset/encoding assertions
            if echo "$method_block" | grep -q "UTF-8\|utf8mb4\|charset\|encoding"; then
                echo -e "     ${GREEN}✅ Charset/encoding verification found${NC}"
            else
                echo -e "     ${YELLOW}⚠️  Consider adding charset verification${NC}"
            fi

            # Check for database transaction/rollback
            if echo "$method_block" | grep -q "@Transactional\|@Rollback\|rollback"; then
                echo -e "     ${GREEN}✅ Transaction management found${NC}"
            else
                echo -e "     ${BLUE}ℹ️  Consider @Transactional for database tests${NC}"
            fi

            # Check for AssertJ or proper assertions
            if echo "$method_block" | grep -q "assertThat\|assertEquals\|assertTrue"; then
                echo -e "     ${GREEN}✅ Proper assertions found${NC}"
            else
                echo -e "     ${RED}❌ Missing proper assertions${NC}"
                ((method_issues++))
            fi

            # Check for Korean text in string literals
            local korean_strings
            korean_strings=$(echo "$method_block" | grep -o '"[^"]*[가-힣][^"]*"' || true)
            if [[ -n "$korean_strings" ]]; then
                echo -e "     ${BLUE}📝 Korean strings:${NC}"
                echo "$korean_strings" | while read -r korean_str; do
                    echo -e "       ${YELLOW}$korean_str${NC}"
                done
            fi

            echo
        done < <(echo "$test_methods_with_korean")
    fi

    return $method_issues
}

# Function to check for comprehensive Korean text testing
check_korean_test_coverage() {
    local file="$1"
    local coverage_issues=0

    # Check if file has comprehensive Korean text tests
    local korean_test_patterns=(
        "한국어.*저장\|Korean.*save\|save.*한국어"
        "한국어.*조회\|Korean.*retrieve\|retrieve.*한국어"
        "한국어.*검색\|Korean.*search\|search.*한국어"
        "charset\|encoding\|utf8mb4"
        "nickname.*한국어\|한국어.*nickname"
        "company.*한국어\|한국어.*company"
    )

    echo -e "${BLUE}📊 Testing coverage analysis:${NC}"

    for pattern in "${korean_test_patterns[@]}"; do
        if grep -qi "$pattern" "$file"; then
            echo -e "   ${GREEN}✅ $(echo "$pattern" | cut -d'\' -f1) testing found${NC}"
        else
            echo -e "   ${YELLOW}⚠️  Missing $(echo "$pattern" | cut -d'\' -f1) testing${NC}"
        fi
    done

    # Check for database integration tests
    if grep -q "@DataJpaTest\|@SpringBootTest\|TestEntityManager" "$file"; then
        echo -e "   ${GREEN}✅ Database integration testing${NC}"
    else
        echo -e "   ${BLUE}ℹ️  Consider database integration tests for Korean text${NC}"
    fi

    # Check for REST API tests
    if grep -q "@WebMvcTest\|MockMvc\|@AutoConfigureTestDatabase" "$file"; then
        echo -e "   ${GREEN}✅ REST API testing setup${NC}"
    else
        echo -e "   ${BLUE}ℹ️  Consider REST API tests for Korean text handling${NC}"
    fi

    return $coverage_issues
}

# Function to check for anti-patterns in Korean text testing
check_korean_test_antipatterns() {
    local file="$1"
    local antipattern_issues=0

    echo -e "${BLUE}🚨 Anti-pattern analysis:${NC}"

    # Check for hardcoded question marks (encoding failure indicators)
    if grep -q '".*[가-힣].*?"' "$file"; then
        echo -e "   ${RED}❌ Question marks mixed with Korean text found${NC}"
        echo -e "   ${BLUE}Fix:${NC} Check for encoding corruption in test data"
        ((antipattern_issues++))
    fi

    # Check for byte arrays without charset specification
    if grep -q "getBytes()" "$file" && grep -q "$KOREAN_CHAR_PATTERN" "$file"; then
        echo -e "   ${YELLOW}⚠️  getBytes() without charset found with Korean text${NC}"
        echo -e "   ${BLUE}Fix:${NC} Use getBytes(StandardCharsets.UTF_8)"
    fi

    # Check for FileReader/FileWriter without charset
    if grep -q "FileReader\|FileWriter" "$file" && grep -q "$KOREAN_CHAR_PATTERN" "$file"; then
        echo -e "   ${YELLOW}⚠️  FileReader/FileWriter without charset found${NC}"
        echo -e "   ${BLUE}Fix:${NC} Use Files.readString/writeString with UTF-8"
    fi

    # Check for String.format with Korean text (can be problematic)
    if grep -q "String.format.*[가-힣]\|format.*\"[^\"]*[가-힣]" "$file"; then
        echo -e "   ${BLUE}ℹ️  String.format with Korean text - verify formatting${NC}"
    fi

    # Check for missing @DisplayName with Korean methods
    local methods_without_display_name
    methods_without_display_name=$(grep -n "void.*[가-힣]" "$file" | while read -r line; do
        local line_num
        line_num=$(echo "$line" | cut -d: -f1)

        # Check if @DisplayName exists in previous 3 lines
        local context
        context=$(sed -n "$((line_num-3)),$line_num p" "$file")

        if ! echo "$context" | grep -q "@DisplayName"; then
            echo "$line_num"
        fi
    done)

    if [[ -n "$methods_without_display_name" ]]; then
        echo -e "   ${YELLOW}⚠️  Test methods with Korean names missing @DisplayName${NC}"
        echo -e "   ${BLUE}Lines:${NC} $(echo "$methods_without_display_name" | tr '\n' ' ')"
        echo -e "   ${BLUE}Recommendation:${NC} Add @DisplayName for better test reporting"
    fi

    if [[ $antipattern_issues -eq 0 ]]; then
        echo -e "   ${GREEN}✅ No anti-patterns detected${NC}"
    fi

    return $antipattern_issues
}

# Function to show Korean test best practices
show_korean_test_best_practices() {
    echo
    echo -e "${BLUE}🏆 Korean Text Testing Best Practices${NC}"
    echo "====================================="
    echo
    echo -e "${BLUE}1. Comprehensive Test Example:${NC}"
    cat << 'EOF'
@Test
@DisplayName("한국어 닉네임 저장 및 조회 테스트")
@Transactional
void testKoreanNicknameSaveAndRetrieve() {
    // Given
    String koreanNickname = "한국어닉네임";
    User user = User.builder()
        .username("testuser")
        .nickname(koreanNickname)
        .build();

    // When
    User savedUser = userRepository.save(user);
    User retrievedUser = userRepository.findById(savedUser.getId()).get();

    // Then
    assertThat(retrievedUser.getNickname())
        .isEqualTo(koreanNickname)
        .containsPattern("[가-힣]+");

    // Verify encoding integrity
    byte[] nicknameBytes = retrievedUser.getNickname().getBytes(StandardCharsets.UTF_8);
    String reconstructed = new String(nicknameBytes, StandardCharsets.UTF_8);
    assertThat(reconstructed).isEqualTo(koreanNickname);
}
EOF
    echo
    echo -e "${BLUE}2. REST API Testing:${NC}"
    cat << 'EOF'
@Test
@DisplayName("한국어 데이터 REST API 요청/응답 테스트")
void testKoreanDataRestApi() throws Exception {
    // Given
    String koreanData = "한국어데이터";

    // When & Then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content("{\"nickname\":\"" + koreanData + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value(koreanData))
        .andExpect(content().encoding("UTF-8"));
}
EOF
    echo
    echo -e "${BLUE}3. Database Integration:${NC}"
    cat << 'EOF'
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:test-data-korean.sql")
class KoreanTextDataJpaTest {
    // Tests here use real database with Korean test data
}
EOF
    echo
    echo -e "${BLUE}4. Charset Verification:${NC}"
    echo "   • Always test round-trip: save → retrieve → verify"
    echo "   • Use StandardCharsets.UTF_8 explicitly"
    echo "   • Verify byte-level encoding integrity"
    echo "   • Test with various Korean character sets (한글, 漢字, etc.)"
    echo
    echo -e "${BLUE}5. Test Data Examples:${NC}"
    echo '   • Basic: "한국어"'
    echo '   • Complex: "한국어 테스트 데이터 🇰🇷"'
    echo '   • Mixed: "Korean한국어Mixed文字"'
    echo '   • Special: "한국어\n줄바꿈\t탭"'
}

# Function to validate single test file
validate_test_file() {
    local file="$1"
    local file_issues=0

    # Check if file is readable
    if [[ ! -r "$file" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Cannot read file: $file"
        return 1
    fi

    echo -e "${BLUE}🔍 Checking test file:${NC} $file"

    # Check if file contains Korean text
    if ! grep -q "$KOREAN_CHAR_PATTERN" "$file"; then
        echo -e "${BLUE}ℹ️  INFO:${NC} No Korean text found in test file"
        echo
        return 0
    fi

    # File encoding check
    local encoding
    encoding=$(file -b --mime-encoding "$file" 2>/dev/null || echo "unknown")
    if [[ "$encoding" != "utf-8" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Test file with Korean text must be UTF-8 encoded"
        echo -e "   Current encoding: ${YELLOW}$encoding${NC}"
        ((file_issues++))
    fi

    # Check Korean text in test methods
    if ! check_korean_test_methods "$file"; then
        ((file_issues++))
    fi

    # Check testing coverage
    check_korean_test_coverage "$file"

    # Check for anti-patterns
    if ! check_korean_test_antipatterns "$file"; then
        ((file_issues++))
    fi

    if [[ $file_issues -eq 0 ]]; then
        echo -e "${GREEN}✅ PASSED:${NC} Korean text testing looks good"
    else
        echo -e "${RED}❌ FAILED:${NC} $file_issues Korean text testing issues found"
        ((issues_found += file_issues))
    fi

    echo
    return $file_issues
}

# Main validation logic
main() {
    local exit_code=0

    # If specific files are passed as arguments, check only those
    if [[ $# -gt 0 ]]; then
        for file in "$@"; do
            # Check if file matches test pattern
            if echo "$file" | grep -E "$TEST_PATTERN" > /dev/null; then
                ((total_files++))
                if [[ -f "$file" ]]; then
                    ((checked_files++))
                    if ! validate_test_file "$file"; then
                        exit_code=1
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING:${NC} File not found: $file"
                fi
            fi
        done
    else
        # Check all test files in the project
        while IFS= read -r -d '' file; do
            ((total_files++))
            ((checked_files++))
            if ! validate_test_file "$file"; then
                exit_code=1
            fi
        done < <(find . -path "*/src/test/*" -name "*.java" -type f -print0 2>/dev/null)
    fi

    # Summary
    echo "=============================="
    echo -e "${BLUE}📊 Korean Text Testing Summary${NC}"
    echo "Total test files found: $total_files"
    echo "Files checked: $checked_files"
    echo "Files with Korean text: $korean_test_files"

    if [[ $issues_found -eq 0 ]]; then
        echo -e "${GREEN}✅ SUCCESS:${NC} All Korean text testing follows best practices"

        if [[ $korean_test_files -gt 0 ]]; then
            echo
            echo -e "${BLUE}💡 Korean Testing Statistics:${NC}"
            echo "• Test files with Korean text: $korean_test_files"
            echo "• Coverage appears comprehensive"
            echo "• No anti-patterns detected"
        else
            echo
            echo -e "${BLUE}💡 Recommendation:${NC}"
            echo "• Consider adding Korean text integration tests"
            echo "• Test database charset handling with Korean data"
            echo "• Verify REST API Korean text encoding"
        fi
    else
        echo -e "${RED}❌ FAILURE:${NC} $issues_found Korean text testing issues need to be fixed"
        show_korean_test_best_practices
        exit_code=1
    fi

    return $exit_code
}

# Run main function with all arguments
main "$@"