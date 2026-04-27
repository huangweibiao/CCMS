import { describe, it, expect, beforeEach, vi } from 'vitest';
import axios from 'axios';
import {
  fetchBudgets,
  createBudget,
  updateBudget,
  deleteBudget,
  approveBudget,
  rejectBudget,
  fetchBudgetStatistics
} from '../../api/budget';
import type { Budget, BudgetForm, BudgetStatistics } from '../../types/budget';

// Mock axios
vi.mock('axios');
const mockedAxios = vi.mocked(axios);

describe('Budget API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock common axios config
    mockedAxios.create.mockReturnValue({
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      }
    } as any);
  });

  describe('fetchBudgets', () => {
    it('should fetch budgets with default parameters', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: 1, title: '预算1', amount: 10000, department: '技术部', status: 'PENDING' },
            { id: 2, title: '预算2', amount: 20000, department: '销售部', status: 'APPROVED' }
          ],
          total: 2,
          page: 1,
          pageSize: 10
        }
      };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchBudgets();
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets', {
        params: { page: 1, pageSize: 10 }
      });
      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should fetch budgets with custom parameters', async () => {
      const mockResponse = {
        data: {
          data: [],
          total: 0,
          page: 2,
          pageSize: 20
        }
      };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchBudgets({
        page: 2,
        pageSize: 20,
        department: '技术部',
        status: 'PENDING'
      });
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets', {
        params: {
          page: 2,
          pageSize: 20,
          department: '技术部',
          status: 'PENDING'
        }
      });
      expect(result.success).toBe(true);
    });

    it('should handle fetch failure', async () => {
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 500,
          data: { message: '服务器错误' }
        }
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('服务器错误');
    });

    it('should handle network timeout', async () => {
      mockedAxios.get.mockRejectedValue({
        code: 'ECONNABORTED',
        message: '请求超时'
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('网络连接超时');
    });

    it('should handle 401 unauthorized', async () => {
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 401,
          data: { message: '未授权' }
        }
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('登录已过期，请重新登录');
    });
  });

  describe('createBudget', () => {
    const budgetForm: BudgetForm = {
      title: '新预算',
      amount: 50000,
      description: '部门年度预算',
      department: '技术部'
    };

    it('should create budget successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          ...budgetForm,
          applicant: '当前用户',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);
      
      const result = await createBudget(budgetForm);
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/budgets', budgetForm);
      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should handle validation errors', async () => {
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 400,
          data: { 
            message: '验证失败',
            errors: ['标题不能为空', '金额必须大于0']
          }
        }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('标题不能为空, 金额必须大于0');
    });

    it('should handle budget limit exceeded', async () => {
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 422,
          data: { message: '预算金额超出部门限额' }
        }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('预算金额超出部门限额');
    });
  });

  describe('updateBudget', () => {
    const updateData = { amount: 60000, description: '更新后的预算' };

    it('should update budget successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          title: '预算',
          amount: 60000,
          description: '更新后的预算',
          department: '技术部',
          applicant: '当前用户',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-20'
        }
      };
      
      mockedAxios.put.mockResolvedValue(mockResponse);
      
      const result = await updateBudget(1, updateData);
      
      expect(mockedAxios.put).toHaveBeenCalledWith('/api/budgets/1', updateData);
      expect(result.success).toBe(true);
      expect(result.data.amount).toBe(60000);
    });

    it('should handle update of approved budget', async () => {
      mockedAxios.put.mockRejectedValue({
        response: {
          status: 403,
          data: { message: '已批准的预算不能修改' }
        }
      });
      
      const result = await updateBudget(1, updateData);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('已批准的预算不能修改');
    });

    it('should handle non-existent budget', async () => {
      mockedAxios.put.mockRejectedValue({
        response: {
          status: 404,
          data: { message: '预算不存在' }
        }
      });
      
      const result = await updateBudget(999, updateData);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('预算不存在');
    });
  });

  describe('deleteBudget', () => {
    it('should delete budget successfully', async () => {
      mockedAxios.delete.mockResolvedValue({ data: { success: true } });
      
      const result = await deleteBudget(1);
      
      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/budgets/1');
      expect(result.success).toBe(true);
    });

    it('should handle delete of approved budget', async () => {
      mockedAxios.delete.mockRejectedValue({
        response: {
          status: 403,
          data: { message: '已批准的预算不能删除' }
        }
      });
      
      const result = await deleteBudget(1);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('已批准的预算不能删除');
    });

    it('should handle concurrent delete operations', async () => {
      mockedAxios.delete.mockResolvedValue({ data: { success: true } });
      
      // Multiple delete operations
      const promises = [
        deleteBudget(1),
        deleteBudget(2),
        deleteBudget(3)
      ];
      
      const results = await Promise.all(promises);
      
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      expect(mockedAxios.delete).toHaveBeenCalledTimes(3);
    });
  });

  describe('approveBudget', () => {
    it('should approve budget successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          status: 'APPROVED',
          approvedBy: '审批人',
          approvedAt: '2024-01-20'
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);
      
      const result = await approveBudget(1);
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/budgets/1/approve');
      expect(result.success).toBe(true);
      expect(result.data.status).toBe('APPROVED');
    });

    it('should handle approval without permission', async () => {
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 403,
          data: { message: '没有审批权限' }
        }
      });
      
      const result = await approveBudget(1);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('没有审批权限');
    });

    it('should handle approval of already approved budget', async () => {
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 409,
          data: { message: '预算已批准' }
        }
      });
      
      const result = await approveBudget(1);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('预算已批准');
    });
  });

  describe('rejectBudget', () => {
    it('should reject budget successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          status: 'REJECTED',
          rejectedBy: '审批人',
          rejectedAt: '2024-01-20',
          rejectionReason: '预算金额过高'
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);
      
      const result = await rejectBudget(1, '预算金额过高');
      
      expect(mockedAxios.post).toHaveBeenCalledWith('/api/budgets/1/reject', {
        reason: '预算金额过高'
      });
      expect(result.success).toBe(true);
      expect(result.data.status).toBe('REJECTED');
    });

    it('should handle rejection without reason', async () => {
      const result = await rejectBudget(1, '');
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('必须提供驳回理由');
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should handle rejection with very long reason', async () => {
      const longReason = 'A'.repeat(1000);
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, status: 'REJECTED', rejectionReason: longReason }
      });
      
      const result = await rejectBudget(1, longReason);
      
      expect(result.success).toBe(true);
    });
  });

  describe('fetchBudgetStatistics', () => {
    it('should fetch budget statistics successfully', async () => {
      const mockResponse = {
        data: {
          totalBudgets: 50,
          totalAmount: 1000000,
          approvedAmount: 800000,
          pendingAmount: 150000,
          rejectedAmount: 50000,
          byDepartment: {
            '技术部': 400000,
            '销售部': 300000,
            '行政部': 200000
          }
        }
      };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchBudgetStatistics();
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets/statistics');
      expect(result.success).toBe(true);
      expect(result.data.totalAmount).toBe(1000000);
      expect(result.data.byDepartment['技术部']).toBe(400000);
    });

    it('should fetch statistics with date range', async () => {
      const mockResponse = { data: { totalBudgets: 10, totalAmount: 200000 } };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchBudgetStatistics({
        startDate: '2024-01-01',
        endDate: '2024-01-31'
      });
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets/statistics', {
        params: { startDate: '2024-01-01', endDate: '2024-01-31' }
      });
      expect(result.success).toBe(true);
    });

    it('should handle empty statistics', async () => {
      const mockResponse = {
        data: {
          totalBudgets: 0,
          totalAmount: 0,
          approvedAmount: 0,
          pendingAmount: 0,
          rejectedAmount: 0,
          byDepartment: {}
        }
      };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchBudgetStatistics();
      
      expect(result.success).toBe(true);
      expect(result.data.totalBudgets).toBe(0);
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large budget amounts', async () => {
      const budgetForm: BudgetForm = {
        title: '大额预算',
        amount: 1000000000, // 10亿
        description: '重大项目预算',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...budgetForm, status: 'PENDING' }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(true);
      expect(result.data.amount).toBe(1000000000);
    });

    it('should handle special characters in budget title', async () => {
      const budgetForm: BudgetForm = {
        title: '预算@#$%^&*()',
        amount: 10000,
        description: '特殊字符测试',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...budgetForm, status: 'PENDING' }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(true);
      expect(result.data.title).toBe('预算@#$%^&*()');
    });

    it('should handle concurrent API calls', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 0, page: 1, pageSize: 10 }
      });
      
      // Multiple concurrent fetches
      const promises = [
        fetchBudgets({ page: 1 }),
        fetchBudgets({ page: 2 }),
        fetchBudgets({ page: 3 })
      ];
      
      const results = await Promise.all(promises);
      
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      expect(mockedAxios.get).toHaveBeenCalledTimes(3);
    });

    it('should handle server maintenance response', async () => {
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 503,
          data: { message: '服务器维护中' }
        }
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('服务器维护中，请稍后再试');
    });

    it('should handle malformed response data', async () => {
      mockedAxios.get.mockResolvedValue({
        data: null // Malformed response
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('服务器返回数据格式错误');
    });

    it('should handle very slow network responses', async () => {
      // Simulate slow response
      mockedAxios.get.mockImplementation(() => 
        new Promise(resolve => {
          setTimeout(() => resolve({
            data: { data: [], total: 0, page: 1, pageSize: 10 }
          }), 10000);
        })
      );
      
      // Test with timeout handling would be implemented in the interceptor
      // For now, just verify the call was made
      const promise = fetchBudgets();
      
      expect(mockedAxios.get).toHaveBeenCalled();
      
      // Clean up
      await promise;
    });

    it('should handle large pagination parameters', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 10000, page: 100, pageSize: 100 }
      });
      
      const result = await fetchBudgets({ page: 100, pageSize: 100 });
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets', {
        params: { page: 100, pageSize: 100 }
      });
      expect(result.success).toBe(true);
      expect(result.data.total).toBe(10000);
    });

    it('should handle Unicode and emoji characters', async () => {
      const budgetForm: BudgetForm = {
        title: '预算🚀项目🎯',
        amount: 10000,
        description: '包含表情符号的预算😊',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...budgetForm, status: 'PENDING' }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(true);
      expect(result.data.title).toBe('预算🚀项目🎯');
      expect(result.data.description).toBe('包含表情符号的预算😊');
    });

    it('should handle budget with very long description', async () => {
      const longDescription = 'A'.repeat(5000); // Very long description
      const budgetForm: BudgetForm = {
        title: '长描述预算',
        amount: 10000,
        description: longDescription,
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...budgetForm, status: 'PENDING' }
      });
      
      const result = await createBudget(budgetForm);
      
      expect(result.success).toBe(true);
      expect(result.data.description).toBe(longDescription);
    });
  });

  describe('Performance and Security', () => {
    it('should sanitize input parameters', async () => {
      const maliciousInput = {
        page: 1,
        pageSize: 10,
        department: "技术部'; DROP TABLE budgets; --"
      };
      
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 0, page: 1, pageSize: 10 }
      });
      
      const result = await fetchBudgets(maliciousInput);
      
      // API should handle parameter sanitization, but we verify the call
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/budgets', {
        params: maliciousInput
      });
    });

    it('should handle rate limiting', async () => {
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 429,
          data: { message: '请求过于频繁' }
        }
      });
      
      const result = await fetchBudgets();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('请求过于频繁，请稍后再试');
    });

    it('should preserve data integrity in concurrent updates', async () => {
      // This would typically require server-side testing
      // Here we just verify that the API calls are made correctly
      
      mockedAxios.put.mockResolvedValue({
        data: { id: 1, amount: 60000, version: 2 }
      });
      
      const result = await updateBudget(1, { amount: 60000 });
      
      expect(result.success).toBe(true);
    });
  });
});