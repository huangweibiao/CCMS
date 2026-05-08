import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';

/**
 * 个人中心与账户设置E2E测试
 * 覆盖：个人信息管理、密码修改、偏好设置、安全设置
 */
test.describe('个人中心与账户设置', () => {
  
  test('查看和编辑个人资料', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    
    // 验证个人资料页面加载
    await expect(page.locator('h1')).toContainText('个人中心');
    
    // 验证基本信息显示
    await expect(page.locator('.profile-avatar')).toBeVisible();
    await expect(page.locator('.user-name')).toBeVisible();
    await expect(page.locator('.user-role')).toBeVisible();
    await expect(page.locator('.user-department')).toBeVisible();
    
    // 编辑个人资料
    await page.click('button:has-text("编辑资料")');
    
    // 修改昵称
    await page.fill('input[name="nickname"]', '测试昵称');
    
    // 修改手机号
    await page.fill('input[name="phone"]', '13800138001');
    
    // 修改邮箱
    await page.fill('input[name="email"]', 'test@example.com');
    
    // 上传头像
    await page.setInputFiles('input[name="avatar"]', {
      name: 'avatar.png',
      mimeType: 'image/png',
      buffer: Buffer.from('avatar image content')
    });
    
    // 保存修改
    await page.click('button:has-text("保存")');
    await TestHelpers.verifySuccessMessage(page, '保存成功');
    
    // 验证修改生效
    await page.reload();
    await expect(page.locator('.user-name')).toContainText('测试昵称');
  });

  test('修改密码', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    
    // 进入安全设置标签
    await page.click('.el-tabs__item:has-text("安全设置")');
    
    // 点击修改密码
    await page.click('button:has-text("修改密码")');
    
    // 填写原密码
    await page.fill('input[name="oldPassword"]', 'user123');
    
    // 填写新密码（符合复杂度要求）
    await page.fill('input[name="newPassword"]', 'NewPass123!');
    
    // 确认新密码
    await page.fill('input[name="confirmPassword"]', 'NewPass123!');
    
    // 提交修改
    await page.click('button:has-text("确认修改")');
    await TestHelpers.verifySuccessMessage(page, '密码修改成功');
    
    // 验证需要重新登录
    await expect(page).toHaveURL(/.*\/login/);
    
    // 使用新密码登录
    await page.fill('input[type="text"]', 'user@example.com');
    await page.fill('input[type="password"]', 'NewPass123!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('密码复杂度验证', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("安全设置")');
    await page.click('button:has-text("修改密码")');
    
    // 测试密码太短
    await page.fill('input[name="oldPassword"]', 'user123');
    await page.fill('input[name="newPassword"]', '123');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-form-item__error')).toContainText('密码长度不能少于8位');
    
    // 测试密码没有数字
    await page.fill('input[name="newPassword"]', 'Password');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-form-item__error')).toContainText('密码必须包含数字');
    
    // 测试密码没有字母
    await page.fill('input[name="newPassword"]', '12345678');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-form-item__error')).toContainText('密码必须包含字母');
    
    // 测试密码没有特殊字符
    await page.fill('input[name="newPassword"]', 'Password123');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-form-item__error')).toContainText('密码必须包含特殊字符');
    
    // 测试两次密码不一致
    await page.fill('input[name="newPassword"]', 'NewPass123!');
    await page.fill('input[name="confirmPassword"]', 'Different123!');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-form-item__error')).toContainText('两次输入的密码不一致');
    
    // 测试原密码错误
    await page.fill('input[name="oldPassword"]', 'wrongpassword');
    await page.fill('input[name="newPassword"]', 'NewPass123!');
    await page.fill('input[name="confirmPassword"]', 'NewPass123!');
    await page.click('button:has-text("确认修改")');
    await expect(page.locator('.el-message--error')).toContainText('原密码错误');
  });

  test('界面偏好设置', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    
    // 进入偏好设置标签
    await page.click('.el-tabs__item:has-text("偏好设置")');
    
    // 设置主题
    await page.click('.el-radio:has-text("深色主题")');
    await expect(page.locator('body')).toHaveClass(/dark-theme/);
    
    // 设置侧边栏模式
    await page.click('.el-radio:has-text("折叠")');
    await expect(page.locator('.sidebar')).toHaveClass(/collapsed/);
    
    // 设置每页显示条数
    await page.click('.el-select:has(.el-input__placeholder:has-text("每页显示"))');
    await page.click('.el-select-dropdown__item:has-text("50")');
    
    // 设置默认首页
    await page.click('.el-select:has(.el-input__placeholder:has-text("默认首页"))');
    await page.click('.el-select-dropdown__item:has-text("费用申请")');
    
    // 保存设置
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 验证设置持久化
    await page.reload();
    await expect(page.locator('body')).toHaveClass(/dark-theme/);
  });

  test('通知偏好设置', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("通知设置")');
    
    // 启用邮件通知
    await page.check('input[name="emailNotification"]');
    await page.fill('input[name="notificationEmail"]', 'notify@example.com');
    
    // 启用短信通知
    await page.check('input[name="smsNotification"]');
    await page.fill('input[name="notificationPhone"]', '13800138001');
    
    // 选择通知类型
    await page.check('input[name="notifyExpenseApproval"]');
    await page.check('input[name="notifyBudgetWarning"]');
    await page.check('input[name="notifyLoanDue"]');
    await page.uncheck('input[name="notifySystemAnnouncement"]');
    
    // 设置免打扰时间
    await page.check('input[name="enableDnd"]');
    await page.fill('input[name="dndStart"]', '22:00');
    await page.fill('input[name="dndEnd"]', '08:00');
    
    // 保存设置
    await page.click('button:has-text("保存设置")');
    await TestHelpers.verifySuccessMessage(page, '设置保存成功');
    
    // 验证设置生效
    await page.reload();
    await page.click('.el-tabs__item:has-text("通知设置")');
    await expect(page.locator('input[name="emailNotification"]')).toBeChecked();
    await expect(page.locator('input[name="notifySystemAnnouncement"]')).not.toBeChecked();
  });

  test('登录设备管理', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("安全设置")');
    
    // 查看登录设备列表
    await page.click('button:has-text("登录设备")');
    await expect(page.locator('.device-list')).toBeVisible();
    await expect(page.locator('.device-item')).toHaveCount.greaterThan(0);
    
    // 验证当前设备标记
    await expect(page.locator('.device-item').first()).toContainText('当前设备');
    
    // 登出其他设备
    await page.click('.device-item:has-text("Chrome") button:has-text("登出")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '操作成功');
    
    // 验证设备已移除
    await expect(page.locator('.device-item:has-text("Chrome")')).not.toBeVisible();
  });

  test('操作日志查看', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("操作日志")');
    
    // 验证操作日志列表
    await expect(page.locator('.operation-log-list')).toBeVisible();
    await expect(page.locator('.log-item')).toHaveCount.greaterThan(0);
    
    // 按时间筛选
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    await page.click('button:has-text("筛选")');
    
    // 按操作类型筛选
    await page.click('.el-select:has(.el-input__placeholder:has-text("操作类型"))');
    await page.click('.el-select-dropdown__item:has-text("登录")');
    await page.click('button:has-text("筛选")');
    
    // 查看详情
    await page.click('.log-item').first().locator('button:has-text("详情")');
    await expect(page.locator('.log-detail-modal')).toBeVisible();
    await expect(page.locator('.log-time')).toBeVisible();
    await expect(page.locator('.log-ip')).toBeVisible();
    await expect(page.locator('.log-location')).toBeVisible();
    await expect(page.locator('.log-device')).toBeVisible();
  });

  test('账户绑定管理', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("账户绑定")');
    
    // 绑定微信
    await page.click('button:has-text("绑定微信")');
    // 模拟扫码绑定流程
    await expect(page.locator('.qrcode-modal')).toBeVisible();
    await expect(page.locator('.bind-qrcode')).toBeVisible();
    await page.click('.modal-close');
    
    // 绑定手机号
    await page.click('button:has-text("更换手机")');
    await page.fill('input[name="newPhone"]', '13900139001');
    await page.click('button:has-text("获取验证码")');
    // 模拟验证码输入
    await page.fill('input[name="verifyCode"]', '123456');
    await page.click('button:has-text("确认绑定")');
    await TestHelpers.verifySuccessMessage(page, '绑定成功');
    
    // 解绑操作
    await page.click('button:has-text("解绑")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '解绑成功');
  });

  test('账户注销申请', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/user/profile');
    await page.click('.el-tabs__item:has-text("安全设置")');
    
    // 点击注销账户
    await page.click('button:has-text("注销账户")');
    
    // 验证风险提示
    await expect(page.locator('.danger-warning')).toContainText('注销后数据将无法恢复');
    
    // 确认注销条件
    await page.check('input[name="confirmNoOngoingBusiness"]');
    await page.check('input[name="confirmDataLoss"]');
    
    // 输入密码确认
    await page.fill('input[name="confirmPassword"]', 'user123');
    
    // 提交注销申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '注销申请已提交');
    
    // 验证状态变为待审核
    await expect(page.locator('.account-status')).toContainText('注销审核中');
  });
});
