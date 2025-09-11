#!/bin/bash

# StockQuest File Count Check Script
# Ensures commits don't include too many files for better maintainability

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Thresholds
MAX_FILES=15
WARN_FILES=10
IDEAL_FILES=5

# Get staged files
STAGED_FILES=$(git diff --cached --name-only)
FILE_COUNT=$(echo "$STAGED_FILES" | wc -l)

# Remove empty lines from count
FILE_COUNT=$(echo "$STAGED_FILES" | grep -v '^$' | wc -l)

echo "📊 Checking commit file count..."
echo "Files to be committed: $FILE_COUNT"

if [ $FILE_COUNT -eq 0 ]; then
    echo -e "${YELLOW}⚠️  No files staged for commit${NC}"
    exit 0
fi

echo ""
echo "Staged files:"
echo "$STAGED_FILES" | sed 's/^/  /'
echo ""

# Analyze file types and layers
analyze_files() {
    local files="$1"
    
    # Backend layers
    local domain_files=$(echo "$files" | grep "backend.*domain" | wc -l)
    local app_files=$(echo "$files" | grep "backend.*application" | wc -l)
    local adapter_files=$(echo "$files" | grep "backend.*adapter" | wc -l)
    local config_files=$(echo "$files" | grep "backend.*config" | wc -l)
    local test_files=$(echo "$files" | grep "backend.*test" | wc -l)
    
    # Frontend layers
    local frontend_app=$(echo "$files" | grep "frontend.*app" | wc -l)
    local frontend_widgets=$(echo "$files" | grep "frontend.*widgets" | wc -l)
    local frontend_features=$(echo "$files" | grep "frontend.*features" | wc -l)
    local frontend_entities=$(echo "$files" | grep "frontend.*entities" | wc -l)
    local frontend_shared=$(echo "$files" | grep "frontend.*shared" | wc -l)
    local frontend_tests=$(echo "$files" | grep "frontend.*test\|frontend.*spec" | wc -l)
    
    # Infrastructure
    local docker_files=$(echo "$files" | grep -E "(Dockerfile|docker-compose)" | wc -l)
    local github_files=$(echo "$files" | grep "\.github" | wc -l)
    local config_yaml=$(echo "$files" | grep -E "\.(yml|yaml|json|properties)$" | wc -l)
    local docs=$(echo "$files" | grep -E "\.(md|txt)$" | wc -l)
    
    echo "📈 File distribution:"
    
    if [ $domain_files -gt 0 ]; then
        echo "  🏗️  Domain layer: $domain_files files"
    fi
    if [ $app_files -gt 0 ]; then
        echo "  🔧 Application layer: $app_files files"
    fi
    if [ $adapter_files -gt 0 ]; then
        echo "  🔌 Adapter layer: $adapter_files files"
    fi
    if [ $test_files -gt 0 ]; then
        echo "  🧪 Backend tests: $test_files files"
    fi
    
    if [ $frontend_app -gt 0 ]; then
        echo "  📱 Frontend app: $frontend_app files"
    fi
    if [ $frontend_widgets -gt 0 ]; then
        echo "  🧩 Frontend widgets: $frontend_widgets files"
    fi
    if [ $frontend_features -gt 0 ]; then
        echo "  ⚙️  Frontend features: $frontend_features files"
    fi
    if [ $frontend_entities -gt 0 ]; then
        echo "  📦 Frontend entities: $frontend_entities files"
    fi
    if [ $frontend_shared -gt 0 ]; then
        echo "  🤝 Frontend shared: $frontend_shared files"
    fi
    if [ $frontend_tests -gt 0 ]; then
        echo "  🧪 Frontend tests: $frontend_tests files"
    fi
    
    if [ $config_files -gt 0 ]; then
        echo "  ⚙️  Configuration: $config_files files"
    fi
    if [ $docker_files -gt 0 ]; then
        echo "  🐳 Docker: $docker_files files"
    fi
    if [ $github_files -gt 0 ]; then
        echo "  🔄 GitHub: $github_files files"
    fi
    if [ $config_yaml -gt 0 ]; then
        echo "  📋 Config files: $config_yaml files"
    fi
    if [ $docs -gt 0 ]; then
        echo "  📖 Documentation: $docs files"
    fi
    
    # Check for cross-layer changes
    local backend_layers=0
    if [ $domain_files -gt 0 ]; then backend_layers=$((backend_layers + 1)); fi
    if [ $app_files -gt 0 ]; then backend_layers=$((backend_layers + 1)); fi
    if [ $adapter_files -gt 0 ]; then backend_layers=$((backend_layers + 1)); fi
    
    local frontend_layers=0
    if [ $frontend_app -gt 0 ]; then frontend_layers=$((frontend_layers + 1)); fi
    if [ $frontend_widgets -gt 0 ]; then frontend_layers=$((frontend_layers + 1)); fi
    if [ $frontend_features -gt 0 ]; then frontend_layers=$((frontend_layers + 1)); fi
    if [ $frontend_entities -gt 0 ]; then frontend_layers=$((frontend_layers + 1)); fi
    if [ $frontend_shared -gt 0 ]; then frontend_layers=$((frontend_layers + 1)); fi
    
    echo ""
    
    if [ $backend_layers -gt 3 ]; then
        echo -e "${YELLOW}⚠️  Warning: Changes span multiple backend layers${NC}"
        echo "   Consider splitting into separate commits"
    fi
    
    if [ $frontend_layers -gt 3 ]; then
        echo -e "${YELLOW}⚠️  Warning: Changes span multiple frontend layers${NC}"
        echo "   Consider splitting into separate commits"
    fi
    
    if [ $backend_layers -gt 0 ] && [ $frontend_layers -gt 0 ]; then
        echo -e "${YELLOW}⚠️  Warning: Changes include both backend and frontend${NC}"
        echo "   Consider splitting into separate commits"
    fi
}

