#!/bin/bash

# StockQuest Hexagonal Architecture Validation Script
# This script ensures that Hexagonal Architecture rules are followed

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🏗️ Checking Hexagonal Architecture compliance..."

EXIT_CODE=0

# Function to report violations
report_violation() {
    echo -e "${RED}❌ VIOLATION: $1${NC}"
    EXIT_CODE=1
}

report_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
}

report_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Check if Domain layer has Spring dependencies
echo "📦 Checking Domain Layer dependencies..."

DOMAIN_FILES=$(find backend/src/main/java/com/stockquest/domain -name "*.java" 2>/dev/null || true)

if [ -n "$DOMAIN_FILES" ]; then
    # Check for Spring annotations
    SPRING_ANNOTATIONS=$(grep -l "@\(Service\|Component\|Repository\|Controller\|RestController\|Autowired\|Value\|ConfigurationProperties\)" $DOMAIN_FILES 2>/dev/null || true)
    
    if [ -n "$SPRING_ANNOTATIONS" ]; then
        report_violation "Domain layer contains Spring annotations:"
        echo "$SPRING_ANNOTATIONS" | sed 's/^/  /'
        echo ""
        echo "Domain layer must be framework-agnostic!"
        echo "Move Spring annotations to Application or Adapter layers."
    else
        report_success "Domain layer is free of Spring annotations"
    fi
    
    # Check for Spring imports
    SPRING_IMPORTS=$(grep -l "import org\.springframework\." $DOMAIN_FILES 2>/dev/null || true)
    
    if [ -n "$SPRING_IMPORTS" ]; then
        report_violation "Domain layer contains Spring imports:"
        echo "$SPRING_IMPORTS" | sed 's/^/  /'
        echo ""
        echo "Remove Spring imports from Domain layer!"
    else
        report_success "Domain layer is free of Spring imports"
    fi
    
    # Check for JPA annotations
    JPA_ANNOTATIONS=$(grep -l "@\(Entity\|Table\|Column\|Id\|GeneratedValue\|OneToMany\|ManyToOne\|ManyToMany\|OneToOne\|JoinColumn\)" $DOMAIN_FILES 2>/dev/null || true)
    
    if [ -n "$JPA_ANNOTATIONS" ]; then
        report_violation "Domain layer contains JPA annotations:"
        echo "$JPA_ANNOTATIONS" | sed 's/^/  /'
        echo ""
        echo "JPA annotations belong in the Adapter layer (JpaEntity classes)!"
    else
        report_success "Domain layer is free of JPA annotations"
    fi
    
    # Check for Jakarta EE imports
    JAKARTA_IMPORTS=$(grep -l "import jakarta\." $DOMAIN_FILES 2>/dev/null || true)
    
    if [ -n "$JAKARTA_IMPORTS" ]; then
        report_violation "Domain layer contains Jakarta EE imports:"
        echo "$JAKARTA_IMPORTS" | sed 's/^/  /'
    else
        report_success "Domain layer is free of Jakarta EE imports"
    fi
else
    report_warning "No Domain layer files found"
fi

# Check Application layer structure
echo ""
echo "🔧 Checking Application Layer structure..."

APP_FILES=$(find backend/src/main/java/com/stockquest/application -name "*.java" 2>/dev/null || true)

if [ -n "$APP_FILES" ]; then
    # Application services should implement UseCase interfaces
    SERVICES=$(find backend/src/main/java/com/stockquest/application -name "*Service.java" 2>/dev/null || true)
    
    for service in $SERVICES; do
        if [ -f "$service" ]; then
            USE_CASE_IMPL=$(grep -l "implements.*UseCase" "$service" 2>/dev/null || true)
            if [ -z "$USE_CASE_IMPL" ]; then
                report_warning "Service $service should implement a UseCase interface"
            fi
        fi
    done
    
    # Check for @Transactional on services
    TRANSACTIONAL_SERVICES=$(grep -l "@Transactional" $SERVICES 2>/dev/null || true)
    if [ -n "$TRANSACTIONAL_SERVICES" ]; then
        report_success "Application services use @Transactional"
    else
        report_warning "Application services should use @Transactional"
    fi
else
    report_warning "No Application layer files found"
fi

# Check Adapter layer structure
echo ""
echo "🔌 Checking Adapter Layer structure..."

# Web adapters should be in 'in' package
WEB_CONTROLLERS=$(find backend/src/main/java/com/stockquest/adapter/in/web -name "*Controller.java" 2>/dev/null || true)

