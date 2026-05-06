import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('预算管理CRUD操作', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('创建预算计划', async ({ page }) => {
    await page.goto('/budgets');
    await TestHelpers.verifyPageTitle(page, '预算管理');
    
    // 点击新建预算
    await page.click('button:has-text("新建预算")');
    
    // 填写预算信息
    const budgetData = TestDataFactory.createBudgetData();
    await page.fill('input[name="name"]', budgetData.name);
    await page.fill('input[name="amount"]', budgetData.amount.toString());
    await TestHelpers.selectOption(page, '[name="period"]', budgetData.period);
    await TestHelpers.selectOption(page, '[name="department"]', budgetData.department);
    await page.fill('textarea[name="description"]', '自动化测试预算');
    
    // 保存预算
    await page.click('button:has-text("保存")');
    
    // 验证创建成功
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 验证预算出现在列表中
    await TestHelpers.verifyTableContains(page, budgetData.name);
  });

  test('查看预算列表', async ({ page }) => {
    await page.goto('/budgets');
    
    // 验证表格显示
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("预算名称")')).toBeVisible();
    await expect(page.locator('th:has-text("预算金额")')).toBeVisible();
    await expect(page.locator('th:has-text("预算周期")')).toBeVisible();
    await expect(page.locator('th:has-text("执行状态")')).toBeVisible();
  });

  test('查看预算详情', async ({ page }) => {
    await page.goto('/budgets');
    
    // 点击查看详情
    const budgetRow = page.locator('.el-table__row').first();
    await budgetRow.locator('button:has-text("详情")').click();
    
    // 验证详情页面
    await expect(page.locator('.budget-detail')).toBeVisible();
    await expect(page.locator('.budget-name')).toBeVisible();
    await expect(page.locator('.budget-amount')).toBeVisible();
    await expect(page.locator('.budget-used')).toBeVisible();
    await expect(page.locator('.budget-remaining')).toBeVisible();
  });

  test('编辑预算信息', async ({ page }) => {
    await page.goto('/budgets');
    
    // 找到可编辑的预算
    const budgetRow = page.locator('.el-table__row').first();
    await budgetRow.locator('button:has-text("编辑")').click();
    
    // 修改预算信息
    const newAmount = '20000';
    await page.fill('input[name="amount"]', newAmount);
    await page.fill('textarea[name="description"]', '自动化测试修改预算');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '预算更新成功');
  });

  test('删除预算计划', async ({ page }) => {
    await page.goto('/budgets');
    
    // 找到可删除的预算（未执行的预算）
    const deletableRow = page.locator('.el-table__row:has-text("未开始")').first();
    
    if (await deletableRow.count() > 0) {
      await deletableRow.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '预算删除成功');
    }
  });

  test('预算状态筛选', async ({ page }) => {
    await page.goto('/budgets');
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '执行中');
    await page.click('button:has-text("筛选")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('执行中');
    }
  });

  test('预算搜索功能', async ({ page }) => {
    await page.goto('/budgets');
    
    // 搜索预算
    const searchText = '测试';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    await TestHelpers.waitForLoading(page);
    
    // 验证搜索结果
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('预算调整申请', async ({ page }) => {
    await page.goto('/budgets');
    
    // 找到需要调整的预算
    const budgetRow = page.locator('.el-table__row').first();
    await budgetRow.locator('button:has-text("调整")').click();
    
    // 填写调整信息
    await page.fill('input[name="adjustAmount"]', '5000');
    await TestHelpers.selectOption(page, '[name="adjustType"]', '增加');
    await page.fill('textarea[name="reason"]', '项目需求增加预算');
    
    // 提交调整申请
    await page.click('button:has-text("提交调整")');
    
    // 验证调整申请成功
    await TestHelpers.verifySuccessMessage(page, '调整申请已提交');
  });

  test('预算执行分析', async ({ page }) => {
    await page.goto('/budgets');
    
    // 进入预算分析页面
    await page.click('button:has-text("预算分析")');
    await TestHelpers.verifyPageTitle(page, '预算分析');
    
    // 验证分析图表
    await expect(page.locator('.analysis-chart')).toBeVisible();
    await expect(page.locator('.budget-usage-chart')).toBeVisible();
    await expect(page.locator('.dept-comparison-chart')).toBeVisible();
  });

  test('预算导出功能', async ({ page }) => {
    await page.goto('/budgets');
    
    // 导出预算列表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('预算超支预警', async ({ page }) => {
    await page.goto('/budgets');
    
    // 查找超预算的项目
    const overspentItems = page.locator('.el-table__row:has(.warning-badge)');
    
    if (await overspentItems.count() > 0) {
      // 验证超支警告显示
      await expect(overspentItems.first().locator('.warning-badge')).toBeVisible();
      
      // 查看超支详情
      await overspentItems.first().locator('button:has-text("详情")').click();
      await expect(page.locator('.overspent-alert')).toBeVisible();
    }
  });

  test('预算权限边界验证', async ({ page }) => {
    // 普通用户登录
    await TestHelpers.userLogin(page);
    
    // 尝试访问预算管理
    await page.goto('/budgets');
    
    // 验证权限控制（可能显示受限视图或错误页面）
    const budgetContent = page.locator('.budget-content');
    if (await budgetContent.isVisible()) {
      // 验证显示的是受限版本
      await expect(page.locator('.limited-view')).toBeVisible();
    }
  });
});
