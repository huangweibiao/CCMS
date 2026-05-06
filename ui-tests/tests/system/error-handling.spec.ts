import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('错误处理和边界情况测试', () => {
  test.beforeEach(async ({ page }) => {
    await TestHelpers.userLogin(page);
  });

  test('网络错误处理', async ({ page }) => {
    // 模拟网络错误
    await page.route('**/api/**', route => {
      route.abort('failed');
    });

    await page.goto('/expense-apply');

    // 验证错误提示
    await TestHelpers.verifyErrorMessage(page, '网络错误');

    // 移除路由拦截
    await page.unroute('**/api/**');
  });

  test('服务器错误处理', async ({ page }) => {
    // 模拟服务器错误
    await page.route('**/api/expense/applications', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: '服务器内部错误' })
      });
    });

    await page.goto('/expense-apply');

    // 验证错误提示
    await TestHelpers.verifyErrorMessage(page, '服务器错误');

    // 移除路由拦截
    await page.unroute('**/api/expense/applications');
  });

  test('未授权访问处理', async ({ page }) => {
    // 模拟401错误
    await page.route('**/api/**', route => {
      route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ error: '未授权' })
      });
    });

    await page.goto('/expense-apply');

    // 验证重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);

    // 移除路由拦截
    await page.unroute('**/api/**');
  });

  test('404页面处理', async ({ page }) => {
    await page.goto('/non-existent-page');

    // 验证404页面
    await expect(page.locator('.error-page')).toContainText('404');
    await expect(page.locator('.error-page')).toContainText('页面未找到');

    // 验证返回首页按钮
    await page.click('button:has-text("返回首页")');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('表单验证错误', async ({ page }) => {
    await page.goto('/expense-apply');
    await page.click('button:has-text("新建申请")');

    // 提交空表单
    await page.click('button:has-text("提交申请")');

    // 验证表单验证错误
    const formErrors = page.locator('.el-form-item__error');
    await expect(formErrors.first()).toBeVisible();
  });

  test('数据格式验证', async ({ page }) => {
    await page.goto('/expense-apply');
    await page.click('button:has-text("新建申请")');

    // 输入无效金额
    await page.fill('input[name="amount"]', 'abc');
    await page.click('button:has-text("提交申请")');

    // 验证格式错误
    const formErrors = page.locator('.el-form-item__error');
    await expect(formErrors).toContainText(['请输入有效的数字']);
  });

  test('超时处理', async ({ page }) => {
    // 模拟慢速响应
    await page.route('**/api/**', async route => {
      await page.waitForTimeout(30000); // 30秒延迟
      await route.continue();
    });

    await page.goto('/expense-apply');

    // 验证超时提示
    await TestHelpers.verifyErrorMessage(page, '请求超时');

    // 移除路由拦截
    await page.unroute('**/api/**');
  });

  test('并发操作处理', async ({ page }) => {
    await page.goto('/expense-apply');

    // 快速点击多次
    await page.click('button:has-text("新建申请")');
    await page.click('button:has-text("新建申请")');
    await page.click('button:has-text("新建申请")');

    // 验证只打开一个弹窗
    const modals = page.locator('.el-dialog');
    await expect(modals).toHaveCount(1);
  });

  test('大数据量处理', async ({ page }) => {
    // 模拟大量数据
    await page.route('**/api/expense/applications', route => {
      const largeData = {
        data: Array(1000).fill(null).map((_, i) => ({
          id: i,
          title: `测试申请${i}`,
          amount: 100 + i,
          status: 'pending'
        })),
        total: 1000
      };
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(largeData)
      });
    });

    await page.goto('/expense-apply');

    // 验证页面正常加载
    await expect(page.locator('.el-table')).toBeVisible();

    // 移除路由拦截
    await page.unroute('**/api/expense/applications');
  });

  test('特殊字符处理', async ({ page }) => {
    await page.goto('/expense-apply');
    await page.click('button:has-text("新建申请")');

    // 输入特殊字符
    await page.fill('input[name="title"]', '<script>alert("xss")</script>');
    await page.fill('input[name="amount"]', '100');
    await page.click('button:has-text("提交申请")');

    // 验证XSS被转义
    const titleCell = page.locator('.el-table__row').first().locator('td').first();
    const text = await titleCell.textContent();
    expect(text).not.toContain('<script>');
  });

  test('长文本处理', async ({ page }) => {
    await page.goto('/expense-apply');
    await page.click('button:has-text("新建申请")');

    // 输入超长文本
    const longText = 'a'.repeat(10000);
    await page.fill('textarea[name="description"]', longText);

    // 验证文本被截断或正常处理
    const descriptionField = page.locator('textarea[name="description"]');
    const value = await descriptionField.inputValue();
    expect(value.length).toBeLessThanOrEqual(10000);
  });

  test('空数据处理', async ({ page }) => {
    // 模拟空数据响应
    await page.route('**/api/expense/applications', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ data: [], total: 0 })
      });
    });

    await page.goto('/expense-apply');

    // 验证空状态显示
    await expect(page.locator('.el-empty')).toBeVisible();
    await expect(page.locator('.el-empty')).toContainText('暂无数据');

    // 移除路由拦截
    await page.unroute('**/api/expense/applications');
  });

  test('浏览器返回按钮处理', async ({ page }) => {
    await page.goto('/dashboard');
    await page.goto('/expense-apply');

    // 打开弹窗
    await page.click('button:has-text("新建申请")');
    const modal = page.locator('.el-dialog');
    await expect(modal).toBeVisible();

    // 点击浏览器返回按钮
    await page.goBack();

    // 验证页面正确返回
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('多标签页同步', async ({ page, context }) => {
    await page.goto('/expense-apply');

    // 打开新标签页
    const newPage = await context.newPage();
    await newPage.goto('/expense-apply');

    // 在一个标签页创建申请
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '多标签页测试');
    await page.fill('input[name="amount"]', '100');
    await page.click('button:has-text("提交申请")');

    await TestHelpers.verifySuccessMessage(page, '申请提交成功');

    // 在另一个标签页刷新查看
    await newPage.reload();
    await TestHelpers.verifyTableContains(newPage, '多标签页测试');

    await newPage.close();
  });
});
