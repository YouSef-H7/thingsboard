#!/bin/bash
#
# ci-check.sh – Local CI Simulation Script
# Simulates a CI pipeline locally without GitHub Actions or cloud services
# This script validates build, tests, and Docker configuration
#

set -e  # Fail immediately on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'  # No Color

# Banner
echo ""
echo "=========================================="
echo "    LOCAL CI PIPELINE SIMULATION"
echo "=========================================="
echo ""
echo "Starting CI checks at $(date)"
echo ""

# Function to print section headers
print_section() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}▶ $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# Function to handle errors
handle_error() {
    echo ""
    echo -e "${RED}❌ CI Pipeline FAILED at step: $1${NC}"
    echo -e "${RED}Exit code: $2${NC}"
    echo ""
    echo "=========================================="
    echo "    PIPELINE STATUS: FAILED"
    echo "=========================================="
    exit 1
}

# STEP 1: Run Unit Tests
print_section "STEP 1: Running Unit Tests"
echo "Command: mvn clean test"
echo ""
if mvn clean test; then
    echo ""
    echo -e "${GREEN}✓ Unit tests passed${NC}"
else
    handle_error "mvn clean test" $?
fi

# STEP 2: Build Project (Backend + Frontend)
print_section "STEP 2: Building Project (Backend + Frontend)"
echo "Command: mvn clean install -DskipTests -Dlicense.skip=true"
echo ""
if mvn clean install -DskipTests -Dlicense.skip=true 2>&1 | tail -20; then
    echo ""
    echo -e "${GREEN}✓ Project build successful${NC}"
else
    handle_error "mvn clean install" $?
fi

# STEP 3: Validate Docker Configuration
print_section "STEP 3: Validating Docker Configuration"
echo "Command: docker-compose config"
echo ""
if docker-compose config > /dev/null 2>&1; then
    echo -e "${GREEN}✓ docker-compose.yml is valid${NC}"
    docker-compose config | head -5
    echo "..."
else
    handle_error "docker-compose config" $?
fi

# Success banner
echo ""
echo "=========================================="
echo -e "    ${GREEN}✓ PIPELINE STATUS: PASSED${NC}"
echo "=========================================="
echo ""
echo "All CI checks completed successfully:"
echo "  ✓ Unit tests passed"
echo "  ✓ Project built successfully"
echo "  ✓ Docker configuration valid"
echo ""
echo "Completed at $(date)"
echo ""
