import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('认证流程测试', () => {
  test('用户成功登录', async ({ page }) => {
    await page.goto('/login');
    await TestHelpers.verifyPageTitle(page, '登录');
    
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/dashboard/);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证用户信息
    await expect(page.locator('.user-name')).toContainText('user');
  });

  test('管理员成功登录', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[type="text"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/dashboard/);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证管理员权限
    await page.goto('/users');
    await TestHelpers.verifyPageTitle(page, '用户管理');
  });

  test('审批员成功登录', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/dashboard/);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证审批权限
    await page.goto('/approval/pending');
    await TestHelpers.verifyPageTitle(page, '待办审批');
  });

  test('登录失败-错误密码', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    
    // 验证登录失败
    await expect(page).toHaveURL(/.*\/login/);
    await TestHelpers.verifyErrorMessage(page, '用户名或密码错误');
  });

  test('登录失败-用户不存在', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[type="text"]', 'nonexistent@example.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    // 验证登录失败
    await expect(page).toHaveURL(/.*\/login/);
    await TestHelpers.verifyErrorMessage(page, '用户名或密码错误');
  });

  test('登录表单验证', async ({ page }) => {
    await page.goto('/login');
    
    // 提交空表单
    await page.click('button[type="submit"]');
    
    // 验证表单验证错误
    const formErrors = page.locator('.el-form-item__error');
    await expect(formErrors.first()).toBeVisible();
  });

  test('记住密码功能', async ({ page, context }) => {
    await page.goto('/login');
    
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.check('input[name="remember"]');
    await page.click('button[type="submit"]');
    
    // 验证登录成功
    await expect(page).toHaveURL(/.*\/dashboard/);
    
    // 登出
    await page.click('.logout-btn');
    await expect(page).toHaveURL(/.*\/login/);
    
    // 验证用户名已记住
    const usernameField = page.locator('input[type="text"]');
    await expect(usernameField).toHaveValue('user@example.com');
  });

  test('登出功能', async ({ page }) => {
    // 先登录
    await TestHelpers.userLogin(page);
    
    // 点击登出
    await page.click('.logout-btn');
    
    // 验证重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);
    
    // 验证需要重新登录
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('Token过期处理', async ({ page }) => {
    // 登录
    await TestHelpers.userLogin(page);
    
    // 模拟Token过期
    await page.evaluate(() => {
      localStorage.setItem('token', 'expired_token');
    });
    
    // 刷新页面
    await page.reload();
    
    // 验证重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);
    await TestHelpers.verifyErrorMessage(page, '登录已过期');
  });

  test('未登录访问受保护页面', async ({ page }) => {
    // 直接访问受保护页面
    await page.goto('/expense-apply');
    
    // 验证重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('登录后重定向到原页面', async ({ page }) => {
    // 尝试访问受保护页面
    await page.goto('/expense-apply');
    
    // 验证重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);
    
    // 登录
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 验证重定向到原页面
    await expect(page).toHaveURL(/.*\/expense-apply/);
  });

  test('密码显示/隐藏切换', async ({ page }) => {
    await page.goto('/login');
    
    const passwordField = page.locator('input[type="password"]');
    await passwordField.fill('password123');
    
    // 点击显示密码按钮
    await page.click('.password-toggle');
    
    // 验证密码显示
    await expect(page.locator('input[type="text"]')).toHaveValue('password123');
    
    // 点击隐藏密码按钮
    await page.click('.password-toggle');
    
    // 验证密码隐藏
    await expect(page.locator('input[type="password"]')).toHaveValue('password123');
  });

  test('登录页面响应式布局', async ({ page }) => {
    // 桌面端
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/login');
    await expect(page.locator('.login-container')).toBeVisible();
    
    // 平板端
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.reload();
    await expect(page.locator('.login-container')).toBeVisible();
    
    // 移动端
    await page.setViewportSize({ width: 375, height: 667 });
    await page.reload();
    await expect(page.locator('.login-container')).toBeVisible();
  });

  test('多次登录失败锁定', async ({ page }) => {
    await page.goto('/login');
    
    // 多次输入错误密码
    for (let i = 0; i < 5; i++) {
      await page.fill('input[type="text"]', 'user@example.com');
      await page.fill('input[type="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      await page.waitForTimeout(1000);
    }
    
    // 验证账户锁定
    await TestHelpers.verifyErrorMessage(page, '账户已锁定');
    
    // 验证登录按钮禁用
    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeDisabled();
  });
});
