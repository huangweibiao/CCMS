import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 附件管理E2E测试
 * 覆盖：附件上传、预览、下载、删除、批量管理
 */
test.describe('附件管理系统', () => {
  
  test('单文件上传功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 填写基本信息
    await page.fill('input[name="title"]', '附件测试申请');
    await page.fill('input[name="amount"]', '1000');
    
    // 上传单个文件
    await page.setInputFiles('input[type="file"]', {
      name: 'receipt.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('test receipt content')
    });
    
    // 验证上传成功
    await expect(page.locator('.upload-list')).toContainText('receipt.pdf');
    await expect(page.locator('.upload-status')).toContainText('上传成功');
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 验证附件已关联
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', '附件测试申请');
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("查看")');
    await expect(page.locator('.attachment-list')).toContainText('receipt.pdf');
  });

  test('多文件批量上传', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 批量上传多个文件
    await page.setInputFiles('input[type="file"]', [
      {
        name: 'invoice1.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('invoice 1 content')
      },
      {
        name: 'invoice2.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('invoice 2 content')
      },
      {
        name: 'receipt.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('receipt image content')
      }
    ]);
    
    // 验证所有文件上传成功
    await expect(page.locator('.upload-list')).toContainText('invoice1.pdf');
    await expect(page.locator('.upload-list')).toContainText('invoice2.pdf');
    await expect(page.locator('.upload-list')).toContainText('receipt.jpg');
    
    // 验证上传进度
    await expect(page.locator('.upload-progress')).toHaveCount(3);
  });

  test('大文件上传与分片', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 创建大文件（5MB）
    const largeBuffer = Buffer.alloc(5 * 1024 * 1024, 'x');
    
    // 上传大文件
    await page.setInputFiles('input[type="file"]', {
      name: 'large-document.pdf',
      mimeType: 'application/pdf',
      buffer: largeBuffer
    });
    
    // 验证分片上传
    await expect(page.locator('.upload-progress')).toBeVisible();
    await expect(page.locator('.chunk-upload-indicator')).toBeVisible();
    
    // 等待上传完成
    await page.waitForSelector('.upload-status:has-text("上传成功")', { timeout: 60000 });
    
    // 验证文件大小显示
    await expect(page.locator('.file-size')).toContainText('5MB');
  });

  test('文件类型限制验证', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 尝试上传不允许的文件类型
    await page.setInputFiles('input[type="file"]', {
      name: 'malware.exe',
      mimeType: 'application/x-msdownload',
      buffer: Buffer.from('executable content')
    });
    
    // 验证错误提示
    await expect(page.locator('.el-message--error')).toContainText('不支持的文件类型');
    await expect(page.locator('.upload-list')).not.toContainText('malware.exe');
    
    // 尝试上传允许的文件类型
    await page.setInputFiles('input[type="file"]', {
      name: 'document.docx',
      mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      buffer: Buffer.from('word document content')
    });
    
    // 验证上传成功
    await expect(page.locator('.upload-list')).toContainText('document.docx');
  });

  test('文件大小限制验证', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 创建超大文件（超过限制）
    const oversizedBuffer = Buffer.alloc(100 * 1024 * 1024, 'x');
    
    // 尝试上传超大文件
    await page.setInputFiles('input[type="file"]', {
      name: 'oversized.zip',
      mimeType: 'application/zip',
      buffer: oversizedBuffer
    });
    
    // 验证错误提示
    await expect(page.locator('.el-message--error')).toContainText('文件大小超过限制');
  });

  test('图片预览功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 上传图片
    await page.setInputFiles('input[type="file"]', {
      name: 'invoice.png',
      mimeType: 'image/png',
      buffer: Buffer.from('png image content')
    });
    
    // 点击预览
    await page.click('.upload-list .preview-btn');
    
    // 验证预览弹窗
    await expect(page.locator('.image-preview-modal')).toBeVisible();
    await expect(page.locator('.preview-image')).toBeVisible();
    
    // 关闭预览
    await page.click('.preview-close');
    await expect(page.locator('.image-preview-modal')).not.toBeVisible();
  });

  test('PDF预览功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 上传PDF
    await page.setInputFiles('input[type="file"]', {
      name: 'document.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('pdf content')
    });
    
    // 点击预览
    await page.click('.upload-list .preview-btn');
    
    // 验证PDF预览器
    await expect(page.locator('.pdf-preview-modal')).toBeVisible();
    await expect(page.locator('.pdf-viewer')).toBeVisible();
    
    // 测试翻页
    await page.click('.pdf-next-page');
    await expect(page.locator('.page-number')).toContainText('2');
    
    // 关闭预览
    await page.click('.preview-close');
  });

  test('附件下载功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 上传文件
    await page.setInputFiles('input[type="file"]', {
      name: 'download-test.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('download test content')
    });
    
    await page.fill('input[name="title"]', '下载测试');
    await page.click('button:has-text("提交申请")');
    
    // 找到申请并下载附件
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', '下载测试');
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("查看")');
    
    // 下载附件
    const downloadPromise = page.waitForEvent('download');
    await page.click('.attachment-item button:has-text("下载")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('download-test.pdf');
  });

  test('附件删除功能', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 上传文件
    await page.setInputFiles('input[type="file"]', {
      name: 'delete-me.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('delete test content')
    });
    
    // 验证文件存在
    await expect(page.locator('.upload-list')).toContainText('delete-me.pdf');
    
    // 删除附件
    await page.click('.upload-list .delete-btn');
    await page.click('.el-button--primary:has-text("确认")');
    
    // 验证文件已删除
    await expect(page.locator('.upload-list')).not.toContainText('delete-me.pdf');
  });

  test('附件管理中心', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/attachment');
    
    // 验证附件列表
    await expect(page.locator('.attachment-list')).toBeVisible();
    await expect(page.locator('.el-table__row')).toHaveCount.greaterThan(0);
    
    // 按类型筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("文件类型"))');
    await page.click('.el-select-dropdown__item:has-text("PDF")');
    await page.click('button:has-text("筛选")');
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("筛选")');
    
    // 查看附件详情
    await page.click('.el-table__row').first().locator('button:has-text("详情")');
    await expect(page.locator('.attachment-detail')).toBeVisible();
    await expect(page.locator('.file-name')).toBeVisible();
    await expect(page.locator('.file-size')).toBeVisible();
    await expect(page.locator('.upload-time')).toBeVisible();
    await expect(page.locator('.uploader')).toBeVisible();
    await expect(page.locator('.related-business')).toBeVisible();
  });

  test('批量下载附件', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 上传多个文件
    await page.setInputFiles('input[type="file"]', [
      {
        name: 'file1.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('file 1 content')
      },
      {
        name: 'file2.pdf',
        mimeType: 'application/pdf',
        buffer: Buffer.from('file 2 content')
      }
    ]);
    
    await page.fill('input[name="title"]', '批量下载测试');
    await page.click('button:has-text("提交申请")');
    
    // 查看申请详情
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', '批量下载测试');
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("查看")');
    
    // 全选附件
    await page.click('input[type="checkbox"].select-all');
    
    // 批量下载
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("批量下载")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.zip$/);
  });

  test('附件存储统计', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/attachment/statistics');
    
    // 验证统计卡片
    await expect(page.locator('.stat-card:has-text("总文件数")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("总存储空间")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("本月上传")')).toBeVisible();
    
    // 验证文件类型分布
    await expect(page.locator('.file-type-chart')).toBeVisible();
    
    // 验证存储趋势
    await expect(page.locator('.storage-trend-chart')).toBeVisible();
    
    // 查看大文件列表
    await page.click('.el-tabs__item:has-text("大文件")');
    await expect(page.locator('.large-file-list')).toBeVisible();
    
    // 查看重复文件
    await page.click('.el-tabs__item:has-text("重复文件")');
    await expect(page.locator('.duplicate-file-list')).toBeVisible();
  });

  test('附件清理与归档', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/attachment/cleanup');
    
    // 配置清理规则
    await page.check('input[name="enableAutoCleanup"]');
    await page.fill('input[name="retentionDays"]', '365');
    await page.click('.el-select:has(.el-input__placeholder:has-text("清理范围"))');
    await page.click('.el-select-dropdown__item:has-text("已删除业务的附件")');
    
    // 预览可清理文件
    await page.click('button:has-text("预览清理")');
    await expect(page.locator('.cleanup-preview')).toBeVisible();
    await expect(page.locator('.cleanup-count')).toBeVisible();
    await expect(page.locator('.cleanup-size')).toBeVisible();
    
    // 执行清理
    await page.click('button:has-text("执行清理")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '清理完成');
    
    // 查看清理记录
    await page.click('.el-tabs__item:has-text("清理记录")');
    await expect(page.locator('.cleanup-history')).toBeVisible();
  });
});
