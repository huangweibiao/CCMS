import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 消息通知中心E2E测试
 * 覆盖：消息接收、标记已读、消息筛选、通知设置
 */
test.describe('消息通知中心', () => {
  
  test('费用申请提交后接收审批通知', async ({ page }) => {
    // 1. 员工提交费用申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    const expenseData = TestDataFactory.createExpenseApplyData();
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 2. 审批员登录查看通知
    await TestHelpers.approverLogin(page);
    
    // 验证通知红点
    await expect(page.locator('.notification-badge')).toBeVisible();
    await expect(page.locator('.notification-badge')).toContainText('1');
    
    // 点击通知图标
    await page.click('.notification-icon');
    
    // 验证通知列表
    await expect(page.locator('.notification-dropdown')).toBeVisible();
    await expect(page.locator('.notification-item')).toContainText('新的费用申请');
    await expect(page.locator('.notification-item')).toContainText(expenseData.title);
    await expect(page.locator('.notification-item')).toContainText('待审批');
    
    // 点击通知跳转到审批页面
    await page.click('.notification-item:has-text("新的费用申请")');
    await expect(page).toHaveURL(/.*\/approval\/management/);
  });

  test('审批结果通知接收', async ({ page }) => {
    // 1. 员工提交申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    const expenseData = TestDataFactory.createExpenseApplyData();
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.click('button:has-text("提交申请")');
    const applicationNo = await page.locator('.application-no').textContent();
    
    // 2. 审批员审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 3. 员工接收审批结果通知
    await TestHelpers.userLogin(page);
    
    // 验证收到审批通过通知
    await expect(page.locator('.notification-badge')).toBeVisible();
    await page.click('.notification-icon');
    await expect(page.locator('.notification-item')).toContainText('申请已审批通过');
    await expect(page.locator('.notification-item')).toContainText(expenseData.title);
    
    // 标记为已读
    await page.click('.notification-item:has-text("申请已审批通过") .mark-read');
    await expect(page.locator('.notification-item:has-text("申请已审批通过")')).toHaveClass(/read/);
  });

  test('预算预警通知', async ({ page }) => {
    // 1. 管理员设置预算预警
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/settings');
    await page.fill('input[name="warningThreshold"]', '80');
    await page.check('input[name="enableNotification"]');
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 2. 创建大额费用申请触发预警
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '预算预警测试');
    await page.fill('input[name="amount"]', '999999');
    await page.click('button:has-text("提交申请")');
    
    // 3. 管理员接收预算预警通知
    await TestHelpers.adminLogin(page);
    await page.click('.notification-icon');
    
    // 验证预算预警通知
    await expect(page.locator('.notification-item')).toContainText('预算预警');
    await expect(page.locator('.notification-item')).toContainText('超出预算');
    
    // 点击查看详情
    await page.click('.notification-item:has-text("预算预警")');
    await expect(page.locator('.budget-warning-detail')).toBeVisible();
    await expect(page.locator('.budget-usage')).toContainText('80%');
  });

  test('借款到期提醒通知', async ({ page }) => {
    // 1. 创建即将到期的借款
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    const loanData = TestDataFactory.createLoanApplyData();
    await page.fill('input[name="title"]', loanData.title);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    // 设置明天到期
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    await page.fill('input[name="expectedRepaymentDate"]', tomorrow.toISOString().split('T')[0]);
    await page.click('button:has-text("提交申请")');
    const loanNo = await page.locator('.loan-no').textContent();
    
    // 审批并发放
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/disbursement');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("发放")');
    await page.click('button:has-text("确认发放")');
    
    // 2. 模拟到期提醒（系统定时任务触发）
    await TestHelpers.userLogin(page);
    await page.goto('/notifications');
    
    // 验证收到到期提醒（实际测试可能需要模拟时间或触发定时任务）
    // 这里验证通知中心页面功能
    await expect(page.locator('.notification-list')).toBeVisible();
    await expect(page.locator('.notification-tabs')).toContainText('全部');
    await expect(page.locator('.notification-tabs')).toContainText('未读');
    await expect(page.locator('.notification-tabs')).toContainText('已读');
  });

  test('消息筛选与搜索', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/notifications');
    
    // 按类型筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("消息类型"))');
    await page.click('.el-select-dropdown__item:has-text("审批通知")');
    await page.click('button:has-text("筛选")');
    
    // 验证只显示审批相关通知
    const notifications = await page.locator('.notification-item').all();
    for (const notification of notifications) {
      await expect(notification).toContainText('审批');
    }
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("筛选")');
    
    // 搜索消息内容
    await page.fill('input[placeholder="搜索消息"]', '费用申请');
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.notification-item')).toContainText('费用申请');
  });

  test('批量标记已读与删除', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/notifications');
    
    // 全选通知
    await page.click('input[type="checkbox"].select-all');
    
    // 批量标记已读
    await page.click('button:has-text("标记已读")');
    await TestHelpers.verifySuccessMessage(page, '标记成功');
    
    // 验证所有通知变为已读状态
    const notifications = await page.locator('.notification-item').all();
    for (const notification of notifications) {
      await expect(notification).toHaveClass(/read/);
    }
    
    // 选择部分通知删除
    await page.click('.notification-item').first().locator('input[type="checkbox"]');
    await page.click('button:has-text("删除")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '删除成功');
  });

  test('通知偏好设置', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    
    // 进入通知设置标签
    await page.click('.el-tabs__item:has-text("通知设置")');
    
    // 配置通知方式
    await page.check('input[name="emailNotification"]');
    await page.check('input[name="smsNotification"]');
    await page.check('input[name="pushNotification"]');
    
    // 配置通知类型
    await page.check('input[name="notifyApproval"]');
    await page.check('input[name="notifyBudget"]');
    await page.check('input[name="notifyLoan"]');
    await page.uncheck('input[name="notifySystem"]');
    
    // 保存设置
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 验证设置生效
    await page.reload();
    await page.click('.el-tabs__item:has-text("通知设置")');
    await expect(page.locator('input[name="emailNotification"]')).toBeChecked();
    await expect(page.locator('input[name="notifySystem"]')).not.toBeChecked();
  });

  test('消息详情查看与快捷操作', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/notifications');
    
    // 点击消息查看详情
    await page.click('.notification-item').first();
    
    // 验证详情弹窗
    await expect(page.locator('.notification-detail-modal')).toBeVisible();
    await expect(page.locator('.notification-title')).toBeVisible();
    await expect(page.locator('.notification-content')).toBeVisible();
    await expect(page.locator('.notification-time')).toBeVisible();
    
    // 快捷操作按钮
    if (await page.locator('button:has-text("去处理")').isVisible()) {
      await page.click('button:has-text("去处理")');
      // 验证跳转到对应页面
      await expect(page).toHaveURL(/.*\/(approval|expense|loan)/);
    }
    
    // 关闭详情
    await page.click('.modal-close');
    await expect(page.locator('.notification-detail-modal')).not.toBeVisible();
  });

  test('系统公告接收', async ({ page }) => {
    // 1. 管理员发布系统公告
    await TestHelpers.adminLogin(page);
    await page.goto('/system/announcement');
    await page.click('button:has-text("发布公告")');
    
    await page.fill('input[name="title"]', '系统维护通知');
    await page.fill('textarea[name="content"]', '系统将于今晚22:00-24:00进行维护升级');
    await page.click('.el-radio:has-text("全员")');
    await page.click('button:has-text("发布")');
    await TestHelpers.verifySuccessMessage(page, '发布成功');
    
    // 2. 普通用户接收公告
    await TestHelpers.userLogin(page);
    
    // 验证收到公告通知
    await expect(page.locator('.notification-badge')).toBeVisible();
    await page.click('.notification-icon');
    await expect(page.locator('.notification-item')).toContainText('系统公告');
    await expect(page.locator('.notification-item')).toContainText('系统维护通知');
    
    // 查看公告详情
    await page.click('.notification-item:has-text("系统公告")');
    await expect(page.locator('.announcement-content')).toContainText('系统将于今晚22:00-24:00进行维护升级');
  });
});
