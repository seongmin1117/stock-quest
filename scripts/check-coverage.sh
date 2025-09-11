#!/bin/bash

# StockQuest Test Coverage Check Script
# Ensures test coverage doesn't decrease and maintains quality thresholds

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Coverage thresholds
MIN_UNIT_COVERAGE=80
MIN_INTEGRATION_COVERAGE=70
MIN_E2E_COVERAGE=60
WARN_UNIT_COVERAGE=85
WARN_INTEGRATION_COVERAGE=75

echo "📊 Checking test coverage..."

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Not in a git repository, skipping coverage comparison${NC}"
    COMPARE_COVERAGE=false
else
    COMPARE_COVERAGE=true
fi

# Function to extract coverage from file
extract_coverage() {
    local coverage_file="$1"
    local coverage_type="$2"
    
    if [ ! -f "$coverage_file" ]; then
        echo "0"
        return
    fi
    
    case "$coverage_type" in
        "jacoco")
            # Extract line coverage from JaCoCo XML report
            if command -v xmllint >/dev/null 2>&1; then
                xmllint --xpath "//report/counter[@type='LINE']/@covered" "$coverage_file" 2>/dev/null | sed 's/covered="//g' | sed 's/"//g' || echo "0"
            else
                grep -o 'covered="[0-9]*"' "$coverage_file" | head -1 | sed 's/covered="//g' | sed 's/"//g' || echo "0"
            fi
            ;;
        "jest")
            # Extract coverage from Jest JSON report
            if command -v jq >/dev/null 2>&1 && [ -f "$coverage_file" ]; then
                jq '.total.lines.pct' "$coverage_file" 2>/dev/null || echo "0"
            else
                echo "0"
            fi
            ;;
        "lcov")
            # Extract coverage from LCOV info file
            if [ -f "$coverage_file" ]; then
                lcov --summary "$coverage_file" 2>/dev/null | grep -o "lines......: [0-9.]*%" | sed 's/lines......: //g' | sed 's/%//g' || echo "0"
            else
                echo "0"
            fi
            ;;
        *)
            echo "0"
            ;;
    esac
}

# Function to check backend coverage
check_backend_coverage() {
    echo ""
    echo -e "${BLUE}🔧 Backend Coverage (Java/Spring Boot)${NC}"
    
    # Backend paths
    BACKEND_JACOCO_REPORT="backend/build/reports/jacoco/test/jacocoTestReport.xml"
    BACKEND_JACOCO_HTML="backend/build/reports/jacoco/test/html/index.html"
    
    # Check if backend coverage exists
    if [ ! -f "$BACKEND_JACOCO_REPORT" ]; then
        echo -e "${YELLOW}⚠️  Backend coverage report not found${NC}"
        echo "   Run: cd backend && ./gradlew test jacocoTestReport"
        return 1
    fi
    
    # Extract coverage metrics
    BACKEND_LINE_COVERAGE=$(extract_coverage "$BACKEND_JACOCO_REPORT" "jacoco")
    
    if [ "$BACKEND_LINE_COVERAGE" = "0" ]; then
        echo -e "${RED}❌ Could not extract backend coverage metrics${NC}"
        return 1
    fi
    
    echo "  📈 Line Coverage: ${BACKEND_LINE_COVERAGE}%"
    
    # Validate against thresholds
    if (( $(echo "$BACKEND_LINE_COVERAGE < $MIN_UNIT_COVERAGE" | bc -l) )); then
        echo -e "${RED}❌ Backend coverage below minimum threshold!${NC}"
        echo "   Current: ${BACKEND_LINE_COVERAGE}% | Minimum: ${MIN_UNIT_COVERAGE}%"
        echo "   Please add more unit tests to improve coverage."
        return 1
    elif (( $(echo "$BACKEND_LINE_COVERAGE < $WARN_UNIT_COVERAGE" | bc -l) )); then
        echo -e "${YELLOW}⚠️  Backend coverage below recommended threshold${NC}"
        echo "   Current: ${BACKEND_LINE_COVERAGE}% | Recommended: ${WARN_UNIT_COVERAGE}%"
    else
        echo -e "${GREEN}✅ Backend coverage meets standards${NC}"
    fi
    
    # Show coverage report location
    if [ -f "$BACKEND_JACOCO_HTML" ]; then
        echo "   📋 Detailed report: $BACKEND_JACOCO_HTML"
    fi
    
    return 0
}

