import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('预算管理', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('创建新预算计划', async ({ page }) => {
    // 导航到预算管理页面
    await page.goto('/budget/management');
    await TestHelpers.verifyPageTitle(page, '预算管理');
    
    // 点击新建预算按钮
    await page.click('button:has-text("新建预算")');
    
    // 填写预算信息
    const budgetData = TestDataFactory.createBudgetData();
    await page.fill('input[name="name"]', budgetData.name);
    await page.fill('input[name="amount"]', budgetData.amount.toString());
    await page.selectOption('select[name="period"]', budgetData.period);
    await page.selectOption('select[name="department"]', budgetData.department);
    
    // 保存预算
    await page.click('button:has-text("保存")');
    
    // 验证保存成功
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 验证预算出现在列表中
    await TestHelpers.verifyTableContains(page, budgetData.name);
  });

  test('编辑预算信息', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 找到第一个可编辑的预算
    const budgetRow = page.locator('.budget-item').first();
    await budgetRow.locator('button:has-text("编辑")').click();
    
    // 修改预算金额
    const newAmount = '15000';
    await page.fill('input[name="amount"]', newAmount);
    await page.fill('textarea[name="remark"]', '自动化测试修改预算金额');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '预算更新成功');
    
    // 验证金额更新
    await TestHelpers.verifyTableContains(page, newAmount);
  });

  test('预算调整申请', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 找到预算申请调整
    const budgetRow = page.locator('.budget-item').first();
    await budgetRow.locator('button:has-text("调整")').click();
    
    // 填写调整信息
    await page.fill('input[name="adjustAmount"]', '2000');
    await page.selectOption('select[name="adjustType"]', '增加');
    await page.fill('textarea[name="adjustReason"]', '项目需求增加预算');
    
    // 提交调整申请
    await page.click('button:has-text("提交调整")');
    
    // 验证申请成功
    await TestHelpers.verifySuccessMessage(page, '预算调整申请已提交');
  });

  test('预算执行分析', async ({ page }) => {
    await page.goto('/budget/analysis');
    await TestHelpers.verifyPageTitle(page, '预算分析');
    
    // 验证图表显示
    const chart = page.locator('.budget-chart');
    await expect(chart).toBeVisible();
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    await page.click('button:has-text("分析")');
    
    // 验证分析结果显示
    const analysisResult = page.locator('.analysis-result');
    await expect(analysisResult).toBeVisible();
    
    // 验证关键指标
    await expect(page.locator('.budget-usage-rate')).toBeVisible();
    await expect(page.locator('.department-ranking')).toBeVisible();
  });

  test('预算超支预警', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 查找超预算项目
    const overspentItems = page.locator('.budget-item.overspent');
    if (await overspentItems.count() > 0) {
      // 验证超支预警显示
      await expect(overspentItems.first().locator('.warning-badge')).toBeVisible();
      
      // 查看超支详情
      await overspentItems.first().locator('button:has-text("详情")').click();
      
      // 验证超支详情页面
      await TestHelpers.verifyPageTitle(page, '预算详情');
      await expect(page.locator('.overspent-details')).toBeVisible();
    }
  });

  test('预算导出功能', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 点击导出按钮
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出预算")');
    
    // 验证下载开始
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
    
    // 验证导出成功提示
    await TestHelpers.verifySuccessMessage(page, '预算导出成功');
  });

  test('预算批量操作', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 选择多个预算
    const checkboxes = page.locator('.budget-checkbox');
    await checkboxes.first().click();
    await checkboxes.nth(1).click();
    
    // 批量删除操作
    await page.click('button:has-text("批量删除")');
    await TestHelpers.confirmDialog(page);
    
    // 验证删除成功
    await TestHelpers.verifySuccessMessage(page, '批量删除成功');
  });

  test('预算搜索和筛选', async ({ page }) => {
    await page.goto('/budget/management');
    
    // 按部门筛选
    await page.selectOption('select[name="department"]', '技术部');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    const filteredItems = page.locator('.budget-item');
    if (await filteredItems.count() > 0) {
      await expect(filteredItems.first()).toContainText('技术部');
    }
    
    // 按状态筛选
    await page.selectOption('select[name="status"]', '执行中');
    await page.click('button:has-text("筛选")');
    
    // 验证状态筛选
    const activeItems = page.locator('.budget-item.active');
    await expect(activeItems.first().locator('.status-badge')).toHaveText('执行中');
    
    // 搜索功能
    const searchText = '测试';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    
    // 验证搜索结果
    await TestHelpers.verifyTableContains(page, searchText);
  });
});