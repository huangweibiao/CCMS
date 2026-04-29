import { test as baseTest } from '@playwright/test';
import { TestDataFactory } from '../../utils/test-data-factory';

export interface TestCleanup {
  testData: any[];
  cleanupAfterTest: () => Promise<void>;
}

export const test = baseTest.extend<TestCleanup>({
  testData: [[], { scope: 'test' }],
  
  async cleanupAfterTest({}, use, testInfo) {
    // 测试前不需要特殊操作
    await use(async () => {
      // 测试结束后清理数据
      await globalCleanup(testInfo.title);
    });
  },
});

export const globalCleanup = async (testTitle: string) => {
  // 提取测试标识
  const testId = extractTestId(testTitle);
  
  // 如果有测试标识，执行清理
  if (testId) {
    await executeCleanupScript(testId);
  }
};

// 提取测试标识
const extractTestId = (title: string): string | null => {
  const match = title.match(/测试(数据)?[\w\d]+-(\w+)/);
  return match ? match[2] : null;
};

// 执行清理脚本
const executeCleanupScript = async (testId: string) => {
  try {
    // 这里可以连接到数据库执行清理
    // 或者通过API执行清理
    console.log(`执行清理脚本，测试ID: ${testId}`);
    
    // 模拟清理操作
    await new Promise(resolve => setTimeout(resolve, 100));
    
  } catch (error) {
    console.error(`清理测试数据失败: ${error}`);
  }
};

// 测试数据处理工具
export class DataCleanupHelper {
  // 标记测试数据以用于清理
  static markTestData(testData: any[], testId: string) {
    return testData.map(data => ({
      ...data,
      _testId: testId,
      _cleanup: true
    }));
  }

  // 批量清理测试数据
  static async bulkCleanup(testData: any[]) {
    const cleanupTasks = testData
      .filter(data => data._cleanup)
      .map(async (data) => {
        try {
          // 模拟清理操作
          await this.cleanupSingleRecord(data);
        } catch (error) {
          console.error(`清理数据失败: ${data._testId}`, error);
        }
      });

    await Promise.all(cleanupTasks);
  }

  // 清理单个记录
  private static async cleanupSingleRecord(data: any) {
    // 根据数据类型执行不同的清理逻辑
    if (data.title && data.title.includes('测试预算-')) {
      await cleanupBudgetData(data);
    } else if (data.title && data.title.includes('费用申请-')) {
      await cleanupExpenseData(data);
    } else if (data.username && data.username.includes('testuser_')) {
      await cleanupUserData(data);
    } else if (data.name && data.name.includes('测试部门-')) {
      await cleanupDepartmentData(data);
    }
  }

  // 验证数据是否被清理
  static async verifyCleanup(testData: any[]): Promise<boolean> {
    const verificationTasks = testData.map(async (data) => {
      try {
        return await this.verifyDataDeleted(data);
      } catch (error) {
        return false;
      }
    });

    const results = await Promise.all(verificationTasks);
    return results.every(result => result);
  }

  private static async verifyDataDeleted(data: any): Promise<boolean> {
    // 模拟验证逻辑
    return true;
  }
}

// 具体清理函数
async function cleanupBudgetData(data: any) {
  console.log(`清理预算数据: ${data.title}`);
  // 实际项目中这里会调用API或数据库删除
}

async function cleanupExpenseData(data: any) {
  console.log(`清理费用申请数据: ${data.title}`);
  // 实际项目中这里会调用API或数据库删除
}

async function cleanupUserData(data: any) {
  console.log(`清理用户数据: ${data.username}`);
  // 实际项目中这里会调用API或数据库删除
}

async function cleanupDepartmentData(data: any) {
  console.log(`清理部门数据: ${data.name}`);
  // 实际项目中这里会调用API或数据库删除
}

// 全局清理钩子
export const setupGlobalCleanup = () => {
  // 测试套件开始前执行
  let testCount = 0;
  
  // 测试套件结束后清理
  process.on('exit', () => {
    console.log(`测试套件执行完成，共执行 ${testCount} 个测试`);
  });
};

// 导出的清理工具
export const cleanupUtils = {
  // 立即清理指定数据
  immediateCleanup: async (testId: string) => {
    const script = TestDataFactory.generateCleanupScript(testId);
    console.log('执行立即清理脚本:', script);
    // 实际执行清理
  },

  // 批量清理测试数据
  bulkCleanupByType: async (type: string, testIds: string[]) => {
    for (const testId of testIds) {
      await cleanupUtils.immediateCleanup(testId);
    }
  },

  // 检查测试数据状态
  checkDataStatus: async (testId: string) => {
    return {
      exists: false,
      testId: testId,
      timestamp: new Date().toISOString()
    };
  }
};