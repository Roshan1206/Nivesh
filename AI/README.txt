# Ollama Local AI Stack — Setup Guide
# Windows WSL2 | CPU-only | 8GB RAM | 100% Free

## Files in this folder

| File               | What it does                                      |
|--------------------|---------------------------------------------------|
| docker-compose.yml | Runs Ollama + Open WebUI as Docker containers     |
| .wslconfig         | Caps WSL2 RAM so Windows doesn't get starved      |
| config.yaml        | Continue.dev plugin config for VS Code/IntelliJ   |
| start.bat          | Double-click to start the stack                   |
| stop.bat           | Double-click to stop and free RAM                 |
| pull-models.bat    | One-time model download (~3.8GB total)            |

---

## STEP 1 — Place .wslconfig (do this FIRST)

Copy the `.wslconfig` file to:
  C:\Users\<YourWindowsUsername>\.wslconfig

Then open PowerShell and run:
  wsl --shutdown

This limits WSL2 to 6GB so Windows keeps 2GB for itself.

NOTE: If your PC has fewer than 4 cores, edit .wslconfig and
change processors=4 to match your actual core count.

---

## STEP 2 — Configure Docker Desktop

Open Docker Desktop → Settings → Resources:
  - Memory: 5 GB
  - Click "Apply & Restart"

Also check: Settings → Resources → WSL Integration
  - Enable it for your Ubuntu/WSL distro

---

## STEP 3 — Start the Stack

Place the entire folder somewhere on your PC, e.g.:
  C:\Users\Roshan\ollama-stack\

Double-click: start.bat

Wait ~30 seconds for containers to be ready.

---

## STEP 4 — Pull the AI Models (ONE TIME ONLY)

Double-click: pull-models.bat

This downloads ~3.8GB of models. Do this once.
Models are saved in a Docker volume and persist across restarts.

---

## STEP 5 — Open the Chat UI

Open your browser and go to:
  http://localhost:3000

- Sign up with any email/password (this is local, not sent anywhere)
- Select "gemma3:4b" as your model
- Start chatting!

---

## STEP 6 — Set Up Continue.dev in Your IDE

### VS Code
1. Open VS Code → Extensions → search "Continue" → Install
2. Copy config.yaml to: C:\Users\<YourName>\.continue\config.yaml
   (Create the .continue folder if it doesn't exist)
3. Reload VS Code — Continue panel appears in the sidebar
4. Use Ctrl+I to chat, Tab for autocomplete

### IntelliJ IDEA
1. Go to File → Settings → Plugins → Marketplace
2. Search "Continue" → Install → Restart IntelliJ
3. The same config.yaml is read automatically from ~/.continue/config.yaml
4. Continue panel appears in the right sidebar

---

## Daily Use

START:  Double-click start.bat
STOP:   Double-click stop.bat  ← always stop when done to free RAM

---

## URLs

| Service    | URL                       |
|------------|---------------------------|
| Chat UI    | http://localhost:3000     |
| Ollama API | http://localhost:11434    |

---

## Models Installed

| Model               | Size   | Used for                  |
|---------------------|--------|---------------------------|
| gemma3:4b           | ~2.5GB | Chat, docs, code review   |
| qwen2.5-coder:1.5b  | ~1GB   | Tab autocomplete in IDE   |
| nomic-embed-text    | ~270MB | @codebase semantic search |

---

## Troubleshooting

Problem: "Cannot connect to Ollama"
Fix: Make sure start.bat was run and wait 30 seconds

Problem: Everything is slow / freezing
Fix: Close Chrome tabs, other apps; check Docker RAM setting is 5GB

Problem: Models not showing in Open WebUI
Fix: Run pull-models.bat, then refresh the browser

Problem: Continue not working in VS Code
Fix: Check config.yaml is at C:\Users\<Name>\.continue\config.yaml

---

## RAM Usage at a Glance

Windows + WSL2 overhead : ~2.0 GB
Docker daemon           : ~0.3 GB
Ollama (idle)           : ~0.2 GB
gemma3:4b loaded        : ~2.5 GB
qwen2.5-coder:1.5b      : ~1.0 GB
Open WebUI              : ~0.3 GB
--------------------------------
Total                   : ~6.3 GB (safe on 8GB)

Ollama auto-unloads idle models after 5 minutes — this is normal.
