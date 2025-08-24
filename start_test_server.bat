@echo off
echo Installing websockets package...
pip install websockets

echo.
echo Starting Zenflow Test Server...
echo Connect your Android app to: ws://localhost:8080
echo.

python test_server.py

pause
