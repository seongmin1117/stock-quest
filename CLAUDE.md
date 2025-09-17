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

## Recent Implementations

### Company Synchronization Service (2025-01)
- **CompanySyncService**: Synchronizes company market data from external sources
- **YahooFinanceMarketDataClient**: Fetches real-time stock prices (simulated)
- **CompanySyncWebAdapter**: REST endpoints for admin-triggered sync
- **Scheduling**: Automated sync at market open/close times
- **TDD Approach**: Test-first development with comprehensive test coverage

### DCA Simulation Features (2024-12)
- Complete hexagonal architecture implementation
- Korean company data support with proper UTF-8 encoding
- Comprehensive E2E tests for simulation workflows
- Frontend API client integration
- Advanced analytics and reporting capabilities

## New API Endpoints

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