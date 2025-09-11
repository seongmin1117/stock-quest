#!/bin/bash

# StockQuest Documentation Generation Script
# Generates API documentation, architecture diagrams, and project documentation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "📚 Generating StockQuest documentation..."

# Check if we're in the correct directory
if [ ! -f "README.md" ] || [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo -e "${RED}❌ Please run this script from the StockQuest project root directory${NC}"
    exit 1
fi

# Create docs directory if it doesn't exist
mkdir -p docs/{api,architecture,guides,screenshots}

echo "📖 Documentation generation started..."

# Generate API documentation
echo ""
echo -e "${BLUE}🔌 Generating API documentation...${NC}"

# Check if Swagger/OpenAPI is configured
if [ -d "backend/src/main/java" ]; then
    echo "  🔍 Scanning for API endpoints..."
    
    # Count REST endpoints
    REST_ENDPOINTS=$(find backend/src/main/java -name "*.java" | xargs grep -l "@RestController\|@RequestMapping" | wc -l)
    echo "  📊 Found $REST_ENDPOINTS REST controller files"
    
    # Generate API documentation outline
    cat > docs/api/README.md << 'EOF'
# StockQuest API Documentation

This directory contains the API documentation for StockQuest.

## API Overview

StockQuest provides a RESTful API for stock trading simulation, user management, and challenge features.

### Base URL
- **Development**: `http://localhost:8080`
- **Production**: `https://api.stockquest.example.com`

### Authentication
All API endpoints (except public endpoints) require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration  
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{id}` - Get user by ID

### Portfolio Management
- `GET /api/portfolio` - Get user's portfolio
- `POST /api/portfolio/buy` - Buy stocks
- `POST /api/portfolio/sell` - Sell stocks
- `GET /api/portfolio/history` - Get transaction history

### Stock Data
- `GET /api/stocks` - List available stocks
- `GET /api/stocks/{symbol}` - Get stock details
- `GET /api/stocks/{symbol}/history` - Get stock price history

### Challenge System
- `GET /api/challenges` - List available challenges
- `POST /api/challenges/{id}/join` - Join a challenge
- `GET /api/challenges/{id}/leaderboard` - Get challenge leaderboard

### Ranking & Statistics
- `GET /api/ranking/global` - Global user ranking
- `GET /api/ranking/friends` - Friends ranking
- `GET /api/stats/user/{id}` - User statistics

## Response Format

All API responses follow this structure:
```json
{
  "success": true,
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-09-11T12:00:00Z"
}
```

## Error Handling

Error responses include appropriate HTTP status codes and error details:
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Detailed error message",
    "details": { ... }
  },
  "timestamp": "2024-09-11T12:00:00Z"
}
```

## Rate Limiting

API endpoints are rate-limited to prevent abuse:
- **Default**: 100 requests per minute per user
- **Authentication**: 10 requests per minute per IP
- **Trading**: 50 requests per minute per user

## WebSocket Endpoints

Real-time updates are provided via WebSocket:
- `ws://localhost:8080/ws/stock-prices` - Real-time stock price updates
- `ws://localhost:8080/ws/portfolio` - Portfolio update notifications

## Development

To run the API locally:
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

API will be available at: http://localhost:8080

### Swagger UI
Interactive API documentation is available at:
- **Development**: http://localhost:8080/swagger-ui/
- **Production**: https://api.stockquest.example.com/swagger-ui/

## Testing

API endpoints can be tested using:
- **Postman Collection**: `docs/api/StockQuest.postman_collection.json`
- **curl Examples**: See individual endpoint documentation
- **Integration Tests**: `backend/src/test/java/integration/`

EOF
    
    echo -e "${GREEN}✅ API documentation outline created${NC}"
fi

# Generate architecture documentation
echo ""
echo -e "${BLUE}🏗️ Generating architecture documentation...${NC}"

cat > docs/architecture/README.md << 'EOF'
# StockQuest Architecture Documentation

## System Overview

StockQuest is built using modern software architecture patterns to ensure scalability, maintainability, and testability.

### Architecture Patterns

#### Backend: Hexagonal Architecture (Clean Architecture)
- **Domain Layer**: Pure business logic, framework-agnostic
- **Application Layer**: Use cases and application services
- **Adapter Layer**: External integrations (REST, Database, etc.)

#### Frontend: Feature-Sliced Design
- **App**: Application entry point and routing
- **Pages**: Route components and page layouts  
- **Widgets**: Complex UI blocks and business components
- **Features**: Business logic features
- **Entities**: Business entities and domain objects
- **Shared**: Reusable UI components and utilities

### Technology Stack

#### Backend
- **Java 21**: Modern JVM features and performance
- **Spring Boot 3.5**: Enterprise framework with native image support
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Data persistence with Hibernate
- **MySQL 8.0**: Primary database for transactional data
- **Redis 7**: Caching and session storage
- **WebSocket**: Real-time communication

#### Frontend
- **React 18**: Modern UI library with concurrent features
- **Next.js 14**: Full-stack React framework with SSR/SSG
- **TypeScript 5**: Type safety and development experience
- **Tailwind CSS**: Utility-first CSS framework
- **React Query**: Server state management
- **Zustand**: Client state management

#### Infrastructure
- **Docker**: Containerization and development environment
- **Docker Compose**: Multi-container orchestration
- **GitHub Actions**: CI/CD pipeline automation
- **Pre-commit Hooks**: Code quality enforcement

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                           Frontend (Next.js)                    │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │   App   │ │  Pages  │ │ Widgets │ │Features │ │ Shared  │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                │
                          HTTP/WebSocket
                                │
┌─────────────────────────────────────────────────────────────────┐
│                        Backend (Spring Boot)                    │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Adapter Layer                        │   │
│  │  ┌─────────────────┐  ┌─────────────────┐              │   │
│  │  │   Web Adapters  │  │ Persistence     │              │   │
│  │  │   (REST/WS)     │  │ Adapters (JPA)  │              │   │
│  │  └─────────────────┘  └─────────────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                │                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                Application Layer                        │   │
│  │  ┌─────────────────┐  ┌─────────────────┐              │   │
│  │  │   Use Cases     │  │   Services      │              │   │
│  │  │   (Commands)    │  │   (Queries)     │              │   │
│  │  └─────────────────┘  └─────────────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                │                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Domain Layer                           │   │
│  │  ┌─────────────────┐  ┌─────────────────┐              │   │
│  │  │    Entities     │  │     Ports       │              │   │
│  │  │  (Business)     │  │  (Interfaces)   │              │   │
│  │  └─────────────────┘  └─────────────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                │
                        ┌───────┴───────┐
                        │               │
                   ┌─────────┐    ┌─────────┐
                   │  MySQL  │    │  Redis  │
                   │Database │    │  Cache  │
                   └─────────┘    └─────────┘
```

### Data Flow

1. **Request Flow**: Frontend → Web Adapter → Application Service → Domain Logic
2. **Response Flow**: Domain Logic → Application Service → Web Adapter → Frontend
3. **Data Flow**: Domain Entities ↔ Repository Ports ↔ Persistence Adapters ↔ Database

### Security Architecture

- **JWT Authentication**: Stateless authentication with refresh tokens
- **Role-Based Access**: User roles and permissions system
- **API Rate Limiting**: Request throttling and abuse prevention
- **Input Validation**: Request validation at adapter layer
- **SQL Injection Prevention**: Parameterized queries and ORM
- **XSS Prevention**: Content Security Policy and output encoding

### Performance Considerations

- **Database Optimization**: Indexing, query optimization, connection pooling
- **Caching Strategy**: Redis for session data and frequently accessed data
- **Frontend Optimization**: Code splitting, lazy loading, image optimization
- **API Optimization**: Pagination, filtering, and response compression

### Deployment Architecture

- **Development**: Docker Compose with local services
- **Production**: Container orchestration with load balancing
- **Database**: Managed database service with replication
- **Caching**: Distributed Redis cluster for high availability
- **CDN**: Static asset delivery and global distribution

### Monitoring and Observability

- **Application Metrics**: Custom business metrics and KPIs
- **Performance Monitoring**: Response time and throughput tracking
- **Error Tracking**: Exception monitoring and alerting
- **Log Aggregation**: Centralized logging with structured data
- **Health Checks**: Service health endpoints and automated monitoring

EOF

echo -e "${GREEN}✅ Architecture documentation created${NC}"

# Generate development guides
echo ""
echo -e "${BLUE}📝 Generating development guides...${NC}"

cat > docs/guides/DEVELOPMENT.md << 'EOF'
# StockQuest Development Guide

## Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Docker & Docker Compose
- Git
- Your favorite IDE (IntelliJ IDEA recommended for Java, VSCode for frontend)

### Quick Setup
```bash
# Clone the repository
git clone https://github.com/seongmin1117/stock-quest.git
cd stock-quest

# Run the setup script
./scripts/setup-dev.sh

# Start development environment
./scripts/start-dev.sh
```

## Development Workflow

### 1. Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: Feature development branches
- `hotfix/*`: Critical bug fixes

### 2. Commit Guidelines
We use Conventional Commits format:
```
type(scope): description

feat(auth): add JWT token validation
fix(portfolio): resolve stock price calculation error
docs(api): update authentication endpoint documentation
```

### 3. Code Review Process
1. Create feature branch from `develop`
2. Implement feature with tests
3. Create Pull Request with template
4. Code review and approval
5. Merge to `develop`

## Development Standards

### Backend (Java/Spring Boot)

#### Code Style
- Follow Google Java Style Guide
- Use meaningful names for classes, methods, and variables
- Keep methods small and focused (max 20-30 lines)
- Write self-documenting code

#### Architecture Rules
- **Domain Layer**: No framework dependencies, pure business logic
- **Application Layer**: Use cases and services, can use Spring annotations
- **Adapter Layer**: External integrations, all framework code goes here

#### Testing
- **Unit Tests**: Test business logic in isolation
- **Integration Tests**: Test complete workflows
- **Test Coverage**: Maintain >80% line coverage

```java
// Example: Domain entity (no annotations)
public class User {
    private UserId id;
    private Email email;
    private Money balance;
    
    public void buyStock(Stock stock, int quantity) {
        // Business logic here
    }
}

// Example: Application service
@Service
@Transactional
public class BuyStockService implements BuyStockUseCase {
    // Implementation here
}
```

### Frontend (React/TypeScript)

#### Code Style
- Use TypeScript strictly, avoid `any` types
- Use functional components with hooks
- Follow Feature-Sliced Design architecture
- Use meaningful component and file names

#### Component Structure
```typescript
// Feature component example
interface StockPurchaseProps {
  stock: Stock;
  onPurchase: (quantity: number) => void;
}

export const StockPurchase: React.FC<StockPurchaseProps> = ({
  stock,
  onPurchase
}) => {
  // Component logic
  return (
    // JSX here
  );
};
```

#### State Management
- **Server State**: React Query for API data
- **Client State**: Zustand for UI state
- **Form State**: React Hook Form for forms

### Database

#### Migration Strategy
- Use Flyway migrations for database changes
- Never modify existing migrations
- Always test migrations on sample data

#### Naming Conventions
- Tables: snake_case (e.g., `user_portfolios`)
- Columns: snake_case (e.g., `created_at`)
- Indexes: descriptive names (e.g., `idx_users_email`)

## Testing Guidelines

### Backend Testing
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests UserServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Frontend Testing
```bash
# Run all tests
pnpm test

# Run with coverage
pnpm test:coverage

# Run E2E tests
pnpm test:e2e
```

### Test Types
1. **Unit Tests**: Fast, isolated, test single units
2. **Integration Tests**: Test component interactions
3. **E2E Tests**: Test complete user workflows

## Performance Guidelines

### Backend Performance
- Use database indexes appropriately
- Implement caching for frequently accessed data
- Optimize N+1 queries with batch loading
- Use pagination for large datasets

### Frontend Performance
- Code splitting for large components
- Lazy loading for routes
- Image optimization and CDN
- Bundle size monitoring

## Security Considerations

### Authentication & Authorization
- JWT tokens with proper expiration
- Role-based access control
- Secure password hashing (BCrypt)

### Input Validation
- Validate all input at API boundaries
- Use parameterized queries
- Sanitize output to prevent XSS

### Data Protection
- Never log sensitive data
- Encrypt sensitive data at rest
- Use HTTPS in production

## Troubleshooting

### Common Issues

#### Backend won't start
1. Check Java version: `java -version`
2. Check database connection
3. Verify environment variables

#### Frontend build fails
1. Check Node.js version: `node -v`
2. Clear node_modules and reinstall: `pnpm install`
3. Check for TypeScript errors

#### Database connection issues
1. Ensure Docker containers are running
2. Check connection strings
3. Verify database credentials

### Getting Help
1. Check existing documentation
2. Search GitHub issues
3. Ask in team chat
4. Create new GitHub issue

## Tools and Resources

### Recommended IDE Extensions
- **IntelliJ IDEA**: Spring Boot, Lombok, Docker
- **VSCode**: ES7 React snippets, Prettier, ESLint, TypeScript Hero

### Useful Commands
```bash
# Backend
./gradlew bootRun                    # Start backend
./gradlew test                       # Run tests
./gradlew build                      # Build JAR

# Frontend  
pnpm dev                            # Start dev server
pnpm build                          # Build for production
pnpm type-check                     # Check TypeScript

# Docker
docker-compose up -d                # Start services
docker-compose logs -f backend      # View logs
docker-compose down                 # Stop services

# Git
git lg                              # Pretty log
git st                              # Short status
pre-commit run --all-files         # Run validation
```
EOF

echo -e "${GREEN}✅ Development guide created${NC}"

# Make all new scripts executable
chmod +x scripts/setup-git.sh scripts/setup-dev.sh scripts/generate-docs.sh

echo ""
echo -e "${GREEN}🎉 Documentation generation complete!${NC}"
echo ""
echo -e "${BLUE}📚 Generated documentation:${NC}"
echo "  📖 docs/api/README.md - API documentation outline"
echo "  🏗️ docs/architecture/README.md - System architecture"
echo "  📝 docs/guides/DEVELOPMENT.md - Development guide"
echo ""
echo -e "${BLUE}📂 Documentation structure:${NC}"
echo "  docs/"
echo "  ├── api/                  # API documentation"  
echo "  ├── architecture/         # System architecture docs"
echo "  ├── guides/              # Development guides"
echo "  └── screenshots/         # UI screenshots and diagrams"
echo ""
echo -e "${BLUE}🔧 Generated scripts:${NC}"
echo "  🛠️ scripts/setup-git.sh - Git configuration automation"
echo "  🚀 scripts/setup-dev.sh - Development environment setup"
echo "  📚 scripts/generate-docs.sh - Documentation generation"
echo ""
echo "💡 Next steps:"
echo "  1. Review generated documentation"
echo "  2. Add project screenshots to docs/screenshots/"
echo "  3. Enhance API documentation with Swagger/OpenAPI"
echo "  4. Add architecture diagrams and flowcharts"