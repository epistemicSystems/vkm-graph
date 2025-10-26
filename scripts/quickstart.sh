#!/bin/bash
# VKM Graph Quick Start
# Automated setup and launch of the complete system

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}"
echo "╔═══════════════════════════════════════╗"
echo "║   VKM Graph Quick Start              ║"
echo "║   Knowledge Evolution System         ║"
echo "╚═══════════════════════════════════════╝"
echo -e "${NC}"

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

# Check Clojure
if ! command -v clj &> /dev/null; then
    echo -e "${RED}✗ Clojure not found${NC}"
    echo "Install from: https://clojure.org/guides/install_clojure"
    exit 1
fi
echo -e "${GREEN}✓ Clojure installed${NC}"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}✗ Node.js not found${NC}"
    echo "Install from: https://nodejs.org/"
    exit 1
fi
echo -e "${GREEN}✓ Node.js installed${NC}"

# Check API keys
if [ -z "$CLAUDE_API_KEY" ]; then
    echo -e "${YELLOW}⚠ CLAUDE_API_KEY not set${NC}"
    echo "  Set it with: export CLAUDE_API_KEY=sk-ant-..."
    echo "  Or add to .env file"
fi

if [ -z "$OPENAI_API_KEY" ]; then
    echo -e "${YELLOW}⚠ OPENAI_API_KEY not set${NC}"
    echo "  Set it with: export OPENAI_API_KEY=sk-..."
    echo "  Or add to .env file"
fi

# Create .env if it doesn't exist
if [ ! -f "$PROJECT_ROOT/.env" ]; then
    echo -e "\n${YELLOW}Creating .env file...${NC}"
    cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
    echo -e "${GREEN}✓ Created .env (please edit with your API keys)${NC}"
fi

# Install frontend dependencies
if [ ! -d "$PROJECT_ROOT/viz/node_modules" ]; then
    echo -e "\n${YELLOW}Installing frontend dependencies...${NC}"
    cd "$PROJECT_ROOT/viz"
    npm install
    cd "$PROJECT_ROOT"
    echo -e "${GREEN}✓ Frontend dependencies installed${NC}"
fi

# Generate test data
echo -e "\n${YELLOW}Generating test data...${NC}"
bash "$SCRIPT_DIR/generate-test-data.sh"

# Function to check if port is in use
port_in_use() {
    lsof -i:"$1" >/dev/null 2>&1
}

# Check ports
if port_in_use 3000; then
    echo -e "${YELLOW}⚠ Port 3000 already in use (backend)${NC}"
    read -p "Kill existing process? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        lsof -ti:3000 | xargs kill -9
        echo -e "${GREEN}✓ Port 3000 freed${NC}"
    fi
fi

if port_in_use 5173; then
    echo -e "${YELLOW}⚠ Port 5173 already in use (frontend)${NC}"
    read -p "Kill existing process? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        lsof -ti:5173 | xargs kill -9
        echo -e "${GREEN}✓ Port 5173 freed${NC}"
    fi
fi

# Start backend
echo -e "\n${BLUE}Starting backend...${NC}"
cd "$PROJECT_ROOT/core"
nohup clj -M:server > /tmp/vkm-backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓ Backend started (PID: $BACKEND_PID)${NC}"
echo "  Logs: tail -f /tmp/vkm-backend.log"

# Wait for backend to be ready
echo -e "\n${YELLOW}Waiting for backend...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:3000/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend ready!${NC}"
        break
    fi
    sleep 1
    echo -n "."
done

# Start frontend
echo -e "\n${BLUE}Starting frontend...${NC}"
cd "$PROJECT_ROOT/viz"
nohup npm run dev > /tmp/vkm-frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✓ Frontend started (PID: $FRONTEND_PID)${NC}"
echo "  Logs: tail -f /tmp/vkm-frontend.log"

# Wait for frontend
sleep 3

echo -e "\n${GREEN}"
echo "╔═══════════════════════════════════════╗"
echo "║   🚀 VKM Graph is running!           ║"
echo "╚═══════════════════════════════════════╝"
echo -e "${NC}"

echo -e "\n${BLUE}Access points:${NC}"
echo "  Frontend:  http://localhost:5173"
echo "  Backend:   http://localhost:3000"
echo "  Health:    http://localhost:3000/health"

echo -e "\n${BLUE}Process IDs:${NC}"
echo "  Backend:   $BACKEND_PID"
echo "  Frontend:  $FRONTEND_PID"

echo -e "\n${BLUE}Stop the system:${NC}"
echo "  kill $BACKEND_PID $FRONTEND_PID"
echo "  Or: pkill -f 'clj.*server' && pkill -f 'vite'"

echo -e "\n${BLUE}Test data:${NC}"
echo "  Upload files from: data/test-documents/"

echo -e "\n${GREEN}Opening browser...${NC}"
sleep 2

# Open browser (cross-platform)
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:5173
elif command -v open &> /dev/null; then
    open http://localhost:5173
elif command -v start &> /dev/null; then
    start http://localhost:5173
fi

echo -e "\n${GREEN}✓ Setup complete! Happy knowledge graphing! 🎉${NC}\n"
