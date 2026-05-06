import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('仪表板功能', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('仪表板页面加载', async ({ page }) => {
    await page.goto('/dashboard');
    await TestHelpers.verifyPageTitle(page, '仪表板');
    
    // 验证关键组件显示
    await expect(page.locator('.dashboard-header')).toBeVisible();
    await expect(page.locator('.dashboard-content')).toBeVisible();
  });

  test('统计卡片显示', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证统计卡片
    await expect(page.locator('.stat-card')).toHaveCount.greaterThan(0);
    
    // 验证关键指标
    const statCards = page.locator('.stat-card');
    await expect(statCards.first().locator('.stat-value')).toBeVisible();
    await expect(statCards.first().locator('.stat-label')).toBeVisible();
  });

  test('快捷操作入口', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证快捷操作按钮
    await expect(page.locator('.quick-actions')).toBeVisible();
    
    // 测试快捷导航
    await page.click('button:has-text("新建申请")');
    await expect(page).toHaveURL(/.*\/expense-apply/);
  });

  test('待办事项列表', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证待办事项组件
    const todoSection = page.locator('.todo-section');
    await expect(todoSection).toBeVisible();
    
    // 验证待办列表
    const todoItems = todoSection.locator('.todo-item');
    if (await todoItems.count() > 0) {
      await expect(todoItems.first()).toBeVisible();
    }
  });

  test('最近活动记录', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证活动记录组件
    const activitySection = page.locator('.activity-section');
    await expect(activitySection).toBeVisible();
    
    // 验证活动列表
    const activityItems = activitySection.locator('.activity-item');
    if (await activityItems.count() > 0) {
      await expect(activityItems.first()).toBeVisible();
    }
  });

  test('图表组件显示', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证图表组件
    const chartContainer = page.locator('.chart-container');
    if (await chartContainer.count() > 0) {
      await expect(chartContainer.first()).toBeVisible();
    }
  });

  test('通知消息显示', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 点击通知图标
    await page.click('.notification-icon');
    
    // 验证通知面板
    const notificationPanel = page.locator('.notification-panel');
    await expect(notificationPanel).toBeVisible();
    
    // 关闭通知面板
    await page.click('.notification-panel .close-btn');
    await expect(notificationPanel).not.toBeVisible();
  });

  test('个人信息显示', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 验证用户信息
    await expect(page.locator('.user-info')).toBeVisible();
    await expect(page.locator('.user-name')).toBeVisible();
    await expect(page.locator('.user-role')).toBeVisible();
  });

  test('数据刷新功能', async ({ page }) => {
    await page.goto('/dashboard');
    
    // 点击刷新按钮
    await page.click('button:has-text("刷新")');
    
    // 验证加载状态
    await TestHelpers.waitForLoading(page);
    
    // 验证数据刷新成功
    await expect(page.locator('.dashboard-content')).toBeVisible();
  });
});
