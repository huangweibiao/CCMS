import { Page, expect } from '@playwright/test';

/**
 * 测试助手函数库
 */
export class TestHelpers {
  /**
   * 用户登录
   */
  static async login(page: Page, username: string, password: string): Promise<void> {
    await page.goto('/login');
    await page.fill('input[type="text"]', username);
    await page.fill('input[type="password"]', password);
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
  }

  /**
   * 管理员登录
   */
  static async adminLogin(page: Page): Promise<void> {
    await this.login(page, 'admin@example.com', 'admin123');
  }

  /**
   * 审批员登录
   */
  static async approverLogin(page: Page): Promise<void> {
    await this.login(page, 'approver@example.com', 'approver123');
  }

  /**
   * 普通用户登录
   */
  static async userLogin(page: Page): Promise<void> {
    await this.login(page, 'user@example.com', 'user123');
  }

  /**
   * 等待元素出现
   */
  static async waitForElement(page: Page, selector: string, timeout = 10000): Promise<void> {
    await page.waitForSelector(selector, { timeout });
  }

  /**
   * 获取表格数据
   */
  static async getTableData(page: Page, selector = '.el-table'): Promise<string[][]> {
    const table = page.locator(selector);
    const rows = table.locator('tbody tr');
    const data: string[][] = [];
    
    const count = await rows.count();
    for (let i = 0; i < count; i++) {
      const row = rows.nth(i);
      const cells = row.locator('td');
      const rowData: string[] = [];
      
      const cellCount = await cells.count();
      for (let j = 0; j < cellCount; j++) {
        const cell = cells.nth(j);
        const text = await cell.textContent();
        rowData.push(text?.trim() || '');
      }
      data.push(rowData);
    }
    
    return data;
  }

  /**
   * 验证表单字段
   */
  static async validateFormField(page: Page, fieldName: string, expectedValue: string): Promise<void> {
    const field = page.locator(`[name="${fieldName}"]`);
    await expect(field).toHaveValue(expectedValue);
  }

  /**
   * 截屏并保存
   */
  static async takeScreenshot(page: Page, name: string): Promise<void> {
    await page.screenshot({ path: `screenshots/${name}-${Date.now()}.png` });
  }

  /**
   * 确认对话框
   */
  static async confirmDialog(page: Page): Promise<void> {
    await page.click('.el-button--primary:has-text("确认")');
    await page.waitForTimeout(500);
  }

  /**
   * 取消对话框
   */
  static async cancelDialog(page: Page): Promise<void> {
    await page.click('.el-button:has-text("取消")');
    await page.waitForTimeout(500);
  }

  /**
   * 验证成功消息
   */
  static async verifySuccessMessage(page: Page, expectedText?: string): Promise<void> {
    const message = page.locator('.el-message--success');
    await expect(message).toBeVisible();
    if (expectedText) {
      await expect(message).toContainText(expectedText);
    }
  }

  /**
   * 验证错误消息
   */
  static async verifyErrorMessage(page: Page, expectedText?: string): Promise<void> {
    const message = page.locator('.el-message--error');
    await expect(message).toBeVisible();
    if (expectedText) {
      await expect(message).toContainText(expectedText);
    }
  }
}

/**
 * 测试数据工厂
 */
export class TestDataFactory {
  /**
   * 生成随机费用申请数据
   */
  static createExpenseApplication() {
    const id = Math.random().toString(36).substring(7);
    return {
      title: `测试费用申请-${id}`,
      amount: Math.floor(Math.random() * 1000) + 100,
      category: '办公用品',
      description: `自动化测试生成的费用申请-${id}`
    };
  }

  /**
   * 生成随机预算数据
   */
  static createBudgetData() {
    const id = Math.random().toString(36).substring(7);
    return {
      name: `测试预算-${id}`,
      amount: Math.floor(Math.random() * 10000) + 1000,
      period: '2024-Q1',
      department: '技术部'
    };
  }

