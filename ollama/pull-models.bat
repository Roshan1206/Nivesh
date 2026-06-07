@echo off
title Ollama Stack - Pulling Models
echo.
echo  ========================================
echo   Pulling AI Models (one-time setup)
echo   This will take several minutes...
echo  ========================================
echo.

echo  [1/3] Pulling Qwen 2.5 Coder 1.5B (autocomplete, ~1GB)...
docker exec -it ollama ollama pull qwen2.5-coder:1.5b

echo.
echo  [2/3] Pulling Gemma 3 4B (chat + docs, ~2.5GB)...
docker exec -it ollama ollama pull gemma3:4b

echo.
echo  [3/3] Pulling Nomic Embed Text (codebase search, ~270MB)...
docker exec -it ollama ollama pull nomic-embed-text

echo.
echo  ========================================
echo   ✅ All models downloaded!
echo  ========================================
echo.
echo  Open WebUI  →  http://localhost:3000
echo  Select gemma3:4b in the UI to start chatting.
echo.
pause
