# CLAUDE.md - Backend Module

This file provides guidance to Claude Code when working with the Stock Quest backend module.

## Module Overview
Spring Boot backend application following Hexagonal Architecture with domain-driven design.

## Critical Build Configuration
**Java Version**: Java 21 (Temurin)
**JAVA_HOME**: `/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home`

## Build Commands
```bash
# Compile
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava

# Build (without tests)
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew build -x test

# Run application
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew bootRun

# Run tests
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew test

# Database migration
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew flywayMigrate
```

## Architecture Structure

### Hexagonal Architecture Layers
```
src/main/java/com/stockquest/
├── domain/                 # Core business logic (NO Spring dependencies)
│   ├── challenge/          # Challenge domain entities
│   ├── content/           # Article/Category content domain
│   ├── market/            # Market data domain
│   ├── portfolio/         # Portfolio management domain
│   ├── session/           # Challenge session domain
│   └── user/              # User domain
├── application/           # Use cases and orchestration
│   ├── challenge/         # Challenge use cases
│   ├── content/          # Content management use cases
│   └── port/             # Port interfaces (in/out)
└── adapter/              # External integrations
    ├── in/               # Inbound adapters
    │   └── web/          # REST controllers
    └── out/              # Outbound adapters
        └── persistence/  # JPA repositories
```

## Current Issues to Fix

### Missing JPA Entities and Repositories
The following files need to be created to fix compilation errors:

1. **ArticleJpaEntity.java** - JPA entity for Article domain model
2. **ArticleJpaRepository.java** - Spring Data JPA repository for Article
3. **CategoryJpaEntity.java** - JPA entity for Category domain model
4. **CategoryJpaRepository.java** - Spring Data JPA repository for Category

Location: `src/main/java/com/stockquest/adapter/out/persistence/entity/` (for entities)
Location: `src/main/java/com/stockquest/adapter/out/persistence/repository/` (for repositories)

## Development Guidelines

### Domain Layer Rules
- NO Spring Framework dependencies in domain layer
- Use pure Java/Kotlin for domain models
- Domain services should be framework-agnostic
- Port interfaces define contracts for adapters

### Testing Strategy
- Unit tests for domain logic (no Spring context)
- Integration tests for adapters
- E2E tests for complete workflows
- Use @DataJpaTest for repository tests
- Use @WebMvcTest for controller tests

### Database Conventions
- Table names: snake_case (e.g., `challenge_session`)
- Column names: snake_case (e.g., `created_at`)
- Use JPA annotations for mapping
- Always include audit columns (created_at, updated_at)

### API Conventions
- RESTful endpoints following OpenAPI 3.0 spec
- Request/Response DTOs in adapter layer
- Consistent error response format
- JWT authentication for protected endpoints

## Common Pitfalls to Avoid
1. Don't add Spring dependencies to domain layer
2. Don't skip compilation check before committing
3. Don't use mock data in production code paths
4. Don't forget to handle null cases in JPA mappings
5. Always use UTF-8 encoding for Korean text support

## Quick Debugging
```bash
# Check for compilation errors
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew compileJava --console=plain

# Clean build
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew clean

# Check dependency tree
JAVA_HOME=/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home ./gradlew dependencies
```