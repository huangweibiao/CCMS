# CCMS 完整打包脚本
# 功能：前端打包 → 复制到后端资源目录 → 后端打包 → 生成完整部署包

param(
    [string]$Version = "1.0.0",
    [string]$OutputDir = "./dist",
    [switch]$SkipFrontend = $false,
    [switch]$SkipBackend = $false,
    [switch]$SkipTests = $true
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 颜色定义
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"

# 日志函数
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor $Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor $Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor $Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor $Red
}

# 检查命令是否存在
function Test-Command {
    param([string]$Command)
    $exists = $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
    return $exists
}

# 检查必要工具
function Check-Prerequisites {
    Write-Info "检查必要工具..."
    
    $requiredTools = @(
        @{ Name = "node"; DisplayName = "Node.js" },
        @{ Name = "npm"; DisplayName = "npm" },
        @{ Name = "mvn"; DisplayName = "Maven" },
        @{ Name = "java"; DisplayName = "Java" }
    )
    
    $allGood = $true
    foreach ($tool in $requiredTools) {
        if (Test-Command $tool.Name) {
            Write-Success "$($tool.DisplayName) 已安装"
        } else {
            Write-Error "$($tool.DisplayName) 未安装或未添加到PATH"
            $allGood = $false
        }
    }
    
    if (-not $allGood) {
        throw "缺少必要的工具，请安装后重试"
    }
}

