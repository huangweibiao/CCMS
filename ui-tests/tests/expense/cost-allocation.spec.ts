import { test, expect } from '@playwright/test';
import { TestHelpers } from '../../utils/test-helpers';
import { TestDataFactory } from '../../utils/test-data-factory';

/**
 * 费用分摊E2E测试
 * 覆盖：部门分摊、项目分摊、按比例分摊、自定义分摊规则
 */
test.describe('费用分摊管理', () => {
  
  test('费用按部门分摊', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 填写基本信息
    await page.fill('input[name="title"]', '部门分摊测试');
    await page.fill('input[name="amount"]', '10000');
    
    // 开启费用分摊
    await page.check('input[name="enableAllocation"]');
    
    // 添加分摊部门
    await page.click('button:has-text("添加分摊部门")');
    
    // 选择部门1
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').first();
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    await page.fill('input[name="allocationRatio"]', '60');
    await page.fill('input[name="allocationAmount"]', '6000');
    
    // 添加部门2
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    await page.fill('input[name="allocationRatio"]', '40');
    await page.fill('input[name="allocationAmount"]', '4000');
    
    // 验证分摊比例合计为100%
    await expect(page.locator('.total-ratio')).toContainText('100%');
    await expect(page.locator('.total-amount')).toContainText('10000');
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 验证分摊记录
    await page.goto('/expense/application');
    await page.fill('input[placeholder="搜索"]', '部门分摊测试');
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("查看")');
    
    await expect(page.locator('.allocation-detail')).toBeVisible();
    await expect(page.locator('.allocation-item:has-text("技术部")')).toContainText('6000');
    await expect(page.locator('.allocation-item:has-text("市场部")')).toContainText('4000');
  });

  test('费用按项目分摊', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    // 填写基本信息
    await page.fill('input[name="title"]', '项目分摊测试');
    await page.fill('input[name="amount"]', '50000');
    
    // 开启费用分摊
    await page.check('input[name="enableAllocation"]');
    await page.click('.el-radio:has-text("按项目分摊")');
    
    // 添加分摊项目
    await page.click('button:has-text("添加分摊项目")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择项目"))').first();
    await page.click('.el-select-dropdown__item:has-text("项目A")');
    await page.fill('input[name="allocationRatio"]', '50');
    
    await page.click('button:has-text("添加分摊项目")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择项目"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("项目B")');
    await page.fill('input[name="allocationRatio"]', '30');
    
    await page.click('button:has-text("添加分摊项目")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择项目"))').nth(2);
    await page.click('.el-select-dropdown__item:has-text("项目C")');
    await page.fill('input[name="allocationRatio"]', '20');
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 审批通过
    const applicationNo = await page.locator('.application-no').textContent();
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 验证项目成本更新
    await TestHelpers.adminLogin(page);
    await page.goto('/project/cost');
    await expect(page.locator('.project-cost:has-text("项目A")')).toContainText('25000');
    await expect(page.locator('.project-cost:has-text("项目B")')).toContainText('15000');
    await expect(page.locator('.project-cost:has-text("项目C")')).toContainText('10000');
  });

  test('自定义分摊规则', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/system/allocation/rule');
    
    // 新建分摊规则
    await page.click('button:has-text("新建规则")');
    await page.fill('input[name="ruleName"]', '按人数分摊规则');
    await page.fill('textarea[name="description"]', '根据各部门人数比例自动分摊费用');
    
    // 选择分摊类型
    await page.click('.el-select:has(.el-input__placeholder:has-text("分摊类型"))');
    await page.click('.el-select-dropdown__item:has-text("按人数")');
    
    // 配置参与分摊的部门
    await page.click('.el-transfer-panel .el-checkbox:has-text("技术部")');
    await page.click('.el-transfer-panel .el-checkbox:has-text("市场部")');
    await page.click('.el-transfer-panel .el-checkbox:has-text("财务部")');
    await page.click('button:has(.el-icon-arrow-right)');
    
    // 设置各部门人数权重
    await page.fill('input[name="techDeptHeadcount"]', '20');
    await page.fill('input[name="marketDeptHeadcount"]', '15');
    await page.fill('input[name="financeDeptHeadcount"]', '10');
    
    // 保存规则
    await page.click('button:has-text("保存规则")');
    await TestHelpers.verifySuccessMessage(page, '规则保存成功');
    
    // 验证规则列表
    await expect(page.locator('.rule-list')).toContainText('按人数分摊规则');
  });

  test('应用分摊规则', async ({ page }) => {
    // 先创建分摊规则
    await TestHelpers.adminLogin(page);
    await page.goto('/system/allocation/rule');
    await page.click('button:has-text("新建规则")');
    await page.fill('input[name="ruleName"]', '测试分摊规则');
    await page.click('.el-select:has(.el-input__placeholder:has-text("分摊类型"))');
    await page.click('.el-select-dropdown__item:has-text("平均分摊")');
    await page.click('button:has-text("保存规则")');
    
    // 员工使用分摊规则创建申请
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    await page.fill('input[name="title"]', '使用分摊规则测试');
    await page.fill('input[name="amount"]', '9000');
    
    // 开启分摊并选择规则
    await page.check('input[name="enableAllocation"]');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择分摊规则"))');
    await page.click('.el-select-dropdown__item:has-text("测试分摊规则")');
    
    // 验证自动计算分摊金额
    await expect(page.locator('.allocation-item')).toHaveCount(3); // 3个部门平均分摊
    await expect(page.locator('.allocation-item').first()).toContainText('3000');
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
  });

  test('分摊比例校验', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    await page.fill('input[name="title"]', '分摊校验测试');
    await page.fill('input[name="amount"]', '10000');
    await page.check('input[name="enableAllocation"]');
    
    // 添加分摊部门，比例不足100%
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').first();
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    await page.fill('input[name="allocationRatio"]', '50');
    
    // 尝试提交
    await page.click('button:has-text("提交申请")');
    
    // 验证错误提示
    await expect(page.locator('.el-form-item__error')).toContainText('分摊比例合计必须等于100%');
    await expect(page.locator('.total-ratio')).toHaveClass(/error/);
    
    // 修正比例
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    await page.fill('input[name="allocationRatio"]', '50');
    
    // 验证比例合计正确
    await expect(page.locator('.total-ratio')).toContainText('100%');
    await expect(page.locator('.total-ratio')).not.toHaveClass(/error/);
    
    // 成功提交
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
  });

  test('分摊明细查询', async ({ page }) => {
    await TestHelpers.adminLogin(page);
    await page.goto('/report/allocation');
    
    // 选择查询维度
    await page.click('.el-select:has(.el-input__placeholder:has-text("查询维度"))');
    await page.click('.el-select-dropdown__item:has-text("按部门")');
    
    // 选择时间范围
    await page.fill('input[name="startDate"]', '2024-01-01');
    await page.fill('input[name="endDate"]', '2024-12-31');
    
    // 查询分摊明细
    await page.click('button:has-text("查询")');
    
    // 验证分摊明细表
    await expect(page.locator('.allocation-detail-table')).toBeVisible();
    await expect(page.locator('.allocation-detail-table th:has-text("原费用")')).toBeVisible();
    await expect(page.locator('.allocation-detail-table th:has-text("分摊部门")')).toBeVisible();
    await expect(page.locator('.allocation-detail-table th:has-text("分摊比例")')).toBeVisible();
    await expect(page.locator('.allocation-detail-table th:has-text("分摊金额")')).toBeVisible();
    
    // 导出分摊明细
    const downloadPromise = page.waitForEvent('download');
    await page.click('button:has-text("导出")');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toMatch(/\.xlsx$/);
  });

  test('分摊调整与冲销', async ({ page }) => {
    // 先创建一个已分摊的费用
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    await page.fill('input[name="title"]', '分摊调整测试');
    await page.fill('input[name="amount"]', '10000');
    await page.check('input[name="enableAllocation"]');
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').first();
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    await page.fill('input[name="allocationRatio"]', '100');
    await page.click('button:has-text("提交申请")');
    const applicationNo = await page.locator('.application-no').textContent();
    
    // 审批通过
    await TestHelpers.approverLogin(page);
    await page.goto('/approval/management');
    await page.fill('input[placeholder="搜索"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("审批")');
    await page.click('button:has-text("通过")');
    
    // 财务进行分摊调整
    await TestHelpers.adminLogin(page);
    await page.goto('/finance/allocation/adjust');
    await page.fill('input[placeholder="搜索"]', applicationNo!);
    await page.click('button:has-text("搜索")');
    await page.click('button:has-text("调整")');
    
    // 修改分摊比例
    await page.fill('input[name="allocationRatio"]', '70');
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    await page.fill('input[name="allocationRatio"]', '30');
    
    // 确认调整
    await page.click('button:has-text("确认调整")');
    await page.click('.el-button--primary:has-text("确认")');
    await TestHelpers.verifySuccessMessage(page, '调整成功');
    
    // 验证调整记录
    await expect(page.locator('.adjustment-record')).toContainText('从 技术部 100%');
    await expect(page.locator('.adjustment-record')).toContainText('调整为 技术部 70%, 市场部 30%');
  });

  test('分摊与预算关联', async ({ page }) => {
    await TestHelpers.userLogin(page);
    await page.goto('/expense/application');
    await page.click('button:has-text("新建申请")');
    
    await page.fill('input[name="title"]', '分摊预算测试');
    await page.fill('input[name="amount"]', '10000');
    await page.check('input[name="enableAllocation"]');
    
    // 添加分摊部门
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').first();
    await page.click('.el-select-dropdown__item:has-text("技术部")');
    await page.fill('input[name="allocationRatio"]', '60');
    
    await page.click('button:has-text("添加分摊部门")');
    await page.click('.el-select:has(.el-input__placeholder:has-text("选择部门"))').nth(1);
    await page.click('.el-select-dropdown__item:has-text("市场部")');
    await page.fill('input[name="allocationRatio"]', '40');
    
    // 验证预算占用提示
    await expect(page.locator('.budget-info:has-text("技术部")')).toContainText('预算占用: 6000');
    await expect(page.locator('.budget-info:has-text("市场部")')).toContainText('预算占用: 4000');
    
    // 提交申请
    await page.click('button:has-text("提交申请")');
    await TestHelpers.verifySuccessMessage(page, '申请提交成功');
    
    // 验证各部门预算执行
    await TestHelpers.adminLogin(page);
    await page.goto('/budget/execution');
    await expect(page.locator('.dept-budget:has-text("技术部")')).toContainText('已执行: 6000');
    await expect(page.locator('.dept-budget:has-text("市场部")')).toContainText('已执行: 4000');
  });
});
