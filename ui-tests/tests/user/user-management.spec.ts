import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('用户管理(管理员权限)', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/admin/users');
  });

  test('创建新用户', async ({ page }) => {
    // 点击新建用户按钮
    await page.click('button:has-text("新建用户")');
    
    // 填写用户信息
    const userId = Math.random().toString(36).substring(7);
    const userEmail = `testuser_${userId}@example.com`;
    
    await page.fill('input[name="username"]', `testuser_${userId}`);
    await page.fill('input[name="email"]', userEmail);
    await page.fill('input[name="password"]', 'Test123456');
    await page.fill('input[name="confirmPassword"]', 'Test123456');
    await page.selectOption('select[name="department"]', '技术部');
    await page.selectOption('select[name="role"]', '普通用户');
    
    // 保存用户
    await page.click('button:has-text("保存")');
    
    // 验证创建成功
    await TestHelpers.verifySuccessMessage(page, '用户创建成功');
    
    // 验证用户出现在列表中
    await TestHelpers.verifyTableContains(page, userEmail);
  });

  test('编辑用户信息', async ({ page }) => {
    // 找到第一个可编辑的用户
    const userRow = page.locator('.user-item').first();
    await userRow.locator('button:has-text("编辑")').click();
    
    // 修改用户部门
    await page.selectOption('select[name="department"]', '财务部');
    await page.fill('input[name="phone"]', '13800138000');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '用户信息更新成功');
  });

  test('重置用户密码', async ({ page }) => {
    // 找到用户进行密码重置
    const userRow = page.locator('.user-item').first();
    await userRow.locator('button:has-text("重置密码")').click();
    
    // 确认重置
    await TestHelpers.confirmDialog(page);
    
    // 验证重置成功
    await TestHelpers.verifySuccessMessage(page, '密码重置成功');
    
    // 验证临时密码生成
    const tempPassword = page.locator('.temp-password');
    if (await tempPassword.isVisible()) {
      await expect(tempPassword).toBeVisible();
    }
  });

  test('禁用/启用用户', async ({ page }) => {
    // 找到活动用户
    const activeUser = page.locator('.user-item.active').first();
    
    if (await activeUser.count() > 0) {
      // 禁用用户
      await activeUser.locator('button:has-text("禁用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证禁用成功
      await TestHelpers.verifySuccessMessage(page, '用户已禁用');
      
      // 验证状态更新
      await expect(activeUser.locator('.status-badge')).toContainText('已禁用');
      
      // 重新启用
      await activeUser.locator('button:has-text("启用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证启用成功
      await TestHelpers.verifySuccessMessage(page, '用户已启用');
    }
  });

  test('批量用户操作', async ({ page }) => {
    // 选择多个用户
    const checkboxes = page.locator('.user-checkbox');
    await checkboxes.first().click();
    await checkboxes.nth(1).click();
    
    // 批量启用操作
    await page.click('button:has-text("批量启用")');
    await TestHelpers.confirmDialog(page);
    
    // 验证批量操作成功
    await TestHelpers.verifySuccessMessage(page, '批量操作成功');
  });

  test('用户搜索和筛选', async ({ page }) => {
    // 按部门筛选
    await page.selectOption('select[name="department"]', '技术部');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    const filteredUsers = page.locator('.user-item');
    if (await filteredUsers.count() > 0) {
      await expect(filteredUsers.first()).toContainText('技术部');
    }
    
    // 按状态筛选
    await page.selectOption('select[name="status"]', '活跃');
    await page.click('button:has-text("筛选")');
    
    // 验证状态筛选
    const activeUsers = page.locator('.user-item.active');
    await expect(activeUsers.first().locator('.status-badge')).toHaveText('活跃');
    
    // 搜索功能
    const searchText = 'admin';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    
    // 验证搜索结果
    await TestHelpers.verifyTableContains(page, searchText);
  });

  test('部门管理', async ({ page }) => {
    // 导航到部门管理
    await page.goto('/admin/departments');
    await TestHelpers.verifyPageTitle(page, '部门管理');
    
    // 创建新部门
    await page.click('button:has-text("新建部门")');
    const deptId = Math.random().toString(36).substring(7);
    await page.fill('input[name="name"]', `测试部门_${deptId}`);
    await page.fill('input[name="code"]', `DEPT_${deptId}`);
    await page.click('button:has-text("保存")');
    
    // 验证部门创建成功
    await TestHelpers.verifySuccessMessage(page, '部门创建成功');
  });

  test('角色权限管理', async ({ page }) => {
    // 导航到角色管理
    await page.goto('/admin/roles');
    await TestHelpers.verifyPageTitle(page, '角色管理');
    
    // 创建新角色
    await page.click('button:has-text("新建角色")');
    const roleId = Math.random().toString(36).substring(7);
    await page.fill('input[name="name"]', `测试角色_${roleId}`);
    await page.fill('input[name="description"]', '自动化测试角色');
    
    // 设置权限
    await page.check('input[name="expense_apply"]');
    await page.check('input[name="budget_view"]');
    
    // 保存角色
    await page.click('button:has-text("保存")');
    
    // 验证角色创建成功
    await TestHelpers.verifySuccessMessage(page, '角色创建成功');
  });

  test('个人资料修改(普通用户)', async ({ page }) => {
    // 以普通用户登录
    await TestHelpers.userLogin(page);
    
    // 导航到个人资料页面
    await page.goto('/profile');
    await TestHelpers.verifyPageTitle(page, '个人资料');
    
    // 修改个人信息
    await page.fill('input[name="phone"]', '13800138000');
    await page.fill('textarea[name="bio"]', '自动化测试用户资料');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证保存成功
    await TestHelpers.verifySuccessMessage(page, '个人资料更新成功');
  });

  test('修改密码(普通用户)', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/profile/password');
    
    // 填写密码修改信息
    await page.fill('input[name="currentPassword"]', 'user123');
    await page.fill('input[name="newPassword"]', 'NewPass123');
    await page.fill('input[name="confirmPassword"]', 'NewPass123');
    
    // 提交修改
    await page.click('button:has-text("修改密码")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '密码修改成功');
    
    // 重新登录验证新密码
    await page.click('.logout-btn');
    await TestHelpers.login(page, 'user@example.com', 'NewPass123');
    await TestHelpers.verifySuccessMessage(page, '登录成功');
  });

  test('权限边界验证', async ({ page }) => {
    // 普通用户尝试访问管理页面
    await TestHelpers.userLogin(page);
    
    // 尝试访问用户管理
    await page.goto('/admin/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
    
    // 尝试访问角色管理
    await page.goto('/admin/roles');
    await expect(page.locator('.error-page')).toContainText('权限不足');
    
    // 验证普通用户功能正常
    await page.goto('/expense/application');
    await TestHelpers.verifyPageTitle(page, '费用申请');
  });
});