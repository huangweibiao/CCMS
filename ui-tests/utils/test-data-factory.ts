import { faker } from '@faker-js/faker';

export class TestDataFactory {
  // 预算相关测试数据
  static createBudgetData() {
    return {
      title: `测试预算-${faker.string.alphanumeric(8)}`,
      year: faker.date.future().getFullYear(),
      amount: faker.number.float({ min: 10000, max: 1000000, precision: 0.01 }),
      description: faker.lorem.sentence(),
      category: faker.helpers.arrayElement(['办公费用', '差旅费用', '采购费用', '项目费用'])
    };
  }

  static createBudgetAdjustmentData() {
    return {
      adjustAmount: faker.number.float({ min: -50000, max: 50000, precision: 0.01 }),
      reason: faker.lorem.sentence(),
      attachmentName: faker.system.commonFileName({ extension: 'pdf' })
    };
  }

  // 费用申请相关测试数据
  static createExpenseApplyData() {
    return {
      title: `费用申请-${faker.string.alphanumeric(8)}`,
      amount: faker.number.float({ min: 100, max: 50000, precision: 0.01 }),
      description: faker.lorem.paragraph(),
      applyDate: faker.date.recent().toISOString().split('T')[0],
      expenseType: faker.helpers.arrayElement(['差旅费', '采购费', '交通费', '招待费']),
      items: Array.from({ length: faker.number.int({ min: 1, max: 5 }) }, () => ({
        name: faker.commerce.productName(),
        amount: faker.number.float({ min: 10, max: 5000, precision: 0.01 }),
        quantity: faker.number.int({ min: 1, max: 10 }),
        unitPrice: faker.number.float({ min: 5, max: 1000, precision: 0.01 })
      }))
    };
  }

  // 借款申请相关测试数据
  static createLoanApplyData() {
    return {
      title: `借款申请-${faker.string.alphanumeric(8)}`,
      amount: faker.number.float({ min: 1000, max: 50000, precision: 0.01 }),
      purpose: faker.lorem.sentence(),
      expectedRepaymentDate: faker.date.future().toISOString().split('T')[0],
      repayMethod: faker.helpers.arrayElement(['一次性还款', '分期还款'])
    };
  }

  // 人员管理相关测试数据
  static createUserData() {
    return {
      username: `testuser_${faker.string.alphanumeric(8)}`,
      email: faker.internet.email(),
      phone: faker.phone.number('###########'),
      department: faker.helpers.arrayElement(['技术部', '财务部', '人事部', '市场部']),
      position: faker.person.jobTitle(),
      realName: faker.person.fullName(),
      role: faker.helpers.arrayElement(['员工', '部门经理', '财务人员', '管理员'])
    };
  }

  static createDepartmentData() {
    return {
      name: `测试部门-${faker.string.alphanumeric(6)}`,
      leader: faker.person.fullName(),
      description: faker.lorem.sentence(),
      parentDepartment: faker.helpers.arrayElement(['总部', '分公司', null])
    };
  }

  // 报表相关测试数据
  static createReportData() {
    return {
      startDate: faker.date.past().toISOString().split('T')[0],
      endDate: faker.date.recent().toISOString().split('T')[0],
      department: faker.helpers.arrayElement(['全部', '技术部', '财务部']),
      expenseType: faker.helpers.arrayElement(['全部', '差旅费', '采购费']),
      chartType: faker.helpers.arrayElement(['柱状图', '折线图', '饼图'])
    };
  }

  // 系统设置相关测试数据
  static createSystemSettingData() {
    return {
      companyName: `测试公司${faker.string.alphanumeric(4)}`,
      systemTitle: `CCMS测试系统${faker.string.alphanumeric(4)}`,
      logoUrl: faker.image.url(),
      contactEmail: faker.internet.email(),
      contactPhone: faker.phone.number(),
      address: faker.location.streetAddress()
    };
  }

  // 消息通知相关测试数据
  static createMessageData() {
    return {
      title: faker.lorem.words(5),
      content: faker.lorem.paragraph(),
      type: faker.helpers.arrayElement(['通知', '警告', '提醒']),
      priority: faker.helpers.arrayElement(['低', '中', '高']),
      recipientType: faker.helpers.arrayElement(['全部用户', '特定部门', '特定角色'])
    };
  }

  // 审批流程相关测试数据
  static createApprovalProcessData() {
    return {
      name: `审批流程-${faker.string.alphanumeric(8)}`,
      description: faker.lorem.sentence(),
      nodeCount: faker.number.int({ min: 1, max: 5 }),
      approvers: Array.from({ length: faker.number.int({ min: 1, max: 3 }) }, () => ({
        name: faker.person.fullName(),
        role: faker.helpers.arrayElement(['上级主管', '财务审批', '总经理'])
      }))
    };
  }

  // 附件相关测试数据
  static createAttachmentData() {
    return {
      name: faker.system.commonFileName(),
      size: faker.number.int({ min: 1000, max: 10000000 }),
      type: faker.helpers.arrayElement(['image/png', 'application/pdf', 'text/plain']),
      uploadDate: faker.date.recent().toISOString()
    };
  }

  // 测试用户账户数据
  static createTestUsers() {
    return {
      admin: {
        username: process.env.TEST_ADMIN_USERNAME || 'admin',
        password: process.env.TEST_ADMIN_PASSWORD || 'admin123',
        email: 'admin@ccms.com',
        role: '管理员'
      },
      manager: {
        username: process.env.TEST_MANAGER_USERNAME || 'manager',
        password: process.env.TEST_MANAGER_PASSWORD || 'manager123',
        email: 'manager@ccms.com',
        role: '部门经理'
      },
      employee: {
        username: process.env.TEST_EMPLOYEE_USERNAME || 'employee',
        password: process.env.TEST_EMPLOYEE_PASSWORD || 'employee123',
        email: 'employee@ccms.com',
        role: '员工'
      }
    };
  }

  // 批量数据生成方法
  static generateMultiple<T>(generator: () => T, count: number): T[] {
    return Array.from({ length: count }, generator);
  }

  // 验证数据格式方法
  static validateBudgetData(data: any): boolean {
    return data && 
           data.title && 
           data.amount > 0 && 
           data.year && 
           data.category;
  }

  static validateExpenseData(data: any): boolean {
    return data && 
           data.title && 
           data.amount > 0 && 
           data.applyDate && 
           data.expenseType;
  }

  // 清理测试数据
  static generateCleanupScript(testId: string) {
    return `DELETE FROM budget_main WHERE title LIKE '%测试预算-${testId}%';
            DELETE FROM expense_apply WHERE title LIKE '%费用申请-${testId}%';
            DELETE FROM loan_main WHERE title LIKE '%借款申请-${testId}%';
            DELETE FROM sys_user WHERE username LIKE 'testuser_${testId}%';
            DELETE FROM sys_department WHERE name LIKE '%测试部门-${testId}%';`;
  }

  // 测试数据导入导出
  static exportTestData(data: any, type: string) {
    return {
      timestamp: new Date().toISOString(),
      type: type,
      data: data
    };
  }
}

// 导出工厂实例
const testDataFactory = new TestDataFactory();
export default testDataFactory;