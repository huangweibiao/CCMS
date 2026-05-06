import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('审批管理功能', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.approverLogin(page);
  });

  test('查看待办审批列表', async ({ page }) => {
    await page.goto('/approval/pending');
    await TestHelpers.verifyPageTitle(page, '待办审批');
    
    // 验证待办列表显示
    const todoList = page.locator('.todo-list');
    await expect(todoList).toBeVisible();
    
    // 验证列表项
    const todoItems = todoList.locator('.todo-item');
    if (await todoItems.count() > 0) {
      await expect(todoItems.first()).toBeVisible();
      await expect(todoItems.first().locator('.item-title')).toBeVisible();
      await expect(todoItems.first().locator('.item-status')).toBeVisible();
    }
  });

  test('查看已办审批列表', async ({ page }) => {
    await page.goto('/approval/history');
    await TestHelpers.verifyPageTitle(page, '已办审批');
    
    // 验证已办列表显示
    const historyList = page.locator('.history-list');
    await expect(historyList).toBeVisible();
    
    // 验证历史记录
    const historyItems = historyList.locator('.history-item');
    if (await historyItems.count() > 0) {
      await expect(historyItems.first()).toBeVisible();
    }
  });

  test('审批通过操作', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      
      // 查看详情
      await expect(page.locator('.approval-detail')).toBeVisible();
      
      // 点击通过
      await page.click('button:has-text("通过")');
      
      // 填写审批意见
      await page.fill('textarea[name="comment"]', '申请符合规定，予以通过');
      await TestHelpers.confirmDialog(page);
      
      // 验证审批成功
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
  });

  test('审批驳回操作', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      
      // 点击驳回
      await page.click('button:has-text("驳回")');
      
      // 选择驳回原因
      await TestHelpers.selectOption(page, '[name="rejectReason"]', '金额不符');
      await page.fill('textarea[name="comment"]', '申请金额超出预算限额');
      await TestHelpers.confirmDialog(page);
      
      // 验证驳回成功
      await TestHelpers.verifySuccessMessage(page, '审批已驳回');
    }
  });

  test('审批转交操作', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      
      // 点击转交
      await page.click('button:has-text("转交")');
      
      // 选择转交人
      await TestHelpers.selectOption(page, '[name="transferTo"]', '其他审批员');
      await page.fill('textarea[name="comment"]', '请协助审批');
      await TestHelpers.confirmDialog(page);
      
      // 验证转交成功
      await TestHelpers.verifySuccessMessage(page, '审批已转交');
    }
  });

  test('批量审批操作', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 选择多个待审批项
    const checkboxes = page.locator('.el-checkbox');
    const count = await checkboxes.count();
    if (count >= 3) {
      await checkboxes.nth(1).click();
      await checkboxes.nth(2).click();
      
      // 批量通过
      await page.click('button:has-text("批量通过")');
      await page.fill('textarea[name="batchComment"]', '批量审批通过');
      await TestHelpers.confirmDialog(page);
      
      // 验证批量操作成功
      await TestHelpers.verifySuccessMessage(page, '批量审批成功');
    }
  });

  test('审批详情查看', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("查看")').click();
      
      // 验证详情页面
      await expect(page.locator('.approval-detail')).toBeVisible();
      await expect(page.locator('.applicant-info')).toBeVisible();
      await expect(page.locator('.application-content')).toBeVisible();
      await expect(page.locator('.approval-history')).toBeVisible();
      await expect(page.locator('.attachment-list')).toBeVisible();
    }
  });

  test('审批历史查询', async ({ page }) => {
    await page.goto('/approval/history');
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("查询")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    await expect(page.locator('.history-list')).toBeVisible();
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '已通过');
    await page.click('button:has-text("查询")');
    await TestHelpers.waitForLoading(page);
    
    // 验证状态筛选结果
    const historyItems = page.locator('.history-item');
    if (await historyItems.count() > 0) {
      await expect(historyItems.first()).toContainText('已通过');
    }
  });

  test('审批统计信息', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 验证统计卡片
    await expect(page.locator('.statistics-card')).toBeVisible();
    await expect(page.locator('.pending-count')).toBeVisible();
    await expect(page.locator('.approved-today')).toBeVisible();
    await expect(page.locator('.rejected-today')).toBeVisible();
  });

  test('审批提醒功能', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 验证提醒标记
    const urgentItems = page.locator('.todo-item.urgent');
    if (await urgentItems.count() > 0) {
      await expect(urgentItems.first().locator('.urgent-badge')).toBeVisible();
    }
    
    // 验证即将超时提醒
    const warningItems = page.locator('.todo-item.warning');
    if (await warningItems.count() > 0) {
      await expect(warningItems.first().locator('.warning-badge')).toBeVisible();
    }
  });

  test('审批流程图查看', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("查看")').click();
      
      // 查看流程图
      await page.click('button:has-text("流程图")');
      
      // 验证流程图显示
      await expect(page.locator('.flow-chart')).toBeVisible();
      await expect(page.locator('.flow-node')).toBeVisible();
    }
  });

  test('审批意见模板', async ({ page }) => {
    await page.goto('/approval/pending');
    
    // 找到待审批项
    const todoItem = page.locator('.todo-item').first();
    
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      
      // 使用意见模板
      await TestHelpers.selectOption(page, '[name="commentTemplate"]', '同意');
      
      // 验证模板填充
      const commentField = page.locator('textarea[name="comment"]');
      await expect(commentField).not.toHaveValue('');
    }
  });
});
