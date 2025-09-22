#!/bin/bash

# Pre-commit hook for validating Korean text encoding in source files
# This script ensures Korean text is properly encoded as UTF-8 and detects potential encoding issues

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
FILE_PATTERNS='\.(java|sql|yml|yaml|properties|md)$'
KOREAN_CHAR_PATTERN='[가-힣]'

# Statistics
total_files=0
checked_files=0
issues_found=0
korean_files=0

echo -e "${BLUE}🔍 Korean Text Encoding Validation${NC}"
echo "=================================="

# Function to check file encoding
check_file_encoding() {
    local file="$1"

    # Check if file contains Korean characters
    if ! grep -l "$KOREAN_CHAR_PATTERN" "$file" > /dev/null 2>&1; then
        return 0  # No Korean text, skip
    fi

    ((korean_files++))
    echo -e "${BLUE}🔍 Checking Korean text in:${NC} $file"

    # Check file encoding using file command
    local encoding_info
    encoding_info=$(file -b --mime-encoding "$file" 2>/dev/null || echo "unknown")

    if [[ "$encoding_info" != "utf-8" && "$encoding_info" != "us-ascii" ]]; then
        echo -e "${RED}❌ ERROR:${NC} File encoding issue detected"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   Detected encoding: ${YELLOW}$encoding_info${NC}"
        echo -e "   Expected: ${GREEN}utf-8${NC}"
        echo -e "   ${BLUE}Fix:${NC} Convert file to UTF-8 encoding"
        echo
        return 1
    fi

    # Check for BOM (Byte Order Mark) which can cause issues
    if [[ -f "$file" ]] && head -c 3 "$file" | grep -q $'\xEF\xBB\xBF'; then
        echo -e "${YELLOW}⚠️  WARNING:${NC} UTF-8 BOM detected"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   ${BLUE}Recommendation:${NC} Remove BOM for better compatibility"
        echo
    fi

    # Check for common encoding corruption patterns
    local corruption_found=0

    # Check for double-encoded UTF-8 (common issue)
    if grep -q 'â\|€\|™\|â€™\|â€œ\|â€\|ê°€\|ë‚˜\|ë‹¤' "$file" 2>/dev/null; then
        echo -e "${RED}❌ ERROR:${NC} Possible double-encoded UTF-8 corruption detected"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   ${BLUE}Fix:${NC} Re-encode file from original source with proper UTF-8"
        ((corruption_found++))
    fi

    # Check for Windows-1252 to UTF-8 conversion issues
    if grep -q 'â\|€™\|€œ\|€\|Ã¡\|Ã©\|Ã­\|Ã³\|Ãº' "$file" 2>/dev/null; then
        echo -e "${RED}❌ ERROR:${NC} Possible Windows-1252 to UTF-8 conversion corruption"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   ${BLUE}Fix:${NC} Re-encode from original Windows-1252 source"
        ((corruption_found++))
    fi

    # Check for ISO-8859-1 corruption
    if grep -q 'Ã\|Â\|Ä\|Ö\|Ü' "$file" 2>/dev/null; then
        echo -e "${RED}❌ ERROR:${NC} Possible ISO-8859-1 encoding corruption"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   ${BLUE}Fix:${NC} Re-encode from original ISO-8859-1 source"
        ((corruption_found++))
    fi

    # Check for question mark replacements (classic encoding failure)
    local korean_line_with_question_marks
    korean_line_with_question_marks=$(grep -n "$KOREAN_CHAR_PATTERN" "$file" | grep '?' | head -5)
    if [[ -n "$korean_line_with_question_marks" ]]; then
        echo -e "${YELLOW}⚠️  WARNING:${NC} Question marks found on lines with Korean text"
        echo -e "   File: ${YELLOW}$file${NC}"
        echo -e "   Lines: ${YELLOW}$(echo "$korean_line_with_question_marks" | cut -d: -f1 | tr '\n' ' ')${NC}"
        echo -e "   ${BLUE}Review:${NC} Check if question marks are intentional or encoding corruption"
        echo
    fi

    # Java-specific checks
    if [[ "$file" == *.java ]]; then
        check_java_korean_encoding "$file"
    fi

    # Properties file checks
    if [[ "$file" == *.properties ]]; then
        check_properties_korean_encoding "$file"
    fi

    # SQL file checks
    if [[ "$file" == *.sql ]]; then
        check_sql_korean_encoding "$file"
    fi

    if [[ $corruption_found -eq 0 ]]; then
        echo -e "${GREEN}✅ PASSED:${NC} Korean text encoding looks correct"
    else
        echo -e "${RED}❌ FAILED:${NC} $corruption_found encoding issues found"
        ((issues_found += corruption_found))
    fi

    echo
    return $corruption_found
}

# Function to check Java-specific Korean text encoding
check_java_korean_encoding() {
    local file="$1"

    # Check for hardcoded Korean strings without proper Unicode escapes in critical areas
    local string_literals
    string_literals=$(grep -n '"[^"]*[가-힣][^"]*"' "$file" || true)

    if [[ -n "$string_literals" ]]; then
        echo -e "${BLUE}ℹ️  INFO:${NC} Korean string literals found in Java file"
        echo -e "   ${BLUE}Best practice:${NC} Consider using ResourceBundle for internationalization"
        echo -e "   Lines with Korean strings: $(echo "$string_literals" | cut -d: -f1 | tr '\n' ' ')"
    fi

    # Check for potential charset issues in file reading operations
    if grep -q "FileReader\|FileWriter\|new.*File.*Stream" "$file" && grep -q "$KOREAN_CHAR_PATTERN" "$file"; then
        echo -e "${YELLOW}⚠️  WARNING:${NC} File I/O operations detected with Korean text"
        echo -e "   ${BLUE}Recommendation:${NC} Ensure charset is explicitly specified:"
        echo -e "   Files.readString(path, StandardCharsets.UTF_8)"
        echo -e "   Files.writeString(path, content, StandardCharsets.UTF_8)"
    fi
}

