#!/bin/bash

# StockQuest Dependency Validation Script
# Ensures Domain layer remains framework-agnostic and follows Hexagonal Architecture

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "📦 Checking dependency compliance..."

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

# Domain layer dependency validation
check_domain_dependencies() {
    echo ""
    echo -e "${BLUE}🏗️ Validating Domain Layer Dependencies${NC}"
    
    # Find all Java files in domain layer
    DOMAIN_FILES=$(find backend/src/main/java/com/stockquest/domain -name "*.java" 2>/dev/null || true)
    
    if [ -z "$DOMAIN_FILES" ]; then
        report_warning "No Domain layer files found"
        return 0
    fi
    
    # Forbidden dependencies in Domain layer
    FORBIDDEN_IMPORTS=(
        "org.springframework"
        "jakarta.persistence"
        "jakarta.validation.constraints"
        "com.fasterxml.jackson"
        "org.hibernate" 
        "javax.persistence"
        "org.slf4j"
        "ch.qos.logback"
        "com.stockquest.adapter"
        "com.stockquest.infrastructure"
    )
    
    # Allowed external dependencies in Domain layer
    ALLOWED_IMPORTS=(
        "java.util"
        "java.time"
        "java.math"
        "java.lang"
        "java.io"
        "java.security"
        "com.stockquest.domain"
    )
    
    echo "  🔍 Checking for forbidden imports..."
    
    VIOLATIONS_FOUND=false
    
    for domain_file in $DOMAIN_FILES; do
        for forbidden in "${FORBIDDEN_IMPORTS[@]}"; do
            if grep -l "import $forbidden" "$domain_file" 2>/dev/null; then
                report_violation "Domain file $domain_file imports forbidden dependency: $forbidden"
                VIOLATIONS_FOUND=true
            fi
        done
    done
    
    if [ "$VIOLATIONS_FOUND" = false ]; then
        report_success "Domain layer has no forbidden dependencies"
    fi
    
    # Check for annotations that shouldn't be in Domain layer
    echo "  🏷️ Checking for forbidden annotations..."
    
    FORBIDDEN_ANNOTATIONS=(
        "@Entity"
        "@Table" 
        "@Column"
        "@Id"
        "@GeneratedValue"
        "@OneToMany"
        "@ManyToOne"
        "@ManyToMany"
        "@OneToOne"
        "@JoinColumn"
        "@Service"
        "@Component"
        "@Repository"
        "@Controller"
        "@RestController"
        "@Autowired"
        "@Value"
        "@ConfigurationProperties"
        "@Transactional"
        "@JsonProperty"
        "@JsonIgnore"
    )
    
    ANNOTATION_VIOLATIONS=false
    
    for domain_file in $DOMAIN_FILES; do
        for annotation in "${FORBIDDEN_ANNOTATIONS[@]}"; do
            if grep -l "$annotation" "$domain_file" 2>/dev/null; then
                report_violation "Domain file $domain_file contains forbidden annotation: $annotation"
                ANNOTATION_VIOLATIONS=true
            fi
        done
    done
    
    if [ "$ANNOTATION_VIOLATIONS" = false ]; then
        report_success "Domain layer has no forbidden annotations"
    fi
}

# Application layer dependency validation
check_application_dependencies() {
    echo ""
    echo -e "${BLUE}🔧 Validating Application Layer Dependencies${NC}"
    
    APP_FILES=$(find backend/src/main/java/com/stockquest/application -name "*.java" 2>/dev/null || true)
    
    if [ -z "$APP_FILES" ]; then
        report_warning "No Application layer files found"
        return 0
    fi
    
    # Application layer should not depend on Adapter layer
    ADAPTER_IMPORTS=false
    
    for app_file in $APP_FILES; do
        if grep -l "import com.stockquest.adapter" "$app_file" 2>/dev/null; then
            report_violation "Application file $app_file imports from Adapter layer"
            ADAPTER_IMPORTS=true
        fi
    done
    
    if [ "$ADAPTER_IMPORTS" = false ]; then
        report_success "Application layer doesn't import from Adapter layer"
    fi
    
    # Check for proper UseCase interface implementations
    SERVICE_FILES=$(find backend/src/main/java/com/stockquest/application -name "*Service.java" 2>/dev/null || true)
    
    if [ -n "$SERVICE_FILES" ]; then
        echo "  🎯 Checking UseCase interface implementations..."
        
        for service in $SERVICE_FILES; do
            if ! grep -l "implements.*UseCase" "$service" 2>/dev/null; then
                report_warning "Service $service should implement a UseCase interface"
            fi
        done
    fi
}

