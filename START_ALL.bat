e@echo off
echo ================================================================
echo    SCHOOL MANAGEMENT SYSTEM - COMPLETE STARTUP
echo ================================================================
echo.
echo This will start both Backend and Frontend servers.
echo.
echo Login Credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo ================================================================
pause
echo.
echo Starting Backend Server...
cd /d D:\learn\thcsthptTS\backend
start "Backend Server" cmd /k "echo Backend Server && java -jar target/school-management-system-1.0.0.jar"
echo Backend server starting in new window...
echo Waiting 30 seconds for backend to initialize...
timeout /t 30 /nobreak
echo.
echo Starting Frontend Server...
cd /d D:\learn\thcsthptTS\frontend
start "Frontend Server" cmd /k "echo Frontend Server && npm start"
echo Frontend server starting in new window...
echo.
echo ================================================================
echo Both servers are starting!
echo.
echo Backend will be available at: http://localhost:8080
echo Frontend will be available at: http://localhost:3000
echo H2 Console: http://localhost:8080/h2-console
echo.
echo Login at: http://localhost:3000/login
echo   Username: admin
echo   Password: admin123
echo ================================================================
timeout /t 5
start http://localhost:3000/login

