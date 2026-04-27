import { test, expect } from '@playwright/test';

test.describe('费用报销流程', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('创建费用报销申请', async ({ page }) => {
    // 导航到报销页面
    await page.goto('/expense/reimbursement');
    
    // 验证页面加载
    await expect(page.locator('h1')).toContainText('费用报销');
    
    // 点击新建报销按钮
    await page.click('button:has-text("新建报销")');
    
    // 填写报销信息
    await page.fill('input[name="title"]', '商务差旅报销');
    await page.fill('input[name="totalAmount"]', '1500');
    await page.fill('input[name="travelDate"]', '2024-03-15');
    
    // 添加费用明细
    await page.click('button:has-text("添加明细")');
    await page.fill('input[name="items.0.description"]', '机票费用');
    await page.fill('input[name="items.0.amount"]', '800');
    await page.fill('input[name="items.0.category"]', '交通费');
    
    await page.click('button:has-text("添加明细")');
    await page.fill('input[name="items.1.description"]', '住宿费用');
    await page.fill('input[name="items.1.amount"]', '700');
    await page.fill('input[name="items.1.category"]', '住宿费');
    
    // 上传凭证
    await page.setInputFiles('input[type="file"]', {
      name: 'receipt.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('test file content')
    });
    
    // 提交报销申请
    await page.click('button:has-text("提交报销")');
    
    // 验证提交成功
    await expect(page.locator('.success-message')).toContainText('报销申请提交成功');
  });

  test('查看报销申请详情', async ({ page }) => {
    // 导航到报销列表
    await page.goto('/expense/reimbursement');
    
    // 点击查看详情
    const reimbursementRow = page.locator('.reimbursement-item').first();
    await reimbursementRow.locator('button:has-text("查看详情")').click();
    
    // 验证详情页面加载
    await expect(page.locator('h1')).toContainText('报销详情');
    await expect(page.locator('.reimbursement-details')).toBeVisible();
    
    // 验证详细信息显示
    await expect(page.locator('.reimbursement-amount')).toBeVisible();
    await expect(page.locator('.reimbursement-status')).toBeVisible();
  });

  test('导出报销记录', async ({ page }) => {
    // 导航到报销列表
    await page.goto('/expense/reimbursement');
    
    // 选择导出时间范围
    await page.fill('input[name="exportStartDate"]', '2024-01-01');
    await page.fill('input[name="exportEndDate"]', '2024-03-31');
    
    // 点击导出按钮
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出Excel")');
    
    // 验证下载开始
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
    
    // 验证导出成功提示
    await expect(page.locator('.success-message')).toContainText('导出成功');
  });

  test('报销申请状态跟踪', async ({ page }) => {
    // 导航到报销列表
    await page.goto('/expense/reimbursement');
    
    // 查找特定状态的报销申请
    const pendingReimbursement = page.locator('.reimbursement-item:has-text("审核中")').first();
    
    // 验证状态显示
    await expect(pendingReimbursement.locator('.status-badge')).toHaveText('审核中');
    
    // 查看历史记录
    await pendingReimbursement.locator('button:has-text("查看历史")').click();
    
    // 验证历史记录显示
    await expect(page.locator('.approval-history')).toBeVisible();
    await expect(page.locator('.history-item')).toHaveCount(1);
  });
});