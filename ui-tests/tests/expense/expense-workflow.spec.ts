import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 费用申请完整流程测试
 * 覆盖：申请提交 -> 审批流程 -> 报销关联 -> 财务处理
 */
test.describe('费用申请完整业务流程', () => {
  
  test('费用申请提交并审批通过流程', async ({ page }) => {
    const expenseData = TestDataFactory.createExpenseApplyData();
    
    // 1. 员工登录并提交申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 创建新申请
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.fill('textarea[name="description"]', expenseData.description);
    
    // 选择费用类型
    await page.click('.el-select:has(.el-input__placeholder:has-text("费用类型"))');
    await page.click(`.el-select-dropdown__item:has-text("${expenseData.expenseType}")`);
    
    // 添加明细项
    for (const item of expenseData.items) {
      await page.click('button:has-text("添加明细")');
      const lastRow = page.locator('.expense-item-row').last();
      await lastRow.locator('input[name="itemName"]').fill(item.name);
      await lastRow.locator('input[name="quantity"]').fill(item.quantity.toString());
      await lastRow.locator('input[name="unitPrice"]').fill(item.unitPrice.toString());
    }
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 获取申请单号
    const applicationNo = await page.locator('.application-no').textContent();
    expect(applicationNo).toBeTruthy();
    
    // 2. 审批员登录并审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    
    // 查找待审批申请
    await page.fill('input[placeholder="搜索申请单号"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    
    // 审批通过
    await page.click('button:has-text("审批")');
    await page.fill('textarea[name="comment"]', '同意，预算充足');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 3. 员工查看审批结果
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', expenseData.title);
    await page.click('button:has-text("搜索")');
    
    // 验证状态为已审批
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已审批');
  });

  test('费用申请审批驳回后重新提交', async ({ page }) => {
    const expenseData = TestDataFactory.createExpenseApplyData();
    
    // 1. 员工提交申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', '999999'); // 异常高额
    await page.fill('textarea[name="description"]', '测试驳回流程');
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    const applicationNo = await page.locator('.application-no').textContent();
    
    // 2. 审批员驳回
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索申请单号"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.fill('textarea[name="comment"]', '金额异常，请提供详细说明');
    await page.click('button:has-text("驳回")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
    
    // 3. 员工查看驳回并修改
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', expenseData.title);
    await page.click('button:has-text("搜索")');
    
    // 验证驳回状态
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已驳回');
    
    // 查看驳回原因
    await page.click('button:has-text("查看")');
    await expect(page.locator('.rejection-reason')).toContainText('金额异常');
    
    // 修改金额重新提交
    await page.click('button:has-text("重新提交")');
    await page.fill('input[name="amount"]', '5000');
    await page.fill('textarea[name="description"]', '已修改金额，补充说明');
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 4. 再次审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索申请单号"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    await TestHelpers.verifySuccessMessage(page, '审批成功');
  });

  test('费用申请关联报销流程', async ({ page }) => {
    const expenseData = TestDataFactory.createExpenseApplyData();
    
    // 1. 创建并审批费用申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    const applicationNo = await page.locator('.application-no').textContent();
    
    // 快速审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索申请单号"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 2. 员工创建关联报销单
    await TestHelpers.userLogin(page);
    await page.goto('/expense/reimbursement');
    await page.click('button:has-text("新建报销")');
    
    // 关联申请单
    await page.click('.el-select:has(.el-input__placeholder:has-text("关联申请"))');
    await page.click(`.el-select-dropdown__item:has-text("${applicationNo}")`);
    
    // 填写报销信息
    const reimbursementData = TestDataFactory.createReimbursementData();
    await page.fill('input[name="title"]', reimbursementData.title);
    
    // 添加报销明细
    for (const item of reimbursementData.items) {
      await page.click('button:has-text("添加发票")');
      const lastRow = page.locator('.invoice-row').last();
      await lastRow.locator('input[name="description"]').fill(item.description);
      await lastRow.locator('input[name="amount"]').fill(item.amount.toString());
      await lastRow.locator('.el-select').click();
      await page.click(`.el-select-dropdown__item:has-text("${item.category}")`);
    }
    
    // 上传发票附件
    await page.setInputFiles('input[type="file"]', {
      name: 'invoice.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('test invoice content')
    });
    
    await page.click('button:has-text("提交报销")');
    await TestHelpers.verifySuccessMessage(page, '报销提交成功');
    
    // 3. 验证申请单状态更新为已报销
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', expenseData.title);
    await page.click('button:has-text("搜索")');
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已报销');
  });

  test('批量费用申请处理', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 批量创建申请
    const applications = [];
    for (let i = 0; i < 5; i++) {
      const expenseData = TestDataFactory.createExpenseApplyData();
      await page.click('button:has-text("新建申请")');
      await page.fill('input[name="title"]', expenseData.title);
      await page.fill('input[name="amount"]', expenseData.amount.toString());
      await page.fill('textarea[name="description"]', expenseData.description);
      await page.click('button:has-text("提交申请")');
      await TestHelpers.verifySuccessMessage(page, '申请提交成功');
      applications.push(expenseData.title);
      
      // 返回列表
      await page.goto('/expense/application');
    }
    
    // 批量审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    
    // 全选并批量通过
    await page.click('input[type="checkbox"].select-all');
    await page.click('button:has-text("批量通过")');
    await page.fill('textarea[name="batchComment"]', '批量审批通过');
    await page.click('button:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '批量审批成功');
    
    // 验证所有申请已审批
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    for (const title of applications) {
      await page.fill('input[placeholder="搜索"]', title);
      await page.click('button:has-text("搜索")');
      const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
      await expect(statusCell).toContainText('已审批');
      await page.clear('input[placeholder="搜索"]');
    }
  });

  test('费用申请超预算警告', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 尝试创建超预算申请
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '超预算测试申请');
    await page.fill('input[name="amount"]', '999999999'); // 远超预算
    await page.fill('textarea[name="description"]', '测试超预算警告');
    
    // 选择部门预算
    await page.click('.el-select:has(.el-input__placeholder:has-text("预算科目"))');
    await page.click('.el-select-dropdown__item:has-text("办公费用")');
    
    // 提交时验证警告
    await page.click('button:has-text("提交申请")');
    
    // 验证预算警告弹窗
    await expect(page.locator('.el-dialog__title')).toContainText('预算超支警告');
    await expect(page.locator('.budget-warning-content')).toContainText('超出预算');
    
    // 确认继续提交
    await page.click('button:has-text("继续提交")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
  });

  test('费用申请撤回功能', async ({ page }) => {
    const expenseData = TestDataFactory.createExpenseApplyData();
    
    // 提交申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 在审批前撤回
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', expenseData.title);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("撤回")');
    
    // 确认撤回
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '撤回成功');
    
    // 验证状态变为已撤回
    const statusCell = page.locator('.el-table__row').first().locator('.status-cell');
    await expect(statusCell).toContainText('已撤回');
    
    // 验证审批员看不到该申请
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', expenseData.title);
    await page.click('button:has-text("搜索")');
    await expect(page.locator('.el-empty')).toBeVisible();
  });
});
