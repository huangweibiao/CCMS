import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('角色权限管理', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('查看角色列表', async ({ page }) => {
    await page.goto('/admin/roles');
    await TestHelpers.verifyPageTitle(page, '角色管理');
    
    // 验证角色列表表格
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("角色名称")')).toBeVisible();
    await expect(page.locator('th:has-text("角色描述")')).toBeVisible();
    await expect(page.locator('th:has-text("用户数量")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
  });

  test('创建新角色', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 点击新建角色
    await page.click('button:has-text("新建角色")');
    
    // 填写角色信息
    const roleData = TestDataFactory.createRoleData();
    await page.fill('input[name="name"]', roleData.name);
    await page.fill('textarea[name="description"]', roleData.description);
    
    // 设置权限
    await page.check('input[name="perm_expense_view"]');
    await page.check('input[name="perm_expense_apply"]');
    await page.check('input[name="perm_budget_view"]');
    
    // 保存角色
    await page.click('button:has-text("保存")');
    
    // 验证创建成功
    await TestHelpers.verifySuccessMessage(page, '角色创建成功');
    
    // 验证角色出现在列表中
    await TestHelpers.verifyTableContains(page, roleData.name);
  });

  test('编辑角色信息', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到可编辑的角色
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("编辑")').click();
    
    // 修改角色描述
    await page.fill('textarea[name="description"]', '自动化测试修改角色描述');
    
    // 修改权限
    await page.check('input[name="perm_report_view"]');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '角色更新成功');
  });

  test('删除角色', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到可删除的角色（没有用户的角色）
    const deletableRow = page.locator('.el-table__row:has-text("0")').first();
    
    if (await deletableRow.count() > 0) {
      await deletableRow.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '角色删除成功');
    }
  });

  test('角色权限配置', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到角色并配置权限
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("权限")').click();
    
    // 验证权限配置页面
    await expect(page.locator('.permission-config')).toBeVisible();
    
    // 配置权限
    await page.check('input[name="perm_user_view"]');
    await page.check('input[name="perm_user_edit"]');
    
    // 保存权限配置
    await page.click('button:has-text("保存权限")');
    
    // 验证保存成功
    await TestHelpers.verifySuccessMessage(page, '权限配置已保存');
  });

  test('角色搜索功能', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 搜索角色
    const searchText = '管理员';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    await TestHelpers.waitForLoading(page);
    
    // 验证搜索结果
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('查看角色详情', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 点击查看详情
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("详情")').click();
    
    // 验证详情页面
    await expect(page.locator('.role-detail')).toBeVisible();
    await expect(page.locator('.role-name')).toBeVisible();
    await expect(page.locator('.role-permissions')).toBeVisible();
    await expect(page.locator('.role-users')).toBeVisible();
  });

  test('角色关联用户', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到角色并管理用户
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("用户")').click();
    
    // 验证用户管理页面
    await expect(page.locator('.role-users-management')).toBeVisible();
    
    // 添加用户到角色
    await page.click('button:has-text("添加用户")');
    await TestHelpers.selectOption(page, '[name="userId"]', '测试用户');
    await page.click('button:has-text("确认")');
    
    // 验证添加成功
    await TestHelpers.verifySuccessMessage(page, '用户添加成功');
  });

  test('角色权限预览', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到角色并预览权限
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("预览")').click();
    
    // 验证权限预览页面
    await expect(page.locator('.permission-preview')).toBeVisible();
    await expect(page.locator('.permission-list')).toBeVisible();
    await expect(page.locator('.permission-tree')).toBeVisible();
  });

  test('角色复制功能', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 找到角色并复制
    const roleRow = page.locator('.el-table__row').first();
    await roleRow.locator('button:has-text("复制")').click();
    
    // 填写新角色名称
    const newRoleName = '复制角色-' + Math.random().toString(36).substring(7);
    await page.fill('input[name="newName"]', newRoleName);
    await page.click('button:has-text("确认复制")');
    
    // 验证复制成功
    await TestHelpers.verifySuccessMessage(page, '角色复制成功');
    
    // 验证新角色出现在列表中
    await TestHelpers.verifyTableContains(page, newRoleName);
  });

  test('角色导出功能', async ({ page }) => {
    await page.goto('/admin/roles');
    
    // 导出角色列表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('角色权限边界验证', async ({ page }) => {
    // 普通用户登录
    await TestHelpers.userLogin(page);
    
    // 尝试访问角色管理
    await page.goto('/admin/roles');
    
    // 验证权限控制
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });
});
