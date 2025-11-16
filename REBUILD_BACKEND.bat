@echo off
echo ========================================
echo Rebuilding Backend with MySQL Fix
echo ========================================
echo.
cd /d D:\learn\thcsthptTS\backend
echo Cleaning and rebuilding...
mvn clean package -DskipTests
echo.
if %errorlevel% equ 0 (
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    echo The backend has been rebuilt with MySQL-compatible data.sql
    echo.
    echo Next steps:
    echo 1. Start backend: START_BACKEND.bat
    echo 2. Login with: admin / admin123
    echo.
) else (
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Please check the error messages above.
    echo.
)
pause

