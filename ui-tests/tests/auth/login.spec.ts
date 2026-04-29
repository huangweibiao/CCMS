import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('用户登录和认证', () => {
  test('管理员用户成功登录', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证管理员页面访问权限
    await page.goto('/admin/users');
    await TestHelpers.verifyPageTitle(page, '用户管理');
    await expect(page.locator('.user-role')).toContainText('管理员');
  });

  test('审批员用户成功登录', async ({ page }) => {
    await TestHelpers.approverLogin(page);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证审批员页面访问权限
    await page.goto('/approval/management');
    await TestHelpers.verifyPageTitle(page, '审批管理');
    await expect(page.locator('.user-role')).toContainText('审批人员');
  });

  test('普通用户成功登录', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 验证普通用户页面访问权限
    await page.goto('/expense/application');
    await TestHelpers.verifyPageTitle(page, '费用申请');
    await expect(page.locator('.user-role')).toContainText('普通用户');
  });

  test('登录失败显示错误信息', async ({ page }) => {
    await page.goto('/login');
    await TestHelpers.verifyPageTitle(page, '用户登录');
    
    // 填写错误密码
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    
    // 验证显示错误信息
    await TestHelpers.verifyErrorMessage(page, '用户名或密码错误');
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('空表单提交验证', async ({ page }) => {
    await page.goto('/login');
    
    // 不填写任何信息直接提交
    await page.click('button[type="submit"]');
    
    // 验证表单验证错误
    const usernameField = page.locator('input[type="text"]');
    const passwordField = page.locator('input[type="password"]');
    
    await expect(usernameField).toHaveClass(/el-input__inner-error/);
    await expect(passwordField).toHaveClass(/el-input__inner-error/);
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('记住密码功能', async ({ page }) => {
    await page.goto('/login');
    
    // 填写信息并勾选记住密码
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.check('input[type="checkbox"]');
    
    // 点击登录
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('注销功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 点击注销按钮
    await page.click('.logout-btn');
    
    // 验证跳转到登录页
    await expect(page).toHaveURL(/.*\/login/);
    await TestHelpers.verifyPageTitle(page, '用户登录');
  });

  test('Token过期重新登录', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 模拟Token过期（清除登录状态）
    await page.context().clearCookies();
    await page.reload();
    
    // 验证自动跳转到登录页
    await expect(page).toHaveURL(/.*\/login/);
    
    // 重新登录
    await TestHelpers.userLogin(page);
    await TestHelpers.verifySuccessMessage(page, '登录成功');
  });
});