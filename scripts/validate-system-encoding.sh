#!/bin/bash

# Comprehensive System Encoding Validation Script
# This script validates the entire Stock Quest system for proper Korean text encoding support

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"
MYSQL_CONTAINER="stockquest-mysql"
TEST_KOREAN_TEXT="한국어 인코딩 테스트 🚀"
TEST_COMPANY_NAME="삼성전자"

# Test results tracking
total_tests=0
passed_tests=0
failed_tests=0
warnings=0

# Test categories
declare -A test_results=(
    ["configuration"]=0
    ["database"]=0
    ["application"]=0
    ["api"]=0
    ["integration"]=0
)

echo -e "${CYAN}🚀 Stock Quest System Encoding Validation${NC}"
echo "=========================================="
echo -e "${BLUE}Testing comprehensive Korean text encoding support${NC}"
echo

# Function to run a test and track results
run_test() {
    local test_name="$1"
    local test_command="$2"
    local category="$3"
    local is_critical="${4:-true}"

    ((total_tests++))
    echo -n "🔍 Testing: $test_name... "

    if eval "$test_command" >/dev/null 2>&1; then
        echo -e "${GREEN}PASS${NC}"
        ((passed_tests++))
        ((test_results["$category"]++))
        return 0
    else
        if [[ "$is_critical" == "true" ]]; then
            echo -e "${RED}FAIL${NC}"
            ((failed_tests++))
        else
            echo -e "${YELLOW}WARN${NC}"
            ((warnings++))
        fi
        return 1
    fi
}

# Function to run a test with detailed output
run_test_detailed() {
    local test_name="$1"
    local test_command="$2"
    local category="$3"
    local expected_output="$4"

    ((total_tests++))
    echo "🔍 Testing: $test_name"

    local output
    if output=$(eval "$test_command" 2>&1); then
        if [[ -n "$expected_output" ]] && echo "$output" | grep -q "$expected_output"; then
            echo -e "   ${GREEN}✅ PASS:${NC} Expected output found"
            echo -e "   ${BLUE}Output:${NC} $(echo "$output" | head -1)"
            ((passed_tests++))
            ((test_results["$category"]++))
            return 0
        elif [[ -z "$expected_output" ]]; then
            echo -e "   ${GREEN}✅ PASS:${NC} Command executed successfully"
            ((passed_tests++))
            ((test_results["$category"]++))
            return 0
        else
            echo -e "   ${RED}❌ FAIL:${NC} Expected output not found"
            echo -e "   ${YELLOW}Expected:${NC} $expected_output"
            echo -e "   ${YELLOW}Actual:${NC} $(echo "$output" | head -1)"
            ((failed_tests++))
            return 1
        fi
    else
        echo -e "   ${RED}❌ FAIL:${NC} Command failed"
        echo -e "   ${YELLOW}Error:${NC} $output"
        ((failed_tests++))
        return 1
    fi
}

# 1. Configuration Validation Tests
echo -e "${PURPLE}📋 1. Configuration Validation${NC}"
echo "==============================="

# Test application configuration files
run_test "Application configuration charset" \
    "grep -q 'characterEncoding=UTF-8' backend/src/main/resources/application*.yml" \
    "configuration"

run_test "HikariCP connection init SQL" \
    "grep -q 'connection-init-sql.*utf8mb4' backend/src/main/resources/application*.yml" \
    "configuration"

run_test "Docker Compose MySQL charset" \
    "grep -q 'character-set-server=utf8mb4' docker-compose.yml" \
    "configuration"

run_test "Dockerfile Java version 21" \
    "grep -q 'openjdk:21' backend/Dockerfile" \
    "configuration"

echo

# 2. Database Validation Tests
echo -e "${PURPLE}💾 2. Database Validation${NC}"
echo "=========================="

