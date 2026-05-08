import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 借款还款全流程测试
 * 覆盖：借款申请、审批、发放、还款、核销
 */
test.describe('借款还款完整业务流程', () => {
  
  test('借款申请到还款完整流程', async ({ page }) => {
    const loanData = TestDataFactory.createLoanApplyData();
    
    // 1. 员工提交借款申请
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="title"]', loanData.title);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    await page.fill('textarea[name="purpose"]', loanData.purpose);
    await page.fill('input[name="expectedRepaymentDate"]', loanData.expectedRepaymentDate);
    
    // 选择还款方式
    await page.click('.el-select:has(.el-input__placeholder:has-text("还款方式"))');
    await page.click(`.el-select-dropdown__item:has-text("${loanData.repayMethod}")`);
    
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
    const loanNo = await page.locator('.loan-no').textContent();
    
    // 2. 部门经理审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 3. 财务发放借款
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/disbursement');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("发放")');
    
    // 填写发放信息
    await page.fill('input[name="disbursementDate"]', new Date().toISOString().split('T')[0]);
    await page.fill('input[name="bankAccount"]', '6222021234567890123');
    await page.fill('input[name="bankName"]', '中国工商银行');
    await page.click('button:has-text("确认发放")');
    await TestHelpers.verifySuccessMessage(page, '发放成功');
    
    // 4. 员工查看借款状态
    await TestHelpers.userLogin(page);
    await page.goto('/loan/my-loans');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已发放');
    
    // 5. 员工发起还款
    await page.click('button:has-text("还款")');
    
    const repaymentData = TestDataFactory.createRepaymentData();
    await page.fill('input[name="repaymentAmount"]', repaymentData.amount.toString());
    await page.fill('input[name="repaymentDate"]', repaymentData.paymentDate);
    
    // 选择支付方式
    await page.click('.el-select:has(.el-input__placeholder:has-text("支付方式"))');
    await page.click(`.el-select-dropdown__item:has-text("${repaymentData.paymentMethod}")`);
    
    await page.fill('textarea[name="remark"]', repaymentData.remark);
    await page.click('button:has-text("提交还款")');
    await TestHelpers.verifySuccessMessage(page, '还款提交成功');
    
    // 6. 财务确认还款
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/repayment-confirmation');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("确认")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '还款确认成功');
    
    // 7. 验证借款状态更新为已还清
    await TestHelpers.userLogin(page);
    await page.goto('/loan/my-loans');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await expect(statusCell).toContainText('已还清');
  });

  test('分期还款流程', async ({ page }) => {
    const loanData = TestDataFactory.createLoanApplyData();
    loanData.repayMethod = '分期还款';
    
    // 提交借款申请并审批通过
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="title"]', loanData.title);
    await page.fill('input[name="amount"]', '10000');
    await page.click('.el-select:has(.el-input__placeholder:has-text("还款方式"))');
    await page.click('.el-select-dropdown__item:has-text("分期还款")');
    await page.fill('input[name="installments"]', '3');
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
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
    
    // 进行三期还款
    await TestHelpers.userLogin(page);
    for (let i = 1; i <= 3; i++) {
      await page.goto('/loan/my-loans');
      await page.fill('input[placeholder="搜索"]', loanNo!);
      await page.click('button:has-text("搜索")');
      await page.click('button:has-text("还款")');
      
      await page.fill('input[name="repaymentAmount"]', '3333.33');
      await page.fill('input[name="repaymentDate"]', new Date().toISOString().split('T')[0]);
      await page.click('button:has-text("提交还款")');
      await TestHelpers.verifySuccessMessage(page, '还款提交成功');
      
      // 财务确认
      await TestHelpers.adminLogin(page);
      await page.goto('/loan/repayment-confirmation');
      await page.fill('input[placeholder="搜索"]', loanNo!);
      await page.click('button:has-text("搜索")');
      await page.click('button:has-text("确认")');
      await page.click('.el-button--primary:has-text("确认")');
      
      // 验证剩余期数
      await TestHelpers.userLogin(page);
      await page.goto('/loan/my-loans');
      await page.fill('input[placeholder="搜索"]', loanNo!);
      await page.click('button:has-text("搜索")');
      
      if (i < 3) {
        await expect(page.locator('.remaining-installments')).toContainText(`${3 - i}`);
      } else {
        const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
        await expect(statusCell).toContainText('已还清');
      }
    }
  });

  test('借款逾期提醒与处理', async ({ page }) => {
    const loanData = TestDataFactory.createLoanApplyData();
    
    // 创建已发放借款
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="title"]', loanData.title);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    // 设置已过去的还款日期
    await page.fill('input[name="expectedRepaymentDate"]', '2024-01-01');
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
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
    
    // 查看逾期提醒
    await TestHelpers.userLogin(page);
    await page.goto('/notifications');
    await expect(page.locator('.notification-item')).toContainText('借款即将到期');
    
    // 查看逾期借款列表
    await page.goto('/loan/overdue');
    await expect(page.locator('.el-table__row')).toContainText(loanNo!);
    await expect(page.locator('.overdue-days')).toContainText('逾期');
    
    // 查看逾期费用
    await page.click('button:has-text("详情")');
    await expect(page.locator('.overdue-fee')).toBeVisible();
    
    // 发起还款（含逾期费用）
    await page.click('button:has-text("立即还款")');
    await page.fill('input[name="repaymentAmount"]', (loanData.amount * 1.05).toString()); // 含5%逾期费
    await page.click('button:has-text("提交还款")');
    await TestHelpers.verifySuccessMessage(page, '还款提交成功');
  });

  test('借款冲销流程', async ({ page }) => {
    const loanData = TestDataFactory.createLoanApplyData();
    
    // 创建借款并发放
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="title"]', loanData.title);
    await page.fill('input[name="amount"]', '5000');
    await page.click('button:has-text("提交申请")');
    const loanNo = await page.locator('.loan-no').textContent();
    
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
    
    // 创建费用报销冲销借款
    await TestHelpers.userLogin(page);
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    
    // 关联借款
    await page.click('.el-select:has(.el-input__placeholder:has-text("关联借款"))');
    await page.click(`.el-select-dropdown__item:has-text("${loanNo}")`);
    
    await page.fill('input[name="title"]', '差旅费报销');
    await page.click('button:has-text("添加发票")');
    await page.fill('input[name="amount"]', '3000');
    await page.click('button:has-text("提交报销")');
    await TestHelpers.verifySuccessMessage(page, '报销提交成功');
    
    const reimbursementNo = await page.locator('.reimbursement-no').textContent();
    
    // 审批报销
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', reimbursementNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 财务确认冲销
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/write-off');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("冲销")');
    await page.fill('input[name="writeOffAmount"]', '3000');
    await page.fill('textarea[name="remark"]', '关联报销冲销');
    await page.click('button:has-text("确认冲销")');
    await TestHelpers.verifySuccessMessage(page, '冲销成功');
    
    // 验证借款剩余金额
    await TestHelpers.userLogin(page);
    await page.goto('/loan/my-loans');
    await page.fill('input[placeholder="搜索"]', loanNo!);
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.remaining-amount')).toContainText('2000');
  });

  test('借款额度控制', async ({ page }) => {
    // 设置借款额度
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/quota-settings');
    
    await page.fill('input[name="employeeQuota"]', '10000');
    await page.fill('input[name="managerQuota"]', '50000');
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 员工尝试超额借款
    await TestHelpers.userLogin(page);
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="title"]', '超额借款测试');
    await page.fill('input[name="amount"]', '15000'); // 超过10000额度
    await page.click('button:has-text("提交申请")');
    
    // 验证额度警告
    await expect(page.locator('.el-message--warning')).toContainText('超出借款额度');
    
    // 查看当前额度使用情况
    await page.goto('/loan/quota');
    await expect(page.locator('.total-quota')).toContainText('10000');
    await expect(page.locator('.used-quota')).toContainText('0');
    await expect(page.locator('.available-quota')).toContainText('10000');
  });

  test('借款统计报表', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/loan/statistics');
    
    // 选择统计维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("统计维度"))');
    await page.click('.el-select-dropdown__item:has-text("部门")');
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    
    await page.click('button:has-text("生成报表")');
    
    // 验证统计内容
    await expect(page.locator('.stat-card:has-text("借款总额")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("已还款")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("待还款")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("逾期")')).toBeVisible();
    
    // 查看趋势图
    await expect(page.locator('.trend-chart')).toBeVisible();
    
    // 导出报表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出报表")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  });
});
