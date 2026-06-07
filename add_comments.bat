@echo off
title Nivesh Bank — Javadoc Comment Generator
color 0A

echo.
echo  ============================================================
echo   Nivesh Bank — Javadoc Comment Generator
echo  ============================================================
echo.

:: ── Check Python is available ────────────────────────────────
python --version >nul 2>&1
if errorlevel 1 (
    echo  [ERROR] Python is not installed or not in PATH.
    echo.
    echo  Install Python from https://www.python.org/downloads/
    echo  Make sure to check "Add Python to PATH" during install.
    echo.
    pause
    exit /b 1
)

:: ── Run the script from wherever this .bat lives ─────────────
cd /d "%~dp0"
python add_comments.py

:: ── If Python itself crashed, show the error ─────────────────
if errorlevel 1 (
    echo.
    echo  [ERROR] Script exited with an error. See above for details.
    pause
)