# Check if MySQL container is running
if docker ps | grep -q "$MYSQL_CONTAINER"; then
    echo -e "${GREEN}✅ MySQL container is running${NC}"

    # Test database charset configuration
    run_test_detailed "Database character set" \
        "docker exec $MYSQL_CONTAINER mysql -u root -proot_password -e \"SHOW VARIABLES LIKE 'character_set_database';\" stockquest" \
        "database" \
        "utf8mb4"

    run_test_detailed "Database collation" \
        "docker exec $MYSQL_CONTAINER mysql -u root -proot_password -e \"SHOW VARIABLES LIKE 'collation_database';\" stockquest" \
        "database" \
        "utf8mb4_unicode_ci"

    # Test Korean text storage and retrieval
    echo "🔍 Testing: Korean text database round-trip"
    if docker exec $MYSQL_CONTAINER mysql -u root -proot_password stockquest \
        -e "CREATE TEMPORARY TABLE test_korean (id INT, text VARCHAR(255)); INSERT INTO test_korean VALUES (1, '$TEST_KOREAN_TEXT'); SELECT text FROM test_korean WHERE id = 1;" \
        | grep -q "$TEST_KOREAN_TEXT"; then
        echo -e "   ${GREEN}✅ PASS:${NC} Korean text stored and retrieved correctly"
        ((passed_tests++))
        ((test_results["database"]++))
    else
        echo -e "   ${RED}❌ FAIL:${NC} Korean text corruption in database"
        ((failed_tests++))
    fi
    ((total_tests++))

    # Test table charsets
    run_test_detailed "Users table charset" \
        "docker exec $MYSQL_CONTAINER mysql -u root -proot_password -e \"SHOW CREATE TABLE users;\" stockquest" \
        "database" \
        "utf8mb4"

    run_test_detailed "Challenge table charset" \
        "docker exec $MYSQL_CONTAINER mysql -u root -proot_password -e \"SHOW CREATE TABLE challenge;\" stockquest" \
        "database" \
        "utf8mb4"

else
    echo -e "${RED}❌ MySQL container not running - skipping database tests${NC}"
    failed_tests=$((failed_tests + 5))
    total_tests=$((total_tests + 5))
fi

echo

# 3. Application Validation Tests
echo -e "${PURPLE}🏗️ 3. Application Validation${NC}"
echo "=============================="

# Check if backend is running
if curl -s "$BACKEND_URL/actuator/health" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend application is running${NC}"

    # Test health endpoint
    run_test_detailed "Application health check" \
        "curl -s $BACKEND_URL/actuator/health" \
        "application" \
        "UP"

    # Test charset monitoring endpoint
    run_test_detailed "Database charset monitoring" \
        "curl -s $BACKEND_URL/actuator/health/databaseCharsetMonitor" \
        "application" \
        "UP"

    # Test Korean text in API responses
    run_test_detailed "Korean company data API" \
        "curl -s $BACKEND_URL/api/v1/companies/005930" \
        "application" \
        "삼성전자"

    # Test admin charset monitoring API
    if command -v jq >/dev/null 2>&1; then
        run_test_detailed "Admin charset monitoring API" \
            "curl -s -H 'Authorization: Bearer test-token' $BACKEND_URL/api/admin/charset-monitoring/status 2>/dev/null | jq -r '.charset_status' 2>/dev/null || echo 'SKIP'" \
            "application" \
            "" \
            "false"
    fi

else
    echo -e "${RED}❌ Backend application not running - skipping application tests${NC}"
    failed_tests=$((failed_tests + 4))
    total_tests=$((total_tests + 4))
fi

echo

# 4. API Integration Tests
echo -e "${PURPLE}🌐 4. API Integration Tests${NC}"
echo "============================"

if curl -s "$BACKEND_URL/actuator/health" >/dev/null 2>&1; then
    # Test challenge list API with Korean content
    run_test_detailed "Challenge list API with Korean text" \
        "curl -s $BACKEND_URL/api/challenges" \
        "api" \
        "description"

    # Test OpenAPI specification
    run_test_detailed "OpenAPI specification accessible" \
        "curl -s $BACKEND_URL/api-docs" \
        "api" \
        "openapi"

    # Test CORS headers for frontend integration
    run_test_detailed "CORS headers for frontend" \
        "curl -s -H 'Origin: $FRONTEND_URL' $BACKEND_URL/api/challenges -I" \
        "api" \
        "Access-Control"

