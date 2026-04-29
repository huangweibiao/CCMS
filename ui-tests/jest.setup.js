// Jest全局设置文件
const fs = require('fs');
const path = require('path');

// 创建测试目录结构
const testDirs = [
  'coverage',
  'test-results',
  'screenshots',
  'logs'
];

testDirs.forEach(dir => {
  const dirPath = path.join(__dirname, dir);
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
  }
});

// 全局测试配置
global.testConfig = {
  timeout: 30000,
  baseURL: process.env.TEST_BASE_URL || 'http://localhost:8080',
  users: {
    admin: {
      username: 'admin',
      password: 'admin123'
    },
    manager: {
      username: 'manager', 
      password: 'manager123'
    },
    employee: {
      username: 'employee',
      password: 'employee123'
    }
  }
};

// 全局测试助手
global.testHelpers = {
  // 生成测试数据
  generateTestData(type, count = 1) {
    const data = [];
    for (let i = 0; i < count; i++) {
      switch (type) {
        case 'budget':
          data.push({
            title: `测试预算-${Date.now()}-${i}`,
            amount: Math.floor(Math.random() * 100000) + 10000,
            year: new Date().getFullYear(),
            category: ['办公费用', '差旅费用', '采购费用'][i % 3]
          });
          break;
        case 'expense':
          data.push({
            title: `费用申请-${Date.now()}-${i}`,
            amount: Math.floor(Math.random() * 5000) + 100,
            expenseType: ['差旅费', '采购费', '交通费'][i % 3],
            description: `测试描述-${i}`
          });
          break;
        case 'user':
          data.push({
            username: `testuser-${Date.now()}-${i}`,
            email: `test${i}@example.com`,
            department: ['技术部', '财务部', '人事部'][i % 3]
          });
          break;
      }
    }
    return count === 1 ? data[0] : data;
  },

  // 验证响应
  validateResponse(response, expectedStatus = 200) {
    expect(response.status).toBe(expectedStatus);
    expect(response.data).toBeDefined();
    return response.data;
  },

  // 清理测试数据
  async cleanupTestData(testId) {
    try {
      // 模拟清理操作
      console.log(`清理测试数据: ${testId}`);
      return true;
    } catch (error) {
      console.error('清理失败:', error);
      return false;
    }
  }
};

// 测试覆盖率设置
beforeAll(() => {
  console.log('开始执行测试套件');
});

afterAll(() => {
  console.log('测试套件执行完成');
});

// 每个测试前的设置
beforeEach(() => {
  // 测试前的准备工作
  console.log('准备执行测试');
});

afterEach(() => {
  // 测试后的清理工作
  console.log('测试执行完成');
});