  /**
   * 生成随机借款数据
   */
  static createLoanData() {
    const id = Math.random().toString(36).substring(7);
    return {
      purpose: `测试借款-${id}`,
      amount: Math.floor(Math.random() * 5000) + 500,
      repaymentPeriod: 12
    };
  }
}

/**
 * 页面断言库
 */
export class PageAssertions {
  /**
   * 验证页面标题
   */
  static async verifyPageTitle(page: Page, expectedTitle: string): Promise<void> {
    await expect(page.locator('h1')).toContainText(expectedTitle);
  }

  /**
   * 验证导航菜单项
   */
  static async verifyNavMenu(page: Page, menuText: string): Promise<void> {
    const menuItem = page.locator(`.el-menu-item:has-text("${menuText}")`);
    await expect(menuItem).toBeVisible();
  }

  /**
   * 验证表格数据存在
   */
  static async verifyTableContains(page: Page, searchText: string): Promise<void> {
    const table = page.locator('.el-table');
    await expect(table).toContainText(searchText);
  }

  /**
   * 验证页面标题
   */
  static async verifyPageTitle(page: Page, expectedTitle: string): Promise<void> {
    await expect(page.locator('h1')).toContainText(expectedTitle);
  }

  /**
   * 验证元素包含文本
   */
  static async verifyElementContains(page: Page, selector: string, text: string): Promise<void> {
    await expect(page.locator(selector)).toContainText(text);
  }

  /**
   * 等待并点击元素
   */
  static async waitAndClick(page: Page, selector: string, timeout = 10000): Promise<void> {
    await page.waitForSelector(selector, { timeout });
    await page.click(selector);
  }

  /**
   * 清除输入框并填写
   */
  static async clearAndFill(page: Page, selector: string, value: string): Promise<void> {
    await page.fill(selector, '');
    await page.fill(selector, value);
  }

  /**
   * 选择下拉选项（Element UI el-select）
   */
  static async selectOption(page: Page, selector: string, optionText: string): Promise<void> {
    await page.click(selector);
    await page.click(`.el-select-dropdown__item:has-text("${optionText}")`);
  }

  /**
   * 上传文件
   */
  static async uploadFile(page: Page, selector: string, fileName: string, mimeType: string): Promise<void> {
    await page.setInputFiles(selector, {
      name: fileName,
      mimeType: mimeType,
      buffer: Buffer.from('test file content')
    });
  }

  /**
   * 等待加载完成
   */
  static async waitForLoading(page: Page, timeout = 10000): Promise<void> {
    await page.waitForSelector('.el-loading-mask', { state: 'hidden', timeout });
  }

  /**
   * 生成随机还款数据
   */
  static createRepaymentData() {
    const id = Math.random().toString(36).substring(7);
    return {
      amount: Math.floor(Math.random() * 1000) + 100,
      paymentMethod: '银行卡',
      paymentDate: new Date().toISOString().split('T')[0],
      remark: `测试还款-${id}`
    };
  }

  /**
   * 生成随机报销数据
   */
  static createReimbursementData() {
    const id = Math.random().toString(36).substring(7);
    return {
      title: `测试报销-${id}`,
      totalAmount: Math.floor(Math.random() * 2000) + 500,
      items: [
        { description: '交通费', amount: Math.floor(Math.random() * 500) + 100, category: '交通费' },
        { description: '住宿费', amount: Math.floor(Math.random() * 800) + 200, category: '住宿费' }
      ]
    };
  }

  /**
   * 生成随机部门数据
   */
  static createDepartmentData() {
    const id = Math.random().toString(36).substring(7);
    return {
      name: `测试部门-${id}`,
      code: `DEPT-${id.toUpperCase()}`,
      description: `自动化测试部门-${id}`
    };
  }

  /**
   * 生成随机角色数据
   */
  static createRoleData() {
    const id = Math.random().toString(36).substring(7);
    return {
      name: `测试角色-${id}`,
      description: `自动化测试角色-${id}`,
      permissions: ['expense_view', 'expense_apply']
    };
  }
}