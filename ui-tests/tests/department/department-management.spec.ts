import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('部门管理(管理员权限)', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.adminLogin(page);
  });

  test('查看部门列表', async ({ page }) => {
    await page.goto('/departments');
    await TestHelpers.verifyPageTitle(page, '部门管理');
    
    // 验证部门列表表格
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("部门名称")')).toBeVisible();
    await expect(page.locator('th:has-text("部门编码")')).toBeVisible();
    await expect(page.locator('th:has-text("负责人")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
  });

  test('创建新部门', async ({ page }) => {
    await page.goto('/departments');
    
    // 点击新建部门按钮
    await page.click('button:has-text("新建部门")');
    
    // 填写部门信息
    const deptData = TestDataFactory.createDepartmentData();
    await page.fill('input[name="name"]', deptData.name);
    await page.fill('input[name="code"]', deptData.code);
    await page.fill('textarea[name="description"]', deptData.description);
    
    // 选择上级部门
    await TestHelpers.selectOption(page, '[name="parentId"]', '总公司');
    
    // 保存部门
    await page.click('button:has-text("保存")');
    
    // 验证创建成功
    await TestHelpers.verifySuccessMessage(page, '部门创建成功');
    
    // 验证部门出现在列表中
    await TestHelpers.verifyTableContains(page, deptData.name);
  });

  test('编辑部门信息', async ({ page }) => {
    await page.goto('/departments');
    
    // 找到可编辑的部门
    const deptRow = page.locator('.el-table__row').first();
    await deptRow.locator('button:has-text("编辑")').click();
    
    // 修改部门信息
    const newDescription = '自动化测试修改部门描述';
    await page.fill('textarea[name="description"]', newDescription);
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await TestHelpers.verifySuccessMessage(page, '部门信息更新成功');
  });

  test('禁用/启用部门', async ({ page }) => {
    await page.goto('/departments');
    
    // 找到活动部门
    const activeDept = page.locator('.el-table__row:has-text("启用")').first();
    
    if (await activeDept.count() > 0) {
      // 禁用部门
      await activeDept.locator('button:has-text("禁用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证禁用成功
      await TestHelpers.verifySuccessMessage(page, '部门已禁用');
      
      // 重新启用
      await activeDept.locator('button:has-text("启用")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证启用成功
      await TestHelpers.verifySuccessMessage(page, '部门已启用');
    }
  });

  test('删除部门', async ({ page }) => {
    await page.goto('/departments');
    
    // 找到可删除的部门（没有子部门和员工的部门）
    const deletableDept = page.locator('.el-table__row:has-text("可删除")').first();
    
    if (await deletableDept.count() > 0) {
      await deletableDept.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '部门删除成功');
    }
  });

  test('部门搜索功能', async ({ page }) => {
    await page.goto('/departments');
    
    // 搜索部门
    const searchText = '技术';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    await TestHelpers.waitForLoading(page);
    
    // 验证搜索结果
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('部门状态筛选', async ({ page }) => {
    await page.goto('/departments');
    
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

  test('部门详情查看', async ({ page }) => {
    await page.goto('/departments');
    
    // 点击查看详情
    const deptRow = page.locator('.el-table__row').first();
    await deptRow.locator('button:has-text("详情")').click();
    
    // 验证详情页面
    await expect(page.locator('.department-detail')).toBeVisible();
    await expect(page.locator('.dept-name')).toBeVisible();
    await expect(page.locator('.dept-members')).toBeVisible();
    await expect(page.locator('.dept-stats')).toBeVisible();
  });

  test('部门成员管理', async ({ page }) => {
    await page.goto('/departments');
    
    // 进入部门详情
    const deptRow = page.locator('.el-table__row').first();
    await deptRow.locator('button:has-text("成员")').click();
    
    // 验证成员管理页面
    await expect(page.locator('.member-management')).toBeVisible();
    
    // 添加成员
    await page.click('button:has-text("添加成员")');
    await TestHelpers.selectOption(page, '[name="userId"]', '测试用户');
    await page.click('button:has-text("确认")');
    
    // 验证添加成功
    await TestHelpers.verifySuccessMessage(page, '成员添加成功');
  });

  test('部门预算查看', async ({ page }) => {
    await page.goto('/departments');
    
    // 进入部门详情
    const deptRow = page.locator('.el-table__row').first();
    await deptRow.locator('button:has-text("预算")').click();
    
    // 验证预算信息
    await expect(page.locator('.dept-budget')).toBeVisible();
    await expect(page.locator('.budget-amount')).toBeVisible();
    await expect(page.locator('.budget-used')).toBeVisible();
    await expect(page.locator('.budget-remaining')).toBeVisible();
  });

  test('部门树形结构展示', async ({ page }) => {
    await page.goto('/departments');
    
    // 验证树形结构
    const treeView = page.locator('.dept-tree');
    if (await treeView.count() > 0) {
      await expect(treeView).toBeVisible();
      
      // 展开/折叠节点
      const treeNode = treeView.locator('.el-tree-node').first();
      await treeNode.locator('.el-tree-node__expand-icon').click();
    }
  });

  test('部门导出功能', async ({ page }) => {
    await page.goto('/departments');
    
    // 导出部门列表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('部门权限边界验证', async ({ page }) => {
    // 普通用户登录
    await TestHelpers.userLogin(page);
    
    // 尝试访问部门管理
    await page.goto('/departments');
    
    // 验证权限控制
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });
});
