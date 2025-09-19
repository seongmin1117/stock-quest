# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Workflow Requirements

### Build & Commit Protocol
**CRITICAL**: Always verify compilation and build success before any commit or next step.

#### Pre-Commit Checklist
1. ✅ **Backend Compilation**: `JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava`
2. ✅ **Backend Build**: `JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew build -x test`
3. ✅ **Backend Server Start**: `JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew bootRun --dry-run`
4. ✅ **Frontend Type Check**: `npm run type-check`
5. ✅ **Frontend Build**: `npm run build`
6. ✅ **Database Migration**: Verify all Flyway migrations apply cleanly
7. ✅ **API Integration**: Test that frontend can retrieve data from backend
8. ✅ **External Dependencies**: Ensure no external API calls cause server startup failures

#### Development Quality Gates
**For New Features**:
- ✅ **TDD Implementation**: Write tests before implementation (following company sync service example)
- ✅ **Hexagonal Architecture**: Maintain domain purity (no Spring dependencies in domain layer)
- ✅ **API Contract**: Frontend-backend integration tested with real API calls
- ✅ **Error Handling**: Graceful fallbacks when external services unavailable

**For Frontend Components**:
- ✅ **Real Data Integration**: No mock data in production paths
- ✅ **WebSocket Connection**: Real-time features properly connected
- ✅ **Mobile Responsive**: Test on mobile viewport
- ✅ **Accessibility**: WCAG compliance for new UI components

#### Task Planning Requirements
- **Always create TodoWrite checklist** for multi-step tasks
- **Track progress in real-time** with status updates
- **Plan next steps** before completing current tasks
- **Document blockers** and resolution paths

### External API Integration Guidelines
- **Yahoo Finance API**: Keep as simulated for development - never block server startup
- **Real API integration**: Plan for future implementation without breaking current functionality
- **Error handling**: Graceful fallback when external services are unavailable

## Common Development Commands

### Backend (Spring Boot + Java 21)
```bash
# Build and run
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew bootRun
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew build

# Run tests
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew test
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew test --tests "*SpecificTest*"

# Database migrations
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew flywayMigrate
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew flywayRepair

# Clean build
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew clean
```

### Frontend (Next.js 14 + TypeScript)
```bash
# Development
npm run dev

# Build and production
npm run build
npm start

# Code quality
npm run lint
npm run type-check

# API generation from OpenAPI spec
npm run generate-api

# Testing
npm test
npm run test:e2e
```

### Docker Services
```bash
# Start MySQL and Redis
docker-compose up mysql redis -d

# Full stack with profiles
docker-compose --profile backend --profile frontend up -d
```

## Architecture Overview

### Backend - Hexagonal Architecture
The backend strictly follows Hexagonal Architecture (Ports & Adapters) with clear separation of concerns:

1. **Domain Layer** (`domain/`) - Pure business logic, no Spring dependencies
   - Entities: Core business objects (User, Challenge, Order, Portfolio, etc.)
   - Ports: Interfaces defining contracts for external adapters
   - Domain services: Business logic that doesn't fit in entities

2. **Application Layer** (`application/`) - Use cases and orchestration
   - Service classes implementing business use cases
   - Port interfaces (in/out) for inbound and outbound operations
   - Transaction boundaries and workflow coordination
   - DTOs for data transfer between layers

3. **Adapter Layer** (`adapter/`) - External system integration
   - `in/web/`: REST controllers, request/response DTOs
   - `in/websocket/`: WebSocket controllers for real-time features
   - `out/persistence/`: JPA repositories and entity mappings
   - `out/auth/`: JWT and security implementations
   - `out/market/`: External market data API clients

Key architectural rules:
- Domain layer MUST NOT have any Spring or external dependencies
- All external communication goes through ports (interfaces)
- Adapters implement port interfaces
- Application services orchestrate domain logic and adapters

### Frontend - Feature-Sliced Design
The frontend uses Feature-Sliced Design for scalable architecture:

- `app/`: Next.js 14 App Router pages
- `features/`: Business features (place-order, market-data, portfolio)
- `entities/`: Business entities matching backend domain
- `shared/`: Shared resources (API client, UI components, hooks)
- `widgets/`: Complex UI compositions

### Real-time Architecture
The system implements real-time features through:

1. **WebSocket Layer**: Spring WebSocket + STOMP for bidirectional communication
   - Market data streaming
   - Portfolio updates
   - Order execution notifications
   - ML trading signals

