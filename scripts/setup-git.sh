#!/bin/bash

# StockQuest Git Configuration Setup Script
# Configures git hooks, templates, and settings for the project

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "🔧 Setting up Git configuration for StockQuest..."

# Check if we're in the correct directory
if [ ! -f ".gitmessage" ] || [ ! -d ".github" ]; then
    echo -e "${RED}❌ Please run this script from the StockQuest project root directory${NC}"
    exit 1
fi

# Function to check command availability
check_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo -e "${RED}❌ $1 is not installed. Please install $1 and try again.${NC}"
        exit 1
    fi
}

# Check required commands
echo "🔍 Checking required tools..."
check_command "git"
check_command "pre-commit"

# Configure git commit template
echo "📝 Configuring git commit template..."
git config --local commit.template .gitmessage
echo -e "${GREEN}✅ Git commit template configured${NC}"

# Configure git hooks path (if using custom hooks)
if [ -d ".githooks" ]; then
    echo "🪝 Configuring custom git hooks..."
    git config --local core.hooksPath .githooks
    chmod +x .githooks/*
    echo -e "${GREEN}✅ Custom git hooks configured${NC}"
fi

# Install pre-commit hooks
echo "🛡️ Installing pre-commit hooks..."
if pre-commit install --install-hooks; then
    echo -e "${GREEN}✅ Pre-commit hooks installed successfully${NC}"
else
    echo -e "${YELLOW}⚠️  Failed to install pre-commit hooks. You may need to install pre-commit first:${NC}"
    echo "  pip install pre-commit"
    echo "  or"
    echo "  brew install pre-commit"
fi

# Install commit-msg hook
echo "💬 Installing commit-msg hook..."
if pre-commit install --hook-type commit-msg; then
    echo -e "${GREEN}✅ Commit-msg hook installed${NC}"
else
    echo -e "${YELLOW}⚠️  Failed to install commit-msg hook${NC}"
fi

# Configure git user settings (if not already set)
echo "👤 Checking git user configuration..."
if [ -z "$(git config --local user.name)" ] && [ -z "$(git config --global user.name)" ]; then
    echo -e "${YELLOW}⚠️  Git user name not configured${NC}"
    echo "Please set your git user name:"
    echo "  git config --global user.name \"Your Name\""
fi

if [ -z "$(git config --local user.email)" ] && [ -z "$(git config --global user.email)" ]; then
    echo -e "${YELLOW}⚠️  Git user email not configured${NC}"
    echo "Please set your git user email:"
    echo "  git config --global user.email \"your.email@example.com\""
fi

# Configure useful git aliases
echo "⚡ Setting up helpful git aliases..."

# Alias for formatted log
git config --local alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"

# Alias for showing file changes in last commit
git config --local alias.last "log -1 HEAD --stat"

# Alias for better status
git config --local alias.st "status -sb"

# Alias for better diff
git config --local alias.df "diff --color --color-words --abbrev"

# Alias for staging all changes
git config --local alias.add-all "add -A"

# Alias for amending last commit
git config --local alias.amend "commit --amend --no-edit"

# Alias for checking out branches
git config --local alias.co "checkout"
git config --local alias.br "branch"

# Alias for pushing with upstream
git config --local alias.pushup "push -u origin HEAD"

echo -e "${GREEN}✅ Git aliases configured${NC}"

# Configure git settings for better collaboration
echo "🤝 Configuring collaboration settings..."

# Configure line ending handling
git config --local core.autocrlf false
git config --local core.eol lf

# Configure merge strategy
git config --local merge.ours.driver true

# Configure push default
git config --local push.default simple

# Configure pull behavior
git config --local pull.rebase true

echo -e "${GREEN}✅ Collaboration settings configured${NC}"

# Set up branch protection reminders
echo "🛡️ Setting up branch protection reminders..."

# Create a simple pre-push hook reminder
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash

# Get the branch being pushed
branch=$(git rev-parse --abbrev-ref HEAD)

# Check if pushing to main/master
if [[ "$branch" == "main" || "$branch" == "master" ]]; then
    echo "⚠️  Warning: You're about to push to the $branch branch!"
    echo "Make sure you have:"
    echo "  ✅ Reviewed the changes"
    echo "  ✅ Run all tests locally"
    echo "  ✅ Updated documentation if needed"
    echo ""
    read -p "Continue with push? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Push cancelled."
        exit 1
    fi
fi
EOF

chmod +x .git/hooks/pre-push
echo -e "${GREEN}✅ Branch protection reminders configured${NC}"

# Test pre-commit setup
echo "🧪 Testing pre-commit setup..."
if pre-commit run --all-files >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Pre-commit setup test passed${NC}"
else
    echo -e "${YELLOW}⚠️  Pre-commit test had issues. Run 'pre-commit run --all-files' to see details${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Git configuration complete!${NC}"
echo ""
echo "📋 What was configured:"
echo "  ✅ Commit message template (.gitmessage)"
echo "  ✅ Pre-commit hooks with all validations"
echo "  ✅ Useful git aliases (lg, st, df, etc.)"
echo "  ✅ Collaboration settings (line endings, merge, push)"
echo "  ✅ Branch protection reminders"
echo ""
echo "💡 Useful commands:"
echo "  git lg          # Pretty log with graph"
echo "  git st          # Short status"  
echo "  git df          # Better diff"
echo "  git last        # Show last commit with files"
echo "  git pushup      # Push and set upstream"
echo ""
echo "🔧 To run validations manually:"
echo "  pre-commit run --all-files    # Run all pre-commit hooks"
echo "  scripts/check-architecture.sh # Check architecture compliance"
echo "  scripts/check-coverage.sh     # Check test coverage"
echo ""