@echo off
echo =========================================
echo UZIMA Borehole System Launcher
echo =========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install Java 11 or higher from:
    echo https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

echo Starting UZIMA Borehole System...
echo.

REM Run the application
java -jar target\uzima-borehole-system-1.0.0.jar

if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start!
    echo Please ensure:
    echo 1. MySQL database is running
    echo 2. Database is properly set up
    echo 3. All dependencies are installed
    echo.
)

pause
