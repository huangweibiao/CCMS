import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('借款管理', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('提交借款申请', async ({ page }) => {
    // 导航到借款申请页面
    await page.goto('/loan/application');
    await TestHelpers.verifyPageTitle(page, '借款申请');
    
    // 点击新建申请按钮
    await page.click('button:has-text("新建借款")');
    
    // 填写借款信息
    const loanData = TestDataFactory.createLoanData();
    await page.fill('input[name="purpose"]', loanData.purpose);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    await page.selectOption('select[name="repaymentPeriod"]', loanData.repaymentPeriod.toString());
    await page.fill('textarea[name="reason"]', '临时资金周转需要');
    
    // 选择借款类型
    await page.selectOption('select[name="loanType"]', '个人借款');
    
    // 上传相关文件
    if (await page.locator('input[type="file"]').isVisible()) {
      await page.setInputFiles('input[type="file"]', {
        name: 'application.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('test file content')
      });
    }
    
    // 提交借款申请
    await page.click('button:has-text("提交申请")');
    
    // 验证提交成功
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
    // 验证申请出现在列表中
    await page.goto('/loan/list');
    await TestHelpers.verifyTableContains(page, loanData.purpose);
  });

  test('查看借款详情', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到借款记录
    const loanItem = page.locator('.loan-item').first();
    await loanItem.locator('button:has-text("查看详情")').click();
    
    // 验证详情页面加载
    await TestHelpers.verifyPageTitle(page, '借款详情');
    
    // 验证借款信息显示完整
    await expect(page.locator('.loan-amount')).toBeVisible();
    await expect(page.locator('.loan-purpose')).toBeVisible();
    await expect(page.locator('.loan-status')).toBeVisible();
    await expect(page.locator('.repayment-plan')).toBeVisible();
  });

  test('还款操作', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到可还款的借款记录
    const repayableLoan = page.locator('.loan-item:has-text("待还款")').first();
    
    if (await repayableLoan.count() > 0) {
      // 点击还款按钮
      await repayableLoan.locator('button:has-text("还款")').click();
      
      // 填写还款信息
      await page.fill('input[name="repaymentAmount"]', '500');
      await page.selectOption('select[name="paymentMethod"]', '银行卡');
      await page.fill('input[name="paymentDate"]', new Date().toISOString().split('T')[0]);
      
      // 确认还款
      await page.click('button:has-text("确认还款")');
      
      // 验证还款成功
      await TestHelpers.verifySuccessMessage(page, '还款成功');
      
      // 验证状态更新
      await expect(repayableLoan.locator('.loan-status')).toContainText('部分还款');
    }
  });

  test('借款审批流程(审批员视角)', async ({ page }) => {
    // 审批员登录
    await TestHelpers.approverLogin(page);
    
    // 导航到借款审批页面
    await page.goto('/loan/approval');
    await TestHelpers.verifyPageTitle(page, '借款审批');
    
    // 查看待审批借款
    const pendingLoans = page.locator('.pending-loan-item');
    if (await pendingLoans.count() > 0) {
      // 查看第一条待审批借款详情
      await pendingLoans.first().locator('button:has-text("查看")').click();
      
      // 审批通过
      await page.click('button:has-text("通过")');
      await page.fill('textarea[name="approvalComment"]', '申请符合借款政策，予以通过');
      await page.click('.modal button:has-text("确认通过")');
      
      // 验证审批成功
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
  });

  test('借款驳回流程', async ({ page }) => {
    await TestHelpers.approverLogin(page);
    await page.goto('/loan/approval');
    
    // 找到待审批借款进行驳回
    const pendingLoans = page.locator('.pending-loan-item');
    if (await pendingLoans.count() > 0) {
      await pendingLoans.first().locator('button:has-text("查看")').click();
      
      // 驳回借款
      await page.click('button:has-text("驳回")');
      await page.selectOption('select[name="rejectReason"]', '金额不符');
      await page.fill('textarea[name="rejectComment"]', '申请金额超出限额');
      await page.click('.modal button:has-text("确认驳回")');
      
      // 验证驳回成功
      await TestHelpers.verifySuccessMessage(page, '借款已驳回');
    }
  });

  test('借款历史查询', async ({ page }) => {
    await page.goto('/loan/history');
    await TestHelpers.verifyPageTitle(page, '借款历史');
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-03-31');
    await page.click('button:has-text("查询")');
    
    // 验证查询结果
    const historyItems = page.locator('.loan-history-item');
    await expect(historyItems).toBeVisible();
    
    // 按状态筛选
    await page.selectOption('select[name="status"]', '已还清');
    await page.click('button:has-text("查询")');
    
    // 验证状态筛选结果
    const clearedLoans = page.locator('.loan-history-item.cleared');
    if (await clearedLoans.count() > 0) {
      await expect(clearedLoans.first().locator('.status-badge')).toHaveText('已还清');
    }
  });

  test('逾期借款提醒', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 检查逾期借款提醒
    const overdueLoans = page.locator('.loan-item.overdue');
    if (await overdueLoans.count() > 0) {
      // 验证逾期警告显示
      await expect(overdueLoans.first().locator('.overdue-warning')).toBeVisible();
      
      // 验证逾期天数计算
      const overdueDays = await overdueLoans.first().locator('.overdue-days').textContent();
      expect(parseInt(overdueDays || '0')).toBeGreaterThan(0);
    }
  });

  test('还款计划生成和查看', async ({ page }) => {
    await page.goto('/loan/list');
    
    // 找到有还款计划的借款
    const plannedLoan = page.locator('.loan-item:has-text("还款计划")').first();
    
    if (await plannedLoan.count() > 0) {
      // 查看还款计划
      await plannedLoan.locator('button:has-text("还款计划")').click();
      
      // 验证还款计划页面
      await TestHelpers.verifyPageTitle(page, '还款计划');
      
      // 验证还款计划表
      const paymentSchedule = page.locator('.payment-schedule');
      await expect(paymentSchedule).toBeVisible();
      
      // 验证还款明细
      const paymentItems = page.locator('.payment-item');
      await expect(paymentItems.first()).toBeVisible();
    }
  });

  test('借款导出功能', async ({ page }) => {
    await page.goto('/loan/history');
    
    // 点击导出按钮
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出记录")');
    
    // 验证下载开始
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
    
    // 验证导出成功提示
    await TestHelpers.verifySuccessMessage(page, '借款记录导出成功');
  });

  test('借款金额限制验证', async ({ page }) => {
    await page.goto('/loan/application');
    await page.click('button:has-text("新建借款")');
    
    // 测试超出限额的借款金额
    await page.fill('input[name="amount"]', '100000');
    await page.fill('input[name="purpose"]', '测试超额借款');
    
    // 验证表单验证错误
    const amountField = page.locator('input[name="amount"]');
    await amountField.blur();
    
    // 验证金额限制提示
    const validationMessage = page.locator('.el-form-item__error');
    if (await validationMessage.count() > 0) {
      await expect(validationMessage).toContainText(/超出限额/);
    }
  });
});