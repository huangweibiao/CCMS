# CCMS 开发环境快速启动脚本
# 功能：同时启动后端和前端开发服务器

param(
    [switch]$SkipBackend = $false,
    [switch]$SkipFrontend = $false,
    [switch]$BuildFirst = $false
)

$ErrorActionPreference = "Stop"

# 颜色定义
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"

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

# 启动后端
function Start-Backend {
    Write-Info "启动后端开发服务器..."
    
    Push-Location ./backend
    
    try {
        if ($BuildFirst) {
            Write-Info "先编译后端代码..."
            mvn clean compile -DskipTests
            if ($LASTEXITCODE -ne 0) {
                throw "后端编译失败"
            }
        }
        
        # 在新窗口中启动Spring Boot
        Start-Process powershell -ArgumentList "-Command", "cd '$(Get-Location)'; mvn spring-boot:run -Dspring-boot.run.profiles=dev" -WindowStyle Normal
        
        Write-Success "后端启动命令已发送"
        Write-Info "后端API地址: http://localhost:8080"
        
    } finally {
        Pop-Location
    }
}

# 启动前端
function Start-Frontend {
    Write-Info "启动前端开发服务器..."
    
    Push-Location ./frontend
    
    try {
        # 检查node_modules
        if (-not (Test-Path "./node_modules")) {
            Write-Info "安装前端依赖..."
            npm install
            if ($LASTEXITCODE -ne 0) {
                throw "前端依赖安装失败"
            }
        }
        
        # 在新窗口中启动Vite
        Start-Process powershell -ArgumentList "-Command", "cd '$(Get-Location)'; npm run dev" -WindowStyle Normal
        
        Write-Success "前端启动命令已发送"
        Write-Info "前端地址: http://localhost:5173"
        
    } finally {
        Pop-Location
    }
}

# 主函数
function Main {
    Write-Info "========== CCMS 开发环境启动 =========="
    
    try {
        if (-not $SkipBackend) {
            Start-Backend
        } else {
            Write-Warning "跳过后端启动"
        }
        
        if (-not $SkipFrontend) {
            Start-Frontend
        } else {
            Write-Warning "跳过前端启动"
        }
        
        Write-Success "========== 启动命令已发送 =========="
        Write-Info "请查看新打开的窗口了解启动进度"
        Write-Info ""
        Write-Info "访问地址:"
        if (-not $SkipFrontend) {
            Write-Info "  前端: http://localhost:5173"
        }
        if (-not $SkipBackend) {
            Write-Info "  后端API: http://localhost:8080"
        }
        Write-Success "===================================="
        
    } catch {
        Write-Error "启动失败: $_"
        exit 1
    }
}

Main