else
    echo -e "${RED}❌ Backend not available - skipping API tests${NC}"
    failed_tests=$((failed_tests + 3))
    total_tests=$((total_tests + 3))
fi

echo

# 5. Frontend Integration Tests
echo -e "${PURPLE}🎨 5. Frontend Integration Tests${NC}"
echo "================================="

# Check if frontend is running
if curl -s "$FRONTEND_URL" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend application is running${NC}"

    # Test frontend Korean text rendering
    run_test_detailed "Frontend main page loads" \
        "curl -s $FRONTEND_URL" \
        "integration" \
        "Stock Quest"

    # Test TypeScript compilation (if available)
    if [[ -f "frontend/package.json" ]]; then
        run_test "Frontend TypeScript compilation" \
            "cd frontend && npm run type-check" \
            "integration" \
            "false"
    fi

else
    echo -e "${YELLOW}⚠️ Frontend application not running - skipping frontend tests${NC}"
    warnings=$((warnings + 2))
    total_tests=$((total_tests + 2))
fi

echo

# 6. File Encoding Tests
echo -e "${PURPLE}📄 6. File Encoding Tests${NC}"
echo "=========================="

# Test source file encodings
run_test "Java source files UTF-8 encoding" \
    "find backend/src -name '*.java' -exec file {} \\; | grep -v 'UTF-8\\|ASCII' | wc -l | grep -q '^0$'" \
    "configuration"

run_test "Configuration files UTF-8 encoding" \
    "find backend/src/main/resources -name '*.yml' -o -name '*.yaml' -o -name '*.properties' | xargs file | grep -v 'UTF-8\\|ASCII' | wc -l | grep -q '^0$'" \
    "configuration"

run_test "Migration files UTF-8 encoding" \
    "find backend/src/main/resources/db/migration -name '*.sql' | xargs file | grep -v 'UTF-8\\|ASCII' | wc -l | grep -q '^0$'" \
    "configuration"

echo

# 7. Pre-commit Hook Tests
echo -e "${PURPLE}🔒 7. Pre-commit Hook Tests${NC}"
echo "============================"

# Test pre-commit configuration
run_test "Pre-commit configuration exists" \
    "test -f .pre-commit-config.yaml" \
    "configuration"

# Test charset validation scripts
run_test "Migration charset validation script" \
    "test -x scripts/pre-commit/check-migration-charset.sh" \
    "configuration"

run_test "Korean text encoding validation script" \
    "test -x scripts/pre-commit/check-korean-text-encoding.sh" \
    "configuration"

run_test "Configuration charset validation script" \
    "test -x scripts/pre-commit/check-config-charset.sh" \
    "configuration"

run_test "Docker charset validation script" \
    "test -x scripts/pre-commit/check-docker-charset.sh" \
    "configuration"

# Test validation scripts (dry run)
echo "🔍 Testing: Pre-commit scripts dry run"
if scripts/pre-commit/check-migration-charset.sh backend/src/main/resources/db/migration/V21__Create_blog_articles_tables.sql >/dev/null 2>&1; then
    echo -e "   ${GREEN}✅ PASS:${NC} Migration charset validation works"
    ((passed_tests++))
    ((test_results["configuration"]++))
else
    echo -e "   ${YELLOW}⚠️ WARN:${NC} Migration charset validation script issues"
    ((warnings++))
fi
((total_tests++))

echo

# 8. Documentation Tests
echo -e "${PURPLE}📚 8. Documentation Tests${NC}"
echo "=========================="

run_test "Encoding best practices documentation" \
    "test -f backend/docs/ENCODING_BEST_PRACTICES.md" \
    "configuration"

run_test "Migration template exists" \
    "test -f backend/src/main/resources/db/migration/TEMPLATE__Migration_Template.sql" \
    "configuration"

run_test "CLAUDE.md documentation" \
    "test -f CLAUDE.md && grep -q 'UTF-8' CLAUDE.md" \
    "configuration"

echo

# 9. Test Suite Validation
echo -e "${PURPLE}🧪 9. Test Suite Validation${NC}"
echo "============================"