2. **AI/ML Integration**: Advanced analytics and trading intelligence
   - Real-time risk assessment
   - ML-based trading signals
   - Portfolio optimization
   - Backtesting capabilities

3. **Performance Optimization**:
   - Multi-level caching (Caffeine + Redis)
   - Async processing with Spring Reactor
   - Database query optimization with custom indexes
   - Connection pooling and resource management

## Key Business Flows

### Challenge Session Flow
1. User starts a challenge → Creates ChallengeSession with seed money
2. Session tracks portfolio, orders, and performance metrics
3. Real-time market data streams to frontend via WebSocket
4. Orders execute with slippage simulation
5. Session ends → Final performance calculated and leaderboard updated

### Order Execution Pipeline
1. Order validation (balance, instrument availability)
2. Slippage calculation (1-2% based on order size)
3. Portfolio position update
4. WebSocket notification to client
5. Event publishing for analytics

### Company Data Synchronization (NEW)
1. **Automated Sync**: Scheduled at 9:00 AM and 3:30 PM daily
2. **Manual Sync**: Admin-triggered via REST API endpoints
3. **Data Source**: Yahoo Finance API (simulated for development)
4. **Updates**: Market cap calculations based on latest stock prices
5. **Logging**: Sync results tracked in database for audit

### DCA (Dollar Cost Averaging) Simulation
1. User configures investment parameters (amount, frequency, period)
2. System simulates historical performance with real market data
3. Generates detailed reports with metrics and visualizations
4. Supports Korean companies with proper encoding (UTF-8)
5. PDF report generation with comprehensive analytics

### Authentication & Security
- JWT-based authentication with Spring Security
- Role-based access control (USER, ADMIN)
- Rate limiting with Bucket4j
- CORS configuration for frontend integration

## Database Schema Highlights

### Core Tables
- `user`: Authentication and profile
- `challenge`: Challenge definitions with time periods
- `challenge_session`: User participation in challenges
- `challenge_instrument`: Instruments available in each challenge
- `order_history`: All trading orders with execution details
- `portfolio_position`: Current positions per session
- `price_candle`: Historical OHLC market data

### Performance Indexes
The database includes carefully designed indexes for:
- Session-based queries (user_id, challenge_id combinations)
- Time-series data queries (timestamp-based)
- Leaderboard calculations (performance metrics)
- Real-time position lookups

## Testing Strategy

### Backend Tests
- Unit tests for domain logic
- Integration tests for API endpoints
- Contract tests for API compatibility
- Performance tests for critical paths
- Test containers for database testing
- **Note**: Some legacy tests may have compilation issues and are being refactored

### Frontend Tests
- Component unit tests with Jest
- E2E tests with Playwright
- MSW for API mocking during development

## Current Development Focus

### Recently Completed Areas
- **Admin Challenge Management**: ✅ Complete - Full CRUD API integration with TypeScript client
- **Dashboard API Integration**: ✅ Complete - Real backend data instead of mock data
- **Challenge Start Enhancement**: ✅ Complete - Force restart logic and improved session management
- **Code Quality Improvements**: ✅ Complete - Lombok fixes, logging optimization, Korean encoding

### Active Development Areas
- **User Challenge Participation**: Frontend components for users joining and participating in challenges
- **Real-time Challenge Updates**: WebSocket integration for live challenge sessions
- **Challenge Session Management**: Trading functionality within active challenge sessions
- **WebSocket Implementation**: Real-time market data and portfolio updates

## Recent Implementations

### Dashboard API Integration (2025-09-19)
- **Real Data Implementation**: Replaced mock data in dashboard page with actual backend API integration
- **Frontend API Client**: Fixed dashboard-client.ts to use real API endpoints instead of fallback mock data
- **UserStatsCard Integration**: Updated interface to match real API response structure
- **TanStack Query Integration**: Proper error handling and loading states for dashboard data
- **Impact**: Dashboard now displays real user statistics from backend database

### Challenge Start Enhancement (2025-09-19)
- **Force Restart Logic**: Added forceRestart parameter to handle existing active sessions gracefully
- **Challenge Data Expansion**: Created 10 additional diverse challenges with different difficulty levels (BEGINNER to EXPERT)
- **Session Management**: Enhanced logic to end existing sessions before starting new ones
- **Error Handling**: Improved user experience with clear error messages and guidance
- **API Enhancement**: Updated StartChallengeCommand and ChallengeController for better session management

