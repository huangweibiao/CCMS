import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 发票管理E2E测试
 * 覆盖：发票录入、验真、关联报销、归档、统计
 */
test.describe('发票管理系统', () => {
  
  test('发票录入与自动识别', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    
    // 点击新增发票
    await page.click('button:has-text("新增发票")');
    
    // 上传发票图片
    await page.setInputFiles('input[type="file"]', {
      name: 'invoice.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from('test invoice image content')
    });
    
    // 等待OCR识别完成
    await page.waitForSelector('.ocr-result', { timeout: 10000 });
    
    // 验证自动识别字段
    await expect(page.locator('input[name="invoiceNo"]')).not.toBeEmpty();
    await expect(page.locator('input[name="invoiceCode"]')).not.toBeEmpty();
    await expect(page.locator('input[name="amount"]')).not.toBeEmpty();
    await expect(page.locator('input[name="invoiceDate"]')).not.toBeEmpty();
    await expect(page.locator('input[name="sellerName"]')).not.toBeEmpty();
    
    // 补充信息
    await page.click('.el-select:has(.el-input__placeholder:has-text("发票类型"))');
    await page.click('.el-select-dropdown__item:has-text("增值税专用发票")');
    
    await page.click('.el-select:has(.el-input__placeholder:has-text("费用类型"))');
    await page.click('.el-select-dropdown__item:has-text("差旅费")');
    
    await page.fill('textarea[name="remark"]', '差旅住宿费');
    
    // 保存发票
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '发票保存成功');
    
    // 验证发票出现在列表中
    await expect(page.locator('.invoice-list')).toContainText('增值税专用发票');
  });

  test('发票验真功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    
    // 录入待验真发票
    await page.click('button:has-text("新增发票")');
    await page.fill('input[name="invoiceNo"]', '123456789012');
    await page.fill('input[name="invoiceCode"]', '044001900111');
    await page.fill('input[name="amount"]', '1000.00');
    await page.fill('input[name="invoiceDate"]', '2024-01-15');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '发票保存成功');
    
    // 执行验真
    await page.click('button:has-text("验真")');
    
    // 等待验真结果
    await page.waitForSelector('.verification-result', { timeout: 15000 });
    
    // 验证结果显示
    const result = await page.locator('.verification-status').textContent();
    expect(['验真通过', '验真失败', '无法验证']).toContain(result);
    
    // 查看验真详情
    await page.click('button:has-text("查看详情")');
    await expect(page.locator('.verification-detail')).toBeVisible();
    await expect(page.locator('.verify-time')).toBeVisible();
    await expect(page.locator('.verify-source')).toContainText('国家税务总局');
  });

  test('发票关联报销流程', async ({ page }) => {
    // 1. 先录入发票
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    await page.click('button:has-text("新增发票")');
    await page.fill('input[name="invoiceNo"]', 'INV' + Date.now());
    await page.fill('input[name="amount"]', '2000');
    await page.click('.el-select:has(.el-input__placeholder:has-text("发票类型"))');
    await page.click('.el-select-dropdown__item:has-text("增值税普通发票")');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '发票保存成功');
    
    const invoiceNo = await page.locator('.invoice-no').first().textContent();
    
    // 2. 创建报销单并关联发票
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', '差旅费报销');
    
    // 选择发票
    await page.click('button:has-text("选择发票")');
    await page.fill('.invoice-search input', invoiceNo!);
    await page.click(`text=${invoiceNo}`);
    await page.click('button:has-text("确认")');
    
    // 验证发票信息自动填充
    await expect(page.locator('.invoice-amount')).toContainText('2000');
    await expect(page.locator('.invoice-type')).toContainText('增值税普通发票');
    
    // 提交报销
    await page.click('button:has-text("提交报销")');
    await TestHelpers.verifySuccessMessage(page, '报销提交成功');
    
    // 3. 验证发票状态更新为已使用
    await page.goto('/invoice/management');
    await page.fill('input[placeholder="搜索"]', invoiceNo!);
    await page.click('button:has-text("搜索")');
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已使用');
  });

  test('发票重复报销检测', async ({ page }) => {
    // 录入发票并关联报销
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    await page.click('button:has-text("新增发票")');
    const invoiceNo = 'DUP' + Date.now();
    await page.fill('input[name="invoiceNo"]', invoiceNo);
    await page.fill('input[name="amount"]', '1500');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '发票保存成功');
    
    // 第一次报销
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', '第一次报销');
    await page.click('button:has-text("选择发票")');
    await page.fill('.invoice-search input', invoiceNo);
    await page.click(`text=${invoiceNo}`);
    await page.click('button:has-text("确认")');
    await page.click('button:has-text("提交报销")');
    await TestHelpers.verifySuccessMessage(page, '报销提交成功');
    
    // 尝试第二次报销
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', '第二次报销');
    await page.click('button:has-text("选择发票")');
    await page.fill('.invoice-search input', invoiceNo);
    
    // 验证发票标记为已使用
    const invoiceItem = page.locator('.invoice-item').filter({ hasText: invoiceNo });
    await expect(invoiceItem.locator('.status-badge')).toContainText('已使用');
    await expect(invoiceItem.locator('input[type="checkbox"]')).toBeDisabled();
  });

  test('发票归档与查询', async ({ page }) => {
    // 录入多张发票
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    
    const invoices = [];
    for (let i = 0; i < 5; i++) {
      await page.click('button:has-text("新增发票")');
      const invoiceNo = 'ARC' + Date.now() + i;
      await page.fill('input[name="invoiceNo"]', invoiceNo);
      await page.fill('input[name="amount"]', (1000 + i * 100).toString());
      await page.click('.el-select:has(.el-input__placeholder:has-text("发票类型"))');
      await page.click('.el-select-dropdown__item:has-text("增值税普通发票")');
      await page.click('button:has-text("保存")');
      await TestHelpers.verifySuccessMessage(page, '发票保存成功');
      invoices.push(invoiceNo);
    }
    
    // 按金额筛选
    await page.fill('input[name="minAmount"]', '1200');
    await page.fill('input[name="maxAmount"]', '1500');
    await page.click('button:has-text("筛选")');
    await expect(page.locator('.el-table__row')).toHaveCount(3);
    
    // 按类型筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("发票类型"))');
    await page.click('.el-select-dropdown__item:has-text("增值税专用发票")');
    await page.click('button:has-text("筛选")');
    
    // 按日期排序
    await page.click('th:has-text("开票日期")');
    await expect(page.locator('.sort-icon')).toHaveClass(/ascending/);
    await page.click('th:has-text("开票日期")');
    await expect(page.locator('.sort-icon')).toHaveClass(/descending/);
    
    // 导出发票列表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  });

  test('发票统计报表', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/invoice/statistics');
    
    // 选择统计维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("统计维度"))');
    await page.click('.el-select-dropdown__item:has-text("月份")');
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    
    await page.click('button:has-text("生成报表")');
    
    // 验证统计卡片
    await expect(page.locator('.stat-card:has-text("发票总数")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("发票总金额")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("已验真")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("已报销")')).toBeVisible();
    
    // 验证图表
    await expect(page.locator('.invoice-trend-chart')).toBeVisible();
    await expect(page.locator('.invoice-type-pie')).toBeVisible();
    
    // 切换统计维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("统计维度"))');
    await page.click('.el-select-dropdown__item:has-text("费用类型")');
    await page.click('button:has-text("生成报表")');
    await expect(page.locator('.expense-type-chart')).toBeVisible();
  });

  test('发票批量导入', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/import');
    
    // 下载模板
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("下载模板")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toContain('template');
    
    // 上传批量导入文件
    await page.setInputFiles('input[type="file"]', {
      name: 'invoices.xlsx',
      mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      buffer: Buffer.from('mock excel content')
    });
    
    // 预览导入数据
    await page.click('button:has-text("预览")');
    await expect(page.locator('.import-preview')).toBeVisible();
    await expect(page.locator('.preview-row')).toHaveCount.greaterThan(0);
    
    // 确认导入
    await page.click('button:has-text("确认导入")');
    await TestHelpers.verifySuccessMessage(page, '导入成功');
    
    // 查看导入结果
    await expect(page.locator('.import-result')).toContainText('成功导入');
    await expect(page.locator('.import-result')).toContainText('失败');
  });

  test('发票电子归档', async ({ page }) => {
    // 录入发票
    await TestHelpers.userLogin(page);
    await page.goto('/invoice/management');
    await page.click('button:has-text("新增发票")');
    const invoiceNo = 'ARC' + Date.now();
    await page.fill('input[name="invoiceNo"]', invoiceNo);
    await page.fill('input[name="amount"]', '3000');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '发票保存成功');
    
    // 归档发票
    await page.fill('input[placeholder="搜索"]', invoiceNo);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("归档")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '归档成功');
    
    // 验证状态更新
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已归档');
    
    // 查看归档记录
    await page.goto('/invoice/archive');
    await page.fill('input[placeholder="搜索"]', invoiceNo);
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.el-table__row')).toContainText(invoiceNo);
    await expect(page.locator('.archive-date')).toBeVisible();
    await expect(page.locator('.archive-by')).toBeVisible();
    
    // 下载归档发票
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("下载")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.pdf$/);
  });
});
