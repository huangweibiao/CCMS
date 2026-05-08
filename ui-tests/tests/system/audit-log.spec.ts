import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 操作日志与审计E2E测试
 * 覆盖：操作日志查询、审计追踪、日志导出、异常监控
 */
test.describe('操作日志与审计', () => {
  
  test('操作日志查询与筛选', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log');
    
    // 验证日志列表加载
    await expect(page.locator('.log-list')).toBeVisible();
    await expect(page.locator('.el-table__row')).toHaveCount.greaterThan(0);
    
    // 按模块筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("模块"))');
    await page.click('.el-select-dropdown__item:has-text("费用管理")');
    await page.click('button:has-text("筛选")');
    
    // 验证筛选结果
    const rows = await page.locator('.el-table__row').all();
    for (const row of rows) {
      await expect(row).toContainText('费用管理');
    }
    
    // 按操作类型筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("操作类型"))');
    await page.click('.el-select-dropdown__item:has-text("新增")');
    await page.click('button:has-text("筛选")');
    
    // 验证只显示新增操作
    for (const row of await page.locator('.el-table__row').all()) {
      await expect(row).toContainText('新增');
    }
    
    // 按时间范围筛选
    await page.fill('input[name="startTime"]', '2024-01-01 00:00:00');
    await page.fill('input[name="endTime"]', '2024-12-31 23:59:59');
    await page.click('button:has-text("筛选")');
    
    // 按操作人搜索
    await page.fill('input[placeholder="操作人"]', 'admin');
    await page.click('button:has-text("筛选")');
    await expect(page.locator('.el-table__row')).toContainText('admin');
  });

  test('操作日志详情查看', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log');
    
    // 点击查看详情
    await page.click('.el-table__row').first().locator('button:has-text("详情")');
    
    // 验证详情弹窗
    await expect(page.locator('.log-detail-modal')).toBeVisible();
    await expect(page.locator('.log-time')).toBeVisible();
    await expect(page.locator('.log-user')).toBeVisible();
    await expect(page.locator('.log-module')).toBeVisible();
    await expect(page.locator('.log-action')).toBeVisible();
    await expect(page.locator('.log-ip')).toBeVisible();
    await expect(page.locator('.log-params')).toBeVisible();
    
    // 关闭详情
    await page.click('.modal-close');
    await expect(page.locator('.log-detail-modal')).not.toBeVisible();
  });

  test('登录日志审计', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log');
    
    // 切换到登录日志标签
    await page.click('.el-tabs__item:has-text("登录日志")');
    
    // 验证登录日志列表
    await expect(page.locator('.login-log-list')).toBeVisible();
    
    // 按登录状态筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("登录状态"))');
    await page.click('.el-select-dropdown__item:has-text("成功")');
    await page.click('button:has-text("筛选")');
    
    // 验证只显示成功登录
    for (const row of await page.locator('.el-table__row').all()) {
      await expect(row.locator('.status-cell')).toContainText('成功');
    }
    
    // 查看登录失败记录
    await page.click('.el-select:has(.el-input__placeholder:has-text("登录状态"))');
    await page.click('.el-select-dropdown__item:has-text("失败")');
    await page.click('button:has-text("筛选")');
    
    // 验证显示失败原因
    if (await page.locator('.el-table__row').count() > 0) {
      await expect(page.locator('.el-table__row').first()).toContainText('失败原因');
    }
  });

  test('业务数据变更审计', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    
    // 执行一个业务操作
    await page.goto('/budget/management');
    await page.click('button:has-text("新增预算")');
    await page.fill('input[name="title"]', '审计测试预算');
    await page.fill('input[name="amount"]', '50000');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 查看审计日志
    await page.goto('/system/log');
    await page.click('.el-tabs__item:has-text("审计日志")');
    
    // 验证记录了预算创建操作
    await expect(page.locator('.audit-log-list')).toContainText('预算管理');
    await expect(page.locator('.audit-log-list')).toContainText('新增');
    await expect(page.locator('.audit-log-list')).toContainText('审计测试预算');
    
    // 查看变更详情
    await page.click('.el-table__row:has-text("审计测试预算") button:has-text("详情")');
    await expect(page.locator('.audit-detail')).toBeVisible();
    await expect(page.locator('.before-data')).toContainText('无');
    await expect(page.locator('.after-data')).toContainText('审计测试预算');
    await expect(page.locator('.after-data')).toContainText('50000');
  });

  test('敏感操作审计', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    
    // 执行敏感操作：删除用户
    await page.goto('/system/user/management');
    await page.fill('input[placeholder="搜索"]', 'testuser');
    await page.click('button:has-text("搜索")');
    
    if (await page.locator('.el-table__row').count() > 0) {
      await page.click('button:has-text("删除")');
      await page.click('.el-button--primary:has-text("确认")');
      await TestHelpers.verifySuccessMessage(page, '删除成功');
      
      // 查看敏感操作日志
      await page.goto('/system/log');
      await page.click('.el-tabs__item:has-text("敏感操作")');
      
      // 验证记录了删除操作
      await expect(page.locator('.sensitive-log-list')).toContainText('用户管理');
      await expect(page.locator('.sensitive-log-list')).toContainText('删除');
      await expect(page.locator('.sensitive-log-list')).toContainText('高风险');
      
      // 验证需要二次确认
      await page.click('.el-table__row').first().locator('button:has-text("详情")');
      await expect(page.locator('.sensitive-warning')).toContainText('敏感操作');
    }
  });

  test('日志导出功能', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log');
    
    // 设置导出条件
    await page.fill('input[name="startTime"]', '2024-01-01 00:00:00');
    await page.fill('input[name="endTime"]', '2024-12-31 23:59:59');
    await page.click('.el-select:has(.el-input__placeholder:has-text("模块"))');
    await page.click('.el-select-dropdown__item:has-text("费用管理")');
    
    // 导出Excel
    const excelPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    await page.click('text=Excel');
    const excelDownload = await excelPromise;
    expect(excelDownload.suggestedFilename()).toMatch(/\.xlsx$/);
    
    // 导出PDF
    const pdfPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    await page.click('text=PDF');
    const pdfDownload = await pdfPromise;
    expect(pdfDownload.suggestedFilename()).toMatch(/\.pdf$/);
  });

  test('日志定期清理配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log/config');
    
    // 配置日志保留策略
    await page.check('input[name="enableAutoCleanup"]');
    await page.fill('input[name="retentionDays"]', '90');
    
    // 配置清理时间
    await page.fill('input[name="cleanupTime"]', '02:00');
    
    // 选择清理范围
    await page.check('input[name="cleanupOperationLog"]');
    await page.check('input[name="cleanupLoginLog"]');
    await page.uncheck('input[name="cleanupAuditLog"]');
    
    // 保存配置
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
    
    // 手动触发清理
    await page.click('button:has-text("立即清理")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '清理完成');
    
    // 查看清理记录
    await page.click('.el-tabs__item:has-text("清理记录")');
    await expect(page.locator('.cleanup-record-list')).toBeVisible();
  });

  test('异常操作监控与告警', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log/monitor');
    
    // 配置异常规则
    await page.click('button:has-text("新增规则")');
    await page.fill('input[name="ruleName"]', '频繁登录失败告警');
    await page.click('.el-select:has(.el-input__placeholder:has-text("规则类型"))');
    await page.click('.el-select-dropdown__item:has-text("登录异常")');
    await page.fill('input[name="threshold"]', '5');
    await page.fill('input[name="timeWindow"]', '10');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '规则保存成功');
    
    // 查看异常统计
    await expect(page.locator('.abnormal-statistics')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("今日异常")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("本周异常")')).toBeVisible();
    
    // 查看异常详情
    await page.click('button:has-text("查看详情")');
    await expect(page.locator('.abnormal-detail')).toBeVisible();
    await expect(page.locator('.abnormal-ip')).toBeVisible();
    await expect(page.locator('.abnormal-count')).toBeVisible();
    await expect(page.locator('.abnormal-time')).toBeVisible();
    
    // 加入黑名单
    await page.click('button:has-text("加入黑名单")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '已加入黑名单');
  });

  test('审计报表生成', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/log/report');
    
    // 选择报表类型
    await page.click('.el-select:has(.el-input__placeholder:has-text("报表类型"))');
    await page.click('.el-select-dropdown__item:has-text("操作统计报表")');
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    
    // 验证报表内容
    await expect(page.locator('.report-chart')).toBeVisible();
    await expect(page.locator('.operation-statistics')).toBeVisible();
    await expect(page.locator('.user-activity-ranking')).toBeVisible();
    await expect(page.locator('.module-usage-statistics')).toBeVisible();
    
    // 导出报表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出报表")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.pdf$/);
  });
});