### Code Quality Improvements (2025-09-19)
- **Lombok Builder Fixes**: Added @Builder.Default annotations to Company.java to eliminate warnings
- **JWT Logging Optimization**: Reduced log noise by setting JwtTokenProvider to WARN level
- **Korean URL Encoding**: Fixed Korean character handling in CompanyWebAdapter search endpoints
- **Build Verification**: Confirmed all changes compile and build successfully

### Company API Integration Fix (2025-09-18)
- **Frontend API Client Issue**: Fixed `response.data` extraction pattern in company-client.ts
- **Root Cause**: API client was double-extracting data causing `undefined` responses
- **Solution**: Implemented `response.data || response` pattern for all company API methods
- **Impact**: Fixed categories, popular companies, and search APIs returning empty arrays
- **Debug Infrastructure**: Created comprehensive debugging pages for API troubleshooting

### Company Synchronization Service
- **CompanySyncService**: Synchronizes company market data from external sources
- **YahooFinanceMarketDataClient**: Fetches real-time stock prices (simulated)
- **CompanySyncWebAdapter**: REST endpoints for admin-triggered sync
- **Scheduling**: Automated sync at market open/close times
- **TDD Approach**: Test-first development with comprehensive test coverage

### DCA Simulation Features
- Complete hexagonal architecture implementation
- Korean company data support with proper UTF-8 encoding
- Comprehensive E2E tests for simulation workflows
- Frontend API client integration
- Advanced analytics and reporting capabilities

### Admin Challenge Management Integration (2025-09-19)
- **Admin Challenge API Client**: Complete TypeScript client for challenge CRUD operations
- **Real API Integration**: Replaced all mock data with actual backend API calls
- **Challenge Management Pages**: Updated create, list, edit, and detail pages with real functionality
- **Type Safety**: Comprehensive TypeScript types and enums for challenge data structures
- **Error Handling**: Proper loading states, error handling, and validation
- **API Endpoints**: Full CRUD operations including status management and featured challenges
- **Frontend Integration**: Material-UI components with form validation and user authentication

## New API Endpoints

### Dashboard API
```bash
# Get user dashboard data
GET /api/dashboard
Authorization: Bearer <user-token>

# Response includes:
# - totalSessions, activeSessions, completedSessions
# - averageReturn, bestReturn, worstReturn, totalReturn
# - winRate and other performance metrics
```

### Challenge API Enhancement
```bash
# Start challenge with force restart option
POST /api/challenges/{challengeId}/start?forceRestart=true
Authorization: Bearer <user-token>

# Handles existing active sessions gracefully
# - forceRestart=false: Returns error if active session exists
# - forceRestart=true: Ends existing session and starts new one
```

### Company Synchronization (Admin Only)
```bash
# Sync single company
POST /api/v1/companies/sync/{symbol}
Authorization: Bearer <admin-token>

# Sync all companies
POST /api/v1/companies/sync/all
Authorization: Bearer <admin-token>

# Trigger scheduled sync manually
POST /api/v1/companies/sync/scheduled
Authorization: Bearer <admin-token>

# Get sync status
GET /api/v1/companies/sync/status
Authorization: Bearer <admin-token>
```

### Company Search & Information
```bash
# Search companies
GET /api/v1/companies/search?q=삼성&limit=10

# Get company by symbol
GET /api/v1/companies/{symbol}

# Get top companies
GET /api/v1/companies/top?limit=10

# Get companies by category
GET /api/v1/companies/category/{categoryId}

# Get all categories
GET /api/v1/companies/categories
```

### Admin Challenge Management (Admin Only)
```bash
# Create new challenge
POST /api/admin/challenges
Authorization: Bearer <admin-token>
Content-Type: application/json

# Update challenge
PUT /api/admin/challenges/{challengeId}
Authorization: Bearer <admin-token>

# Get challenges with search/filter
GET /api/admin/challenges?title=stock&difficulty=BEGINNER&page=0&size=10
Authorization: Bearer <admin-token>

# Get challenge by ID
GET /api/admin/challenges/{challengeId}
Authorization: Bearer <admin-token>

# Change challenge status
PATCH /api/admin/challenges/{challengeId}/status?status=ACTIVE&modifiedBy={userId}
Authorization: Bearer <admin-token>

# Activate challenge
POST /api/admin/challenges/{challengeId}/activate?modifiedBy={userId}
Authorization: Bearer <admin-token>

# Set featured status
PATCH /api/admin/challenges/{challengeId}/featured?featured=true&modifiedBy={userId}
Authorization: Bearer <admin-token>

# Clone challenge
POST /api/admin/challenges/{challengeId}/clone?newTitle=New%20Title&createdBy={userId}
Authorization: Bearer <admin-token>

# Get popular challenges
GET /api/admin/challenges/popular?limit=10
Authorization: Bearer <admin-token>

# Get featured challenges
GET /api/admin/challenges/featured
Authorization: Bearer <admin-token>
```

