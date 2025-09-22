# CLAUDE.md - Stock Quest Backend Guide for Claude Code

Backend-specific guidance for the Stock Quest trading simulation platform.

## 🎯 Backend Overview

**Stock Quest Backend** is a production-ready Spring Boot application implementing sophisticated trading simulation logic with real-time capabilities.

- **Architecture**: Hexagonal (Ports & Adapters) with Domain-Driven Design
- **Technology Stack**: Spring Boot 3.5.5 + Java 21 + MySQL 8.0 + Redis 7
- **Performance**: EhCache L2 Cache + HikariCP optimization (50-70% query reduction)
- **Status**: Core features complete, 8.11-second startup time

## 🚀 Quick Commands

### Essential Build Commands
```bash
# Set Java environment
export JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home

# Development workflow
./gradlew compileJava    # Verify compilation
./gradlew build -x test  # Build without tests
./gradlew bootRun        # Start server (localhost:8080)

# Quality checks
./gradlew test           # Run test suite
./gradlew flywayMigrate  # Apply database migrations
```

## 🏗️ Hexagonal Architecture

### Core Principles
- **Domain Layer**: Pure business logic, NO Spring dependencies
- **Application Layer**: Use cases and orchestration
- **Adapter Layer**: External integrations (REST, JPA, Redis)
- **Port Interfaces**: Define contracts between layers

### Directory Structure
```
src/main/java/com/stockquest/
├── domain/                    # 🎯 Core Business Logic
│   ├── challenge/             # Challenge entities and domain services
│   ├── portfolio/             # Portfolio management logic
│   ├── market/               # Market data domain
│   ├── session/              # Trading session management
│   └── user/                 # User domain
├── application/              # 🔄 Use Cases & Orchestration
│   ├── challenge/            # Challenge service implementations
│   ├── portfolio/            # Portfolio service implementations
│   └── port/                # Port interfaces (in/out)
└── adapter/                  # 🔌 External Integrations
    ├── in/web/              # REST controllers + WebSocket
    └── out/                 # JPA repositories + External APIs
        ├── persistence/     # Database adapters
        └── market/          # External market data clients
```

## ✅ Current Status: Production Ready (Alpha v0.1.0)

### Core Features Complete
- **Hexagonal Architecture**: 100% implemented with domain purity
- **Trading Simulation**: Order execution, portfolio management, real-time P&L
- **Challenge System**: 13 scenarios with leaderboard rankings
- **Real-time Features**: WebSocket streaming for market data and portfolio updates
- **ML/AI Integration**: Trading signals, portfolio optimization, risk analysis
- **Performance Optimized**: EhCache L2 + HikariCP (50-70% query reduction)
- **Security Hardened**: JWT auth, rate limiting, input validation
- **API Documentation**: Complete OpenAPI 3.0 specification

### Recent Achievements (2025-09)
- **Server Performance**: 8.11-second startup, stable production operation
- **Cache Implementation**: 20 cache regions with template-based policies
- **Database Optimization**: HikariCP 150% efficiency improvement
- **Korean Language Support**: Complete UTF-8 encoding resolution
- **API Client Generation**: Automated TypeScript client from OpenAPI spec
- **Company Sync Service**: Automated market data synchronization

## 🔧 Technology Stack

### Backend Technologies
- **Framework**: Spring Boot 3.5.5 with Java 21 (Temurin)
- **Database**: MySQL 8.0 + Redis 7 for caching
- **Cache**: Hibernate L2 Cache (EhCache 3.10.8) + Caffeine
- **Connection Pool**: HikariCP with optimized settings
- **Authentication**: JWT with Spring Security
- **Real-time**: WebSocket + STOMP protocol
- **Documentation**: OpenAPI 3.0 + Swagger UI
- **Migration**: Flyway for database schema management

### Performance Features
- **Multi-layer Caching**: L1 (JPA) + L2 (EhCache) + L3 (Redis)
- **Connection Optimization**: HikariCP with 150% efficiency improvement
- **Query Optimization**: 50-70% reduction through intelligent caching
- **Real-time Streaming**: WebSocket for sub-100ms data delivery
- **Rate Limiting**: Bucket4j for API protection

## 🌏 Korean Language Support (Critical)

### UTF-8 Configuration Requirements
**MANDATORY**: All environments must use consistent UTF-8 encoding for Korean text support.

