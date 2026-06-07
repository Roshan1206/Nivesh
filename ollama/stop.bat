@echo off
title Ollama Stack - Stopping
echo.
echo  ========================================
echo   Stopping Ollama + Open WebUI Stack
echo  ========================================
echo.

cd /d "%~dp0"
docker compose down

echo.
echo  ✅ Stack stopped. RAM is now free.
echo.
pause
