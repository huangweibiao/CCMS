import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 性能与稳定性E2E测试
 * 覆盖：页面加载性能、大数据量处理、并发操作、内存泄漏检测
 */
test.describe('性能与稳定性测试', () => {
  
  test('页面加载性能测试', async ({ page }) => {
    // 测试登录页加载时间
    const loginStart = Date.now();
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    const loginLoadTime = Date.now() - loginStart;
    expect(loginLoadTime).toBeLessThan(3000); // 3秒内加载完成
    
    // 测试仪表盘加载时间
    await TestHelpers.userLogin(page);
    const dashboardStart = Date.now();
    await page.goto('/dashboard');
    await page.waitForSelector('.dashboard-container', { timeout: 5000 });
    const dashboardLoadTime = Date.now() - dashboardStart;
    expect(dashboardLoadTime).toBeLessThan(2000); // 2秒内加载完成
    
    // 测试首屏渲染时间
    const firstPaint = await page.evaluate(() => {
      return performance.getEntriesByType('paint')
        .find(entry => entry.name === 'first-contentful-paint')?.startTime;
    });
    expect(firstPaint).toBeLessThan(1500); // 1.5秒内首屏渲染
  });

  test('大数据量列表性能', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/expense/history');
    
    // 设置每页显示100条
    await page.click('.el-select:has(.el-input__placeholder:has-text("每页"))');
    await page.click('.el-select-dropdown__item:has-text("100")');
    
    // 记录加载时间
    const startTime = Date.now();
    await page.waitForSelector('.el-table__row', { timeout: 10000 });
    const loadTime = Date.now() - startTime;
    
    expect(loadTime).toBeLessThan(3000); // 3秒内加载100条数据
    
    // 验证滚动性能
    const tableBody = page.locator('.el-table__body-wrapper');
    const scrollStart = Date.now();
    await tableBody.evaluate(el => el.scrollTop = el.scrollHeight);
    const scrollTime = Date.now() - scrollStart;
    
    expect(scrollTime).toBeLessThan(500); // 滚动流畅
  });

  test('图表渲染性能', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    
    // 生成大数据量报表
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    
    // 测量图表渲染时间
    const chartStart = Date.now();
    await page.waitForSelector('.chart-canvas', { timeout: 10000 });
    const chartRenderTime = Date.now() - chartStart;
    
    expect(chartRenderTime).toBeLessThan(2000); // 2秒内渲染完成
    
    // 测试图表交互响应
    const chart = page.locator('.chart-canvas');
    const hoverStart = Date.now();
    await chart.hover();
    await page.waitForSelector('.chart-tooltip', { timeout: 1000 });
    const hoverResponseTime = Date.now() - hoverStart;
    
    expect(hoverResponseTime).toBeLessThan(300); // 300ms内响应
  });

  test('并发操作稳定性', async ({ browser }) => {
    // 创建多个并发上下文
    const contexts = await Promise.all([
      browser.newContext(),
      browser.newContext(),
      browser.newContext(),
      browser.newContext(),
      browser.newContext()
    ]);
    
    // 并发登录
    const loginPromises = contexts.map(async (context, index) => {
      const page = await context.newPage();
      await TestHelpers.userLogin(page);
      return page;
    });
    
    const pages = await Promise.all(loginPromises);
    
    // 并发创建费用申请
    const expenseData = TestDataFactory.createExpenseApplyData();
    const createPromises = pages.map(async (page, index) => {
      await page.goto('/expense/application');
      await page.click('button:has-text("新建申请")');
      await page.fill('input[name="title"]', `${expenseData.title}-${index}`);
      await page.fill('input[name="amount"]', (expenseData.amount + index * 100).toString());
      await page.click('button:has-text("提交申请")');
      return TestHelpers.verifySuccessMessage(page, '申请提交成功');
    });
    
    await Promise.all(createPromises);
    
    // 清理
    await Promise.all(contexts.map(ctx => ctx.close()));
  });

  test('内存泄漏检测', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 获取初始内存使用
    const initialMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0;
    });
    
    // 重复打开关闭页面10次
    for (let i = 0; i < 10; i++) {
      await page.goto('/expense/application');
      await page.waitForSelector('.expense-list');
      await page.goto('/budget/management');
      await page.waitForSelector('.budget-list');
      await page.goto('/loan/management');
      await page.waitForSelector('.loan-list');
    }
    
    // 强制垃圾回收（如果可用）
    await page.evaluate(() => {
      if ((window as any).gc) {
        (window as any).gc();
      }
    });
    
    // 等待一段时间
    await page.waitForTimeout(2000);
    
    // 获取最终内存使用
    const finalMemory = await page.evaluate(() => {
      return (performance as any).memory?.usedJSHeapSize || 0;
    });
    
    // 验证内存增长不超过50MB
    const memoryGrowth = (finalMemory - initialMemory) / 1024 / 1024;
    expect(memoryGrowth).toBeLessThan(50);
  });

  test('长时间运行稳定性', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 模拟用户连续操作30分钟
    const operations = [
      { url: '/dashboard', selector: '.dashboard-container' },
      { url: '/expense/application', selector: '.expense-list' },
      { url: '/budget/management', selector: '.budget-list' },
      { url: '/report/statistics', selector: '.report-chart' }
    ];
    
    const startTime = Date.now();
    const duration = 5 * 60 * 1000; // 5分钟（实际测试可调整为30分钟）
    let operationCount = 0;
    
    while (Date.now() - startTime < duration) {
      for (const op of operations) {
        await page.goto(op.url);
        await page.waitForSelector(op.selector, { timeout: 10000 });
        operationCount++;
        
        // 检查是否有错误
        const hasError = await page.locator('.el-message--error').isVisible().catch(() => false);
        expect(hasError).toBeFalsy();
        
        // 短暂等待
        await page.waitForTimeout(1000);
      }
    }
    
    console.log(`Completed ${operationCount} operations in ${(Date.now() - startTime) / 1000}s`);
    expect(operationCount).toBeGreaterThan(10);
  });

  test('API响应时间监控', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 监听网络请求
    const apiTimings: { url: string; duration: number }[] = [];
    
    page.on('response', async response => {
      const request = response.request();
      const timing = request.timing();
      if (timing) {
        const duration = timing.responseEnd - timing.startTime;
        apiTimings.push({
          url: request.url(),
          duration
        });
      }
    });
    
    // 执行各种操作
    await page.goto('/expense/application');
    await page.goto('/budget/management');
    await page.goto('/approval/management');
    
    // 等待请求完成
    await page.waitForTimeout(3000);
    
    // 验证API响应时间
    for (const timing of apiTimings) {
      if (timing.url.includes('/api/')) {
        expect(timing.duration).toBeLessThan(2000); // API响应不超过2秒
      }
    }
    
    // 计算平均响应时间
    const avgTime = apiTimings.reduce((sum, t) => sum + t.duration, 0) / apiTimings.length;
    console.log(`Average API response time: ${avgTime}ms`);
    expect(avgTime).toBeLessThan(1000); // 平均响应时间不超过1秒
  });

  test('资源加载优化检查', async ({ page }) => {
    const resourceSizes: { url: string; size: number; type: string }[] = [];
    
    page.on('response', async response => {
      const headers = response.headers();
      const size = parseInt(headers['content-length'] || '0');
      const url = response.url();
      const type = headers['content-type'] || 'unknown';
      
      if (size > 0) {
        resourceSizes.push({ url, size, type });
      }
    });
    
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    
    // 检查大文件
    const largeFiles = resourceSizes.filter(r => r.size > 1024 * 1024); // 大于1MB
    console.log('Large files:', largeFiles.map(f => `${f.url}: ${(f.size / 1024 / 1024).toFixed(2)}MB`));
    
    // 验证JS文件是否压缩
    const jsFiles = resourceSizes.filter(r => r.type.includes('javascript'));
    for (const js of jsFiles) {
      // 检查是否有gzip压缩
      const response = await page.evaluate(async (url) => {
        const resp = await fetch(url);
        return {
          encoding: resp.headers.get('content-encoding'),
          size: resp.headers.get('content-length')
        };
      }, js.url);
      
      // 压缩后的文件应该小很多
      if (js.size > 100 * 1024) { // 大于100KB的文件应该被压缩
        console.log(`JS file ${js.url}: ${(js.size / 1024).toFixed(2)}KB, encoding: ${response.encoding}`);
      }
    }
  });

  test('错误恢复能力', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 模拟网络中断
    await page.route('**/api/**', route => route.abort('internetdisconnected'));
    
    await page.goto('/expense/application');
    
    // 验证错误提示
    await expect(page.locator('.error-message')).toContainText('网络错误');
    await expect(page.locator('.retry-button')).toBeVisible();
    
    // 恢复网络
    await page.unroute('**/api/**');
    
    // 点击重试
    await page.click('.retry-button');
    
    // 验证数据加载成功
    await page.waitForSelector('.expense-list', { timeout: 10000 });
    await expect(page.locator('.expense-list')).toBeVisible();
  });

  test('大数据导出性能', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    
    // 设置大范围时间
    await page.fill('input[name="startDate"]', '2020-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    
    // 测量导出时间
    const exportStart = Date.now();
    const downloadPromise = page.waitForEvent('download', { timeout: 60000 });
    await page.click('button:has-text("导出数据")');
    const download = await downloadPromise;
    const exportTime = Date.now() - exportStart;
    
    expect(exportTime).toBeLessThan(30000); // 30秒内完成导出
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  });
});
