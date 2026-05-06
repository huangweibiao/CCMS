# 测试文件批量修复脚本
# 修复后端单元测试中的语法错误

param(
    [switch]$DryRun = $false
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

# 统计信息
$script:fixedFiles = 0
$script:totalReplacements = 0

# 修复函数
function Fix-File {
    param(
        [string]$FilePath,
        [string]$Pattern,
        [string]$Replacement,
        [string]$Description
    )
    
    $content = Get-Content $FilePath -Raw -Encoding UTF8
    $matches = [regex]::Matches($content, $Pattern)
    
    if ($matches.Count -gt 0) {
        if (-not $DryRun) {
            $newContent = $content -replace $Pattern, $Replacement
            Set-Content $FilePath $newContent -Encoding UTF8 -NoNewline
        }
        Write-Info "  $Description : 修复 $($matches.Count) 处"
        $script:totalReplacements += $matches.Count
        return $true
    }
    return $false
}

# 主修复流程
function Main {
    Write-Info "========== 开始修复测试文件 =========="
    
    $testDir = "./backend/src/test/java"
    if (-not (Test-Path $testDir)) {
        throw "测试目录不存在: $testDir"
    }
    
    # 获取所有测试文件
    $testFiles = Get-ChildItem -Path $testDir -Recurse -Filter "*.java"
    Write-Info "找到 $($testFiles.Count) 个测试文件"
    
    foreach ($file in $testFiles) {
        $fileFixed = $false
        $fileName = $file.Name
        
        # 修复1: Page 类型重复声明 (Page Page<T> -> Page<T>)
        if (Fix-File -FilePath $file.FullName `
                   -Pattern "Page Page<" `
                   -Replacement "Page<" `
                   -Description "Page类型重复声明") {
            $fileFixed = $true
        }
        
        # 修复2: 字符串转义错误 "("("("<html> -> " "<html>
        if (Fix-File -FilePath $file.FullName `
                   -Pattern '"\("\("([^"]*)"\)' `
                   -Replacement '"$1"' `
                   -Description "字符串转义错误") {
            $fileFixed = $true
        }
        
        # 修复3: 三重引号 "("("text") -> "text"
        if (Fix-File -FilePath $file.FullName `
                   -Pattern '"\("\("([^"]*)"\)' `
                   -Replacement '"$1"' `
                   -Description "三重引号错误") {
            $fileFixed = $true
        }
        
        # 修复4: 双重引号 "("text") -> "text"
        if (Fix-File -FilePath $file.FullName `
                   -Pattern '"\("([^"]*)"\)' `
                   -Replacement '"$1"' `
                   -Description "双重引号错误") {
            $fileFixed = $true
        }
        
        # 修复5: 缺少分号 (特定模式) - 简化正则
        if (Fix-File -FilePath $file.FullName `
                   -Pattern 'PageImpl<>\([^)]+\)(?!;)' `
                   -Replacement 'PageImpl<>($1);' `
                   -Description "PageImpl缺少分号") {
            $fileFixed = $true
        }
        
        # 修复6: 方法调用缺少分号
        if (Fix-File -FilePath $file.FullName `
                   -Pattern '(authService\.getUserList\([^)]+\))\s*\n' `
                   -Replacement '$1;\n' `
                   -Description "方法调用缺少分号") {
            $fileFixed = $true
        }
        
        if ($fileFixed) {
            $script:fixedFiles++
            Write-Success "修复完成: $fileName"
        }
    }
    
    Write-Info "========== 修复统计 =========="
    Write-Info "修复文件数: $fixedFiles"
    Write-Info "总修复次数: $totalReplacements"
    
    if ($DryRun) {
        Write-Warning "这是模拟运行，实际未修改文件。去掉 -DryRun 参数执行实际修复。"
    } else {
        Write-Success "修复完成！"
    }
}

# 执行主函数
Main
