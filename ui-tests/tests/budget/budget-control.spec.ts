import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 预算控制与预警测试
 * 覆盖：预算编制、执行控制、预警提醒、调整流程
 */
test.describe('预算控制与预警系统', () => {
  
  test('预算执行实时监控', async ({ page }) => {
    // 管理员登录查看预算仪表盘
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/dashboard');
    
    // 验证预算仪表盘加载
    await expect(page.locator('h1')).toContainText('预算管理');
    
    // 查看预算执行概览
    await expect(page.locator('.budget-overview')).toBeVisible();
    await expect(page.locator('.total-budget')).toContainText('总预算');
    await expect(page.locator('.used-budget')).toContainText('已使用');
    await expect(page.locator('.remaining-budget')).toContainText('剩余');
    
    // 查看执行率图表
    await expect(page.locator('.execution-chart')).toBeVisible();
    
    // 点击部门查看详情
    await page.click('.dept-budget-item:has-text("技术部")');
    await expect(page.locator('.dept-detail-modal')).toBeVisible();
    await expect(page.locator('.monthly-execution')).toBeVisible();
  });

  test('预算超支预警通知', async ({ page }) => {
    // 1. 设置预算预警阈值
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/settings');
    
    // 配置预警规则
    await page.fill('input[name="warningThreshold"]', '80');
    await page.fill('input[name="criticalThreshold"]', '95');
    await page.check('input[name="enableEmailNotification"]');
    await page.check('input[name="enableSystemNotification"]');
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 2. 创建费用申请触发预警
    const budgetData = TestDataFactory.createBudgetData();
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', budgetData.title);
    await page.fill('input[name="amount"]', (budgetData.amount * 0.9).toString()); // 使用90%预算
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 3. 审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 4. 验证预警通知
    await TestHelpers.adminLogin(page);
    await page.goto('/notifications');
    await expect(page.locator('.notification-item')).toContainText('预算预警');
    await expect(page.locator('.notification-item')).toContainText('执行率超过80%');
  });

  test('预算冻结与解冻', async ({ page }) => {
    // 1. 创建预算
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    await page.click('button:has-text("新增预算")');
    
    const budgetData = TestDataFactory.createBudgetData();
    await page.fill('input[name="title"]', budgetData.title);
    await page.fill('input[name="amount"]', budgetData.amount.toString());
    await page.fill('input[name="year"]', budgetData.year.toString());
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 2. 冻结预算
    await page.fill('input[placeholder="搜索预算"]', budgetData.title);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("冻结")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '预算冻结成功');
    
    // 验证状态为已冻结
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已冻结');
    
    // 3. 验证无法使用冻结预算
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("预算科目"))');
    
    // 冻结的预算不应出现在下拉选项中
    const options = await page.locator('.el-select-dropdown__item').allTextContents();
    expect(options).not.toContain(budgetData.title);
    
    // 4. 解冻预算
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    await page.fill('input[placeholder="搜索预算"]', budgetData.title);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("解冻")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '预算解冻成功');
    
    // 验证状态恢复
    await expect(statusCell).toContainText('正常');
  });

  test('预算调整申请流程', async ({ page }) => {
    // 1. 创建初始预算
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    await page.click('button:has-text("新增预算")');
    
    const budgetData = TestDataFactory.createBudgetData();
    await page.fill('input[name="title"]', budgetData.title);
    await page.fill('input[name="amount"]', budgetData.amount.toString());
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 2. 提交预算调整申请
    await page.goto('/budget/adjustment');
    await page.click('button:has-text("新增调整")');
    
    // 选择要调整的预算
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择预算"))');
    await page.click(`.el-select-dropdown__item:has-text("${budgetData.title}")`);
    
    // 填写调整信息
    const adjustmentData = TestDataFactory.createBudgetAdjustmentData();
    await page.fill('input[name="adjustAmount"]', adjustmentData.adjustAmount.toString());
    await page.fill('textarea[name="reason"]', adjustmentData.reason);
    
    // 上传附件
    await page.setInputFiles('input[type="file"]', {
      name: adjustmentData.attachmentName,
      mimeType: 'application/pdf',
      buffer: Buffer.from('adjustment document')
    });
    
    await page.click('button:has-text("提交调整")');
    await TestHelpers.verifySuccessMessage(page, '调整申请提交成功');
    
    // 3. 审批调整申请
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 4. 验证预算金额已更新
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    await page.fill('input[placeholder="搜索预算"]', budgetData.title);
    await page.click('button:has-text("搜索")');
    
    const newAmount = budgetData.amount + adjustmentData.adjustAmount;
    await expect(page.locator('.amount-cell')).toContainText(newAmount.toString());
  });

  test('跨部门预算调拨', async ({ page }) => {
    // 1. 创建两个部门预算
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    
    // 创建技术部预算
    await page.click('button:has-text("新增预算")');
    await page.fill('input[name="title"]', '技术部2024预算');
    await page.fill('input[name="amount"]', '100000');
    await page.click('.el-select:has(.el-input__placeholder:has-text("部门"))');
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    await page.click('button:has-text("保存")');
    
    // 创建市场部预算
    await page.click('button:has-text("新增预算")');
    await page.fill('input[name="title"]', '市场部2024预算');
    await page.fill('input[name="amount"]', '50000');
    await page.click('.el-select:has(.el-input__placeholder:has-text("部门"))');
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    await page.click('button:has-text("保存")');
    
    // 2. 发起预算调拨
    await page.goto('/budget/transfer');
    await page.click('button:has-text("新增调拨")');
    
    // 选择调出部门
    await page.click('.el-select:has(.el-input__placeholder:has-text("调出部门"))');
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    
    // 选择调入部门
    await page.click('.el-select:has(.el-input__placeholder:has-text("调入部门"))');
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    
    // 填写调拨金额
    await page.fill('input[name="transferAmount"]', '20000');
    await page.fill('textarea[name="reason"]', '项目协作需要');
    await page.click('button:has-text("提交调拨")');
    await TestHelpers.verifySuccessMessage(page, '调拨申请提交成功');
    
    // 3. 审批调拨
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 4. 验证预算余额更新
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/management');
    
    // 技术部预算减少
    await page.fill('input[placeholder="搜索预算"]', '技术部2024预算');
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.amount-cell')).toContainText('80000');
    
    // 市场部预算增加
    await page.clear('input[placeholder="搜索预算"]');
    await page.fill('input[placeholder="搜索预算"]', '市场部2024预算');
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.amount-cell')).toContainText('70000');
  });

  test('预算执行分析报表', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/analysis');
    
    // 选择分析维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("分析维度"))');
    await page.click('.el-select-dropdown__item:has-text("部门")');
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    
    // 验证报表内容
    await expect(page.locator('.analysis-chart')).toBeVisible();
    await expect(page.locator('.data-table')).toBeVisible();
    
    // 验证图表类型切换
    await page.click('button:has-text("柱状图")');
    await expect(page.locator('.bar-chart')).toBeVisible();
    
    await page.click('button:has-text("饼图")');
    await expect(page.locator('.pie-chart')).toBeVisible();
    
    await page.click('button:has-text("折线图")');
    await expect(page.locator('.line-chart')).toBeVisible();
    
    // 导出报表
    await page.click('button:has-text("导出")');
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("Excel")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toContain('.xlsx');
  });

  test('预算年度结转', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/carryover');
    
    // 选择结转年度
    await page.click('.el-select:has(.el-input__placeholder:has-text("源年度"))');
    await page.click('.el-select-dropdown__item:has-text("2024")');
    
    await page.click('.el-select:has(.el-input__placeholder:has-text("目标年度"))');
    await page.click('.el-select-dropdown__item:has-text("2025")');
    
    // 选择结转方式
    await page.click('.el-radio:has-text("余额结转")');
    
    // 查看可结转预算列表
    await expect(page.locator('.carryover-list')).toBeVisible();
    
    // 选择要结转的预算
    await page.click('input[type="checkbox"].select-all');
    
    // 执行结转
    await page.click('button:has-text("执行结转")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '结转成功');
    
    // 验证2025年预算已创建
    await page.goto('/budget/management');
    await page.click('.el-select:has(.el-input__placeholder:has-text("年度"))');
    await page.click('.el-select-dropdown__item:has-text("2025")');
    await expect(page.locator('.el-table__row')).toHaveCount.greaterThan(0);
  });
});
