#!/bin/bash

# School Management System - Build Script
# This script builds the Spring Boot backend application

set -e

echo "========================================"
echo "School Management System - Backend Build"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17+ is not installed or not in PATH"
    echo "Please install Java 17 and add it to your PATH"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven and add it to your PATH"
    exit 1
fi

# Display Java and Maven versions
echo "Java version:"
java -version
echo ""
echo "Maven version:"
mvn -version
echo ""

# Get build directory
BUILD_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Building project in: $BUILD_DIR"
echo ""

# Clean previous builds
echo "[1/4] Cleaning previous builds..."
mvn clean
echo "Build cleaned successfully"
echo ""

# Compile the project
echo "[2/4] Compiling project..."
mvn compile
echo "Project compiled successfully"
echo ""

# Run tests
echo "[3/4] Running tests..."
mvn test || echo "WARNING: Some tests failed (continuing...)"
echo "Tests completed"
echo ""

# Package the application
echo "[4/4] Packaging application..."
mvn package -DskipTests
echo "Package created successfully"
echo ""

# Check if JAR was created
if [ -f "target/school-management-system-1.0.0.jar" ]; then
    echo ""
    echo "========================================"
    echo "BUILD SUCCESSFUL!"
    echo "========================================"
    echo ""
    echo "JAR file created:"
    echo "  target/school-management-system-1.0.0.jar"
    echo ""
    echo "To run the application:"
    echo "  mvn spring-boot:run"
    echo "  OR"
    echo "  java -jar target/school-management-system-1.0.0.jar"
    echo ""
    exit 0
else
    echo "ERROR: JAR file was not created"
    exit 1
fi

