@echo off
chcp 65001 >nul
title CCMS 开发环境启动

echo ==========================================
echo    CCMS 开发环境快速启动
echo ==========================================
echo.

REM 检查PowerShell
where powershell >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 PowerShell
    pause
    exit /b 1
)

REM 解析参数
set "ARGS="
:parse_args
if "%~1"=="" goto :run
if "%~1"=="--skip-backend" set "ARGS=%ARGS% -SkipBackend"
if "%~1"=="--skip-frontend" set "ARGS=%ARGS% -SkipFrontend"
if "%~1"=="--build" set "ARGS=%ARGS% -BuildFirst"
shift
goto :parse_args

:run
echo [信息] 正在启动开发环境...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0dev-start.ps1" %ARGS%

if %errorlevel% neq 0 (
    echo.
    echo [错误] 启动失败
    pause
    exit /b 1
)

echo.
pause
