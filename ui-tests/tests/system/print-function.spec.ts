import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 打印功能E2E测试
 * 覆盖：费用单打印、审批单打印、报表打印、打印模板配置
 */
test.describe('打印功能', () => {
  
  test('费用申请单打印', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 找到一条记录并点击打印
    await page.click('.expense-item').first().locator('button:has-text("打印")');
    
    // 验证打印弹窗
    await expect(page.locator('.print-modal')).toBeVisible();
    await expect(page.locator('.print-preview')).toBeVisible();
    
    // 验证打印内容包含关键信息
    await expect(page.locator('.print-content')).toContainText('费用申请单');
    await expect(page.locator('.print-content')).toContainText('申请单号');
    await expect(page.locator('.print-content')).toContainText('申请人');
    await expect(page.locator('.print-content')).toContainText('申请日期');
    await expect(page.locator('.print-content')).toContainText('费用明细');
    
    // 选择打印机（模拟）
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择打印机"))');
    await page.click('.el-select-dropdown__item:has-text("默认打印机")');
    
    // 执行打印
    await page.click('button:has-text("打印")');
    await TestHelpers.verifySuccessMessage(page, '已发送到打印机');
  });

  test('费用报销单打印', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/reimbursement');
    
    // 查看报销单详情
    await page.click('button:has-text("查看")').first();
    
    // 点击打印按钮
    await page.click('button:has-text("打印")');
    
    // 验证打印预览
    await expect(page.locator('.print-preview')).toBeVisible();
    await expect(page.locator('.print-content')).toContainText('费用报销单');
    
    // 验证包含发票信息
    await expect(page.locator('.print-content')).toContainText('发票信息');
    await expect(page.locator('.print-content')).toContainText('发票号码');
    await expect(page.locator('.print-content')).toContainText('发票金额');
    
    // 验证包含审批记录
    await expect(page.locator('.print-content')).toContainText('审批记录');
    await expect(page.locator('.print-content')).toContainText('审批人');
    await expect(page.locator('.print-content')).toContainText('审批意见');
  });

  test('借款单打印', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/loan/my-loans');
    
    // 找到借款记录
    await page.fill('input[placeholder="搜索"]', '测试借款');
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("查看")');
    
    // 打印借款单
    await page.click('button:has-text("打印")');
    
    // 验证借款单打印内容
    await expect(page.locator('.print-content')).toContainText('借款单');
    await expect(page.locator('.print-content')).toContainText('借款金额');
    await expect(page.locator('.print-content')).toContainText('借款用途');
    await expect(page.locator('.print-content')).toContainText('预计还款日期');
    await expect(page.locator('.print-content')).toContainText('审批状态');
  });

  test('批量打印功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/history');
    
    // 选择多条记录
    await page.click('input[type="checkbox"].select-all');
    
    // 点击批量打印
    await page.click('button:has-text("批量打印")');
    
    // 验证批量打印预览
    await expect(page.locator('.batch-print-modal')).toBeVisible();
    await expect(page.locator('.print-count')).toContainText('共');
    
    // 选择打印模板
    await page.click('.el-select:has(.el-input__placeholder:has-text("打印模板"))');
    await page.click('.el-select-dropdown__item:has-text("标准模板")');
    
    // 执行批量打印
    await page.click('button:has-text("确认打印")');
    await TestHelpers.verifySuccessMessage(page, '批量打印任务已创建');
  });

  test('打印模板配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/print/template');
    
    // 新建打印模板
    await page.click('button:has-text("新建模板")');
    await page.fill('input[name="templateName"]', '自定义费用单模板');
    await page.click('.el-select:has(.el-input__placeholder:has-text("适用单据"))');
    await page.click('.el-select-dropdown__item:has-text("费用申请单")');
    
    // 配置模板字段
    await page.click('button:has-text("添加字段")');
    await page.click('.field-item:has-text("申请单号")');
    await page.click('.field-item:has-text("申请人")');
    await page.click('.field-item:has-text("申请日期")');
    await page.click('.field-item:has-text("费用明细")');
    await page.click('.field-item:has-text("审批记录")');
    await page.click('button:has-text("确认")');
    
    // 设置字段顺序
    await page.dragAndDrop('.field-item:has-text("申请人")', '.field-item:has-text("申请单号")');
    
    // 保存模板
    await page.click('button:has-text("保存模板")');
    await TestHelpers.verifySuccessMessage(page, '模板保存成功');
    
    // 验证模板出现在列表中
    await expect(page.locator('.template-list')).toContainText('自定义费用单模板');
  });

  test('打印预览功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 点击打印预览
    await page.click('.expense-item').first().locator('button:has-text("预览")');
    
    // 验证预览弹窗
    await expect(page.locator('.print-preview-modal')).toBeVisible();
    
    // 验证缩放功能
    await page.click('button:has-text("放大")');
    await expect(page.locator('.preview-content')).toHaveCSS('transform', /scale/);
    
    await page.click('button:has-text("缩小")');
    
    // 验证分页显示
    await expect(page.locator('.page-indicator')).toContainText('1 /');
    
    // 关闭预览
    await page.click('.preview-close');
    await expect(page.locator('.print-preview-modal')).not.toBeVisible();
  });

  test('打印为PDF', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 点击打印
    await page.click('.expense-item').first().locator('button:has-text("打印")');
    
    // 选择打印为PDF
    await page.click('.el-radio:has-text("保存为PDF")');
    
    // 下载PDF
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("下载")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.pdf$/);
  });

  test('打印页面设置', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('.expense-item').first().locator('button:has-text("打印")');
    
    // 打开页面设置
    await page.click('button:has-text("页面设置")');
    
    // 设置纸张大小
    await page.click('.el-select:has(.el-input__placeholder:has-text("纸张大小"))');
    await page.click('.el-select-dropdown__item:has-text("A4")');
    
    // 设置纸张方向
    await page.click('.el-radio:has-text("横向")');
    
    // 设置页边距
    await page.fill('input[name="marginTop"]', '20');
    await page.fill('input[name="marginBottom"]', '20');
    await page.fill('input[name="marginLeft"]', '15');
    await page.fill('input[name="marginRight"]', '15');
    
    // 设置页眉页脚
    await page.check('input[name="showHeader"]');
    await page.fill('input[name="headerText"]', 'CCMS费控管理系统');
    await page.check('input[name="showFooter"]');
    await page.fill('input[name="footerText"]', '第 {page} 页 共 {total} 页');
    
    // 应用设置
    await page.click('button:has-text("应用")');
    await TestHelpers.verifySuccessMessage(page, '设置已应用');
    
    // 验证预览更新
    await expect(page.locator('.print-preview')).toHaveClass(/landscape/);
  });

  test('报表打印', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    
    // 生成报表
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    
    // 点击打印报表
    await page.click('button:has-text("打印报表")');
    
    // 验证报表打印预览
    await expect(page.locator('.print-preview')).toBeVisible();
    await expect(page.locator('.print-content')).toContainText('费用分析报表');
    await expect(page.locator('.print-content')).toContainText('统计周期');
    
    // 验证图表也被包含在打印中
    await expect(page.locator('.print-chart')).toBeVisible();
    
    // 执行打印
    await page.click('button:has-text("打印")');
  });

  test('打印历史记录', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/print/history');
    
    // 验证打印历史列表
    await expect(page.locator('.print-history-list')).toBeVisible();
    await expect(page.locator('.print-history-item')).toHaveCount.greaterThan(0);
    
    // 查看打印详情
    await page.click('.print-history-item').first().locator('button:has-text("详情")');
    await expect(page.locator('.print-detail')).toBeVisible();
    await expect(page.locator('.print-detail')).toContainText('打印时间');
    await expect(page.locator('.print-detail')).toContainText('打印人');
    await expect(page.locator('.print-detail')).toContainText('打印内容');
    await expect(page.locator('.print-detail')).toContainText('打印份数');
    
    // 重新打印
    await page.click('button:has-text("重新打印")');
    await TestHelpers.verifySuccessMessage(page, '已发送到打印机');
  });
});
