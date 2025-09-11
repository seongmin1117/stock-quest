#!/bin/bash

# StockQuest Git Configuration Setup
# This script configures Git for optimal StockQuest development

set -e

echo "🔧 StockQuest Git Setup"
echo "====================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if we're in StockQuest project root
if [ ! -f ".gitmessage" ] || [ ! -d ".claude" ]; then
    print_error "This script must be run from the StockQuest project root directory"
    exit 1
fi

echo "📋 Configuring Git for StockQuest development..."
echo ""

# 1. Set commit message template
print_info "Setting up commit message template..."
if git config commit.template .gitmessage; then
    print_success "Commit template configured"
else
    print_error "Failed to set commit template"
    exit 1
fi

# 2. Configure useful Git aliases
print_info "Setting up Git aliases..."

# Basic aliases
git config alias.co checkout
git config alias.br branch  
git config alias.ci commit
git config alias.st status
git config alias.unstage 'reset HEAD --'
git config alias.last 'log -1 HEAD --stat'

# StockQuest specific aliases
git config alias.domain-add 'add backend/src/main/java/com/stockquest/domain/'
git config alias.app-add 'add backend/src/main/java/com/stockquest/application/'
git config alias.adapter-add 'add backend/src/main/java/com/stockquest/adapter/'
git config alias.frontend-add 'add frontend/src/'

# Log aliases for better history viewing
git config alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"
git config alias.tree "log --graph --full-history --all --color --pretty=format:'%x1b[31m%h%x09%C(yellow)%d%Creset%x20%s%x20%C(bold blue)[%an]%Creset'"

# Show files in commits
git config alias.show-files "log --name-only --pretty=format:'%C(yellow)%h%Creset - %s %C(green)(%cr)%Creset'"

print_success "Git aliases configured"

# 3. Configure merge and rebase settings  
print_info "Configuring merge and rebase settings..."
git config merge.tool vimdiff
git config pull.rebase true
git config rebase.autoStash true
print_success "Merge and rebase settings configured"

# 4. Configure commit and push settings
print_info "Configuring commit and push settings..."
git config push.default simple
git config core.editor "code --wait" 2>/dev/null || git config core.editor vim
print_success "Commit and push settings configured"

# 5. Configure line ending handling
print_info "Configuring line ending settings..."
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows
    git config core.autocrlf true
    print_success "Windows line ending settings applied"
else
    # macOS/Linux
    git config core.autocrlf input
    print_success "Unix line ending settings applied"
fi

# 6. Set up .gitignore handling
print_info "Configuring .gitignore settings..."
git config core.excludesfile ~/.gitignore_global 2>/dev/null || true
print_success "Global .gitignore configured"

# 7. Configure diff and status settings
print_info "Configuring diff and status settings..."
git config diff.renames true
git config status.showUntrackedFiles all
git config color.ui auto
print_success "Diff and status settings configured"

echo ""
echo "🎯 Git Configuration Summary"
echo "============================="
echo ""
echo "📝 Commit Template: .gitmessage"
echo "🔗 Useful Aliases:"
echo "   git lg          - Pretty log with graph"
echo "   git tree        - Tree view of commits"
echo "   git show-files  - Show changed files in commits"
echo "   git co          - checkout"
echo "   git br          - branch"
echo "   git ci          - commit" 
echo "   git st          - status"
echo ""
echo "📖 StockQuest Specific Aliases:"
echo "   git domain-add  - Add domain layer files"
echo "   git app-add     - Add application layer files"
echo "   git adapter-add - Add adapter layer files"
echo "   git frontend-add - Add frontend files"
echo ""

# 8. Show current Git configuration
echo "🔧 Current Git Configuration:"
echo "============================="
echo "User Name: $(git config user.name || echo 'Not configured')"
echo "User Email: $(git config user.email || echo 'Not configured')"
echo "Commit Template: $(git config commit.template || echo 'Not configured')"
echo "Default Editor: $(git config core.editor || echo 'Not configured')"
echo "Pull Strategy: $(git config pull.rebase || echo 'Not configured')"
echo ""

