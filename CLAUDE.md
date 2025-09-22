# CLAUDE.md - Stock Quest Project Guide for Claude Code

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with the Stock Quest codebase.

## 🎯 Project Overview

**Stock Quest** is a sophisticated stock trading simulation platform that helps users learn investing through historical market data replay. The platform accelerates real market data (10-100x speed) while anonymizing company names to eliminate bias.

- **Version**: Alpha (v0.1.0)
- **Architecture**: Hexagonal (Backend) + Feature-Sliced Design (Frontend)
- **Status**: Core features complete, ready for performance optimization

## 🚀 Quick Start

### Prerequisites
- Java 21 (Temurin)
- Node.js 20+
- Docker & Docker Compose
- MySQL 8.0
- Redis 7

### Development Commands

```bash
# Backend (Spring Boot)
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew bootRun

# Frontend (Next.js)
npm run dev

# Database Services
docker-compose up mysql redis -d

# Generate API Client (Frontend)
npm run generate-api

# Run Tests
./gradlew test  # Backend
npm test        # Frontend
```

## 🏗️ Architecture & Technology Stack

### Backend - Hexagonal Architecture
```
src/main/java/com/stockquest/
├── domain/        # Pure business logic (NO Spring dependencies)
├── application/   # Use cases and orchestration
└── adapter/       # External integrations
    ├── in/web/    # REST controllers, WebSocket
    └── out/       # JPA, Redis, External APIs
```

**Key Technologies:**
- Spring Boot 3.5.5 + Java 21
- MySQL 8.0 + Redis 7
- Hibernate with L2 Cache (EhCache 3.10.8)
- JWT Authentication + Spring Security
- WebSocket for real-time features

### Frontend - Feature-Sliced Design
```
src/
├── app/          # Next.js 14 App Router
├── features/     # Business features
├── entities/     # Business entities
├── widgets/      # Complex UI compositions
└── shared/       # Shared resources
```

**Key Technologies:**
- Next.js 14 (App Router) + React 18
- TypeScript 5.5 + TanStack Query
- Material-UI + Heroicons
- OpenAPI Client Generation (Orval)

## 🔧 Development Workflow

### Pre-Commit Checklist
```bash
# 1. Backend Compilation
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava

# 2. Frontend Type Check
npm run type-check

# 3. Build Verification
./gradlew build -x test  # Backend
npm run build            # Frontend

# 4. Test Critical Paths
./gradlew test
npm test
```

### Git Workflow
```bash
# NEVER auto-commit unless explicitly requested
# Always verify builds before committing
# Use conventional commit messages
```

## 🌏 Korean Language Support (Critical)

### UTF-8 Configuration Requirements
```yaml
# Database Connection (MANDATORY)
spring.datasource.url: jdbc:mysql://localhost:3306/stockquest?characterEncoding=UTF-8&useUnicode=true

# MySQL Docker Configuration
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci

# HikariCP Initialization
hikari.connection-init-sql: "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
```

### Known Issue Resolution
- **Problem**: Double UTF-8 encoding in challenge data (IDs 4-13 were corrupted)
- **Solution**: Always use `--default-character-set=utf8mb4` with MySQL client
- **Prevention**: Use application-level data entry, not direct SQL

## 📊 Core Business Features

### Trading Simulation
- **Market Data**: Real historical data compressed 10-100x
- **Company Anonymization**: Companies shown as A, B, C to prevent bias
- **Order Types**: Market/Limit orders with slippage simulation
- **Portfolio Management**: Real-time P&L calculation

### Advanced Modules
- **DCA Simulation**: Dollar Cost Averaging backtesting
- **ML Trading Signals**: AI-powered trading recommendations
- **Portfolio Optimization**: Automated rebalancing algorithms
- **Risk Management**: VaR, portfolio risk metrics
- **Backtesting**: Historical strategy validation

### Challenge System
- **13 Scenarios**: From "COVID Crash" to "Global Diversification"
- **Difficulty Levels**: Beginner → Expert
- **Real-time Leaderboard**: Competitive rankings
- **Session Management**: Track progress across challenges

