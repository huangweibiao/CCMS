import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('报表统计', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('费用统计报表', async ({ page }) => {
    // 导航到费用统计页面
    await page.goto('/report/expense');
    await TestHelpers.verifyPageTitle(page, '费用统计');
    
    // 验证统计图表显示
    const expenseChart = page.locator('.expense-chart');
    await expect(expenseChart).toBeVisible();
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    
    // 选择统计维度
    await page.selectOption('select[name="dimension"]', '部门');
    await page.click('button:has-text("生成报表")');
    
    // 验证报表生成
    const reportContent = page.locator('.report-content');
    await expect(reportContent).toBeVisible();
    
    // 验证统计数据
    await expect(page.locator('.total-expense')).toBeVisible();
    await expect(page.locator('.avg-expense')).toBeVisible();
    await expect(page.locator('.top-department')).toBeVisible();
  });

  test('预算执行分析报表', async ({ page }) => {
    await page.goto('/report/budget');
    await TestHelpers.verifyPageTitle(page, '预算分析');
    
    // 验证预算分析图表
    const budgetChart = page.locator('.budget-chart');
    await expect(budgetChart).toBeVisible();
    
    // 选择预算周期
    await page.selectOption('select[name="period"]', '2024-Q1');
    await page.click('button:has-text("分析")');
    
    // 验证分析结果
    const analysisResult = page.locator('.analysis-result');
    await expect(analysisResult).toBeVisible();
    
    // 验证关键指标
    await expect(page.locator('.budget-usage-rate')).toBeVisible();
    await expect(page.locator('.overspend-alert')).toBeVisible();
    await expect(page.locator('.department-ranking')).toBeVisible();
  });

  test('部门费用对比报表', async ({ page }) => {
    await page.goto('/report/department-comparison');
    await TestHelpers.verifyPageTitle(page, '部门费用对比');
    
    // 验证对比图表
    const comparisonChart = page.locator('.comparison-chart');
    await expect(comparisonChart).toBeVisible();
    
    // 选择对比时间段
    await page.fill('input[name="compareStart"]', '2024-01-01');
    await page.fill('input[name="compareEnd"]', '2024-03-31');
    await page.click('button:has-text("对比")');
    
    // 验证对比结果
    const compareResult = page.locator('.compare-result');
    await expect(compareResult).toBeVisible();
    
    // 验证部门排名
    await expect(page.locator('.department-rank')).toBeVisible();
  });

  test('借款还款统计报表', async ({ page }) => {
    await page.goto('/report/loan');
    await TestHelpers.verifyPageTitle(page, '借款还款统计');
    
    // 验证借款统计图表
    const loanChart = page.locator('.loan-chart');
    await expect(loanChart).toBeVisible();
    
    // 生成借款报表
    await page.fill('input[name="reportDate"]', '2024-03-31');
    await page.click('button:has-text("生成报表")');
    
    // 验证借款统计
    await expect(page.locator('.total-loan-amount')).toBeVisible();
    await expect(page.locator('.overdue-loan-count')).toBeVisible();
    await expect(page.locator('.repayment-rate')).toBeVisible();
  });

  test('审批效率分析报表', async ({ page }) => {
    await page.goto('/report/approval');
    await TestHelpers.verifyPageTitle(page, '审批效率分析');
    
    // 验证审批分析图表
    const approvalChart = page.locator('.approval-chart');
    await expect(approvalChart).toBeVisible();
    
    // 分析审批效率
    await page.selectOption('select[name="approvalType"]', '费用审批');
    await page.click('button:has-text("分析")');
    
    // 验证效率指标
    await expect(page.locator('.avg-approval-time')).toBeVisible();
    await expect(page.locator('.approval-rate')).toBeVisible();
    await expect(page.locator('.approver-ranking')).toBeVisible();
  });

  test('数据导出功能', async ({ page }) => {
    await page.goto('/report/expense');
    
    // 导出Excel报表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出Excel")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
    await TestHelpers.verifySuccessMessage(page, '报表导出成功');
    
    // 导出PDF报表
    const pdfDownloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出PDF")');
    
    const pdfDownload = await pdfDownloadPromise;
    expect(pdfDownload.suggestedFilename()).toMatch(/\.pdf$/);
    await TestHelpers.verifySuccessMessage(page, 'PDF导出成功');
  });

  test('自定义报表参数', async ({ page }) => {
    await page.goto('/report/custom');
    await TestHelpers.verifyPageTitle(page, '自定义报表');
    
    // 设置报表参数
    await page.check('input[name="showChart"]');
    await page.check('input[name="showTable"]');
    await page.selectOption('select[name="chartType"]', '柱状图');
    
    // 选择数据字段
    await page.check('input[name="field_amount"]');
    await page.check('input[name="field_department"]');
    await page.check('input[name="field_category"]');
    
    // 生成自定义报表
    await page.click('button:has-text("生成报表")');
    
    // 验证自定义报表显示
    const customReport = page.locator('.custom-report');
    await expect(customReport).toBeVisible();
    
    // 验证选择的字段显示
    await expect(page.locator('.amount-column')).toBeVisible();
    await expect(page.locator('.department-column')).toBeVisible();
  });

  test('实时数据刷新', async ({ page }) => {
    await page.goto('/report/expense');
    
    // 验证初始数据
    const initialData = page.locator('.report-data');
    await expect(initialData).toBeVisible();
    
    // 点击刷新按钮
    await page.click('button:has-text("刷新数据")');
    
    // 验证数据刷新提示
    await TestHelpers.verifySuccessMessage(page, '数据已刷新');
    
    // 验证数据重新加载
    await expect(initialData).toBeVisible();
  });

  test('报表权限控制', async ({ page }) => {
    // 验证管理员权限
    await page.goto('/report/expense');
    await expect(page.locator('h1')).toContainText('费用统计');
    
    // 切换到普通用户
    await page.click('.logout-btn');
    await TestHelpers.userLogin(page);
    
    // 普通用户访问报表页面
    await page.goto('/report/expense');
    
    // 验证权限控制（可能显示受限视图或错误页面）
    const reportContent = page.locator('.report-content');
    if (await reportContent.isVisible()) {
      // 验证显示的是受限版本
      await expect(page.locator('.limited-view')).toBeVisible();
    } else {
      // 或者显示权限错误
      await expect(page.locator('.error-page')).toContainText('权限不足');
    }
  });

  test('报表打印功能', async ({ page }) => {
    await page.goto('/report/expense');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    
    // 点击打印按钮
    await page.click('button:has-text("打印")');
    
    // 验证打印对话框或打印样式
    const printDialog = page.locator('.print-dialog');
    if (await printDialog.isVisible()) {
      await expect(printDialog).toBeVisible();
      await page.click('button:has-text("确认打印")');
    } else {
      // 验证打印样式应用
      await expect(page.locator('body')).toHaveClass(/print-mode/);
    }
  });

  test('数据趋势分析', async ({ page }) => {
    await page.goto('/report/trend');
    await TestHelpers.verifyPageTitle(page, '趋势分析');
    
    // 选择趋势分析类型
    await page.selectOption('select[name="trendType"]', '月度趋势');
    await page.fill('input[name="trendPeriod"]', '6');
    
    // 生成趋势分析
    await page.click('button:has-text("分析趋势")');
    
    // 验证趋势图表
    const trendChart = page.locator('.trend-chart');
    await expect(trendChart).toBeVisible();
    
    // 验证趋势数据
    await expect(page.locator('.trend-data')).toBeVisible();
    await expect(page.locator('.growth-rate')).toBeVisible();
    await expect(page.locator('.forecast')).toBeVisible();
  });

  test('报表保存和分享', async ({ page }) => {
    await page.goto('/report/expense');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    
    // 保存报表配置
    await page.click('button:has-text("保存配置")');
    await page.fill('input[name="reportName"]', '测试报表配置');
    await page.click('.modal button:has-text("保存")');
    
    // 验证保存成功
    await TestHelpers.verifySuccessMessage(page, '报表配置已保存');
    
    // 验证分享功能
    await page.click('button:has-text("分享")');
    const shareDialog = page.locator('.share-dialog');
    await expect(shareDialog).toBeVisible();
  });
});