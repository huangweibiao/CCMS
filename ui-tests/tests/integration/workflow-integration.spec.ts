import { test, expect } from '@playwright/test';
import { TestHelpers, TestDataFactory } from '../../utils/test-helpers';

test.describe('端到端工作流集成测试', () => {
  test('完整费用申请审批流程', async ({ page }) => {
    // 1. 用户创建费用申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense-apply');
    
    const expenseData = TestDataFactory.createExpenseApplication();
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await TestHelpers.selectOption(page, '[name="category"]', expenseData.category);
    await page.fill('textarea[name="description"]', expenseData.description);
    await page.click('button:has-text("提交申请")');
    
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 2. 审批员审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/pending');
    
    const todoItem = page.locator(`.todo-item:has-text("${expenseData.title}")`).first();
    await expect(todoItem).toBeVisible();
    
    await todoItem.locator('button:has-text("审批")').click();
    await page.click('button:has-text("通过")');
    await page.fill('textarea[name="comment"]', '申请合理，予以通过');
    await TestHelpers.confirmDialog(page);
    
    await TestHelpers.verifySuccessMessage(page, '审批通过');
    
    // 3. 用户查看审批结果
    await TestHelpers.userLogin(page);
    await page.goto('/expense-apply');
    
    const expenseRow = page.locator(`.el-table__row:has-text("${expenseData.title}")`).first();
    await expect(expenseRow).toContainText('已通过');
  });

  test('完整借款申请审批流程', async ({ page }) => {
    // 1. 用户创建借款申请
    await TestHelpers.userLogin(page);
    await page.goto('/loan/list');
    
    const loanData = TestDataFactory.createLoanData();
    await page.click('button:has-text("新建借款")');
    await page.fill('input[name="purpose"]', loanData.purpose);
    await page.fill('input[name="amount"]', loanData.amount.toString());
    await TestHelpers.selectOption(page, '[name="repaymentPeriod"]', '12');
    await page.click('button:has-text("提交申请")');
    
    await TestHelpers.verifySuccessMessage(page, '借款申请提交成功');
    
    // 2. 审批员审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/pending');
    
    const todoItem = page.locator(`.todo-item:has-text("${loanData.purpose}")`).first();
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      await page.click('button:has-text("通过")');
      await page.fill('textarea[name="comment"]', '借款用途合理，予以通过');
      await TestHelpers.confirmDialog(page);
      
      await TestHelpers.verifySuccessMessage(page, '审批通过');
      
      // 3. 用户查看借款状态
      await TestHelpers.userLogin(page);
      await page.goto('/loan/list');
      
      const loanRow = page.locator(`.el-table__row:has-text("${loanData.purpose}")`).first();
      await expect(loanRow).toContainText('已通过');
    }
  });

  test('完整报销申请审批流程', async ({ page }) => {
    // 1. 用户创建报销申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense-reimburse');
    
    const reimburseData = TestDataFactory.createReimbursementData();
    await page.click('button:has-text("新建报销")');
    await page.fill('input[name="title"]', reimburseData.title);
    await page.fill('input[name="totalAmount"]', reimburseData.totalAmount.toString());
    await page.click('button:has-text("提交申请")');
    
    await TestHelpers.verifySuccessMessage(page, '报销申请提交成功');
    
    // 2. 审批员审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/pending');
    
    const todoItem = page.locator(`.todo-item:has-text("${reimburseData.title}")`).first();
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      await page.click('button:has-text("通过")');
      await page.fill('textarea[name="comment"]', '报销金额合理，予以通过');
      await TestHelpers.confirmDialog(page);
      
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
  });

  test('用户创建和权限分配流程', async ({ page }) => {
    // 1. 管理员创建用户
    await TestHelpers.adminLogin(page);
    await page.goto('/users');
    
    const userId = Math.random().toString(36).substring(7);
    const userEmail = `newuser_${userId}@example.com`;
    
    await page.click('button:has-text("新建用户")');
    await page.fill('input[name="username"]', `newuser_${userId}`);
    await page.fill('input[name="email"]', userEmail);
    await page.fill('input[name="password"]', 'NewUser123');
    await page.fill('input[name="confirmPassword"]', 'NewUser123');
    await TestHelpers.selectOption(page, '[name="department"]', '技术部');
    await TestHelpers.selectOption(page, '[name="role"]', '普通用户');
    await page.click('button:has-text("保存")');
    
    await TestHelpers.verifySuccessMessage(page, '用户创建成功');
    
    // 2. 新用户登录
    await TestHelpers.login(page, userEmail, 'NewUser123');
    await TestHelpers.verifySuccessMessage(page, '登录成功');
    
    // 3. 验证用户权限
    await page.goto('/expense-apply');
    await TestHelpers.verifyPageTitle(page, '费用申请');
    
    // 4. 验证无管理员权限
    await page.goto('/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });

  test('预算创建和执行流程', async ({ page }) => {
    // 1. 管理员创建预算
    await TestHelpers.adminLogin(page);
    await page.goto('/budgets');
    
    const budgetData = TestDataFactory.createBudgetData();
    await page.click('button:has-text("新建预算")');
    await page.fill('input[name="name"]', budgetData.name);
    await page.fill('input[name="amount"]', budgetData.amount.toString());
    await TestHelpers.selectOption(page, '[name="period"]', budgetData.period);
    await TestHelpers.selectOption(page, '[name="department"]', budgetData.department);
    await page.click('button:has-text("保存")');
    
    await TestHelpers.verifySuccessMessage(page, '预算创建成功');
    
    // 2. 用户创建费用申请（使用预算）
    await TestHelpers.userLogin(page);
    await page.goto('/expense-apply');
    
    const expenseData = TestDataFactory.createExpenseApplication();
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', '500');
    await TestHelpers.selectOption(page, '[name="budget"]', budgetData.name);
    await page.click('button:has-text("提交申请")');
    
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 3. 审批员审批
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/pending');
    
    const todoItem = page.locator(`.todo-item:has-text("${expenseData.title}")`).first();
    if (await todoItem.count() > 0) {
      await todoItem.locator('button:has-text("审批")').click();
      await page.click('button:has-text("通过")');
      await TestHelpers.confirmDialog(page);
      
      await TestHelpers.verifySuccessMessage(page, '审批通过');
    }
    
    // 4. 管理员查看预算执行情况
    await TestHelpers.adminLogin(page);
    await page.goto('/budgets');
    
    const budgetRow = page.locator(`.el-table__row:has-text("${budgetData.name}")`).first();
    await budgetRow.locator('button:has-text("详情")').click();
    
    await expect(page.locator('.budget-used')).toBeVisible();
    await expect(page.locator('.budget-remaining')).toBeVisible();
  });

  test('多角色切换和权限验证', async ({ page }) => {
    // 1. 管理员验证所有权限
    await TestHelpers.adminLogin(page);
    
    await page.goto('/users');
    await TestHelpers.verifyPageTitle(page, '用户管理');
    
    await page.goto('/budgets');
    await TestHelpers.verifyPageTitle(page, '预算管理');
    
    await page.goto('/reports/expense');
    await TestHelpers.verifyPageTitle(page, '费用统计');
    
    // 2. 审批员验证审批权限
    await TestHelpers.approverLogin(page);
    
    await page.goto('/approval/pending');
    await TestHelpers.verifyPageTitle(page, '待办审批');
    
    // 3. 普通用户验证受限权限
    await TestHelpers.userLogin(page);
    
    await page.goto('/expense-apply');
    await TestHelpers.verifyPageTitle(page, '费用申请');
    
    await page.goto('/loan/list');
    await TestHelpers.verifyPageTitle(page, '我的借款');
    
    await page.goto('/users');
    await expect(page.locator('.error-page')).toContainText('权限不足');
  });

  test('数据导出和报表生成流程', async ({ page }) => {
    // 1. 创建测试数据
    await TestHelpers.userLogin(page);
    await page.goto('/expense-apply');
    
    const expenseData = TestDataFactory.createExpenseApplication();
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', expenseData.title);
    await page.fill('input[name="amount"]', expenseData.amount.toString());
    await page.click('button:has-text("提交申请")');
    
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 2. 管理员生成报表
    await TestHelpers.adminLogin(page);
    await page.goto('/reports/expense');
    
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    
    await TestHelpers.waitForLoading(page);
    await expect(page.locator('.report-data')).toBeVisible();
    
    // 3. 导出报表
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出Excel")');
    
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.(xlsx|xls)$/);
  });
});
