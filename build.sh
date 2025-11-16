#!/bin/bash

# ========================================
# School Management System - Build Script
# Linux/Mac Bash Script
# ========================================

set -e

echo ""
echo "========== School Management System Build =========="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/"
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "WARNING: Node.js is not installed. Skipping frontend build."
else
    echo "Node.js version: $(node --version)"
fi

echo ""
echo "[1] Build Backend Only (with tests)"
echo "[2] Build Backend (skip tests)"
echo "[3] Build Frontend Only"
echo "[4] Build Both Backend and Frontend"
echo "[5] Run Backend (mvn spring-boot:run)"
echo "[6] Clean All Build Artifacts"
echo "[7] Full Clean Build (Backend + Frontend)"
echo ""

read -p "Enter your choice (1-7): " choice

case $choice in
    1)
        echo ""
        echo "Building Backend with tests..."
        cd backend
        mvn clean install
        cd ..
        echo "Build complete!"
        ;;
    2)
        echo ""
        echo "Building Backend (skipping tests)..."
        cd backend
        mvn clean install -DskipTests
        cd ..
        echo "Build complete!"
        ;;
    3)
        echo ""
        echo "Building Frontend..."
        cd frontend
        npm install
        npm run build
        cd ..
        echo "Build complete!"
        ;;
    4)
        echo ""
        echo "Building Backend..."
        cd backend
        mvn clean install -DskipTests
        cd ..

        echo ""
        echo "Building Frontend..."
        cd frontend
        npm install
        npm run build
        cd ..

        echo "All builds complete!"
        ;;
    5)
        echo ""
        echo "Starting Backend (mvn spring-boot:run)..."
        echo "Press Ctrl+C to stop"
        cd backend
        mvn spring-boot:run
        cd ..
        ;;
    6)
        echo ""
        echo "Cleaning all build artifacts..."
        cd backend
        mvn clean
        cd ..

        cd frontend
        rm -rf node_modules build
        cd ..

        echo "Cleanup complete!"
        ;;
    7)
        echo ""
        echo "Full clean build..."

        echo "Cleaning previous builds..."
        cd backend
        mvn clean
        cd ..

        cd frontend
        rm -rf node_modules build
        cd ..

        echo "Building Backend..."
        cd backend
        mvn clean install -DskipTests
        cd ..

        echo "Building Frontend..."
        cd frontend
        npm install
        npm run build
        cd ..

        echo "Full build complete!"
        ;;
    *)
        echo "Invalid choice!"
        exit 1
        ;;
esac

echo ""
echo "========== Build Process Finished =========="
echo ""

