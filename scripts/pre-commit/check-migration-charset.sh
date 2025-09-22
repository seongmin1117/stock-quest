#!/bin/bash

# Pre-commit hook for validating database migration charset configuration
# This script ensures all SQL migration files include proper UTF-8 charset declarations

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REQUIRED_CHARSET="utf8mb4"
REQUIRED_COLLATION="utf8mb4_unicode_ci"
MIGRATION_PATTERN="backend/src/main/resources/db/migration/V.*\.sql$"

# Statistics
total_files=0
checked_files=0
issues_found=0

echo -e "${BLUE}📊 Database Migration Charset Validation${NC}"
echo "=========================================="

# Function to check CREATE TABLE statements
check_create_table_charset() {
    local file="$1"
    local line_num="$2"
    local line="$3"

    # Skip if this is just a comment about charset
    if echo "$line" | grep -q "COMMENT.*charset\|-- .*charset"; then
        return 0
    fi

    # Check if CREATE TABLE statement includes proper charset
    if echo "$line" | grep -i "CREATE TABLE" > /dev/null; then
        local full_statement=""
        local current_line="$line_num"

        # Read the complete CREATE TABLE statement (may span multiple lines)
        while IFS= read -r next_line || [[ -n "$next_line" ]]; do
            full_statement+=" $next_line"
            if echo "$next_line" | grep -q ");"; then
                break
            fi
            ((current_line++))
        done < <(tail -n +$line_num "$file")

        # Check if charset specification is present
        if ! echo "$full_statement" | grep -i "CHARSET.*utf8mb4\|DEFAULT CHARSET.*utf8mb4" > /dev/null; then
            echo -e "${RED}❌ ERROR:${NC} Missing charset specification in CREATE TABLE statement"
            echo -e "   File: ${YELLOW}$file${NC}"
            echo -e "   Line: ${YELLOW}$line_num${NC}"
            echo -e "   Statement: $(echo "$full_statement" | tr -s ' ' | head -c 100)..."
            echo -e "   ${BLUE}Fix:${NC} Add 'ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci' to CREATE TABLE statement"
            echo
            return 1
        fi

        # Check if collation is properly specified
        if ! echo "$full_statement" | grep -i "COLLATE.*utf8mb4_unicode_ci" > /dev/null; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Missing collation specification in CREATE TABLE statement"
            echo -e "   File: ${YELLOW}$file${NC}"
            echo -e "   Line: ${YELLOW}$line_num${NC}"
            echo -e "   ${BLUE}Recommendation:${NC} Add 'COLLATE utf8mb4_unicode_ci' for explicit collation"
            echo
        fi
    fi

    return 0
}

# Function to check for Korean text without charset context
check_korean_text_charset() {
    local file="$1"
    local line_num="$2"
    local line="$3"

    # Check if line contains Korean characters
    if echo "$line" | grep -P '[\p{Hangul}]' > /dev/null 2>/dev/null || echo "$line" | grep -E '[가-힣]' > /dev/null; then
        # Check if this is in a CREATE TABLE context without proper charset
        local context_lines=""
        local start_line=$((line_num - 5 > 1 ? line_num - 5 : 1))
        local end_line=$((line_num + 5))

        context_lines=$(sed -n "${start_line},${end_line}p" "$file")

        if echo "$context_lines" | grep -i "CREATE TABLE" > /dev/null; then
            if ! echo "$context_lines" | grep -i "utf8mb4" > /dev/null; then
                echo -e "${RED}❌ ERROR:${NC} Korean text found in CREATE TABLE without utf8mb4 charset"
                echo -e "   File: ${YELLOW}$file${NC}"
                echo -e "   Line: ${YELLOW}$line_num${NC}"
                echo -e "   Korean text: ${YELLOW}$(echo "$line" | grep -oE '[가-힣]+' | head -3 | tr '\n' ' ')${NC}"
                echo -e "   ${BLUE}Fix:${NC} Ensure table uses 'ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'"
                echo
                return 1
            fi
        fi
    fi

    return 0
}

# Function to validate single migration file
validate_migration_file() {
    local file="$1"
    local file_issues=0

    echo -e "${BLUE}🔍 Checking:${NC} $file"

    # Check if file is readable
    if [[ ! -r "$file" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Cannot read file: $file"
        return 1
    fi

    # Read file line by line
    local line_num=0
    while IFS= read -r line || [[ -n "$line" ]]; do
        ((line_num++))

        # Check CREATE TABLE charset
        if ! check_create_table_charset "$file" "$line_num" "$line"; then
            ((file_issues++))
        fi

        # Check Korean text charset context
        if ! check_korean_text_charset "$file" "$line_num" "$line"; then
            ((file_issues++))
        fi

    done < "$file"

    if [[ $file_issues -eq 0 ]]; then
        echo -e "${GREEN}✅ PASSED:${NC} No charset issues found"
    else
        echo -e "${RED}❌ FAILED:${NC} $file_issues charset issues found"
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
            # Check if file matches migration pattern
            if echo "$file" | grep -E "$MIGRATION_PATTERN" > /dev/null; then
                ((total_files++))
                if [[ -f "$file" ]]; then
                    ((checked_files++))
                    if ! validate_migration_file "$file"; then
                        exit_code=1
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING:${NC} File not found: $file"
                fi
            fi
        done
    else
        # Check all migration files in the project
        while IFS= read -r -d '' file; do
            ((total_files++))
            ((checked_files++))
            if ! validate_migration_file "$file"; then
                exit_code=1
            fi
        done < <(find . -path "*/db/migration/V*.sql" -type f -print0 2>/dev/null)
    fi

    # Summary
    echo "=========================================="
    echo -e "${BLUE}📊 Migration Charset Validation Summary${NC}"
    echo "Total migration files found: $total_files"
    echo "Files checked: $checked_files"

    if [[ $issues_found -eq 0 ]]; then
        echo -e "${GREEN}✅ SUCCESS:${NC} All migration files have proper charset configuration"
        echo
        echo -e "${BLUE}💡 Best Practices:${NC}"
        echo "• Always use: ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        echo "• Test Korean text with: INSERT INTO table (column) VALUES ('한국어 테스트');"
        echo "• Use migration template: backend/src/main/resources/db/migration/TEMPLATE__Migration_Template.sql"
    else
        echo -e "${RED}❌ FAILURE:${NC} $issues_found charset issues need to be fixed"
        echo
        echo -e "${BLUE}🔧 How to Fix:${NC}"
        echo "1. Add charset specification to CREATE TABLE statements:"
        echo "   ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        echo
        echo "2. Use the migration template for new migrations:"
        echo "   cp backend/src/main/resources/db/migration/TEMPLATE__Migration_Template.sql \\"
        echo "      backend/src/main/resources/db/migration/V{number}__{description}.sql"
        echo
        echo "3. Test Korean text handling after applying migration"
        exit_code=1
    fi

    return $exit_code
}

# Run main function with all arguments
main "$@"