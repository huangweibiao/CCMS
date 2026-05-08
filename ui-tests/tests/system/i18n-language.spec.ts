import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 多语言国际化E2E测试
 * 覆盖：语言切换、翻译完整性、RTL支持、日期数字格式
 */
test.describe('多语言国际化', () => {
  
  test('切换系统语言', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 打开语言选择器
    await page.click('.language-selector');
    
    // 切换到英文
    await page.click('.language-option:has-text("English")');
    
    // 验证界面语言已切换
    await expect(page.locator('.sidebar')).toContainText('Dashboard');
    await expect(page.locator('.sidebar')).toContainText('Expense');
    await expect(page.locator('.sidebar')).toContainText('Budget');
    await expect(page.locator('.sidebar')).toContainText('Approval');
    
    // 验证登录信息
    await expect(page.locator('.user-menu')).toContainText('Profile');
    await expect(page.locator('.user-menu')).toContainText('Logout');
    
    // 切换回中文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("简体中文")');
    
    // 验证已切换回中文
    await expect(page.locator('.sidebar')).toContainText('仪表盘');
    await expect(page.locator('.sidebar')).toContainText('费用管理');
  });

  test('登录页面多语言', async ({ page }) => {
    await page.goto('/login');
    
    // 验证默认中文
    await expect(page.locator('h1')).toContainText('用户登录');
    await expect(page.locator('button[type="submit"]')).toContainText('登录');
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 验证英文界面
    await expect(page.locator('h1')).toContainText('Login');
    await expect(page.locator('button[type="submit"]')).toContainText('Sign In');
    await expect(page.locator('input[type="text"]')).toHaveAttribute('placeholder', /username/i);
    
    // 切换到日文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("日本語")');
    
    // 验证日文界面
    await expect(page.locator('h1')).toContainText('ログイン');
  });

  test('费用申请页面多语言', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 验证页面标题
    await expect(page.locator('h1')).toContainText('Expense Application');
    
    // 验证按钮文本
    await expect(page.locator('button:has-text("New Application")')).toBeVisible();
    
    // 验证表格列名
    await expect(page.locator('th:has-text("Title")')).toBeVisible();
    await expect(page.locator('th:has-text("Amount")')).toBeVisible();
    await expect(page.locator('th:has-text("Status")')).toBeVisible();
    await expect(page.locator('th:has-text("Operation")')).toBeVisible();
    
    // 点击新建申请
    await page.click('button:has-text("New Application")');
    
    // 验证表单标签
    await expect(page.locator('label:has-text("Title")')).toBeVisible();
    await expect(page.locator('label:has-text("Amount")')).toBeVisible();
    await expect(page.locator('label:has-text("Description")')).toBeVisible();
    
    // 验证提交按钮
    await expect(page.locator('button:has-text("Submit")')).toBeVisible();
    await expect(page.locator('button:has-text("Cancel")')).toBeVisible();
  });

  test('日期格式本地化', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/history');
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 验证日期格式为MM/DD/YYYY
    const dateCells = await page.locator('.date-cell').all();
    for (const cell of dateCells.slice(0, 5)) {
      const text = await cell.textContent();
      // 验证日期格式包含/
      expect(text).toMatch(/\d{1,2}\/\d{1,2}\/\d{4}/);
    }
    
    // 切换到中文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("简体中文")');
    
    // 验证日期格式为YYYY-MM-DD
    for (const cell of await page.locator('.date-cell').all()) {
      const text = await cell.textContent();
      expect(text).toMatch(/\d{4}-\d{2}-\d{2}/);
    }
  });

  test('数字和货币格式本地化', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/budget/management');
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 验证货币格式
    const amountCells = await page.locator('.amount-cell').all();
    for (const cell of amountCells.slice(0, 5)) {
      const text = await cell.textContent();
      // 验证包含千分位逗号和小数点
      expect(text).toMatch(/\$?[\d,]+\.\d{2}/);
    }
    
    // 切换到中文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("简体中文")');
    
    // 验证中文货币格式
    for (const cell of await page.locator('.amount-cell').all()) {
      const text = await cell.textContent();
      // 验证包含人民币符号或元
      expect(text).toMatch(/(¥|元|CNY)/);
    }
  });

  test('消息提示多语言', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 执行操作触发消息
    await page.goto('/expense/application');
    await page.click('button:has-text("New Application")');
    await page.fill('input[name="title"]', 'Test Application');
    await page.fill('input[name="amount"]', '100');
    await page.click('button:has-text("Submit")');
    
    // 验证英文消息提示
    await expect(page.locator('.el-message--success')).toContainText('Application submitted successfully');
    
    // 切换到中文后再次测试
    await page.click('.language-selector');
    await page.click('.language-option:has-text("简体中文")');
    
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '中文测试');
    await page.fill('input[name="amount"]', '200');
    await page.click('button:has-text("提交申请")');
    
    // 验证中文消息提示
    await expect(page.locator('.el-message--success')).toContainText('申请提交成功');
  });

  test('验证提示多语言', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 提交空表单触发验证
    await page.click('button:has-text("Submit")');
    
    // 验证英文验证提示
    await expect(page.locator('.el-form-item__error')).toContainText('Title is required');
    await expect(page.locator('.el-form-item__error')).toContainText('Amount is required');
    
    // 切换到中文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("简体中文")');
    
    // 再次提交空表单
    await page.click('button:has-text("提交申请")');
    
    // 验证中文验证提示
    await expect(page.locator('.el-form-item__error')).toContainText('请输入标题');
    await expect(page.locator('.el-form-item__error')).toContainText('请输入金额');
  });

  test('语言偏好持久化', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 刷新页面
    await page.reload();
    
    // 验证语言设置已持久化
    await expect(page.locator('.sidebar')).toContainText('Dashboard');
    
    // 重新登录后验证
    await page.click('.logout-btn');
    await TestHelpers.userLogin(page);
    
    // 验证语言仍然是英文
    await expect(page.locator('.sidebar')).toContainText('Dashboard');
  });

  test('RTL语言支持', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 切换到阿拉伯语（RTL语言）
    await page.click('.language-selector');
    await page.click('.language-option:has-text("العربية")');
    
    // 验证RTL布局
    await expect(page.locator('body')).toHaveAttribute('dir', 'rtl');
    await expect(page.locator('.sidebar')).toHaveCSS('right', '0px');
    
    // 验证文本方向
    await expect(page.locator('.main-content')).toHaveCSS('direction', 'rtl');
    
    // 切换回LTR语言
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 验证恢复LTR布局
    await expect(page.locator('body')).toHaveAttribute('dir', 'ltr');
    await expect(page.locator('.sidebar')).toHaveCSS('left', '0px');
  });

  test('翻译完整性检查', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 切换到英文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("English")');
    
    // 遍历主要页面检查翻译
    const pages = [
      { url: '/dashboard', keywords: ['Dashboard', 'Overview', 'Statistics'] },
      { url: '/expense/application', keywords: ['Expense', 'Application', 'New'] },
      { url: '/budget/management', keywords: ['Budget', 'Management', 'Create'] },
      { url: '/approval/management', keywords: ['Approval', 'Pending', 'Approved'] },
      { url: '/user/profile', keywords: ['Profile', 'Settings', 'Security'] }
    ];
    
    for (const pageInfo of pages) {
      await page.goto(pageInfo.url);
      
      // 检查页面是否包含未翻译的中文
      const pageText = await page.locator('body').textContent();
      const chineseChars = pageText?.match(/[\u4e00-\u9fa5]/g);
      
      // 允许少量专业术语或用户名包含中文
      if (chineseChars && chineseChars.length > 10) {
        console.warn(`Page ${pageInfo.url} may have untranslated content: ${chineseChars.length} Chinese characters found`);
      }
      
      // 验证关键翻译存在
      for (const keyword of pageInfo.keywords) {
        await expect(page.locator('body')).toContainText(keyword);
      }
    }
  });

  test('语言包懒加载', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 监听网络请求
    const languageRequests: string[] = [];
    page.on('request', request => {
      const url = request.url();
      if (url.includes('lang') || url.includes('i18n')) {
        languageRequests.push(url);
      }
    });
    
    // 切换到日文
    await page.click('.language-selector');
    await page.click('.language-option:has-text("日本語")');
    
    // 验证语言包被加载
    await page.waitForTimeout(1000);
    
    const japaneseRequest = languageRequests.find(url => 
      url.includes('ja') || url.includes('jp') || url.includes('japanese')
    );
    
    expect(japaneseRequest).toBeTruthy();
  });
});