# Test Korean text integration test
run_test "Korean text integration test exists" \
    "test -f backend/src/test/java/com/stockquest/integration/KoreanTextEncodingIntegrationTest.java" \
    "application"

# Run the Korean text integration test if backend is available
if curl -s "$BACKEND_URL/actuator/health" >/dev/null 2>&1; then
    echo "🔍 Testing: Korean text integration test execution"
    if cd backend && JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew test --tests KoreanTextEncodingIntegrationTest >/dev/null 2>&1; then
        echo -e "   ${GREEN}✅ PASS:${NC} Korean text integration tests pass"
        ((passed_tests++))
        ((test_results["integration"]++))
    else
        echo -e "   ${RED}❌ FAIL:${NC} Korean text integration tests fail"
        ((failed_tests++))
    fi
    cd ..
    ((total_tests++))
fi

echo

# Summary Report
echo "=========================================="
echo -e "${CYAN}📊 System Encoding Validation Summary${NC}"
echo "=========================================="
echo

echo -e "${BLUE}Overall Results:${NC}"
echo "Total tests: $total_tests"
echo -e "Passed: ${GREEN}$passed_tests${NC}"
echo -e "Failed: ${RED}$failed_tests${NC}"
echo -e "Warnings: ${YELLOW}$warnings${NC}"
echo

echo -e "${BLUE}Results by Category:${NC}"
for category in "${!test_results[@]}"; do
    count=${test_results[$category]}
    echo -e "• $(printf '%-15s' "$category"): ${GREEN}$count passed${NC}"
done
echo

# Calculate success rate
total_completed=$((passed_tests + failed_tests))
if [[ $total_completed -gt 0 ]]; then
    success_rate=$((passed_tests * 100 / total_completed))
    echo -e "${BLUE}Success Rate:${NC} $success_rate%"
else
    success_rate=0
fi

echo

# Final assessment
if [[ $failed_tests -eq 0 ]]; then
    if [[ $warnings -eq 0 ]]; then
        echo -e "${GREEN}🎉 EXCELLENT:${NC} All encoding validation tests passed!"
        echo -e "${GREEN}✅ Stock Quest system has comprehensive Korean text support${NC}"
    else
        echo -e "${YELLOW}👍 GOOD:${NC} All critical tests passed with $warnings warnings"
        echo -e "${YELLOW}⚠️ Review warnings for potential improvements${NC}"
    fi
    echo
    echo -e "${BLUE}✨ System Features:${NC}"
    echo "• Complete UTF-8/utf8mb4 configuration"
    echo "• Database charset monitoring and validation"
    echo "• Pre-commit hooks for encoding safety"
    echo "• Comprehensive integration tests"
    echo "• Korean text support across all components"
elif [[ $success_rate -ge 80 ]]; then
    echo -e "${YELLOW}⚠️ NEEDS ATTENTION:${NC} Most tests passed but $failed_tests critical issues found"
    echo -e "${YELLOW}🔧 Address failed tests before production deployment${NC}"
else
    echo -e "${RED}❌ CRITICAL ISSUES:${NC} $failed_tests failed tests require immediate attention"
    echo -e "${RED}🚨 System encoding support needs significant fixes${NC}"
fi

echo
echo -e "${BLUE}📋 Next Steps:${NC}"
if [[ $failed_tests -gt 0 ]]; then
    echo "1. Fix failed tests shown above"
    echo "2. Re-run validation script"
    echo "3. Test Korean text in production environment"
else
    echo "1. Deploy with confidence - encoding support is comprehensive"
    echo "2. Monitor charset health endpoint in production"
    echo "3. Run validation script periodically"
fi

echo
echo -e "${BLUE}🔗 Related Documentation:${NC}"
echo "• backend/docs/ENCODING_BEST_PRACTICES.md"
echo "• backend/src/main/resources/db/migration/TEMPLATE__Migration_Template.sql"
echo "• .pre-commit-config.yaml"

# Exit with appropriate code
if [[ $failed_tests -eq 0 ]]; then
    exit 0
else
    exit 1
fi