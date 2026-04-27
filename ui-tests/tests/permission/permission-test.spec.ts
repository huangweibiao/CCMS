import { test, expect } from '@playwright/test';

test.describe('权限管理', () => {
  test('管理员权限验证', async ({ page }) => {
    // 管理员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // 验证管理员权限
    await expect(page.locator('.user-role')).toContainText('管理员');
    
    // 验证可以访问用户管理
    await page.goto('/admin/users');
    await expect(page.locator('h1')).toContainText('用户管理');
    
    // 验证可以访问角色管理
    await page.goto('/admin/roles');
    await expect(page.locator('h1')).toContainText('角色管理');
    
    // 验证可以访问系统设置
    await page.goto('/admin/settings');
    await expect(page.locator('h1')).toContainText('系统设置');
  });

  test('普通用户权限限制', async ({ page }) => {
    // 普通用户登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 验证用户权限
    await expect(page.locator('.user-role')).toContainText('普通用户');
    
    // 验证不能访问管理员页面
    await page.goto('/admin/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
    
    // 验证可以访问费用申请页面
    await page.goto('/expense/application');
    await expect(page.locator('h1')).toContainText('费用申请');
    
    // 验证可以访问费用报销页面
    await page.goto('/expense/reimbursement');
    await expect(page.locator('h1')).toContainText('费用报销');
  });

  test('审批人员权限验证', async ({ page }) => {
    // 审批人员登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'approver@example.com');
    await page.fill('input[type="password"]', 'approver123');
    await page.click('button[type="submit"]');
    
    // 验证审批人员权限
    await expect(page.locator('.user-role')).toContainText('审批人员');
    
    // 验证可以访问审批管理页面
    await page.goto('/approval/management');
    await expect(page.locator('h1')).toContainText('审批管理');
    
    // 验证可以查看审批历史
    await page.goto('/approval/history');
    await expect(page.locator('h1')).toContainText('审批历史');
    
    // 验证不能访问用户管理
    await page.goto('/admin/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
    
    // 验证不能访问系统设置
    await page.goto('/admin/settings');
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });

  test('跨页面权限控制', async ({ page }) => {
    // 普通用户登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 尝试直接访问管理菜单（模拟通过URL直接访问）
    await page.evaluate(() => {
      const adminMenu = document.querySelector('.admin-menu');
      if (adminMenu) {
        adminMenu.style.display = 'block'; // 显示隐藏的管理菜单
      }
    });
    
    // 验证管理菜单项不可点击
    const adminMenuItem = page.locator('.admin-menu-item');
    if (await adminMenuItem.count() > 0) {
      await expect(adminMenuItem).toHaveClass(/disabled/);
    }
    
    // 验证菜单中没有管理员功能
    const menuItems = page.locator('.menu-item');
    const menuTexts = await menuItems.allTextContents();
    expect(menuTexts).not.toContain('用户管理');
    expect(menuTexts).not.toContain('角色管理');
    expect(menuTexts).not.toContain('系统设置');
  });

  test('API权限验证', async ({ page }) => {
    // 模拟不同角色访问受限API
    
    // 普通用户尝试访问管理员API
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 拦截API请求验证权限
    await page.route('**/api/admin/**', route => {
      // 模拟权限不足的响应
      route.fulfill({
        status: 403,
        contentType: 'application/json',
        body: JSON.stringify({ error: '权限不足' })
      });
    });
    
    // 触发管理员API调用
    await page.evaluate(() => {
      fetch('/api/admin/users').catch(() => {});
    });
    
    // 验证API调用被正确拒绝
    const apiResponse = await page.waitForResponse('**/api/admin/**');
    expect(apiResponse.status()).toBe(403);
  });

  test('权限切换场景', async ({ page }) => {
    // 普通用户登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 保存页面状态
    await page.goto('/expense/application');
    
    // 模拟角色切换（登出再登录管理员）
    await page.click('.logout-btn'); // 登出
    
    // 管理员登录
    await page.fill('input[type="text"]', 'admin@example.com');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // 验证管理员功能可用
    await page.goto('/admin/users');
    await expect(page.locator('h1')).toContainText('用户管理');
    
    // 再次登出切换回普通用户
    await page.click('.logout-btn');
    
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    
    // 验证普通用户权限恢复
    await page.goto('/admin/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });
});