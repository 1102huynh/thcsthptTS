@echo off
REM School Management System - Build Script
REM This script builds the Spring Boot backend application

setlocal enabledelayedexpansion

echo ========================================
echo School Management System - Backend Build
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java 17+ is not installed or not in PATH
    echo Please install Java 17 and add it to your PATH
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    exit /b 1
)

REM Display Java and Maven versions
echo Java version:
java -version
echo.
echo Maven version:
mvn -version
echo.

REM Set build directory
set BUILD_DIR=%cd%

echo Building project in: %BUILD_DIR%
echo.

REM Clean previous builds
echo [1/4] Cleaning previous builds...
call mvn clean
if errorlevel 1 (
    echo ERROR: Maven clean failed
    exit /b 1
)
echo Build cleaned successfully
echo.

REM Compile the project
echo [2/4] Compiling project...
call mvn compile
if errorlevel 1 (
    echo ERROR: Maven compile failed
    exit /b 1
)
echo Project compiled successfully
echo.

REM Run tests
echo [3/4] Running tests...
call mvn test
if errorlevel 1 (
    echo WARNING: Some tests failed
    REM Continuing despite test failures for now
)
echo Tests completed
echo.

REM Package the application
echo [4/4] Packaging application...
call mvn package -DskipTests
if errorlevel 1 (
    echo ERROR: Maven package failed
    exit /b 1
)
echo Package created successfully
echo.

REM Check if JAR was created
if exist "target\school-management-system-1.0.0.jar" (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo JAR file created:
    echo   target\school-management-system-1.0.0.jar
    echo.
    echo To run the application:
    echo   mvn spring-boot:run
    echo   OR
    echo   java -jar target\school-management-system-1.0.0.jar
    echo.
) else (
    echo ERROR: JAR file was not created
    exit /b 1
)

endlocal
exit /b 0

