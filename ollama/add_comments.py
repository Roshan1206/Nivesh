"""
Nivesh Bank - Javadoc Comment Generator
Adds /** ... */ block comments to Java methods using Ollama AI (with template fallback).
Rules:
  - Only .java files
  - Skip @SpringBootApplication classes (main + test)
  - Skip methods that already have /** above them
  - All methods: public, protected, private
  - Spacing: 2 blank lines after method, then comment, then method signature
"""

import os
import re
import sys
import json
import time
import urllib.request
import urllib.error
from pathlib import Path

# ─────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────
OLLAMA_URL   = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "gemma3:4b"
TIMEOUT_SECS = 60

# Matches: optional annotations, then method signature line
# Captures: (indent, return_type + name + params)
METHOD_PATTERN = re.compile(
    r'^(?P<indent>[ \t]*)'
    r'(?:(?:@\w+(?:\([^)]*\))?\s*\n(?:[ \t]*))*)'   # annotations (lookahead handled separately)
    r'(?P<modifiers>(?:public|protected|private|static|final|synchronized|abstract|native|default|transient|volatile)\s+)*'
    r'(?P<return_type>[\w<>\[\].,\s?]+?)\s+'
    r'(?P<name>\w+)\s*'
    r'\((?P<params>[^)]*)\)\s*'
    r'(?:throws\s+[\w,\s]+)?\s*\{',
    re.MULTILINE
)

SKIP_METHODS = {"main", "lambda"}

# ─────────────────────────────────────────────
# OLLAMA CALL
# ─────────────────────────────────────────────
def ask_ollama(method_signature: str, class_name: str) -> str | None:
    prompt = (
        f"You are a Java documentation expert.\n"
        f"Write a single Javadoc comment block for this Java method in class '{class_name}'.\n"
        f"Method signature:\n{method_signature}\n\n"
        f"Rules:\n"
        f"- Start with /**\n"
        f"- End with */\n"
        f"- First line: one sentence summary of what the method does\n"
        f"- Add @param for each parameter with a short description\n"
        f"- Add @return if return type is not void\n"
        f"- Add @throws if method throws exceptions\n"
        f"- No markdown, no code blocks, no explanation outside the comment\n"
        f"- Do NOT include the method signature itself\n"
        f"Output only the Javadoc block, nothing else."
    )
    payload = json.dumps({
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "stream": False,
        "options": {"temperature": 0.2, "num_predict": 300}
    }).encode("utf-8")

    try:
        req = urllib.request.Request(
            OLLAMA_URL,
            data=payload,
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req, timeout=TIMEOUT_SECS) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            raw = data.get("response", "").strip()
            # Ensure it starts/ends correctly
            if "/**" in raw and "*/" in raw:
                start = raw.index("/**")
                end   = raw.rindex("*/") + 2
                return raw[start:end]
    except (urllib.error.URLError, json.JSONDecodeError, KeyError):
        return None
    return None


# ─────────────────────────────────────────────
# TEMPLATE FALLBACK
# ─────────────────────────────────────────────
def build_template_comment(name: str, params_str: str, return_type: str, indent: str) -> str:
    lines = [f"{indent}/**"]

    # Summary line from method name (camelCase → words)
    words = re.sub(r'([A-Z])', r' \1', name).strip().lower()
    lines.append(f"{indent} * {words.capitalize()}.")

    # @param lines
    if params_str.strip():
        params = [p.strip() for p in params_str.split(",") if p.strip()]
        for param in params:
            parts = param.split()
            if len(parts) >= 2:
                pname = parts[-1].strip(".")
                ptype = " ".join(parts[:-1])
                lines.append(f"{indent} *")
                lines.append(f"{indent} * @param {pname} the {pname} ({ptype})")

    # @return line
    rt = return_type.strip()
    if rt and rt not in ("void", "Void"):
        lines.append(f"{indent} *")
        lines.append(f"{indent} * @return the {rt.lower().replace('<', '').replace('>', '')}")

    lines.append(f"{indent} */")
    return "\n".join(lines)


# ─────────────────────────────────────────────
# CHECK: is Ollama reachable?
# ─────────────────────────────────────────────
def ollama_is_up() -> bool:
    try:
        req = urllib.request.Request("http://localhost:11434/api/tags", method="GET")
        with urllib.request.urlopen(req, timeout=5):
            return True
    except Exception:
        return False


