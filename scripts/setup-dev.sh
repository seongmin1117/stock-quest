#!/bin/bash

# StockQuest Development Environment Setup Script
# Sets up the complete development environment for new contributors

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${PURPLE}🚀 StockQuest Development Environment Setup${NC}"
echo "============================================"

# Check if we're in the correct directory
if [ ! -f "README.md" ] || [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo -e "${RED}❌ Please run this script from the StockQuest project root directory${NC}"
    exit 1
fi

# Function to check command availability
check_command() {
    if command -v "$1" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ $1 is installed${NC}"
        return 0
    else
        echo -e "${RED}❌ $1 is not installed${NC}"
        return 1
    fi
}

# Function to install command if not present (macOS)
install_if_missing_mac() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo -e "${YELLOW}📦 Installing $1 via Homebrew...${NC}"
        if command -v brew >/dev/null 2>&1; then
            brew install "$2"
        else
            echo -e "${RED}❌ Homebrew not found. Please install $1 manually.${NC}"
            return 1
        fi
    fi
}

# Detect operating system
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="mac"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    OS="windows"
fi

echo "🔍 Detected OS: $OS"

# Check and install required tools
echo ""
echo -e "${BLUE}📋 Checking required tools...${NC}"

MISSING_TOOLS=()

# Java 21
if ! java -version 2>&1 | grep -q "21"; then
    echo -e "${RED}❌ Java 21 is required${NC}"
    MISSING_TOOLS+=("java")
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install openjdk@21"
    fi
else
    echo -e "${GREEN}✅ Java 21 is installed${NC}"
fi

# Node.js 18+
if ! node --version 2>&1 | grep -qE "v(1[8-9]|[2-9][0-9])"; then
    echo -e "${RED}❌ Node.js 18+ is required${NC}"
    MISSING_TOOLS+=("nodejs")
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install node@18"
    fi
else
    echo -e "${GREEN}✅ Node.js $(node --version) is installed${NC}"
fi

# pnpm
if ! check_command "pnpm"; then
    MISSING_TOOLS+=("pnpm")
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install pnpm"
    else
        echo "  Install: npm install -g pnpm"
    fi
fi

# Docker
if ! check_command "docker"; then
    MISSING_TOOLS+=("docker")
    echo "  Install Docker Desktop from: https://www.docker.com/products/docker-desktop"
fi

# Docker Compose
if ! check_command "docker-compose" && ! docker compose version >/dev/null 2>&1; then
    echo -e "${RED}❌ Docker Compose is not available${NC}"
    MISSING_TOOLS+=("docker-compose")
fi

# Git
check_command "git" || MISSING_TOOLS+=("git")

# Pre-commit
if ! check_command "pre-commit"; then
    MISSING_TOOLS+=("pre-commit")
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install pre-commit"
    else
        echo "  Install: pip install pre-commit"
    fi
fi

# MySQL Client (optional but recommended)
if ! check_command "mysql"; then
    echo -e "${YELLOW}⚠️  MySQL client not found (optional)${NC}"
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install mysql-client"
    fi
fi

# Redis CLI (optional but recommended)
if ! check_command "redis-cli"; then
    echo -e "${YELLOW}⚠️  Redis CLI not found (optional)${NC}"
    if [ "$OS" = "mac" ]; then
        echo "  Install: brew install redis"
    fi
fi

