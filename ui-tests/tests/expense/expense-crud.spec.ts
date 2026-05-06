import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('费用申请CRUD操作', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('创建费用申请', async ({ page }) => {
    await page.goto('/expense-apply');
    await TestHelpers.verifyPageTitle(page, '费用申请');
    
    // 点击新建申请
    await page.click('button:has-text("新建申请")');
    
    // 填写申请信息
    const expenseData = TestDataFactory.createExpenseApplication();
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await TestHelpers.selectOption(page, '[name="category"]', expenseData.category);
    await page.fill('textarea[name="description"]', expenseData.description);
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    
    // 验证提交成功
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 验证申请出现在列表中
    await TestHelpers.verifyTableContains(page, expenseData.title);
  });

  test('查看费用申请列表', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 验证表格显示
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("申请标题")')).toBeVisible();
    await expect(page.locator('th:has-text("申请金额")')).toBeVisible();
    await expect(page.locator('th:has-text("申请时间")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
  });

  test('查看费用申请详情', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 点击查看详情
    const expenseRow = page.locator('.el-table__row').first();
    await expenseRow.locator('button:has-text("查看")').click();
    
    // 验证详情页面
    await TestHelpers.verifyPageTitle(page, '申请详情');
    await expect(page.locator('.expense-detail')).toBeVisible();
    await expect(page.locator('.expense-title')).toBeVisible();
    await expect(page.locator('.expense-amount')).toBeVisible();
    await expect(page.locator('.expense-status')).toBeVisible();
  });

  test('编辑费用申请', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 找到可编辑的申请（草稿或待审批状态）
    const editableRow = page.locator('.el-table__row:has-text("草稿"), .el-table__row:has-text("待审批")').first();
    
    if (await editableRow.count() > 0) {
      await editableRow.locator('button:has-text("编辑")').click();
      
      // 修改申请信息
      const newAmount = '999';
      await page.fill('input[name="amount"]', newAmount);
      await page.fill('textarea[name="description"]', '自动化测试修改申请');
      
      // 保存修改
      await page.click('button:has-text("保存")');
      
      // 验证修改成功
      await TestHelpers.verifySuccessMessage(page, '申请更新成功');
    }
  });

  test('删除费用申请', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 找到可删除的申请（草稿状态）
    const deletableRow = page.locator('.el-table__row:has-text("草稿")').first();
    
    if (await deletableRow.count() > 0) {
      await deletableRow.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '申请已删除');
    }
  });

  test('费用申请状态筛选', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 按状态筛选
    await TestHelpers.selectOption(page, '[name="status"]', '待审批');
    await page.click('button:has-text("筛选")');
    await TestHelpers.waitForLoading(page);
    
    // 验证筛选结果
    const rows = page.locator('.el-table__row');
    if (await rows.count() > 0) {
      await expect(rows.first()).toContainText('待审批');
    }
  });

  test('费用申请搜索功能', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 搜索申请
    const searchText = '测试';
    await page.fill('input[name="search"]', searchText);
    await page.click('button:has-text("搜索")');
    await TestHelpers.waitForLoading(page);
    
    // 验证搜索结果
    const table = page.locator('.el-table');
    if (await table.locator('.el-table__row').count() > 0) {
      await expect(table).toContainText(searchText);
    }
  });

  test('费用申请分页功能', async ({ page }) => {
    await page.goto('/expense-apply');
    
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

  test('费用申请导出功能', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 导出申请记录
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('费用申请附件上传', async ({ page }) => {
    await page.goto('/expense-apply');
    await page.click('button:has-text("新建申请")');
    
    // 填写基本信息
    const expenseData = TestDataFactory.createExpenseApplication();
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    
    // 上传附件
    await TestHelpers.uploadFile(page, 'input[type="file"]', 'invoice.pdf', 'application/pdf');
    
    // 验证附件上传成功
    await expect(page.locator('.upload-file-list')).toBeVisible();
    await expect(page.locator('.upload-file-list')).toContainText('invoice.pdf');
  });

  test('费用申请批量操作', async ({ page }) => {
    await page.goto('/expense-apply');
    
    // 选择多个申请
    const checkboxes = page.locator('.el-checkbox');
    const count = await checkboxes.count();
    if (count >= 3) {
      await checkboxes.nth(1).click();
      await checkboxes.nth(2).click();
      
      // 批量删除
      await page.click('button:has-text("批量删除")');
      await TestHelpers.confirmDialog(page);
      
      // 验证批量操作成功
      await TestHelpers.verifySuccessMessage(page, '批量删除成功');
    }
  });
});