if [ -n "$WEB_CONTROLLERS" ]; then
    report_success "Web controllers are in adapter/in/web package"
    
    # Controllers should use UseCase dependencies
    for controller in $WEB_CONTROLLERS; do
        if [ -f "$controller" ]; then
            USE_CASE_DEPS=$(grep -l "UseCase" "$controller" 2>/dev/null || true)
            if [ -z "$USE_CASE_DEPS" ]; then
                report_warning "Controller $controller should depend on UseCase interfaces"
            fi
        fi
    done
else
    report_warning "No web controllers found in adapter/in/web"
fi

# Persistence adapters should be in 'out' package
PERSISTENCE_ADAPTERS=$(find backend/src/main/java/com/stockquest/adapter/out/persistence -name "*Adapter.java" 2>/dev/null || true)

if [ -n "$PERSISTENCE_ADAPTERS" ]; then
    report_success "Persistence adapters are in adapter/out/persistence package"
    
    # Adapters should implement Repository interfaces from Domain
    for adapter in $PERSISTENCE_ADAPTERS; do
        if [ -f "$adapter" ]; then
            REPO_IMPL=$(grep -l "implements.*Repository" "$adapter" 2>/dev/null || true)
            if [ -z "$REPO_IMPL" ]; then
                report_warning "Adapter $adapter should implement a Repository interface"
            fi
        fi
    done
else
    report_warning "No persistence adapters found in adapter/out/persistence"
fi

# Check for reverse dependencies (Adapter -> Domain is OK, Domain -> Adapter is NOT OK)
echo ""
echo "🔄 Checking dependency direction..."

DOMAIN_TO_ADAPTER=$(find backend/src/main/java/com/stockquest/domain -name "*.java" -exec grep -l "\.adapter\." {} \; 2>/dev/null || true)

if [ -n "$DOMAIN_TO_ADAPTER" ]; then
    report_violation "Domain layer depends on Adapter layer:"
    echo "$DOMAIN_TO_ADAPTER" | sed 's/^/  /'
    echo ""
    echo "Domain should not depend on Adapters!"
    echo "Use Port interfaces instead."
else
    report_success "Domain layer doesn't depend on Adapters"
fi

# Check naming conventions
echo ""
echo "📝 Checking naming conventions..."

# Domain entities should not have 'Entity' suffix (that's for JPA entities)
DOMAIN_ENTITIES=$(find backend/src/main/java/com/stockquest/domain -name "*Entity.java" 2>/dev/null || true)

if [ -n "$DOMAIN_ENTITIES" ]; then
    report_warning "Domain classes with 'Entity' suffix found:"
    echo "$DOMAIN_ENTITIES" | sed 's/^/  /'
    echo ""
    echo "Consider removing 'Entity' suffix from Domain classes."
    echo "Reserve 'Entity' suffix for JPA entities in Adapter layer."
fi

# JPA entities should be in persistence adapter
JPA_ENTITIES=$(find backend/src/main/java/com/stockquest/adapter/out/persistence -name "*JpaEntity.java" 2>/dev/null || true)

if [ -n "$JPA_ENTITIES" ]; then
    report_success "JPA entities use proper naming convention"
fi

# Check for proper package structure
echo ""
echo "📁 Checking package structure..."

# Domain should have port sub-packages
DOMAIN_PORTS=$(find backend/src/main/java/com/stockquest/domain -path "*/port/*.java" 2>/dev/null || true)

if [ -n "$DOMAIN_PORTS" ]; then
    report_success "Domain layer has port interfaces"
else
    report_warning "Domain layer should have port interfaces in port/ sub-packages"
fi

# Application should have port/in interfaces  
APP_PORTS=$(find backend/src/main/java/com/stockquest/application -path "*/port/in/*.java" 2>/dev/null || true)

if [ -n "$APP_PORTS" ]; then
    report_success "Application layer has inbound port interfaces"
else
    report_warning "Application layer should have port/in interfaces for UseCases"
fi

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}🎉 All architecture checks passed!${NC}"
else
    echo -e "${RED}💥 Architecture violations found!${NC}"
    echo ""
    echo "📚 Quick fixes:"
    echo "1. Move Spring annotations from Domain to Application/Adapter layers"
    echo "2. Use Port interfaces instead of direct dependencies"
    echo "3. Keep Domain layer framework-agnostic"
    echo "4. Follow hexagonal architecture patterns"
    echo ""
    echo "📖 See .claude/ARCHITECTURE_RULES.md for detailed guidelines"
fi

exit $EXIT_CODE