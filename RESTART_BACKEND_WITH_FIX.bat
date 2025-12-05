@echo off
echo ========================================
echo RESTARTING BACKEND WITH NEW SECURITY CONFIG
echo ========================================
echo.

cd /d "%~dp0backend"

echo Step 1: Stopping any running backend process...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq *spring-boot*" 2>nul
timeout /t 2 /nobreak >nul

echo.
echo Step 2: Cleaning previous build...
call mvnw clean

echo.
echo Step 3: Compiling with new SecurityConfig...
call mvnw compile

echo.
echo Step 4: Starting backend...
echo.
echo Backend will start now. Watch for:
echo   "Started SchoolManagementApplication"
echo.
echo Then test with: curl http://localhost:8080/api/news
echo.

call mvnw spring-boot:run

pause