## 🎯 Current Development Status

### ✅ Completed (2025-09)
- OpenAPI 3.1 compatibility
- Frontend API client automation
- Korean encoding issues resolved
- Hibernate L2 Cache implementation
- HikariCP optimization (150% improvement)
- 10 Repository adapters for Hexagonal Architecture
- Feature-Sliced Design migration
- Admin Challenge Management System
- Company Synchronization Service
- DCA Simulation Module

### 🔥 High Priority Tasks
1. **Performance**: Query optimization, pagination
2. **Real-time**: WebSocket market data streaming
3. **Mobile**: Responsive UI optimization
4. **Testing**: 80% coverage target

## 🔒 Security & Performance

### Security Measures
- JWT with Refresh Tokens
- Rate Limiting (Bucket4j)
- Spring Security with role-based access
- Input validation and sanitization

### Performance Optimizations
- Multi-layer caching (Hibernate L2C + Redis + Caffeine)
- HikariCP connection pooling (tuned)
- Database indexes on critical paths
- Next.js bundle optimization

## 📚 API Documentation

### Backend Endpoints
```
GET    /api/challenges                 # List challenges
POST   /api/challenges/{id}/start      # Start challenge
GET    /api/sessions/{id}              # Get session details
POST   /api/orders                     # Place order
GET    /api/portfolio/{sessionId}      # Get portfolio
GET    /api/v1/ml/signals/active       # Get ML signals
POST   /api/v1/portfolio/optimize      # Portfolio optimization
```

### Frontend Routes
```
/                     # Landing page
/challenges           # Challenge list
/challenges/[id]      # Challenge details
/dashboard            # User dashboard
/trading/[sessionId]  # Trading interface
/leaderboard          # Rankings
```

## 🐛 Troubleshooting

### Common Issues & Solutions

1. **Korean Text Corruption**
   ```sql
   -- Fix: Use UTF-8 client
   docker exec stockquest-mysql mysql --default-character-set=utf8mb4
   ```

2. **Backend Won't Start**
   ```bash
   # Check Java version
   java -version  # Must be Java 21

   # Clean and rebuild
   ./gradlew clean build
   ```

3. **Frontend TypeScript Errors**
   ```bash
   # Regenerate API client
   npm run generate-api

   # Clean install
   rm -rf node_modules .next
   npm install
   ```

4. **Generated API Files**
   ```bash
   # These are in .gitignore
   # Regenerate with:
   npm run generate-api
   ```

## 📋 Important Notes for Claude Code

### Development Principles
1. **NEVER** commit without explicit user request
2. **ALWAYS** verify compilation before any commit
3. **USE** TodoWrite for multi-step tasks
4. **CHECK** existing patterns before implementing new features
5. **MAINTAIN** Hexagonal Architecture boundaries (no Spring in domain layer)
6. **FOLLOW** Feature-Sliced Design for frontend features
7. **TEST** Korean text handling in all new features
8. **IGNORE** generated API files (they're in .gitignore)

### Architecture Rules
- Domain layer: Pure business logic, NO Spring dependencies
- Application layer: Use cases and orchestration
- Adapter layer: External integrations only
- Frontend features: Follow Feature-Sliced Design structure
- API clients: Use generated TypeScript clients from OpenAPI

### Quality Standards
- Evidence-based decisions (measure before optimizing)
- Clean Architecture (separation of concerns)
- Type safety (full TypeScript coverage)
- Progressive enhancement (graceful degradation)
- Mobile-first responsive design

## 🔗 Related Documentation

- `/backend/CLAUDE.md` - Backend-specific guide
- `/frontend/CLAUDE.md` - Frontend-specific guide
- `/README.md` - Public project documentation
- `/backend/docs/API-GUIDE-FOR-FRONTEND.md` - API integration guide
- `/backend/docs/openapi-complete.json` - OpenAPI specification

---
*Last Updated: 2025-09-22*
*Maintained for Claude Code optimal assistance*