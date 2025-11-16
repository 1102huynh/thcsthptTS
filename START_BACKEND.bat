@echo off
echo ========================================
echo Starting School Management System Backend
echo ========================================
echo.
cd /d D:\learn\thcsthptTS\backend
echo Current directory: %CD%
echo.
echo Checking if port 8080 is free...
netstat -ano | findstr ":8080" >nul
if %errorlevel% equ 0 (
    echo WARNING: Port 8080 is already in use!
    echo Please close the application using port 8080 first.
    pause
    exit /b 1
)
echo Port 8080 is free.
echo.
echo Starting backend server...
echo Login credentials: admin / admin123
echo.
echo Please wait for "Started SchoolManagementApplication" message...
echo.
java -jar target/school-management-system-1.0.0.jar

