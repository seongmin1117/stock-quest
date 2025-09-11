#!/bin/bash

# StockQuest Quick Setup Script
# This script helps you quickly set up the StockQuest project

set -e

echo "🚀 StockQuest Setup Script"
echo "=========================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        print_success "Java 21+ found"
    else
        print_error "Java 21+ required, found Java $JAVA_VERSION"
        exit 1
    fi
else
    print_error "Java not found. Please install Java 21+"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -ge 18 ]; then
        print_success "Node.js 18+ found"
    else
        print_error "Node.js 18+ required, found Node.js $NODE_VERSION"
        exit 1
    fi
else
    print_error "Node.js not found. Please install Node.js 18+"
    exit 1
fi

# Check pnpm
if command -v pnpm &> /dev/null; then
    print_success "pnpm found"
else
    print_warning "pnpm not found. Installing pnpm..."
    npm install -g pnpm
    print_success "pnpm installed"
fi

# Check Docker
if command -v docker &> /dev/null; then
    print_success "Docker found"
else
    print_error "Docker not found. Please install Docker"
    exit 1
fi

# Check Docker Compose
if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
    print_success "Docker Compose found"
else
    print_error "Docker Compose not found. Please install Docker Compose"
    exit 1
fi

echo ""
echo "📦 Setting up environment..."

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    print_warning ".env file not found. Creating from .env.example..."
    cp .env.example .env
    print_success ".env file created. Please edit it with your values."
else
    print_success ".env file exists"
fi

# Create backend .env if needed
if [ ! -f backend/src/main/resources/application-local.yml ]; then
    print_warning "Creating backend local configuration..."
    cat > backend/src/main/resources/application-local.yml << EOF
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockquest?serverTimezone=UTC&characterEncoding=UTF-8
    username: stockquest
    password: stockquest123
  
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your_very_long_random_string_at_least_32_characters_for_local_dev
  expiration: 86400000

logging:
  level:
    com.stockquest: DEBUG
EOF
    print_success "Backend local configuration created"
fi

# Create frontend .env.local if needed
if [ ! -f frontend/.env.local ]; then
    print_warning "Creating frontend local configuration..."
    cat > frontend/.env.local << EOF
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_MOCK_API=false
EOF
    print_success "Frontend local configuration created"
fi

echo ""
echo "🐳 Starting Docker services..."

# Start MySQL and Redis
docker-compose up -d mysql redis

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
for i in {1..30}; do
    if docker exec stockquest-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
        print_success "MySQL is ready"
        break
    fi
    echo -n "."
    sleep 1
done

# Wait for Redis to be ready
echo "Waiting for Redis to be ready..."
for i in {1..10}; do
    if docker exec stockquest-redis redis-cli ping 2>/dev/null | grep -q PONG; then
        print_success "Redis is ready"
        break
    fi
    echo -n "."
    sleep 1
done

echo ""
echo "🔨 Building backend..."
cd backend
chmod +x gradlew
./gradlew build -x test
print_success "Backend built successfully"

echo ""
echo "📚 Running database migrations..."
./gradlew flywayMigrate
print_success "Database migrations completed"

cd ..

echo ""
echo "📦 Installing frontend dependencies..."
cd frontend
pnpm install
print_success "Frontend dependencies installed"

cd ..

echo ""
echo "✨ Setup completed successfully!"
echo ""
echo "To start the application:"
echo "  1. Backend:  cd backend && ./gradlew bootRun"
echo "  2. Frontend: cd frontend && pnpm dev"
echo ""
echo "Access the application at:"
echo "  - Frontend: http://localhost:3000"
echo "  - Backend API: http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "To run tests:"
echo "  - Backend:  cd backend && ./gradlew test"
echo "  - Frontend: cd frontend && pnpm test"
echo ""
print_success "Happy coding! 🎉"