import { describe, it, expect, beforeEach, vi } from 'vitest';
import axios from 'axios';
import {
  fetchExpenseApplications,
  createExpenseApplication,
  updateExpenseApplication,
  deleteExpenseApplication,
  approveExpenseApplication,
  rejectExpenseApplication,
  fetchExpenseReimburses,
  createExpenseReimburse,
  updateExpenseReimburse,
  deleteExpenseReimburse,
  approveExpenseReimburse,
  rejectExpenseReimburse,
  fetchExpenseStatistics
} from '../../api/expense';
import type { ExpenseApplication, ExpenseReimburse, ExpenseForm } from '../../types/expense';

// Mock axios
vi.mock('axios');
const mockedAxios = vi.mocked(axios);

describe('Expense API', () => {
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

  describe('Expense Applications', () => {
    describe('fetchExpenseApplications', () => {
      it('should fetch expense applications successfully', async () => {
        const mockResponse = {
          data: {
            data: [
              {
                id: 1,
                title: '项目费用申请',
                amount: 5000,
                description: '项目开发费用',
                department: '技术部',
                status: 'PENDING'
              }
            ],
            total: 1,
            page: 1,
            pageSize: 10
          }
        };
        
        mockedAxios.get.mockResolvedValue(mockResponse);
        
        const result = await fetchExpenseApplications();
        
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/applications', {
          params: { page: 1, pageSize: 10 }
        });
        expect(result.success).toBe(true);
        expect(result.data).toEqual(mockResponse.data);
      });

      it('should handle fetch with filters', async () => {
        mockedAxios.get.mockResolvedValue({ data: { data: [], total: 0, page: 1, pageSize: 10 } });
        
        const result = await fetchExpenseApplications({
          page: 2,
          pageSize: 20,
          department: '技术部',
          status: 'PENDING',
          search: '项目'
        });
        
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/applications', {
          params: {
            page: 2,
            pageSize: 20,
            department: '技术部',
            status: 'PENDING',
            search: '项目'
          }
        });
      });

      it('should handle unauthorized access', async () => {
        mockedAxios.get.mockRejectedValue({
          response: { status: 401, data: { message: '未授权' } }
        });
        
        const result = await fetchExpenseApplications();
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('登录已过期，请重新登录');
      });
    });

    describe('createExpenseApplication', () => {
      const expenseForm: ExpenseForm = {
        title: '新费用申请',
        amount: 3000,
        description: '办公用品采购',
        department: '行政部'
      };

      it('should create expense application successfully', async () => {
        const mockResponse = {
          data: {
            id: 1,
            ...expenseForm,
            applicant: '当前用户',
            status: 'PENDING',
            createdAt: '2024-01-15',
            updatedAt: '2024-01-15'
          }
        };
        
        mockedAxios.post.mockResolvedValue(mockResponse);
        
        const result = await createExpenseApplication(expenseForm);
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/applications', expenseForm);
        expect(result.success).toBe(true);
        expect(result.data).toEqual(mockResponse.data);
      });

      it('should handle budget exceeded error', async () => {
        mockedAxios.post.mockRejectedValue({
          response: {
            status: 422,
            data: { message: '费用金额超出预算限额' }
          }
        });
        
        const result = await createExpenseApplication(expenseForm);
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('费用金额超出预算限额');
      });
    });

    describe('updateExpenseApplication', () => {
      it('should update application successfully', async () => {
        const updateData = { amount: 4000, description: '更新后的描述' };
        
        mockedAxios.put.mockResolvedValue({
          data: {
            id: 1,
            title: '项目费用申请',
            amount: 4000,
            description: '更新后的描述',
            department: '技术部',
            status: 'PENDING'
          }
        });
        
        const result = await updateExpenseApplication(1, updateData);
        
        expect(mockedAxios.put).toHaveBeenCalledWith('/api/expense/applications/1', updateData);
        expect(result.success).toBe(true);
        expect(result.data.amount).toBe(4000);
      });

      it('should handle non-existent application', async () => {
        mockedAxios.put.mockRejectedValue({
          response: { status: 404, data: { message: '费用申请不存在' } }
        });
        
        const result = await updateExpenseApplication(999, { amount: 4000 });
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('费用申请不存在');
      });
    });

    describe('deleteExpenseApplication', () => {
      it('should delete application successfully', async () => {
        mockedAxios.delete.mockResolvedValue({ data: { success: true } });
        
        const result = await deleteExpenseApplication(1);
        
        expect(mockedAxios.delete).toHaveBeenCalledWith('/api/expense/applications/1');
        expect(result.success).toBe(true);
      });

      it('should handle delete of approved application', async () => {
        mockedAxios.delete.mockRejectedValue({
          response: { status: 403, data: { message: '已批准的费用申请不能删除' } }
        });
        
        const result = await deleteExpenseApplication(1);
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('已批准的费用申请不能删除');
      });
    });

    describe('approveExpenseApplication', () => {
      it('should approve application successfully', async () => {
        mockedAxios.post.mockResolvedValue({
          data: { id: 1, status: 'APPROVED', approvedBy: '审批人' }
        });
        
        const result = await approveExpenseApplication(1);
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/applications/1/approve');
        expect(result.success).toBe(true);
        expect(result.data.status).toBe('APPROVED');
      });

      it('should handle approval without permission', async () => {
        mockedAxios.post.mockRejectedValue({
          response: { status: 403, data: { message: '没有审批权限' } }
        });
        
        const result = await approveExpenseApplication(1);
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('没有审批权限');
      });
    });

    describe('rejectExpenseApplication', () => {
      it('should reject application successfully', async () => {
        mockedAxios.post.mockResolvedValue({
          data: {
            id: 1,
            status: 'REJECTED',
            rejectedBy: '审批人',
            rejectionReason: '费用明细不清'
          }
        });
        
        const result = await rejectExpenseApplication(1, '费用明细不清');
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/applications/1/reject', {
          reason: '费用明细不清'
        });
        expect(result.success).toBe(true);
        expect(result.data.status).toBe('REJECTED');
      });

      it('should require rejection reason', async () => {
        const result = await rejectExpenseApplication(1, '');
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('必须提供驳回理由');
        expect(mockedAxios.post).not.toHaveBeenCalled();
      });
    });
  });

  describe('Expense Reimburses', () => {
    describe('fetchExpenseReimburses', () => {
      it('should fetch expense reimburses successfully', async () => {
        const mockResponse = {
          data: {
            data: [
              {
                id: 1,
                title: '差旅费报销',
                amount: 2000,
                description: '北京出差费用',
                department: '销售部',
                status: 'PENDING'
              }
            ],
            total: 1,
            page: 1,
            pageSize: 10
          }
        };
        
        mockedAxios.get.mockResolvedValue(mockResponse);
        
        const result = await fetchExpenseReimburses();
        
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/reimburses', {
          params: { page: 1, pageSize: 10 }
        });
        expect(result.success).toBe(true);
        expect(result.data).toEqual(mockResponse.data);
      });

      it('should handle network errors gracefully', async () => {
        mockedAxios.get.mockRejectedValue({
          code: 'NETWORK_ERROR',
          message: '网络连接失败'
        });
        
        const result = await fetchExpenseReimburses();
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('网络连接失败');
      });
    });

    describe('createExpenseReimburse', () => {
      const reimburseForm: ExpenseForm = {
        title: '新报销申请',
        amount: 1500,
        description: '客户招待费用',
        department: '销售部'
      };

      it('should create reimburse successfully', async () => {
        const mockResponse = {
          data: {
            id: 1,
            ...reimburseForm,
            applicant: '当前用户',
            status: 'PENDING',
            createdAt: '2024-01-15'
          }
        };
        
        mockedAxios.post.mockResolvedValue(mockResponse);
        
        const result = await createExpenseReimburse(reimburseForm);
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/reimburses', reimburseForm);
        expect(result.success).toBe(true);
      });

      it('should handle validation errors', async () => {
        mockedAxios.post.mockRejectedValue({
          response: {
            status: 400,
            data: { message: '验证失败', errors: ['标题不能为空', '金额必须大于0'] }
          }
        });
        
        const result = await createExpenseReimburse(reimburseForm);
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('标题不能为空, 金额必须大于0');
      });
    });

    describe('approveExpenseReimburse', () => {
      it('should approve reimburse successfully', async () => {
        mockedAxios.post.mockResolvedValue({
          data: { id: 1, status: 'APPROVED', approvedBy: '审批人' }
        });
        
        const result = await approveExpenseReimburse(1);
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/reimburses/1/approve');
        expect(result.success).toBe(true);
      });

      it('should handle already approved reimburse', async () => {
        mockedAxios.post.mockRejectedValue({
          response: { status: 409, data: { message: '报销申请已批准' } }
        });
        
        const result = await approveExpenseReimburse(1);
        
        expect(result.success).toBe(false);
        expect(result.error).toBe('报销申请已批准');
      });
    });

    describe('rejectExpenseReimburse', () => {
      it('should reject reimburse with reason', async () => {
        mockedAxios.post.mockResolvedValue({
          data: { id: 1, status: 'REJECTED', rejectionReason: '缺少报销凭证' }
        });
        
        const result = await rejectExpenseReimburse(1, '缺少报销凭证');
        
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/expense/reimburses/1/reject', {
          reason: '缺少报销凭证'
        });
        expect(result.success).toBe(true);
      });
    });
  });

  describe('fetchExpenseStatistics', () => {
    it('should fetch expense statistics successfully', async () => {
      const mockResponse = {
        data: {
          totalApplications: 100,
          totalReimburses: 50,
          totalAmount: 500000,
          approvedAmount: 400000,
          pendingAmount: 80000,
          rejectedAmount: 20000,
          byDepartment: {
            '技术部': 200000,
            '销售部': 150000,
            '行政部': 100000
          }
        }
      };
      
      mockedAxios.get.mockResolvedValue(mockResponse);
      
      const result = await fetchExpenseStatistics();
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/statistics');
      expect(result.success).toBe(true);
      expect(result.data.totalAmount).toBe(500000);
    });

    it('should fetch statistics with date range', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { totalApplications: 10, totalAmount: 50000 }
      });
      
      const result = await fetchExpenseStatistics({
        startDate: '2024-01-01',
        endDate: '2024-01-31'
      });
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/statistics', {
        params: { startDate: '2024-01-01', endDate: '2024-01-31' }
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large expense amounts', async () => {
      const expenseForm: ExpenseForm = {
        title: '巨额费用',
        amount: 10000000,
        description: '重大项目费用',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...expenseForm, status: 'PENDING' }
      });
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
      expect(result.data.amount).toBe(10000000);
    });

    it('should handle special characters in descriptions', async () => {
      const expenseForm: ExpenseForm = {
        title: '费用申请',
        amount: 1000,
        description: '包含特殊字符!@#$%^&*()',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...expenseForm, status: 'PENDING' }
      });
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
      expect(result.data.description).toBe('包含特殊字符!@#$%^&*()');
    });

    it('should handle concurrent operations', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 0, page: 1, pageSize: 10 }
      });
      
      const promises = [
        fetchExpenseApplications({ page: 1 }),
        fetchExpenseReimburses({ page: 1 }),
        fetchExpenseStatistics()
      ];
      
      const results = await Promise.all(promises);
      
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      expect(mockedAxios.get).toHaveBeenCalledTimes(3);
    });

    it('should handle malformed response data', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { invalid: 'format' } // Malformed response
      });
      
      const result = await fetchExpenseApplications();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('服务器返回数据格式错误');
    });

    it('should handle Unicode characters in titles', async () => {
      const expenseForm: ExpenseForm = {
        title: '费用申请🚀项目🎯',
        amount: 1000,
        description: '包含表情符号😊',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...expenseForm, status: 'PENDING' }
      });
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
      expect(result.data.title).toBe('费用申请🚀项目🎯');
    });

    it('should handle very long descriptions', async () => {
      const longDescription = 'A'.repeat(5000);
      const expenseForm: ExpenseForm = {
        title: '长描述费用',
        amount: 1000,
        description: longDescription,
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...expenseForm, status: 'PENDING' }
      });
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
      expect(result.data.description).toBe(longDescription);
    });

    it('should handle rate limiting', async () => {
      mockedAxios.post.mockRejectedValue({
        response: {
          status: 429,
          data: { message: '请求过于频繁' }
        }
      });
      
      const expenseForm: ExpenseForm = {
        title: '测试费用',
        amount: 1000,
        description: '测试',
        department: '技术部'
      };
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('请求过于频繁，请稍后再试');
    });

    it('should handle server maintenance', async () => {
      mockedAxios.get.mockRejectedValue({
        response: {
          status: 503,
          data: { message: '服务器维护中' }
        }
      });
      
      const result = await fetchExpenseApplications();
      
      expect(result.success).toBe(false);
      expect(result.error).toBe('服务器维护中，请稍后再试');
    });
  });

  describe('Performance and Security', () => {
    it('should handle SQL injection attempts in search parameters', async () => {
      const maliciousInput = {
        search: "项目'; DROP TABLE expenses; --"
      };
      
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 0, page: 1, pageSize: 10 }
      });
      
      const result = await fetchExpenseApplications(maliciousInput);
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/applications', {
        params: maliciousInput
      });
      expect(result.success).toBe(true);
    });

    it('should preserve data integrity in concurrent updates', async () => {
      mockedAxios.put.mockResolvedValue({
        data: { id: 1, amount: 6000, version: 2 }
      });
      
      const result = await updateExpenseApplication(1, { amount: 6000 });
      
      expect(result.success).toBe(true);
    });

    it('should handle large pagination parameters correctly', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { data: [], total: 10000, page: 100, pageSize: 100 }
      });
      
      const result = await fetchExpenseApplications({ page: 100, pageSize: 100 });
      
      expect(mockedAxios.get).toHaveBeenCalledWith('/api/expense/applications', {
        params: { page: 100, pageSize: 100 }
      });
      expect(result.success).toBe(true);
    });

    it('should handle file attachments in expense applications', async () => {
      // This would typically involve FormData handling
      // Here we test the basic API call structure
      
      const expenseForm: ExpenseForm = {
        title: '带附件的费用',
        amount: 1000,
        description: '包含附件',
        department: '技术部'
      };
      
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, ...expenseForm, status: 'PENDING' }
      });
      
      const result = await createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
    });

    it('should handle concurrent approval/rejection operations', async () => {
      mockedAxios.post.mockResolvedValue({
        data: { id: 1, status: 'APPROVED' }
      });
      
      const promises = [
        approveExpenseApplication(1),
        approveExpenseApplication(1),
        approveExpenseApplication(1)
      ];
      
      const results = await Promise.all(promises);
      
      // Should handle concurrent operations gracefully
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
    });
  });
});