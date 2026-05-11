import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 图表交互E2E测试
 * 覆盖：图表类型切换、数据筛选、钻取、联动、导出
 */
test.describe('图表交互功能', () => {
  
  test('图表类型切换', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    
    // 生成报表
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    
    // 等待图表加载
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 切换到柱状图
    await page.click('button:has-text("柱状图")');
    await expect(page.locator('.bar-chart')).toBeVisible();
    
    // 切换到折线图
    await page.click('button:has-text("折线图")');
    await expect(page.locator('.line-chart')).toBeVisible();
    
    // 切换到饼图
    await page.click('button:has-text("饼图")');
    await expect(page.locator('.pie-chart')).toBeVisible();
    
    // 切换到雷达图
    await page.click('button:has-text("雷达图")');
    await expect(page.locator('.radar-chart')).toBeVisible();
    
    // 切换到散点图
    await page.click('button:has-text("散点图")');
    await expect(page.locator('.scatter-chart')).toBeVisible();
  });

  test('图表数据筛选与联动', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    
    // 生成报表
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 点击饼图某一块
    await page.click('.pie-chart .chart-sector').first();
    
    // 验证其他图表联动更新
    await expect(page.locator('.bar-chart')).toContainText('已筛选');
    await expect(page.locator('.data-table')).toContainText('已筛选');
    
    // 清除筛选
    await page.click('button:has-text("清除筛选")');
    
    // 验证恢复原始数据
    await expect(page.locator('.bar-chart')).not.toContainText('已筛选');
  });

  test('图表数据钻取', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/dept-comparison');
    
    // 生成部门对比报表
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 双击部门柱状图进行下钻
    await page.dblclick('.bar-chart .bar-item:has-text("技术部")');
    
    // 验证下钻到部门明细
    await expect(page.locator('.chart-title')).toContainText('技术部费用明细');
    await expect(page.locator('.chart-container')).toContainText('前端组');
    await expect(page.locator('.chart-container')).toContainText('后端组');
    
    // 点击返回上级
    await page.click('button:has-text("返回")');
    
    // 验证回到部门汇总视图
    await expect(page.locator('.chart-title')).toContainText('部门费用对比');
  });

  test('图表悬停提示', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 悬停在柱状图上
    await page.hover('.bar-chart .bar-item').first();
    
    // 验证提示框显示
    await expect(page.locator('.chart-tooltip')).toBeVisible();
    await expect(page.locator('.chart-tooltip')).toContainText('金额');
    await expect(page.locator('.chart-tooltip')).toContainText('占比');
    
    // 悬停在饼图上
    await page.hover('.pie-chart .chart-sector').first();
    await expect(page.locator('.chart-tooltip')).toContainText('类别');
  });

  test('图表缩放与平移', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/expense-trend');
    
    // 生成趋势报表
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.line-chart', { timeout: 10000 });
    
    // 框选缩放
    await page.dragAndDrop(
      '.line-chart',
      '.line-chart',
      { sourcePosition: { x: 100, y: 100 }, targetPosition: { x: 300, y: 300 } }
    );
    
    // 验证缩放后显示
    await expect(page.locator('.chart-zoom-info')).toContainText('缩放');
    
    // 点击还原
    await page.click('button:has-text("还原")');
    await expect(page.locator('.chart-zoom-info')).not.toBeVisible();
  });

  test('图表数据导出', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 导出图表为图片
    const imagePromise = page.waitForEvent('download');
    await page.click('button:has-text("导出图片")');
    const imageDownload = await imagePromise;
    expect(imageDownload.suggestedFilename()).toMatch(/\.(png|jpg)$/);
    
    // 导出图表数据
    const dataPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出数据")');
    const dataDownload = await dataPromise;
    expect(dataDownload.suggestedFilename()).toMatch(/\.xlsx$/);
  });

  test('图表数据表格联动', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/statistics');
    await page.click('button:has-text("生成报表")');
    await page.waitForSelector('.chart-container', { timeout: 10000 });
    
    // 点击图表中的数据点
    await page.click('.bar-chart .bar-item').first();
    
    // 验证数据表格高亮对应行
    const highlightedRow = page.locator('.data-table .highlighted-row');
    await expect(highlightedRow).toBeVisible();
    
    // 点击表格行
    await page.click('.data-table tr').nth(2);
    
    // 验证图表高亮对应数据点
    await expect(page.locator('.bar-chart .highlighted-bar')).toBeVisible();
  });

  test('多图表布局配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/dashboard');
    
    // 添加图表组件
    await page.click('button:has-text("添加图表")');
    await page.click('.chart-type:has-text("费用趋势")');
    await page.click('button:has-text("确认")');
    
    // 拖拽调整布局
    const chart1 = page.locator('.dashboard-chart').first();
    const chart2 = page.locator('.dashboard-chart').nth(1);
    await chart1.dragTo(chart2);
    
    // 调整图表大小
    const resizeHandle = page.locator('.resize-handle').first();
    await resizeHandle.dragTo(resizeHandle, {
      targetPosition: { x: 200, y: 150 }
    });
    
    // 保存布局
    await page.click('button:has-text("保存布局")');
    await TestHelpers.verifySuccessMessage(page, '布局保存成功');
    
    // 刷新验证布局持久化
    await page.reload();
    await expect(page.locator('.dashboard-chart')).toHaveCount.greaterThan(1);
  });

  test('图表实时数据刷新', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/realtime');
    
    // 开启自动刷新
    await page.check('input[name="autoRefresh"]');
    await page.fill('input[name="refreshInterval"]', '5');
    
    // 记录初始数据
    const initialData = await page.locator('.chart-data-summary').textContent();
    
    // 等待自动刷新
    await page.waitForTimeout(6000);
    
    // 验证数据已更新（如果有新数据）
    const updatedData = await page.locator('.chart-data-summary').textContent();
    // 数据可能相同也可能不同，取决于是否有新数据产生
    console.log('Data updated from:', initialData, 'to:', updatedData);
    
    // 手动刷新
    await page.click('button:has-text("刷新")');
    await expect(page.locator('.last-update-time')).toContainText('刚刚');
  });

  test('图表对比分析', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/comparison');
    
    // 选择对比维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("对比维度"))');
    await page.click('.el-select-dropdown__item:has-text("同比")');
    
    // 选择时间范围
    await page.fill('input[name="currentPeriod"]', '2024-01');
    await page.fill('input[name="comparePeriod"]', '2023-01');
    
    // 生成对比报表
    await page.click('button:has-text("生成对比")');
    await page.waitForSelector('.comparison-chart', { timeout: 10000 });
    
    // 验证对比图表显示
    await expect(page.locator('.comparison-chart')).toContainText('2024年1月');
    await expect(page.locator('.comparison-chart')).toContainText('2023年1月');
    
    // 验证增长率显示
    await expect(page.locator('.growth-rate')).toBeVisible();
  });
});
