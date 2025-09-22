#!/bin/bash

# Pre-commit hook for validating Docker charset configuration
# This script ensures Docker configurations include proper UTF-8 charset settings

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration patterns
DOCKER_PATTERN='(docker-compose.*\.ya?ml|Dockerfile)$'

# Required MySQL charset settings
REQUIRED_MYSQL_CHARSET_PARAMS=(
    "--character-set-server=utf8mb4"
    "--collation-server=utf8mb4_unicode_ci"
)

# Statistics
total_files=0
checked_files=0
issues_found=0

echo -e "${BLUE}🐳 Docker Charset Configuration Validation${NC}"
echo "==========================================="

# Function to check Docker Compose YAML files
check_docker_compose() {
    local file="$1"
    local file_issues=0

    echo -e "${BLUE}🔍 Checking Docker Compose:${NC} $file"

    # Check for MySQL service configuration
    if grep -q "mysql" "$file"; then
        echo -e "${BLUE}📦 MySQL service detected${NC}"

        # Check for charset command parameters
        local has_charset_server=false
        local has_collation_server=false

        if grep -q "\-\-character-set-server=utf8mb4" "$file"; then
            has_charset_server=true
        fi

        if grep -q "\-\-collation-server=utf8mb4_unicode_ci" "$file"; then
            has_collation_server=true
        fi

        if ! $has_charset_server; then
            echo -e "${RED}❌ ERROR:${NC} Missing --character-set-server=utf8mb4 in MySQL command"
            echo -e "   ${BLUE}Fix:${NC} Add to MySQL service command section"
            ((file_issues++))
        fi

        if ! $has_collation_server; then
            echo -e "${RED}❌ ERROR:${NC} Missing --collation-server=utf8mb4_unicode_ci in MySQL command"
            echo -e "   ${BLUE}Fix:${NC} Add to MySQL service command section"
            ((file_issues++))
        fi

        # Check for environment variables
        if grep -q "MYSQL_DATABASE" "$file"; then
            echo -e "${GREEN}✅ INFO:${NC} MySQL database environment variable found"
        fi

        # Check for volume mounts (important for data persistence)
        if grep -q "volumes:" "$file" && grep -A 5 -B 5 "mysql" "$file" | grep -q "volumes:"; then
            echo -e "${GREEN}✅ INFO:${NC} MySQL volume mount configured"
        else
            echo -e "${YELLOW}⚠️  WARNING:${NC} Consider adding volume mount for MySQL data persistence"
        fi

        # Check for port configuration
        if grep -A 10 -B 10 "mysql" "$file" | grep -q "3306:3306\|ports:"; then
            echo -e "${GREEN}✅ INFO:${NC} MySQL port configuration found"
        fi

        # Show correct MySQL configuration if issues found
        if [[ $file_issues -gt 0 ]]; then
            show_mysql_docker_config
        fi
    fi

    # Check for Redis service (optional but good to validate)
    if grep -q "redis" "$file"; then
        echo -e "${BLUE}📦 Redis service detected${NC}"

        # Redis doesn't need charset configuration, but check for basic setup
        if grep -A 5 -B 5 "redis" "$file" | grep -q "volumes:"; then
            echo -e "${GREEN}✅ INFO:${NC} Redis volume mount configured"
        fi
    fi

    # Check for application service using database
    if grep -q "depends_on:" "$file" && grep -A 10 "depends_on:" "$file" | grep -q "mysql"; then
        echo -e "${GREEN}✅ INFO:${NC} Application service properly depends on MySQL"
    fi

    # Check for network configuration
    if grep -q "networks:" "$file"; then
        echo -e "${GREEN}✅ INFO:${NC} Custom network configuration found"
    fi

    return $file_issues
}

