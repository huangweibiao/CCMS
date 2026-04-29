import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('系统功能和响应式测试', () => {
  test('响应式布局适配测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 桌面端测试 (1920x1080)
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/dashboard');
    
    // 验证桌面布局
    await expect(page.locator('.desktop-nav')).toBeVisible();
    await expect(page.locator('.sidebar')).toBeVisible();
    
    // 平板端测试 (768x1024)
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.reload();
    
    // 验证平板布局
    await expect(page.locator('.tablet-menu')).toBeVisible();
    
    // 移动端测试 (375x667)
    await page.setViewportSize({ width: 375, height: 667 });
    await page.reload();
    
    // 验证移动布局
    await expect(page.locator('.mobile-header')).toBeVisible();
    await expect(page.locator('.hamburger-menu')).toBeVisible();
    
    // 测试移动菜单展开
    await page.click('.hamburger-menu');
    await expect(page.locator('.mobile-nav')).toBeVisible();
  });

  test('导航菜单交互测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 验证默认导航项
    await expect(page.locator('.nav-item.active')).toContainText('仪表板');
    
    // 点击费用申请菜单
    await page.click('.nav-item:has-text("费用申请")');
    await expect(page).toHaveURL(/.*\/expense\/application/);
    await TestHelpers.verifyPageTitle(page, '费用申请');
    
    // 点击借款管理菜单
    await page.click('.nav-item:has-text("借款管理")');
    await expect(page).toHaveURL(/.*\/loan\/list/);
    
    // 返回仪表板
    await page.click('.nav-item:has-text("仪表板")');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('消息通知系统测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 点击消息通知图标
    await page.click('.notification-bell');
    
    // 验证通知面板显示
    const notificationPanel = page.locator('.notification-panel');
    await expect(notificationPanel).toBeVisible();
    
    // 验证通知列表
    const notifications = page.locator('.notification-item');
    if (await notifications.count() > 0) {
      await expect(notifications.first()).toBeVisible();
      
      // 标记为已读
      await notifications.first().locator('.mark-read').click();
      await TestHelpers.verifySuccessMessage(page, '标记为已读');
    }
    
    // 关闭通知面板
    await page.click('.notification-panel .close-btn');
    await expect(notificationPanel).not.toBeVisible();
  });

  test('系统设置功能测试', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    
    // 导航到系统设置
    await page.goto('/admin/settings');
    await TestHelpers.verifyPageTitle(page, '系统设置');
    
    // 修改系统参数
    await page.fill('input[name="systemName"]', 'CCMS测试系统');
    await page.fill('input[name="pageSize"]', '20');
    await page.selectOption('select[name="dateFormat"]', 'YYYY-MM-DD');
    
    // 保存设置
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 验证设置生效（重新加载页面）
    await page.reload();
    await expect(page.locator('input[name="systemName"]')).toHaveValue('CCMS测试系统');
  });

  test('数据表分页和排序测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 验证分页控件
    const pagination = page.locator('.el-pagination');
    await expect(pagination).toBeVisible();
    
    // 点击下一页
    await page.click('.el-pager .number:last-child');
    await page.waitForTimeout(500);
    
    // 验证页面切换
    await expect(page.locator('.current-page')).toContainText('2');
    
    // 测试排序功能
    await page.click('.sortable-column:has-text("申请时间")');
    await page.waitForTimeout(500);
    
    // 验证排序状态
    await expect(page.locator('.sortable-column:has-text("申请时间")')).toHaveClass(/ascending/);
  });

  test('搜索和筛选功能测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 测试搜索功能
    const searchText = '测试';
    await page.fill('.search-input', searchText);
    await page.click('.search-button');
    
    // 验证搜索结果
    await TestHelpers.verifyTableContains(page, searchText);
    
    // 测试状态筛选
    await page.selectOption('.status-filter', 'pending');
    await page.click('.filter-button');
    
    // 验证状态筛选结果
    const filteredItems = page.locator('.application-item.pending');
    if (await filteredItems.count() > 0) {
      await expect(filteredItems.first().locator('.status-badge')).toHaveText('待审批');
    }
    
    // 清除筛选条件
    await page.click('.clear-filters');
    await expect(page.locator('.search-input')).toHaveValue('');
  });

  test('弹窗和模态框交互测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 点击新建申请按钮打开弹窗
    await page.click('button:has-text("新建申请")');
    
    // 验证弹窗显示
    const modal = page.locator('.el-dialog');
    await expect(modal).toBeVisible();
    await expect(modal.locator('.el-dialog__title')).toContainText('新建费用申请');
    
    // 点击取消按钮关闭弹窗
    await page.click('.el-dialog .el-button:has-text("取消")');
    await expect(modal).not.toBeVisible();
    
    // 再次打开弹窗
    await page.click('button:has-text("新建申请")');
    await expect(modal).toBeVisible();
    
    // 点击弹窗外部关闭
    await page.click('.el-dialog__wrapper', { position: { x: 10, y: 10 } });
    await expect(modal).not.toBeVisible();
  });

  test('加载状态和错误处理测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 模拟网络延迟
    await page.route('**/api/expense/applications', async route => {
      await page.waitForTimeout(2000); // 2秒延迟
      await route.continue();
    });
    
    await page.goto('/expense/application');
    
    // 验证加载状态显示
    const loadingIndicator = page.locator('.loading-spinner');
    await expect(loadingIndicator).toBeVisible();
    
    // 等待加载完成
    await page.waitForTimeout(2500);
    await expect(loadingIndicator).not.toBeVisible();
    
    // 模拟API错误
    await page.route('**/api/expense/applications', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ error: '服务器内部错误' })
      });
    });
    
    await page.reload();
    
    // 验证错误处理
    await expect(page.locator('.error-message')).toContainText('服务器错误');
    
    // 移除路由拦截
    await page.unroute('**/api/expense/applications');
  });

  test('数据验证和表单错误处理', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 测试空表单提交
    await page.click('button:has-text("提交")');
    
    // 验证表单验证错误
    const formErrors = page.locator('.el-form-item__error');
    await expect(formErrors.first()).toBeVisible();
    
    // 测试无效数据
    await page.fill('input[name="amount"]', 'abc');
    await page.click('button:has-text("提交")');
    
    // 验证数字格式错误
    await expect(formErrors).toContainText(['请输入有效的数字']);
    
    // 测试负值
    await page.fill('input[name="amount"]', '-100');
    await page.click('button:has-text("提交")');
    
    // 验证正值要求
    await expect(formErrors).toContainText(['金额必须大于0']);
    
    // 测试超大值
    await page.fill('input[name="amount"]', '9999999');
    await page.click('button:has-text("提交")');
    
    // 验证最大值限制
    await expect(formErrors).toContainText(['金额超出限制']);
  });

  test('快捷键和辅助功能测试', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 测试ESC键关闭弹窗
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    const modal = page.locator('.el-dialog');
    await expect(modal).toBeVisible();
    
    // 按ESC键
    await page.keyboard.press('Escape');
    await expect(modal).not.toBeVisible();
    
    // 测试回车键提交表单
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '测试申请');
    
    // 在表单内按回车
    await page.keyboard.press('Enter');
    
    // 验证表单验证触发
    const formErrors = page.locator('.el-form-item__error');
    await expect(formErrors).toBeVisible();
    
    // 测试Tab键导航
    await page.keyboard.press('Tab');
    await expect(page.locator('input[name="title"]')).toBeFocused();
  });

  test('浏览器兼容性测试', async ({ page }) => {
    // 这个测试在每个浏览器项目中执行
    await TestHelpers.userLogin(page);
    
    // 验证基本功能在所有浏览器中正常工作
    await page.goto('/dashboard');
    await TestHelpers.verifyPageTitle(page, '仪表板');
    
    // 验证CSS样式正确加载
    await expect(page.locator('.dashboard-header')).toHaveCSS('display', 'flex');
    
    // 验证JavaScript功能
    await page.click('.nav-toggle');
    await expect(page.locator('.sidebar')).toHaveClass(/collapsed/);
    
    // 验证图表渲染
    const chart = page.locator('.dashboard-chart');
    if (await chart.isVisible()) {
      await expect(chart).toBeVisible();
    }
  });

  test('性能和加载速度测试', async ({ page }) => {
    // 记录页面加载时间
    const startTime = Date.now();
    
    await TestHelpers.userLogin(page);
    await page.goto('/dashboard');
    
    const loadTime = Date.now() - startTime;
    
    // 验证页面在合理时间内加载完成
    expect(loadTime).toBeLessThan(5000); // 5秒内完成加载
    
    // 验证关键资源加载
    await expect(page.locator('.dashboard-content')).toBeVisible();
    
    // 测试页面切换性能
    const navigationStart = Date.now();
    await page.goto('/expense/application');
    const navigationTime = Date.now() - navigationStart;
    
    expect(navigationTime).toBeLessThan(3000); // 3秒内完成页面切换
    
    // 验证数据表渲染性能
    const table = page.locator('.el-table');
    await expect(table).toBeVisible({ timeout: 10000 });
  });
});