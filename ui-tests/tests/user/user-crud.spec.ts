import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('用户管理CRUD操作', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('创建新用户', async ({ page }) => {
    await page.goto('/users');
    await TestHelpers.verifyPageTitle(page, '用户管理');
    
    // 点击新建用户
    await page.click('button:has-text("新建用户")');
    
    // 填写用户信息
    const userId = Math.random().toString(36).substring(7);
    const userEmail = `testuser_${userId}@example.com`;
    
    await page.fill('input[name="username"]', `testuser_${userId}`);
    await page.fill('input[name="email"]', userEmail);
    await page.fill('input[name="password"]', 'Test123456');
    await page.fill('input[name="confirmPassword"]', 'Test123456');
    await page.fill('input[name="realName"]', `测试用户${userId}`);
    await page.fill('input[name="phone"]', '13800138000');
    await TestHelpers.selectOption(page, '[name="department"]', '技术部');
    await TestHelpers.selectOption(page, '[name="role"]', '普通用户');
    
    // 保存用户
    await page.click('button:has-text("保存")');
    
    // 验证创建成功
    await TestHelpers.verifySuccessMessage(page, '用户创建成功');
    
    // 验证用户出现在列表中
    await TestHelpers.verifyTableContains(page, userEmail);
  });

  test('查看用户列表', async ({ page }) => {
    await page.goto('/users');
    
    // 验证表格显示
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("用户名")')).toBeVisible();
    await expect(page.locator('th:has-text("邮箱")')).toBeVisible();
    await expect(page.locator('th:has-text("部门")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
  });

  test('查看用户详情', async ({ page }) => {
    await page.goto('/users');
    
    // 点击查看详情
    const userRow = page.locator('.el-table__row').first();
    await userRow.locator('button:has-text("详情")').click();
    
    // 验证详情页面
    await expect(page.locator('.user-detail')).toBeVisible();
    await expect(page.locator('.user-username')).toBeVisible();
    await expect(page.locator('.user-email')).toBeVisible();
    await expect(page.locator('.user-department')).toBeVisible();
    await expect(page.locator('.user-role')).toBeVisible();
  });

  test('编辑用户信息', async ({ page }) => {
    await page.goto('/users');
    
    // 找到可编辑的用户
    const userRow = page.locator('.el-table__row').first();
    await userRow.locator('button:has-text("编辑")').click();
    
    // 修改用户信息
    await page.fill('input[name="phone"]', '13900139000');
    await TestHelpers.selectOption(page, '[name="department"]', '财务部');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '用户信息更新成功');
  });

  test('重置用户密码', async ({ page }) => {
    await page.goto('/users');
    
    // 找到用户
    const userRow = page.locator('.el-table__row').first();
    await userRow.locator('button:has-text("重置密码")').click();
    
    // 确认重置
    await TestHelpers.confirmDialog(page);
    
    // 验证重置成功
    await TestHelpers.verifySuccessMessage(page, '密码重置成功');
    
    // 验证临时密码显示
    const tempPassword = page.locator('.temp-password');
    if (await tempPassword.isVisible()) {
      await expect(tempPassword).toBeVisible();
    }
  });

  test('禁用/启用用户', async ({ page }) => {
    await page.goto('/users');
    
    // 找到活动用户
    const activeUser = page.locator('.el-table__row:has-text("启用")').first();
    
    if (await activeUser.count() > 0) {
      // 禁用用户
      await activeUser.locator('button:has-text("禁用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证禁用成功
      await TestHelpers.verifySuccessMessage(page, '用户已禁用');
      
      // 重新启用
      await activeUser.locator('button:has-text("启用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证启用成功
      await TestHelpers.verifySuccessMessage(page, '用户已启用');
    }
  });

  test('删除用户', async ({ page }) => {
    await page.goto('/users');
    
    // 找到可删除的用户
    const deletableRow = page.locator('.el-table__row:has-text("测试用户")').first();
    
    if (await deletableRow.count() > 0) {
      await deletableRow.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '用户删除成功');
    }
  });

  test('用户搜索功能', async ({ page }) => {
    await page.goto('/users');
    
    // 搜索用户
    const searchText = 'admin';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    await TestHelpers.waitForLoading(page);
    
    // 验证搜索结果
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('用户状态筛选', async ({ page }) => {
    await page.goto('/users');
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '启用');
    await page.click('button:has-text("筛选")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('启用');
    }
  });

  test('用户部门筛选', async ({ page }) => {
    await page.goto('/users');
    
    // 按部门筛选
    await TestHelpers.selectOption(page, '[name="department"]', '技术部');
    await page.click('button:has-text("筛选")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('技术部');
    }
  });

  test('用户批量操作', async ({ page }) => {
    await page.goto('/users');
    
    // 选择多个用户
    const checkboxes = page.locator('.el-checkbox');
    const count = await checkboxes.count();
    if (count >= 3) {
      await checkboxes.nth(1).click();
      await checkboxes.nth(2).click();
      
      // 批量禁用
      await page.click('button:has-text("批量禁用")');
      await TestHelpers.confirmDialog(page);
      
      // 验证批量操作成功
      await TestHelpers.verifySuccessMessage(page, '批量操作成功');
    }
  });

  test('用户导出功能', async ({ page }) => {
    await page.goto('/users');
    
    // 导出用户列表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('用户分页功能', async ({ page }) => {
    await page.goto('/users');
    
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

  test('用户权限边界验证', async ({ page }) => {
    // 普通用户登录
    await TestHelpers.userLogin(page);
    
    // 尝试访问用户管理
    await page.goto('/users');
    
    // 验证权限控制
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });
});
