import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('还款管理', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('提交还款申请', async ({ page }) => {
    // 导航到还款申请页面
    await page.goto('/repayment/apply');
    await TestHelpers.verifyPageTitle(page, '还款申请');
    
    // 选择待还款的借款
    await TestHelpers.selectOption(page, '.el-select', '测试借款');
    
    // 填写还款信息
    const repaymentData = TestDataFactory.createRepaymentData();
    await page.fill('input[name="amount"]', repaymentData.amount.toString());
    await TestHelpers.selectOption(page, '[name="paymentMethod"]', repaymentData.paymentMethod);
    await page.fill('input[name="paymentDate"]', repaymentData.paymentDate);
    await page.fill('textarea[name="remark"]', repaymentData.remark);
    
    // 提交还款申请
    await page.click('button:has-text("提交还款")');
    
    // 验证提交成功
    await TestHelpers.verifySuccessMessage(page, '还款申请提交成功');
    
    // 验证还款记录出现在列表中
    await page.goto('/repayment/list');
    await TestHelpers.verifyTableContains(page, repaymentData.remark);
  });

  test('查看还款记录列表', async ({ page }) => {
    await page.goto('/repayment/list');
    await TestHelpers.verifyPageTitle(page, '还款记录');
    
    // 验证还款记录表格显示
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列存在
    await expect(page.locator('th:has-text("还款金额")')).toBeVisible();
    await expect(page.locator('th:has-text("还款日期")')).toBeVisible();
    await expect(page.locator('th:has-text("还款状态")')).toBeVisible();
    
    // 验证分页控件
    const pagination = page.locator('.el-pagination');
    await expect(pagination).toBeVisible();
  });

  test('查看还款详情', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 找到还款记录
    const repaymentRow = page.locator('.el-table__row').first();
    await repaymentRow.locator('button:has-text("查看")').click();
    
    // 验证详情页面加载
    await TestHelpers.verifyPageTitle(page, '还款详情');
    
    // 验证还款信息显示完整
    await expect(page.locator('.repayment-amount')).toBeVisible();
    await expect(page.locator('.repayment-date')).toBeVisible();
    await expect(page.locator('.repayment-status')).toBeVisible();
    await expect(page.locator('.loan-info')).toBeVisible();
  });

  test('还款记录筛选', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '已确认');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    await TestHelpers.waitForLoading(page);
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('已确认');
    }
    
    // 按时间范围筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    await TestHelpers.waitForLoading(page);
    await expect(page.locator('.el-table')).toBeVisible();
  });

  test('还款记录搜索', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 搜索特定还款记录
    const searchText = '测试';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    
    // 验证搜索结果
    await TestHelpers.waitForLoading(page);
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('还款导出功能', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 点击导出按钮
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    // 验证下载开始
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    
    // 验证导出成功提示
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('还款统计信息', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 验证统计卡片显示
    await expect(page.locator('.statistics-card')).toBeVisible();
    
    // 验证关键指标
    await expect(page.locator('.total-repayment')).toBeVisible();
    await expect(page.locator('.pending-repayment')).toBeVisible();
    await expect(page.locator('.overdue-count')).toBeVisible();
  });

  test('还款审批流程(审批员视角)', async ({ page }) => {
    // 先创建还款申请
    await TestHelpers.userLogin(page);
    await page.goto('/repayment/apply');
    const repaymentData = TestDataFactory.createRepaymentData();
    await page.fill('input[name="amount"]', repaymentData.amount.toString());
    await page.fill('textarea[name="remark"]', repaymentData.remark);
    await page.click('button:has-text("提交还款")');
    await TestHelpers.verifySuccessMessage(page, '还款申请提交成功');
    
    // 审批员登录
    await TestHelpers.approverLogin(page);
    
    // 导航到审批页面
    await page.goto('/approval/pending');
    await TestHelpers.verifyPageTitle(page, '待办审批');
    
    // 找到还款审批
    const approvalItem = page.locator('.approval-item:has-text("还款")').first();
    if (await approvalItem.count() > 0) {
      await approvalItem.locator('button:has-text("审批")').click();
      
      // 审批通过
      await page.click('button:has-text("通过")');
      await page.fill('textarea[name="comment"]', '还款金额正确，予以通过');
      await TestHelpers.confirmDialog(page);
      
      // 验证审批成功
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
  });

  test('部分还款功能', async ({ page }) => {
    await page.goto('/repayment/apply');
    
    // 选择借款
    await TestHelpers.selectOption(page, '.el-select', '测试借款');
    
    // 输入部分还款金额
    await page.fill('input[name="amount"]', '500');
    await page.fill('textarea[name="remark"]', '部分还款测试');
    
    // 提交还款
    await page.click('button:has-text("提交还款")');
    await TestHelpers.verifySuccessMessage(page, '还款申请提交成功');
    
    // 验证还款状态为部分还款
    await page.goto('/repayment/list');
    await TestHelpers.verifyTableContains(page, '部分还款');
  });

  test('还款记录分页', async ({ page }) => {
    await page.goto('/repayment/list');
    
    // 验证分页控件
    const pagination = page.locator('.el-pagination');
    await expect(pagination).toBeVisible();
    
    // 如果有多页，测试翻页
    const nextButton = page.locator('.btn-next');
    if (await nextButton.isEnabled()) {
      await nextButton.click();
      await TestHelpers.waitForLoading(page);
      
      // 验证页面切换
      await expect(page.locator('.el-pagination .active')).toContainText('2');
    }
  });

  test('还款取消功能', async ({ page }) => {
    // 创建还款申请
    await page.goto('/repayment/apply');
    const repaymentData = TestDataFactory.createRepaymentData();
    await page.fill('input[name="amount"]', repaymentData.amount.toString());
    await page.fill('textarea[name="remark"]', repaymentData.remark);
    await page.click('button:has-text("提交还款")');
    await TestHelpers.verifySuccessMessage(page, '还款申请提交成功');
    
    // 找到待处理的还款申请
    await page.goto('/repayment/list');
    const pendingRow = page.locator('.el-table__row:has-text("待处理")').first();
    
    if (await pendingRow.count() > 0) {
      // 点击取消按钮
      await pendingRow.locator('button:has-text("取消")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证取消成功
      await TestHelpers.verifySuccessMessage(page, '还款申请已取消');
    }
  });
});
