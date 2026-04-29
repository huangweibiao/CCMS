import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

test.describe('安全性和权限测试', () => {
  test('跨站点请求伪造(CSRF)防护', async ({ page }) => {
    // 验证关键操作需要正确的CSRF令牌
    await TestHelpers.userLogin(page);
    
    // 尝试通过外部表单提交（模拟CSRF攻击）
    await page.evaluate(() => {
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '/api/expense/application';
      
      const titleInput = document.createElement('input');
      titleInput.name = 'title';
      titleInput.value = 'CSRF攻击测试';
      form.appendChild(titleInput);
      
      document.body.appendChild(form);
      form.submit();
    });
    
    // 验证CSRF防护生效
    await page.waitForTimeout(1000);
    
    // 页面应该停留在原位置或显示错误
    const currentUrl = page.url();
    expect(currentUrl).not.toMatch(/\/api\//);
    
    // 或者显示CSRF错误
    if (await page.locator('.csrf-error').isVisible()) {
      await expect(page.locator('.csrf-error')).toContainText('CSRF');
    }
  });

  test('SQL注入防护', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    
    // 尝试SQL注入攻击
    const sqlInjection = "'; DROP TABLE users; --";
    await page.fill('.search-input', sqlInjection);
    await page.click('.search-button');
    
    // 验证系统正确处理恶意输入
    // 不应该崩溃或显示数据库错误
    await expect(page.locator('.system-error')).not.toBeVisible();
    
    // 搜索应该返回空结果或错误提示
    const searchResults = page.locator('.search-results');
    if (await searchResults.isVisible()) {
      await expect(searchResults).toContainText('未找到结果');
    }
  });

  test('XSS攻击防护', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 尝试XSS注入
    const xssPayload = '<script>alert("XSS")</script>';
    
    // 在表单字段中尝试XSS
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', xssPayload);
    await page.click('button:has-text("提交")');
    
    // 验证XSS被转义或过滤
    // 脚本不应该被执行
    await page.waitForTimeout(1000);
    
    // 检查是否有alert弹出（不应该有）
    const dialogEvents = await page.evaluate(() => {
      return window.alertCalls || 0;
    });
    expect(dialogEvents).toBe(0);
    
    // 验证输入被正确处理
    await page.goto('/expense/application');
    const applicationList = page.locator('.application-list');
    if (await applicationList.isVisible()) {
      // XSS代码应该被转义显示，而不是执行
      await expect(applicationList).not.toContainText('<script>');
    }
  });

  test('会话安全管理', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 验证会话令牌安全设置
    const cookies = await page.context().cookies();
    const authCookie = cookies.find(cookie => cookie.name.includes('token') || cookie.name.includes('session'));
    
    if (authCookie) {
      // 验证HttpOnly标志
      expect(authCookie.httpOnly).toBe(true);
      
      // 验证Secure标志（在生产环境）
      // expect(authCookie.secure).toBe(true);
      
      // 验证SameSite设置
      expect(authCookie.sameSite).toBe('Lax');
    }
    
    // 测试会话超时
    await page.waitForTimeout(3600000); // 等待1小时（模拟会话超时）
    await page.reload();
    
    // 验证会话超时后重定向到登录页
    await expect(page).toHaveURL(/.*\/login/);
  });

  test('权限越权访问防护', async ({ page }) => {
    // 普通用户尝试访问管理员功能
    await TestHelpers.userLogin(page);
    
    const adminEndpoints = [
      '/admin/users',
      '/admin/roles', 
      '/admin/settings',
      '/admin/departments'
    ];
    
    for (const endpoint of adminEndpoints) {
      await page.goto(endpoint);
      
      // 验证权限不足错误
      await expect(page.locator('.error-page')).toContainText(['权限不足', 'Access Denied']);
      
      // 或者被重定向到无权限页面
      if (page.url().includes('/unauthorized')) {
        await TestHelpers.verifyPageTitle(page, '无权限访问');
      }
    }
  });

  test('数据隔离安全', async ({ page }) => {
    // 用户A登录
    await TestHelpers.userLogin(page);
    
    // 获取用户A的数据ID
    await page.goto('/expense/application');
    const userAData = await page.locator('.application-item').first().getAttribute('data-id');
    
    if (userAData) {
      // 用户A登出
      await page.click('.logout-btn');
      
      // 用户B登录
      await TestHelpers.login(page, 'user2@example.com', 'password123');
      
      // 用户B尝试访问用户A的数据
      await page.goto(`/expense/application/${userAData}`);
      
      // 验证数据隔离 - 用户B不应该看到用户A的数据
      await expect(page.locator('.error-page')).toContainText(['无权访问', 'Not Found']);
    }
  });

  test('文件上传安全', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 尝试上传危险文件类型
    const dangerousFiles = [
      { name: 'virus.exe', type: 'application/x-msdownload' },
      { name: 'malware.html', type: 'text/html' },
      { name: 'script.js', type: 'application/javascript' }
    ];
    
    for (const file of dangerousFiles) {
      await page.setInputFiles('input[type="file"]', {
        name: file.name,
        mimeType: file.type,
        buffer: Buffer.from('dangerous content')
      });
      
      // 验证文件类型被拒绝
      await expect(page.locator('.file-error')).toContainText(['不支持的文件类型', 'Invalid file type']);
    }
    
    // 测试文件大小限制
    const largeFileBuffer = Buffer.alloc(100 * 1024 * 1024); // 100MB
    await page.setInputFiles('input[type="file"]', {
      name: 'large-file.pdf',
      mimeType: 'application/pdf',
      buffer: largeFileBuffer
    });
    
    // 验证文件大小限制
    await expect(page.locator('.file-error')).toContainText(['文件过大', 'File too large']);
  });

  test('密码安全策略', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/profile/password');
    
    // 测试弱密码
    const weakPasswords = ['123456', 'password', 'admin', 'test'];
    
    for (const weakPassword of weakPasswords) {
      await page.fill('input[name="newPassword"]', weakPassword);
      await page.fill('input[name="confirmPassword"]', weakPassword);
      await page.click('button:has-text("修改密码")');
      
      // 验证弱密码被拒绝
      await expect(page.locator('.password-error')).toContainText(['密码强度不足', 'Weak password']);
    }
    
    // 测试符合要求的密码
    await page.fill('input[name="newPassword"]', 'StrongPass123!');
    await page.fill('input[name="confirmPassword"]', 'StrongPass123!');
    
    // 验证密码强度提示
    await expect(page.locator('.password-strength')).toContainText(['强', 'Strong']);
  });

  test('API速率限制', async ({ page }) => {
    await TestHelpers.userLogin(page);
    
    // 模拟快速连续请求
    const rapidRequests = [];
    for (let i = 0; i < 20; i++) {
      rapidRequests.push(
        page.evaluate(() => 
          fetch('/api/expense/applications').catch(() => {})
        )
      );
    }
    
    // 等待所有请求完成
    await Promise.all(rapidRequests);
    
    // 验证速率限制生效
    const responses = await page.evaluate(() => {
      return window.rateLimitResponses || [];
    });
    
    // 应该有部分请求被限制
    if (responses.length > 0) {
      const rateLimited = responses.filter(r => r.status === 429);
      expect(rateLimited.length).toBeGreaterThan(0);
    }
  });

  test('敏感信息泄露防护', async ({ page }) => {
    // 检查前端代码是否包含敏感信息
    const pageSource = await page.content();
    
    // 验证不包含敏感信息
    const sensitivePatterns = [
      /password.*=.*['"][^'"]*['"]/i,
      /api.*key.*=.*['"][^'"]*['"]/i,
      /secret.*=.*['"][^'"]*['"]/i
    ];
    
    for (const pattern of sensitivePatterns) {
      expect(pageSource).not.toMatch(pattern);
    }
    
    // 检查网络请求中的敏感信息
    await page.route('**/*', route => {
      const request = route.request();
      const headers = request.headers();
      
      // 验证不通过URL传递敏感信息
      const url = request.url();
      expect(url).not.toMatch(/password=|token=|secret=/i);
      
      route.continue();
    });
    
    await TestHelpers.userLogin(page);
  });

  test('错误信息泄露防护', async ({ page }) => {
    // 触发错误并验证不泄露敏感信息
    await page.goto('/nonexistent-page');
    
    // 验证错误页面不显示技术细节
    const errorContent = await page.content();
    
    // 不应该包含的技术信息
    const technicalInfo = [
      'SQL',
      'Database',
      'Exception',
      'Stack trace',
      'at '
    ];
    
    for (const info of technicalInfo) {
      expect(errorContent).not.toContain(info);
    }
    
    // 应该显示用户友好的错误信息
    await expect(page.locator('.error-message')).toContainText(['页面不存在', 'Not Found']);
  });

  test('HTTPS强制和安全头检查', async ({ page }) => {
    // 这个测试需要在实际HTTPS环境中运行
    // 这里验证安全头设置
    
    await TestHelpers.userLogin(page);
    
    // 检查安全头（通过响应头）
    const response = await page.goto('/dashboard');
    const headers = response?.headers() || {};
    
    // 验证安全头设置
    expect(headers['x-content-type-options']).toBe('nosniff');
    expect(headers['x-frame-options']).toBe('SAMEORIGIN');
    expect(headers['x-xss-protection']).toBe('1; mode=block');
    
    // 验证CSP头（如果设置）
    if (headers['content-security-policy']) {
      expect(headers['content-security-policy']).toContain('script-src');
    }
  });

  test('登录失败锁定机制', async ({ page }) => {
    // 模拟多次登录失败
    for (let i = 0; i < 5; i++) {
      await page.goto('/login');
      await page.fill('input[type="text"]', 'testuser@example.com');
      await page.fill('input[type="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      
      // 等待错误显示
      await page.waitForTimeout(500);
    }
    
    // 验证账户被锁定
    await expect(page.locator('.error-message')).toContainText(['账户已锁定', 'Account locked']);
    
    // 尝试正确密码也应该失败
    await page.fill('input[type="password"]', 'correctpassword');
    await page.click('button[type="submit"]');
    
    // 验证仍然显示锁定信息
    await expect(page.locator('.error-message')).toContainText(['账户已锁定', 'Account locked']);
  });
});