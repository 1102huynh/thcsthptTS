@echo off
REM ========================================
REM School Management System - Build Script
REM Windows Batch Script
REM ========================================

setlocal enabledelayedexpansion

echo.
echo ========== School Management System Build ==========
echo.

REM Check if Maven is installed
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    exit /b 1
)

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo WARNING: Node.js is not installed. Skipping frontend build.
) else (
    echo Node.js version:
    node --version
)

echo.
echo [1] Build Backend Only (with tests)
echo [2] Build Backend (skip tests)
echo [3] Build Frontend Only
echo [4] Build Both Backend and Frontend
echo [5] Run Backend (mvn spring-boot:run)
echo [6] Clean All Build Artifacts
echo [7] Full Clean Build (Backend + Frontend)
echo.

set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" (
    echo.
    echo Building Backend with tests...
    cd backend
    mvn clean install
    cd ..
    echo Build complete!
) else if "%choice%"=="2" (
    echo.
    echo Building Backend (skipping tests)...
    cd backend
    mvn clean install -DskipTests
    cd ..
    echo Build complete!
) else if "%choice%"=="3" (
    echo.
    echo Building Frontend...
    cd frontend
    call npm install
    call npm run build
    cd ..
    echo Build complete!
) else if "%choice%"=="4" (
    echo.
    echo Building Backend...
    cd backend
    mvn clean install -DskipTests
    cd ..

    echo.
    echo Building Frontend...
    cd frontend
    call npm install
    call npm run build
    cd ..

    echo All builds complete!
) else if "%choice%"=="5" (
    echo.
    echo Starting Backend (mvn spring-boot:run)...
    echo Press Ctrl+C to stop
    cd backend
    mvn spring-boot:run
    cd ..
) else if "%choice%"=="6" (
    echo.
    echo Cleaning all build artifacts...
    cd backend
    mvn clean
    cd ..

    cd frontend
    if exist node_modules rmdir /s /q node_modules
    if exist build rmdir /s /q build
    cd ..

    echo Cleanup complete!
) else if "%choice%"=="7" (
    echo.
    echo Full clean build...

    echo Cleaning previous builds...
    cd backend
    mvn clean
    cd ..

    cd frontend
    if exist node_modules rmdir /s /q node_modules
    if exist build rmdir /s /q build
    cd ..

    echo Building Backend...
    cd backend
    mvn clean install -DskipTests
    cd ..

    echo Building Frontend...
    cd frontend
    call npm install
    call npm run build
    cd ..

    echo Full build complete!
) else (
    echo Invalid choice!
    exit /b 1
)

echo.
echo ========== Build Process Finished ==========
echo.
pause

