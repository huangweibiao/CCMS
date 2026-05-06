# 完整自动修复脚本 - 修复所有测试文件错误
param([switch]$DryRun = $false)

$ErrorActionPreference = "Stop"

function Write-Info { param([string]$Message); Write-Host "[INFO] $Message" -ForegroundColor Cyan }
function Write-Success { param([string]$Message); Write-Host "[SUCCESS] $Message" -ForegroundColor Green }
function Write-Warning { param([string]$Message); Write-Host "[WARNING] $Message" -ForegroundColor Yellow }

$script:fixedCount = 0
$script:errorCount = 0

function Fix-JavaFile {
    param([string]$FilePath)
    
    try {
        $content = [System.IO.File]::ReadAllText($FilePath)
        $originalContent = $content
        $fileName = Split-Path $FilePath -Leaf
        
        # 修复1: Page Page<T> -> Page<T>
        $content = $content -replace 'Page\s+Page\s**<', 'Page<'
        
        # 修复2: 方法调用缺少分号 (特定模式)
        $content = $content -replace '(getUserList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getLoanList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getRepaymentList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getExpenseApplyList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getExpenseReimburseList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getAuditLogList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getApprovalList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getFeeTypeList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getOperLogList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getMessageList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getReportTemplateList\([^)]+\))(?!\s*;)', '$1;'
        $content = $content -replace '(getBudgetList\([^)]+\))(?!\s*;)', '$1;'
        
        # 修复3: Page<T> variable = new PageImpl<> -> 添加分号
        $content = $content -replace '(new\s+PageImpl<>\([^)]+\))(?!\s*;)', '$1;'
        
        # 修复4: 赋值语句缺少分号 (通用模式)
        $content = $content -replace '(=\s*new\s+PageImpl<>\([^)]+\))(?!\s*;)', '$1;'
        
        # 修复5: 方法链调用缺少分号
        $content = $content -replace '(\)\s*\)\s*)(?!\s*[;\)])', '$1;'
        
        if ($content -ne $originalContent) {
            if (-not $DryRun) {
                [System.IO.File]::WriteAllText($FilePath, $content, [System.Text.UTF8Encoding]::new($false))
            }
            $script:fixedCount++
            Write-Success "修复: $fileName"
            return $true
        }
    } catch {
        Write-Warning "修复失败 ${FilePath}: $_"
        $script:errorCount++
    }
    return $false
}

# 主流程
Write-Info "========== 开始完整自动修复 =========="

$testDir = "./backend/src/test/java"
$javaFiles = Get-ChildItem -Path $testDir -Recurse -Filter "*.java"

Write-Info "找到 $($javaFiles.Count) 个测试文件"

foreach ($file in $javaFiles) {
    Fix-JavaFile -FilePath $file.FullName
}

Write-Info "========== 修复统计 =========="
Write-Info "修复文件数: $fixedCount"
if ($errorCount -gt 0) {
    Write-Warning "错误数: $errorCount"
}

if ($DryRun) {
    Write-Warning "这是模拟运行，实际未修改文件"
} else {
    Write-Success "修复完成！"
}
