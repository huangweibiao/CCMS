# CCMS企业费控管理系统打包脚本
# ==========================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "CCMS企业费控管理系统打包脚本" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 设置变量
$BackendDir = "backend"
$FrontendDir = "frontend"
$StaticDir = "$BackendDir\src\main\resources\static"

# 步骤1: 清除历史打包文件
Write-Host "[1/5] 正在清除历史打包文件..." -ForegroundColor Yellow
if (Test-Path "$FrontendDir\dist") {
    Remove-Item -Recurse -Force "$FrontendDir\dist"
    Write-Host "      已清除前端dist目录" -ForegroundColor Green
}
if (Test-Path "$BackendDir\target") {
    # 单独删除target下的文件，保留static目录供后续使用
    Get-ChildItem -Path "$BackendDir\target" -Recurse -Force | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item -Recurse -Force "$BackendDir\target" -ErrorAction SilentlyContinue
    Write-Host "      已清除后端target目录" -ForegroundColor Green
}
Write-Host "      清除完成" -ForegroundColor Green
Write-Host ""

# 步骤2: 构建前端项目
Write-Host "[2/5] 正在构建前端项目..." -ForegroundColor Yellow
Set-Location $FrontendDir
try {
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "      前端依赖安装失败！" -ForegroundColor Red
        Set-Location ..
        exit 1
    }

    # npm run build
    Write-Host "      执行前端打包..." -ForegroundColor Cyan
    node -e "
    const { execSync } = require('child_process');
    try {
      execSync('npm run build', { stdio: 'inherit' });
    } catch (error) {
      console.error('前端构建失败:', error.message);
      process.exit(1);
    }
    "
} catch {
    Write-Host "      前端构建出错: $_" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..
Write-Host "      前端构建完成" -ForegroundColor Green
Write-Host ""

# 步骤3: 打包后端项目（先打包，再复制前端文件）
Write-Host "[3/5] 正在打包后端项目..." -ForegroundColor Yellow
Set-Location $BackendDir
try {
    Write-Host "      执行Maven打包..." -ForegroundColor Cyan
    # 使用cmd执行mvn命令，避免PowerShell参数解析问题
    cmd /c "mvn clean package -DskipTests"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "      后端打包失败！" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
} catch {
    Write-Host "      后端打包出错: $_" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..
Write-Host "      后端打包完成" -ForegroundColor Green
Write-Host ""

# 步骤4: 复制前端打包文件到后端static目录
Write-Host "[4/5] 正在复制前端文件到后端..." -ForegroundColor Yellow
if (-not (Test-Path $StaticDir)) {
    New-Item -ItemType Directory -Path $StaticDir -Force | Out-Null
}
Copy-Item -Path "$FrontendDir\dist\*" -Destination $StaticDir -Recurse -Force
Write-Host "      复制完成" -ForegroundColor Green
Write-Host ""

# 步骤5: 完成
Write-Host "[5/5] 打包完成！" -ForegroundColor Green
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
$JarFile = "$BackendDir\target\ccms-backend-1.0.0.jar"
if (Test-Path $JarFile) {
    $JarSize = (Get-Item $JarFile).Length / 1MB
    Write-Host "输出文件: $JarFile $([math]::Round($JarSize, 2)) MB)" -ForegroundColor Green
}
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "运行命令:" -ForegroundColor Yellow
Write-Host "  开发环境: java -jar $BackendDir\target\ccms-backend-1.0.0.jar --spring.profiles.active=dev"
Write-Host "  生产环境: java -jar $BackendDir\target\ccms-backend-1.0.0.jar --spring.profiles.active=prod"
Write-Host "  指定端口: java -jar $BackendDir\target\ccms-backend-1.0.0.jar --server.port=8280 --spring.profiles.active=dev"
Write-Host ""
Write-Host "快速启动开发环境:" -ForegroundColor Yellow
Write-Host "  java -jar $BackendDir\target\ccms-backend-1.0.0.jar --server.port=8280 --spring.profiles.active=dev"
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan