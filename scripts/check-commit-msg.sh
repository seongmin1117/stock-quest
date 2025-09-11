#!/bin/bash

# StockQuest Commit Message Validation Script
# Ensures commit messages follow the conventional commits format

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get commit message from the first argument (the commit message file)
COMMIT_MSG_FILE=${1:-".git/COMMIT_EDITMSG"}

if [ ! -f "$COMMIT_MSG_FILE" ]; then
    echo -e "${RED}❌ Commit message file not found: $COMMIT_MSG_FILE${NC}"
    exit 1
fi

COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")

echo "📝 Validating commit message format..."

# Skip validation for merge commits
if echo "$COMMIT_MSG" | head -n 1 | grep -q "^Merge "; then
    echo -e "${GREEN}✅ Merge commit detected, skipping validation${NC}"
    exit 0
fi

# Skip validation for revert commits
if echo "$COMMIT_MSG" | head -n 1 | grep -q "^Revert "; then
    echo -e "${GREEN}✅ Revert commit detected, skipping validation${NC}"
    exit 0
fi

# Extract the first line (subject)
SUBJECT=$(echo "$COMMIT_MSG" | head -n 1)

echo "Subject: $SUBJECT"

# Conventional Commits regex pattern for StockQuest
# Format: type(scope): description
CONVENTIONAL_PATTERN='^(feat|fix|docs|style|refactor|perf|test|chore|build|ci|revert)(\(([a-z0-9\-\/]+)\))?: .{1,72}$'

# StockQuest specific scopes
VALID_SCOPES=(
    # Backend scopes
    "domain" "application" "adapter" "config" "security" "auth" "challenge" "session" "ranking" "user"
    "portfolio" "stock" "transaction" "notification" "batch" "api" "infrastructure" "database" "cache"
    
    # Frontend scopes  
    "app" "pages" "widgets" "features" "entities" "shared" "ui" "hooks" "utils" "types"
    "auth" "challenge" "portfolio" "ranking" "profile" "components" "services" "stores"
    
    # Infrastructure scopes
    "docker" "ci" "cd" "github" "scripts" "docs" "deps" "build" "release"
    
    # General scopes
    "root" "global" "config" "migration" "seed" "test" "e2e" "unit" "integration"
)

# Check if subject matches conventional commits pattern
if ! echo "$SUBJECT" | grep -qE "$CONVENTIONAL_PATTERN"; then
    echo -e "${RED}❌ Invalid commit message format!${NC}"
    echo ""
    echo -e "${BLUE}Expected format:${NC}"
    echo "  type(scope): description"
    echo ""
    echo -e "${BLUE}Valid types:${NC}"
    echo "  feat:     ✨ new feature"
    echo "  fix:      🐛 bug fix"
    echo "  docs:     📝 documentation"
    echo "  style:    💄 formatting, missing semi colons, etc"
    echo "  refactor: ♻️  code change that neither fixes a bug nor adds a feature"
    echo "  perf:     ⚡ performance improvement"
    echo "  test:     ✅ add missing tests or correct existing tests"
    echo "  chore:    🔧 maintain"
    echo "  build:    📦 changes that affect the build system"
    echo "  ci:       🔄 changes to CI configuration files and scripts"
    echo "  revert:   ⏪ revert previous commit"
    echo ""
    echo -e "${BLUE}Example valid messages (영어+한국어 혼용):${NC}"
    echo "  feat(auth): add JWT token validation 기능"
    echo "  fix(portfolio): 주식 price calculation error 수정"
    echo "  docs(api): authentication endpoint 문서 업데이트"
    echo "  refactor(domain): user entity 구조 simplify"
    echo ""
    exit 1
fi

# Extract type and scope from subject
TYPE=$(echo "$SUBJECT" | sed -E 's/^([a-z]+)(\([^)]+\))?: .*/\1/')
SCOPE_WITH_PARENS=$(echo "$SUBJECT" | sed -E 's/^[a-z]+(\([^)]+\))?: .*/\1/')
SCOPE=$(echo "$SCOPE_WITH_PARENS" | sed 's/[()]//g')