# Function to check frontend coverage
check_frontend_coverage() {
    echo ""
    echo -e "${BLUE}🎨 Frontend Coverage (React/TypeScript)${NC}"
    
    # Frontend paths
    FRONTEND_JSON_REPORT="frontend/coverage/coverage-summary.json"
    FRONTEND_LCOV_REPORT="frontend/coverage/lcov.info"
    FRONTEND_HTML_REPORT="frontend/coverage/lcov-report/index.html"
    
    # Check if frontend coverage exists
    if [ ! -f "$FRONTEND_JSON_REPORT" ] && [ ! -f "$FRONTEND_LCOV_REPORT" ]; then
        echo -e "${YELLOW}⚠️  Frontend coverage report not found${NC}"
        echo "   Run: cd frontend && pnpm test:coverage"
        return 1
    fi
    
    # Extract coverage metrics
    FRONTEND_COVERAGE=0
    if [ -f "$FRONTEND_JSON_REPORT" ]; then
        FRONTEND_COVERAGE=$(extract_coverage "$FRONTEND_JSON_REPORT" "jest")
    elif [ -f "$FRONTEND_LCOV_REPORT" ]; then
        FRONTEND_COVERAGE=$(extract_coverage "$FRONTEND_LCOV_REPORT" "lcov")
    fi
    
    if [ "$FRONTEND_COVERAGE" = "0" ]; then
        echo -e "${RED}❌ Could not extract frontend coverage metrics${NC}"
        return 1
    fi
    
    echo "  📈 Line Coverage: ${FRONTEND_COVERAGE}%"
    
    # Validate against thresholds
    if (( $(echo "$FRONTEND_COVERAGE < $MIN_UNIT_COVERAGE" | bc -l) )); then
        echo -e "${RED}❌ Frontend coverage below minimum threshold!${NC}"
        echo "   Current: ${FRONTEND_COVERAGE}% | Minimum: ${MIN_UNIT_COVERAGE}%"
        echo "   Please add more unit tests to improve coverage."
        return 1
    elif (( $(echo "$FRONTEND_COVERAGE < $WARN_UNIT_COVERAGE" | bc -l) )); then
        echo -e "${YELLOW}⚠️  Frontend coverage below recommended threshold${NC}"
        echo "   Current: ${FRONTEND_COVERAGE}% | Recommended: ${WARN_UNIT_COVERAGE}%"
    else
        echo -e "${GREEN}✅ Frontend coverage meets standards${NC}"
    fi
    
    # Show coverage report location
    if [ -f "$FRONTEND_HTML_REPORT" ]; then
        echo "   📋 Detailed report: $FRONTEND_HTML_REPORT"
    fi
    
    return 0
}

# Function to check E2E coverage
check_e2e_coverage() {
    echo ""
    echo -e "${BLUE}🧪 E2E Test Coverage (Playwright)${NC}"
    
    # E2E paths
    E2E_REPORT="e2e/test-results/results.json"
    E2E_HTML_REPORT="e2e/test-results/index.html"
    
    # Check if E2E tests exist
    E2E_TEST_FILES=$(find . -name "*.e2e.ts" -o -name "*.spec.ts" | grep -E "(e2e|test)" | wc -l)
    
    if [ "$E2E_TEST_FILES" -eq 0 ]; then
        echo -e "${YELLOW}⚠️  No E2E tests found${NC}"
        echo "   Consider adding Playwright E2E tests for critical user flows."
        return 0
    fi
    
    echo "  📝 E2E Test Files: $E2E_TEST_FILES"
    
    # Check critical paths coverage
    echo "  🎯 Checking critical path coverage..."
    
    # Define critical paths for StockQuest
    CRITICAL_PATHS=(
        "authentication" "login" "signup"
        "portfolio" "stock" "trading"
        "challenge" "ranking" "leaderboard"
        "dashboard" "profile" "settings"
    )
    
    COVERED_PATHS=0
    for path in "${CRITICAL_PATHS[@]}"; do
        if find . -name "*.e2e.ts" -o -name "*.spec.ts" | xargs grep -l "$path" >/dev/null 2>&1; then
            COVERED_PATHS=$((COVERED_PATHS + 1))
        fi
    done
    
    E2E_COVERAGE=$(echo "scale=1; $COVERED_PATHS * 100 / ${#CRITICAL_PATHS[@]}" | bc)
    
    echo "  📈 Critical Path Coverage: ${E2E_COVERAGE}% (${COVERED_PATHS}/${#CRITICAL_PATHS[@]})"
    
    if (( $(echo "$E2E_COVERAGE < $MIN_E2E_COVERAGE" | bc -l) )); then
        echo -e "${YELLOW}⚠️  E2E coverage below recommended threshold${NC}"
        echo "   Current: ${E2E_COVERAGE}% | Recommended: ${MIN_E2E_COVERAGE}%"
        echo "   Missing critical paths:"
        for path in "${CRITICAL_PATHS[@]}"; do
            if ! find . -name "*.e2e.ts" -o -name "*.spec.ts" | xargs grep -l "$path" >/dev/null 2>&1; then
                echo "   - $path"
            fi
        done
    else
        echo -e "${GREEN}✅ E2E coverage meets standards${NC}"
    fi
    
    return 0
}

