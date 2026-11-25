@echo off
setlocal
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set. Please set JAVA_HOME to your JDK 21+ directory.
    exit /b 1
)
where python >nul 2>nul
if %errorlevel%==0 (
    python "%~dp0build.py"
) else (
    py -3 "%~dp0build.py"
)
endlocal
