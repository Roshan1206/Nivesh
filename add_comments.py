"""
Nivesh Bank - Javadoc Comment Generator
Adds /** ... */ block comments to Java methods using Ollama AI (with template fallback).
Rules:
  - Only .java files
  - Skip @SpringBootApplication classes (main + test)
  - Skip methods that already have /** above them
  - All methods: public, protected, private
  - Spacing: 2 blank lines after method end, then comment, then method signature
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
OLLAMA_URL        = "http://localhost:11434/api/generate"
OLLAMA_MODEL      = "gemma3:4b"
CONNECT_TIMEOUT   = 10   # seconds to establish connection
TOKEN_TIMEOUT     = 60   # seconds to wait for EACH token chunk (streaming)
WARMUP_TIMEOUT    = 300  # seconds for first-ever request (CPU cold load can take 2-3 min)

# Folders to always skip even if they have src/
SKIP_FOLDERS = {".git", ".idea", ".gradle", ".mvn", "build", "target",
                "out", "node_modules", ".continue", "ollama", "__pycache__"}

METHOD_PATTERN = re.compile(
    r'^(?P<indent>[ \t]*)'
    r'(?P<modifiers>(?:(?:public|protected|private|static|final|synchronized|abstract|native|default|transient|volatile)\s+)+)'
    r'(?P<return_type>[\w<>\[\].,\s?]+?)\s+'
    r'(?P<name>\w+)\s*'
    r'\((?P<params>[^)]*)\)\s*'
    r'(?:throws\s+[\w,\s]+\s*)?\{',
    re.MULTILINE
)

SKIP_METHOD_NAMES = {"main"}


# ─────────────────────────────────────────────
# OLLAMA CALL  (streaming to avoid CPU timeout)
# ─────────────────────────────────────────────
_ollama_warmed_up = False  # track whether first request has completed


def ask_ollama(method_signature: str, class_name: str) -> str | None:
    global _ollama_warmed_up

    prompt = (
        f"You are a Java documentation expert.\n"
        f"Write ONLY a Javadoc comment block for this Java method in class '{class_name}'.\n"
        f"Method: {method_signature}\n\n"
        f"Output format (output NOTHING else, no explanation, no markdown):\n"
        f"/**\n"
        f" * One sentence describing what the method does.\n"
        f" *\n"
        f" * @param paramName description\n"
        f" * @return returnType\n"
        f" */"
    )

    payload = json.dumps({
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "stream": True,          # stream=True: read tokens as they arrive
        "options": {
            "temperature": 0.1,
            "num_predict": 250,  # short — Javadoc doesn't need more
            "stop": ["*/"]       # stop token: end as soon as comment closes
        }
    }).encode("utf-8")

    # First request takes longer — model must load into RAM
    timeout = WARMUP_TIMEOUT if not _ollama_warmed_up else TOKEN_TIMEOUT

    try:
        req = urllib.request.Request(
            OLLAMA_URL,
            data=payload,
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        collected = []
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            for raw_line in resp:
                if not raw_line.strip():
                    continue
                try:
                    chunk = json.loads(raw_line.decode("utf-8"))
                except json.JSONDecodeError:
                    continue
                token = chunk.get("response", "")
                collected.append(token)
                if chunk.get("done", False):
                    break

        _ollama_warmed_up = True
        full = "".join(collected).strip()

        # Reconstruct the closing */ that the stop token consumed
        if "/**" in full:
            start = full.index("/**")
            body  = full[start:]
            if not body.rstrip().endswith("*/"):
                body = body.rstrip() + "\n */"
            return body

    except (urllib.error.URLError,
            urllib.error.HTTPError,
            json.JSONDecodeError,
            KeyError,
            TimeoutError,
            OSError,
            ConnectionResetError,
            ConnectionRefusedError):
        return None
    except Exception:
        return None

    return None


# ─────────────────────────────────────────────
# TEMPLATE FALLBACK
# ─────────────────────────────────────────────
def clean_param_type(raw_type: str) -> str:
    """Strip Spring/Jakarta annotations from a param type string.
    e.g. '@RequestHeader String' -> 'String'
         '@PathVariable UUID'    -> 'UUID'
    """
    # Remove @Annotation(...) and @Annotation tokens
    cleaned = re.sub(r'@\w+(?:\([^)]*\))?', '', raw_type).strip()
    return cleaned if cleaned else raw_type.strip()


def build_template_comment(name: str, params_str: str, return_type: str, indent: str) -> str:
    sp = indent + " "  # single space prefix for all * lines
    lines = [f"{indent}/**"]

    # Summary: camelCase -> spaced words
    words = re.sub(r'([A-Z])', r' \1', name).strip().lower()
    lines.append(f"{sp}* {words.capitalize()}.")

    # Parse params splitting on commas not inside <> brackets
    param_list = []
    if params_str.strip():
        depth   = 0
        current = ""
        for ch in params_str:
            if ch == "<":
                depth += 1
                current += ch
            elif ch == ">":
                depth -= 1
                current += ch
            elif ch == "," and depth == 0:
                param_list.append(current.strip())
                current = ""
            else:
                current += ch
        if current.strip():
            param_list.append(current.strip())

    # Build valid param entries (skip malformed)
    param_lines = []
    for param in param_list:
        tokens = param.split()
        if len(tokens) < 2:
            continue
        pname = tokens[-1].strip(".,")
        raw_type = " ".join(tokens[:-1])
        ptype = clean_param_type(raw_type)
        param_lines.append(f"{sp}* @param {pname} the {pname}")

    rt = return_type.strip()
    has_return = rt and rt not in ("void", "Void")

    # Format:
    #   * description.
    #   *                       <- blank after description (always)
    #   * @param x the x        <- params, NO blank lines between them
    #   * @param y the y
    #   *                       <- blank before @return (only if both params and return exist)
    #   * @return Type
    #   */
    if param_lines or has_return:
        lines.append(f"{sp}*")  # blank line after description

    lines.extend(param_lines)   # all @param, no blanks between

    if has_return:
        if param_lines:
            lines.append(f"{sp}*")  # blank between @param block and @return
        lines.append(f"{sp}* @return {rt}")

    lines.append(f"{indent} */")
    return "\n".join(lines)


# ─────────────────────────────────────────────
# OLLAMA HEALTH CHECK
# ─────────────────────────────────────────────
def ollama_is_up() -> bool:
    try:
        req = urllib.request.Request("http://localhost:11434/api/tags", method="GET")
        with urllib.request.urlopen(req, timeout=5):
            return True
    except Exception:
        return False


# ─────────────────────────────────────────────
# HELPERS
# ─────────────────────────────────────────────
def is_main_class(content: str) -> bool:
    return "@SpringBootApplication" in content


def get_class_name(content: str, filepath: str) -> str:
    m = re.search(r'\bclass\s+(\w+)', content)
    return m.group(1) if m else Path(filepath).stem


def already_has_javadoc(lines: list, method_line_idx: int) -> bool:
    """Walk backwards skipping blank lines and annotations. Return True if */ found."""
    idx = method_line_idx - 1
    while idx >= 0:
        stripped = lines[idx].strip()
        if stripped == "" or stripped.startswith("@"):
            idx -= 1
            continue
        return stripped.endswith("*/")
    return False


# ─────────────────────────────────────────────
# PROCESS ONE FILE
# ─────────────────────────────────────────────
def process_file(filepath: str, use_ollama: bool) -> tuple:
    with open(filepath, "r", encoding="utf-8", errors="replace") as f:
        original = f.read()

    if is_main_class(original):
        print(f"    ⏭  Skipped (main class): {Path(filepath).name}")
        return 0, 0

    lines      = original.splitlines()
    class_name = get_class_name(original, filepath)
    insertions = []
    found      = 0
    skipped    = 0

    for match in METHOD_PATTERN.finditer(original):
        name        = match.group("name")
        params_str  = match.group("params") or ""
        return_type = (match.group("return_type") or "").strip()
        indent      = match.group("indent") or ""
        modifiers   = (match.group("modifiers") or "").strip()

        if name in SKIP_METHOD_NAMES:
            continue
        if return_type in ("class", "interface", "enum", "record", "@interface"):
            continue
        if not return_type:
            continue

        line_idx = original[:match.start()].count("\n")
        found += 1

        if already_has_javadoc(lines, line_idx):
            skipped += 1
            continue

        sig = f"{modifiers} {return_type} {name}({params_str})".strip()

        if use_ollama:
            comment = ask_ollama(sig, class_name)
            if comment is None:
                print(f"      ⚠  Ollama slow for {name}(), used template")
                comment = build_template_comment(name, params_str, return_type, indent)
        else:
            comment = build_template_comment(name, params_str, return_type, indent)

        # Re-indent Ollama output to match method indent
        indented_lines = []
        for ln in comment.splitlines():
            s = ln.strip()
            if s.startswith("/**"):
                indented_lines.append(indent + "/**")
            elif s.startswith("*/"):
                indented_lines.append(indent + " */")
            elif s.startswith("*"):
                indented_lines.append(indent + " " + s)
            else:
                indented_lines.append(ln)
        insertions.append((line_idx, "\n".join(indented_lines)))

    if not insertions:
        print(f"    ✅ Nothing to add ({skipped} already commented): {Path(filepath).name}")
        return found, 0

    # Apply from bottom to top so line indices stay valid
    insertions.sort(key=lambda x: x[0], reverse=True)
    result_lines = lines[:]

    for line_idx, comment in insertions:
        # Walk up past the method's own @Annotation lines to find
        # the true insertion point (comment goes BEFORE annotations)
        insert_at = line_idx
        while insert_at > 0:
            prev = result_lines[insert_at - 1].strip()
            if prev.startswith("@") or prev == "":
                insert_at -= 1
            else:
                break

        # Skip back over any blank lines above annotations so we insert
        # right after the previous non-blank content
        while insert_at > 0 and result_lines[insert_at - 1].strip() == "":
            insert_at -= 1

        # Insert: 2 blank lines, then the comment block (no trailing blank)
        comment_lines = comment.splitlines()
        block = ["", ""] + comment_lines
        for i, bl in enumerate(block):
            result_lines.insert(insert_at + i, bl)

        # Remove any blank line that now sits between */ and the next content
        # (annotation or method signature). insert_at + len(block) is the line
        # right after the inserted comment.
        after_idx = insert_at + len(block)
        while after_idx < len(result_lines) and result_lines[after_idx].strip() == "":
            result_lines.pop(after_idx)

    new_content = "\n".join(result_lines)
    if not new_content.endswith("\n"):
        new_content += "\n"

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(new_content)

    added = len(insertions)
    print(f"    ✅ {added} comment(s) added, {skipped} skipped: {Path(filepath).name}")
    return found, added


# ─────────────────────────────────────────────
# FIND JAVA FILES IN A MODULE
# ─────────────────────────────────────────────
def find_java_files(module_path: str) -> list:
    java_files = []
    for root, dirs, files in os.walk(module_path):
        # Prune skip folders so os.walk doesn't descend into them
        dirs[:] = [d for d in dirs if d not in SKIP_FOLDERS]
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
def has_java_in_src(folder) -> bool:
    """True if folder/src/ exists and contains at least one .java file."""
    src = folder / "src"
    if not src.exists():
        return False
    for _, _, files in os.walk(str(src)):
        if any(f.endswith(".java") for f in files):
            return True
    return False


def count_java_modules(candidate) -> int:
    """Count direct subfolders that look like Java service modules."""
    try:
        count = 0
        for d in candidate.iterdir():
            if (d.is_dir()
                    and d.name not in SKIP_FOLDERS
                    and not d.name.startswith(".")
                    and has_java_in_src(d)):
                count += 1
        return count
    except PermissionError:
        return 0


def find_project_root() -> str:
    """
    Detects project root using multiple strategies:
    1. Walk UP from script location for settings.gradle / pom.xml
    2. Use script dir if it contains >=1 Java service subfolder
    3. Use parent dir if it contains >=1 Java service subfolder
    4. Fall back to script dir
    Uses os.path.abspath to handle Windows path quirks.
    """
    script_dir = Path(os.path.abspath(__file__)).parent

    # Strategy 1: Gradle/Maven root markers
    for candidate in [script_dir, *script_dir.parents]:
        if any((candidate / f).exists() for f in
               ["settings.gradle", "settings.gradle.kts", "pom.xml"]):
            return str(candidate)

    # Strategy 2: script dir itself has Java modules
    if count_java_modules(script_dir) >= 1:
        return str(script_dir)

    # Strategy 3: parent dir has Java modules
    parent = script_dir.parent
    if count_java_modules(parent) >= 1:
        return str(parent)

    return str(script_dir)


# ─────────────────────────────────────────────
# FIND ALL MODULES
# ─────────────────────────────────────────────
def find_modules(root: str) -> list:
    """
    A module is any direct subfolder of root that:
    - Is not in SKIP_FOLDERS
    - Does not start with '.'
    - Contains a src/ directory (with Java files inside)
    Does NOT require build.gradle — supports flat microservice layouts.
    """
    modules = []
    for entry in sorted(Path(root).iterdir(), key=lambda e: e.name):
        if not entry.is_dir():
            continue
        if entry.name in SKIP_FOLDERS or entry.name.startswith("."):
            continue
        src_path = entry / "src"
        if not src_path.exists():
            continue
        # Must have at least one .java file somewhere inside
        if find_java_files(str(entry)):
            modules.append(str(entry))
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

    root = find_project_root()
    print(f"  📁 Project root detected: {root}")
    print()

    use_ollama = ollama_is_up()
    if use_ollama:
        print(f"  🤖 Ollama is UP — warming up {OLLAMA_MODEL}...")
        print(f"      (CPU cold start can take 2-3 min. Please wait, do not close this window)")
        # Send a tiny warmup request so the model loads into RAM before real processing.
        # Without this, the first method times out every time.
        warmup_payload = json.dumps({
            "model": OLLAMA_MODEL,
            "prompt": "Say: ready",
            "stream": True,
            "options": {"num_predict": 5}
        }).encode("utf-8")
        try:
            warmup_req = urllib.request.Request(
                OLLAMA_URL,
                data=warmup_payload,
                headers={"Content-Type": "application/json"},
                method="POST"
            )
            warmed = False
            with urllib.request.urlopen(warmup_req, timeout=WARMUP_TIMEOUT) as wresp:
                for raw_line in wresp:
                    if not raw_line.strip():
                        continue
                    try:
                        chunk = json.loads(raw_line.decode("utf-8"))
                    except Exception:
                        continue
                    if chunk.get("done", False):
                        warmed = True
                        break
            if warmed:
                print(f"  ✅ Model ready — using AI with template fallback")
            else:
                raise Exception("no done signal")
        except Exception as e:
            print(f"  ⚠  Warmup failed ({e}) — using template-based comments")
            use_ollama = False
    else:
        print("  ⚠  Ollama is DOWN — using template-based comments")
    print()

    modules = find_modules(root)
    if not modules:
        print("  ❌ No modules with Java source files found.")
        print(f"     Looked inside: {root}")
        print("     Each module folder must contain a src/ directory with .java files.")
        input("\n  Press Enter to exit...")
        sys.exit(1)

    print("  Select modules to process:")
    print()
    for i, mod in enumerate(modules, 1):
        name       = Path(mod).name
        java_count = len(find_java_files(mod))
        print(f"    [{i:2}] {name:<30} ({java_count} Java files)")
    print()

    print("  Enter numbers, names, or mix. Examples:  2   |  auth  |  1,3,5  |  auth,customer  |  a")
    print()
    choice = input("  Your choice: ").strip().lower()

    module_names = [Path(m).name.lower() for m in modules]

    if choice in ("a", "all"):
        selected = modules
    else:
        seen    = set()
        selected = []
        for part in choice.replace(";", ",").split(","):
            part = part.strip()
            if not part:
                continue
            # Match by number
            if part.isdigit():
                idx = int(part) - 1
                if 0 <= idx < len(modules) and idx not in seen:
                    seen.add(idx)
                    selected.append(modules[idx])
            else:
                # Match by name — partial, case-insensitive
                matches = [
                    i for i, n in enumerate(module_names)
                    if part in n and i not in seen
                ]
                for idx in matches:
                    seen.add(idx)
                    selected.append(modules[idx])

    if not selected:
        print("\n  ❌ No valid selection. Try a number like '2' or a name like 'auth'.")
        input("\n  Press Enter to exit...")
        sys.exit(0)

    print()
    print(f"  Processing {len(selected)} module(s)...")
    print()

    total_found = 0
    total_added = 0
    start_time  = time.time()

    for mod in selected:
        mod_name   = Path(mod).name
        java_files = find_java_files(mod)
        print(f"  📦 Module: {mod_name} ({len(java_files)} files)")

        for jf in java_files:
            try:
                found, added = process_file(jf, use_ollama)
                total_found += found
                total_added += added
            except Exception as e:
                print(f"    ⚠  Error processing {Path(jf).name}: {e} — skipping")
                continue

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
