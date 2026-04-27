import { test, expect } from '@playwright/test';

test.describe('审批流程', () => {
  test('审批人员登录并审批申请', async ({ page }) => {
    // 审批人员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
    
    // 导航到审批管理页面
    await page.goto('/approval/management');
    
    // 验证审批页面加载
    await expect(page.locator('h1')).toContainText('审批管理');
    
    // 查看待审批列表
    const pendingList = page.locator('.pending-approvals');
    await expect(pendingList).toBeVisible();
    
    // 查看第一个待审批申请详情
    const firstApplication = page.locator('.approval-item').first();
    await firstApplication.locator('button:has-text("查看详情")').click();
    
    // 审批通过
    await page.click('button:has-text("通过")');
    
    // 填写审批意见
    await page.fill('textarea[name="comment"]', '申请符合公司规定，予以通过');
    await page.click('.modal button:has-text("确认通过")');
    
    // 验证审批成功
    await expect(page.locator('.success-message')).toContainText('审批成功');
    
    // 验证申请状态更新
    await expect(firstApplication.locator('.status-badge')).toHaveText('已通过');
  });

  test('审批人员驳回复申请', async ({ page }) => {
    // 审批人员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    
    // 导航到审批管理页面
    await page.goto('/approval/management');
    
    // 查找需要驳回的申请
    const applicationToReject = page.locator('.approval-item:has-text("高额申请")').first();
    
    // 查看详情
    await applicationToReject.locator('button:has-text("查看详情")').click();
    
    // 驳回申请
    await page.click('button:has-text("驳回")');
    
    // 选择驳回原因
    await page.selectOption('select[name="rejectReason"]', '金额超限');
    await page.fill('textarea[name="rejectComment"]', '申请金额超出预算限额');
    await page.click('.modal button:has-text("确认驳回")');
    
    // 验证驳回成功
    await expect(page.locator('.success-message')).toContainText('驳回成功');
  });

  test('批量审批操作', async ({ page }) => {
    // 审批人员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    
    // 导航到审批管理页面
    await page.goto('/approval/management');
    
    // 全选当前页申请
    await page.click('.approval-select-all');
    
    // 批量通过
    await page.click('button:has-text("批量通过")');
    
    // 填写批量审批意见
    await page.fill('textarea[name="batchComment"]', '批量审批通过');
    await page.click('.modal button:has-text("确认批量操作")');
    
    // 验证批量操作成功
    await expect(page.locator('.success-message')).toContainText('批量处理成功');
    
    // 验证所有选择项状态更新
    const selectedItems = page.locator('.approval-item.selected');
    await expect(selectedItems.locator('.status-badge')).toHaveText('已通过');
  });

  test('审批历史查询', async ({ page }) => {
    // 审批人员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    
    // 导航到审批历史页面
    await page.goto('/approval/history');
    
    // 验证历史页面加载
    await expect(page.locator('h1')).toContainText('审批历史');
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    await page.click('button:has-text("查询")');
    
    // 按状态筛选
    await page.selectOption('select[name="status"]', 'approved');
    await page.click('button:has-text("查询")');
    
    // 验证筛选结果
    const historyItems = page.locator('.approval-history-item');
    await expect(historyItems).toHaveCount(5);
    
    // 验证历史记录包含必要信息
    await expect(historyItems.first().locator('.application-title')).toBeVisible();
    await expect(historyItems.first().locator('.approval-action')).toBeVisible();
    await expect(historyItems.first().locator('.approval-time')).toBeVisible();
  });
});