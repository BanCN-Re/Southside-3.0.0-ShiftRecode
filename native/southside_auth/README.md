# Southside Native Authentication — Reconstructed Source

Functionally reconstructed C++17 source for `southside_auth.dll`, based on the unpacked runtime image and JNI/assembly analysis.

## Build

Ensure `JAVA_HOME` points to a JDK 21+ installation and `zig` is available on `PATH` (or set `ZIG` to `zig.exe`), then run:

```bat
build.bat
```

Output: `build/southside_auth.dll`

The build uses Zig C++ and vendored Dear ImGui 1.92.6 / nlohmann-json 3.12.0. `CMakeLists.txt` is also provided for building with an existing C++17 toolchain (MinGW / MSVC).

## Recovered behavior

- JNI exports and Java callback interface
- Login (`t=1`), registration (`t=4`), and top-up (`t=5`) request generation
- Chat (`t=7,m`) and presence (`t=6,ui,a`) packets
- SHA-256/Base64/alphanumeric request signatures and device identifiers
- AES-256-GCM `SouthCredentials.dat` storage with 12-byte nonce and 16-byte tag
- WSS endpoint and reconnect state handling
- Dear ImGui login, registration, and top-up window

## Source layout

- `src/`: JNI exports, session state, protocol, credentials, and login window
- `vendor/`: source copies of Dear ImGui and nlohmann-json
- `build.py` / `build.bat`: Zig-based build entry points
- `CMakeLists.txt`: CMake build configuration

The VMProtect-managed server-response branch is represented by a tolerant functional reconstruction. Its hidden internal branch layout and original local names are not claimed to be byte-for-byte identical to the original implementation.
