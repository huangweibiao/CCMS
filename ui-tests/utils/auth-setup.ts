import { APIRequestContext, request } from '@playwright/test';

/**
 * 认证设置和测试数据管理
 */
export class AuthSetup {
  private static apiContext: APIRequestContext;

  /**
   * 初始化API上下文
   */
  static async initApiContext(): Promise<void> {
    if (!this.apiContext) {
      this.apiContext = await request.newContext({
        baseURL: 'http://localhost:8080', // 后端API地址
        extraHTTPHeaders: {
          'Content-Type': 'application/json',
        },
      });
    }
  }

  /**
   * 获取管理员Token
   */
  static async getAdminToken(): Promise<string> {
    await this.initApiContext();
    
    const response = await this.apiContext.post('/api/auth/login', {
      data: {
        username: 'admin@example.com',
        password: 'admin123'
      }
    });
    
    const data = await response.json();
    return data.token;
  }

  /**
   * 获取审批员Token
   */
  static async getApproverToken(): Promise<string> {
    await this.initApiContext();
    
    const response = await this.apiContext.post('/api/auth/login', {
      data: {
        username: 'approver@example.com',
        password: 'approver123'
      }
    });
    
    const data = await response.json();
    return data.token;
  }

  /**
   * 创建测试费用申请
   */
  static async createTestExpense(token: string): Promise<any> {
    await this.initApiContext();
    
    const response = await this.apiContext.post('/api/expense/application', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      data: {
        title: '自动化测试费用申请',
        amount: 500,
        category: '办公用品',
        description: '用于UI自动化测试的费用申请'
      }
    });
    
    return await response.json();
  }

  /**
   * 创建测试预算
   */
  static async createTestBudget(token: string): Promise<any> {
    await this.initApiContext();
    
    const response = await this.apiContext.post('/api/budget/create', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      data: {
        name: '自动化测试预算',
        amount: 10000,
        period: '2024-Q1',
        department: '技术部'
      }
    });
    
    return await response.json();
  }

  /**
   * 创建测试借款
   */
  static async createTestLoan(token: string): Promise<any> {
    await this.initApiContext();
    
    const response = await this.apiContext.post('/api/loan/apply', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      data: {
        purpose: '自动化测试借款',
        amount: 2000,
        repaymentPeriod: 6
      }
    });
    
    return await response.json();
  }

  /**
   * 清理测试数据
   */
  static async cleanupTestData(token: string, resourceType: string, resourceId: string): Promise<void> {
    await this.initApiContext();
    
    await this.apiContext.delete(`/api/${resourceType}/${resourceId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  /**
   * 验证API响应
   */
  static async verifyApiResponse(response: any, expectedStatus = 200): Promise<void> {
    if (response.status !== expectedStatus) {
      throw new Error(`API响应状态错误: ${response.status}, 预期: ${expectedStatus}`);
    }
  }

  /**
   * 设置测试Cookie
   */
  static async setTestCookie(page: any, token: string): Promise<void> {
    await page.context().addCookies([
      {
        name: 'auth_token',
        value: token,
        domain: 'localhost',
        path: '/'
      }
    ]);
  }

  /**
   * 清除所有测试数据
   */
  static async cleanupAllTestData(): Promise<void> {
    // 在实际项目中，这里会调用专门的清理接口
    // 暂时使用延时模拟清理过程
    await new Promise(resolve => setTimeout(resolve, 1000));
  }
}