# 前端打包
function Build-Frontend {
    Write-Info "========== 开始前端打包 =========="
    
    $frontendDir = "./frontend"
    if (-not (Test-Path $frontendDir)) {
        throw "前端目录不存在: $frontendDir"
    }
    
    Push-Location $frontendDir
    
    try {
        # 检查node_modules
        if (-not (Test-Path "./node_modules")) {
            Write-Info "安装前端依赖..."
            npm install
            if ($LASTEXITCODE -ne 0) {
                throw "前端依赖安装失败"
            }
        }
        
        # 执行打包
        Write-Info "执行前端打包..."
        npm run build
        if ($LASTEXITCODE -ne 0) {
            throw "前端打包失败"
        }
        
        # 检查打包结果
        if (-not (Test-Path "./dist/index.html")) {
            throw "前端打包结果不完整，缺少index.html"
        }
        
        $distSize = (Get-ChildItem ./dist -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        Write-Success "前端打包完成，大小: $([math]::Round($distSize, 2)) MB"
        
    } finally {
        Pop-Location
    }
}

# 复制前端资源到后端
function Copy-FrontendToBackend {
    Write-Info "========== 复制前端资源到后端 =========="
    
    $sourceDir = "./frontend/dist"
    $targetDir = "./backend/src/main/resources/static"
    
    if (-not (Test-Path $sourceDir)) {
        throw "前端打包目录不存在: $sourceDir"
    }
    
    # 清理旧文件
    if (Test-Path $targetDir) {
        Write-Info "清理旧的前端资源..."
        Remove-Item -Path $targetDir -Recurse -Force
    }
    
    # 创建目标目录
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    
    # 复制文件
    Write-Info "复制前端文件到后端资源目录..."
    Copy-Item -Path "$sourceDir/*" -Destination $targetDir -Recurse -Force
    
    $fileCount = (Get-ChildItem $targetDir -Recurse -File).Count
    Write-Success "前端资源复制完成，共 $fileCount 个文件"
}

# 后端打包
function Build-Backend {
    param([bool]$SkipTests = $true)
    
    Write-Info "========== 开始后端打包 =========="
    
    $backendDir = "./backend"
    if (-not (Test-Path $backendDir)) {
        throw "后端目录不存在: $backendDir"
    }
    
    Push-Location $backendDir
    
    try {
        # 清理旧的target目录
        if (Test-Path "./target") {
            Write-Info "清理旧的构建目录..."
            Remove-Item -Path "./target" -Recurse -Force
        }
        
        # 执行Maven打包
        Write-Info "执行Maven打包..."
        $mvnArgs = @("clean", "package")
        
        if ($SkipTests) {
            $mvnArgs += "--define"
            $mvnArgs += "skipTests=true"
            $mvnArgs += "--define"
            $mvnArgs += "maven.test.skip=true"
        }
        
        & mvn $mvnArgs
        
        if ($LASTEXITCODE -ne 0) {
            throw "后端打包失败"
        }
        
        # 检查打包结果
        $jarFile = "./target/ccms-backend-$Version-exec.jar"
        if (-not (Test-Path $jarFile)) {
            throw "后端打包结果不存在: $jarFile"
        }
        
        $jarSize = (Get-Item $jarFile).Length / 1MB
        Write-Success "后端打包完成，大小: $([math]::Round($jarSize, 2)) MB"
        
    } finally {
        Pop-Location
    }
}

# 创建部署包
function Create-DeploymentPackage {
    Write-Info "========== 创建部署包 =========="
    
    # 创建输出目录
    $deployDir = "$OutputDir/ccms-$Version"
    if (Test-Path $deployDir) {
        Remove-Item -Path $deployDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $deployDir -Force | Out-Null
    
    # 复制JAR文件
    $jarSource = "./backend/target/ccms-backend-$Version-exec.jar"
    $jarTarget = "$deployDir/ccms-backend-$Version.jar"
    Copy-Item -Path $jarSource -Destination $jarTarget -Force
    Write-Info "复制JAR文件: $jarTarget"
    
    # 创建启动脚本
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
    Write-Info "创建启动脚本: start.bat"
    
    # 创建Linux启动脚本
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
    Write-Info "创建Linux启动脚本: start.sh"
    
    # 创建配置文件目录
    $configDir = "$deployDir/config"
    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
    
    # 复制配置文件
    $configSource = "./backend/src/main/resources/application.yml"
    if (Test-Path $configSource) {
        Copy-Item -Path $configSource -Destination $configDir -Force
        Write-Info "复制配置文件"
    }
    
    # 创建README
    $readme = @"
# CCMS 企业费控管理系统 v$Version

## 部署说明

### 系统要求
- Java 21 或更高版本
- MySQL 8.0 或更高版本
- Redis 6.0 或更高版本（可选）

### 快速启动

#### Windows
```
双击 start.bat
```

#### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

### 配置说明

1. 修改 `config/application.yml` 配置数据库连接
2. 配置JWT密钥和其他安全参数
3. 配置邮件服务器（可选）

### 默认端口
- 应用端口: 8080
- 数据库: 3306
- Redis: 6379

### 访问地址
- 前端: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## 目录结构

```
ccms-$Version/
├── ccms-backend-$Version.jar  # 主程序
├── start.bat                  # Windows启动脚本
├── start.sh                   # Linux启动脚本
├── config/                    # 配置文件目录
│   └── application.yml
└── README.md                  # 本文件
```

## 技术支持

如有问题，请联系技术支持团队。
"@
    
    $readme | Out-File -FilePath "$deployDir/README.md" -Encoding UTF8
    Write-Info "创建README文件"
    
    # 打包为ZIP
    $zipFile = "$OutputDir/ccms-$Version.zip"
    if (Test-Path $zipFile) {
        Remove-Item -Path $zipFile -Force
    }
    
    Compress-Archive -Path $deployDir -DestinationPath $zipFile -Force
    
    $zipSize = (Get-Item $zipFile).Length / 1MB
    Write-Success "部署包创建完成: $zipFile"
    Write-Info "部署包大小: $([math]::Round($zipSize, 2)) MB"
}

# 打印构建信息
function Show-BuildInfo {
    Write-Info "========== 构建信息 =========="
    Write-Info "版本: $Version"
    Write-Info "输出目录: $OutputDir"
    Write-Info "跳过前端: $SkipFrontend"
    Write-Info "跳过后端: $SkipBackend"
    Write-Info "跳过测试: $SkipTests"
    Write-Info "============================"
}

# 主函数
function Main {
    $startTime = Get-Date
    
    try {
        Show-BuildInfo
        
        # 检查前提条件
        Check-Prerequisites
        
        # 前端打包
        if (-not $SkipFrontend) {
            Build-Frontend
            Copy-FrontendToBackend
        } else {
            Write-Warning "跳过前端打包"
        }
        
        # 后端打包
        if (-not $SkipBackend) {
            Build-Backend -SkipTests $SkipTests
        } else {
            Write-Warning "跳过后端打包"
        }
        
        # 创建部署包
        Create-DeploymentPackage
        
        $endTime = Get-Date
        $duration = $endTime - $startTime
        
        Write-Success "========== 构建完成 =========="
        Write-Info "总耗时: $([math]::Round($duration.TotalMinutes, 2)) 分钟"
        Write-Info "部署包位置: $OutputDir/ccms-$Version.zip"
        Write-Success "=============================="
        
    } catch {
        Write-Error "构建失败: $_"
        exit 1
    }
}

# 执行主函数
Main
