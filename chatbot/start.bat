@echo off
REM Globex Chatbot API Startup Script for Windows

echo.
echo ========================================
echo  Globex Chatbot API - Database Edition
echo ========================================
echo.

REM Check if virtual environment exists
if not exist "venv\Scripts\activate.bat" (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate virtual environment
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install/Update dependencies
echo Installing dependencies...
pip install -r requirements.txt -q

REM Start the API
echo.
echo ========================================
echo  Starting Globex Chatbot API...
echo ========================================
echo.
echo API Documentation: http://localhost:8000/docs
echo Health Check: http://localhost:8000/health
echo Swagger UI: http://localhost:8000/redoc
echo.
echo Press Ctrl+C to stop the server
echo.

python chatbot_api.py

pause
