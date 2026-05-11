import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 财务凭证对接E2E测试
 * 覆盖：凭证生成、凭证审核、ERP对接、付款管理
 */
test.describe('财务凭证对接', () => {
  
  test('费用报销生成财务凭证', async ({ page }) => {
    // 1. 员工提交报销单
    await TestHelpers.userLogin(page);
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    
    const reimburseData = TestDataFactory.createReimbursementData();
    await page.fill('input[name="title"]', reimburseData.title);
    await page.fill('input[name="totalAmount"]', reimburseData.totalAmount.toString());
    
    // 添加发票
    await page.click('button:has-text("添加发票")');
    await page.fill('input[name="invoiceNo"]', 'INV' + Date.now());
    await page.fill('input[name="amount"]', reimburseData.totalAmount.toString());
    await page.click('button:has-text("保存")');
    
    await page.click('button:has-text("提交报销")');
    await TestHelpers.verifySuccessMessage(page, '报销提交成功');
    
    const reimburseNo = await page.locator('.reimburse-no').textContent();
    
    // 2. 审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', reimburseNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 3. 财务生成凭证
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/voucher');
    await page.fill('input[placeholder="搜索"]', reimburseNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("生成凭证")');
    
    // 选择凭证模板
    await page.click('.el-select:has(.el-input__placeholder:has-text("凭证模板"))');
    await page.click('.el-select-dropdown__item:has-text("费用报销凭证")');
    
    // 填写会计科目
    await page.click('.el-select:has(.el-input__placeholder:has-text("借方科目"))');
    await page.click('.el-select-dropdown__item:has-text("管理费用-差旅费")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("贷方科目"))');
    await page.click('.el-select-dropdown__item:has-text("银行存款")');
    
    await page.click('button:has-text("确认生成")');
    await TestHelpers.verifySuccessMessage(page, '凭证生成成功');
    
    // 验证凭证信息
    const voucherNo = await page.locator('.voucher-no').textContent();
    expect(voucherNo).toMatch(/PZ\d+/);
    
    // 验证凭证状态
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已生成');
  });

  test('凭证审核流程', async ({ page }) => {
    // 先创建一个待审核的凭证
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/voucher');
    await page.click('button:has-text("新建凭证")');
    await page.fill('input[name="voucherDate"]', new Date().toISOString().split('T')[0]);
    await page.fill('textarea[name="summary"]', '测试凭证');
    
    // 添加分录
    await page.click('button:has-text("添加分录")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("科目"))').first();
    await page.click('.el-select-dropdown__item:has-text("管理费用")');
    await page.fill('input[name="debitAmount"]', '1000');
    
    await page.click('button:has-text("添加分录")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("科目"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("银行存款")');
    await page.fill('input[name="creditAmount"]', '1000');
    
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '凭证保存成功');
    
    const voucherNo = await page.locator('.voucher-no').textContent();
    
    // 提交审核
    await page.fill('input[placeholder="搜索"]', voucherNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("提交审核")');
    await TestHelpers.verifySuccessMessage(page, '已提交审核');
    
    // 凭证审核员登录审核
    await page.goto('/login');
    await page.fill('input[type="text"]', 'voucher@example.com');
    await page.fill('input[type="password"]', 'voucher123');
    await page.click('button[type="submit"]');
    
    await page.goto('/finance/voucher/audit');
    await page.fill('input[placeholder="搜索"]', voucherNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审核")');
    
    // 查看凭证详情
    await expect(page.locator('.voucher-detail')).toBeVisible();
    await expect(page.locator('.debit-total')).toContainText('1000');
    await expect(page.locator('.credit-total')).toContainText('1000');
    
    // 审核通过
    await page.click('button:has-text("通过")');
    await page.fill('textarea[name="auditOpinion"]', '审核通过');
    await page.click('button:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '审核通过');
    
    // 验证状态更新
    await expect(statusCell).toContainText('已审核');
  });

  test('ERP系统对接', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/erp/config');
    
    // 配置ERP连接
    await page.click('.el-select:has(.el-input__placeholder:has-text("ERP系统"))');
    await page.click('.el-select-dropdown__item:has-text("SAP")');
    
    await page.fill('input[name="erpHost"]', 'https://sap.example.com');
    await page.fill('input[name="erpUsername"]', 'ccms_user');
    await page.fill('input[name="erpPassword"]', 'password123');
    await page.fill('input[name="companyCode"]', '1000');
    
    // 测试连接
    await page.click('button:has-text("测试连接")');
    await TestHelpers.verifySuccessMessage(page, '连接成功');
    
    // 配置科目映射
    await page.click('.el-tabs__item:has-text("科目映射")');
    await page.click('button:has-text("添加映射")');
    await page.fill('input[name="localAccount"]', '管理费用-差旅费');
    await page.fill('input[name="erpAccount"]', '660101');
    await page.click('button:has-text("保存")');
    
    // 保存配置
    await page.click('button:has-text("保存配置")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
    
    // 测试同步凭证
    await page.goto('/finance/voucher');
    await page.click('button:has-text("同步到ERP")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '同步成功');
    
    // 验证同步状态
    await expect(page.locator('.sync-status')).toContainText('已同步');
    await expect(page.locator('.erp-voucher-no')).toBeVisible();
  });

  test('付款管理', async ({ page }) => {
    // 1. 创建已审批的报销单
    await TestHelpers.userLogin(page);
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', '付款测试');
    await page.fill('input[name="totalAmount"]', '5000');
    await page.click('button:has-text("提交报销")');
    const reimburseNo = await page.locator('.reimburse-no').textContent();
    
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', reimburseNo!);
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 2. 财务创建付款单
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/payment');
    await page.click('button:has-text("新建付款")');
    
    // 关联报销单
    await page.click('.el-select:has(.el-input__placeholder:has-text("关联单据"))');
    await page.click(`.el-select-dropdown__item:has-text("${reimburseNo}")`);
    
    // 填写付款信息
    await page.fill('input[name="payeeName"]', '张三');
    await page.fill('input[name="payeeAccount"]', '6222021234567890123');
    await page.fill('input[name="payeeBank"]', '中国工商银行');
    await page.fill('input[name="paymentAmount"]', '5000');
    
    // 选择付款方式
    await page.click('.el-select:has(.el-input__placeholder:has-text("付款方式"))');
    await page.click('.el-select-dropdown__item:has-text("银行转账")');
    
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '付款单创建成功');
    
    const paymentNo = await page.locator('.payment-no').textContent();
    
    // 3. 审批付款单
    await page.click('button:has-text("提交审批")');
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', paymentNo!);
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 4. 执行付款
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/payment');
    await page.fill('input[placeholder="搜索"]', paymentNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("执行付款")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '付款成功');
    
    // 验证状态更新
    await expect(page.locator('.status-cell')).toContainText('已付款');
  });

  test('凭证模板管理', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/voucher/template');
    
    // 新建凭证模板
    await page.click('button:has-text("新建模板")');
    await page.fill('input[name="templateName"]', '差旅费报销模板');
    await page.fill('textarea[name="description"]', '适用于差旅费用报销');
    
    // 配置模板分录
    await page.click('button:has-text("添加分录")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("科目"))').first();
    await page.click('.el-select-dropdown__item:has-text("管理费用-差旅费")');
    await page.click('.el-radio:has-text("借方")');
    await page.fill('input[name="formula"]', '{amount}');
    
    await page.click('button:has-text("添加分录")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("科目"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("银行存款")');
    await page.click('.el-radio:has-text("贷方")');
    await page.fill('input[name="formula"]', '{amount}');
    
    await page.click('button:has-text("保存模板")');
    await TestHelpers.verifySuccessMessage(page, '模板保存成功');
    
    // 验证模板列表
    await expect(page.locator('.template-list')).toContainText('差旅费报销模板');
    
    // 使用模板生成凭证
    await page.goto('/finance/voucher');
    await page.click('button:has-text("使用模板")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择模板"))');
    await page.click('.el-select-dropdown__item:has-text("差旅费报销模板")');
    await page.fill('input[name="amount"]', '3000');
    await page.click('button:has-text("生成")');
    
    // 验证自动填充科目
    await expect(page.locator('.debit-account')).toContainText('管理费用-差旅费');
    await expect(page.locator('.credit-account')).toContainText('银行存款');
    await expect(page.locator('.debit-amount')).toContainText('3000');
  });

  test('财务月结处理', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/month-end');
    
    // 选择会计期间
    await page.click('.el-select:has(.el-input__placeholder:has-text("会计期间"))');
    await page.click('.el-select-dropdown__item:has-text("2024-01")');
    
    // 检查月结前提条件
    await page.click('button:has-text("检查")');
    await expect(page.locator('.check-result')).toBeVisible();
    
    // 验证所有凭证已审核
    const voucherCheck = page.locator('.check-item:has-text("凭证审核")');
    await expect(voucherCheck.locator('.check-status')).toContainText('通过');
    
    // 验证银行对账完成
    const bankCheck = page.locator('.check-item:has-text("银行对账")');
    await expect(bankCheck.locator('.check-status')).toContainText('通过');
    
    // 执行月结
    await page.click('button:has-text("执行月结")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '月结成功');
    
    // 验证期间状态
    await expect(page.locator('.period-status')).toContainText('已结账');
    
    // 验证不能再修改该期间凭证
    await page.goto('/finance/voucher');
    await page.fill('input[name="voucherDate"]', '2024-01-15');
    await page.click('button:has-text("保存")');
    await expect(page.locator('.el-message--error')).toContainText('该会计期间已结账');
  });

  test('财务报表生成', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/report');
    
    // 选择报表类型
    await page.click('.el-select:has(.el-input__placeholder:has-text("报表类型"))');
    await page.click('.el-select-dropdown__item:has-text("资产负债表")');
    
    // 选择会计期间
    await page.click('.el-select:has(.el-input__placeholder:has-text("会计期间"))');
    await page.click('.el-select-dropdown__item:has-text("2024-01")');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    await expect(page.locator('.financial-report')).toBeVisible();
    
    // 验证报表数据
    await expect(page.locator('.report-item:has-text("资产总计")')).toBeVisible();
    await expect(page.locator('.report-item:has-text("负债总计")')).toBeVisible();
    await expect(page.locator('.report-item:has-text("所有者权益")')).toBeVisible();
    
    // 导出报表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  });
});
