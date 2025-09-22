#!/bin/bash

# Pre-commit hook for validating charset configuration in application settings
# This script ensures all configuration files have proper UTF-8 charset settings

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration patterns
CONFIG_PATTERN='application.*\.(yml|yaml|properties)$'
REQUIRED_CHARSET_PARAMS=(
    "characterEncoding=UTF-8"
    "useUnicode=true"
    "connection-init-sql.*utf8mb4"
)

# Statistics
total_files=0
checked_files=0
issues_found=0

echo -e "${BLUE}⚙️  Configuration Charset Validation${NC}"
echo "===================================="

# Function to check YAML/YML configuration
check_yaml_config() {
    local file="$1"
    local file_issues=0

    echo -e "${BLUE}🔍 Checking YAML config:${NC} $file"

    # Check for datasource URL configuration
    if grep -q "spring:" "$file" && grep -q "datasource:" "$file"; then
        # Check for required charset parameters in datasource URL
        local has_char_encoding=false
        local has_unicode=false
        local has_init_sql=false

        if grep -q "characterEncoding.*UTF-8\|characterEncoding.*utf-8" "$file"; then
            has_char_encoding=true
        fi

        if grep -q "useUnicode.*true" "$file"; then
            has_unicode=true
        fi

        if grep -q "connection-init-sql.*utf8mb4\|connectionInitSql.*utf8mb4" "$file"; then
            has_init_sql=true
        fi

        # Report missing configurations
        if ! $has_char_encoding; then
            echo -e "${RED}❌ ERROR:${NC} Missing characterEncoding=UTF-8 in datasource URL"
            echo -e "   ${BLUE}Fix:${NC} Add characterEncoding=UTF-8 to spring.datasource.url"
            ((file_issues++))
        fi

        if ! $has_unicode; then
            echo -e "${RED}❌ ERROR:${NC} Missing useUnicode=true in datasource URL"
            echo -e "   ${BLUE}Fix:${NC} Add useUnicode=true to spring.datasource.url"
            ((file_issues++))
        fi

        if ! $has_init_sql; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Missing connection-init-sql with utf8mb4"
            echo -e "   ${BLUE}Recommendation:${NC} Add hikari.connection-init-sql: \"SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci\""
            echo -e "   ${BLUE}Location:${NC} Under spring.datasource.hikari"
        fi

        # Check for server timezone (recommended)
        if ! grep -q "serverTimezone" "$file"; then
            echo -e "${BLUE}ℹ️  INFO:${NC} Consider adding serverTimezone=UTC to datasource URL"
        fi

        # Show correct configuration example
        if [[ $file_issues -gt 0 ]]; then
            echo
            echo -e "${BLUE}📋 Correct Configuration Example:${NC}"
            echo "spring:"
            echo "  datasource:"
            echo "    url: jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
            echo "    hikari:"
            echo "      connection-init-sql: \"SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci\""
            echo
        fi
    fi

    # Check for JPA/Hibernate configuration
    if grep -q "jpa:" "$file"; then
        # Check for explicit charset configuration in JPA
        if ! grep -q "hibernate.connection.CharSet\|hibernate.connection.characterEncoding" "$file"; then
            echo -e "${BLUE}ℹ️  INFO:${NC} JPA configuration relies on datasource charset settings"
        fi
    fi

    # Check for internationalization settings
    if grep -q "spring:" "$file"; then
        if grep -q "messages:" "$file"; then
            if ! grep -q "encoding.*UTF-8\|encoding.*utf-8" "$file"; then
                echo -e "${YELLOW}⚠️  WARNING:${NC} Consider setting spring.messages.encoding: UTF-8"
            fi
        fi
    fi

    return $file_issues
}

# Function to check Properties configuration
check_properties_config() {
    local file="$1"
    local file_issues=0

    echo -e "${BLUE}🔍 Checking Properties config:${NC} $file"

    # Check datasource URL in properties format
    if grep -q "spring.datasource.url" "$file"; then
        local url_line
        url_line=$(grep "spring.datasource.url" "$file")

        if ! echo "$url_line" | grep -q "characterEncoding=UTF-8"; then
            echo -e "${RED}❌ ERROR:${NC} Missing characterEncoding=UTF-8 in datasource URL"
            echo -e "   Line: ${YELLOW}$url_line${NC}"
            echo -e "   ${BLUE}Fix:${NC} Add characterEncoding=UTF-8 to URL parameters"
            ((file_issues++))
        fi

        if ! echo "$url_line" | grep -q "useUnicode=true"; then
            echo -e "${RED}❌ ERROR:${NC} Missing useUnicode=true in datasource URL"
            echo -e "   Line: ${YELLOW}$url_line${NC}"
            echo -e "   ${BLUE}Fix:${NC} Add useUnicode=true to URL parameters"
            ((file_issues++))
        fi
    fi

    # Check for HikariCP init SQL
    if ! grep -q "spring.datasource.hikari.connection-init-sql.*utf8mb4" "$file"; then
        if grep -q "spring.datasource" "$file"; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Missing HikariCP connection-init-sql with utf8mb4"
            echo -e "   ${BLUE}Add:${NC} spring.datasource.hikari.connection-init-sql=SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
        fi
    fi

    # Check for messages encoding
    if grep -q "spring.messages" "$file"; then
        if ! grep -q "spring.messages.encoding=UTF-8" "$file"; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Consider setting spring.messages.encoding=UTF-8"
        fi
    fi

    if [[ $file_issues -gt 0 ]]; then
        echo
        echo -e "${BLUE}📋 Correct Properties Configuration:${NC}"
        echo "spring.datasource.url=jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
        echo "spring.datasource.hikari.connection-init-sql=SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
        echo "spring.messages.encoding=UTF-8"
        echo
    fi

    return $file_issues
}