# Function to check Dockerfile
check_dockerfile() {
    local file="$1"
    local file_issues=0

    echo -e "${BLUE}🔍 Checking Dockerfile:${NC} $file"

    # Check base image for potential charset issues
    local base_image
    base_image=$(grep "^FROM" "$file" | head -1 | awk '{print $2}')

    if [[ -n "$base_image" ]]; then
        echo -e "${BLUE}📦 Base image:${NC} $base_image"

        # Check for Alpine Linux (common charset issues)
        if echo "$base_image" | grep -q "alpine"; then
            echo -e "${YELLOW}⚠️  WARNING:${NC} Alpine Linux detected"
            echo -e "   ${BLUE}Note:${NC} Ensure musl libc supports UTF-8 properly"

            # Check if locale packages are installed
            if ! grep -q "apk.*locale\|apk.*musl-locales" "$file"; then
                echo -e "${YELLOW}⚠️  WARNING:${NC} Consider installing locale support for Alpine"
                echo -e "   ${BLUE}Add:${NC} RUN apk add --no-cache musl-locales"
            fi
        fi

        # Check for Ubuntu/Debian (usually good charset support)
        if echo "$base_image" | grep -E "(ubuntu|debian)" > /dev/null; then
            echo -e "${GREEN}✅ INFO:${NC} Ubuntu/Debian base has good UTF-8 support"
        fi
    fi

    # Check for Java applications (common in this project)
    if grep -q "java\|openjdk" "$file"; then
        echo -e "${BLUE}☕ Java application detected${NC}"

        # Check for explicit charset/locale settings
        if grep -q "LANG=\|LC_ALL=\|file.encoding=" "$file"; then
            echo -e "${GREEN}✅ INFO:${NC} Explicit locale/charset configuration found"
        else
            echo -e "${BLUE}ℹ️  INFO:${NC} Consider adding explicit charset environment variables"
            echo -e "   ${BLUE}Example:${NC} ENV LANG=C.UTF-8 LC_ALL=C.UTF-8"
        fi

        # Check for JVM charset options
        if grep -q "Dfile.encoding=UTF-8\|Dfile.encoding=utf-8" "$file"; then
            echo -e "${GREEN}✅ INFO:${NC} JVM UTF-8 encoding explicitly set"
        else
            echo -e "${BLUE}ℹ️  INFO:${NC} Consider adding JVM charset options"
            echo -e "   ${BLUE}Example:${NC} JAVA_OPTS=\"-Dfile.encoding=UTF-8\""
        fi
    fi

    # Check for Node.js applications
    if grep -q "node\|npm" "$file"; then
        echo -e "${BLUE}🟢 Node.js application detected${NC}"
        echo -e "${GREEN}✅ INFO:${NC} Node.js has good UTF-8 support by default"
    fi

    # Check for COPY/ADD instructions with potential text files
    local copy_instructions
    copy_instructions=$(grep -n "COPY\|ADD" "$file" | grep -E "\.(yml|yaml|properties|sql|md|txt)" || true)

    if [[ -n "$copy_instructions" ]]; then
        echo -e "${BLUE}📄 Text files being copied into container:${NC}"
        echo "$copy_instructions" | while read -r line; do
            echo -e "   ${YELLOW}$(echo "$line" | cut -d: -f1):${NC} $(echo "$line" | cut -d: -f2-)"
        done
        echo -e "${BLUE}ℹ️  Ensure:${NC} All copied text files are UTF-8 encoded"
    fi

    return $file_issues
}

# Function to show correct MySQL Docker configuration
show_mysql_docker_config() {
    echo
    echo -e "${BLUE}📋 Correct MySQL Docker Compose Configuration:${NC}"
    cat << 'EOF'
services:
  mysql:
    image: mysql:8.0
    container_name: stockquest-mysql
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: stockquest
      MYSQL_USER: stockquest
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - stockquest-network

volumes:
  mysql_data:

networks:
  stockquest-network:
    driver: bridge
EOF
    echo
}

