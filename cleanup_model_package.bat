@echo off
REM ========================================
REM CLEANUP SCRIPT - Delete wrong package
REM ========================================

echo ========================================
echo Deleting incorrect 'model' package...
echo ========================================

cd /d "%~dp0"

REM Navigate to backend directory
cd backend\src\main\java\com\schoolmanagement

REM Check if model folder exists
if exist "model" (
    echo Found 'model' folder. Deleting...
    rmdir /s /q model
    echo ✅ Successfully deleted model folder!
) else (
    echo ℹ️  'model' folder not found. Already clean!
)

echo.
echo ========================================
echo Cleanup complete!
echo ========================================
echo.
echo All entities are now correctly in 'entity' package.
echo.
echo Next step: Run 'mvn clean compile' in backend folder
echo.
pause