# Function to check environment-specific configurations
check_environment_config() {
    local file="$1"

    # Get environment from filename
    local env=""
    if echo "$file" | grep -q "dev"; then
        env="development"
    elif echo "$file" | grep -q "prod"; then
        env="production"
    elif echo "$file" | grep -q "test"; then
        env="test"
    fi

    if [[ -n "$env" ]]; then
        echo -e "${BLUE}ℹ️  Environment:${NC} $env"

        # Production-specific checks
        if [[ "$env" == "production" ]]; then
            if ! grep -q "connection-init-sql" "$file"; then
                echo -e "${RED}❌ CRITICAL:${NC} Production MUST have connection-init-sql for charset safety"
                echo -e "   ${BLUE}Fix:${NC} Add connection-init-sql: \"SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci\""
                return 1
            fi
        fi
    fi

    return 0
}

# Function to validate single configuration file
validate_config_file() {
    local file="$1"
    local file_issues=0

    # Check if file is readable
    if [[ ! -r "$file" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Cannot read file: $file"
        return 1
    fi

    # Check file type and validate accordingly
    if [[ "$file" == *.yml || "$file" == *.yaml ]]; then
        if ! check_yaml_config "$file"; then
            ((file_issues++))
        fi
    elif [[ "$file" == *.properties ]]; then
        if ! check_properties_config "$file"; then
            ((file_issues++))
        fi
    fi

    # Environment-specific checks
    if ! check_environment_config "$file"; then
        ((file_issues++))
    fi

    # Check for Korean text in configuration (should be in UTF-8)
    if grep -q '[가-힣]' "$file"; then
        echo -e "${BLUE}ℹ️  INFO:${NC} Korean text found in configuration"

        # Verify file encoding
        local encoding
        encoding=$(file -b --mime-encoding "$file" 2>/dev/null || echo "unknown")
        if [[ "$encoding" != "utf-8" ]]; then
            echo -e "${RED}❌ ERROR:${NC} Configuration file with Korean text must be UTF-8 encoded"
            echo -e "   Current encoding: ${YELLOW}$encoding${NC}"
            ((file_issues++))
        fi
    fi

    if [[ $file_issues -eq 0 ]]; then
        echo -e "${GREEN}✅ PASSED:${NC} Configuration charset settings are correct"
    else
        echo -e "${RED}❌ FAILED:${NC} $file_issues configuration issues found"
        ((issues_found += file_issues))
    fi

    echo
    return $file_issues
}

# Function to show configuration best practices
show_config_best_practices() {
    echo -e "${BLUE}🏆 Configuration Best Practices${NC}"
    echo "================================"
    echo
    echo -e "${BLUE}1. MySQL Datasource URL (Required):${NC}"
    echo "   jdbc:mysql://host:port/db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC"
    echo
    echo -e "${BLUE}2. HikariCP Connection Initialization (Required):${NC}"
    echo "   connection-init-sql: \"SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci\""
    echo
    echo -e "${BLUE}3. Messages Encoding (Recommended):${NC}"
    echo "   spring.messages.encoding: UTF-8"
    echo
    echo -e "${BLUE}4. Complete YAML Example:${NC}"
    cat << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockquest?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    username: ${DB_USERNAME:stockquest}
    password: ${DB_PASSWORD:password}
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
      validation-timeout: 5000
      connection-test-query: "SELECT 1"
  messages:
    encoding: UTF-8
    basename: messages
EOF
    echo
    echo -e "${BLUE}5. Validation Commands:${NC}"
    echo "   # Check MySQL charset"
    echo "   mysql> SHOW VARIABLES LIKE 'character_set_%';"
    echo
    echo "   # Test Korean text"
    echo "   mysql> SELECT '한국어 테스트' as test_korean;"
}

# Main validation logic
main() {
    local exit_code=0

    # If specific files are passed as arguments, check only those
    if [[ $# -gt 0 ]]; then
        for file in "$@"; do
            # Check if file matches configuration pattern
            if echo "$file" | grep -E "$CONFIG_PATTERN" > /dev/null; then
                ((total_files++))
                if [[ -f "$file" ]]; then
                    ((checked_files++))
                    if ! validate_config_file "$file"; then
                        exit_code=1
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING:${NC} File not found: $file"
                fi
            fi
        done
    else
        # Check all configuration files in the project
        while IFS= read -r -d '' file; do
            ((total_files++))
            ((checked_files++))
            if ! validate_config_file "$file"; then
                exit_code=1
            fi
        done < <(find . -type f | grep -E "$CONFIG_PATTERN" | grep -v node_modules | grep -v ".git" -print0 2>/dev/null)
    fi

    # Summary
    echo "===================================="
    echo -e "${BLUE}📊 Configuration Charset Summary${NC}"
    echo "Total config files found: $total_files"
    echo "Files checked: $checked_files"

    if [[ $issues_found -eq 0 ]]; then
        echo -e "${GREEN}✅ SUCCESS:${NC} All configuration files have proper charset settings"
        echo
        echo -e "${BLUE}💡 Maintenance Tips:${NC}"
        echo "• Test database connection with Korean text after configuration changes"
        echo "• Verify charset settings in all environments (dev/staging/prod)"
        echo "• Monitor application logs for charset-related warnings"
    else
        echo -e "${RED}❌ FAILURE:${NC} $issues_found configuration issues need to be fixed"
        echo
        show_config_best_practices
        exit_code=1
    fi

    return $exit_code
}

# Run main function with all arguments
main "$@"