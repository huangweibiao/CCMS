import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 系统配置与数据字典E2E测试
 * 覆盖：系统参数配置、数据字典管理、费用类型配置
 */
test.describe('系统配置与数据字典', () => {
  
  test('系统基础配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/config');
    
    // 修改系统名称
    await page.fill('input[name="systemName"]', 'CCMS费控管理系统-测试');
    
    // 修改公司名称
    await page.fill('input[name="companyName"]', '测试科技有限公司');
    
    // 上传Logo
    await page.setInputFiles('input[name="logo"]', {
      name: 'logo.png',
      mimeType: 'image/png',
      buffer: Buffer.from('test logo content')
    });
    
    // 配置登录页背景
    await page.setInputFiles('input[name="loginBackground"]', {
      name: 'bg.jpg',
      mimeType: 'image/jpeg',
      buffer: Buffer.from('test background content')
    });
    
    // 保存配置
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
    
    // 验证配置生效
    await page.goto('/login');
    await expect(page.locator('.system-name')).toContainText('CCMS费控管理系统-测试');
  });

  test('邮件服务器配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/config');
    
    // 切换到邮件配置标签
    await page.click('.el-tabs__item:has-text("邮件配置")');
    
    // 配置SMTP服务器
    await page.fill('input[name="smtpHost"]', 'smtp.example.com');
    await page.fill('input[name="smtpPort"]', '587');
    await page.fill('input[name="smtpUsername"]', 'noreply@example.com');
    await page.fill('input[name="smtpPassword"]', 'password123');
    
    // 启用SSL
    await page.check('input[name="smtpSsl"]');
    
    // 测试连接
    await page.click('button:has-text("测试连接")');
    await TestHelpers.verifySuccessMessage(page, '连接成功');
    
    // 保存配置
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
  });

  test('短信服务配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/config');
    
    // 切换到短信配置标签
    await page.click('.el-tabs__item:has-text("短信配置")');
    
    // 选择短信服务商
    await page.click('.el-select:has(.el-input__placeholder:has-text("服务商"))');
    await page.click('.el-select-dropdown__item:has-text("阿里云")');
    
    // 配置AccessKey
    await page.fill('input[name="accessKeyId"]', 'LTAIxxxxxxxxxxxx');
    await page.fill('input[name="accessKeySecret"]', 'xxxxxxxxxxxxxxxx');
    await page.fill('input[name="signName"]', 'CCMS系统');
    
    // 测试发送
    await page.fill('input[name="testPhone"]', '13800138000');
    await page.click('button:has-text("发送测试")');
    await TestHelpers.verifySuccessMessage(page, '发送成功');
    
    // 保存配置
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
  });

  test('数据字典管理', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/dict');
    
    // 新增字典类型
    await page.click('button:has-text("新增类型")');
    await page.fill('input[name="dictType"]', 'expense_category');
    await page.fill('input[name="dictName"]', '费用类别');
    await page.fill('textarea[name="remark"]', '费用申请类别字典');
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '保存成功');
    
    // 添加字典数据
    await page.click('.el-table__row:has-text("费用类别") button:has-text("管理数据")');
    
    const dictItems = [
      { label: '差旅费', value: 'travel', sort: 1 },
      { label: '办公费', value: 'office', sort: 2 },
      { label: '招待费', value: 'entertainment', sort: 3 }
    ];
    
    for (const item of dictItems) {
      await page.click('button:has-text("新增")');
      await page.fill('input[name="dictLabel"]', item.label);
      await page.fill('input[name="dictValue"]', item.value);
      await page.fill('input[name="sort"]', item.sort.toString());
      await page.click('button:has-text("确定")');
      await TestHelpers.verifySuccessMessage(page, '添加成功');
    }
    
    // 验证字典数据
    await expect(page.locator('.dict-data-list')).toContainText('差旅费');
    await expect(page.locator('.dict-data-list')).toContainText('办公费');
    
    // 修改字典项
    await page.click('.dict-data-list tr:has-text("差旅费") button:has-text("编辑")');
    await page.fill('input[name="dictLabel"]', '差旅交通费');
    await page.click('button:has-text("确定")');
    await TestHelpers.verifySuccessMessage(page, '修改成功');
    
    // 验证修改生效
    await expect(page.locator('.dict-data-list')).toContainText('差旅交通费');
  });

  test('费用类型配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/expense-type');
    
    // 新增费用类型
    await page.click('button:has-text("新增类型")');
    await page.fill('input[name="typeName"]', '培训费');
    await page.fill('input[name="typeCode"]', 'TRAINING');
    
    // 选择所属分类
    await page.click('.el-select:has(.el-input__placeholder:has-text("所属分类"))');
    await page.click('.el-select-dropdown__item:has-text("管理费用")');
    
    // 设置预算控制
    await page.check('input[name="budgetControl"]');
    await page.fill('input[name="defaultBudget"]', '10000');
    
    // 设置审批流程
    await page.click('.el-select:has(.el-input__placeholder:has-text("审批流程"))');
    await page.click('.el-select-dropdown__item:has-text("费用申请流程")');
    
    // 上传图标
    await page.setInputFiles('input[name="icon"]', {
      name: 'training.png',
      mimeType: 'image/png',
      buffer: Buffer.from('icon content')
    });
    
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '保存成功');
    
    // 验证费用类型出现在列表中
    await expect(page.locator('.expense-type-list')).toContainText('培训费');
    
    // 验证在费用申请页面可用
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("费用类型"))');
    await expect(page.locator('.el-select-dropdown__item')).toContainText('培训费');
  });

  test('字典数据缓存刷新', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/dict');
    
    // 修改字典数据
    await page.click('.el-table__row:has-text("费用类别") button:has-text("管理数据")');
    await page.click('button:has-text("新增")');
    await page.fill('input[name="dictLabel"]', '测试类别');
    await page.fill('input[name="dictValue"]', 'test');
    await page.click('button:has-text("确定")');
    await TestHelpers.verifySuccessMessage(page, '添加成功');
    
    // 刷新缓存
    await page.click('button:has-text("刷新缓存")');
    await TestHelpers.verifySuccessMessage(page, '缓存刷新成功');
    
    // 验证新字典项在系统中生效
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("费用类别"))');
    await expect(page.locator('.el-select-dropdown__item')).toContainText('测试类别');
  });

  test('系统参数配置', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/config');
    
    // 切换到参数配置标签
    await page.click('.el-tabs__item:has-text("参数配置")');
    
    // 配置分页大小
    await page.fill('input[name="pageSize"]', '20');
    
    // 配置会话超时时间
    await page.fill('input[name="sessionTimeout"]', '30');
    
    // 配置密码策略
    await page.fill('input[name="passwordMinLength"]', '8');
    await page.check('input[name="passwordRequireNumber"]');
    await page.check('input[name="passwordRequireSpecial"]');
    await page.fill('input[name="passwordExpireDays"]', '90');
    
    // 配置登录失败锁定
    await page.fill('input[name="loginFailLimit"]', '5');
    await page.fill('input[name="lockDuration"]', '30');
    
    // 保存配置
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '配置保存成功');
    
    // 验证密码策略生效
    await page.goto('/user/profile');
    await page.click('button:has-text("修改密码")');
    await page.fill('input[name="newPassword"]', '123');
    await page.click('button:has-text("保存")');
    await expect(page.locator('.el-form-item__error')).toContainText('密码长度不能少于8位');
  });

  test('数据字典导入导出', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/dict');
    
    // 导出字典
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
    
    // 导入字典
    await page.click('button:has-text("导入")');
    await page.setInputFiles('input[type="file"]', {
      name: 'dict-import.xlsx',
      mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      buffer: Buffer.from('mock dict data')
    });
    await page.click('button:has-text("确认导入")');
    await TestHelpers.verifySuccessMessage(page, '导入成功');
  });
});