# Analyze the files
analyze_files "$STAGED_FILES"

# Check file count thresholds
echo ""
if [ $FILE_COUNT -le $IDEAL_FILES ]; then
    echo -e "${GREEN}✅ Excellent! Atomic commit with $FILE_COUNT files${NC}"
    echo "   This is the ideal commit size for easy review and tracking"
    exit 0
elif [ $FILE_COUNT -le $WARN_FILES ]; then
    echo -e "${GREEN}✅ Good commit size: $FILE_COUNT files${NC}"
    echo "   Still manageable for code review"
    exit 0
elif [ $FILE_COUNT -le $MAX_FILES ]; then
    echo -e "${YELLOW}⚠️  Large commit: $FILE_COUNT files${NC}"
    echo ""
    echo "📋 Consider splitting this commit:"
    echo ""
    echo "💡 Splitting strategies:"
    echo "  1. By layer (Domain → Application → Adapter)"
    echo "  2. By feature (Core logic → Tests → Documentation)"
    echo "  3. By component (Backend → Frontend)"
    echo "  4. By change type (New features → Bug fixes → Tests)"
    echo ""
    echo "📖 See .claude/COMMIT_GUIDELINES.md for detailed guidelines"
    echo ""
    
    # Ask user if they want to continue
    if [ -t 0 ]; then  # Check if running in interactive terminal
        read -p "Do you want to continue with this large commit? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Commit aborted. Please split into smaller commits."
            exit 1
        fi
    else
        echo "⚠️  Proceeding with large commit (non-interactive mode)"
    fi
    
    exit 0
else
    echo -e "${RED}❌ Commit too large: $FILE_COUNT files (max: $MAX_FILES)${NC}"
    echo ""
    echo "🚫 This commit exceeds the maximum file limit!"
    echo ""
    echo "📋 Required actions:"
    echo "  1. Split this commit into smaller, logical units"
    echo "  2. Each commit should focus on one specific change"
    echo "  3. Follow the Single Responsibility Principle for commits"
    echo ""
    echo "🔧 Quick splitting commands:"
    echo "  git reset HEAD~1                    # Unstage all files"
    echo "  git add <specific-files>            # Stage only related files"
    echo "  git commit -m 'specific change'     # Commit focused change"
    echo "  # Repeat for remaining changes"
    echo ""
    echo "💡 Suggested split by layers:"
    
    # Suggest splitting strategy based on file types
    domain_files=$(echo "$STAGED_FILES" | grep "backend.*domain" || true)
    if [ -n "$domain_files" ]; then
        echo "  # Domain layer commit:"
        echo "  git add backend/src/main/java/com/stockquest/domain/"
        echo "  git commit -m 'feat(domain/...): add domain logic'"
        echo ""
    fi
    
    app_files=$(echo "$STAGED_FILES" | grep "backend.*application" || true)
    if [ -n "$app_files" ]; then
        echo "  # Application layer commit:"
        echo "  git add backend/src/main/java/com/stockquest/application/"
        echo "  git commit -m 'feat(app/...): add use case'"
        echo ""
    fi
    
    adapter_files=$(echo "$STAGED_FILES" | grep "backend.*adapter" || true)
    if [ -n "$adapter_files" ]; then
        echo "  # Adapter layer commit:"
        echo "  git add backend/src/main/java/com/stockquest/adapter/"
        echo "  git commit -m 'feat(adapter/...): add adapter implementation'"
        echo ""
    fi
    
    test_files=$(echo "$STAGED_FILES" | grep "test" || true)
    if [ -n "$test_files" ]; then
        echo "  # Test commit:"
        echo "  git add backend/src/test/ frontend/tests/"
        echo "  git commit -m 'test(...): add comprehensive tests'"
        echo ""
    fi
    
    echo "📖 See .claude/COMMIT_GUIDELINES.md for more splitting strategies"
    exit 1
fi