# Function to check Properties file Korean encoding
check_properties_korean_encoding() {
    local file="$1"

    # Properties files should use Unicode escapes for non-ASCII characters
    if grep -q "$KOREAN_CHAR_PATTERN" "$file"; then
        echo -e "${YELLOW}⚠️  WARNING:${NC} Direct Korean characters in properties file"
        echo -e "   ${BLUE}Best practice:${NC} Use Unicode escapes or UTF-8 properties files"
        echo -e "   Example: \\u{Unicode} or save as UTF-8 with explicit charset loading"
    fi
}

# Function to check SQL file Korean encoding
check_sql_korean_encoding() {
    local file="$1"

    # Check for INSERT/UPDATE statements with Korean text
    local korean_dml
    korean_dml=$(grep -in "INSERT\|UPDATE.*SET" "$file" | grep "$KOREAN_CHAR_PATTERN" || true)

    if [[ -n "$korean_dml" ]]; then
        echo -e "${BLUE}ℹ️  INFO:${NC} Korean text in SQL DML statements"
        echo -e "   ${BLUE}Ensure:${NC} Database connection uses UTF-8 charset"
        echo -e "   Lines: $(echo "$korean_dml" | cut -d: -f1 | tr '\n' ' ')"
    fi
}

# Function to validate single file
validate_file() {
    local file="$1"

    # Check if file is readable
    if [[ ! -r "$file" ]]; then
        echo -e "${RED}❌ ERROR:${NC} Cannot read file: $file"
        return 1
    fi

    # Check if file is binary
    if file "$file" | grep -q "binary"; then
        return 0  # Skip binary files
    fi

    return $(check_file_encoding "$file")
}

# Function to show helpful encoding information
show_encoding_help() {
    echo -e "${BLUE}🔧 Encoding Issue Resolution Guide${NC}"
    echo "=================================="
    echo
    echo -e "${BLUE}1. Convert file to UTF-8:${NC}"
    echo "   iconv -f <source-encoding> -t UTF-8 <file> > <file>.utf8"
    echo "   mv <file>.utf8 <file>"
    echo
    echo -e "${BLUE}2. Remove BOM from UTF-8 file:${NC}"
    echo "   sed -i '1s/^\xEF\xBB\xBF//' <file>"
    echo
    echo -e "${BLUE}3. Check file encoding:${NC}"
    echo "   file -b --mime-encoding <file>"
    echo "   hexdump -C <file> | head -3"
    echo
    echo -e "${BLUE}4. Test Korean text:${NC}"
    echo "   echo '한국어 테스트' > test-korean.txt"
    echo "   file -b --mime-encoding test-korean.txt"
    echo
    echo -e "${BLUE}5. Database testing:${NC}"
    echo "   Use: mysql --default-character-set=utf8mb4"
    echo "   Test: SELECT '한국어' as test_korean;"
}

# Main validation logic
main() {
    local exit_code=0

    # If specific files are passed as arguments, check only those
    if [[ $# -gt 0 ]]; then
        for file in "$@"; do
            # Check if file matches our pattern
            if echo "$file" | grep -E "$FILE_PATTERNS" > /dev/null; then
                ((total_files++))
                if [[ -f "$file" ]]; then
                    ((checked_files++))
                    if ! validate_file "$file"; then
                        exit_code=1
                    fi
                else
                    echo -e "${YELLOW}⚠️  WARNING:${NC} File not found: $file"
                fi
            fi
        done
    else
        # Check all matching files in the project
        while IFS= read -r -d '' file; do
            ((total_files++))
            ((checked_files++))
            if ! validate_file "$file"; then
                exit_code=1
            fi
        done < <(find . -type f | grep -E "$FILE_PATTERNS" | grep -v node_modules | grep -v ".git" | grep -v "build/" | grep -v "target/" -print0 2>/dev/null)
    fi

    # Summary
    echo "=================================="
    echo -e "${BLUE}📊 Korean Text Encoding Summary${NC}"
    echo "Total files scanned: $total_files"
    echo "Files checked: $checked_files"
    echo "Files with Korean text: $korean_files"

    if [[ $issues_found -eq 0 ]]; then
        echo -e "${GREEN}✅ SUCCESS:${NC} All Korean text is properly encoded"
        if [[ $korean_files -gt 0 ]]; then
            echo -e "${BLUE}💡 Tips:${NC}"
            echo "• Always save files as UTF-8 without BOM"
            echo "• Test Korean text display in target environment"
            echo "• Use ResourceBundle for Java internationalization"
        fi
    else
        echo -e "${RED}❌ FAILURE:${NC} $issues_found encoding issues need to be fixed"
        echo
        show_encoding_help
        exit_code=1
    fi

    return $exit_code
}

# Run main function with all arguments
main "$@"