```yaml
# Database Connection (REQUIRED)
spring.datasource.url: jdbc:mysql://localhost:3306/stockquest?characterEncoding=UTF-8&useUnicode=true&serverTimezone=UTC

# HikariCP Configuration
hikari.connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"

# MySQL Server Settings
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci
```

### Critical Parameters
- `useUnicode=true` - **CRITICAL**: Enables Unicode support
- `characterEncoding=UTF-8` - Sets client character encoding
- `utf8mb4` - Required for full Korean character support

### Verification Commands
```bash
# Test Korean company data
curl -X GET "http://localhost:8080/api/v1/companies/005930" -H "Content-Type: application/json"

# Should display: "삼성전자" (not corrupted characters)
```

## 📊 Core Business Logic

### Trading Simulation Engine
- **Market Data**: Real historical data compressed 10-100x speed
- **Company Anonymization**: Companies shown as A, B, C to prevent bias
- **Order Execution**: Market/Limit orders with 1-2% slippage simulation
- **Portfolio Management**: Real-time P&L calculation and position tracking
- **Challenge System**: 13 scenarios from "COVID Crash" to "Global Diversification"

### Advanced Features
- **ML Trading Signals**: AI-powered trading recommendations
- **Portfolio Optimization**: Automated rebalancing algorithms
- **Risk Management**: VaR calculation and portfolio risk metrics
- **DCA Simulation**: Dollar Cost Averaging backtesting
- **Backtesting Engine**: Historical strategy validation

## 🔧 Development Guidelines

### Hexagonal Architecture Rules
- **Domain Layer**: NO Spring dependencies, pure business logic
- **Application Layer**: Use cases and orchestration
- **Adapter Layer**: External integrations (REST, JPA, Redis)
- **Port Interfaces**: Define contracts between layers

### Code Quality Standards
- **Testing**: Unit (domain) + Integration (adapters) + E2E (workflows)
- **Database**: snake_case naming, audit columns required
- **API**: RESTful design with OpenAPI 3.0 documentation
- **Security**: JWT authentication, input validation, rate limiting
- **Korean Support**: UTF-8 encoding for all text handling

### Pre-Commit Checklist
```bash
# 1. Compilation check
./gradlew compileJava

# 2. Build verification
./gradlew build -x test

# 3. Test critical paths
./gradlew test

# 4. Verify Korean text support
curl -s http://localhost:8080/api/v1/companies/005930 | jq .nameKr
```

## 📚 API Documentation

### OpenAPI 3.0 Integration
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- **Frontend Guide**: `/backend/docs/API-GUIDE-FOR-FRONTEND.md`

### Key API Categories
```
🔐 Authentication      /api/auth/*              JWT + Spring Security
🏢 Company Data        /api/v1/companies/*      Korean company information
🎯 Challenges          /api/challenges/*        Trading challenge system
💼 Portfolio           /api/portfolio/*         Position management
📊 Market Data         /api/market/*            Real-time market information
🤖 ML/AI Features      /api/v1/ml/*            Trading signals & optimization
🛠 Admin Tools         /api/admin/*             Administrative functions
```

### Frontend Integration
- **TypeScript Client**: Auto-generated from OpenAPI spec
- **Real-time Updates**: WebSocket integration for live data
- **Error Handling**: Consistent error response format
- **Authentication**: JWT Bearer token management

## 🐛 Troubleshooting

### Common Issues & Solutions

**Server Won't Start**:
```bash
# Check Java version
java -version  # Must be Java 21

# Verify JAVA_HOME
echo $JAVA_HOME

# Clean rebuild
./gradlew clean build
```

**Database Connection Issues**:
```bash
# Check MySQL container
docker ps | grep mysql

# Verify connection string has useUnicode=true
grep -r "useUnicode" src/main/resources/

# Test database connectivity
./gradlew flywayValidate
```

**Korean Text Corruption**:
```bash
# Verify UTF-8 encoding in database URL
grep -r "characterEncoding=UTF-8" src/main/resources/

# Test API response
curl -s http://localhost:8080/api/v1/companies/005930 | jq .nameKr
```

**Performance Issues**:
```bash
# Check cache hit rates
curl -s http://localhost:8080/actuator/metrics/cache.gets

# Monitor database connections
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections
```

### Debug Commands
```bash
# Compilation check
./gradlew compileJava --console=plain

# Dependency analysis
./gradlew dependencies --configuration compileClasspath

# Test API endpoints
curl -s http://localhost:8080/api-docs | jq .info.title
curl -s http://localhost:8080/actuator/health
```

---
*Backend guide for Claude Code - Updated 2025-09-22*