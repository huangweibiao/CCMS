import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('借款管理CRUD操作', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('创建借款申请', async ({ page }) => {
    await page.goto('/loan/list');
    await TestHelpers.verifyPageTitle(page, '我的借款');
    
    // 点击新建借款
    await page.click('button:has-text("新建借款")');
    
    // 填写借款信息
    const loanData = TestDataFactory.createLoanData();
    await page.fill('input[name="purpose"]', loanData.purpose);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    await TestHelpers.selectOption(page, '[name="repaymentPeriod"]', loanData.repaymentPeriod.toString());
    await page.fill('textarea[name="reason"]', '临时资金周转需要');
    
    // 提交借款申请
    await page.click('button:has-text("提交申请")');
    
    // 验证提交成功
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
    // 验证借款出现在列表中
    await TestHelpers.verifyTableContains(page, loanData.purpose);
  });

  test('查看借款列表', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 验证表格显示
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("借款用途")')).toBeVisible();
    await expect(page.locator('th:has-text("借款金额")')).toBeVisible();
    await expect(page.locator('th:has-text("申请时间")')).toBeVisible();
    await expect(page.locator('th:has-text("借款状态")')).toBeVisible();
  });

  test('查看借款详情', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 点击查看详情
    const loanRow = page.locator('.el-table__row').first();
    await loanRow.locator('button:has-text("查看")').click();
    
    // 验证详情页面
    await expect(page.locator('.loan-detail')).toBeVisible();
    await expect(page.locator('.loan-purpose')).toBeVisible();
    await expect(page.locator('.loan-amount')).toBeVisible();
    await expect(page.locator('.loan-status')).toBeVisible();
    await expect(page.locator('.repayment-plan')).toBeVisible();
  });

  test('编辑借款申请', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到可编辑的借款（草稿或待审批状态）
    const editableRow = page.locator('.el-table__row:has-text("草稿"), .el-table__row:has-text("待审批")').first();
    
    if (await editableRow.count() > 0) {
      await editableRow.locator('button:has-text("编辑")').click();
      
      // 修改借款信息
      await page.fill('textarea[name="reason"]', '自动化测试修改借款原因');
      
      // 保存修改
      await page.click('button:has-text("保存")');
      
      // 验证修改成功
      await TestHelpers.verifySuccessMessage(page, '借款申请更新成功');
    }
  });

  test('取消借款申请', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到可取消的借款（待审批状态）
    const cancelableRow = page.locator('.el-table__row:has-text("待审批")').first();
    
    if (await cancelableRow.count() > 0) {
      await cancelableRow.locator('button:has-text("取消")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证取消成功
      await TestHelpers.verifySuccessMessage(page, '借款申请已取消');
    }
  });

  test('借款状态筛选', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '待还款');
    await page.click('button:has-text("筛选")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('待还款');
    }
  });

  test('借款搜索功能', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 搜索借款
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

  test('借款导出功能', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 导出借款记录
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('查看还款计划', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到有还款计划的借款
    const loanRow = page.locator('.el-table__row').first();
    await loanRow.locator('button:has-text("还款计划")').click();
    
    // 验证还款计划页面
    await expect(page.locator('.repayment-schedule')).toBeVisible();
    await expect(page.locator('.schedule-item')).toBeVisible();
  });

  test('借款统计信息', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 验证统计卡片
    await expect(page.locator('.statistics-card')).toBeVisible();
    await expect(page.locator('.total-loan')).toBeVisible();
    await expect(page.locator('.pending-loan')).toBeVisible();
    await expect(page.locator('.overdue-loan')).toBeVisible();
  });

  test('借款分页功能', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 验证分页控件
    const pagination = page.locator('.el-pagination');
    await expect(pagination).toBeVisible();
    
    // 测试翻页
    const nextButton = page.locator('.btn-next');
    if (await nextButton.isEnabled()) {
      await nextButton.click();
      await TestHelpers.waitForLoading(page);
      await expect(page.locator('.el-pagination .active')).toContainText('2');
    }
  });

  test('逾期借款提醒', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 检查逾期借款
    const overdueLoans = page.locator('.el-table__row:has(.overdue-badge)');
    
    if (await overdueLoans.count() > 0) {
      // 验证逾期警告显示
      await expect(overdueLoans.first().locator('.overdue-badge')).toBeVisible();
      await expect(overdueLoans.first().locator('.overdue-days')).toBeVisible();
    }
  });
});
