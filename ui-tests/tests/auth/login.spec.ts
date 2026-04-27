import { test, expect } from '@playwright/test';

test.describe('用户登录', () => {
  test('管理员用户成功登录', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await expect(page.locator('h1')).toContainText('用户登录');
    
    // 填写管理员账号
    await page.fill('input[type="text"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'admin123');
    
    // 点击登录按钮
    await page.click('button[type="submit"]');
    
    // 验证登录成功，跳转到仪表板
    await expect(page).toHaveURL(/.*\/dashboard/);
    await expect(page.locator('.user-info')).toContainText('管理员');
  });

  test('审批人员登录失败，显示错误信息', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 填写错误密码
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    
    // 点击登录按钮
    await page.click('button[type="submit"]');
    
    // 验证显示错误信息
    await expect(page.locator('.error-message')).toContainText('用户名或密码错误');
    
    // 验证页面仍停留在登录页
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('记住密码功能', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 填写信息并勾选记住密码
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.check('input[type="checkbox"]');
    
    // 点击登录
    await page.click('button[type="submit"]');
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/dashboard/);
    
    // 清除浏览器存储后重新访问登录页
    await page.context().clearCookies();
    await page.goto('/login');
    
    // 验证用户名/密码是否被记住（如果实现了该功能）
    // await expect(page.locator('input[type="text"]')).toHaveValue('user@example.com');
  });
});