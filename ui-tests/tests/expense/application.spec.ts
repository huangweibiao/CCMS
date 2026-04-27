import { test, expect } from '@playwright/test';

test.describe('费用申请流程', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('创建新的费用申请', async ({ page }) => {
    // 导航到费用申请页面
    await page.goto('/expense/application');
    
    // 验证页面加载
    await expect(page.locator('h1')).toContainText('费用申请');
    
    // 点击新建申请按钮
    await page.click('button:has-text("新建申请")');
    
    // 填写申请信息
    await page.fill('input[name="title"]', '办公用品采购');
    await page.fill('input[name="amount"]', '500');
    await page.selectOption('select[name="category"]', '办公用品');
    await page.fill('textarea[name="description"]', '购买打印机墨盒和打印纸');
    
    // 上传文件
    await page.setInputFiles('input[type="file"]', {
      name: 'invoice.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from('test file content')
    });
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    
    // 验证申请成功创建
    await expect(page.locator('.success-message')).toContainText('申请提交成功');
    
    // 验证申请出现在列表中
    await expect(page.locator('.application-list')).toContainText('办公用品采购');
  });

  test('编辑已提交的费用申请', async ({ page }) => {
    // 导航到费用申请列表
    await page.goto('/expense/application');
    
    // 找到待审批的申请
    const applicationRow = page.locator('.application-item:has-text("待审批")').first();
    
    // 点击编辑按钮
    await applicationRow.locator('button:has-text("编辑")').click();
    
    // 修改申请信息
    await page.fill('input[name="amount"]', '600');
    await page.fill('textarea[name="description"]', '更新采购数量');
    
    // 保存修改
    await page.click('button:has-text("保存")');
    
    // 验证修改成功
    await expect(page.locator('.success-message')).toContainText('申请更新成功');
  });

  test('删除费用申请', async ({ page }) => {
    // 导航到费用申请列表
    await page.goto('/expense/application');
    
    // 找到可删除的申请
    const applicationRow = page.locator('.application-item:has-text("草稿")').first();
    
    // 点击删除按钮
    await applicationRow.locator('button:has-text("删除")').click();
    
    // 确认删除
    await page.click('.modal button:has-text("确认")');
    
    // 验证删除成功
    await expect(page.locator('.success-message')).toContainText('申请删除成功');
    
    // 验证申请已从列表中移除
    await expect(applicationRow).not.toBeVisible();
  });

  test('筛选费用申请', async ({ page }) => {
    // 导航到费用申请列表
    await page.goto('/expense/application');
    
    // 按状态筛选
    await page.selectOption('select[name="status"]', 'pending');
    await page.click('button:has-text("筛选")');
    
    // 验证只显示待审批的申请
    const pendingItems = page.locator('.application-item:has-text("待审批")');
    await expect(pendingItems).toHaveCount(1);
    
    // 按时间范围筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    await expect(page.locator('.filter-result')).toContainText('筛选结果');
  });
});