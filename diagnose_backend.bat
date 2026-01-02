@echo off
REM ========================================
REM BACKEND DIAGNOSTIC SCRIPT
REM ========================================

echo.
echo ========================================
echo CHECKING BACKEND STATUS
echo ========================================
echo.

REM Check 1: Is port 8080 in use?
echo [1/4] Checking if port 8080 is in use...
netstat -ano | findstr :8080
if %ERRORLEVEL% EQU 0 (
    echo ✅ Port 8080 is in use - backend might be running
) else (
    echo ❌ Port 8080 is NOT in use - backend is NOT running!
    echo.
    echo SOLUTION: Start backend with:
    echo    cd backend
    echo    mvn spring-boot:run
    goto :end
)

echo.
echo [2/4] Testing backend health endpoint...
curl -s http://localhost:8080/api/grade-levels >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Backend responded
) else (
    echo ⚠️  Backend didn't respond or returned error
)

echo.
echo [3/4] Testing with verbose output...
curl -v http://localhost:8080/api/grade-levels 2>&1 | findstr "HTTP"

echo.
echo [4/4] Full diagnostic...
echo.
echo Base URL: http://localhost:8080/api
echo.
echo Testing endpoints:
curl -I http://localhost:8080/api/grade-levels 2>&1 | findstr "HTTP"
curl -I http://localhost:8080/api/classes 2>&1 | findstr "HTTP"
curl -I http://localhost:8080/api/subjects 2>&1 | findstr "HTTP"
curl -I http://localhost:8080/api/assignments 2>&1 | findstr "HTTP"

:end
echo.
echo ========================================
echo DIAGNOSIS COMPLETE
echo ========================================
echo.
echo If you see "❌ Port 8080 is NOT in use":
echo   → Backend is NOT running
echo   → Start it with: cd backend ^&^& mvn spring-boot:run
echo.
echo If you see "HTTP/1.1 404":
echo   → Backend is running but endpoints not found
echo   → Restart backend: Ctrl+C then mvn spring-boot:run
echo.
echo If you see "HTTP/1.1 401":
echo   → Backend is working! Just need to login
echo.
echo If you see "HTTP/1.1 200":
echo   → Everything works perfectly!
echo.
pause