# Adapter layer dependency validation
check_adapter_dependencies() {
    echo ""
    echo -e "${BLUE}🔌 Validating Adapter Layer Dependencies${NC}"
    
    # Web adapters should only depend on Application layer, not Domain directly
    WEB_CONTROLLERS=$(find backend/src/main/java/com/stockquest/adapter/in/web -name "*Controller.java" 2>/dev/null || true)
    
    if [ -n "$WEB_CONTROLLERS" ]; then
        echo "  🌐 Checking Web Controller dependencies..."
        
        for controller in $WEB_CONTROLLERS; do
            # Controllers should depend on UseCase interfaces, not services directly
            if grep -l "import com.stockquest.application.*Service" "$controller" 2>/dev/null; then
                report_warning "Controller $controller imports Service classes directly. Use UseCase interfaces instead."
            fi
            
            # Controllers should not import Domain entities directly
            if grep -l "import com.stockquest.domain.*" "$controller" 2>/dev/null; then
                DOMAIN_IMPORTS=$(grep "import com.stockquest.domain" "$controller" | grep -v "port")
                if [ -n "$DOMAIN_IMPORTS" ]; then
                    report_warning "Controller $controller imports Domain entities. Use DTOs instead."
                fi
            fi
        done
    fi
    
    # Persistence adapters validation
    PERSISTENCE_ADAPTERS=$(find backend/src/main/java/com/stockquest/adapter/out/persistence -name "*Adapter.java" 2>/dev/null || true)
    
    if [ -n "$PERSISTENCE_ADAPTERS" ]; then
        echo "  💾 Checking Persistence Adapter dependencies..."
        
        for adapter in $PERSISTENCE_ADAPTERS; do
            # Adapters should implement Repository interfaces from Domain
            if ! grep -l "implements.*Repository" "$adapter" 2>/dev/null; then
                report_warning "Adapter $adapter should implement a Domain Repository interface"
            fi
        done
    fi
}

# Check for circular dependencies
check_circular_dependencies() {
    echo ""
    echo -e "${BLUE}🔄 Checking for Circular Dependencies${NC}"
    
    # Domain → Application (NOT ALLOWED)
    DOMAIN_TO_APP=$(find backend/src/main/java/com/stockquest/domain -name "*.java" -exec grep -l "import com.stockquest.application" {} \; 2>/dev/null || true)
    
    if [ -n "$DOMAIN_TO_APP" ]; then
        report_violation "Domain layer depends on Application layer:"
        echo "$DOMAIN_TO_APP" | sed 's/^/  /'
    else
        report_success "No Domain → Application dependencies found"
    fi
    
    # Domain → Adapter (NOT ALLOWED)  
    DOMAIN_TO_ADAPTER=$(find backend/src/main/java/com/stockquest/domain -name "*.java" -exec grep -l "import com.stockquest.adapter" {} \; 2>/dev/null || true)
    
    if [ -n "$DOMAIN_TO_ADAPTER" ]; then
        report_violation "Domain layer depends on Adapter layer:"
        echo "$DOMAIN_TO_ADAPTER" | sed 's/^/  /'
    else
        report_success "No Domain → Adapter dependencies found"
    fi
    
    # Application → Adapter (NOT ALLOWED)
    APP_TO_ADAPTER=$(find backend/src/main/java/com/stockquest/application -name "*.java" -exec grep -l "import com.stockquest.adapter" {} \; 2>/dev/null || true)
    
    if [ -n "$APP_TO_ADAPTER" ]; then
        report_violation "Application layer depends on Adapter layer:"
        echo "$APP_TO_ADAPTER" | sed 's/^/  /'
    else
        report_success "No Application → Adapter dependencies found"
    fi
}

