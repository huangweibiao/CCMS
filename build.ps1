# CCMS企业费控管理系统打包脚本
# 优化版本：参考WMS脚本结构，增强可读性和流程清晰度
# ==========================================

param(
    [string]$Version = "1.0.0",
    [string]$OutputDir = "./dist",
    [switch]$SkipFrontend = $false,
    [switch]$SkipBackend = $false
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "CCMS企业费控管理系统打包脚本" -ForegroundColor Cyan
Write-Host "版本: $Version" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 设置错误处理
$ErrorActionPreference = "Stop"

# 工具变量
$BackendDir = "backend"
$FrontendDir = "frontend"
$StaticDir = "$BackendDir\src\main\resources\static"

# 步骤1: 检查必要工具
Write-Host "[1/5] 正在检查必要工具..." -ForegroundColor Yellow
$tools = @(
    @{ Name = "node"; DisplayName = "Node.js" },
    @{ Name = "npm"; DisplayName = "npm" },
    @{ Name = "mvn"; DisplayName = "Maven" },
    @{ Name = "java"; DisplayName = "Java" }
)

$allToolsAvailable = $true
foreach ($tool in $tools) {
    if ($null -eq (Get-Command $tool.Name -ErrorAction SilentlyContinue)) {
        Write-Host "      ❌ $($tool.DisplayName) 未安装或未添加到PATH" -ForegroundColor Red
        $allToolsAvailable = $false
    } else {
        Write-Host "      ✅ $($tool.DisplayName) 已安装" -ForegroundColor Green
    }
}

if (-not $allToolsAvailable) {
    Write-Host "      依赖检查失败，请安装必要的工具后重试" -ForegroundColor Red
    exit 1
}
Write-Host "      工具检查完成" -ForegroundColor Green
Write-Host ""

# 步骤2: 清除历史打包文件
Write-Host "[2/5] 正在清除历史打包文件..." -ForegroundColor Yellow
if (Test-Path "$FrontendDir\dist") {
    Remove-Item -Recurse -Force "$FrontendDir\dist"
    Write-Host "      已清除前端dist目录" -ForegroundColor Green
}

if (Test-Path "$StaticDir") {
    Remove-Item -Recurse -Force "$StaticDir"
    Write-Host "      已清除后端static目录" -ForegroundColor Green
}

if (Test-Path "$BackendDir\target") {
    Remove-Item -Recurse -Force "$BackendDir\target" -ErrorAction SilentlyContinue
    Write-Host "      已清除后端target目录" -ForegroundColor Green
}

if (Test-Path $OutputDir) {
    Remove-Item -Recurse -Force $OutputDir
    Write-Host "      已清除部署目录" -ForegroundColor Green
}

Write-Host "      清除完成" -ForegroundColor Green
Write-Host ""

# 步骤3: 构建前端项目并复制到后端
if (-not $SkipFrontend) {
    Write-Host "[3/5] 正在构建前端项目并整合到后端..." -ForegroundColor Yellow
    
    # 前端构建
    Set-Location $FrontendDir
    try {
        # 安装依赖
        Write-Host "      安装前端依赖..." -ForegroundColor Cyan
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Host "      前端依赖安装失败！" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        # 执行构建
        Write-Host "      执行前端打包..." -ForegroundColor Cyan
        npm run build
        if ($LASTEXITCODE -ne 0) {
            Write-Host "      前端构建失败！" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        # 检查构建结果
        if (-not (Test-Path "./dist/index.html")) {
            Write-Host "      前端打包结果不完整！" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        $distSize = (Get-ChildItem ./dist -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Host "      前端构建完成 ($([math]::Round($distSize, 2)) MB)" -ForegroundColor Green
        
    } catch {
        Write-Host "      前端构建出错: $_" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    Set-Location ..

    # 复制前端文件到后端
    Write-Host "      复制前端资源到后端..." -ForegroundColor Cyan
    if (-not (Test-Path $StaticDir)) {
        New-Item -ItemType Directory -Path $StaticDir -Force | Out-Null
    }
    Copy-Item -Path "$FrontendDir\dist\*" -Destination $StaticDir -Recurse -Force
    $fileCount = (Get-ChildItem $StaticDir -Recurse -File).Count
    Write-Host "      前端资源整合完成 ($fileCount 个文件)" -ForegroundColor Green
} else {
    Write-Host "[3/5] 跳过前端打包..." -ForegroundColor Yellow
    Write-Host "      使用现有的前端资源" -ForegroundColor Green
}
Write-Host ""

# 步骤4: 打包后端项目（包含前端资源）
if (-not $SkipBackend) {
    Write-Host "[4/5] 正在打包后端项目（包含前端资源）..." -ForegroundColor Yellow
    Set-Location $BackendDir
    try {
        Write-Host "      执行Maven打包..." -ForegroundColor Cyan
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "      后端打包失败！" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        # 查找打包结果
        $jarFiles = Get-ChildItem "./target" -Filter "ccms-backend*.jar" | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*original*" }
        if ($jarFiles.Count -eq 0) {
            Write-Host "      未找到打包结果！" -ForegroundColor Red
            Set-Location ..
            exit 1
        }

        $jarFile = $jarFiles[0].FullName
        $jarSize = (Get-Item $jarFile).Length / 1MB
        Write-Host "      后端打包完成 ($([math]::Round($jarSize, 2)) MB)" -ForegroundColor Green
        
    } catch {
        Write-Host "      后端打包出错: $_" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    Set-Location ..
} else {
    Write-Host "[4/5] 跳过后端打包..." -ForegroundColor Yellow
    Write-Host "      使用现有的后端打包结果" -ForegroundColor Green
}
Write-Host ""

# 步骤5: 创建部署包
Write-Host "[5/5] 正在创建部署包..." -ForegroundColor Yellow

# 创建部署目录
$deployDir = "$OutputDir/ccms-$Version"
New-Item -ItemType Directory -Path $deployDir -Force | Out-Null

# 复制JAR文件
$jarSource = "$BackendDir\target\ccms-backend-$Version.jar"
if (-not (Test-Path $jarSource)) {
    $jarFiles = Get-ChildItem "$BackendDir\target" -Filter "ccms-backend*.jar" | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*original*" }
    if ($jarFiles.Count -eq 0) {
        Write-Host "      未找到JAR文件！" -ForegroundColor Red
        exit 1
    }
    $jarSource = $jarFiles[0].FullName
}

$jarTarget = "$deployDir\ccms-backend-$Version.jar"
Copy-Item -Path $jarSource -Destination $jarTarget -Force
Write-Host "      复制JAR文件" -ForegroundColor Green

# 创建启动脚本
Write-Host "      创建启动脚本..." -ForegroundColor Cyan

# Windows启动脚本
$startScript = @"
@echo off
chcp 65001 >nul
echo ==========================================
echo    CCMS 企业费控管理系统
echo    Version: $Version
echo ==========================================
echo.

set JAVA_OPTS=-Xmx512m -Xms256m -Dfile.encoding=UTF-8
set JAR_FILE=ccms-backend-$Version.jar

 echo 正在启动应用...
 java %JAVA_OPTS% -jar %JAR_FILE% --spring.profiles.active=prod

pause
"@
$startScript | Out-File -FilePath "$deployDir/start.bat" -Encoding UTF8

# Linux启动脚本
$startSh = @"
#!/bin/bash

echo "=========================================="
echo "   CCMS 企业费控管理系统"
echo "   Version: $Version"
echo "=========================================="
echo ""

JAVA_OPTS="-Xmx512m -Xms256m -Dfile.encoding=UTF-8"
JAR_FILE="ccms-backend-$Version.jar"

echo "正在启动应用..."
java \$JAVA_OPTS -jar \$JAR_FILE --spring.profiles.active=prod
"@
$startSh | Out-File -FilePath "$deployDir/start.sh" -Encoding UTF8

# 创建配置目录
$configDir = "$deployDir/config"
New-Item -ItemType Directory -Path $configDir -Force | Out-Null

# 复制默认配置
if (Test-Path "$BackendDir/src/main/resources/application.yml") {
    Copy-Item -Path "$BackendDir/src/main/resources/application.yml" -Destination $configDir -Force
    Write-Host "      复制配置文件" -ForegroundColor Green
}

# 打包为ZIP
$zipFile = "$OutputDir/ccms-$Version.zip"
Compress-Archive -Path $deployDir -DestinationPath $zipFile -Force
$zipSize = (Get-Item $zipFile).Length / 1MB

Write-Host "      部署包创建完成" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "打包完成！" -ForegroundColor Green
Write-Host ""
Write-Host "重要文件信息:" -ForegroundColor Yellow
Write-Host "  部署包: $zipFile ($([math]::Round($zipSize, 2)) MB)" -ForegroundColor Green
Write-Host "  主程序: $jarTarget" -ForegroundColor Green
Write-Host "  部署目录: $deployDir" -ForegroundColor Green
Write-Host ""

Write-Host "运行命令:" -ForegroundColor Yellow
Write-Host "  Windows:  cd '$deployDir' && .\\start.bat" -ForegroundColor White
Write-Host "  Linux:    cd '$deployDir' && chmod +x start.sh && ./start.sh" -ForegroundColor White
Write-Host "  Java命令: java -jar '$jarTarget' --spring.profiles.active=prod" -ForegroundColor White
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan