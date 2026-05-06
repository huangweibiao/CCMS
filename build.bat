@echo off
chcp 65001 >nul
title CCMS 完整打包脚本

echo ==========================================
echo    CCMS 企业费控管理系统 - 完整打包脚本
echo ==========================================
echo.

REM 检查PowerShell
where powershell >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 PowerShell，请安装后重试
    pause
    exit /b 1
)

REM 执行PowerShell脚本
echo [信息] 正在启动打包流程...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0build.ps1" %*

if %errorlevel% neq 0 (
    echo.
    echo [错误] 打包失败
    pause
    exit /b 1
)

echo.
pause
