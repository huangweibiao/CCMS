import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useExpenseStore } from '../../stores/expense';
import { 
  fetchExpenseApplications, 
  createExpenseApplication, 
  updateExpenseApplication,
  deleteExpenseApplication,
  fetchExpenseReimburses,
  createExpenseReimburse,
  updateExpenseReimburse,
  deleteExpenseReimburse
} from '../../api/expense';
import type { ExpenseApplication, ExpenseReimburse, ExpenseForm } from '../../types/expense';

// Mock the expense API
vi.mock('../../api/expense', () => ({
  fetchExpenseApplications: vi.fn(),
  createExpenseApplication: vi.fn(),
  updateExpenseApplication: vi.fn(),
  deleteExpenseApplication: vi.fn(),
  fetchExpenseReimburses: vi.fn(),
  createExpenseReimburse: vi.fn(),
  updateExpenseReimburse: vi.fn(),
  deleteExpenseReimburse: vi.fn(),
}));

describe('Expense Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('Initial State', () => {
    it('should initialize with default values', () => {
      const expenseStore = useExpenseStore();
      
      expect(expenseStore.expenseApplications).toEqual([]);
      expect(expenseStore.expenseReimburses).toEqual([]);
      expect(expenseStore.loading).toBe(false);
      expect(expenseStore.error).toBe('');
      expect(expenseStore.currentPage).toBe(1);
      expect(expenseStore.pageSize).toBe(10);
      expect(expenseStore.total).toBe(0);
    });
  });

  describe('Expense Applications', () => {
    it('should load expense applications successfully', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '项目费用申请',
          amount: 5000,
          description: '项目开发相关费用',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      const mockResponse = {
        data: mockApplications,
        total: 1,
        page: 1,
        pageSize: 10
      };
      
      (fetchExpenseApplications as any).mockResolvedValue(mockResponse);
      
      const expenseStore = useExpenseStore();
      const result = await expenseStore.loadExpenseApplications({ page: 1, pageSize: 10 });
      
      expect(fetchExpenseApplications).toHaveBeenCalledWith({ page: 1, pageSize: 10 });
      expect(expenseStore.expenseApplications).toEqual(mockApplications);
      expect(expenseStore.total).toBe(1);
      expect(result.success).toBe(true);
    });

    it('should create expense application successfully', async () => {
      const expenseForm: ExpenseForm = {
        title: '新费用申请',
        amount: 3000,
        description: '办公用品采购',
        department: '行政部'
      };
      
      const mockApplication: ExpenseApplication = {
        id: 1,
        ...expenseForm,
        applicant: '当前用户',
        status: 'PENDING',
        createdAt: '2024-01-20',
        updatedAt: '2024-01-20'
      };
      
      (createExpenseApplication as any).mockResolvedValue(mockApplication);
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [];
      
      const result = await expenseStore.createExpenseApplication(expenseForm);
      
      expect(createExpenseApplication).toHaveBeenCalledWith(expenseForm);
      expect(expenseStore.expenseApplications).toContainEqual(mockApplication);
      expect(result.success).toBe(true);
    });

    it('should update expense application successfully', async () => {
      const existingApplication: ExpenseApplication = {
        id: 1,
        title: '原申请',
        amount: 3000,
        description: '原描述',
        department: '技术部',
        applicant: '张三',
        status: 'PENDING',
        createdAt: '2024-01-15',
        updatedAt: '2024-01-15'
      };
      
      const updateData = { amount: 4000, description: '更新描述' };
      const updatedApplication = { ...existingApplication, ...updateData };
      
      (updateExpenseApplication as any).mockResolvedValue(updatedApplication);
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [existingApplication];
      
      const result = await expenseStore.updateExpenseApplication(1, updateData);
      
      expect(updateExpenseApplication).toHaveBeenCalledWith(1, updateData);
      expect(expenseStore.expenseApplications).toContainEqual(updatedApplication);
      expect(result.success).toBe(true);
    });

    it('should delete expense application successfully', async () => {
      const applicationToDelete: ExpenseApplication = {
        id: 1,
        title: '待删除申请',
        amount: 3000,
        description: '测试删除',
        department: '技术部',
        applicant: '张三',
        status: 'PENDING',
        createdAt: '2024-01-15',
        updatedAt: '2024-01-15'
      };
      
      (deleteExpenseApplication as any).mockResolvedValue({ success: true });
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [applicationToDelete];
      
      const result = await expenseStore.deleteExpenseApplication(1);
      
      expect(deleteExpenseApplication).toHaveBeenCalledWith(1);
      expect(expenseStore.expenseApplications).not.toContainEqual(applicationToDelete);
      expect(result.success).toBe(true);
    });
  });

  describe('Expense Reimburses', () => {
    it('should load expense reimburses successfully', async () => {
      const mockReimburses: ExpenseReimburse[] = [
        {
          id: 1,
          title: '差旅费报销',
          amount: 2000,
          description: '北京出差费用',
          department: '销售部',
          applicant: '李四',
          status: 'APPROVED',
          createdAt: '2024-01-10',
          updatedAt: '2024-01-10'
        }
      ];
      
      const mockResponse = {
        data: mockReimburses,
        total: 1,
        page: 1,
        pageSize: 10
      };
      
      (fetchExpenseReimburses as any).mockResolvedValue(mockResponse);
      
      const expenseStore = useExpenseStore();
      const result = await expenseStore.loadExpenseReimburses({ page: 1, pageSize: 10 });
      
      expect(fetchExpenseReimburses).toHaveBeenCalledWith({ page: 1, pageSize: 10 });
      expect(expenseStore.expenseReimburses).toEqual(mockReimburses);
      expect(result.success).toBe(true);
    });

    it('should create expense reimburse successfully', async () => {
      const reimburseForm: ExpenseForm = {
        title: '新报销申请',
        amount: 1500,
        description: '客户招待费用',
        department: '销售部'
      };
      
      const mockReimburse: ExpenseReimburse = {
        id: 1,
        ...reimburseForm,
        applicant: '当前用户',
        status: 'PENDING',
        createdAt: '2024-01-20',
        updatedAt: '2024-01-20'
      };
      
      (createExpenseReimburse as any).mockResolvedValue(mockReimburse);
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseReimburses = [];
      
      const result = await expenseStore.createExpenseReimburse(reimburseForm);
      
      expect(createExpenseReimburse).toHaveBeenCalledWith(reimburseForm);
      expect(expenseStore.expenseReimburses).toContainEqual(mockReimburse);
      expect(result.success).toBe(true);
    });

    it('should update expense reimburse successfully', async () => {
      const existingReimburse: ExpenseReimburse = {
        id: 1,
        title: '原报销',
        amount: 1500,
        description: '原描述',
        department: '销售部',
        applicant: '李四',
        status: 'PENDING',
        createdAt: '2024-01-10',
        updatedAt: '2024-01-10'
      };
      
      const updateData = { amount: 1800, description: '更新描述' };
      const updatedReimburse = { ...existingReimburse, ...updateData };
      
      (updateExpenseReimburse as any).mockResolvedValue(updatedReimburse);
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseReimburses = [existingReimburse];
      
      const result = await expenseStore.updateExpenseReimburse(1, updateData);
      
      expect(updateExpenseReimburse).toHaveBeenCalledWith(1, updateData);
      expect(expenseStore.expenseReimburses).toContainEqual(updatedReimburse);
      expect(result.success).toBe(true);
    });

    it('should delete expense reimburse successfully', async () => {
      const reimburseToDelete: ExpenseReimburse = {
        id: 1,
        title: '待删除报销',
        amount: 1500,
        description: '测试删除',
        department: '销售部',
        applicant: '李四',
        status: 'PENDING',
        createdAt: '2024-01-10',
        updatedAt: '2024-01-10'
      };
      
      (deleteExpenseReimburse as any).mockResolvedValue({ success: true });
      
      const expenseStore = useExpenseStore();
      expenseStore.expenseReimburses = [reimburseToDelete];
      
      const result = await expenseStore.deleteExpenseReimburse(1);
      
      expect(deleteExpenseReimburse).toHaveBeenCalledWith(1);
      expect(expenseStore.expenseReimburses).not.toContainEqual(reimburseToDelete);
      expect(result.success).toBe(true);
    });
  });

  describe('Getters', () => {
    beforeEach(() => {
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [
        {
          id: 1,
          title: '项目费用申请',
          amount: 5000,
          description: '项目开发',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        },
        {
          id: 2,
          title: '办公费用申请',
          amount: 3000,
          description: '办公用品',
          department: '行政部',
          applicant: '李四',
          status: 'APPROVED',
          createdAt: '2024-01-10',
          updatedAt: '2024-01-12'
        },
        {
          id: 3,
          title: '培训费用申请',
          amount: 8000,
          description: '员工培训',
          department: '人事部',
          applicant: '王五',
          status: 'REJECTED',
          createdAt: '2024-01-05',
          updatedAt: '2024-01-08'
        }
      ];
      
      expenseStore.expenseReimburses = [
        {
          id: 1,
          title: '差旅费报销',
          amount: 2000,
          description: '北京出差',
          department: '销售部',
          applicant: '赵六',
          status: 'PENDING',
          createdAt: '2024-01-18',
          updatedAt: '2024-01-18'
        },
        {
          id: 2,
          title: '招待费报销',
          amount: 1500,
          description: '客户招待',
          department: '销售部',
          applicant: '赵六',
          status: 'APPROVED',
          createdAt: '2024-01-12',
          updatedAt: '2024-01-14'
        }
      ];
    });

    describe('pendingApplications', () => {
      it('should return only pending expense applications', () => {
        const expenseStore = useExpenseStore();
        const pending = expenseStore.pendingApplications;
        
        expect(pending).toHaveLength(1);
        expect(pending[0].status).toBe('PENDING');
        expect(pending[0].id).toBe(1);
      });
    });

    describe('approvedApplications', () => {
      it('should return only approved expense applications', () => {
        const expenseStore = useExpenseStore();
        const approved = expenseStore.approvedApplications;
        
        expect(approved).toHaveLength(1);
        expect(approved[0].status).toBe('APPROVED');
        expect(approved[0].id).toBe(2);
      });
    });

    describe('rejectedApplications', () => {
      it('should return only rejected expense applications', () => {
        const expenseStore = useExpenseStore();
        const rejected = expenseStore.rejectedApplications;
        
        expect(rejected).toHaveLength(1);
        expect(rejected[0].status).toBe('REJECTED');
        expect(rejected[0].id).toBe(3);
      });
    });

    describe('pendingReimburses', () => {
      it('should return only pending expense reimburses', () => {
        const expenseStore = useExpenseStore();
        const pending = expenseStore.pendingReimburses;
        
        expect(pending).toHaveLength(1);
        expect(pending[0].status).toBe('PENDING');
        expect(pending[0].id).toBe(1);
      });
    });

    describe('approvedReimburses', () => {
      it('should return only approved expense reimburses', () => {
        const expenseStore = useExpenseStore();
        const approved = expenseStore.approvedReimburses;
        
        expect(approved).toHaveLength(1);
        expect(approved[0].status).toBe('APPROVED');
        expect(approved[0].id).toBe(2);
      });
    });

    describe('totalApplicationAmount', () => {
      it('should calculate total amount of all applications', () => {
        const expenseStore = useExpenseStore();
        const total = expenseStore.totalApplicationAmount;
        
        expect(total).toBe(5000 + 3000 + 8000);
      });
    });

    describe('totalReimburseAmount', () => {
      it('should calculate total amount of all reimburses', () => {
        const expenseStore = useExpenseStore();
        const total = expenseStore.totalReimburseAmount;
        
        expect(total).toBe(2000 + 1500);
      });
    });

    describe('applicationsByDepartment', () => {
      it('should group applications by department', () => {
        const expenseStore = useExpenseStore();
        const grouped = expenseStore.applicationsByDepartment;
        
        expect(grouped['技术部']).toHaveLength(1);
        expect(grouped['行政部']).toHaveLength(1);
        expect(grouped['人事部']).toHaveLength(1);
      });
    });

    describe('reimbursesByDepartment', () => {
      it('should group reimburses by department', () => {
        const expenseStore = useExpenseStore();
        const grouped = expenseStore.reimbursesByDepartment;
        
        expect(grouped['销售部']).toHaveLength(2);
      });
    });

    describe('recentApplications', () => {
      it('should return recent applications sorted by date', () => {
        const expenseStore = useExpenseStore();
        const recent = expenseStore.recentApplications;
        
        expect(recent).toHaveLength(3);
        // Should be sorted by createdAt descending
        expect(recent[0].id).toBe(1); // 2024-01-15
        expect(recent[1].id).toBe(2); // 2024-01-10
        expect(recent[2].id).toBe(3); // 2024-01-05
      });
    });

    describe('recentReimburses', () => {
      it('should return recent reimburses sorted by date', () => {
        const expenseStore = useExpenseStore();
        const recent = expenseStore.recentReimburses;
        
        expect(recent).toHaveLength(2);
        // Should be sorted by createdAt descending
        expect(recent[0].id).toBe(1); // 2024-01-18
        expect(recent[1].id).toBe(2); // 2024-01-12
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large expense amounts', async () => {
      const expenseForm: ExpenseForm = {
        title: '大额费用申请',
        amount: 10000000, // 1000万
        description: '重大项目费用',
        department: '技术部'
      };
      
      const mockApplication: ExpenseApplication = {
        id: 1,
        ...expenseForm,
        applicant: '当前用户',
        status: 'PENDING',
        createdAt: '2024-01-20',
        updatedAt: '2024-01-20'
      };
      
      (createExpenseApplication as any).mockResolvedValue(mockApplication);
      
      const expenseStore = useExpenseStore();
      const result = await expenseStore.createExpenseApplication(expenseForm);
      
      expect(result.success).toBe(true);
      expect(result.data.amount).toBe(10000000);
    });

    it('should handle concurrent operations', async () => {
      const expenseForm: ExpenseForm = {
        title: '测试申请',
        amount: 1000,
        description: '测试',
        department: '测试部'
      };
      
      (createExpenseApplication as any).mockResolvedValue({
        id: 1,
        ...expenseForm,
        applicant: '当前用户',
        status: 'PENDING',
        createdAt: '2024-01-20',
        updatedAt: '2024-01-20'
      });
      
      const expenseStore = useExpenseStore();
      
      // Start multiple concurrent create operations
      const promises = [
        expenseStore.createExpenseApplication(expenseForm),
        expenseStore.createExpenseApplication(expenseForm),
        expenseStore.createExpenseApplication(expenseForm)
      ];
      
      const results = await Promise.all(promises);
      
      // All should succeed
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      // Should have created 3 applications
      expect(expenseStore.expenseApplications).toHaveLength(3);
    });

    it('should handle empty lists gracefully', () => {
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [];
      expenseStore.expenseReimburses = [];
      
      expect(expenseStore.pendingApplications).toEqual([]);
      expect(expenseStore.approvedApplications).toEqual([]);
      expect(expenseStore.rejectedApplications).toEqual([]);
      expect(expenseStore.pendingReimburses).toEqual([]);
      expect(expenseStore.approvedReimburses).toEqual([]);
      expect(expenseStore.totalApplicationAmount).toBe(0);
      expect(expenseStore.totalReimburseAmount).toBe(0);
    });

    it('should handle null and undefined values in data', () => {
      const expenseStore = useExpenseStore();
      expenseStore.expenseApplications = [
        {
          id: 1,
          title: '测试申请',
          amount: 1000,
          description: null as any,
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-20',
          updatedAt: '2024-01-20'
        }
      ];
      
      // Should not crash when accessing getters
      expect(expenseStore.pendingApplications).toHaveLength(1);
      expect(expenseStore.pendingApplications[0].description).toBeNull();
    });
  });
});