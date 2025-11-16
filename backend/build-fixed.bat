@echo off
REM School Management System - Maven Build with Central Repository Fix
REM This script overrides the corporate Maven configuration to use Maven Central

echo ========================================
echo School Management System - Maven Build
echo ========================================
echo.

REM Navigate to backend directory
cd /d D:\learn\thcsthptTS\backend

echo Clearing Maven cache of corporate artifacts...
if exist "%USERPROFILE%\.m2\repository\xifin" (
    rmdir /s /q "%USERPROFILE%\.m2\repository\xifin" 2>nul
    echo Cleared xifin artifacts
)

echo.
echo Building with Maven Central Repository...
echo Command: mvn clean install -DskipTests
echo.

REM Run Maven with explicit repository settings
REM Using -Dmaven.repo.local to use a clean repository
REM Using -Dresolve.with=true to ignore cached settings

mvn ^
    -s settings.xml ^
    -Dorg.slf4j.simpleLogger.defaultLogLevel=info ^
    clean install ^
    -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo To run the application:
    echo   mvn -s settings.xml spring-boot:run
    echo.
    echo Or:
    echo   java -jar target\school-management-system-1.0.0.jar
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED
    echo ========================================
    echo Error code: %ERRORLEVEL%
    echo.
)

pause