# Check package structure compliance
check_package_structure() {
    echo ""
    echo -e "${BLUE}📁 Validating Package Structure${NC}"
    
    # Domain should have port sub-packages
    DOMAIN_PORTS=$(find backend/src/main/java/com/stockquest/domain -path "*/port/*.java" 2>/dev/null || true)
    
    if [ -n "$DOMAIN_PORTS" ]; then
        report_success "Domain layer has port interfaces"
    else
        report_warning "Domain layer should have port/ sub-packages for interfaces"
    fi
    
    # Application should have port/in interfaces  
    APP_IN_PORTS=$(find backend/src/main/java/com/stockquest/application -path "*/port/in/*.java" 2>/dev/null || true)
    
    if [ -n "$APP_IN_PORTS" ]; then
        report_success "Application layer has inbound port interfaces"
    else
        report_warning "Application layer should have port/in/ interfaces for UseCases"
    fi
    
    # Adapters should be properly separated
    WEB_ADAPTERS=$(find backend/src/main/java/com/stockquest/adapter/in -name "*.java" 2>/dev/null || true)
    PERSISTENCE_ADAPTERS=$(find backend/src/main/java/com/stockquest/adapter/out -name "*.java" 2>/dev/null || true)
    
    if [ -n "$WEB_ADAPTERS" ]; then
        report_success "Inbound adapters properly placed in adapter/in/"
    fi
    
    if [ -n "$PERSISTENCE_ADAPTERS" ]; then
        report_success "Outbound adapters properly placed in adapter/out/"
    fi
}

# Frontend dependency checks (basic validation)
check_frontend_dependencies() {
    echo ""
    echo -e "${BLUE}🎨 Validating Frontend Dependencies${NC}"
    
    if [ ! -f "frontend/package.json" ]; then
        report_warning "Frontend package.json not found"
        return 0
    fi
    
    # Check for outdated or vulnerable dependencies
    if command -v npm >/dev/null 2>&1; then
        echo "  🔍 Checking for npm security vulnerabilities..."
        cd frontend
        if npm audit --audit-level=high >/dev/null 2>&1; then
            report_success "No high-severity npm vulnerabilities found"
        else
            report_warning "High-severity npm vulnerabilities detected. Run 'npm audit' to see details."
        fi
        cd - >/dev/null
    fi
    
    # Check for proper layer separation in frontend imports
    FEATURE_FILES=$(find frontend/src -name "*.ts" -o -name "*.tsx" 2>/dev/null | grep -E "(features|pages)" || true)
    
    if [ -n "$FEATURE_FILES" ]; then
        echo "  🏗️ Checking Feature-Sliced Design compliance..."
        
        for feature_file in $FEATURE_FILES; do
            # Features should not import from other features directly
            if grep -l "from.*features/.*/.*" "$feature_file" 2>/dev/null; then
                report_warning "Feature file $feature_file imports from other features. Use shared layer."
            fi
        done
    fi
}

# Main execution
echo "🔍 StockQuest Dependency Validation"
echo "=================================="

check_domain_dependencies
check_application_dependencies  
check_adapter_dependencies
check_circular_dependencies
check_package_structure
check_frontend_dependencies

echo ""
echo "📊 Dependency Check Summary:"
echo "  🏗️ Domain Layer: Must be framework-agnostic"
echo "  🔧 Application Layer: Can use Spring framework"
echo "  🔌 Adapter Layer: Can use any external frameworks"
echo "  🎨 Frontend Layer: Follow Feature-Sliced Design"

if [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 All dependency checks passed!${NC}"
    echo ""
    echo "💡 Hexagonal Architecture Guidelines:"
    echo "  • Domain layer: Pure business logic, no frameworks"
    echo "  • Application layer: Use cases, can use @Transactional"  
    echo "  • Adapter layer: Framework integrations, Spring annotations OK"
    echo "  • Dependencies flow: Adapter → Application → Domain"
else
    echo ""
    echo -e "${RED}💥 Dependency violations found!${NC}"
    echo ""
    echo "🔧 Quick fixes:"
    echo "  1. Move Spring annotations from Domain to Application/Adapter"
    echo "  2. Use Port interfaces instead of direct dependencies"
    echo "  3. Keep Domain layer framework-agnostic"
    echo "  4. Follow dependency inversion principle"
    echo ""
    echo "📖 See .claude/ARCHITECTURE_RULES.md for detailed guidelines"
fi

exit $EXIT_CODE