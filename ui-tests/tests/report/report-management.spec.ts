import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('报表统计功能', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('费用统计报表', async ({ page }) => {
    await page.goto('/reports/expense');
    await TestHelpers.verifyPageTitle(page, '费用统计');
    
    // 验证报表组件
    await expect(page.locator('.report-container')).toBeVisible();
    await expect(page.locator('.expense-chart')).toBeVisible();
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 验证报表数据
    await expect(page.locator('.report-data')).toBeVisible();
    await expect(page.locator('.total-amount')).toBeVisible();
  });

  test('预算执行报表', async ({ page }) => {
    await page.goto('/reports/budget');
    await TestHelpers.verifyPageTitle(page, '预算执行报表');
    
    // 验证预算报表组件
    await expect(page.locator('.budget-report')).toBeVisible();
    await expect(page.locator('.budget-usage-chart')).toBeVisible();
    
    // 选择预算周期
    await TestHelpers.selectOption(page, '[name="period"]', '2024-Q1');
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 验证报表数据
    await expect(page.locator('.budget-data')).toBeVisible();
  });

  test('部门费用对比报表', async ({ page }) => {
    await page.goto('/reports/department');
    await TestHelpers.verifyPageTitle(page, '部门费用对比');
    
    // 验证对比报表组件
    await expect(page.locator('.comparison-report')).toBeVisible();
    await expect(page.locator('.dept-comparison-chart')).toBeVisible();
    
    // 选择对比时间
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    await page.click('button:has-text("对比分析")');
    await TestHelpers.waitForLoading(page);
    
    // 验证对比结果
    await expect(page.locator('.comparison-result')).toBeVisible();
    await expect(page.locator('.dept-ranking')).toBeVisible();
  });

  test('借款还款统计报表', async ({ page }) => {
    await page.goto('/reports/loan');
    await TestHelpers.verifyPageTitle(page, '借款还款统计');
    
    // 验证借款报表组件
    await expect(page.locator('.loan-report')).toBeVisible();
    await expect(page.locator('.loan-chart')).toBeVisible();
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 验证统计数据
    await expect(page.locator('.loan-stats')).toBeVisible();
    await expect(page.locator('.total-loan')).toBeVisible();
    await expect(page.locator('.repayment-rate')).toBeVisible();
  });

  test('报表导出功能', async ({ page }) => {
    await page.goto('/reports/expense');
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 导出Excel
    const excelPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出Excel")');
    const excelDownload = await excelPromise;
    expect(excelDownload.suggestedFilename()).toMatch(/\.(xlsx|xls)$/);
    
    // 导出PDF
    const pdfPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出PDF")');
    const pdfDownload = await pdfPromise;
    expect(pdfDownload.suggestedFilename()).toMatch(/\.pdf$/);
  });

  test('报表打印功能', async ({ page }) => {
    await page.goto('/reports/expense');
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 点击打印按钮
    await page.click('button:has-text("打印")');
    
    // 验证打印对话框
    const printDialog = page.locator('.print-dialog');
    await expect(printDialog).toBeVisible();
    
    // 关闭打印对话框
    await page.click('.print-dialog .close-btn');
  });

  test('报表数据刷新', async ({ page }) => {
    await page.goto('/reports/expense');
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 点击刷新按钮
    await page.click('button:has-text("刷新")');
    await TestHelpers.waitForLoading(page);
    
    // 验证数据刷新成功
    await expect(page.locator('.report-data')).toBeVisible();
    await TestHelpers.verifySuccessMessage(page, '数据已刷新');
  });

  test('报表权限控制', async ({ page }) => {
    // 普通用户登录
    await TestHelpers.userLogin(page);
    
    // 访问报表页面
    await page.goto('/reports/expense');
    
    // 验证权限控制
    const reportContent = page.locator('.report-content');
    if (await reportContent.isVisible()) {
      // 验证显示的是受限版本
      await expect(page.locator('.limited-view')).toBeVisible();
    }
  });

  test('自定义报表参数', async ({ page }) => {
    await page.goto('/reports/expense');
    
    // 设置自定义参数
    await page.check('input[name="showChart"]');
    await page.check('input[name="showTable"]');
    await TestHelpers.selectOption(page, '[name="chartType"]', '柱状图');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 验证自定义报表显示
    await expect(page.locator('.custom-report')).toBeVisible();
  });

  test('报表保存和分享', async ({ page }) => {
    await page.goto('/reports/expense');
    await page.click('button:has-text("生成报表")');
    await TestHelpers.waitForLoading(page);
    
    // 保存报表配置
    await page.click('button:has-text("保存配置")');
    await page.fill('input[name="configName"]', '测试报表配置');
    await TestHelpers.confirmDialog(page);
    
    // 验证保存成功
    await TestHelpers.verifySuccessMessage(page, '配置已保存');
    
    // 分享报表
    await page.click('button:has-text("分享")');
    const shareDialog = page.locator('.share-dialog');
    await expect(shareDialog).toBeVisible();
  });
});
