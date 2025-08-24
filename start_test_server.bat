@echo off
echo Starting Zenflow Remote Test Server...
echo.
echo Make sure Python 3.7+ is installed and websockets package is available.
echo If websockets is not installed, run: pip install websockets
echo.

:: Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.7+ from https://python.org
    pause
    exit /b 1
)

:: Try to install websockets if not available
python -c "import websockets" >nul 2>&1
if errorlevel 1 (
    echo Installing websockets package...
    pip install websockets
    if errorlevel 1 (
        echo ERROR: Failed to install websockets
        echo Please run: pip install websockets
        pause
        exit /b 1
    )
)

:: Get local IP address
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        set LOCAL_IP=%%b
        goto :found_ip
    )
)
:found_ip

echo.
echo ========================================
echo   Zenflow Remote Test Server
echo ========================================
echo Server will start on: ws://%LOCAL_IP%:8080
echo.
echo Use this IP in your Android app: %LOCAL_IP%
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

:: Start the server
python test_server.py 8080

pause