## Korean Language Encoding Configuration

### **CRITICAL**: UTF-8 Encoding Standards
All environments MUST use consistent UTF-8 encoding to support Korean text (한글). Any deviation causes data corruption that requires manual database fixes.

#### Environment Configuration Requirements

**1. Development Environment (application-dev.yml)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/stockquest?serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&allowPublicKeyRetrieval=true&useSSL=false&autoReconnect=true&useLocalSessionState=true&rewriteBatchedStatements=true
    hikari:
      connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
```

**2. Docker Environment (docker-compose.yml)**
```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/stockquest?serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&allowPublicKeyRetrieval=true&useSSL=false&autoReconnect=true&useLocalSessionState=true&rewriteBatchedStatements=true
```

**3. MySQL Server Configuration (docker-compose.yml)**
```yaml
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci
  - --default-time-zone=+09:00
```

#### **REQUIRED** MySQL Connection Parameters
These parameters are **MANDATORY** in ALL environments:
- `characterEncoding=UTF-8` - Sets client character encoding
- `useUnicode=true` - **CRITICAL**: Enables Unicode support
- `serverTimezone=UTC` - Prevents timezone issues
- `allowPublicKeyRetrieval=true` - For secure connections
- `useSSL=false` - Development environment setting
- `autoReconnect=true` - Connection reliability
- `useLocalSessionState=true` - Performance optimization
- `rewriteBatchedStatements=true` - Batch operation optimization

#### Encoding Verification Commands

**Database Encoding Status**:
```bash
# Check MySQL server charset configuration
docker exec stockquest-mysql mysql -u root -prootpassword -e "SHOW VARIABLES LIKE '%char%';"

# Verify table and column encodings
docker exec stockquest-mysql mysql -u root -prootpassword stockquest -e "SHOW TABLE STATUS WHERE Name='company';"

# Check actual data encoding
docker exec stockquest-mysql mysql -u root -prootpassword stockquest -e "SELECT symbol, name_kr, HEX(name_kr) FROM company WHERE symbol='005930';"
```

**API Response Verification**:
```bash
# Test Korean company data (should show clear Korean text)
curl -X GET "http://localhost:8080/api/v1/companies/005930" -H "Content-Type: application/json"

# Test category data (should show clear Korean text)
curl -X GET "http://localhost:8080/api/v1/companies/categories" -H "Content-Type: application/json"
```

#### Troubleshooting Korean Text Issues

**Symptoms of Encoding Problems**:
- Korean text appears as: `ì‚¼ì„±ì „ìž` instead of `삼성전자`
- API responses show corrupted characters
- Database contains double-encoded UTF-8 bytes

**Root Cause Analysis Checklist**:
1. ✅ Verify `useUnicode=true` in ALL database connection URLs
2. ✅ Check MySQL server charset is `utf8mb4`
3. ✅ Confirm HikariCP `connection-init-sql` is set
4. ✅ Validate environment variable consistency between dev/docker
5. ✅ Test with fresh data insertion (not corrupted legacy data)

**Recovery Steps for Corrupted Data**:
1. Fix connection string configuration
2. Use `CompanySyncService` to refresh data from external sources
3. For manual fixes, use direct Korean text (not hex encoding)
4. Clear application cache after data fixes
5. Restart application to ensure new connections use correct encoding

#### Prevention Guidelines

**For All New Features**:
- Test Korean text handling in development environment
- Verify Docker environment uses identical database connection parameters
- Add Korean text to automated tests
- Document any Korean-specific requirements

**For Database Changes**:
- Always specify `utf8mb4` charset for new tables
- Use `utf8mb4_unicode_ci` collation for Korean text columns
- Test migration scripts with Korean sample data
- Verify foreign key relationships preserve encoding

**For Production Deployment**:
- Validate encoding configuration in production environment variables
- Test Korean data flow end-to-end before deployment
- Monitor for encoding-related errors in logs
- Maintain backup of Korean data before major updates

#### Historical Context
This configuration was established after resolving Korean text corruption issues in January 2025. The root cause was `useUnicode=true` missing from Docker environment database connection, while development environment had correct settings. This led to Korean text being double-encoded and corrupted during JSON serialization.