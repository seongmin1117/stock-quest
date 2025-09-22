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

### Critical Issue: Double UTF-8 Encoding (Resolved 2025-09-22)

#### Problem Summary
A severe double UTF-8 encoding issue was discovered in Korean text data across multiple database tables:

**Affected Data:**
- `challenge_instrument` table: 13 records with corrupted Korean instrument names
- `challenge` table: 4 fields (`name`, `description`, `learning_objectives`, `market_context`)
- **Symptoms**: Korean text displayed as `ê¸°ìˆ ì£¼ A` instead of `기술주 A`

**Impact Scope:**
- 17 total corrupted records affecting challenge system functionality
- User-facing Korean text completely unreadable
- Challenge selection and trading interface compromised

#### Root Cause Analysis

**Primary Cause**: Direct MySQL client usage without proper charset specification
```bash
# WRONG: This causes double encoding
mysql -u root -p stockquest

# CORRECT: Must specify charset
mysql --default-character-set=utf8mb4 -u root -p stockquest
```

**Technical Details:**
1. **Initial Setup**: Database correctly configured for UTF-8 (utf8mb4)
2. **Data Entry Error**: Korean text entered via MySQL client without charset flag
3. **Double Encoding**: Already UTF-8 encoded Korean text was re-encoded as UTF-8
4. **Corruption Result**: Binary data stored as `LATIN1` interpreted as `UTF-8`

**Timeline:**
- Data corruption occurred during initial data seeding (manual SQL entry)
- Issue persisted undetected until comprehensive Korean text audit
- Application layer (Spring Boot) always handled Korean correctly

#### Resolution Process

**1. Data Assessment**
```sql
-- Identified corrupted records
SELECT id, name FROM challenge WHERE name LIKE '%ê%' OR name LIKE '%ì%';
SELECT id, instrument_name FROM challenge_instrument WHERE instrument_name LIKE '%ê%';
```

**2. Backup Creation**
```sql
-- Safety backup before correction
CREATE TABLE challenge_backup AS SELECT * FROM challenge;
CREATE TABLE challenge_instrument_backup AS SELECT * FROM challenge_instrument;
```

**3. Data Recovery**
```sql
-- Fixed double-encoded Korean text
UPDATE challenge SET name = '코로나 대폭락' WHERE id = 4;
UPDATE challenge SET description = '2020년 3월 코로나19...' WHERE id = 4;
-- (Continued for all affected records)
```

**4. Verification**
```sql
-- Confirmed proper Korean display
SELECT id, name, description FROM challenge WHERE id BETWEEN 4 AND 13;
```

#### Prevention Measures (CRITICAL)

**1. MySQL Client Usage Rules**
```bash
# MANDATORY: Always use charset flag
docker exec stockquest-mysql mysql --default-character-set=utf8mb4 -u root -p stockquest

# For mysqldump (backup/restore)
mysqldump --default-character-set=utf8mb4 --single-transaction stockquest > backup.sql
mysql --default-character-set=utf8mb4 stockquest < backup.sql
```

**2. Application-Level Data Entry (RECOMMENDED)**
```java
// Use Spring Boot repositories for Korean data
challengeRepository.save(challenge); // Proper UTF-8 handling guaranteed
```

**3. Data Quality Checks**
```sql
-- Regular Korean text validation
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = 'stockquest'
  AND (column_name LIKE '%name%' OR column_name LIKE '%description%');

-- Check for encoding issues
SELECT * FROM challenge WHERE name REGEXP '[ê|ì|â|î]';
```

#### Verification Methods

**1. Database Level**
```sql
-- Verify Korean text storage
SELECT id, name, HEX(name) as hex_encoding FROM challenge WHERE id = 4;
-- Should show proper Korean characters, not garbage
```

**2. API Level**
```bash
# Test API response encoding
curl -H "Accept: application/json; charset=utf-8" http://localhost:8080/api/challenges/4
```

**3. Frontend Level**
```javascript
// Verify proper display in browser
console.log(challenge.name); // Should display: "코로나 대폭락"
```

#### Reference Commands

**Safe MySQL Access:**
```bash
# Production-safe connection
docker exec stockquest-mysql mysql \
  --default-character-set=utf8mb4 \
  --database=stockquest \
  --user=root \
  --password

# Quick charset verification
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';
```

**Korean Text Testing:**
```sql
-- Test Korean input/output
SELECT '한글 테스트' as test_korean;
-- Should display exactly: 한글 테스트

-- Detect encoding problems
SELECT name, CHAR_LENGTH(name), OCTET_LENGTH(name)
FROM challenge
WHERE CHAR_LENGTH(name) != OCTET_LENGTH(name)/3;
```

**Emergency Recovery:**
```sql
-- If corruption detected, restore from backup
DROP TABLE challenge;
CREATE TABLE challenge AS SELECT * FROM challenge_backup;
```

#### Lessons Learned

1. **Never bypass application layer** for Korean data entry
2. **Always specify charset** when using MySQL client directly
3. **Implement automated Korean text validation** in CI/CD pipeline
4. **Regular audits** of Korean text quality across all tables
5. **Document encoding requirements** for all team members

This issue highlights the critical importance of proper UTF-8 handling in international applications and serves as a reminder that database configuration alone is insufficient - client tools must also be properly configured.

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
- Korean encoding issues comprehensively resolved (double UTF-8 encoding fixed)
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
   ```bash
   # CRITICAL: Always use charset flag with MySQL client
   docker exec stockquest-mysql mysql --default-character-set=utf8mb4 -u root -p stockquest
   ```

   **See detailed analysis in "Korean Language Support → Critical Issue" section above**
   - Double UTF-8 encoding causes `ê¸°ìˆ ì£¼` instead of `기술주`
   - Prevention: Use application-level data entry, never direct SQL for Korean text
   - Verification: Check API responses and frontend display after any Korean data changes

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
7. **TEST** Korean text handling in all new features (verify encoding end-to-end)
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