# ─────────────────────────────────────────────
# CHECK: skip this file?
# ─────────────────────────────────────────────
def is_main_class(content: str) -> bool:
    return "@SpringBootApplication" in content


# ─────────────────────────────────────────────
# EXTRACT CLASS NAME
# ─────────────────────────────────────────────
def get_class_name(content: str, filepath: str) -> str:
    m = re.search(r'\bclass\s+(\w+)', content)
    if m:
        return m.group(1)
    return Path(filepath).stem


# ─────────────────────────────────────────────
# CHECK: already has /** directly before method
# ─────────────────────────────────────────────
def already_has_javadoc(lines: list[str], method_line_idx: int) -> bool:
    """
    Walk backwards from the method line, skipping blank lines and @annotation lines.
    If we find */ it means there's already a Javadoc comment.
    """
    idx = method_line_idx - 1
    while idx >= 0:
        stripped = lines[idx].strip()
        if stripped == "" or stripped.startswith("@"):
            idx -= 1
            continue
        if stripped.endswith("*/"):
            return True
        break
    return False


# ─────────────────────────────────────────────
# PROCESS ONE FILE
# ─────────────────────────────────────────────
def process_file(filepath: str, use_ollama: bool) -> tuple[int, int]:
    """Returns (methods_found, methods_commented)"""
    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        original = f.read()

    if is_main_class(original):
        print(f"    ⏭  Skipped (main class): {Path(filepath).name}")
        return 0, 0

    lines      = original.splitlines()
    class_name = get_class_name(original, filepath)

    # We'll rebuild the file inserting comments before methods
    # Work from bottom to top so line indices stay valid
    insertions = []  # list of (line_index, comment_text)

    found   = 0
    skipped = 0

    for match in METHOD_PATTERN.finditer(original):
        name        = match.group("name")
        params_str  = match.group("params") or ""
        return_type = (match.group("return_type") or "").strip()
        indent      = match.group("indent") or ""
        modifiers   = (match.group("modifiers") or "").strip()

        # Skip constructors/lambdas/main
        if name in SKIP_METHODS:
            continue
        # Skip if it looks like a class/interface declaration
        if return_type in ("class", "interface", "enum", "record", "@interface"):
            continue
        # Skip annotation-only or empty return types
        if not return_type:
            continue

        # Find line number of this match
        method_char_pos  = match.start()
        line_idx         = original[:method_char_pos].count("\n")

        found += 1

        # Skip if already has Javadoc
        if already_has_javadoc(lines, line_idx):
            skipped += 1
            continue

        # Generate comment
        sig = f"{modifiers} {return_type} {name}({params_str})".strip()

        if use_ollama:
            comment = ask_ollama(sig, class_name)
            if comment is None:
                print(f"      ⚠  Ollama timeout for {name}(), using template")
                comment = build_template_comment(name, params_str, return_type, indent)
        else:
            comment = build_template_comment(name, params_str, return_type, indent)

        # Re-indent comment to match method indent (Ollama output may have no indent)
        indented_lines = []
        for l in comment.splitlines():
            stripped = l.strip()
            if stripped.startswith("/**") or stripped.startswith("*") or stripped.startswith("*/"):
                indented_lines.append(indent + stripped)
            else:
                indented_lines.append(l)
        indented_comment = "\n".join(indented_lines)

        insertions.append((line_idx, indented_comment))

    if not insertions:
        print(f"    ✅ Nothing to add ({skipped} already commented): {Path(filepath).name}")
        return found, 0

    # Apply insertions from bottom to top
    insertions.sort(key=lambda x: x[0], reverse=True)
    result_lines = lines[:]

    for line_idx, comment in insertions:
        # Rule: 2 blank lines before comment, comment, then method
        # Find where to insert: line_idx is the method signature line
        # Remove any existing blank lines just above it (we'll re-add exactly 2)
        insert_at = line_idx
        while insert_at > 0 and result_lines[insert_at - 1].strip() == "":
            insert_at -= 1

        # Build: 2 blank lines + comment lines
        comment_lines = comment.splitlines()
        block = ["", ""] + comment_lines
        for i, bl in enumerate(block):
            result_lines.insert(insert_at + i, bl)

    new_content = "\n".join(result_lines)
    if not new_content.endswith("\n"):
        new_content += "\n"

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(new_content)

    added = len(insertions)
    print(f"    ✅ {added} comment(s) added, {skipped} skipped: {Path(filepath).name}")
    return found, added