# Validate scope if present
if [ -n "$SCOPE" ] && [ "$SCOPE" != "$SCOPE_WITH_PARENS" ]; then
    SCOPE_VALID=false
    for valid_scope in "${VALID_SCOPES[@]}"; do
        if [ "$SCOPE" = "$valid_scope" ]; then
            SCOPE_VALID=true
            break
        fi
        # Allow nested scopes (e.g., "auth/jwt", "api/v1")
        if echo "$SCOPE" | grep -q "^${valid_scope}/"; then
            SCOPE_VALID=true
            break
        fi
    done
    
    if [ "$SCOPE_VALID" = false ]; then
        echo -e "${YELLOW}⚠️  Warning: Unknown scope '$SCOPE'${NC}"
        echo ""
        echo -e "${BLUE}Valid StockQuest scopes:${NC}"
        echo -e "${BLUE}Backend:${NC} ${VALID_SCOPES[@]:0:15}"
        echo -e "${BLUE}Frontend:${NC} ${VALID_SCOPES[@]:15:15}"
        echo -e "${BLUE}Infrastructure:${NC} ${VALID_SCOPES[@]:30:10}"
        echo -e "${BLUE}General:${NC} ${VALID_SCOPES[@]:40}"
        echo ""
        echo "💡 You can use nested scopes like 'auth/jwt', 'api/v1', etc."
        echo ""
        
        # Ask user if they want to continue with unknown scope (in interactive mode)
        if [ -t 0 ]; then
            read -p "Continue with unknown scope? (y/N): " -n 1 -r
            echo ""
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                echo "Commit aborted. Please use a valid scope."
                exit 1
            fi
        fi
    fi
fi

# Check subject length
SUBJECT_LENGTH=${#SUBJECT}
if [ $SUBJECT_LENGTH -gt 72 ]; then
    echo -e "${RED}❌ Subject line too long: $SUBJECT_LENGTH characters (max 72)${NC}"
    echo ""
    echo "Please shorten the commit message subject."
    exit 1
fi

# Check if subject starts with capital letter after type(scope):
DESCRIPTION=$(echo "$SUBJECT" | sed -E 's/^[a-z]+(\([^)]+\))?: (.*)/\2/')
FIRST_CHAR=$(echo "$DESCRIPTION" | cut -c1)

if [[ "$FIRST_CHAR" =~ [A-Z] ]]; then
    echo -e "${YELLOW}⚠️  Convention: Use lowercase for description after type(scope):${NC}"
    echo "  Instead of: $SUBJECT"
    echo "  Use: $(echo "$SUBJECT" | sed -E 's/^([a-z]+(\([^)]+\))?: )([A-Z])/\1\L\3/')"
    echo "  Note: 영어+한국어 혼용은 권장됩니다 (English+Korean mixing is encouraged)"
    echo ""
fi

# Check for imperative mood hints
DESCRIPTION_LOWER=$(echo "$DESCRIPTION" | tr '[:upper:]' '[:lower:]')
if echo "$DESCRIPTION_LOWER" | grep -qE '^(added|fixed|changed|updated|removed|created|deleted|modified)'; then
    echo -e "${YELLOW}⚠️  Convention: Use imperative mood (present tense)${NC}"
    echo "  Instead of: 'added feature' → use: 'add feature'"
    echo "  Instead of: 'fixed bug' → use: 'fix bug'"
    echo "  Instead of: 'updated docs' → use: 'update docs'"
    echo ""
fi

# Check if subject ends with period
if echo "$SUBJECT" | grep -q '\.$'; then
    echo -e "${YELLOW}⚠️  Convention: Don't end subject line with period${NC}"
    echo ""
fi

# Validate body structure if present
BODY=$(echo "$COMMIT_MSG" | tail -n +3)
if [ -n "$BODY" ]; then
    # Check for blank line after subject
    SECOND_LINE=$(echo "$COMMIT_MSG" | sed -n '2p')
    if [ -n "$SECOND_LINE" ]; then
        echo -e "${YELLOW}⚠️  Convention: Leave blank line after subject${NC}"
        echo ""
    fi
    
    # Check body line lengths
    echo "$BODY" | while IFS= read -r line; do
        if [ ${#line} -gt 72 ]; then
            echo -e "${YELLOW}⚠️  Body line too long: ${#line} characters (recommended max 72)${NC}"
        fi
    done
fi

# Check for common StockQuest patterns
if echo "$TYPE" | grep -q "feat" && echo "$DESCRIPTION" | grep -qiE "(implement|add|create).*api"; then
    echo -e "${GREEN}💡 Feature detected: API implementation${NC}"
    echo "   Consider including: endpoint specification, request/response examples"
fi

if echo "$TYPE" | grep -q "feat" && echo "$DESCRIPTION" | grep -qiE "(component|ui|page)"; then
    echo -e "${GREEN}💡 Feature detected: UI component${NC}"
    echo "   Consider including: responsive design, accessibility notes"
fi

if echo "$TYPE" | grep -q "fix" && echo "$DESCRIPTION" | grep -qiE "(security|auth|vulnerability)"; then
    echo -e "${GREEN}🔒 Security fix detected${NC}"
    echo "   Consider including: security impact assessment"
fi

echo -e "${GREEN}✅ Commit message format is valid${NC}"

# Show commit summary
echo ""
echo "📊 Commit Summary:"
echo "  Type: $TYPE"
if [ -n "$SCOPE" ]; then
    echo "  Scope: $SCOPE"
fi
echo "  Length: $SUBJECT_LENGTH/72 characters"
echo ""