# 9. Check if user info is configured
if [ -z "$(git config user.name)" ] || [ -z "$(git config user.email)" ]; then
    print_warning "Git user information is not configured!"
    echo ""
    echo "Please configure your Git user information:"
    echo "  git config user.name \"Your Name\""
    echo "  git config user.email \"your.email@example.com\""
    echo ""
    echo "For global configuration (recommended):"
    echo "  git config --global user.name \"Your Name\""
    echo "  git config --global user.email \"your.email@example.com\""
    echo ""
fi

# 10. Verify .gitmessage template
if [ -f ".gitmessage" ]; then
    print_success "Commit message template is ready"
    echo ""
    print_info "To see the commit template in action:"
    echo "  git commit    # Will open editor with template"
    echo ""
else
    print_error "Commit message template file not found!"
    exit 1
fi

# 11. Test git log aliases
print_info "Testing log aliases..."
echo ""
echo "📊 Recent commits (using 'git lg' alias):"
git lg -5 2>/dev/null || print_warning "No commits found (this is normal for new repositories)"
echo ""

# 12. Create helpful commit message examples
cat > .git-commit-examples.md << 'EOF'
# Git Commit Message Examples for StockQuest

## Good Examples

### Features
```
feat(domain/user): add email validation
feat(app/auth): implement JWT token refresh
feat(adapter/web): add portfolio endpoints
feat(widget/portfolio): display real-time values
```

### Bug Fixes
```
fix(domain/order): handle null quantity validation
fix(app/trading): prevent duplicate order submission
fix(adapter/persistence): resolve N+1 query issue
fix(feature/auth): handle login form validation errors
```

### Documentation
```
docs(readme): update installation instructions
docs(api): add portfolio endpoint examples
docs(architecture): update hexagonal diagram
```

### Tests
```
test(domain/user): add email validation tests
test(app/auth): add JWT service integration tests
test(e2e): add complete trading flow test
```

### Refactoring
```
refactor(app/portfolio): extract calculation service
refactor(shared/api): improve error handling
refactor(domain/challenge): simplify status enum
```

## Commit Workflow Example

```bash
# 1. Domain first
git add backend/src/main/java/com/stockquest/domain/notification/
git commit -m "feat(domain/notification): add Notification entity"

# 2. Application layer
git add backend/src/main/java/com/stockquest/application/notification/
git commit -m "feat(app/notification): add notification service"

# 3. Adapter layer
git add backend/src/main/java/com/stockquest/adapter/out/notification/
git commit -m "feat(adapter/notification): add email adapter"

# 4. Web layer
git add backend/src/main/java/com/stockquest/adapter/in/web/notification/
git commit -m "feat(adapter/web): add notification endpoints"

# 5. Tests
git add backend/src/test/java/com/stockquest/domain/notification/
git commit -m "test(domain/notification): add entity validation tests"

# 6. Frontend types
git add frontend/src/entities/notification/
git commit -m "feat(entity/notification): add notification types"

# 7. Frontend feature
git add frontend/src/features/notifications/
git commit -m "feat(feature/notifications): add notification list"

# 8. Frontend widget
git add frontend/src/widgets/notifications/
git commit -m "feat(widget/notifications): add notification panel"
```
EOF

print_success "Created .git-commit-examples.md for reference"

echo ""
print_success "🎉 Git setup complete!"
echo ""
print_info "Next steps:"
echo "1. Configure your user information (if not done already)"
echo "2. Read .claude/COMMIT_GUIDELINES.md for detailed guidelines"  
echo "3. Check .git-commit-examples.md for practical examples"
echo "4. Try 'git commit' to see the template in action"
echo ""
print_info "Happy coding with consistent Git history! 🚀"