import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('报销管理', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('创建报销申请', async ({ page }) => {
    // 导航到报销申请页面
    await page.goto('/expense-reimburse');
    await TestHelpers.verifyPageTitle(page, '费用报销');
    
    // 点击新建报销按钮
    await page.click('button:has-text("新建报销")');
    
    // 填写报销信息
    const reimburseData = TestDataFactory.createReimbursementData();
    await page.fill('input[name="title"]', reimburseData.title);
    await page.fill('input[name="totalAmount"]', reimburseData.totalAmount.toString());
    
    // 添加费用明细
    for (let i = 0; i < reimburseData.items.length; i++) {
      if (i > 0) {
        await page.click('button:has-text("添加明细")');
      }
      await page.fill(`input[name="items.${i}.description"]`, reimburseData.items[i].description);
      await page.fill(`input[name="items.${i}.amount"]`, reimburseData.items[i].amount.toString());
      await TestHelpers.selectOption(page, `[name="items.${i}.category"]`, reimburseData.items[i].category);
    }
    
    // 上传凭证
    await TestHelpers.uploadFile(page, 'input[type="file"]', 'receipt.pdf', 'application/pdf');
    
    // 提交报销申请
    await page.click('button:has-text("提交申请")');
    
    // 验证提交成功
    await TestHelpers.verifySuccessMessage(page, '报销申请提交成功');
    
    // 验证报销出现在列表中
    await TestHelpers.verifyTableContains(page, reimburseData.title);
  });

  test('查看报销列表', async ({ page }) => {
    await page.goto('/expense-reimburse');
    await TestHelpers.verifyPageTitle(page, '费用报销');
    
    // 验证报销列表表格
    const table = page.locator('.el-table');
    await expect(table).toBeVisible();
    
    // 验证表格列
    await expect(page.locator('th:has-text("报销标题")')).toBeVisible();
    await expect(page.locator('th:has-text("报销金额")')).toBeVisible();
    await expect(page.locator('th:has-text("申请时间")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
  });

  test('查看报销详情', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 找到报销记录
    const reimburseRow = page.locator('.el-table__row').first();
    await reimburseRow.locator('button:has-text("查看")').click();
    
    // 验证详情页面
    await TestHelpers.verifyPageTitle(page, '报销详情');
    
    // 验证详情信息
    await expect(page.locator('.reimburse-title')).toBeVisible();
    await expect(page.locator('.reimburse-amount')).toBeVisible();
    await expect(page.locator('.reimburse-status')).toBeVisible();
    await expect(page.locator('.expense-items')).toBeVisible();
    await expect(page.locator('.attachment-list')).toBeVisible();
  });

  test('编辑报销申请', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 找到可编辑的报销申请（草稿或待审批状态）
    const editableRow = page.locator('.el-table__row:has-text("草稿"), .el-table__row:has-text("待审批")').first();
    
    if (await editableRow.count() > 0) {
      await editableRow.locator('button:has-text("编辑")').click();
      
      // 修改报销金额
      const newAmount = '2000';
      await page.fill('input[name="totalAmount"]', newAmount);
      await page.fill('textarea[name="remark"]', '自动化测试修改报销金额');
      
      // 保存修改
      await page.click('button:has-text("保存")');
      
      // 验证修改成功
      await TestHelpers.verifySuccessMessage(page, '报销申请更新成功');
      
      // 验证金额更新
      await TestHelpers.verifyTableContains(page, newAmount);
    }
  });

  test('删除报销申请', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 找到可删除的报销申请（草稿状态）
    const deletableRow = page.locator('.el-table__row:has-text("草稿")').first();
    
    if (await deletableRow.count() > 0) {
      await deletableRow.locator('button:has-text("删除")').click();
      await TestHelpers.confirmDialog(page);
      
      // 验证删除成功
      await TestHelpers.verifySuccessMessage(page, '报销申请已删除');
    }
  });

  test('报销状态筛选', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
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

  test('报销搜索功能', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 搜索报销
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

  test('报销审批流程', async ({ page }) => {
    // 先创建报销申请
    const reimburseData = TestDataFactory.createReimbursementData();
    await page.goto('/expense-reimburse');
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', reimburseData.title);
    await page.fill('input[name="totalAmount"]', reimburseData.totalAmount.toString());
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '报销申请提交成功');
    
    // 审批员登录并审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/pending');
    
    // 找到报销审批
    const approvalItem = page.locator(`.approval-item:has-text("${reimburseData.title}")`).first();
    if (await approvalItem.count() > 0) {
      await approvalItem.locator('button:has-text("审批")').click();
      await page.click('button:has-text("通过")');
      await page.fill('textarea[name="comment"]', '报销金额合理，予以通过');
      await TestHelpers.confirmDialog(page);
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
  });

  test('报销导出功能', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 导出报销记录
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|csv)$/);
    await TestHelpers.verifySuccessMessage(page, '导出成功');
  });

  test('报销统计信息', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 验证统计卡片
    await expect(page.locator('.statistics-card')).toBeVisible();
    await expect(page.locator('.total-reimburse')).toBeVisible();
    await expect(page.locator('.pending-reimburse')).toBeVisible();
    await expect(page.locator('.approved-reimburse')).toBeVisible();
  });

  test('报销分页功能', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 验证分页
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

  test('报销附件下载', async ({ page }) => {
    await page.goto('/expense-reimburse');
    
    // 找到有附件的报销记录
    const rowWithAttachment = page.locator('.el-table__row:has(.attachment-icon)').first();
    
    if (await rowWithAttachment.count() > 0) {
      await rowWithAttachment.locator('button:has-text("查看")').click();
      
      // 下载附件
      const downloadPromise = page.waitForEvent('download');
      await page.click('.attachment-item button:has-text("下载")');
      
      const download = await downloadPromise;
      expect(download.suggestedFilename()).toBeTruthy();
    }
  });
});
