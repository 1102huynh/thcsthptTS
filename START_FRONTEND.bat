@echo off
echo ========================================
echo Starting School Management System Frontend
echo ========================================
echo.
cd /d D:\learn\thcsthptTS\frontend
echo Current directory: %CD%
echo.
echo Starting frontend server...
echo Login credentials: admin / admin123
echo.
echo Opening browser at http://localhost:3000
echo.
start http://localhost:3000/login
npm start