# Function to show Dockerfile best practices
show_dockerfile_best_practices() {
    echo -e "${BLUE}🏆 Dockerfile Charset Best Practices${NC}"
    echo "==================================="
    echo
    echo -e "${BLUE}1. Java Applications:${NC}"
    echo "   ENV LANG=C.UTF-8 LC_ALL=C.UTF-8"
    echo "   ENV JAVA_OPTS=\"-Dfile.encoding=UTF-8 -Duser.timezone=UTC\""
    echo
    echo -e "${BLUE}2. Alpine Linux:${NC}"
    echo "   RUN apk add --no-cache musl-locales"
    echo "   ENV LANG=C.UTF-8"
    echo
    echo -e "${BLUE}3. Ubuntu/Debian:${NC}"
    echo "   ENV LANG=C.UTF-8 LC_ALL=C.UTF-8"
    echo "   RUN apt-get update && apt-get install -y locales"
    echo
    echo -e "${BLUE}4. Complete Java Example:${NC}"
    cat << 'EOF'
FROM openjdk:21-jre-slim

# Set charset and locale
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Copy application
COPY target/app.jar /app/app.jar

# Run application
CMD ["java", "-jar", "/app/app.jar"]
EOF
}

# Function to validate single Docker file
validate_docker_file() {
    local file="$1"
    local file_issues=0

    # Check if file is readable
    if [[ ! -r "$file" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Cannot read file: $file"
        return 1
    fi

    # Determine file type and validate accordingly
    if echo "$file" | grep -q "docker-compose.*\.ya?ml"; then
        if ! check_docker_compose "$file"; then
            ((file_issues++))
        fi
    elif echo "$file" | grep -q "Dockerfile"; then
        if ! check_dockerfile "$file"; then
            ((file_issues++))
        fi
    fi

    # Check file encoding (should be UTF-8)
    local encoding
    encoding=$(file -b --mime-encoding "$file" 2>/dev/null || echo "unknown")
    if [[ "$encoding" != "utf-8" && "$encoding" != "us-ascii" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Docker configuration file should be UTF-8 encoded"
        echo -e "   Current encoding: ${YELLOW}$encoding${NC}"
        ((file_issues++))
    fi

    if [[ $file_issues -eq 0 ]]; then
        echo -e "${GREEN}✅ PASSED:${NC} Docker charset configuration looks good"
    else
        echo -e "${RED}❌ FAILED:${NC} $file_issues Docker configuration issues found"
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
            # Check if file matches Docker pattern
            if echo "$file" | grep -E "$DOCKER_PATTERN" > /dev/null; then
                ((total_files++))
                if [[ -f "$file" ]]; then
                    ((checked_files++))
                    if ! validate_docker_file "$file"; then
                        exit_code=1
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING:${NC} File not found: $file"
                fi
            fi
        done
    else
        # Check all Docker files in the project
        while IFS= read -r -d '' file; do
            ((total_files++))
            ((checked_files++))
            if ! validate_docker_file "$file"; then
                exit_code=1
            fi
        done < <(find . -type f | grep -E "$DOCKER_PATTERN" | grep -v node_modules | grep -v ".git" -print0 2>/dev/null)
    fi

    # Summary
    echo "==========================================="
    echo -e "${BLUE}📊 Docker Charset Configuration Summary${NC}"
    echo "Total Docker files found: $total_files"
    echo "Files checked: $checked_files"

    if [[ $issues_found -eq 0 ]]; then
        echo -e "${GREEN}✅ SUCCESS:${NC} All Docker configurations have proper charset support"
        echo
        echo -e "${BLUE}💡 Verification Commands:${NC}"
        echo "• Test MySQL charset: docker exec <mysql-container> mysql -e \"SHOW VARIABLES LIKE 'character_set_%';\""
        echo "• Test application charset: docker exec <app-container> locale"
        echo "• Test Korean text: docker exec <mysql-container> mysql -e \"SELECT '한국어' as test_korean;\""
    else
        echo -e "${RED}❌ FAILURE:${NC} $issues_found Docker configuration issues need to be fixed"
        echo
        show_mysql_docker_config
        show_dockerfile_best_practices
        exit_code=1
    fi

    return $exit_code
}

# Run main function with all arguments
main "$@"