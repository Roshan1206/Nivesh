@echo off
title Ollama Stack - Starting
echo.
echo  ========================================
echo   Starting Ollama + Open WebUI Stack
echo  ========================================
echo.

@REM cd /d "%~dp0"
docker compose up -d

echo.
echo  ✅ Stack is starting up...
echo.
echo  Open WebUI  →  http://localhost:3000
echo  Ollama API  →  http://localhost:11434
echo.
echo  First time? Run pull-models.bat next!
echo.
pause
