#!/bin/bash

# StockQuest Automated Monitoring Script
# Ensures Spring Boot application is always running and healthy

export JAVA_HOME="/Users/seongmin/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"
LOG_FILE="$SCRIPT_DIR/monitoring.log"
PID_FILE="$SCRIPT_DIR/stockquest.pid"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

check_health() {
    # Check if application is responding on port 8080
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        return 0  # Healthy
    else
        return 1  # Unhealthy
    fi
}

is_running() {
    if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        return 0  # Running
    else
        return 1  # Not running
    fi
}

start_application() {
    log "🚀 Starting Spring Boot application..."
    cd "$BACKEND_DIR" || exit 1
    
    nohup $JAVA_HOME/bin/java -jar build/libs/*.jar \
        --spring.profiles.active=dev \
        --server.port=8080 \
        > "$SCRIPT_DIR/app.log" 2>&1 &
    
    echo $! > "$PID_FILE"
    log "📋 Application started with PID: $(cat "$PID_FILE")"
}

stop_application() {
    if [ -f "$PID_FILE" ]; then
        local PID=$(cat "$PID_FILE")
        log "🛑 Stopping application (PID: $PID)..."
        kill "$PID" 2>/dev/null || true
        rm -f "$PID_FILE"
        sleep 3
        
        # Force kill if still running
        if kill -0 "$PID" 2>/dev/null; then
            log "⚠️  Force killing application..."
            kill -9 "$PID" 2>/dev/null || true
        fi
        
        # Clean up port
        lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    fi
}

build_application() {
    log "🔨 Building application..."
    cd "$BACKEND_DIR" || exit 1
    ./gradlew clean build -x test --no-daemon
    if [ $? -eq 0 ]; then
        log "✅ Build successful"
        return 0
    else
        log "❌ Build failed"
        return 1
    fi
}

monitor_loop() {
    local consecutive_failures=0
    local max_failures=3
    
    while true; do
        if is_running && check_health; then
            echo -ne "\r$(date '+%H:%M:%S') - ${GREEN}✅ Application healthy${NC}   "
            consecutive_failures=0
        else
            consecutive_failures=$((consecutive_failures + 1))
            log "⚠️  Health check failed (attempt $consecutive_failures/$max_failures)"
            
            if [ $consecutive_failures -ge $max_failures ]; then
                log "🔄 Restarting application due to health check failures..."
                restart_application
                consecutive_failures=0
            fi
        fi
        
        sleep 10
    done
}

restart_application() {
    stop_application
    sleep 2
    if build_application; then
        start_application
        
        # Wait for application to be ready (up to 60 seconds)
        local wait_time=0
        while [ $wait_time -lt 60 ]; do
            if check_health; then
                log "✅ Application restarted successfully"
                return 0
            fi
            sleep 2
            wait_time=$((wait_time + 2))
        done
        
        log "❌ Application failed to start properly"
        return 1
    else
        log "❌ Build failed, cannot restart"
        return 1
    fi
}

case "$1" in
    start)
        if is_running; then
            log "⚠️  Application is already running"
        else
            if build_application; then
                start_application
            fi
        fi
        ;;
    stop)
        stop_application
        log "🛑 Application stopped"
        ;;
    restart)
        restart_application
        ;;
    status)
        if is_running && check_health; then
            echo -e "${GREEN}✅ Application is running and healthy${NC}"
            exit 0
        elif is_running; then
            echo -e "${YELLOW}⚠️  Application is running but unhealthy${NC}"
            exit 1
        else
            echo -e "${RED}❌ Application is not running${NC}"
            exit 1
        fi
        ;;
    monitor)
        log "🔍 Starting continuous monitoring..."
        monitor_loop
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|monitor}"
        echo
        echo "  start     - Build and start the application"
        echo "  stop      - Stop the application"
        echo "  restart   - Stop, rebuild, and start the application"
        echo "  status    - Check if application is running and healthy"
        echo "  monitor   - Continuously monitor and auto-restart if needed"
        exit 1
        ;;
esac