#!/usr/bin/env python3
import os
from pathlib import Path
import shutil
import subprocess

root = Path(__file__).resolve().parent
compiler_path = Path("tools") / "zig-windows-x86_64-0.14.0" / "zig.exe"
candidates = []
if os.environ.get("ZIG"):
    candidates.append(Path(os.environ["ZIG"]))
candidates.append(root / compiler_path)
zig_in_path = shutil.which("zig")
if zig_in_path:
    candidates.append(Path(zig_in_path))

zig = next((candidate for candidate in candidates if candidate.is_file()), None)
if zig is None:
    raise SystemExit("Zig compiler was not found; install Zig or set the ZIG environment variable to zig.exe")

java_home_env = os.environ.get("JAVA_HOME")
if not java_home_env:
    raise SystemExit("JAVA_HOME is not set; please point JAVA_HOME to a JDK 21+ installation")
java_home = Path(java_home_env)

sources = [
    "src/southside_auth.cpp",
    "src/session.cpp",
    "src/crypto.cpp",
    "src/login_window.cpp",
    "vendor/imgui/imgui.cpp",
    "vendor/imgui/imgui_draw.cpp",
    "vendor/imgui/imgui_tables.cpp",
    "vendor/imgui/imgui_widgets.cpp",
    "vendor/imgui/backends/imgui_impl_win32.cpp",
    "vendor/imgui/backends/imgui_impl_dx11.cpp",
]

command = [
    str(zig), "c++", "-target", "x86_64-windows-gnu", "-std=c++17", "-O2", "-shared",
    "-DUNICODE", "-D_UNICODE",
    f"-I{java_home / 'include'}",
    f"-I{java_home / 'include' / 'win32'}",
    f"-I{root / 'src'}",
    f"-I{root / 'vendor' / 'json' / 'single_include'}",
    f"-I{root / 'vendor' / 'imgui'}",
    f"-I{root / 'vendor' / 'imgui' / 'backends'}",
    *sources,
    "src/southside_auth.def",
    "-lbcrypt", "-ld3d11", "-ld3dcompiler_47", "-ldwmapi", "-lgdi32", "-lshell32",
    "-luser32", "-lole32", "-luuid",
    "-o", "build/southside_auth.dll",
]

(root / "build").mkdir(exist_ok=True)
subprocess.run(command, cwd=root, check=True)