# Function to compare with previous coverage
compare_coverage() {
    if [ "$COMPARE_COVERAGE" = false ]; then
        return 0
    fi
    
    echo ""
    echo -e "${BLUE}📊 Coverage Comparison${NC}"
    
    # Get current branch
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
    
    # Try to get coverage from main/develop branch
    MAIN_BRANCH="main"
    if ! git rev-parse --verify "$MAIN_BRANCH" >/dev/null 2>&1; then
        MAIN_BRANCH="develop"
        if ! git rev-parse --verify "$MAIN_BRANCH" >/dev/null 2>&1; then
            echo -e "${YELLOW}⚠️  No main/develop branch found for comparison${NC}"
            return 0
        fi
    fi
    
    if [ "$CURRENT_BRANCH" = "$MAIN_BRANCH" ]; then
        echo -e "${GREEN}✅ On main branch, skipping coverage comparison${NC}"
        return 0
    fi
    
    echo "  🔄 Comparing coverage with $MAIN_BRANCH branch..."
    
    # This would require more complex implementation to checkout main branch
    # and run tests, which might be too expensive for pre-commit hook
    echo -e "${BLUE}ℹ️  Coverage comparison not implemented yet${NC}"
    echo "   Consider implementing coverage difference tracking in CI/CD"
    
    return 0
}

# Main execution
EXIT_CODE=0

# Check for required tools
if ! command -v bc >/dev/null 2>&1; then
    echo -e "${RED}❌ 'bc' command not found. Please install bc for coverage calculations.${NC}"
    exit 1
fi

# Run coverage checks
check_backend_coverage || EXIT_CODE=1
check_frontend_coverage || EXIT_CODE=1
check_e2e_coverage || EXIT_CODE=1
compare_coverage || EXIT_CODE=1

echo ""
echo "📋 Coverage Summary:"
echo "  📊 Backend: Unit tests should be ≥${MIN_UNIT_COVERAGE}%"
echo "  📊 Frontend: Unit tests should be ≥${MIN_UNIT_COVERAGE}%"
echo "  📊 E2E: Critical paths should be ≥${MIN_E2E_COVERAGE}%"

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 All coverage checks passed!${NC}"
    echo ""
    echo "💡 Tips for maintaining good coverage:"
    echo "  • Write unit tests for new features"
    echo "  • Add integration tests for API endpoints"
    echo "  • Create E2E tests for critical user flows"
    echo "  • Run coverage reports locally before committing"
    echo ""
    echo "🔧 Commands to run coverage locally:"
    echo "  Backend:  cd backend && ./gradlew test jacocoTestReport"
    echo "  Frontend: cd frontend && pnpm test:coverage"
    echo "  E2E:      cd e2e && pnpm test:e2e"
else
    echo ""
    echo -e "${RED}💥 Coverage checks failed!${NC}"
    echo ""
    echo "🔧 To improve coverage:"
    echo "  1. Add unit tests for uncovered code paths"
    echo "  2. Add integration tests for API endpoints"
    echo "  3. Add E2E tests for critical user workflows"
    echo "  4. Run coverage reports to identify gaps:"
    echo "     - Backend:  ./gradlew test jacocoTestReport"
    echo "     - Frontend: pnpm test:coverage"
    echo ""
    echo "📖 See .claude/TESTING_GUIDELINES.md for testing best practices"
fi

exit $EXIT_CODE