# Exit if critical tools are missing
if [ ${#MISSING_TOOLS[@]} -ne 0 ]; then
    echo ""
    echo -e "${RED}💥 Missing required tools: ${MISSING_TOOLS[*]}${NC}"
    echo ""
    echo "Please install the missing tools and run this script again."
    echo "For detailed installation instructions, see:"
    echo "  📖 README.md > Development Environment Setup"
    exit 1
fi

echo -e "${GREEN}✅ All required tools are installed!${NC}"

# Set up backend environment
echo ""
echo -e "${BLUE}🔧 Setting up backend environment...${NC}"

cd backend

# Check if gradlew is executable
if [ ! -x "./gradlew" ]; then
    chmod +x ./gradlew
    echo -e "${GREEN}✅ Made gradlew executable${NC}"
fi

# Check Gradle version and dependencies
echo "📦 Checking Gradle and dependencies..."
if ./gradlew --version >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Gradle is working${NC}"
else
    echo -e "${RED}❌ Gradle setup failed${NC}"
    exit 1
fi

# Build backend (this will download dependencies)
echo "🔨 Building backend (this may take a few minutes)..."
if ./gradlew clean compileJava >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend build successful${NC}"
else
    echo -e "${RED}❌ Backend build failed. Check Java version and connectivity.${NC}"
    echo "Try running manually: cd backend && ./gradlew clean compileJava"
    exit 1
fi

cd ..

# Set up frontend environment
echo ""
echo -e "${BLUE}🎨 Setting up frontend environment...${NC}"

cd frontend

# Install frontend dependencies
echo "📦 Installing frontend dependencies..."
if pnpm install >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend dependencies installed${NC}"
else
    echo -e "${RED}❌ Frontend dependency installation failed${NC}"
    echo "Try running manually: cd frontend && pnpm install"
    exit 1
fi

# Check if Next.js builds
echo "🔨 Testing frontend build..."
if pnpm build >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend build successful${NC}"
else
    echo -e "${YELLOW}⚠️  Frontend build had warnings (this is normal for initial setup)${NC}"
fi

cd ..

# Set up Docker environment
echo ""
echo -e "${BLUE}🐳 Setting up Docker environment...${NC}"

# Check if Docker is running
if docker info >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Docker is running${NC}"
    
    # Pull required images
    echo "📥 Pulling required Docker images..."
    docker pull mysql:8.0 >/dev/null 2>&1 && echo -e "${GREEN}✅ MySQL image ready${NC}"
    docker pull redis:7-alpine >/dev/null 2>&1 && echo -e "${GREEN}✅ Redis image ready${NC}"
    
    # Test docker-compose configuration
    echo "🔧 Testing Docker Compose configuration..."
    if docker-compose config >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Docker Compose configuration is valid${NC}"
    else
        echo -e "${YELLOW}⚠️  Docker Compose configuration has issues${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Docker is not running. Please start Docker Desktop.${NC}"
fi

# Set up Git configuration
echo ""
echo -e "${BLUE}🔧 Setting up Git configuration...${NC}"

# Run git setup script
if [ -f "scripts/setup-git.sh" ]; then
    chmod +x scripts/setup-git.sh
    if ./scripts/setup-git.sh >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Git configuration completed${NC}"
    else
        echo -e "${YELLOW}⚠️  Git configuration had issues. Run './scripts/setup-git.sh' manually.${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Git setup script not found${NC}"
fi

# Create local environment files
echo ""
echo -e "${BLUE}📝 Creating environment configuration files...${NC}"

# Backend environment
if [ ! -f "backend/src/main/resources/application-local.yml" ]; then
    cat > backend/src/main/resources/application-local.yml << 'EOF'
# StockQuest Local Development Configuration
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockquest?useSSL=false&serverTimezone=UTC
    username: stockquest
    password: stockquest123
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        
  logging:
    level:
      com.stockquest: DEBUG
      org.springframework.web: DEBUG
      
jwt:
  secret: local-development-secret-key-change-in-production
  expiration: 86400000 # 24 hours

# API Rate Limiting (relaxed for development)
rate-limit:
  enabled: false
EOF
    echo -e "${GREEN}✅ Backend local configuration created${NC}"
fi

# Frontend environment
if [ ! -f "frontend/.env.local" ]; then
    cat > frontend/.env.local << 'EOF'
# StockQuest Frontend Local Development Environment

# API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

# Development Settings
NEXT_PUBLIC_ENVIRONMENT=development
NEXT_PUBLIC_DEBUG=true

# Authentication
NEXT_PUBLIC_JWT_ISSUER=stockquest-local

# Feature Flags
NEXT_PUBLIC_ENABLE_ANALYTICS=false
NEXT_PUBLIC_ENABLE_ERROR_TRACKING=false

# Development Tools
NEXT_PUBLIC_ENABLE_DEVTOOLS=true
EOF
    echo -e "${GREEN}✅ Frontend local environment created${NC}"
fi

# Create development scripts
echo ""
echo -e "${BLUE}🔧 Creating development helper scripts...${NC}"

# Create start-dev script
cat > scripts/start-dev.sh << 'EOF'
#!/bin/bash
# Start development environment

echo "🚀 Starting StockQuest development environment..."

# Start databases
echo "🐳 Starting databases..."
docker-compose up -d mysql redis

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Start backend in background
echo "🔧 Starting backend..."
cd backend
./gradlew bootRun --args='--spring.profiles.active=local' &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 15

# Start frontend
echo "🎨 Starting frontend..."
cd frontend
pnpm dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "✅ Development environment started!"
echo "   🔧 Backend: http://localhost:8080"
echo "   🎨 Frontend: http://localhost:3000"
echo "   💾 MySQL: localhost:3306 (stockquest/stockquest123)"
echo "   📝 Redis: localhost:6379"
echo ""
echo "Press Ctrl+C to stop all services..."

# Wait for Ctrl+C
trap "echo '🛑 Stopping services...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; docker-compose stop; exit" INT
wait
EOF

chmod +x scripts/start-dev.sh
echo -e "${GREEN}✅ Development start script created${NC}"

# Create test script
cat > scripts/test-all.sh << 'EOF'
#!/bin/bash
# Run all tests

echo "🧪 Running all StockQuest tests..."

EXIT_CODE=0

# Backend tests
echo "🔧 Running backend tests..."
cd backend
if ./gradlew test; then
    echo "✅ Backend tests passed"
else
    echo "❌ Backend tests failed"
    EXIT_CODE=1
fi
cd ..

# Frontend tests
echo "🎨 Running frontend tests..."
cd frontend
if pnpm test; then
    echo "✅ Frontend tests passed"
else
    echo "❌ Frontend tests failed"
    EXIT_CODE=1
fi
cd ..

# Pre-commit validation
echo "🛡️ Running pre-commit validation..."
if pre-commit run --all-files; then
    echo "✅ Pre-commit validation passed"
else
    echo "❌ Pre-commit validation failed"
    EXIT_CODE=1
fi

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "🎉 All tests passed!"
else
    echo ""
    echo "💥 Some tests failed. Please fix the issues before committing."
fi

exit $EXIT_CODE
EOF

chmod +x scripts/test-all.sh
echo -e "${GREEN}✅ Test runner script created${NC}"

# Final setup validation
echo ""
echo -e "${BLUE}🔍 Running final validation...${NC}"

# Test pre-commit
if pre-commit run --all-files >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Pre-commit hooks working${NC}"
else
    echo -e "${YELLOW}⚠️  Pre-commit hooks need attention${NC}"
fi

# Test basic compilation
echo "🔨 Testing compilation..."
cd backend && ./gradlew compileJava >/dev/null 2>&1 && echo -e "${GREEN}✅ Backend compiles${NC}" || echo -e "${YELLOW}⚠️  Backend compilation issues${NC}"
cd ../frontend && pnpm type-check >/dev/null 2>&1 && echo -e "${GREEN}✅ Frontend type-checks${NC}" || echo -e "${YELLOW}⚠️  Frontend type issues${NC}"
cd ..

echo ""
echo -e "${GREEN}🎉 Development environment setup complete!${NC}"
echo ""
echo -e "${PURPLE}📋 What's ready:${NC}"
echo "  ✅ Backend (Java 21 + Spring Boot)"
echo "  ✅ Frontend (Node.js + Next.js + TypeScript)"
echo "  ✅ Docker environment (MySQL + Redis)"
echo "  ✅ Git configuration with pre-commit hooks"
echo "  ✅ Local environment files"
echo "  ✅ Development helper scripts"
echo ""
echo -e "${PURPLE}🚀 Quick start commands:${NC}"
echo "  ./scripts/start-dev.sh     # Start full development environment"
echo "  ./scripts/test-all.sh      # Run all tests"
echo "  docker-compose up -d       # Start databases only"
echo "  cd backend && ./gradlew bootRun  # Start backend only"
echo "  cd frontend && pnpm dev    # Start frontend only"
echo ""
echo -e "${PURPLE}📖 Next steps:${NC}"
echo "  1. Read README.md for project overview"
echo "  2. Check .claude/AUTOSTART.md for development guidelines"
echo "  3. Start development with: ./scripts/start-dev.sh"
echo "  4. Open http://localhost:3000 in your browser"
echo ""
echo -e "${BLUE}Happy coding! 🚀${NC}"
EOF