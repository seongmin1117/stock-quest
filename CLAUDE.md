# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

### Frontend Tests
- Component unit tests with Jest
- E2E tests with Playwright
- MSW for API mocking during development