# ─────────────────────────────────────────────
# FIND ALL JAVA FILES IN A MODULE
# ─────────────────────────────────────────────
def find_java_files(module_path: str) -> list[str]:
    java_files = []
    for root, _, files in os.walk(module_path):
        # Only src/main/java and src/test/java
        norm = root.replace("\\", "/")
        if "src/main/java" not in norm and "src/test/java" not in norm:
            continue
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))
    return java_files


# ─────────────────────────────────────────────
# AUTO-DETECT PROJECT ROOT
# ─────────────────────────────────────────────
def find_project_root() -> str | None:
    # Start from script location, walk up looking for settings.gradle or pom.xml
    start = Path(__file__).resolve().parent
    for candidate in [start, *start.parents]:
        if (candidate / "settings.gradle").exists() or \
           (candidate / "settings.gradle.kts").exists() or \
           (candidate / "pom.xml").exists():
            return str(candidate)
    return None


# ─────────────────────────────────────────────
# FIND ALL MODULES (subdirs with build.gradle)
# ─────────────────────────────────────────────
def find_modules(root: str) -> list[str]:
    modules = []
    for entry in sorted(os.scandir(root), key=lambda e: e.name):
        if not entry.is_dir():
            continue
        has_gradle = (
            os.path.exists(os.path.join(entry.path, "build.gradle")) or
            os.path.exists(os.path.join(entry.path, "build.gradle.kts")) or
            os.path.exists(os.path.join(entry.path, "pom.xml"))
        )
        has_src = os.path.exists(os.path.join(entry.path, "src"))
        if has_gradle and has_src:
            modules.append(entry.path)
    return modules


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────
def main():
    print()
    print("=" * 60)
    print("  Nivesh Bank — Javadoc Comment Generator")
    print("=" * 60)
    print()

    # Detect project root
    root = find_project_root()
    if not root:
        print("  ❌ Could not find project root (no settings.gradle or pom.xml found).")
        print("     Place add_comments.py inside your Nivesh Bank project folder.")
        input("\n  Press Enter to exit...")
        sys.exit(1)

    print(f"  📁 Project root: {root}")
    print()

    # Check Ollama
    use_ollama = ollama_is_up()
    if use_ollama:
        print(f"  🤖 Ollama is UP — using AI ({OLLAMA_MODEL}) with template fallback")
    else:
        print("  ⚠  Ollama is DOWN — using template-based comments")
    print()

    # Find modules
    modules = find_modules(root)
    if not modules:
        print("  ❌ No modules found. Make sure the script is inside the project root.")
        input("\n  Press Enter to exit...")
        sys.exit(1)

    # Show menu
    print("  Select modules to process (comma-separated numbers, or 'a' for all):")
    print()
    for i, mod in enumerate(modules, 1):
        name = Path(mod).name
        java_count = len(find_java_files(mod))
        print(f"    [{i}] {name}  ({java_count} Java files)")
    print()

    choice = input("  Your choice: ").strip().lower()

    if choice == "a":
        selected = modules
    else:
        indices = []
        for part in choice.split(","):
            part = part.strip()
            if part.isdigit():
                idx = int(part) - 1
                if 0 <= idx < len(modules):
                    indices.append(idx)
        selected = [modules[i] for i in indices]

    if not selected:
        print("\n  ❌ No valid selection. Exiting.")
        input("\n  Press Enter to exit...")
        sys.exit(0)

    print()
    print(f"  Processing {len(selected)} module(s)...")
    print()

    total_found   = 0
    total_added   = 0
    start_time    = time.time()

    for mod in selected:
        mod_name   = Path(mod).name
        java_files = find_java_files(mod)
        print(f"  📦 Module: {mod_name} ({len(java_files)} files)")

        for jf in java_files:
            found, added = process_file(jf, use_ollama)
            total_found += found
            total_added += added

        print()

    elapsed = time.time() - start_time

    print("=" * 60)
    print(f"  ✅ Done in {elapsed:.1f}s")
    print(f"     Methods found : {total_found}")
    print(f"     Comments added: {total_added}")
    print(f"     Skipped       : {total_found - total_added}")
    print("=" * 60)
    print()
    input("  Press Enter to exit...")


if __name__ == "__main__":
    main()
